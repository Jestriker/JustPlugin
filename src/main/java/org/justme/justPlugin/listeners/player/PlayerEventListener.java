package org.justme.justPlugin.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.justme.justPlugin.JustPlugin;

/**
 * Handles general player events: death, respawn, teleport (back location),
 * movement (teleport warmup), and advancement hiding for vanished players.
 */
public class PlayerEventListener implements Listener {

    private final JustPlugin plugin;

    public PlayerEventListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getTeleportManager().setBackLocation(player.getUniqueId(), player.getLocation());
        plugin.getPlayerListener().setDeathLocation(player.getUniqueId(), player.getLocation());
        // Persist immediately
        plugin.getPlayerListener().saveBackLocation(player.getUniqueId());
        plugin.getPlayerListener().saveDeathLocation(player.getUniqueId());
    }

    // Advancement hiding for vanished/super-vanished players
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        java.util.UUID uuid = player.getUniqueId();
        if (plugin.getVanishManager().isVanished(uuid) || plugin.getVanishManager().isSuperVanished(uuid)) {
            event.message(null);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            String worldName = plugin.getConfig().getString("spawn.world");
            if (worldName != null && plugin.getConfig().contains("spawn.x")) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location spawnLoc = new Location(world,
                            plugin.getConfig().getDouble("spawn.x"),
                            plugin.getConfig().getDouble("spawn.y"),
                            plugin.getConfig().getDouble("spawn.z"),
                            (float) plugin.getConfig().getDouble("spawn.yaw"),
                            (float) plugin.getConfig().getDouble("spawn.pitch"));
                    event.setRespawnLocation(spawnLoc);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND
                || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            plugin.getTeleportManager().setBackLocation(event.getPlayer().getUniqueId(), event.getFrom());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Early exit: only process if the player actually moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        plugin.getTeleportManager().handleMoveDuringTeleport(event.getPlayer());
    }
}

