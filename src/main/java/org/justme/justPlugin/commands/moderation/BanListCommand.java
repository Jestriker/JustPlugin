package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * /banlist [page] - List banned players with pagination.
 * /baniplist [page] - List IP-banned players with pagination.
 * Detects which mode via the label used.
 */
public class BanListCommand implements TabExecutor {

    private final JustPlugin plugin;
    private static final int PAGE_SIZE = 8;

    public BanListCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        boolean isIpMode = label.equalsIgnoreCase("baniplist") || label.equalsIgnoreCase("ipbanlist");

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(CC.error("Invalid page number."));
                return true;
            }
        }

        if (isIpMode) {
            showIpBans(sender, page);
        } else {
            showBans(sender, page);
        }
        return true;
    }

    private void showBans(CommandSender sender, int page) {
        YamlConfiguration config = plugin.getDataManager().getBansConfig();
        ConfigurationSection bans = config.getConfigurationSection("bans");

        if (bans == null || bans.getKeys(false).isEmpty()) {
            sender.sendMessage(CC.info("There are no banned players."));
            return;
        }

        List<String> keys = new ArrayList<>(bans.getKeys(false));
        int totalPages = (int) Math.ceil((double) keys.size() / PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, keys.size());

        sender.sendMessage(CC.prefixed("<yellow>Ban List <gray>(Page " + page + " / " + totalPages + ", " + keys.size() + " total)"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i = start; i < end; i++) {
            String uuidStr = keys.get(i);
            String name = config.getString("bans." + uuidStr + ".name", "Unknown");
            String reason = config.getString("bans." + uuidStr + ".reason", "No reason");
            String bannedBy = config.getString("bans." + uuidStr + ".bannedBy", "Unknown");
            long time = config.getLong("bans." + uuidStr + ".time", 0L);
            long expires = config.getLong("bans." + uuidStr + ".expires", -1L);

            String dateStr = time > 0 ? sdf.format(new Date(time)) : "Unknown";
            String expiresStr;
            if (expires == -1L) {
                expiresStr = "<red>Permanent";
            } else {
                long remaining = expires - System.currentTimeMillis();
                if (remaining > 0) {
                    expiresStr = "<yellow>" + TimeUtil.formatDuration(remaining) + " remaining";
                } else {
                    expiresStr = "<green>Expired";
                }
            }

            sender.sendMessage(CC.line("<white>" + name + " <dark_gray>(" + uuidStr.substring(0, 8) + "...)"));
            sender.sendMessage(CC.translate("   <dark_gray>  Reason: <gray>" + reason + " <dark_gray>| By: <white>" + bannedBy + " <dark_gray>| " + dateStr));
            sender.sendMessage(CC.translate("   <dark_gray>  Duration: " + expiresStr));
        }

        if (totalPages > 1) {
            String nextCmd = "/banlist " + (page + 1);
            String prevCmd = "/banlist " + (page - 1);
            StringBuilder nav = new StringBuilder(" <dark_gray>");
            if (page > 1) nav.append(CC.clickCmd("<aqua>[← Prev]</aqua>", prevCmd, true)).append(" ");
            if (page < totalPages) nav.append(CC.clickCmd("<aqua>[Next →]</aqua>", nextCmd, true));
            sender.sendMessage(CC.translate(nav.toString()));
        }
    }

    private void showIpBans(CommandSender sender, int page) {
        YamlConfiguration config = plugin.getDataManager().getBansConfig();
        ConfigurationSection ipbans = config.getConfigurationSection("ipbans");

        if (ipbans == null || ipbans.getKeys(false).isEmpty()) {
            sender.sendMessage(CC.info("There are no IP-banned players."));
            return;
        }

        List<String> keys = new ArrayList<>(ipbans.getKeys(false));
        int totalPages = (int) Math.ceil((double) keys.size() / PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, keys.size());

        sender.sendMessage(CC.prefixed("<yellow>IP Ban List <gray>(Page " + page + " / " + totalPages + ", " + keys.size() + " total)"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i = start; i < end; i++) {
            String key = keys.get(i);
            String ip = config.getString("ipbans." + key + ".ip", key);
            String reason = config.getString("ipbans." + key + ".reason", "No reason");
            String bannedBy = config.getString("ipbans." + key + ".bannedBy", "Unknown");
            long time = config.getLong("ipbans." + key + ".time", 0L);
            long expires = config.getLong("ipbans." + key + ".expires", -1L);
            List<String> names = config.getStringList("ipbans." + key + ".associatedNames");
            List<String> uuids = config.getStringList("ipbans." + key + ".associatedUuids");

            String dateStr = time > 0 ? sdf.format(new Date(time)) : "Unknown";
            String expiresStr;
            if (expires == -1L) {
                expiresStr = "<red>Permanent";
            } else {
                long remaining = expires - System.currentTimeMillis();
                if (remaining > 0) {
                    expiresStr = "<yellow>" + TimeUtil.formatDuration(remaining) + " remaining";
                } else {
                    expiresStr = "<green>Expired";
                }
            }

            sender.sendMessage(CC.line("<white>IP: <yellow>" + ip));
            sender.sendMessage(CC.translate("   <dark_gray>  Players: <white>" + (names.isEmpty() ? "Unknown" : String.join(", ", names))));
            if (!uuids.isEmpty()) {
                sender.sendMessage(CC.translate("   <dark_gray>  UUIDs: <gray>" + String.join(", ", uuids.stream().map(u -> u.substring(0, 8) + "...").toList())));
            }
            sender.sendMessage(CC.translate("   <dark_gray>  Reason: <gray>" + reason + " <dark_gray>| By: <white>" + bannedBy + " <dark_gray>| " + dateStr));
            sender.sendMessage(CC.translate("   <dark_gray>  Duration: " + expiresStr));
        }

        if (totalPages > 1) {
            String nextCmd = "/baniplist " + (page + 1);
            String prevCmd = "/baniplist " + (page - 1);
            StringBuilder nav = new StringBuilder(" <dark_gray>");
            if (page > 1) nav.append(CC.clickCmd("<aqua>[← Prev]</aqua>", prevCmd, true)).append(" ");
            if (page < totalPages) nav.append(CC.clickCmd("<aqua>[Next →]</aqua>", nextCmd, true));
            sender.sendMessage(CC.translate(nav.toString()));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

