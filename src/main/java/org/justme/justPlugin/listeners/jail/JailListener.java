package org.justme.justPlugin.listeners.jail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

/**
 * Handles jail-related events:
 * - Teleport to jail on join if still jailed
 * - Block commands for jailed players
 */
public class JailListener implements Listener {

    private final JustPlugin plugin;

    public JailListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getJailManager().isJailed(player.getUniqueId())) {
            plugin.getJailManager().handleJoin(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getJailManager().isJailed(player.getUniqueId())) return;

        String message = event.getMessage();
        if (plugin.getJailManager().isCommandBlocked(message)) {
            event.setCancelled(true);
            player.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jail.command-blocked")));
        }
    }
}
