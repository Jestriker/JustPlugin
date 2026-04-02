package org.justme.justPlugin.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;

/**
 * Manages spawn protection - prevents block breaking, block placing, PvP,
 * and explosions within a configurable radius of the world spawn.
 */
public class SpawnProtectionManager {

    private final JustPlugin plugin;

    public SpawnProtectionManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("spawn-protection.enabled", false);
    }

    public int getRadius() {
        return plugin.getConfig().getInt("spawn-protection.radius", 50);
    }

    public boolean isPreventBlockBreak() {
        return plugin.getConfig().getBoolean("spawn-protection.prevent-block-break", true);
    }

    public boolean isPreventBlockPlace() {
        return plugin.getConfig().getBoolean("spawn-protection.prevent-block-place", true);
    }

    public boolean isPreventPvp() {
        return plugin.getConfig().getBoolean("spawn-protection.prevent-pvp", true);
    }

    public boolean isPreventExplosions() {
        return plugin.getConfig().getBoolean("spawn-protection.prevent-explosions", true);
    }

    /**
     * Checks if a location is within the spawn protection radius.
     * Uses the world's spawn location as center, checking X/Z distance only.
     */
    public boolean isInSpawnRadius(Location location) {
        if (location == null || location.getWorld() == null) return false;

        World world = location.getWorld();
        Location spawn = world.getSpawnLocation();
        int radius = getRadius();

        double dx = location.getX() - spawn.getX();
        double dz = location.getZ() - spawn.getZ();
        return (dx * dx + dz * dz) <= (double) radius * radius;
    }

    /**
     * Checks if a player can bypass spawn protection.
     */
    public boolean canBypass(Player player) {
        return player.hasPermission("justplugin.spawnprotection.bypass");
    }
}
