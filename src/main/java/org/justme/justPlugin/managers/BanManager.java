package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.*;

public class BanManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;

    public BanManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    // --- Custom ban screen builder ---
    private Component buildBanScreen(String title, String reason, String bannedBy, String duration) {
        StringBuilder screen = new StringBuilder();
        screen.append("\n");
        screen.append("<red><bold>").append(title).append("</bold></red>\n");
        screen.append("\n");
        screen.append("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>\n");
        screen.append("\n");
        screen.append("<gray>Reason: <white>").append(reason).append("</white></gray>\n");
        if (duration != null) {
            screen.append("<gray>Duration: <yellow>").append(duration).append("</yellow></gray>\n");
        }
        screen.append("<gray>Banned by: <white>").append(bannedBy).append("</white></gray>\n");
        screen.append("\n");
        screen.append("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>\n");
        screen.append("\n");
        screen.append("<dark_gray>If you believe this is a mistake,</dark_gray>\n");
        screen.append("<dark_gray>contact an administrator.</dark_gray>");
        return CC.translate(screen.toString());
    }

    // --- Name/UUID Bans (ban both UUID and name) ---
    public void ban(UUID uuid, String playerName, String reason, String bannedBy) {
        YamlConfiguration config = dataManager.getBansConfig();
        String path = "bans." + uuid.toString();
        config.set(path + ".name", playerName);
        config.set(path + ".reason", reason);
        config.set(path + ".bannedBy", bannedBy);
        config.set(path + ".time", System.currentTimeMillis());
        config.set(path + ".expires", -1L);
        dataManager.saveBans();

        // Ban both name and UUID via Bukkit
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, (Date) null, bannedBy);

        var player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.kick(buildBanScreen("You have been banned!", reason, bannedBy, null));
        }
    }

    public void tempBan(UUID uuid, String playerName, String reason, String bannedBy, long durationMs) {
        YamlConfiguration config = dataManager.getBansConfig();
        String path = "bans." + uuid.toString();
        long expiresAt = System.currentTimeMillis() + durationMs;
        config.set(path + ".name", playerName);
        config.set(path + ".reason", reason);
        config.set(path + ".bannedBy", bannedBy);
        config.set(path + ".time", System.currentTimeMillis());
        config.set(path + ".expires", expiresAt);
        dataManager.saveBans();

        Date expireDate = new Date(expiresAt);
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, expireDate, bannedBy);

        var player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.kick(buildBanScreen("You have been temporarily banned!", reason, bannedBy, TimeUtil.formatDuration(durationMs)));
        }
    }

    public boolean unban(UUID uuid) {
        YamlConfiguration config = dataManager.getBansConfig();
        if (!config.contains("bans." + uuid.toString())) return false;
        String name = config.getString("bans." + uuid.toString() + ".name", "");
        config.set("bans." + uuid.toString(), null);
        dataManager.saveBans();
        Bukkit.getBanList(BanList.Type.NAME).pardon(name);
        return true;
    }

    /**
     * Unban by name — finds the UUID that has that name in the bans config and unbans it.
     */
    public boolean unbanByName(String name) {
        YamlConfiguration config = dataManager.getBansConfig();
        ConfigurationSection bans = config.getConfigurationSection("bans");
        if (bans == null) return false;
        for (String key : bans.getKeys(false)) {
            String bannedName = config.getString("bans." + key + ".name", "");
            if (bannedName.equalsIgnoreCase(name)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    return unban(uuid);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return false;
    }

    public boolean isBanned(UUID uuid) {
        YamlConfiguration config = dataManager.getBansConfig();
        if (!config.contains("bans." + uuid.toString())) return false;
        long expires = config.getLong("bans." + uuid.toString() + ".expires", -1L);
        if (expires != -1L && System.currentTimeMillis() > expires) {
            unban(uuid);
            return false;
        }
        return true;
    }

    /**
     * Check if a player name is banned (by looking up all ban entries).
     */
    public boolean isBannedByName(String name) {
        YamlConfiguration config = dataManager.getBansConfig();
        ConfigurationSection bans = config.getConfigurationSection("bans");
        if (bans == null) return false;
        for (String key : bans.getKeys(false)) {
            String bannedName = config.getString("bans." + key + ".name", "");
            if (bannedName.equalsIgnoreCase(name)) {
                long expires = config.getLong("bans." + key + ".expires", -1L);
                if (expires != -1L && System.currentTimeMillis() > expires) {
                    try {
                        unban(UUID.fromString(key));
                    } catch (IllegalArgumentException ignored) {}
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Get ban reason for a UUID.
     */
    public String getBanReason(UUID uuid) {
        YamlConfiguration config = dataManager.getBansConfig();
        return config.getString("bans." + uuid.toString() + ".reason", "Banned");
    }

    public String getBanBannedBy(UUID uuid) {
        YamlConfiguration config = dataManager.getBansConfig();
        return config.getString("bans." + uuid.toString() + ".bannedBy", "Unknown");
    }

    public long getBanExpires(UUID uuid) {
        YamlConfiguration config = dataManager.getBansConfig();
        return config.getLong("bans." + uuid.toString() + ".expires", -1L);
    }

    // --- IP Bans (now also bans UUID + name of the associated player) ---
    public void banIp(String ip, String reason, String bannedBy) {
        banIp(ip, reason, bannedBy, null, null);
    }

    public void banIp(String ip, String reason, String bannedBy, UUID associatedUuid, String associatedName) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        config.set("ipbans." + safePath + ".ip", ip);
        config.set("ipbans." + safePath + ".reason", reason);
        config.set("ipbans." + safePath + ".bannedBy", bannedBy);
        config.set("ipbans." + safePath + ".time", System.currentTimeMillis());
        config.set("ipbans." + safePath + ".expires", -1L);

        // Store associated UUIDs and names for cross-reference
        List<String> uuids = config.getStringList("ipbans." + safePath + ".associatedUuids");
        List<String> names = config.getStringList("ipbans." + safePath + ".associatedNames");
        if (associatedUuid != null && !uuids.contains(associatedUuid.toString())) {
            uuids.add(associatedUuid.toString());
        }
        if (associatedName != null && names.stream().noneMatch(n -> n.equalsIgnoreCase(associatedName))) {
            names.add(associatedName);
        }
        config.set("ipbans." + safePath + ".associatedUuids", uuids);
        config.set("ipbans." + safePath + ".associatedNames", names);
        dataManager.saveBans();

        Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, (Date) null, bannedBy);

        // Also ban the UUID and name if provided and IP is not local
        if (associatedUuid != null && !isBanned(associatedUuid)) {
            if (isLocalIp(ip)) {
                // For local IPs, still ban by UUID+name so they can't rejoin
                ban(associatedUuid, associatedName != null ? associatedName : "Unknown", reason, bannedBy);
            } else {
                ban(associatedUuid, associatedName != null ? associatedName : "Unknown", reason, bannedBy);
            }
        }

        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ip)) {
                player.kick(buildBanScreen("Your IP has been banned!", reason, bannedBy, null));
            }
        }
    }

    public void tempBanIp(String ip, String reason, String bannedBy, long durationMs) {
        tempBanIp(ip, reason, bannedBy, durationMs, null, null);
    }

    public void tempBanIp(String ip, String reason, String bannedBy, long durationMs, UUID associatedUuid, String associatedName) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        long expiresAt = System.currentTimeMillis() + durationMs;
        config.set("ipbans." + safePath + ".ip", ip);
        config.set("ipbans." + safePath + ".reason", reason);
        config.set("ipbans." + safePath + ".bannedBy", bannedBy);
        config.set("ipbans." + safePath + ".time", System.currentTimeMillis());
        config.set("ipbans." + safePath + ".expires", expiresAt);

        List<String> uuids = config.getStringList("ipbans." + safePath + ".associatedUuids");
        List<String> names = config.getStringList("ipbans." + safePath + ".associatedNames");
        if (associatedUuid != null && !uuids.contains(associatedUuid.toString())) {
            uuids.add(associatedUuid.toString());
        }
        if (associatedName != null && names.stream().noneMatch(n -> n.equalsIgnoreCase(associatedName))) {
            names.add(associatedName);
        }
        config.set("ipbans." + safePath + ".associatedUuids", uuids);
        config.set("ipbans." + safePath + ".associatedNames", names);
        dataManager.saveBans();

        Date expireDate = new Date(expiresAt);
        Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, expireDate, bannedBy);

        if (associatedUuid != null && !isBanned(associatedUuid)) {
            tempBan(associatedUuid, associatedName != null ? associatedName : "Unknown", reason, bannedBy, durationMs);
        }

        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ip)) {
                player.kick(buildBanScreen("Your IP has been temporarily banned!", reason, bannedBy, TimeUtil.formatDuration(durationMs)));
            }
        }
    }

    public boolean unbanIp(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        if (!config.contains("ipbans." + safePath)) return false;

        // Also unban associated UUIDs
        List<String> uuids = config.getStringList("ipbans." + safePath + ".associatedUuids");
        for (String uuidStr : uuids) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                if (isBanned(uuid)) {
                    unban(uuid);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        // Also pardon associated names from Bukkit's NAME ban list as a safety net
        List<String> names = config.getStringList("ipbans." + safePath + ".associatedNames");
        for (String name : names) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(name);
        }

        // Also pardon all associated UUIDs from Bukkit's ban list directly
        for (String uuidStr : uuids) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer.getName() != null) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(offlinePlayer.getName());
                }
            } catch (IllegalArgumentException ignored) {}
        }

        config.set("ipbans." + safePath, null);
        dataManager.saveBans();
        Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        return true;
    }

    /**
     * Unban IP by player name or UUID — finds the IP ban entry that has this player associated.
     */
    public boolean unbanIpByNameOrUuid(String input) {
        YamlConfiguration config = dataManager.getBansConfig();
        ConfigurationSection ipbans = config.getConfigurationSection("ipbans");
        if (ipbans == null) return false;

        // Try parsing as UUID
        UUID targetUuid = null;
        try {
            targetUuid = UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {}

        for (String key : ipbans.getKeys(false)) {
            List<String> names = config.getStringList("ipbans." + key + ".associatedNames");
            List<String> uuids = config.getStringList("ipbans." + key + ".associatedUuids");
            String ip = config.getString("ipbans." + key + ".ip", "");

            boolean match = false;
            if (targetUuid != null && uuids.contains(targetUuid.toString())) {
                match = true;
            }
            if (!match) {
                for (String name : names) {
                    if (name.equalsIgnoreCase(input)) {
                        match = true;
                        break;
                    }
                }
            }

            if (match) {
                return unbanIp(ip);
            }
        }
        return false;
    }

    public boolean isIpBanned(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        if (!config.contains("ipbans." + safePath)) return false;
        long expires = config.getLong("ipbans." + safePath + ".expires", -1L);
        if (expires != -1L && System.currentTimeMillis() > expires) {
            unbanIp(ip);
            return false;
        }
        return true;
    }

    /**
     * Find the IP ban entry associated with a player name or UUID.
     * Returns a map with keys: ip, reason, bannedBy, associatedNames, associatedUuids, time, expires.
     * Returns null if no IP ban found for this player.
     */
    public Map<String, Object> findIpBanByPlayer(String input) {
        YamlConfiguration config = dataManager.getBansConfig();
        ConfigurationSection ipbans = config.getConfigurationSection("ipbans");
        if (ipbans == null) return null;

        UUID targetUuid = null;
        try {
            targetUuid = UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {}

        for (String key : ipbans.getKeys(false)) {
            List<String> names = config.getStringList("ipbans." + key + ".associatedNames");
            List<String> uuids = config.getStringList("ipbans." + key + ".associatedUuids");
            String ip = config.getString("ipbans." + key + ".ip", "");

            boolean match = false;
            if (targetUuid != null && uuids.contains(targetUuid.toString())) {
                match = true;
            }
            if (!match) {
                for (String name : names) {
                    if (name.equalsIgnoreCase(input)) {
                        match = true;
                        break;
                    }
                }
            }

            if (match) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("ip", ip);
                info.put("reason", config.getString("ipbans." + key + ".reason", "IP Banned"));
                info.put("bannedBy", config.getString("ipbans." + key + ".bannedBy", "Unknown"));
                info.put("associatedNames", names);
                info.put("associatedUuids", uuids);
                info.put("time", config.getLong("ipbans." + key + ".time", 0L));
                info.put("expires", config.getLong("ipbans." + key + ".expires", -1L));
                return info;
            }
        }
        return null;
    }

    /**
     * Get IP ban details by IP address.
     * Returns a map with keys: ip, reason, bannedBy, associatedNames, associatedUuids, time, expires.
     * Returns null if no IP ban found for this IP.
     */
    public Map<String, Object> getIpBanDetails(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        if (!config.contains("ipbans." + safePath)) return null;

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("ip", ip);
        info.put("reason", config.getString("ipbans." + safePath + ".reason", "IP Banned"));
        info.put("bannedBy", config.getString("ipbans." + safePath + ".bannedBy", "Unknown"));
        info.put("associatedNames", config.getStringList("ipbans." + safePath + ".associatedNames"));
        info.put("associatedUuids", config.getStringList("ipbans." + safePath + ".associatedUuids"));
        info.put("time", config.getLong("ipbans." + safePath + ".time", 0L));
        info.put("expires", config.getLong("ipbans." + safePath + ".expires", -1L));
        return info;
    }

    public String getIpBanReason(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        return config.getString("ipbans." + safePath + ".reason", "IP Banned");
    }

    public String getIpBanBannedBy(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_").replace(":", "_");
        return config.getString("ipbans." + safePath + ".bannedBy", "Unknown");
    }

    /**
     * Look up a player's last recorded IP from their player data file.
     * Works even when the player is offline.
     */
    public String getLastIp(UUID uuid) {
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        return data.getString("last-ip", null);
    }

    /**
     * Check if an IP address is a localhost / local / loopback address.
     */
    public static boolean isLocalIp(String ip) {
        if (ip == null) return false;
        return ip.equals("127.0.0.1")
                || ip.equals("0.0.0.0")
                || ip.equals("localhost")
                || ip.equals("::1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.")
                || ip.startsWith("172.19.") || ip.startsWith("172.20.") || ip.startsWith("172.21.")
                || ip.startsWith("172.22.") || ip.startsWith("172.23.") || ip.startsWith("172.24.")
                || ip.startsWith("172.25.") || ip.startsWith("172.26.") || ip.startsWith("172.27.")
                || ip.startsWith("172.28.") || ip.startsWith("172.29.") || ip.startsWith("172.30.")
                || ip.startsWith("172.31.");
    }

    /**
     * Checks if a joining player should be blocked:
     * 1. UUID is banned
     * 2. Name is banned
     * 3. IP is banned (for non-local IPs: new accounts from banned IPs get normal banned too)
     *
     * Returns the kick Component if banned, null if allowed.
     */
    public Component checkJoinBan(UUID uuid, String name, String ip) {
        // Check UUID ban
        if (isBanned(uuid)) {
            String reason = getBanReason(uuid);
            String bannedBy = getBanBannedBy(uuid);
            long expires = getBanExpires(uuid);
            String duration = null;
            if (expires != -1L) {
                long remaining = expires - System.currentTimeMillis();
                if (remaining > 0) {
                    duration = TimeUtil.formatDuration(remaining);
                }
            }
            return buildBanScreen(expires == -1L ? "You have been banned!" : "You have been temporarily banned!", reason, bannedBy, duration);
        }

        // Check name ban (different UUID using same name — edge case)
        if (isBannedByName(name)) {
            return buildBanScreen("You have been banned!", "Your account name is banned.", "System", null);
        }

        // Check IP ban
        if (ip != null && isIpBanned(ip)) {
            String reason = getIpBanReason(ip);
            String bannedBy = getIpBanBannedBy(ip);

            // If connecting from a non-local IP that is banned, also ban their UUID+name
            if (!isLocalIp(ip) && !isBanned(uuid)) {
                ban(uuid, name, "Associated with banned IP: " + ip, "System");
            }

            return buildBanScreen("Your IP has been banned!", reason, bannedBy, null);
        }

        return null;
    }
}
