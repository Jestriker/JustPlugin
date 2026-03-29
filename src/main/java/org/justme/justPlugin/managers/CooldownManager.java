package org.justme.justPlugin.managers;

import org.justme.justPlugin.JustPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player, per-feature delays (minimum time between successive command uses).
 * Delays are auto-skipped for OPs. Only explicit delaybypass permissions skip them for non-OPs.
 *
 * TERMINOLOGY (used throughout the plugin):
 *   "cooldown" = countdown timer BEFORE the teleport executes (e.g. 3s standing still)
 *   "delay"    = minimum time between successive uses of the same command (e.g. 5 minutes)
 */
public class CooldownManager {

    private final JustPlugin plugin;
    private final Map<UUID, Map<String, Long>> lastUseTimes = new ConcurrentHashMap<>();

    public CooldownManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    // =====================
    // DELAY (between uses)
    // =====================

    /**
     * Check if a player is still in the delay period for a feature.
     *
     * @param uuid       Player UUID
     * @param featureKey Feature key (e.g., "tpa", "tpahere", "warp", "spawn")
     * @return true if still in delay period
     */
    public boolean isOnDelay(UUID uuid, String featureKey) {
        int delaySeconds = getDelaySeconds(featureKey);
        if (delaySeconds <= 0) return false;
        Map<String, Long> playerDelays = lastUseTimes.get(uuid);
        if (playerDelays == null) return false;
        Long lastUse = playerDelays.get(featureKey);
        if (lastUse == null) return false;
        return System.currentTimeMillis() - lastUse < delaySeconds * 1000L;
    }

    /**
     * Get remaining delay seconds for a player and feature.
     */
    public int getRemainingDelaySeconds(UUID uuid, String featureKey) {
        int delaySeconds = getDelaySeconds(featureKey);
        if (delaySeconds <= 0) return 0;
        Map<String, Long> playerDelays = lastUseTimes.get(uuid);
        if (playerDelays == null) return 0;
        Long lastUse = playerDelays.get(featureKey);
        if (lastUse == null) return 0;
        long elapsed = System.currentTimeMillis() - lastUse;
        long remaining = (delaySeconds * 1000L) - elapsed;
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    /**
     * Record that a player has used a feature (sets delay start to now).
     */
    public void setDelayStart(UUID uuid, String featureKey) {
        lastUseTimes.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(featureKey, System.currentTimeMillis());
    }

    /**
     * Get configured delay seconds (between uses) for a feature from config.
     * Reads from teleport.delay-{featureKey}. Default 0 (disabled).
     */
    public int getDelaySeconds(String featureKey) {
        return plugin.getConfig().getInt("teleport.delay-" + featureKey, 0);
    }

    // =====================
    // COOLDOWN (pre-TP countdown) - read from config
    // =====================

    /**
     * Get configured cooldown seconds (pre-TP countdown) for a feature from config.
     * Reads from teleport.cooldown-{featureKey}. Default 3.
     */
    public double getCooldownSeconds(String featureKey) {
        return plugin.getConfig().getDouble("teleport.cooldown-" + featureKey, 3.0);
    }

    // =====================
    // LEGACY compatibility methods (map old names to new)
    // =====================

    /** @deprecated Use {@link #isOnDelay} */
    @Deprecated
    public boolean isOnCooldown(UUID uuid, String featureKey) {
        return isOnDelay(uuid, featureKey);
    }

    /** @deprecated Use {@link #getRemainingDelaySeconds} */
    @Deprecated
    public int getRemainingSeconds(UUID uuid, String featureKey) {
        return getRemainingDelaySeconds(uuid, featureKey);
    }

    /** @deprecated Use {@link #setDelayStart} */
    @Deprecated
    public void setCooldown(UUID uuid, String featureKey) {
        setDelayStart(uuid, featureKey);
    }

    /**
     * Format seconds into a human-readable string like "3m 0s", "30m 0s".
     */
    public static String formatTime(int seconds) {
        if (seconds < 60) return seconds + "s";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (minutes < 60) return secs > 0 ? minutes + "m " + secs + "s" : minutes + "m";
        int hours = minutes / 60;
        int mins = minutes % 60;
        return mins > 0 ? hours + "h " + mins + "m" : hours + "h";
    }
}
