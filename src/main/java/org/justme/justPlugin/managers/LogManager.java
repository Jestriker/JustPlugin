package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

/**
 * Centralized logging system. Every log call:
 * 1. Always prints to the server console.
 * 2. Broadcasts in-game to online players with justplugin.log.&lt;category&gt; permission.
 * <p>
 * Categories: moderation, economy, teleport, vanish, gamemode, player, admin, item
 */
public class LogManager {

    private final JustPlugin plugin;

    public LogManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Log an action.
     *
     * @param category The log category (e.g. "moderation", "economy", "teleport").
     * @param message  MiniMessage-formatted message describing the action.
     */
    public void log(String category, String message) {
        // 1. Always log to console (strip formatting)
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(CC.translate(message));
        plugin.getLogger().info("[" + category.toUpperCase() + "] " + plain);

        // 2. Broadcast to players with the log permission
        String perm = "justplugin.log." + category;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(perm)) {
                p.sendMessage(CC.translate("<dark_gray>[<gradient:#ff6b6b:#ee5a24>LOG</gradient>] <gray>[<yellow>" + category.toUpperCase() + "</yellow>] " + message));
            }
        }

        // 3. Send to Discord webhook if enabled
        WebhookManager webhookManager = plugin.getWebhookManager();
        if (webhookManager != null && webhookManager.isEnabled()) {
            webhookManager.sendLog(category, plain, null);
        }
    }
}

