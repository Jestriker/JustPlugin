package org.justme.justPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.GodManager;

public class VanishListener implements Listener {

    private final GodManager godManager;

    public VanishListener(JustPlugin plugin) {
        this.godManager = plugin.getGodManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player player) {
            if (godManager.isGod(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
