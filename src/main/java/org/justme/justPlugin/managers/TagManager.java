package org.justme.justPlugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages tags (prefix/suffix labels) that players can equip.
 * Tags are defined in tags.yml or via StorageProvider when a database is configured.
 * Equipped tags are stored in player data files or database.
 */
public class TagManager {

    private final JustPlugin plugin;
    private final DatabaseManager databaseManager;
    private final File tagsFile;
    private YamlConfiguration tagsConfig;

    // tag id -> TagData
    private final Map<String, TagData> tags = new LinkedHashMap<>();

    // player UUID -> equipped tag id
    private final ConcurrentHashMap<UUID, String> equippedTags = new ConcurrentHashMap<>();

    public TagManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        loadTags();
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

    // ==================== Tag Data ====================

    public static class TagData {
        public final String id;
        public final String display;
        public final String type; // "prefix" or "suffix"
        public final String permission;

        public TagData(String id, String display, String type, String permission) {
            this.id = id;
            this.display = display;
            this.type = type;
            this.permission = permission;
        }
    }

    // ==================== Tag CRUD ====================

    /**
     * Get all registered tags.
     */
    public Collection<TagData> getAllTags() {
        return Collections.unmodifiableCollection(tags.values());
    }

    /**
     * Get a tag by ID.
     */
    public TagData getTag(String id) {
        return tags.get(id.toLowerCase());
    }

    /**
     * Check if a tag exists.
     */
    public boolean tagExists(String id) {
        return tags.containsKey(id.toLowerCase());
    }

    /**
     * Create a new tag.
     */
    public void createTag(String id, String display, String type) {
        String lower = id.toLowerCase();
        String permission = "justplugin.tag." + lower;
        TagData tag = new TagData(lower, display, type, permission);
        tags.put(lower, tag);
        saveTags();
    }

    /**
     * Delete a tag. Unequips it from all players who have it equipped.
     */
    public void deleteTag(String id) {
        String lower = id.toLowerCase();
        tags.remove(lower);

        // Unequip from all players who have this tag
        equippedTags.entrySet().removeIf(entry -> entry.getValue().equals(lower));

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                SchedulerUtil.runAsync(plugin, () -> provider.deleteTag(lower));
            }
        }

        saveTags();
    }

    /**
     * Get all tag IDs.
     */
    public Set<String> getTagIds() {
        return Collections.unmodifiableSet(tags.keySet());
    }

    // ==================== Equipped Tags ====================

    /**
     * Get the tag a player has equipped, or null.
     */
    public String getEquippedTagId(UUID uuid) {
        return equippedTags.get(uuid);
    }

    /**
     * Get the TagData for a player's equipped tag, or null.
     */
    public TagData getEquippedTag(UUID uuid) {
        String id = equippedTags.get(uuid);
        if (id == null) return null;
        return tags.get(id);
    }

    /**
     * Equip a tag on a player.
     */
    public void equipTag(UUID uuid, String tagId) {
        equippedTags.put(uuid, tagId.toLowerCase());
        saveEquippedTag(uuid, tagId.toLowerCase());
    }

    /**
     * Unequip a player's tag.
     */
    public void unequipTag(UUID uuid) {
        equippedTags.remove(uuid);
        saveEquippedTag(uuid, null);
    }

    /**
     * Check if a player has a tag equipped.
     */
    public boolean hasEquippedTag(UUID uuid) {
        return equippedTags.containsKey(uuid);
    }

    // ==================== Display Helpers ====================

    /**
     * Get the display component string for a player's equipped prefix tag, or empty string.
     */
    public String getEquippedPrefix(UUID uuid) {
        TagData tag = getEquippedTag(uuid);
        if (tag != null && "prefix".equalsIgnoreCase(tag.type)) {
            return tag.display + " ";
        }
        return "";
    }

    /**
     * Get the display component string for a player's equipped suffix tag, or empty string.
     */
    public String getEquippedSuffix(UUID uuid) {
        TagData tag = getEquippedTag(uuid);
        if (tag != null && "suffix".equalsIgnoreCase(tag.type)) {
            return " " + tag.display;
        }
        return "";
    }

    // ==================== Persistence ====================

    /**
     * Load a player's equipped tag from their data file into the cache.
     */
    public void loadPlayer(UUID uuid) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = provider.getPlayerData(uuid);
                Object tagId = data.get("equipped-tag");
                if (tagId != null && !tagId.toString().isEmpty() && tags.containsKey(tagId.toString())) {
                    equippedTags.put(uuid, tagId.toString());
                }
                return;
            }
        }
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        String tagId = data.getString("equipped-tag");
        if (tagId != null && !tagId.isEmpty() && tags.containsKey(tagId)) {
            equippedTags.put(uuid, tagId);
        }
    }

    /**
     * Remove a player from the in-memory cache (on quit).
     */
    public void unloadPlayer(UUID uuid) {
        // Keep in cache - cheap memory, needed for display lookups
    }

    private void saveEquippedTag(UUID uuid, String tagId) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = provider.getPlayerData(uuid);
                if (tagId != null) {
                    data.put("equipped-tag", tagId);
                } else {
                    data.remove("equipped-tag");
                }
                SchedulerUtil.runAsync(plugin, () -> provider.savePlayerData(uuid, data));
                return;
            }
        }
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (tagId != null) {
            data.set("equipped-tag", tagId);
        } else {
            data.set("equipped-tag", null);
        }
        plugin.getDataManager().savePlayerData(uuid, data);
    }

    /**
     * Load tags from tags.yml or database.
     */
    public void loadTags() {
        tags.clear();
        if (isUsingDatabase()) {
            loadTagsFromDatabase();
        } else {
            loadTagsFromYaml();
        }
    }

    private void loadTagsFromDatabase() {
        StorageProvider provider = getStorageProvider();
        if (provider == null) { loadTagsFromYaml(); return; }

        Map<String, Map<String, Object>> allTags = provider.getAllTags();
        if (allTags.isEmpty()) {
            // Create default tags in database
            createDefaultTags();
            return;
        }

        for (Map.Entry<String, Map<String, Object>> entry : allTags.entrySet()) {
            String id = entry.getKey();
            Map<String, Object> data = entry.getValue();
            String display = data.getOrDefault("display", id).toString();
            String type = data.getOrDefault("type", "prefix").toString();
            String permission = data.getOrDefault("permission", "justplugin.tag." + id).toString();

            tags.put(id.toLowerCase(), new TagData(id.toLowerCase(), display, type, permission));
        }

        plugin.getLogger().info("[Tags] Loaded " + tags.size() + " tag(s) from database.");
    }

    private void loadTagsFromYaml() {
        if (!tagsFile.exists()) {
            // Create default tags.yml with example tags
            tagsConfig = new YamlConfiguration();
            createDefaultTags();
            return;
        } else {
            tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        }

        ConfigurationSection tagsSection = tagsConfig.getConfigurationSection("tags");
        if (tagsSection == null) return;

        for (String id : tagsSection.getKeys(false)) {
            ConfigurationSection tagSection = tagsSection.getConfigurationSection(id);
            if (tagSection == null) continue;

            String display = tagSection.getString("display", id);
            String type = tagSection.getString("type", "prefix");
            String permission = tagSection.getString("permission", "justplugin.tag." + id);

            tags.put(id.toLowerCase(), new TagData(id.toLowerCase(), display, type, permission));
        }

        plugin.getLogger().info("[Tags] Loaded " + tags.size() + " tag(s) from tags.yml.");
    }

    private void createDefaultTags() {
        tags.put("vip", new TagData("vip", "<gold>[VIP]</gold>", "prefix", "justplugin.tag.vip"));
        tags.put("mvp", new TagData("mvp", "<light_purple>[MVP]</light_purple>", "prefix", "justplugin.tag.mvp"));
        tags.put("og", new TagData("og", "<green>[OG]</green>", "suffix", "justplugin.tag.og"));
        saveTags();
    }

    /**
     * Save all tags to tags.yml or database.
     */
    public void saveTags() {
        if (isUsingDatabase()) {
            saveTagsToDatabase();
        } else {
            saveTagsToYaml();
        }
    }

    private void saveTagsToDatabase() {
        StorageProvider provider = getStorageProvider();
        if (provider == null) { saveTagsToYaml(); return; }

        for (TagData tag : tags.values()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("display", tag.display);
            data.put("type", tag.type);
            data.put("permission", tag.permission);
            provider.saveTag(tag.id, data);
        }
    }

    private void saveTagsToYaml() {
        if (tagsConfig == null) {
            tagsConfig = new YamlConfiguration();
        }
        // Clear existing tags section
        tagsConfig.set("tags", null);

        for (TagData tag : tags.values()) {
            String path = "tags." + tag.id;
            tagsConfig.set(path + ".display", tag.display);
            tagsConfig.set(path + ".type", tag.type);
            tagsConfig.set(path + ".permission", tag.permission);
        }

        try {
            tagsConfig.save(tagsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save tags.yml: " + e.getMessage());
        }
    }
}
