package org.justme.justPlugin.listeners.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Handles server-level events: server list ping (MOTD, maintenance icon,
 * vanished player hiding) and tab completion filtering for vanished players.
 */
public class ServerListener implements Listener {

    private final JustPlugin plugin;

    public ServerListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    // Server list ping - set server MOTD, maintenance icon, and hide vanished players
    @SuppressWarnings("removal")
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // Check if maintenance mode is active - fully replace MOTD and optionally set icon
        if (plugin.getCommandSettings().isEnabled("maintenance")
                && plugin.getMaintenanceManager().isActive()) {
            String maintenanceMotd = plugin.getMaintenanceManager().getMaintenanceMotd()
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            event.motd(CC.translate(maintenanceMotd));

            org.bukkit.util.CachedServerIcon icon = plugin.getMaintenanceManager().getCachedIcon();
            if (icon != null) {
                event.setServerIcon(icon);
            }
        } else {
            // Normal mode - set the server list MOTD if configured
            String serverMotd = plugin.getMotdManager().getServerMotd();
            if (serverMotd != null && !serverMotd.isEmpty()) {
                String resolved = serverMotd
                        .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
                event.motd(CC.translate(resolved));
            }

            org.bukkit.util.CachedServerIcon urlIcon = plugin.getIconManager().getCachedIcon();
            if (urlIcon != null) {
                event.setServerIcon(urlIcon);
            }
        }

        event.setMaxPlayers(event.getMaxPlayers());
        try {
            Iterator<Player> iterator = event.iterator();
            while (iterator.hasNext()) {
                Player p = iterator.next();
                if (plugin.getVanishManager().isVanished(p.getUniqueId())) {
                    iterator.remove();
                }
            }
        } catch (UnsupportedOperationException ignored) {}
    }

    // Prevent vanished players from appearing in tab completion
    @EventHandler
    public void onTabComplete(com.destroystokyo.paper.event.server.AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player viewer)) return;
        if (viewer.hasPermission("justplugin.vanish.see")) return;

        Set<String> vanishedNames = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getVanishManager().isVanished(p.getUniqueId())) {
                vanishedNames.add(p.getName().toLowerCase());
            }
        }
        if (vanishedNames.isEmpty()) return;

        event.completions().removeIf(completion -> {
            String s = completion.suggestion().toLowerCase();
            return vanishedNames.contains(s);
        });
    }
}

