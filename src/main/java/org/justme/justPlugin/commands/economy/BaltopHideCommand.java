package org.justme.justPlugin.commands.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaltopHideCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BaltopHideCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // Hiding another player
        if (args.length >= 1) {
            if (sender instanceof Player p && !p.hasPermission("justplugin.baltophide.others")) {
                sender.sendMessage(CC.error("You don't have permission to hide other players from the balance leaderboard."));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            UUID targetUuid;
            String targetName;
            if (target != null) {
                targetUuid = target.getUniqueId();
                targetName = target.getName();
            } else {
                @SuppressWarnings("deprecation")
                OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
                if (!offP.hasPlayedBefore()) {
                    sender.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> has never joined the server."));
                    return true;
                }
                targetUuid = offP.getUniqueId();
                targetName = offP.getName() != null ? offP.getName() : args[0];
            }

            plugin.getEconomyManager().toggleBaltopHidden(targetUuid);
            boolean hidden = plugin.getEconomyManager().isBaltopHidden(targetUuid);

            String senderName = sender instanceof Player ? sender.getName() : "Console";
            sender.sendMessage(CC.success("<yellow>" + targetName + "</yellow> is now " + (hidden ? "<red>hidden" : "<green>visible") + " on the balance leaderboard."));

            if (target != null && !target.equals(sender)) {
                target.sendMessage(CC.info("You have been " + (hidden ? "<red>hidden from" : "<green>made visible on") + " the balance leaderboard by <yellow>" + senderName + "</yellow>."));
            }

            // Log
            plugin.getLogManager().log("economy", "<yellow>" + senderName + "</yellow> " + (hidden ? "hid" : "unhid") + " <yellow>" + targetName + "</yellow> from the balance leaderboard");

            // Notify players with notification permission
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.baltophide.notify") && !p.equals(sender)) {
                    p.sendMessage(CC.info("<yellow>" + senderName + "</yellow> " + (hidden ? "hid" : "unhid") + " <yellow>" + targetName + "</yellow> from the balance leaderboard."));
                }
            }
            return true;
        }

        // Self-hide
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Console must specify a player: /baltophide <player>"));
            return true;
        }

        plugin.getEconomyManager().toggleBaltopHidden(player.getUniqueId());
        boolean hidden = plugin.getEconomyManager().isBaltopHidden(player.getUniqueId());

        if (hidden) {
            player.sendMessage(CC.success("You are now <yellow>hidden</yellow> from the balance leaderboard."));
        } else {
            player.sendMessage(CC.success("You are now <yellow>visible</yellow> on the balance leaderboard."));
        }

        // Log
        plugin.getLogManager().log("economy", "<yellow>" + player.getName() + "</yellow> " + (hidden ? "hid" : "unhid") + " themselves from the balance leaderboard");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.baltophide.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
