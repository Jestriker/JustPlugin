package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.*;

public class HomeManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final DatabaseManager databaseManager;
    private final int maxHomes;

    public HomeManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.maxHomes = plugin.getConfig().getInt("homes.max-homes", 5);
    }

    private boolean isUsingDatabase() {
        if (databaseManager == null) return false;
        StorageProvider provider = databaseManager.getProvider();
        if (provider == null) return false;
        String type = provider.getType();
        return "sqlite".equals(type) || "mysql".equals(type);
    }

    private StorageProvider getStorageProvider() {
        return databaseManager != null ? databaseManager.getProvider() : null;
    }

    public Map<String, Location> getHomes(UUID uuid) {
        if (isUsingDatabase()) {
            return getHomesFromDatabase(uuid);
        } else {
            return getHomesFromYaml(uuid);
        }
    }

    private Map<String, Location> getHomesFromDatabase(UUID uuid) {
        Map<String, Location> homes = new LinkedHashMap<>();
        StorageProvider provider = getStorageProvider();
        if (provider == null) return getHomesFromYaml(uuid);

        Map<String, Map<String, Object>> allHomes = provider.getAllHomes();
        String prefix = uuid.toString() + ".";
        for (Map.Entry<String, Map<String, Object>> entry : allHomes.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) continue;
            String name = entry.getKey().substring(prefix.length());
            Map<String, Object> data = entry.getValue();

            String world = data.getOrDefault("world", "").toString();
            if (world.isEmpty() || Bukkit.getWorld(world) == null) continue;

            homes.put(name, new Location(
                    Bukkit.getWorld(world),
                    data.containsKey("x") ? ((Number) data.get("x")).doubleValue() : 0,
                    data.containsKey("y") ? ((Number) data.get("y")).doubleValue() : 0,
                    data.containsKey("z") ? ((Number) data.get("z")).doubleValue() : 0,
                    data.containsKey("yaw") ? ((Number) data.get("yaw")).floatValue() : 0,
                    data.containsKey("pitch") ? ((Number) data.get("pitch")).floatValue() : 0
            ));
        }
        return homes;
    }

    private Map<String, Location> getHomesFromYaml(UUID uuid) {
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

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("world", location.getWorld().getName());
                data.put("x", location.getX());
                data.put("y", location.getY());
                data.put("z", location.getZ());
                data.put("yaw", (double) location.getYaw());
                data.put("pitch", (double) location.getPitch());
                String key = uuid.toString() + "." + name.toLowerCase();
                SchedulerUtil.runAsync(plugin, () -> provider.saveHome(key, data));
            }
        } else {
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            String path = "homes." + name.toLowerCase();
            data.set(path + ".world", location.getWorld().getName());
            data.set(path + ".x", location.getX());
            data.set(path + ".y", location.getY());
            data.set(path + ".z", location.getZ());
            data.set(path + ".yaw", location.getYaw());
            data.set(path + ".pitch", location.getPitch());
            dataManager.savePlayerData(uuid, data);
        }
        return true;
    }

    public boolean deleteHome(UUID uuid, String name) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Location> homes = getHomes(uuid);
                if (!homes.containsKey(name.toLowerCase())) return false;
                String key = uuid.toString() + "." + name.toLowerCase();
                SchedulerUtil.runAsync(plugin, () -> provider.deleteHome(key));
                return true;
            }
        }

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
