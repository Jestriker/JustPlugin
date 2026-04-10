package org.justme.justPlugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages offline mail stored in mail.yml or via StorageProvider when a database is configured.
 * Each player's mail is stored under players.{uuid} as a list of entries
 * containing sender, sender-name, message, timestamp, and read status.
 */
public class MailManager {

    private final JustPlugin plugin;
    private final DatabaseManager databaseManager;
    private final File mailFile;
    private YamlConfiguration mailConfig;

    public MailManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.mailFile = new File(plugin.getDataFolder(), "mail.yml");
        load();
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

    private void load() {
        if (!mailFile.exists()) {
            try {
                mailFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("[Mail] Failed to create mail.yml: " + e.getMessage());
            }
        }
        mailConfig = YamlConfiguration.loadConfiguration(mailFile);
    }

    private void save() {
        try {
            mailConfig.save(mailFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Mail] Failed to save mail.yml: " + e.getMessage());
        }
    }

    /**
     * Returns the maximum number of mail messages a player can hold.
     */
    public int getMaxMail() {
        return plugin.getConfig().getInt("mail.max-per-player", 50);
    }

    /**
     * Sends a mail message to the target player (can be offline).
     *
     * @return true if sent successfully, false if mailbox is full
     */
    public boolean sendMail(UUID senderUUID, String senderName, UUID targetUUID, String message) {
        List<Map<String, Object>> mail = getMail(targetUUID);
        if (mail.size() >= getMaxMail()) {
            return false;
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("sender", senderUUID.toString());
        entry.put("sender-name", senderName);
        entry.put("message", message);
        entry.put("timestamp", System.currentTimeMillis());
        entry.put("read", false);

        mail.add(entry);
        setMail(targetUUID, mail);

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> mailData = new LinkedHashMap<>();
                mailData.put("entries", mail);
                SchedulerUtil.runAsync(plugin, () -> provider.saveMail(targetUUID.toString(), mailData));
            }
        } else {
            save();
        }
        return true;
    }

    /**
     * Returns all mail entries for the given player.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMail(UUID uuid) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Map<String, Object>> allMail = provider.getAllMail();
                Map<String, Object> playerMail = allMail.get(uuid.toString());
                if (playerMail != null && playerMail.containsKey("entries")) {
                    Object entries = playerMail.get("entries");
                    if (entries instanceof List<?> list) {
                        List<Map<String, Object>> result = new ArrayList<>();
                        for (Object obj : list) {
                            if (obj instanceof Map) {
                                result.add((Map<String, Object>) obj);
                            }
                        }
                        return result;
                    }
                }
                return new ArrayList<>();
            }
        }

        String path = "players." + uuid.toString();
        List<?> raw = mailConfig.getList(path);
        if (raw == null) return new ArrayList<>();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object obj : raw) {
            if (obj instanceof Map) {
                result.add((Map<String, Object>) obj);
            }
        }
        return result;
    }

    /**
     * Returns the number of unread mail messages for the given player.
     */
    public int getUnreadCount(UUID uuid) {
        List<Map<String, Object>> mail = getMail(uuid);
        int count = 0;
        for (Map<String, Object> entry : mail) {
            Object read = entry.get("read");
            if (read == null || Boolean.FALSE.equals(read)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Marks all mail as read for the given player.
     */
    public void markAllRead(UUID uuid) {
        List<Map<String, Object>> mail = getMail(uuid);
        for (Map<String, Object> entry : mail) {
            entry.put("read", true);
        }
        setMail(uuid, mail);

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> mailData = new LinkedHashMap<>();
                mailData.put("entries", mail);
                SchedulerUtil.runAsync(plugin, () -> provider.saveMail(uuid.toString(), mailData));
            }
        } else {
            save();
        }
    }

    /**
     * Clears all read messages for the given player.
     *
     * @return the number of messages removed
     */
    public int clearRead(UUID uuid) {
        List<Map<String, Object>> mail = getMail(uuid);
        int before = mail.size();
        mail.removeIf(entry -> Boolean.TRUE.equals(entry.get("read")));
        int removed = before - mail.size();
        setMail(uuid, mail);

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> mailData = new LinkedHashMap<>();
                mailData.put("entries", mail);
                SchedulerUtil.runAsync(plugin, () -> provider.saveMail(uuid.toString(), mailData));
            }
        } else {
            save();
        }
        return removed;
    }

    /**
     * Clears all messages for the given player.
     *
     * @return the number of messages removed
     */
    public int clearAll(UUID uuid) {
        List<Map<String, Object>> mail = getMail(uuid);
        int count = mail.size();

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                SchedulerUtil.runAsync(plugin, () -> provider.deleteMail(uuid.toString()));
            }
        } else {
            mailConfig.set("players." + uuid.toString(), null);
            save();
        }
        return count;
    }

    private void setMail(UUID uuid, List<Map<String, Object>> mail) {
        if (!isUsingDatabase()) {
            mailConfig.set("players." + uuid.toString(), mail);
        }
        // Database saves are handled at the call site
    }
}
