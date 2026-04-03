package org.justme.justPlugin.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Self-hosted config web editor.
 * Runs a lightweight HTTP server that serves a beautiful SPA for editing ALL plugin config files.
 * Changes are staged with a session code that must be applied in-game via /applyedits.
 *
 * Supported config files:
 * - config.yml (main config)
 * - motd.yml
 * - scoreboard.yml
 * - stats.yml
 * - icon.yml
 * - maintenance/config.yml
 * - texts/general.yml, texts/teleport.yml, texts/economy.yml, etc. (15 text files)
 *
 * Security measures:
 * - Authentication via a secret token generated on startup (printed to console)
 * - Default bind to 127.0.0.1 (localhost only)
 * - No wildcard CORS (same-origin only)
 * - Request body size limit (1 MB)
 * - Rate limiting per IP (30 requests/minute)
 * - Sensitive config keys are redacted in API responses
 * - Cryptographically secure session code generation
 */
@SuppressWarnings("unused")
public class WebEditorManager {

    private final JustPlugin plugin;
    private HttpServer server;
    private final Map<String, PendingSession> sessions = new ConcurrentHashMap<>();
    private SchedulerUtil.CancellableTask cleanupTask;

    /** Cryptographically secure RNG for session codes and auth token. */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** Secret auth token generated on each startup. Required for all API requests. */
    private String authToken;

    /** Rate limiting: tracks request count per IP per minute. */
    private final Map<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private static final int RATE_LIMIT_MAX = 30; // max requests per IP per minute

    /** Maximum request body size (1 MB). */
    private static final int MAX_BODY_SIZE = 1_048_576;

    /** Config keys whose values are redacted in API responses. */
    private static final Set<String> REDACTED_KEYS = Set.of(
            "discord-webhook.url"
    );

    /** All known config file identifiers mapped to their relative paths. */
    private static final LinkedHashMap<String, String> CONFIG_FILES = new LinkedHashMap<>();
    static {
        CONFIG_FILES.put("config", "config.yml");
        CONFIG_FILES.put("motd", "motd.yml");
        CONFIG_FILES.put("scoreboard", "scoreboard.yml");
        CONFIG_FILES.put("stats", "stats.yml");
        CONFIG_FILES.put("icon", "icon.yml");
        CONFIG_FILES.put("maintenance", "maintenance/config.yml");
        CONFIG_FILES.put("texts-general", "texts/general.yml");
        CONFIG_FILES.put("texts-teleport", "texts/teleport.yml");
        CONFIG_FILES.put("texts-warp", "texts/warp.yml");
        CONFIG_FILES.put("texts-home", "texts/home.yml");
        CONFIG_FILES.put("texts-economy", "texts/economy.yml");
        CONFIG_FILES.put("texts-moderation", "texts/moderation.yml");
        CONFIG_FILES.put("texts-player", "texts/player.yml");
        CONFIG_FILES.put("texts-chat", "texts/chat.yml");
        CONFIG_FILES.put("texts-inventory", "texts/inventory.yml");
        CONFIG_FILES.put("texts-world", "texts/world.yml");
        CONFIG_FILES.put("texts-team", "texts/team.yml");
        CONFIG_FILES.put("texts-trade", "texts/trade.yml");
        CONFIG_FILES.put("texts-maintenance", "texts/maintenance.yml");
        CONFIG_FILES.put("texts-info", "texts/info.yml");
        CONFIG_FILES.put("texts-misc", "texts/misc.yml");
        CONFIG_FILES.put("texts-kits", "texts/kits.yml");
        CONFIG_FILES.put("texts-nick", "texts/nick.yml");
        CONFIG_FILES.put("database", "database.yml");
        CONFIG_FILES.put("automessages", "automessages.yml");
    }

    /**
     * Represents a pending config edit session.
     */
    public static class PendingSession {
        public final String code;
        public final String fileId;
        public final Map<String, Object> changes;
        public final long createdAt;
        public final long expiresAt;

        public PendingSession(String code, String fileId, Map<String, Object> changes) {
            this.code = code;
            this.fileId = fileId;
            this.changes = changes;
            this.createdAt = System.currentTimeMillis();
            this.expiresAt = this.createdAt + (10 * 60 * 1000); // 10 minutes
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public WebEditorManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the web editor HTTP server if enabled in config.
     */
    public boolean start() {
        if (!plugin.getConfig().getBoolean("web-editor.enabled", false)) {
            return false;
        }

        int port = plugin.getConfig().getInt("web-editor.port", 8585);
        String bindAddress = plugin.getConfig().getString("web-editor.bind-address", "127.0.0.1");

        // Generate a new auth token for this session
        authToken = generateAuthToken();

        try {
            server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            // Serve the web editor page
            server.createContext("/", this::handlePage);
            // API endpoints
            server.createContext("/api/config", this::handleConfigApi);
            // File list endpoint
            server.createContext("/api/files", this::handleFilesApi);

            server.start();

            // Schedule cleanup of expired sessions and rate limit counters (every 60 seconds)
            cleanupTask = SchedulerUtil.runTaskTimer(plugin, this::periodicCleanup, 20L * 60, 20L * 60);

            plugin.getLogger().info("Web editor started on " + bindAddress + ":" + port);
            plugin.getLogger().info("Web editor auth token: " + authToken);
            plugin.getLogger().info("Open http://" + ("0.0.0.0".equals(bindAddress) ? "localhost" : bindAddress) + ":" + port + " in your browser to access the editor.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start web editor on " + bindAddress + ":" + port, e);
            return false;
        }
    }

    /**
     * Stop the web editor HTTP server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        sessions.clear();
        rateLimitMap.clear();
        authToken = null;
    }

    public boolean isRunning() {
        return server != null;
    }

    public int getPort() {
        return plugin.getConfig().getInt("web-editor.port", 8585);
    }

    /**
     * Get the current auth token (for displaying to admins in-game if needed).
     */
    public String getAuthToken() {
        return authToken;
    }

    // ==========================================
    // Rate Limiting
    // ==========================================

    /**
     * Check if a request from the given IP should be rate-limited.
     * Returns true if the request is allowed, false if rate-limited.
     */
    private boolean checkRateLimit(String ip) {
        AtomicInteger counter = rateLimitMap.computeIfAbsent(ip, k -> new AtomicInteger(0));
        return counter.incrementAndGet() <= RATE_LIMIT_MAX;
    }

    /**
     * Validate the auth token from the request.
     * Token can be in the Authorization header (Bearer token) or as a ?token= query param.
     */
    private boolean validateAuth(HttpExchange exchange) {
        if (authToken == null) return false;

        // Check Authorization header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (authToken.equals(token)) return true;
        }

        // Check query parameter
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    if (authToken.equals(token)) return true;
                }
            }
        }
        return false;
    }

    // ==========================================
    // Config File Loading
    // ==========================================

    /**
     * Get a ConfigurationSection for the given file ID.
     * Loads the YAML file from the plugin data folder.
     */
    private YamlConfiguration loadConfigFile(String fileId) {
        if ("config".equals(fileId)) {
            // Main config.yml - use plugin's config which handles defaults/migration
            plugin.reloadConfig();
            // Copy it into a fresh YamlConfiguration to avoid modifying the live config
            YamlConfiguration copy = new YamlConfiguration();
            for (String key : plugin.getConfig().getKeys(true)) {
                if (!plugin.getConfig().isConfigurationSection(key)) {
                    copy.set(key, plugin.getConfig().get(key));
                }
            }
            return copy;
        }

        String relativePath = CONFIG_FILES.get(fileId);
        if (relativePath == null) return null;

        File file = new File(plugin.getDataFolder(), relativePath);
        if (!file.exists()) {
            // Try to save default from resources
            String resourcePath = relativePath;
            try (InputStream in = plugin.getResource(resourcePath)) {
                if (in != null) {
                    file.getParentFile().mkdirs();
                    java.nio.file.Files.copy(in, file.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create default config file: " + relativePath);
            }
        }

        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }

    /**
     * Save changes to a config file by its file ID.
     */
    private void saveConfigFile(String fileId, Map<String, Object> changes) {
        if ("config".equals(fileId)) {
            for (Map.Entry<String, Object> entry : changes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Object current = plugin.getConfig().get(key);
                if (current != null) {
                    value = coerceType(value, current);
                }
                plugin.getConfig().set(key, value);
            }
            plugin.saveConfig();
            return;
        }

        String relativePath = CONFIG_FILES.get(fileId);
        if (relativePath == null) return;

        File file = new File(plugin.getDataFolder(), relativePath);
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object current = config.get(key);
            if (current != null) {
                value = coerceType(value, current);
            }
            config.set(key, value);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save config file: " + relativePath, e);
        }
    }

    /**
     * Extract the "file" query parameter from the request URI.
     * Defaults to "config" if not specified.
     */
    private String getFileIdFromQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("file=")) {
                    String fileId = param.substring(5);
                    if (CONFIG_FILES.containsKey(fileId)) {
                        return fileId;
                    }
                }
            }
        }
        return "config";
    }

    // ==========================================
    // HTTP Handlers
    // ==========================================

    private void handlePage(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (!"/".equals(path) && !"/index.html".equals(path)) {
            sendResponse(exchange, 404, "text/plain", "Not Found");
            return;
        }

        // The page itself embeds the auth token so JS can use it for API calls.
        String html = WebEditorPage.getHtml(authToken, getPort());
        sendResponse(exchange, 200, "text/html; charset=utf-8", html);
    }

    private void handleFilesApi(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        if (!checkRateLimit(clientIp)) {
            sendResponse(exchange, 429, "application/json", "{\"error\":\"Too many requests.\"}");
            return;
        }
        if (!validateAuth(exchange)) {
            sendResponse(exchange, 401, "application/json", "{\"error\":\"Unauthorized.\"}");
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }

        // Return list of all config files with metadata
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, String> entry : CONFIG_FILES.entrySet()) {
            if (!first) json.append(",");
            first = false;
            String id = entry.getKey();
            String path = entry.getValue();
            String displayName = getFileDisplayName(id);
            String category = getFileCategory(id);
            String icon = getFileIcon(id);
            json.append("{\"id\":\"").append(escapeJson(id))
                .append("\",\"path\":\"").append(escapeJson(path))
                .append("\",\"name\":\"").append(escapeJson(displayName))
                .append("\",\"category\":\"").append(escapeJson(category))
                .append("\",\"icon\":\"").append(escapeJson(icon))
                .append("\"}");
        }
        json.append("]");
        sendResponse(exchange, 200, "application/json", json.toString());
    }

    private void handleConfigApi(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        // Rate limiting
        if (!checkRateLimit(clientIp)) {
            sendResponse(exchange, 429, "application/json", "{\"error\":\"Too many requests. Try again later.\"}");
            return;
        }

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "text/plain", "");
            return;
        }

        // Authenticate all API requests
        if (!validateAuth(exchange)) {
            sendResponse(exchange, 401, "application/json", "{\"error\":\"Unauthorized. Invalid or missing auth token.\"}");
            return;
        }

        String fileId = getFileIdFromQuery(exchange);

        if ("GET".equals(exchange.getRequestMethod())) {
            handleGetConfig(exchange, fileId);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePostConfig(exchange, fileId);
        } else {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
        }
    }

    private void handleGetConfig(HttpExchange exchange, String fileId) throws IOException {
        YamlConfiguration config = loadConfigFile(fileId);
        if (config == null) {
            sendResponse(exchange, 404, "application/json", "{\"error\":\"Config file not found: " + escapeJson(fileId) + "\"}");
            return;
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"version\":\"").append(escapeJson(plugin.getPluginMeta().getVersion())).append("\",");
        json.append("\"fileId\":\"").append(escapeJson(fileId)).append("\",");
        json.append("\"fileName\":\"").append(escapeJson(getFileDisplayName(fileId))).append("\",");
        json.append("\"config\":");
        serializeConfigSection(config, json, "");
        json.append("}");

        sendResponse(exchange, 200, "application/json", json.toString());
    }

    /**
     * Validate Origin/Referer header for CSRF protection on POST requests.
     * Returns true if the request is valid, false if it should be rejected.
     * Allows requests with no Origin AND no Referer (direct API calls).
     */
    private boolean validateCsrf(HttpExchange exchange) {
        int port = getPort();
        String bindAddress = plugin.getConfig().getString("web-editor.bind-address", "127.0.0.1");
        String expectedHost = ("0.0.0.0".equals(bindAddress) ? "localhost" : bindAddress) + ":" + port;

        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null) {
            // Origin header present - validate it
            return origin.equals("http://" + expectedHost)
                    || origin.equals("http://localhost:" + port)
                    || origin.equals("http://127.0.0.1:" + port);
        }

        String referer = exchange.getRequestHeaders().getFirst("Referer");
        if (referer != null) {
            // No Origin, but Referer present - validate it
            return referer.startsWith("http://" + expectedHost + "/")
                    || referer.startsWith("http://localhost:" + port + "/")
                    || referer.startsWith("http://127.0.0.1:" + port + "/");
        }

        // No Origin and no Referer - allow (direct API calls)
        return true;
    }

    private void handlePostConfig(HttpExchange exchange, String fileId) throws IOException {
        // CSRF validation for POST requests
        if (!validateCsrf(exchange)) {
            sendResponse(exchange, 403, "application/json", "{\"error\":\"Forbidden. Origin validation failed.\"}");
            return;
        }

        // Read request body with size limit
        String body;
        try (InputStream is = exchange.getRequestBody()) {
            byte[] bodyBytes = is.readNBytes(MAX_BODY_SIZE);
            if (is.read() != -1) {
                sendResponse(exchange, 413, "application/json", "{\"error\":\"Request body too large. Maximum size is 1 MB.\"}");
                return;
            }
            body = new String(bodyBytes, StandardCharsets.UTF_8);
        }

        // Parse the JSON changes using Gson
        Map<String, Object> changes = parseChangesJson(body);
        if (changes == null || changes.isEmpty()) {
            sendResponse(exchange, 400, "application/json", "{\"error\":\"No changes provided\"}");
            return;
        }

        // Generate a session code
        String code = generateCode();
        PendingSession session = new PendingSession(code, fileId, changes);
        sessions.put(code, session);

        // Log to console
        String fileName = CONFIG_FILES.getOrDefault(fileId, fileId);
        plugin.getLogger().info("Web editor session created: " + code + " (" + changes.size() + " changes to " + fileName + ", expires in 10 min)");

        String response = "{\"code\":\"" + escapeJson(code) + "\",\"changes\":" + changes.size()
                + ",\"file\":\"" + escapeJson(fileName) + "\",\"expiresIn\":600}";
        sendResponse(exchange, 200, "application/json", response);
    }

    // ==========================================
    // Session Management
    // ==========================================

    /**
     * Look up a pending session by its code.
     * Returns null if not found or expired.
     */
    public PendingSession getSession(String code) {
        PendingSession session = sessions.get(code.toUpperCase());
        if (session != null && session.isExpired()) {
            sessions.remove(code.toUpperCase());
            return null;
        }
        return session;
    }

    /**
     * Remove a session after it has been applied.
     */
    public void removeSession(String code) {
        sessions.remove(code.toUpperCase());
    }

    /**
     * Apply a session's changes to the appropriate config file.
     * Must be called from the main server thread.
     */
    public int applySession(PendingSession session) {
        saveConfigFile(session.fileId, session.changes);
        return session.changes.size();
    }

    /**
     * Get active (non-expired) session codes for tab completion.
     */
    public List<String> getActiveCodes() {
        List<String> codes = new ArrayList<>();
        for (Map.Entry<String, PendingSession> entry : sessions.entrySet()) {
            if (!entry.getValue().isExpired()) {
                codes.add(entry.getKey());
            }
        }
        return codes;
    }

    /** Periodic cleanup of expired sessions and rate limit counters. */
    private void periodicCleanup() {
        sessions.entrySet().removeIf(e -> e.getValue().isExpired());
        rateLimitMap.clear();
    }

    // ==========================================
    // File Metadata
    // ==========================================

    private String getFileDisplayName(String fileId) {
        return switch (fileId) {
            case "config" -> "Main Config";
            case "motd" -> "MOTD";
            case "scoreboard" -> "Scoreboard";
            case "stats" -> "Stats GUI";
            case "icon" -> "Server Icon";
            case "maintenance" -> "Maintenance";
            case "texts-general" -> "General Messages";
            case "texts-teleport" -> "Teleport Messages";
            case "texts-warp" -> "Warp Messages";
            case "texts-home" -> "Home Messages";
            case "texts-economy" -> "Economy Messages";
            case "texts-moderation" -> "Moderation Messages";
            case "texts-player" -> "Player Messages";
            case "texts-chat" -> "Chat Messages";
            case "texts-inventory" -> "Inventory Messages";
            case "texts-world" -> "World Messages";
            case "texts-team" -> "Team Messages";
            case "texts-trade" -> "Trade Messages";
            case "texts-maintenance" -> "Maintenance Messages";
            case "texts-info" -> "Info Messages";
            case "texts-misc" -> "Misc Messages";
            case "texts-kits" -> "Kit Messages";
            case "texts-nick" -> "Nickname & Tag Messages";
            case "database" -> "Database";
            case "automessages" -> "Auto Messages";
            default -> fileId;
        };
    }

    private String getFileCategory(String fileId) {
        if (fileId.startsWith("texts-")) return "Messages";
        return switch (fileId) {
            case "config" -> "Core";
            case "motd", "scoreboard", "icon" -> "Display";
            case "stats" -> "Display";
            case "maintenance" -> "System";
            case "database" -> "System";
            case "automessages" -> "System";
            default -> "Other";
        };
    }

    private String getFileIcon(String fileId) {
        return switch (fileId) {
            case "config" -> "\u2699\uFE0F"; // ⚙️
            case "motd" -> "\uD83D\uDCE8"; // 📨
            case "scoreboard" -> "\uD83D\uDCCA"; // 📊
            case "stats" -> "\uD83D\uDCC8"; // 📈
            case "icon" -> "\uD83D\uDDBC\uFE0F"; // 🖼️
            case "maintenance" -> "\uD83D\uDEE0\uFE0F"; // 🛠️
            case "texts-general" -> "\uD83D\uDCDD"; // 📝
            case "texts-teleport" -> "\uD83C\uDF00"; // 🌀
            case "texts-warp" -> "\u2728"; // ✨
            case "texts-home" -> "\uD83C\uDFE0"; // 🏠
            case "texts-economy" -> "\uD83D\uDCB0"; // 💰
            case "texts-moderation" -> "\uD83D\uDEE1\uFE0F"; // 🛡️
            case "texts-player" -> "\uD83D\uDC64"; // 👤
            case "texts-chat" -> "\uD83D\uDCAC"; // 💬
            case "texts-inventory" -> "\uD83C\uDF92"; // 🎒
            case "texts-world" -> "\uD83C\uDF0D"; // 🌍
            case "texts-team" -> "\uD83D\uDC65"; // 👥
            case "texts-trade" -> "\uD83E\uDD1D"; // 🤝
            case "texts-maintenance" -> "\uD83D\uDD27"; // 🔧
            case "texts-info" -> "\u2139\uFE0F"; // ℹ️
            case "texts-misc" -> "\uD83D\uDCE6"; // 📦
            case "texts-kits" -> "\uD83C\uDF81"; // 🎁
            case "texts-nick" -> "\uD83C\uDFF7\uFE0F"; // 🏷️
            case "database" -> "\uD83D\uDDC4\uFE0F"; // 🗄️
            case "automessages" -> "\uD83D\uDCE2"; // 📢
            default -> "\uD83D\uDCC4"; // 📄
        };
    }

    // ==========================================
    // JSON Serialization (redacts sensitive keys)
    // ==========================================

    private boolean isRedactedKey(String fullPath) {
        for (String redacted : REDACTED_KEYS) {
            if (fullPath.equals(redacted) || fullPath.endsWith("." + redacted)) {
                return true;
            }
        }
        return false;
    }

    private void serializeConfigSection(ConfigurationSection section, StringBuilder sb, String pathPrefix) {
        sb.append("{");
        boolean first = true;
        for (String key : section.getKeys(false)) {
            if (!first) sb.append(",");
            first = false;

            String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            sb.append("\"").append(escapeJson(key)).append("\":");

            Object val = section.get(key);
            if (val instanceof ConfigurationSection cs) {
                serializeConfigSection(cs, sb, fullPath);
            } else if (val instanceof MemoryConfiguration mc) {
                serializeConfigSection(mc, sb, fullPath);
            } else if (isRedactedKey(fullPath)) {
                sb.append("\"***REDACTED***\"");
            } else if (val instanceof Boolean b) {
                sb.append(b.toString());
            } else if (val instanceof Number n) {
                if (val instanceof Integer || val instanceof Long) {
                    sb.append(n.longValue());
                } else {
                    sb.append(n.doubleValue());
                }
            } else if (val instanceof List<?> list) {
                sb.append("[");
                boolean listFirst = true;
                for (Object item : list) {
                    if (!listFirst) sb.append(",");
                    listFirst = false;
                    if (item instanceof String s) {
                        sb.append("\"").append(escapeJson(s)).append("\"");
                    } else if (item instanceof Number n) {
                        sb.append(n);
                    } else if (item instanceof Boolean b) {
                        sb.append(b);
                    } else if (item instanceof Map) {
                        sb.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
                    } else {
                        sb.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
                    }
                }
                sb.append("]");
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(val))).append("\"");
            }
        }
        sb.append("}");
    }

    /**
     * Parse the changes JSON using Gson for robust, safe parsing.
     * Expects: {"changes": {"key": value, ...}}
     */
    private Map<String, Object> parseChangesJson(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("changes") || !root.get("changes").isJsonObject()) {
                return null;
            }

            JsonObject changesObj = root.getAsJsonObject("changes");
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : changesObj.entrySet()) {
                String key = entry.getKey();
                JsonElement element = entry.getValue();

                // Block attempts to modify redacted keys via the web editor
                if (isRedactedKey(key)) continue;

                if (element.isJsonNull()) {
                    result.put(key, "");
                } else if (element.isJsonPrimitive()) {
                    JsonPrimitive prim = element.getAsJsonPrimitive();
                    if (prim.isBoolean()) {
                        result.put(key, prim.getAsBoolean());
                    } else if (prim.isNumber()) {
                        Number num = prim.getAsNumber();
                        if (prim.getAsString().contains(".")) {
                            result.put(key, num.doubleValue());
                        } else {
                            long lv = num.longValue();
                            if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
                                result.put(key, (int) lv);
                            } else {
                                result.put(key, lv);
                            }
                        }
                    } else {
                        result.put(key, prim.getAsString());
                    }
                }
                // Skip arrays/objects in changes - flat key-value only
            }
            return result;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse web editor changes JSON", e);
            return null;
        }
    }

    /**
     * Coerce a value to match the type of the existing config value.
     */
    private Object coerceType(Object newVal, Object existingVal) {
        if (existingVal instanceof Boolean && newVal instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        if (existingVal instanceof Integer && newVal instanceof Number n) {
            return n.intValue();
        }
        if (existingVal instanceof Long && newVal instanceof Number n) {
            return n.longValue();
        }
        if (existingVal instanceof Double && newVal instanceof Number n) {
            return n.doubleValue();
        }
        if (existingVal instanceof Integer && newVal instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        if (existingVal instanceof Double && newVal instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return newVal;
    }

    // ==========================================
    // Utility
    // ==========================================

    private String generateAuthToken() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int attempt = 0; attempt < 100; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
            }
            String code = sb.toString();
            if (!sessions.containsKey(code)) {
                return code;
            }
        }
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void sendResponse(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Get the map of all config files.
     */
    public static Map<String, String> getConfigFiles() {
        return Collections.unmodifiableMap(CONFIG_FILES);
    }
}

