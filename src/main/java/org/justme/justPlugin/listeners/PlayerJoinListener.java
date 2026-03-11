package org.justme.justPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.VanishManager;

public class PlayerJoinListener implements Listener {

    private final JustPlugin plugin;
    private final VanishManager vanishManager;

    public PlayerJoinListener(JustPlugin plugin) {
        this.plugin = plugin;
        this.vanishManager = plugin.getVanishManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        org.bukkit.entity.Player joined = event.getPlayer();
        // Hide vanished players from newly joined player
        for (java.util.UUID vanishedId : vanishManager.getVanished()) {
            org.bukkit.entity.Player vanishedPlayer = plugin.getServer().getPlayer(vanishedId);
            if (vanishedPlayer != null && !joined.hasPermission("justplugin.vanish.see")) {
                joined.hidePlayer(plugin, vanishedPlayer);
            }
        }
        // Send MOTD
        java.util.List<String> motd = plugin.getConfig().getStringList("motd");
        if (!motd.isEmpty()) {
            for (String line : motd) {
                joined.sendMessage(line.replace("&", "§"));
            }
        }
    }
}
