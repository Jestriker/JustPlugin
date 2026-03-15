package org.justme.justPlugin.managers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Self-hosted config web editor.
 * Runs a lightweight HTTP server that serves a beautiful SPA for editing plugin config.
 * Changes are staged with a session code that must be applied in-game via /applyedits.
 */
@SuppressWarnings("unused")
public class WebEditorManager {

    private final JustPlugin plugin;
    private HttpServer server;
    private final Map<String, PendingSession> sessions = new ConcurrentHashMap<>();
    private int taskId = -1;

    /**
     * Represents a pending config edit session.
     */
    public static class PendingSession {
        public final String code;
        public final Map<String, Object> changes;
        public final long createdAt;
        public final long expiresAt;

        public PendingSession(String code, Map<String, Object> changes) {
            this.code = code;
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
        String bindAddress = plugin.getConfig().getString("web-editor.bind-address", "0.0.0.0");

        try {
            server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            // Serve the web editor page
            server.createContext("/", this::handlePage);
            // API endpoints
            server.createContext("/api/config", this::handleConfigApi);

            server.start();

            // Schedule cleanup of expired sessions (every 60 seconds)
            taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupSessions, 20L * 60, 20L * 60).getTaskId();

            plugin.getLogger().info("Web editor started on port " + port);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start web editor on port " + port, e);
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
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        sessions.clear();
    }

    public boolean isRunning() {
        return server != null;
    }

    public int getPort() {
        return plugin.getConfig().getInt("web-editor.port", 8585);
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

        String html = WebEditorPage.getHtml();
        sendResponse(exchange, 200, "text/html; charset=utf-8", html);
    }

    private void handleConfigApi(HttpExchange exchange) throws IOException {
        // CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "text/plain", "");
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            handleGetConfig(exchange);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePostConfig(exchange);
        } else {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
        }
    }

    private void handleGetConfig(HttpExchange exchange) throws IOException {
        // Serialize the config to JSON
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"version\":\"").append(escapeJson(plugin.getPluginMeta().getVersion())).append("\",");
        json.append("\"config\":");
        serializeConfigSection(plugin.getConfig(), json);
        json.append("}");

        sendResponse(exchange, 200, "application/json", json.toString());
    }

    private void handlePostConfig(HttpExchange exchange) throws IOException {
        // Read request body
        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Parse the JSON changes
        Map<String, Object> changes = parseChangesJson(body);
        if (changes == null || changes.isEmpty()) {
            sendResponse(exchange, 400, "application/json", "{\"error\":\"No changes provided\"}");
            return;
        }

        // Generate a session code
        String code = generateCode();
        PendingSession session = new PendingSession(code, changes);
        sessions.put(code, session);

        // Log to console
        plugin.getLogger().info("Web editor session created: " + code + " (" + changes.size() + " changes, expires in 10 min)");

        String response = "{\"code\":\"" + escapeJson(code) + "\",\"changes\":" + changes.size() + ",\"expiresIn\":600}";
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
     * Apply a session's changes to the plugin config.
     * Must be called from the main server thread.
     */
    public int applySession(PendingSession session) {
        int applied = 0;
        for (Map.Entry<String, Object> entry : session.changes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Convert numeric strings back to proper types
            Object current = plugin.getConfig().get(key);
            if (current != null) {
                value = coerceType(value, current);
            }

            plugin.getConfig().set(key, value);
            applied++;
        }
        plugin.saveConfig();
        return applied;
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

    private void cleanupSessions() {
        sessions.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    // ==========================================
    // JSON Serialization (no external library)
    // ==========================================

    private void serializeConfigSection(ConfigurationSection section, StringBuilder sb) {
        sb.append("{");
        boolean first = true;
        for (String key : section.getKeys(false)) {
            if (!first) sb.append(",");
            first = false;

            sb.append("\"").append(escapeJson(key)).append("\":");

            Object val = section.get(key);
            if (val instanceof ConfigurationSection cs) {
                serializeConfigSection(cs, sb);
            } else if (val instanceof MemoryConfiguration mc) {
                serializeConfigSection(mc, sb);
            } else if (val instanceof Boolean b) {
                sb.append(b.toString());
            } else if (val instanceof Number n) {
                // Preserve int vs double
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
     * Simple JSON parser for the changes object.
     * Expects: {"changes": {"key": value, ...}}
     */
    private Map<String, Object> parseChangesJson(String json) {
        try {
            // Strip outer object to get changes map
            int changesIdx = json.indexOf("\"changes\"");
            if (changesIdx == -1) return null;

            // Find the opening brace of the changes object
            int braceStart = json.indexOf('{', changesIdx);
            if (braceStart == -1) return null;

            // Find matching closing brace
            int depth = 0;
            int braceEnd = -1;
            for (int i = braceStart; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        braceEnd = i;
                        break;
                    }
                }
            }
            if (braceEnd == -1) return null;

            String changesStr = json.substring(braceStart + 1, braceEnd);
            return parseKeyValuePairs(changesStr);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse web editor changes JSON", e);
            return null;
        }
    }

    /**
     * Parse flat key-value pairs from JSON-like content.
     * Handles strings, numbers, booleans.
     */
    private Map<String, Object> parseKeyValuePairs(String content) {
        Map<String, Object> result = new LinkedHashMap<>();
        int i = 0;
        while (i < content.length()) {
            // Skip whitespace and commas
            while (i < content.length() && (content.charAt(i) == ' ' || content.charAt(i) == ',' ||
                    content.charAt(i) == '\n' || content.charAt(i) == '\r' || content.charAt(i) == '\t')) i++;
            if (i >= content.length()) break;

            // Read key
            if (content.charAt(i) != '"') break;
            i++; // skip opening quote
            StringBuilder key = new StringBuilder();
            while (i < content.length() && content.charAt(i) != '"') {
                if (content.charAt(i) == '\\' && i + 1 < content.length()) {
                    i++;
                    key.append(content.charAt(i));
                } else {
                    key.append(content.charAt(i));
                }
                i++;
            }
            i++; // skip closing quote

            // Skip colon and whitespace
            while (i < content.length() && (content.charAt(i) == ':' || content.charAt(i) == ' ')) i++;

            // Read value
            if (i >= content.length()) break;

            char c = content.charAt(i);
            if (c == '"') {
                // String value
                i++;
                StringBuilder val = new StringBuilder();
                while (i < content.length() && content.charAt(i) != '"') {
                    if (content.charAt(i) == '\\' && i + 1 < content.length()) {
                        i++;
                        switch (content.charAt(i)) {
                            case 'n' -> val.append('\n');
                            case 't' -> val.append('\t');
                            case 'r' -> val.append('\r');
                            default -> val.append(content.charAt(i));
                        }
                    } else {
                        val.append(content.charAt(i));
                    }
                    i++;
                }
                i++; // skip closing quote
                result.put(key.toString(), val.toString());
            } else if (c == 't' || c == 'f') {
                // Boolean
                if (content.startsWith("true", i)) {
                    result.put(key.toString(), true);
                    i += 4;
                } else if (content.startsWith("false", i)) {
                    result.put(key.toString(), false);
                    i += 5;
                }
            } else if (c == '-' || (c >= '0' && c <= '9')) {
                // Number
                StringBuilder numStr = new StringBuilder();
                boolean isFloat = false;
                while (i < content.length() && (content.charAt(i) == '-' || content.charAt(i) == '.' ||
                        (content.charAt(i) >= '0' && content.charAt(i) <= '9'))) {
                    if (content.charAt(i) == '.') isFloat = true;
                    numStr.append(content.charAt(i));
                    i++;
                }
                if (isFloat) {
                    result.put(key.toString(), Double.parseDouble(numStr.toString()));
                } else {
                    long lv = Long.parseLong(numStr.toString());
                    if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
                        result.put(key.toString(), (int) lv);
                    } else {
                        result.put(key.toString(), lv);
                    }
                }
            } else if (c == 'n' && content.startsWith("null", i)) {
                result.put(key.toString(), "");
                i += 4;
            }
        }
        return result;
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

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // No I/O/0/1 to avoid confusion
        Random rng = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        String code = sb.toString();
        // Ensure unique
        if (sessions.containsKey(code)) return generateCode();
        return code;
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
}



