package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
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

    // --- Name/UUID Bans ---
    public void ban(UUID uuid, String playerName, String reason, String bannedBy) {
        YamlConfiguration config = dataManager.getBansConfig();
        String path = "bans." + uuid.toString();
        config.set(path + ".name", playerName);
        config.set(path + ".reason", reason);
        config.set(path + ".bannedBy", bannedBy);
        config.set(path + ".time", System.currentTimeMillis());
        config.set(path + ".expires", -1L);
        dataManager.saveBans();

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

    // --- IP Bans ---
    public void banIp(String ip, String reason, String bannedBy) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_");
        config.set("ipbans." + safePath + ".ip", ip);
        config.set("ipbans." + safePath + ".reason", reason);
        config.set("ipbans." + safePath + ".bannedBy", bannedBy);
        config.set("ipbans." + safePath + ".time", System.currentTimeMillis());
        config.set("ipbans." + safePath + ".expires", -1L);
        dataManager.saveBans();

        Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, (Date) null, bannedBy);

        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ip)) {
                player.kick(buildBanScreen("Your IP has been banned!", reason, bannedBy, null));
            }
        }
    }

    public void tempBanIp(String ip, String reason, String bannedBy, long durationMs) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_");
        long expiresAt = System.currentTimeMillis() + durationMs;
        config.set("ipbans." + safePath + ".ip", ip);
        config.set("ipbans." + safePath + ".reason", reason);
        config.set("ipbans." + safePath + ".bannedBy", bannedBy);
        config.set("ipbans." + safePath + ".time", System.currentTimeMillis());
        config.set("ipbans." + safePath + ".expires", expiresAt);
        dataManager.saveBans();

        Date expireDate = new Date(expiresAt);
        Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, expireDate, bannedBy);

        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ip)) {
                player.kick(buildBanScreen("Your IP has been temporarily banned!", reason, bannedBy, TimeUtil.formatDuration(durationMs)));
            }
        }
    }

    public boolean unbanIp(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_");
        if (!config.contains("ipbans." + safePath)) return false;
        config.set("ipbans." + safePath, null);
        dataManager.saveBans();
        Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        return true;
    }

    public boolean isIpBanned(String ip) {
        YamlConfiguration config = dataManager.getBansConfig();
        String safePath = ip.replace(".", "_");
        if (!config.contains("ipbans." + safePath)) return false;
        long expires = config.getLong("ipbans." + safePath + ".expires", -1L);
        if (expires != -1L && System.currentTimeMillis() > expires) {
            unbanIp(ip);
            return false;
        }
        return true;
    }

    /**
     * Look up a player's last recorded IP from their player data file.
     * Works even when the player is offline.
     */
    public String getLastIp(UUID uuid) {
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        return data.getString("last-ip", null);
    }
}
