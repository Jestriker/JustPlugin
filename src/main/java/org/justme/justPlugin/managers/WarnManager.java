package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Manages the warning system.
 * Data persisted in warns.yml.
 * Each warning counter tracks active (non-lifted) warnings to determine punishment level.
 * Lifted warnings remain in history but don't count toward the next punishment.
 */
public class WarnManager {

    private final JustPlugin plugin;
    private final File warnsFile;
    private YamlConfiguration warnsConfig;

    public WarnManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.warnsFile = new File(plugin.getDataFolder(), "warns.yml");
        this.warnsConfig = YamlConfiguration.loadConfiguration(warnsFile);
    }

    public static class WarnEntry {
        public final int index;
        public final String reason;
        public final String warnedBy;
        public final long timestamp;
        public final String punishment;
        public final String punishmentDetail;
        public boolean lifted;
        public String liftedBy;
        public String liftReason;
        public long liftedAt;

        public WarnEntry(int index, String reason, String warnedBy, long timestamp,
                         String punishment, String punishmentDetail,
                         boolean lifted, String liftedBy, String liftReason, long liftedAt) {
            this.index = index;
            this.reason = reason;
            this.warnedBy = warnedBy;
            this.timestamp = timestamp;
            this.punishment = punishment;
            this.punishmentDetail = punishmentDetail;
            this.lifted = lifted;
            this.liftedBy = liftedBy;
            this.liftReason = liftReason;
            this.liftedAt = liftedAt;
        }
    }

    /**
     * Add a warning to a player and auto-execute the configured punishment.
     * Returns the WarnEntry created.
     */
    public WarnEntry addWarn(UUID uuid, String playerName, String reason, String warnedBy) {
        String path = "warns." + uuid.toString();
        ConfigurationSection section = warnsConfig.getConfigurationSection(path);
        int nextIndex = 1;
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    int k = Integer.parseInt(key);
                    if (k >= nextIndex) nextIndex = k + 1;
                } catch (NumberFormatException ignored) {}
            }
        }

        // Count active (non-lifted) warnings to determine which punishment level
        int activeCount = getActiveWarnCount(uuid) + 1; // +1 for the new one

        // Look up punishment config
        String punishmentAction = getPunishmentAction(activeCount);
        String punishmentDetail = getPunishmentDetail(activeCount);

        // Save warn
        String warnPath = path + "." + nextIndex;
        warnsConfig.set(warnPath + ".name", playerName);
        warnsConfig.set(warnPath + ".reason", reason != null ? reason : "No reason specified");
        warnsConfig.set(warnPath + ".warnedBy", warnedBy);
        warnsConfig.set(warnPath + ".timestamp", System.currentTimeMillis());
        warnsConfig.set(warnPath + ".punishment", punishmentAction);
        warnsConfig.set(warnPath + ".punishmentDetail", punishmentDetail);
        warnsConfig.set(warnPath + ".lifted", false);
        saveWarns();

        WarnEntry entry = new WarnEntry(nextIndex, reason != null ? reason : "No reason specified",
                warnedBy, System.currentTimeMillis(), punishmentAction, punishmentDetail,
                false, null, null, 0);

        // Execute punishment
        executePunishment(uuid, playerName, punishmentAction, punishmentDetail, reason, warnedBy, activeCount);

        return entry;
    }

    /**
     * Lift (soft-remove) a warning. Keeps it in history but marks as lifted.
     */
    public boolean liftWarn(UUID uuid, int index, String liftedBy, String liftReason) {
        String path = "warns." + uuid.toString() + "." + index;
        if (!warnsConfig.contains(path)) return false;
        if (warnsConfig.getBoolean(path + ".lifted", false)) return false; // Already lifted

        warnsConfig.set(path + ".lifted", true);
        warnsConfig.set(path + ".liftedBy", liftedBy);
        warnsConfig.set(path + ".liftReason", liftReason != null ? liftReason : "No reason");
        warnsConfig.set(path + ".liftedAt", System.currentTimeMillis());
        saveWarns();
        return true;
    }

    /**
     * Get all warnings for a player (including lifted ones).
     */
    public List<WarnEntry> getWarns(UUID uuid) {
        List<WarnEntry> list = new ArrayList<>();
        String path = "warns." + uuid.toString();
        ConfigurationSection section = warnsConfig.getConfigurationSection(path);
        if (section == null) return list;

        List<Integer> keys = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            try { keys.add(Integer.parseInt(key)); } catch (NumberFormatException ignored) {}
        }
        Collections.sort(keys);

        for (int idx : keys) {
            String wp = path + "." + idx;
            list.add(new WarnEntry(
                    idx,
                    warnsConfig.getString(wp + ".reason", "No reason"),
                    warnsConfig.getString(wp + ".warnedBy", "Unknown"),
                    warnsConfig.getLong(wp + ".timestamp", 0L),
                    warnsConfig.getString(wp + ".punishment", "NoPunishment"),
                    warnsConfig.getString(wp + ".punishmentDetail", ""),
                    warnsConfig.getBoolean(wp + ".lifted", false),
                    warnsConfig.getString(wp + ".liftedBy", null),
                    warnsConfig.getString(wp + ".liftReason", null),
                    warnsConfig.getLong(wp + ".liftedAt", 0L)
            ));
        }
        return list;
    }

    /**
     * Get a specific warn entry.
     */
    public WarnEntry getWarn(UUID uuid, int index) {
        String wp = "warns." + uuid.toString() + "." + index;
        if (!warnsConfig.contains(wp)) return null;
        return new WarnEntry(
                index,
                warnsConfig.getString(wp + ".reason", "No reason"),
                warnsConfig.getString(wp + ".warnedBy", "Unknown"),
                warnsConfig.getLong(wp + ".timestamp", 0L),
                warnsConfig.getString(wp + ".punishment", "NoPunishment"),
                warnsConfig.getString(wp + ".punishmentDetail", ""),
                warnsConfig.getBoolean(wp + ".lifted", false),
                warnsConfig.getString(wp + ".liftedBy", null),
                warnsConfig.getString(wp + ".liftReason", null),
                warnsConfig.getLong(wp + ".liftedAt", 0L)
        );
    }

    /**
     * Count active (non-lifted) warnings.
     */
    public int getActiveWarnCount(UUID uuid) {
        int count = 0;
        String path = "warns." + uuid.toString();
        ConfigurationSection section = warnsConfig.getConfigurationSection(path);
        if (section == null) return 0;
        for (String key : section.getKeys(false)) {
            if (!warnsConfig.getBoolean(path + "." + key + ".lifted", false)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Total warnings (including lifted).
     */
    public int getTotalWarnCount(UUID uuid) {
        String path = "warns." + uuid.toString();
        ConfigurationSection section = warnsConfig.getConfigurationSection(path);
        if (section == null) return 0;
        return section.getKeys(false).size();
    }

    public String getPlayerName(UUID uuid) {
        String path = "warns." + uuid.toString();
        ConfigurationSection section = warnsConfig.getConfigurationSection(path);
        if (section == null) return null;
        for (String key : section.getKeys(false)) {
            String name = warnsConfig.getString(path + "." + key + ".name");
            if (name != null) return name;
        }
        return null;
    }

    // --- Punishment configuration ---

    private String getPunishmentAction(int activeCount) {
        ConfigurationSection punishments = plugin.getConfig().getConfigurationSection("warns.punishments");
        if (punishments == null) return getDefaultPunishmentAction(activeCount);
        String val = punishments.getString(String.valueOf(activeCount));
        if (val == null) return getDefaultPunishmentAction(activeCount);
        // Parse action type from config string like "TempBan 5m" or "ChatMessage" or "Ban"
        String[] parts = val.split(" ", 2);
        return parts[0];
    }

    private String getPunishmentDetail(int activeCount) {
        ConfigurationSection punishments = plugin.getConfig().getConfigurationSection("warns.punishments");
        if (punishments == null) return getDefaultPunishmentDetail(activeCount);
        String val = punishments.getString(String.valueOf(activeCount));
        if (val == null) return getDefaultPunishmentDetail(activeCount);
        String[] parts = val.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    private String getDefaultPunishmentAction(int count) {
        return switch (count) {
            case 1 -> "ChatMessage";
            case 2 -> "Kick";
            case 3 -> "TempBan";
            case 4 -> "TempBan";
            case 5 -> "TempBan";
            case 6 -> "TempBan";
            default -> "Ban";
        };
    }

    private String getDefaultPunishmentDetail(int count) {
        return switch (count) {
            case 1 -> "";
            case 2 -> "";
            case 3 -> "5m";
            case 4 -> "1d";
            case 5 -> "30d";
            case 6 -> "365d";
            default -> "";
        };
    }

    private void executePunishment(UUID uuid, String playerName, String action, String detail,
                                   String reason, String warnedBy, int warnNumber) {
        String defaultReason = reason != null ? reason : "Warning #" + warnNumber;
        String appealSource = plugin.getConfig().getString("warns.appeal-source",
                plugin.getConfig().getString("discord-link", "/discord"));

        Player target = Bukkit.getPlayer(uuid);

        switch (action.toLowerCase()) {
            case "chatmessage" -> {
                if (target != null) {
                    target.sendMessage(CC.error("You have received a warning! <gray>(#" + warnNumber + ")"));
                    target.sendMessage(CC.line("Reason: <white>" + defaultReason));
                    target.sendMessage(CC.line("Warned by: <white>" + warnedBy));
                    target.sendMessage(CC.line("This is warning #<yellow>" + warnNumber + "</yellow>. Further warnings may result in punishment."));
                    target.sendMessage(CC.line("Appeal: <aqua>" + appealSource));
                }
            }
            case "kick" -> {
                if (target != null) {
                    StringBuilder screen = new StringBuilder();
                    screen.append("\n<red><bold>You have been kicked!</bold></red>\n\n");
                    screen.append("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>\n\n");
                    screen.append("<gray>Reason: <white>").append(defaultReason).append("</white></gray>\n");
                    screen.append("<gray>Warning #<yellow>").append(warnNumber).append("</yellow></gray>\n");
                    screen.append("<gray>Warned by: <white>").append(warnedBy).append("</white></gray>\n\n");
                    screen.append("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>\n\n");
                    screen.append("<dark_gray>Appeal at: ").append(appealSource).append("</dark_gray>");
                    target.kick(CC.translate(screen.toString()));
                }
            }
            case "tempban" -> {
                long durationMs = TimeUtil.parseDuration(detail.isEmpty() ? "5m" : detail);
                plugin.getBanManager().tempBan(uuid, playerName, "Warning #" + warnNumber + ": " + defaultReason, warnedBy, durationMs);
            }
            case "ban" -> {
                plugin.getBanManager().ban(uuid, playerName, "Warning #" + warnNumber + ": " + defaultReason, warnedBy);
            }
            case "chatmute" -> {
                plugin.getMuteManager().mute(uuid, playerName, "Warning #" + warnNumber + ": " + defaultReason, warnedBy);
            }
            case "chattempmute" -> {
                long durationMs = TimeUtil.parseDuration(detail.isEmpty() ? "5m" : detail);
                plugin.getMuteManager().tempMute(uuid, playerName, "Warning #" + warnNumber + ": " + defaultReason, warnedBy, durationMs);
            }
            case "nopunishment" -> {
                // No action, just log
            }
        }
    }

    public String formatDate(long timestamp) {
        if (timestamp == 0) return "Unknown";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    public void saveWarns() {
        try {
            warnsConfig.save(warnsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warns: " + e.getMessage());
        }
    }

    public void reloadWarns() {
        warnsConfig = YamlConfiguration.loadConfiguration(warnsFile);
    }
}


