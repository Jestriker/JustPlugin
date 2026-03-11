package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.util.*;

public class HomeManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final int maxHomes;

    public HomeManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.maxHomes = plugin.getConfig().getInt("homes.max-homes", 5);
    }

    public Map<String, Location> getHomes(UUID uuid) {
        Map<String, Location> homes = new LinkedHashMap<>();
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        ConfigurationSection section = data.getConfigurationSection("homes");
        if (section == null) return homes;
        for (String name : section.getKeys(false)) {
            ConfigurationSection hs = section.getConfigurationSection(name);
            if (hs == null) continue;
            String world = hs.getString("world");
            if (world == null || Bukkit.getWorld(world) == null) continue;
            homes.put(name, new Location(
                    Bukkit.getWorld(world),
                    hs.getDouble("x"), hs.getDouble("y"), hs.getDouble("z"),
                    (float) hs.getDouble("yaw"), (float) hs.getDouble("pitch")
            ));
        }
        return homes;
    }

    public Location getHome(UUID uuid, String name) {
        return getHomes(uuid).get(name.toLowerCase());
    }

    public boolean setHome(UUID uuid, String name, Location location) {
        Map<String, Location> homes = getHomes(uuid);
        if (!homes.containsKey(name.toLowerCase()) && homes.size() >= maxHomes) {
            return false;
        }
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        String path = "homes." + name.toLowerCase();
        data.set(path + ".world", location.getWorld().getName());
        data.set(path + ".x", location.getX());
        data.set(path + ".y", location.getY());
        data.set(path + ".z", location.getZ());
        data.set(path + ".yaw", location.getYaw());
        data.set(path + ".pitch", location.getPitch());
        dataManager.savePlayerData(uuid, data);
        return true;
    }

    public boolean deleteHome(UUID uuid, String name) {
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        if (!data.contains("homes." + name.toLowerCase())) return false;
        data.set("homes." + name.toLowerCase(), null);
        dataManager.savePlayerData(uuid, data);
        return true;
    }

    public Set<String> getHomeNames(UUID uuid) {
        return getHomes(uuid).keySet();
    }

    public int getMaxHomes() {
        return maxHomes;
    }
}

