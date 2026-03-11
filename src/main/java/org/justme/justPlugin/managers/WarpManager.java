package org.justme.justPlugin.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpManager {

    private final JustPlugin plugin;
    private final File dataFile;
    private YamlConfiguration config;
    private final Map<String, Location> warps = new HashMap<>();

    public WarpManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "warps.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create warps.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        if (config.getConfigurationSection("warps") != null) {
            for (String name : config.getConfigurationSection("warps").getKeys(false)) {
                Location loc = (Location) config.get("warps." + name);
                if (loc != null) warps.put(name.toLowerCase(), loc);
            }
        }
    }

    public void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save warps.yml: " + e.getMessage());
        }
    }

    public void setWarp(String name, Location location) {
        warps.put(name.toLowerCase(), location);
        config.set("warps." + name.toLowerCase(), location);
        save();
    }

    public Location getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public boolean deleteWarp(String name) {
        if (!warps.containsKey(name.toLowerCase())) return false;
        warps.remove(name.toLowerCase());
        config.set("warps." + name.toLowerCase(), null);
        save();
        return true;
    }

    public boolean renameWarp(String oldName, String newName) {
        Location loc = warps.remove(oldName.toLowerCase());
        if (loc == null) return false;
        config.set("warps." + oldName.toLowerCase(), null);
        warps.put(newName.toLowerCase(), loc);
        config.set("warps." + newName.toLowerCase(), loc);
        save();
        return true;
    }

    public Set<String> getWarpNames() {
        return Collections.unmodifiableSet(warps.keySet());
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }
}
