package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Manages the database configuration and storage provider lifecycle.
 * Loads database.yml, initializes the chosen provider, and falls back to YAML on failure.
 */
public class DatabaseManager {

    private final JustPlugin plugin;
    private StorageProvider provider;
    private YamlConfiguration dbConfig;
    private final File dbConfigFile;

    public DatabaseManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dbConfigFile = new File(plugin.getDataFolder(), "database.yml");
        loadConfig();
        initProvider();
    }

    private void loadConfig() {
        // Save default database.yml if it doesn't exist
        if (!dbConfigFile.exists()) {
            plugin.saveResource("database.yml", false);
        }
        dbConfig = YamlConfiguration.loadConfiguration(dbConfigFile);

        // Merge missing keys from default
        InputStream defaultStream = plugin.getResource("database.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (defaults.isConfigurationSection(key)) continue;
                if (!dbConfig.contains(key, true)) {
                    dbConfig.set(key, defaults.get(key));
                    changed = true;
                }
            }
            if (changed) {
                try {
                    dbConfig.save(dbConfigFile);
                } catch (IOException e) {
                    plugin.getLogger().warning("[Database] Failed to save database.yml defaults: " + e.getMessage());
                }
            }
        }
    }

    private void initProvider() {
        String type = dbConfig.getString("storage-type", "yaml").toLowerCase().trim();

        switch (type) {
            case "sqlite" -> {
                try {
                    String file = dbConfig.getString("sqlite.file", "justplugin.db");
                    SQLiteStorageProvider sqlite = new SQLiteStorageProvider(plugin, file);
                    sqlite.init();
                    provider = sqlite;
                    return;
                } catch (Exception e) {
                    plugin.getLogger().severe("[Database] Failed to initialize SQLite: " + e.getMessage());
                    plugin.getLogger().warning("[Database] Falling back to YAML storage.");
                }
            }
            case "mysql" -> {
                try {
                    String host = dbConfig.getString("mysql.host", "localhost");
                    int port = dbConfig.getInt("mysql.port", 3306);
                    String database = dbConfig.getString("mysql.database", "justplugin");
                    String username = dbConfig.getString("mysql.username", "root");
                    String password = dbConfig.getString("mysql.password", "");
                    int poolSize = dbConfig.getInt("mysql.pool-size", 10);
                    boolean useSsl = dbConfig.getBoolean("mysql.use-ssl", false);

                    MySQLStorageProvider mysql = new MySQLStorageProvider(plugin, host, port, database,
                            username, password, poolSize, useSsl);
                    mysql.init();
                    provider = mysql;
                    return;
                } catch (Exception e) {
                    plugin.getLogger().severe("[Database] Failed to initialize MySQL: " + e.getMessage());
                    plugin.getLogger().warning("[Database] Falling back to YAML storage.");
                }
            }
            case "yaml" -> {
                // fall through to yaml init below
            }
            default -> {
                plugin.getLogger().warning("[Database] Unknown storage type '" + type + "'. Using YAML.");
            }
        }

        // Default / fallback: YAML
        try {
            YamlStorageProvider yaml = new YamlStorageProvider(plugin);
            yaml.init();
            provider = yaml;
        } catch (Exception e) {
            plugin.getLogger().severe("[Database] Failed to initialize YAML storage: " + e.getMessage());
            // This should never happen, but just in case
            throw new RuntimeException("Cannot initialize any storage provider!", e);
        }
    }

    /**
     * Returns the active storage provider.
     */
    public StorageProvider getProvider() {
        return provider;
    }

    /**
     * Returns the human-readable storage type name.
     */
    public String getStorageType() {
        return provider != null ? provider.getType() : "none";
    }

    /**
     * Shuts down the active storage provider gracefully.
     */
    public void shutdown() {
        if (provider != null) {
            provider.shutdown();
        }
    }
}
