package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player nicknames with MiniMessage color support,
 * permission-based formatting, blocked words, and persistence.
 */
public class NickManager {

    private final JustPlugin plugin;
    private final DatabaseManager databaseManager;
    private final ConcurrentHashMap<UUID, String> nicknames = new ConcurrentHashMap<>();

    public NickManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
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

    // ==================== Configuration ====================

    public int getMaxLength() {
        return plugin.getConfig().getInt("nickname.max-length", 16);
    }

    public boolean allowDuplicates() {
        return plugin.getConfig().getBoolean("nickname.allow-duplicates", true);
    }

    public List<String> getBlockedWords() {
        return plugin.getConfig().getStringList("nickname.blocked-words");
    }

    // ==================== Nickname CRUD ====================

    /**
     * Get a player's nickname, or null if not set.
     */
    public String getNickname(UUID uuid) {
        return nicknames.get(uuid);
    }

    /**
     * Set a player's nickname (raw MiniMessage string).
     * Persists to player data file or database.
     */
    public void setNickname(UUID uuid, String nickname) {
        nicknames.put(uuid, nickname);
        saveToPlayerData(uuid, nickname);
    }

    /**
     * Remove a player's nickname.
     */
    public void removeNickname(UUID uuid) {
        nicknames.remove(uuid);
        saveToPlayerData(uuid, null);
    }

    /**
     * Check if a player has a nickname set.
     */
    public boolean hasNickname(UUID uuid) {
        return nicknames.containsKey(uuid);
    }

    // ==================== Validation ====================

    /**
     * Strip MiniMessage tags from input and return plain text length.
     */
    public int getPlainLength(String input) {
        Component parsed = MiniMessage.miniMessage().deserialize(input);
        String plain = PlainTextComponentSerializer.plainText().serialize(parsed);
        return plain.length();
    }

    /**
     * Check if a nickname contains any blocked words (case-insensitive).
     */
    public boolean containsBlockedWord(String nickname) {
        String plain = PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage().deserialize(nickname))
                .toLowerCase();
        for (String blocked : getBlockedWords()) {
            if (plain.contains(blocked.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a nickname is already in use by another player.
     */
    public boolean isDuplicate(UUID uuid, String nickname) {
        if (allowDuplicates()) return false;
        String plainNew = PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage().deserialize(nickname))
                .toLowerCase();
        for (var entry : nicknames.entrySet()) {
            if (entry.getKey().equals(uuid)) continue;
            String plainExisting = PlainTextComponentSerializer.plainText()
                    .serialize(MiniMessage.miniMessage().deserialize(entry.getValue()))
                    .toLowerCase();
            if (plainExisting.equals(plainNew)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strip color/format tags the player does not have permission for.
     * Returns a sanitized nickname string.
     */
    public String stripUnpermitted(Player player, String nickname) {
        boolean hasColor = player.hasPermission("justplugin.nick.color");
        boolean hasFormat = player.hasPermission("justplugin.nick.format");
        boolean hasRainbow = player.hasPermission("justplugin.nick.rainbow");

        if (hasColor && hasFormat && hasRainbow) {
            return nickname; // Player has all permissions
        }

        String result = nickname;

        if (!hasRainbow) {
            // Strip <rainbow> and <gradient:...> tags
            result = result.replaceAll("<rainbow(:[^>]*)?>", "");
            result = result.replaceAll("</rainbow>", "");
            result = result.replaceAll("<gradient(:[^>]*)?>", "");
            result = result.replaceAll("</gradient>", "");
        }

        if (!hasColor) {
            // Strip color tags: <red>, <#ff0000>, <color:red>, etc.
            result = result.replaceAll("<#[0-9a-fA-F]{6}>", "");
            result = result.replaceAll("<color:[^>]+>", "");
            result = result.replaceAll("</color>", "");
            // Named colors
            String[] colors = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
                    "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua",
                    "red", "light_purple", "yellow", "white"};
            for (String color : colors) {
                result = result.replace("<" + color + ">", "");
                result = result.replace("</" + color + ">", "");
            }
        }

        if (!hasFormat) {
            // Strip formatting tags: <bold>, <italic>, <underlined>, <strikethrough>, <obfuscated>
            String[] formats = {"bold", "b", "italic", "em", "i", "underlined", "u",
                    "strikethrough", "st", "obfuscated", "obf"};
            for (String fmt : formats) {
                result = result.replace("<" + fmt + ">", "");
                result = result.replace("</" + fmt + ">", "");
            }
        }

        return result;
    }

    // ==================== Display ====================

    /**
     * Get the display name component for a player (nickname or real name).
     */
    public Component getDisplayName(Player player) {
        String nick = nicknames.get(player.getUniqueId());
        if (nick != null) {
            return MiniMessage.miniMessage().deserialize(nick);
        }
        return Component.text(player.getName());
    }

    /**
     * Apply the nickname as the player's display name.
     */
    public void applyDisplayName(Player player) {
        player.displayName(getDisplayName(player));
    }

    // ==================== Persistence ====================

    /**
     * Load a player's nickname from their data file into the cache.
     */
    public void loadPlayer(UUID uuid) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = provider.getPlayerData(uuid);
                Object nick = data.get("nickname");
                if (nick != null && !nick.toString().isEmpty()) {
                    nicknames.put(uuid, nick.toString());
                }
                return;
            }
        }
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        String nick = data.getString("nickname");
        if (nick != null && !nick.isEmpty()) {
            nicknames.put(uuid, nick);
        }
    }

    /**
     * Remove a player from the in-memory cache (on quit).
     * Data remains in the player file.
     */
    public void unloadPlayer(UUID uuid) {
        // Keep in cache - it's cheap and needed for duplicate checks
    }

    private void saveToPlayerData(UUID uuid, String nickname) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = provider.getPlayerData(uuid);
                if (nickname != null) {
                    data.put("nickname", nickname);
                } else {
                    data.remove("nickname");
                }
                SchedulerUtil.runAsync(plugin, () -> provider.savePlayerData(uuid, data));
                return;
            }
        }
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (nickname != null) {
            data.set("nickname", nickname);
        } else {
            data.set("nickname", null);
        }
        plugin.getDataManager().savePlayerData(uuid, data);
    }
}
