package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.util.*;

public class WarpManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final Map<String, Location> warps = new LinkedHashMap<>();

    public WarpManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        loadWarps();
    }

    private void loadWarps() {
        warps.clear();
        YamlConfiguration config = dataManager.getWarpsConfig();
        ConfigurationSection section = config.getConfigurationSection("warps");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            ConfigurationSection ws = section.getConfigurationSection(name);
            if (ws == null) continue;
            String world = ws.getString("world");
            if (world == null || Bukkit.getWorld(world) == null) continue;
            warps.put(name.toLowerCase(), new Location(
                    Bukkit.getWorld(world),
                    ws.getDouble("x"), ws.getDouble("y"), ws.getDouble("z"),
                    (float) ws.getDouble("yaw"), (float) ws.getDouble("pitch")
            ));
        }
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public Location getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public Set<String> getWarpNames() {
        return Collections.unmodifiableSet(warps.keySet());
    }

    public void setWarp(String name, Location location) {
        warps.put(name.toLowerCase(), location);
        saveWarp(name.toLowerCase(), location);
    }

    public boolean deleteWarp(String name) {
        if (!warps.containsKey(name.toLowerCase())) return false;
        warps.remove(name.toLowerCase());
        YamlConfiguration config = dataManager.getWarpsConfig();
        config.set("warps." + name.toLowerCase(), null);
        dataManager.saveWarps();
        return true;
    }

    public boolean renameWarp(String oldName, String newName) {
        Location loc = warps.get(oldName.toLowerCase());
        if (loc == null) return false;
        deleteWarp(oldName);
        setWarp(newName, loc);
        return true;
    }

    private void saveWarp(String name, Location loc) {
        YamlConfiguration config = dataManager.getWarpsConfig();
        String path = "warps." + name;
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        dataManager.saveWarps();
    }

    public void reload() {
        dataManager.reloadWarps();
        loadWarps();
    }
}

