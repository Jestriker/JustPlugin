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

public class UnbanCommand implements TabExecutor {

    private final JustPlugin plugin;

    public UnbanCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /unban <player | uuid>"));
            return true;
        }

        String input = args[0];
        boolean success;
        String displayName;

        // Try as UUID first
        try {
            UUID uuid = UUID.fromString(input);
            OfflinePlayer offP = Bukkit.getOfflinePlayer(uuid);
            displayName = offP.getName() != null ? offP.getName() : input;
            success = plugin.getBanManager().unban(uuid);
        } catch (IllegalArgumentException e) {
            // Try as name - first via Bukkit, then by scanning bans
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(input);
            UUID uuid = offP.getUniqueId();
            displayName = input;

            // Try UUID-based unban first
            success = plugin.getBanManager().unban(uuid);

            // If that didn't work, try name-based lookup in the bans config
            if (!success) {
                success = plugin.getBanManager().unbanByName(input);
            }
        }

        if (success) {
            sender.sendMessage(CC.success("<yellow>" + displayName + "</yellow> has been unbanned."));
            String unbannedBy = sender instanceof org.bukkit.entity.Player ? sender.getName() : "Console";
            plugin.getLogManager().log("moderation", "<yellow>" + unbannedBy + "</yellow> unbanned <yellow>" + displayName + "</yellow>");

            // Check if this player also has an associated IP ban
            Map<String, Object> ipInfo = plugin.getBanManager().findIpBanByPlayer(input);
            if (ipInfo != null) {
                String ip = (String) ipInfo.get("ip");
                @SuppressWarnings("unchecked")
                List<String> names = (List<String>) ipInfo.get("associatedNames");
                String nameList = names.isEmpty() ? "none" : String.join(", ", names);
                sender.sendMessage(CC.warning("This player also has an <red>IP ban</red> on <yellow>" + ip + "</yellow>."));
                sender.sendMessage(CC.warning("Associated accounts: <yellow>" + nameList + "</yellow>"));
                sender.sendMessage(CC.warning("Use <yellow>/unbanip " + ip + "</yellow> to fully remove the IP ban."));
            }
        } else {
            sender.sendMessage(CC.error("<yellow>" + displayName + "</yellow> is not banned!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
