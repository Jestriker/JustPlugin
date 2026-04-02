package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.justme.justPlugin.JustPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Sends log embeds to a Discord webhook.
 * Webhook URL is stored in config under "discord-webhook.url".
 */
public class WebhookManager {

    private final JustPlugin plugin;
    private final HttpClient httpClient;

    // Retry configuration: exponential backoff delays in milliseconds
    private static final long[] RETRY_DELAYS_MS = {1000L, 3000L, 10000L};
    private static final int MAX_ATTEMPTS = 3;

    // Circuit breaker: pause sending after consecutive failures
    private static final int CIRCUIT_BREAKER_THRESHOLD = 10;
    private static final long CIRCUIT_BREAKER_PAUSE_MS = 5 * 60 * 1000L; // 5 minutes
    private int consecutiveFailures = 0;
    private long circuitOpenUntil = 0;

    public WebhookManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("discord-webhook.enabled", true);
    }

    public String getWebhookUrl() {
        return plugin.getConfig().getString("discord-webhook.url", "");
    }

    public void setWebhookUrl(String url) {
        plugin.getConfig().set("discord-webhook.url", url);
        plugin.getConfig().set("discord-webhook.enabled", true);
        plugin.saveConfig();
    }

    public void disable() {
        plugin.getConfig().set("discord-webhook.enabled", false);
        plugin.saveConfig();
    }

    /**
     * Send a test embed to verify the webhook URL.
     * Returns a CompletableFuture with the HTTP status code.
     */
    public CompletableFuture<Integer> sendTest(String url) {
        String json = buildEmbed("Test Log", "This is a test log from **JustPlugin**. If you see this, the webhook is working!", 0x00AAFF, "System");
        return sendRawAsync(url, json);
    }

    /**
     * Send a log entry to the configured webhook (async).
     */
    public void sendLog(String category, String plainMessage, String executor) {
        if (!isEnabled()) return;
        String url = getWebhookUrl();
        if (url == null || url.isEmpty()) return;

        int color = getCategoryColor(category);
        String title = "[" + category.toUpperCase() + "] Log";
        String json = buildEmbed(title, plainMessage, color, executor);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sendWithRetry(url, json);
        });
    }

    private int getCategoryColor(String category) {
        return switch (category.toLowerCase()) {
            case "moderation" -> 0xFF0000;   // Red
            case "economy" -> 0x00FF00;      // Green
            case "teleport" -> 0x00AAFF;     // Blue
            case "vanish" -> 0x9900FF;       // Purple
            case "gamemode" -> 0xFF9900;      // Orange
            case "player" -> 0xFFFF00;        // Yellow
            case "admin" -> 0xFF3366;         // Pink
            case "item" -> 0x00FFAA;          // Teal
            case "warn" -> 0xFF6600;          // Dark Orange
            case "mute" -> 0xCC0000;          // Dark Red
            default -> 0x808080;              // Gray
        };
    }

    private String buildEmbed(String title, String description, int color, String executor) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String safeDesc = escapeJson(description);
        String safeTitle = escapeJson(title);
        String safeExec = escapeJson(executor != null ? executor : "System");

        return """
                {
                  "embeds": [
                    {
                      "title": "%s",
                      "description": "%s",
                      "color": %d,
                      "timestamp": "%s",
                      "footer": {
                        "text": "Executed by: %s | JustPlugin"
                      }
                    }
                  ]
                }
                """.formatted(safeTitle, safeDesc, color, timestamp, safeExec);
    }

    /**
     * Check if the circuit breaker is currently open (pausing webhook sends).
     */
    private boolean isCircuitOpen() {
        if (consecutiveFailures >= CIRCUIT_BREAKER_THRESHOLD) {
            if (System.currentTimeMillis() < circuitOpenUntil) {
                return true;
            }
            // Pause period expired - reset and allow a retry
            consecutiveFailures = 0;
            plugin.getLogger().info("Webhook circuit breaker reset, resuming sends.");
        }
        return false;
    }

    private void recordSuccess() {
        consecutiveFailures = 0;
    }

    private void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitOpenUntil = System.currentTimeMillis() + CIRCUIT_BREAKER_PAUSE_MS;
            plugin.getLogger().warning("Webhook circuit breaker activated after " + consecutiveFailures
                    + " consecutive failures. Pausing webhook sends for 5 minutes.");
        }
    }

    /**
     * Send a webhook request with exponential backoff retry (max 3 attempts).
     * Respects the circuit breaker - skips sending if circuit is open.
     */
    private void sendWithRetry(String url, String json) {
        if (isCircuitOpen()) {
            return;
        }

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                sendRawSync(url, json);
                recordSuccess();
                return;
            } catch (Exception e) {
                if (attempt < MAX_ATTEMPTS - 1) {
                    try {
                        Thread.sleep(RETRY_DELAYS_MS[attempt]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    recordFailure();
                    plugin.getLogger().warning("Failed to send webhook log after " + MAX_ATTEMPTS
                            + " attempts: " + e.getMessage());
                }
            }
        }
    }

    private CompletableFuture<Integer> sendRawAsync(String url, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(-1);
        }
    }

    private void sendRawSync(String url, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(10))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}


