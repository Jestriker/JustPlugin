package org.justme.justPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;

public class DeathListener implements Listener {

    private final BackManager backManager;

    public DeathListener(JustPlugin plugin) {
        this.backManager = plugin.getBackManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        backManager.setDeathLocation(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }
}
