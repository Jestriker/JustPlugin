package org.justme.justPlugin.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final JustPlugin plugin;
    private final File dataFile;
    private YamlConfiguration config;
    // playerUUID -> homeName -> Location
    private final Map<UUID, Map<String, Location>> homes = new HashMap<>();

    public HomeManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "homes.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create homes.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Location> playerHomes = new HashMap<>();
            if (config.getConfigurationSection(uuidStr) != null) {
                for (String homeName : config.getConfigurationSection(uuidStr).getKeys(false)) {
                    Location loc = (Location) config.get(uuidStr + "." + homeName);
                    if (loc != null) playerHomes.put(homeName, loc);
                }
            }
            homes.put(uuid, playerHomes);
        }
    }

    public void save() {
        for (Map.Entry<UUID, Map<String, Location>> entry : homes.entrySet()) {
            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {
                config.set(entry.getKey().toString() + "." + homeEntry.getKey(), homeEntry.getValue());
            }
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save homes.yml: " + e.getMessage());
        }
    }

    public void setHome(UUID playerId, String name, Location location) {
        homes.computeIfAbsent(playerId, k -> new HashMap<>()).put(name.toLowerCase(), location);
        config.set(playerId.toString() + "." + name.toLowerCase(), location);
        try { config.save(dataFile); } catch (IOException e) { plugin.getLogger().severe("Error saving home: " + e.getMessage()); }
    }

    public Location getHome(UUID playerId, String name) {
        Map<String, Location> playerHomes = homes.get(playerId);
        if (playerHomes == null) return null;
        return playerHomes.get(name.toLowerCase());
    }

    public boolean deleteHome(UUID playerId, String name) {
        Map<String, Location> playerHomes = homes.get(playerId);
        if (playerHomes == null || !playerHomes.containsKey(name.toLowerCase())) return false;
        playerHomes.remove(name.toLowerCase());
        config.set(playerId.toString() + "." + name.toLowerCase(), null);
        try { config.save(dataFile); } catch (IOException e) { plugin.getLogger().severe("Error saving home: " + e.getMessage()); }
        return true;
    }

    public Set<String> getHomes(UUID playerId) {
        Map<String, Location> playerHomes = homes.get(playerId);
        if (playerHomes == null) return Collections.emptySet();
        return Collections.unmodifiableSet(playerHomes.keySet());
    }

    public boolean hasHome(UUID playerId, String name) {
        Map<String, Location> playerHomes = homes.get(playerId);
        return playerHomes != null && playerHomes.containsKey(name.toLowerCase());
    }
}
