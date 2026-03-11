package org.justme.justPlugin.managers;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemporaryBanManager {

    public static class TempBan {
        public final String target;   // name or IP
        public final String reason;
        public final long expireTime; // millis
        public final String bannedBy;
        public final boolean isIp;

        public TempBan(String target, String reason, long expireTime, String bannedBy, boolean isIp) {
            this.target = target;
            this.reason = reason;
            this.expireTime = expireTime;
            this.bannedBy = bannedBy;
            this.isIp = isIp;
        }
    }

    private final JustPlugin plugin;
    private final File dataFile;
    private YamlConfiguration config;
    private final Map<String, TempBan> tempBans = new HashMap<>();

    public TemporaryBanManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "tempbans.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create tempbans.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        if (config.getConfigurationSection("bans") != null) {
            for (String key : config.getConfigurationSection("bans").getKeys(false)) {
                String target = config.getString("bans." + key + ".target");
                String reason = config.getString("bans." + key + ".reason");
                long expireTime = config.getLong("bans." + key + ".expireTime");
                String bannedBy = config.getString("bans." + key + ".bannedBy");
                boolean isIp = config.getBoolean("bans." + key + ".isIp");
                if (target != null && reason != null && bannedBy != null) {
                    tempBans.put(key.toLowerCase(), new TempBan(target, reason, expireTime, bannedBy, isIp));
                }
            }
        }
    }

    public void save() {
        for (Map.Entry<String, TempBan> entry : tempBans.entrySet()) {
            String key = entry.getKey();
            TempBan ban = entry.getValue();
            config.set("bans." + key + ".target", ban.target);
            config.set("bans." + key + ".reason", ban.reason);
            config.set("bans." + key + ".expireTime", ban.expireTime);
            config.set("bans." + key + ".bannedBy", ban.bannedBy);
            config.set("bans." + key + ".isIp", ban.isIp);
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save tempbans.yml: " + e.getMessage());
        }
    }

    public static long parseDuration(String durationStr) {
        long millis = 0;
        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(durationStr.toLowerCase());
        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            switch (matcher.group(2)) {
                case "d": millis += amount * 86400000L; break;
                case "h": millis += amount * 3600000L; break;
                case "m": millis += amount * 60000L; break;
                case "s": millis += amount * 1000L; break;
            }
        }
        return millis;
    }

    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    public void addTempBan(String target, String reason, long durationMillis, String bannedBy, boolean isIp) {
        long expireTime = System.currentTimeMillis() + durationMillis;
        TempBan ban = new TempBan(target, reason, expireTime, bannedBy, isIp);
        tempBans.put(target.toLowerCase(), ban);
        // Also add to vanilla ban list
        if (isIp) {
            Bukkit.getBanList(BanList.Type.IP).addBan(target, reason + " (Temp: " + formatDuration(durationMillis) + ")", new Date(expireTime), bannedBy);
        } else {
            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason + " (Temp: " + formatDuration(durationMillis) + ")", new Date(expireTime), bannedBy);
        }
        save();
    }

    public void removeTempBan(String target) {
        TempBan ban = tempBans.remove(target.toLowerCase());
        if (ban != null) {
            config.set("bans." + target.toLowerCase(), null);
            if (ban.isIp) {
                Bukkit.getBanList(BanList.Type.IP).pardon(target);
            } else {
                Bukkit.getBanList(BanList.Type.NAME).pardon(target);
            }
            try { config.save(dataFile); } catch (IOException e) { plugin.getLogger().severe("Error saving: " + e.getMessage()); }
        }
    }

    public boolean isTempBanned(String target) {
        TempBan ban = tempBans.get(target.toLowerCase());
        if (ban == null) return false;
        if (System.currentTimeMillis() > ban.expireTime) {
            removeTempBan(target);
            return false;
        }
        return true;
    }

    public TempBan getTempBan(String target) {
        TempBan ban = tempBans.get(target.toLowerCase());
        if (ban == null) return null;
        if (System.currentTimeMillis() > ban.expireTime) {
            removeTempBan(target);
            return null;
        }
        return ban;
    }

    public void checkExpired() {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, TempBan> entry : tempBans.entrySet()) {
            if (System.currentTimeMillis() > entry.getValue().expireTime) {
                expired.add(entry.getKey());
            }
        }
        for (String target : expired) {
            removeTempBan(target);
        }
    }
}
