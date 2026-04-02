package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final JustPlugin plugin;
    private final File playerDataFolder;
    private final File warpsFile;
    private final File bansFile;
    private final File teamsFile;

    private YamlConfiguration warpsConfig;
    private YamlConfiguration bansConfig;
    private YamlConfiguration teamsConfig;

    // In-memory cache for player data - avoids repeated disk reads
    private final ConcurrentHashMap<UUID, YamlConfiguration> playerDataCache = new ConcurrentHashMap<>();

    private BukkitTask autoSaveTask;

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

    public File getPlayerDataFolder() {
        return playerDataFolder;
    }

    // --- Player Data ---

    /**
     * Returns cached player data, loading from disk on cache miss.
     */
    public YamlConfiguration getPlayerData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, id -> {
            File file = new File(playerDataFolder, id.toString() + ".yml");
            return YamlConfiguration.loadConfiguration(file);
        });
    }

    public void savePlayerData(UUID uuid, YamlConfiguration config) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Saves player data asynchronously to avoid blocking the main thread.
     */
    public void savePlayerDataAsync(UUID uuid, YamlConfiguration config) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerData(uuid, config));
    }

    /**
     * Loads player data into the cache (call on join).
     */
    public void loadPlayerDataToCache(UUID uuid) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        playerDataCache.put(uuid, YamlConfiguration.loadConfiguration(file));
    }

    /**
     * Saves and removes player data from the cache (call on quit).
     */
    public void unloadPlayerData(UUID uuid) {
        YamlConfiguration config = playerDataCache.remove(uuid);
        if (config != null) {
            savePlayerData(uuid, config);
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

    /**
     * Saves warps asynchronously.
     */
    public void saveWarpsAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveWarps);
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

    /**
     * Saves bans asynchronously.
     */
    public void saveBansAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveBans);
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

    /**
     * Saves teams asynchronously.
     */
    public void saveTeamsAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveTeams);
    }

    public void reloadTeams() {
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
    }

    public void reloadAll() {
        reloadWarps();
        reloadBans();
        reloadTeams();
    }

    // --- Auto-save ---

    /**
     * Starts the auto-save task that periodically saves all cached player data asynchronously.
     * Interval is read from config (data.auto-save-interval, in minutes, default 5).
     */
    public void startAutoSave() {
        int intervalMinutes = plugin.getConfig().getInt("data.auto-save-interval", 5);
        long intervalTicks = intervalMinutes * 60L * 20L; // minutes -> ticks
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (var entry : playerDataCache.entrySet()) {
                savePlayerData(entry.getKey(), entry.getValue());
            }
            saveWarps();
            saveBans();
            saveTeams();
        }, intervalTicks, intervalTicks);
        plugin.getLogger().info("[DataManager] Auto-save started (every " + intervalMinutes + " minutes).");
    }

    /**
     * Stops the auto-save task.
     */
    public void stopAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }

    /**
     * Synchronously saves all cached player data, warps, bans, and teams.
     * Call this during plugin shutdown to ensure no data is lost.
     */
    public void saveAllCached() {
        for (var entry : playerDataCache.entrySet()) {
            savePlayerData(entry.getKey(), entry.getValue());
        }
        saveWarps();
        saveBans();
        saveTeams();
        plugin.getLogger().info("[DataManager] All cached data saved.");
    }
}
