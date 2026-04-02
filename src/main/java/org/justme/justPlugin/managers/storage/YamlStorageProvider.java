package org.justme.justPlugin.managers.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * YAML flat-file storage provider - wraps the existing file-based I/O
 * from DataManager for backwards compatibility.
 */
public class YamlStorageProvider implements StorageProvider {

    private final JustPlugin plugin;
    private final File playerDataFolder;
    private final File warpsFile;
    private final File bansFile;
    private final File teamsFile;

    private YamlConfiguration warpsConfig;
    private YamlConfiguration bansConfig;
    private YamlConfiguration teamsConfig;

    public YamlStorageProvider(JustPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.bansFile = new File(plugin.getDataFolder(), "bans.yml");
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
    }

    @Override
    public void init() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
        plugin.getLogger().info("[Database] YAML storage provider initialized.");
    }

    @Override
    public void shutdown() {
        // YAML has no connections to close - configs are saved on each write
        plugin.getLogger().info("[Database] YAML storage provider shut down.");
    }

    // --- Player Data ---

    @Override
    public Map<String, Object> getPlayerData(UUID uuid) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        if (!file.exists()) return new LinkedHashMap<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return flattenSection(config);
    }

    @Override
    public void savePlayerData(UUID uuid, Map<String, Object> data) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("[Database] Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }

    @Override
    public Set<UUID> getAllPlayerUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    String name = file.getName().replace(".yml", "");
                    uuids.add(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                    // skip files that aren't valid UUIDs
                }
            }
        }
        return uuids;
    }

    // --- Warps ---

    @Override
    public Map<String, Map<String, Object>> getAllWarps() {
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String key : warpsConfig.getKeys(false)) {
            ConfigurationSection section = warpsConfig.getConfigurationSection(key);
            if (section != null) {
                result.put(key, flattenSection(section));
            }
        }
        return result;
    }

    @Override
    public void saveWarp(String name, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            warpsConfig.set(name + "." + entry.getKey(), entry.getValue());
        }
        saveConfig(warpsConfig, warpsFile, "warps");
    }

    @Override
    public void deleteWarp(String name) {
        warpsConfig.set(name, null);
        saveConfig(warpsConfig, warpsFile, "warps");
    }

    // --- Bans ---

    @Override
    public Map<String, Map<String, Object>> getAllBans() {
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String key : bansConfig.getKeys(false)) {
            ConfigurationSection section = bansConfig.getConfigurationSection(key);
            if (section != null) {
                result.put(key, flattenSection(section));
            }
        }
        return result;
    }

    @Override
    public void saveBan(String key, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            bansConfig.set(key + "." + entry.getKey(), entry.getValue());
        }
        saveConfig(bansConfig, bansFile, "bans");
    }

    @Override
    public void deleteBan(String key) {
        bansConfig.set(key, null);
        saveConfig(bansConfig, bansFile, "bans");
    }

    // --- Teams ---

    @Override
    public Map<String, Map<String, Object>> getAllTeams() {
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String key : teamsConfig.getKeys(false)) {
            ConfigurationSection section = teamsConfig.getConfigurationSection(key);
            if (section != null) {
                result.put(key, flattenSection(section));
            }
        }
        return result;
    }

    @Override
    public void saveTeam(String name, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            teamsConfig.set(name + "." + entry.getKey(), entry.getValue());
        }
        saveConfig(teamsConfig, teamsFile, "teams");
    }

    @Override
    public void deleteTeam(String name) {
        teamsConfig.set(name, null);
        saveConfig(teamsConfig, teamsFile, "teams");
    }

    @Override
    public String getType() {
        return "yaml";
    }

    // --- Helpers ---

    private void saveConfig(YamlConfiguration config, File file, String label) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("[Database] Failed to save " + label + ": " + e.getMessage());
        }
    }

    /**
     * Flattens a ConfigurationSection into a simple key-value map,
     * preserving dot-separated paths for nested sections.
     */
    private Map<String, Object> flattenSection(ConfigurationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(true)) {
            if (!section.isConfigurationSection(key)) {
                map.put(key, section.get(key));
            }
        }
        return map;
    }
}
