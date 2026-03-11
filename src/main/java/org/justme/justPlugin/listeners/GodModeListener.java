package org.justme.justPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.GodManager;

/**
 * Cancels damage events for players that have god mode enabled.
 */
public class GodModeListener implements Listener {

    private final GodManager godManager;

    public GodModeListener(JustPlugin plugin) {
        this.godManager = plugin.getGodManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godManager.isGod(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
