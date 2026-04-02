package org.justme.justPlugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.SpawnProtectionManager;

/**
 * Listens for events in the spawn protection radius and cancels them
 * if the player does not have the bypass permission.
 */
public class SpawnProtectionListener implements Listener {

    private final JustPlugin plugin;
    private final SpawnProtectionManager manager;

    public SpawnProtectionListener(JustPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getSpawnProtectionManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!manager.isEnabled() || !manager.isPreventBlockBreak()) return;
        if (!manager.isInSpawnRadius(event.getBlock().getLocation())) return;
        if (manager.canBypass(event.getPlayer())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(
                plugin.getMessageManager().error("world.spawn-protection.denied"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!manager.isEnabled() || !manager.isPreventBlockPlace()) return;
        if (!manager.isInSpawnRadius(event.getBlock().getLocation())) return;
        if (manager.canBypass(event.getPlayer())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(
                plugin.getMessageManager().error("world.spawn-protection.denied"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!manager.isEnabled() || !manager.isPreventPvp()) return;

        // Only handle PvP (player vs player)
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (!(damager instanceof Player attacker) || !(victim instanceof Player)) return;

        // Check if either player is in spawn radius
        if (!manager.isInSpawnRadius(attacker.getLocation()) && !manager.isInSpawnRadius(victim.getLocation())) return;
        if (manager.canBypass(attacker)) return;

        event.setCancelled(true);
        attacker.sendMessage(
                plugin.getMessageManager().error("world.spawn-protection.denied"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!manager.isEnabled() || !manager.isPreventExplosions()) return;
        if (!manager.isInSpawnRadius(event.getLocation())) return;

        // Remove all blocks from the explosion that are in the spawn radius
        event.blockList().removeIf(block -> manager.isInSpawnRadius(block.getLocation()));
    }
}
