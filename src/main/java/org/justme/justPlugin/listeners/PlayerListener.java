package org.justme.justPlugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.justme.justPlugin.JustPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared state holder and persistence helper used by categorized sub-listeners.
 * Manages god mode, death locations, back locations, and inventory snapshots.
 * <p>
 * Event handling is delegated to categorized sub-listeners in subpackages:
 * - connection/ - Login, join, quit events
 * - chat/       - Async chat, mute checks, formatting
 * - combat/     - Damage, god mode, potion effects
 * - player/     - Death, respawn, teleport, movement, advancements
 * - server/     - Server list ping, tab completion
 * - inventory/  - PayNote redemption
 */
public class PlayerListener {

    private final JustPlugin plugin;
    private final Set<UUID> godMode = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();

    public PlayerListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    // God mode helpers
    public boolean isGodMode(UUID uuid) {
        return godMode.contains(uuid);
    }

    public void toggleGodMode(UUID uuid) {
        if (godMode.contains(uuid)) {
            godMode.remove(uuid);
        } else {
            godMode.add(uuid);
        }
    }

    public void removeGodMode(UUID uuid) {
        godMode.remove(uuid);
    }

    // Death location helpers
    public Location getDeathLocation(UUID uuid) {
        return deathLocations.get(uuid);
    }

    public boolean hasDeathLocation(UUID uuid) {
        return deathLocations.containsKey(uuid);
    }

    public void setDeathLocation(UUID uuid, Location loc) {
        deathLocations.put(uuid, loc.clone());
    }

    // --- Persistence helpers for back & death locations ---

    public void saveBackLocation(UUID uuid) {
        Location loc = plugin.getTeleportManager().getBackLocation(uuid);
        if (loc == null) return;
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("lastBack.world", loc.getWorld().getName());
        data.set("lastBack.x", loc.getX());
        data.set("lastBack.y", loc.getY());
        data.set("lastBack.z", loc.getZ());
        data.set("lastBack.yaw", loc.getYaw());
        data.set("lastBack.pitch", loc.getPitch());
        plugin.getDataManager().savePlayerDataAsync(uuid, data);
    }

    public void loadBackLocation(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (!data.contains("lastBack.world")) return;
        World world = Bukkit.getWorld(data.getString("lastBack.world", ""));
        if (world == null) return;
        Location loc = new Location(world,
                data.getDouble("lastBack.x"),
                data.getDouble("lastBack.y"),
                data.getDouble("lastBack.z"),
                (float) data.getDouble("lastBack.yaw"),
                (float) data.getDouble("lastBack.pitch"));
        plugin.getTeleportManager().setBackLocation(uuid, loc);
    }

    public void saveDeathLocation(UUID uuid) {
        Location loc = deathLocations.get(uuid);
        if (loc == null) return;
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("lastDeath.world", loc.getWorld().getName());
        data.set("lastDeath.x", loc.getX());
        data.set("lastDeath.y", loc.getY());
        data.set("lastDeath.z", loc.getZ());
        data.set("lastDeath.yaw", loc.getYaw());
        data.set("lastDeath.pitch", loc.getPitch());
        plugin.getDataManager().savePlayerDataAsync(uuid, data);
    }

    public void loadDeathLocation(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (!data.contains("lastDeath.world")) return;
        World world = Bukkit.getWorld(data.getString("lastDeath.world", ""));
        if (world == null) return;
        Location loc = new Location(world,
                data.getDouble("lastDeath.x"),
                data.getDouble("lastDeath.y"),
                data.getDouble("lastDeath.z"),
                (float) data.getDouble("lastDeath.yaw"),
                (float) data.getDouble("lastDeath.pitch"));
        deathLocations.put(uuid, loc);
    }

    public void saveInventorySnapshot(Player player) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);

        // Save main inventory (slots 0-35)
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            data.set("inventory.slot_" + i, (i < contents.length && contents[i] != null) ? contents[i] : null);
        }

        // Save armor
        data.set("inventory.helmet", player.getInventory().getHelmet());
        data.set("inventory.chestplate", player.getInventory().getChestplate());
        data.set("inventory.leggings", player.getInventory().getLeggings());
        data.set("inventory.boots", player.getInventory().getBoots());

        // Save offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        data.set("inventory.offhand", offhand.getType() != Material.AIR ? offhand : null);

        // Save ender chest (27 slots)
        ItemStack[] enderContents = player.getEnderChest().getContents();
        for (int i = 0; i < 27; i++) {
            data.set("enderchest.slot_" + i, (i < enderContents.length && enderContents[i] != null) ? enderContents[i] : null);
        }

        plugin.getDataManager().savePlayerDataAsync(uuid, data);
    }
}
