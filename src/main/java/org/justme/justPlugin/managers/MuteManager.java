package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player mutes (permanent and temporary).
 * Data is persisted in bans.yml under a "mutes" section.
 */
public class MuteManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;

    // In-memory cache: uuid -> MuteEntry
    private final Map<UUID, MuteEntry> mutes = new ConcurrentHashMap<>();

    public MuteManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        loadMutes();
    }

    public static class MuteEntry {
        public final UUID uuid;
        public final String playerName;
        public final String reason;
        public final String mutedBy;
        public final long time;
        public final long expires; // -1 = permanent

        public MuteEntry(UUID uuid, String playerName, String reason, String mutedBy, long time, long expires) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.reason = reason;
            this.mutedBy = mutedBy;
            this.time = time;
            this.expires = expires;
        }

        public boolean isExpired() {
            return expires != -1L && System.currentTimeMillis() > expires;
        }

        public long getRemainingMs() {
            if (expires == -1L) return -1L;
            return Math.max(0, expires - System.currentTimeMillis());
        }
    }

    private void loadMutes() {
        YamlConfiguration config = dataManager.getBansConfig();
        ConfigurationSection section = config.getConfigurationSection("mutes");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String name = config.getString("mutes." + key + ".name", "Unknown");
                String reason = config.getString("mutes." + key + ".reason", "Muted");
                String mutedBy = config.getString("mutes." + key + ".mutedBy", "Unknown");
                long time = config.getLong("mutes." + key + ".time", System.currentTimeMillis());
                long expires = config.getLong("mutes." + key + ".expires", -1L);
                MuteEntry entry = new MuteEntry(uuid, name, reason, mutedBy, time, expires);
                if (!entry.isExpired()) {
                    mutes.put(uuid, entry);
                } else {
                    // Clean up expired mute
                    config.set("mutes." + key, null);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        dataManager.saveBans();
    }

    public void mute(UUID uuid, String playerName, String reason, String mutedBy) {
        MuteEntry entry = new MuteEntry(uuid, playerName, reason, mutedBy, System.currentTimeMillis(), -1L);
        mutes.put(uuid, entry);
        saveMute(uuid, entry);

        Player target = Bukkit.getPlayer(uuid);
        if (target != null) {
            target.sendMessage(CC.error("You have been muted!"));
            target.sendMessage(CC.line("Reason: <white>" + reason));
            target.sendMessage(CC.line("Duration: <red>Permanent"));
            target.sendMessage(CC.line("Muted by: <white>" + mutedBy));
        }
    }

    public void tempMute(UUID uuid, String playerName, String reason, String mutedBy, long durationMs) {
        long expiresAt = System.currentTimeMillis() + durationMs;
        MuteEntry entry = new MuteEntry(uuid, playerName, reason, mutedBy, System.currentTimeMillis(), expiresAt);
        mutes.put(uuid, entry);
        saveMute(uuid, entry);

        Player target = Bukkit.getPlayer(uuid);
        if (target != null) {
            target.sendMessage(CC.error("You have been temporarily muted!"));
            target.sendMessage(CC.line("Reason: <white>" + reason));
            target.sendMessage(CC.line("Duration: <yellow>" + TimeUtil.formatDuration(durationMs)));
            target.sendMessage(CC.line("Muted by: <white>" + mutedBy));
        }
    }

    public boolean unmute(UUID uuid) {
        if (!mutes.containsKey(uuid)) {
            // Check disk
            YamlConfiguration config = dataManager.getBansConfig();
            if (!config.contains("mutes." + uuid.toString())) return false;
        }
        mutes.remove(uuid);
        YamlConfiguration config = dataManager.getBansConfig();
        config.set("mutes." + uuid.toString(), null);
        dataManager.saveBans();

        Player target = Bukkit.getPlayer(uuid);
        if (target != null) {
            target.sendMessage(CC.success("You have been unmuted."));
        }
        return true;
    }

    public boolean isMuted(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) return false;
        if (entry.isExpired()) {
            mutes.remove(uuid);
            YamlConfiguration config = dataManager.getBansConfig();
            config.set("mutes." + uuid.toString(), null);
            dataManager.saveBans();
            return false;
        }
        return true;
    }

    public MuteEntry getMuteEntry(UUID uuid) {
        if (!isMuted(uuid)) return null;
        return mutes.get(uuid);
    }

    public String getMuteReason(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        return entry != null ? entry.reason : null;
    }

    public long getMuteExpiry(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        return entry != null ? entry.expires : -1L;
    }

    /**
     * Unmute by name — finds the UUID that has that name.
     */
    public boolean unmuteByName(String name) {
        for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
            if (entry.getValue().playerName.equalsIgnoreCase(name)) {
                return unmute(entry.getKey());
            }
        }
        // Check disk
        YamlConfiguration config = dataManager.getBansConfig();
        ConfigurationSection section = config.getConfigurationSection("mutes");
        if (section == null) return false;
        for (String key : section.getKeys(false)) {
            String mutedName = config.getString("mutes." + key + ".name", "");
            if (mutedName.equalsIgnoreCase(name)) {
                try {
                    return unmute(UUID.fromString(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return false;
    }

    /**
     * Check if a player name is muted.
     */
    public boolean isMutedByName(String name) {
        for (MuteEntry entry : mutes.values()) {
            if (entry.playerName.equalsIgnoreCase(name) && !entry.isExpired()) {
                return true;
            }
        }
        return false;
    }

    private void saveMute(UUID uuid, MuteEntry entry) {
        YamlConfiguration config = dataManager.getBansConfig();
        String path = "mutes." + uuid.toString();
        config.set(path + ".name", entry.playerName);
        config.set(path + ".reason", entry.reason);
        config.set(path + ".mutedBy", entry.mutedBy);
        config.set(path + ".time", entry.time);
        config.set(path + ".expires", entry.expires);
        dataManager.saveBans();
    }
}

