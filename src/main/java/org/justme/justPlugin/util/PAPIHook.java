package org.justme.justPlugin.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Thin bridge to PlaceholderAPI. All calls are safe to make even when PAPI is not installed -
 * the class never directly imports PlaceholderAPI, using reflection-safe checks only.
 */
public final class PAPIHook {

    private static Boolean available;

    private PAPIHook() {}

    /**
     * @return true if PlaceholderAPI is loaded on the server.
     */
    public static boolean isAvailable() {
        if (available == null) {
            available = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        }
        return available;
    }

    /**
     * Reset the cached availability flag (call on reload if needed).
     */
    public static void reset() {
        available = null;
    }

    /**
     * Replace %placeholder% tokens via PlaceholderAPI for the given player.
     * If PAPI is not installed or the input contains no %-tokens, returns the input unchanged.
     */
    public static String setPlaceholders(Player player, String text) {
        if (!isAvailable() || text == null || !text.contains("%")) return text;
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            return text;
        }
    }
}

