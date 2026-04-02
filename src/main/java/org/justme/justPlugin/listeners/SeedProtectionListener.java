package org.justme.justPlugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

/**
 * Protects the world seed from being viewed by non-authorized players.
 * Blocks the /seed command and hides it from tab completion.
 */
public class SeedProtectionListener implements Listener {

    private final JustPlugin plugin;

    public SeedProtectionListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean("seed-protection.enabled", false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase().trim();

        // Check if command is /seed (with or without arguments)
        if (!message.equals("/seed") && !message.startsWith("/seed ")) return;

        // Allow bypass
        if (player.hasPermission("justplugin.seedprotection.bypass")) return;

        event.setCancelled(true);
        player.sendMessage(plugin.getMessageManager().error("world.seed-protection.denied"));

        // Alert staff
        if (plugin.getConfig().getBoolean("seed-protection.alert-staff", true)) {
            String alertMsg = plugin.getMessageManager().raw("world.seed-protection.staff-alert",
                    "{player}", player.getName());
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("justplugin.seedprotection.notify")) {
                    staff.sendMessage(CC.translate(alertMsg));
                }
            }
        }

        // Log to webhook
        if (plugin.getConfig().getBoolean("seed-protection.log-to-webhook", true)) {
            plugin.getLogManager().log("security",
                    "<yellow>" + player.getName() + "</yellow> tried to view the world seed");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTabComplete(TabCompleteEvent event) {
        if (!isEnabled()) return;
        if (!(event.getSender() instanceof Player player)) return;
        if (player.hasPermission("justplugin.seedprotection.bypass")) return;

        // Remove /seed from tab completion results
        event.getCompletions().removeIf(s -> s.equalsIgnoreCase("seed") || s.equalsIgnoreCase("/seed"));
    }
}
