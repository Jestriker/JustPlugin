package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {

    private final JustPlugin plugin;
    private final File playerDataFolder;
    private final File warpsFile;
    private final File bansFile;
    private final File teamsFile;

    private YamlConfiguration warpsConfig;
    private YamlConfiguration bansConfig;
    private YamlConfiguration teamsConfig;

    public DataManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.bansFile = new File(plugin.getDataFolder(), "bans.yml");
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        init();
    }

    private void init() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
    }

    // --- Player Data ---
    public YamlConfiguration getPlayerData(UUID uuid) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    public void savePlayerData(UUID uuid, YamlConfiguration config) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }

    // --- Warps ---
    public YamlConfiguration getWarpsConfig() {
        return warpsConfig;
    }

    public void saveWarps() {
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warps: " + e.getMessage());
        }
    }

    public void reloadWarps() {
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    // --- Bans ---
    public YamlConfiguration getBansConfig() {
        return bansConfig;
    }

    public void saveBans() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save bans: " + e.getMessage());
        }
    }

    public void reloadBans() {
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    // --- Teams ---
    public YamlConfiguration getTeamsConfig() {
        return teamsConfig;
    }

    public void saveTeams() {
        try {
            teamsConfig.save(teamsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save teams: " + e.getMessage());
        }
    }

    public void reloadTeams() {
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
    }

    public void reloadAll() {
        reloadWarps();
        reloadBans();
        reloadTeams();
    }
}

