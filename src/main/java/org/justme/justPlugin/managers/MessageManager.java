package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all configurable text messages for JustPlugin.
 * <p>
 * Messages are loaded from YAML files in the {@code texts/} folder, organized by category.
 * Each file contains message keys with MiniMessage-formatted values and placeholder documentation.
 * <p>
 * Usage:
 * <pre>
 *   MessageManager mm = plugin.getMessageManager();
 *   // Simple message (no placeholders)
 *   player.sendMessage(mm.error("general.no-permission"));
 *   // Message with placeholders
 *   player.sendMessage(mm.success("teleport.tpa-sent", "{player}", target.getName()));
 * </pre>
 */
public class MessageManager {

    private final JustPlugin plugin;
    private final File textsFolder;
    private final Map<String, String> messages = new ConcurrentHashMap<>();

    // All text config file names (relative to texts/ folder)
    private static final String[] TEXT_FILES = {
            "general.yml",
            "teleport.yml",
            "warp.yml",
            "home.yml",
            "economy.yml",
            "moderation.yml",
            "player.yml",
            "chat.yml",
            "inventory.yml",
            "world.yml",
            "team.yml",
            "trade.yml",
            "maintenance.yml",
            "info.yml",
            "misc.yml",
            "nick.yml",
            "kits.yml"
    };

    public MessageManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.textsFolder = new File(plugin.getDataFolder(), "texts");
        loadAll();
    }

    // ==================== Loading ====================

    /**
     * Loads all message files from the texts/ folder.
     * Creates defaults from resources if files don't exist.
     * Migrates any missing keys from defaults.
     */
    public void loadAll() {
        messages.clear();

        // Create texts/ folder if it doesn't exist
        if (!textsFolder.exists()) {
            textsFolder.mkdirs();
        }

        for (String fileName : TEXT_FILES) {
            File file = new File(textsFolder, fileName);

            // Save default from resources if not present
            if (!file.exists()) {
                saveDefaultTextFile(fileName);
            }

            // Load the file
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Migrate missing keys from default
            migrateTextFile(fileName, config, file);

            // Determine category prefix from filename (e.g., "general.yml" -> "general")
            String category = fileName.replace(".yml", "");

            // Load all keys into the messages map
            for (String key : config.getKeys(true)) {
                if (config.isConfigurationSection(key)) continue;
                Object value = config.get(key);
                if (value instanceof String strValue) {
                    messages.put(category + "." + key, strValue);
                }
            }
        }

        plugin.getLogger().info("[Messages] Loaded " + messages.size() + " configurable messages from " + TEXT_FILES.length + " text files.");
    }

    /**
     * Saves a default text file from the JAR resources to the texts/ folder.
     */
    private void saveDefaultTextFile(String fileName) {
        String resourcePath = "texts/" + fileName;
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in != null) {
                File outFile = new File(textsFolder, fileName);
                Files.copy(in, outFile.toPath());
            } else {
                plugin.getLogger().warning("[Messages] Default resource not found: " + resourcePath);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("[Messages] Failed to save default " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Migrates missing keys from the default resource into an existing config file.
     */
    private void migrateTextFile(String fileName, YamlConfiguration config, File file) {
        String resourcePath = "texts/" + fileName;
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return;
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (defaults.isConfigurationSection(key)) continue;
                if (!config.contains(key, true)) {
                    config.set(key, defaults.get(key));
                    changed = true;
                }
            }
            if (changed) {
                config.save(file);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[Messages] Failed to migrate " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Reload all message files.
     */
    public void reload() {
        loadAll();
    }

    // ==================== Message Retrieval ====================

    /**
     * Get a raw message string by key (e.g., "general.no-permission").
     * Returns the key itself if not found.
     */
    public String raw(String key) {
        return messages.getOrDefault(key, key);
    }

    /**
     * Get a raw message string with placeholder replacements.
     * Placeholders are provided as pairs: "{player}", "Steve", "{amount}", "100"
     */
    public String raw(String key, String... replacements) {
        String msg = raw(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    /**
     * Get a Component with the plugin prefix + message formatted as an error (red).
     */
    public Component error(String key, String... replacements) {
        return CC.error(raw(key, replacements));
    }

    /**
     * Get a Component with the plugin prefix + message formatted as success (green).
     */
    public Component success(String key, String... replacements) {
        return CC.success(raw(key, replacements));
    }

    /**
     * Get a Component with the plugin prefix + message formatted as info (gray).
     */
    public Component info(String key, String... replacements) {
        return CC.info(raw(key, replacements));
    }

    /**
     * Get a Component with the plugin prefix + message formatted as warning (yellow).
     */
    public Component warning(String key, String... replacements) {
        return CC.warning(raw(key, replacements));
    }

    /**
     * Get a Component with the plugin prefix + raw MiniMessage content.
     */
    public Component prefixed(String key, String... replacements) {
        return CC.prefixed(raw(key, replacements));
    }

    /**
     * Get a Component with no prefix, just the translated message.
     */
    public Component translate(String key, String... replacements) {
        return CC.translate(raw(key, replacements));
    }

    /**
     * Get a continuation line (indented, no prefix).
     */
    public Component line(String key, String... replacements) {
        return CC.line(raw(key, replacements));
    }

    /**
     * Check if a message key exists.
     */
    public boolean has(String key) {
        return messages.containsKey(key);
    }
}




