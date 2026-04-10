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
    private final File jailsFile;
    private final File kitsFile;
    private final File mutesFile;
    private final File warnsFile;
    private final File mailFile;
    private final File homesFile;
    private final File nicknamesFile;
    private final File tagsFile;
    private final File transactionsFile;
    private final File vaultsFile;
    private final File ignoresFile;

    private YamlConfiguration warpsConfig;
    private YamlConfiguration bansConfig;
    private YamlConfiguration teamsConfig;
    private YamlConfiguration jailsConfig;
    private YamlConfiguration kitsConfig;
    private YamlConfiguration mutesConfig;
    private YamlConfiguration warnsConfig;
    private YamlConfiguration mailConfig;
    private YamlConfiguration homesConfig;
    private YamlConfiguration nicknamesConfig;
    private YamlConfiguration tagsConfig;
    private YamlConfiguration transactionsConfig;
    private YamlConfiguration vaultsConfig;
    private YamlConfiguration ignoresConfig;

    public YamlStorageProvider(JustPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.bansFile = new File(plugin.getDataFolder(), "bans.yml");
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        this.jailsFile = new File(plugin.getDataFolder(), "jails.yml");
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        this.mutesFile = new File(plugin.getDataFolder(), "mutes.yml");
        this.warnsFile = new File(plugin.getDataFolder(), "warns.yml");
        this.mailFile = new File(plugin.getDataFolder(), "mail.yml");
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        this.nicknamesFile = new File(plugin.getDataFolder(), "nicknames.yml");
        this.tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        this.transactionsFile = new File(plugin.getDataFolder(), "transactions.yml");
        this.vaultsFile = new File(plugin.getDataFolder(), "vaults.yml");
        this.ignoresFile = new File(plugin.getDataFolder(), "ignores.yml");
    }

    @Override
    public void init() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
        jailsConfig = YamlConfiguration.loadConfiguration(jailsFile);
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        mutesConfig = YamlConfiguration.loadConfiguration(mutesFile);
        warnsConfig = YamlConfiguration.loadConfiguration(warnsFile);
        mailConfig = YamlConfiguration.loadConfiguration(mailFile);
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        nicknamesConfig = YamlConfiguration.loadConfiguration(nicknamesFile);
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        transactionsConfig = YamlConfiguration.loadConfiguration(transactionsFile);
        vaultsConfig = YamlConfiguration.loadConfiguration(vaultsFile);
        ignoresConfig = YamlConfiguration.loadConfiguration(ignoresFile);
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

    // --- Jails ---

    @Override
    public Map<String, Map<String, Object>> getAllJails() {
        return getAllFromYaml(jailsConfig, jailsFile);
    }

    @Override
    public void saveJail(String name, Map<String, Object> data) {
        saveToYaml(jailsConfig, jailsFile, name, data, "jails");
    }

    @Override
    public void deleteJail(String name) {
        deleteFromYaml(jailsConfig, jailsFile, name, "jails");
    }

    // --- Kits ---

    @Override
    public Map<String, Map<String, Object>> getAllKits() {
        return getAllFromYaml(kitsConfig, kitsFile);
    }

    @Override
    public void saveKit(String name, Map<String, Object> data) {
        saveToYaml(kitsConfig, kitsFile, name, data, "kits");
    }

    @Override
    public void deleteKit(String name) {
        deleteFromYaml(kitsConfig, kitsFile, name, "kits");
    }

    // --- Mutes ---

    @Override
    public Map<String, Map<String, Object>> getAllMutes() {
        return getAllFromYaml(mutesConfig, mutesFile);
    }

    @Override
    public void saveMute(String key, Map<String, Object> data) {
        saveToYaml(mutesConfig, mutesFile, key, data, "mutes");
    }

    @Override
    public void deleteMute(String key) {
        deleteFromYaml(mutesConfig, mutesFile, key, "mutes");
    }

    // --- Warns ---

    @Override
    public Map<String, Map<String, Object>> getAllWarns() {
        return getAllFromYaml(warnsConfig, warnsFile);
    }

    @Override
    public void saveWarn(String key, Map<String, Object> data) {
        saveToYaml(warnsConfig, warnsFile, key, data, "warns");
    }

    @Override
    public void deleteWarn(String key) {
        deleteFromYaml(warnsConfig, warnsFile, key, "warns");
    }

    // --- Mail ---

    @Override
    public Map<String, Map<String, Object>> getAllMail() {
        return getAllFromYaml(mailConfig, mailFile);
    }

    @Override
    public void saveMail(String key, Map<String, Object> data) {
        saveToYaml(mailConfig, mailFile, key, data, "mail");
    }

    @Override
    public void deleteMail(String key) {
        deleteFromYaml(mailConfig, mailFile, key, "mail");
    }

    // --- Homes ---

    @Override
    public Map<String, Map<String, Object>> getAllHomes() {
        return getAllFromYaml(homesConfig, homesFile);
    }

    @Override
    public void saveHome(String key, Map<String, Object> data) {
        saveToYaml(homesConfig, homesFile, key, data, "homes");
    }

    @Override
    public void deleteHome(String key) {
        deleteFromYaml(homesConfig, homesFile, key, "homes");
    }

    // --- Nicknames ---

    @Override
    public Map<String, Map<String, Object>> getAllNicknames() {
        return getAllFromYaml(nicknamesConfig, nicknamesFile);
    }

    @Override
    public void saveNickname(String key, Map<String, Object> data) {
        saveToYaml(nicknamesConfig, nicknamesFile, key, data, "nicknames");
    }

    @Override
    public void deleteNickname(String key) {
        deleteFromYaml(nicknamesConfig, nicknamesFile, key, "nicknames");
    }

    // --- Tags ---

    @Override
    public Map<String, Map<String, Object>> getAllTags() {
        return getAllFromYaml(tagsConfig, tagsFile);
    }

    @Override
    public void saveTag(String key, Map<String, Object> data) {
        saveToYaml(tagsConfig, tagsFile, key, data, "tags");
    }

    @Override
    public void deleteTag(String key) {
        deleteFromYaml(tagsConfig, tagsFile, key, "tags");
    }

    // --- Transactions ---

    @Override
    public Map<String, Map<String, Object>> getAllTransactions() {
        return getAllFromYaml(transactionsConfig, transactionsFile);
    }

    @Override
    public void saveTransaction(String key, Map<String, Object> data) {
        saveToYaml(transactionsConfig, transactionsFile, key, data, "transactions");
    }

    @Override
    public void deleteTransaction(String key) {
        deleteFromYaml(transactionsConfig, transactionsFile, key, "transactions");
    }

    // --- Vaults ---

    @Override
    public Map<String, Map<String, Object>> getAllVaults() {
        return getAllFromYaml(vaultsConfig, vaultsFile);
    }

    @Override
    public void saveVault(String key, Map<String, Object> data) {
        saveToYaml(vaultsConfig, vaultsFile, key, data, "vaults");
    }

    @Override
    public void deleteVault(String key) {
        deleteFromYaml(vaultsConfig, vaultsFile, key, "vaults");
    }

    // --- Ignores ---

    @Override
    public Map<String, Map<String, Object>> getAllIgnores() {
        return getAllFromYaml(ignoresConfig, ignoresFile);
    }

    @Override
    public void saveIgnore(String key, Map<String, Object> data) {
        saveToYaml(ignoresConfig, ignoresFile, key, data, "ignores");
    }

    @Override
    public void deleteIgnore(String key) {
        deleteFromYaml(ignoresConfig, ignoresFile, key, "ignores");
    }

    // --- Generic YAML helpers ---

    private Map<String, Map<String, Object>> getAllFromYaml(YamlConfiguration config, File file) {
        YamlConfiguration reloaded = YamlConfiguration.loadConfiguration(file);
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String key : reloaded.getKeys(false)) {
            ConfigurationSection section = reloaded.getConfigurationSection(key);
            if (section != null) {
                result.put(key, flattenSection(section));
            }
        }
        return result;
    }

    private void saveToYaml(YamlConfiguration config, File file, String key, Map<String, Object> data, String label) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            config.set(key + "." + entry.getKey(), entry.getValue());
        }
        saveConfig(config, file, label);
    }

    private void deleteFromYaml(YamlConfiguration config, File file, String key, String label) {
        config.set(key, null);
        saveConfig(config, file, label);
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
