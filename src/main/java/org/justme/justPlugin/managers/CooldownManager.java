package org.justme.justPlugin.managers;

import org.justme.justPlugin.JustPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player, per-feature cooldowns (time between command uses).
 * Cooldowns apply even to OPs — only explicit bypass permissions skip them.
 */
public class CooldownManager {

    private final JustPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player is on cooldown for a feature.
     *
     * @param uuid       Player UUID
     * @param featureKey Feature key (e.g., "tpa", "tpahere", "warp", "spawn")
     * @return true if still on cooldown
     */
    public boolean isOnCooldown(UUID uuid, String featureKey) {
        int cooldownSeconds = getCooldownSeconds(featureKey);
        if (cooldownSeconds <= 0) return false;
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return false;
        Long lastUse = playerCooldowns.get(featureKey);
        if (lastUse == null) return false;
        return System.currentTimeMillis() - lastUse < cooldownSeconds * 1000L;
    }

    /**
     * Get remaining cooldown seconds for a player and feature.
     */
    public int getRemainingSeconds(UUID uuid, String featureKey) {
        int cooldownSeconds = getCooldownSeconds(featureKey);
        if (cooldownSeconds <= 0) return 0;
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0;
        Long lastUse = playerCooldowns.get(featureKey);
        if (lastUse == null) return 0;
        long elapsed = System.currentTimeMillis() - lastUse;
        long remaining = (cooldownSeconds * 1000L) - elapsed;
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    /**
     * Set the cooldown for a player and feature to now.
     */
    public void setCooldown(UUID uuid, String featureKey) {
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(featureKey, System.currentTimeMillis());
    }

    /**
     * Get configured cooldown seconds for a feature from config.
     */
    public int getCooldownSeconds(String featureKey) {
        return plugin.getConfig().getInt("teleport.cooldown-" + featureKey, 0);
    }
}


