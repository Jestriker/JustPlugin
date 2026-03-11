package org.justme.justPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.VanishManager;

/**
 * Handles vanish-related events: ensures vanished players remain hidden
 * from other players when they join (or when a vanished player logs in).
 */
public class VanishListener implements Listener {

    private final JustPlugin plugin;
    private final VanishManager vanishManager;

    public VanishListener(JustPlugin plugin) {
        this.plugin = plugin;
        this.vanishManager = plugin.getVanishManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        // Hide all currently vanished players from the new joiner
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(joining) && vanishManager.isVanished(online.getUniqueId())) {
                joining.hidePlayer(plugin, online);
            }
        }
        // If the joining player themselves was vanished (e.g. reconnect), re-apply vanish
        if (vanishManager.isVanished(joining.getUniqueId())) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.equals(joining)) {
                    online.hidePlayer(plugin, joining);
                }
            }
        }
    }
}
