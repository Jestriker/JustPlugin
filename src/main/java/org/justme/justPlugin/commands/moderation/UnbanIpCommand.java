package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UnbanIpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public UnbanIpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /unbanip <ip | player | uuid>"));
            return true;
        }

        String input = args[0];
        boolean isIp = input.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || input.contains(":");

        // Get ban info before unbanning
        Map<String, Object> info;
        if (isIp) {
            info = plugin.getBanManager().getIpBanDetails(input);
        } else {
            info = plugin.getBanManager().findIpBanByPlayer(input);
        }

        if (info == null) {
            if (isIp) {
                sender.sendMessage(CC.error("IP <yellow>" + input + "</yellow> is not banned!"));
            } else {
                sender.sendMessage(CC.error("No IP ban found for <yellow>" + input + "</yellow>!"));
            }
            return true;
        }

        String ip = (String) info.get("ip");
        String reason = (String) info.get("reason");
        String bannedBy = (String) info.get("bannedBy");
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) info.get("associatedNames");
        @SuppressWarnings("unchecked")
        List<String> uuids = (List<String>) info.get("associatedUuids");

        // Perform the unban
        boolean success = plugin.getBanManager().unbanIp(ip);
        if (!success) {
            sender.sendMessage(CC.error("Failed to unban IP <yellow>" + ip + "</yellow>!"));
            return true;
        }

        // Display detailed info about what was unbanned
        String unbannedBy = sender instanceof org.bukkit.entity.Player ? sender.getName() : "Console";
        plugin.getLogManager().log("moderation", "<yellow>" + unbannedBy + "</yellow> unbanned IP <yellow>" + ip + "</yellow>");
        sender.sendMessage(CC.translate(""));
        sender.sendMessage(CC.success("<bold>IP Ban Removed</bold>"));
        sender.sendMessage(CC.line("IP: <yellow>" + ip));
        sender.sendMessage(CC.line("Reason: <yellow>" + reason));
        sender.sendMessage(CC.line("Banned by: <yellow>" + bannedBy));

        if (!names.isEmpty()) {
            sender.sendMessage(CC.line("Associated names: <yellow>" + String.join("<dark_gray>, <yellow>", names)));
        }
        if (!uuids.isEmpty()) {
            StringBuilder uuidDisplay = new StringBuilder();
            for (int i = 0; i < uuids.size(); i++) {
                if (i > 0) uuidDisplay.append("<dark_gray>, <yellow>");
                String uuidStr = uuids.get(i);
                try {
                    OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                    String name = off.getName();
                    if (name != null) {
                        uuidDisplay.append(name).append(" <dark_gray>(<gray>").append(uuidStr).append("<dark_gray>)");
                    } else {
                        uuidDisplay.append(uuidStr);
                    }
                } catch (IllegalArgumentException e) {
                    uuidDisplay.append(uuidStr);
                }
            }
            sender.sendMessage(CC.line("Associated UUIDs: <yellow>" + uuidDisplay));
        }

        int totalUnbanned = 1 + uuids.size(); // IP + associated accounts
        sender.sendMessage(CC.line("Unbanned: <green>" + totalUnbanned + "</green> record" + (totalUnbanned == 1 ? "" : "s") + " <gray>(IP + associated accounts)"));
        sender.sendMessage(CC.translate(""));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
