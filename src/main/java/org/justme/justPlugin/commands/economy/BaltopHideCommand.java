package org.justme.justPlugin.commands.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

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
                sender.sendMessage(plugin.getMessageManager().error("economy.baltophide.no-permission-others"));
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
                    sender.sendMessage(plugin.getMessageManager().error("general.never-joined", "{player}", args[0]));
                    return true;
                }
                targetUuid = offP.getUniqueId();
                targetName = offP.getName() != null ? offP.getName() : args[0];
            }

            plugin.getEconomyManager().toggleBaltopHidden(targetUuid);
            boolean hidden = plugin.getEconomyManager().isBaltopHidden(targetUuid);

            String senderName = sender instanceof Player ? sender.getName() : "Console";
            sender.sendMessage(plugin.getMessageManager().success(hidden ? "economy.baltophide.hidden-other" : "economy.baltophide.visible-other", "{player}", targetName));

            if (target != null && !target.equals(sender)) {
                target.sendMessage(plugin.getMessageManager().info(hidden ? "economy.baltophide.notify-target-hidden" : "economy.baltophide.notify-target-visible", "{player}", senderName));
            }

            // Log
            plugin.getLogManager().log("economy", "<yellow>" + senderName + "</yellow> " + (hidden ? "hid" : "unhid") + " <yellow>" + targetName + "</yellow> from the balance leaderboard");

            // Notify players with notification permission
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.baltophide.notify") && !p.equals(sender)) {
                    p.sendMessage(plugin.getMessageManager().info(hidden ? "economy.baltophide.notify-staff-hidden" : "economy.baltophide.notify-staff-visible", "{player}", senderName, "{target}", targetName));
                }
            }
            return true;
        }

        // Self-hide
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("economy.baltophide.console-usage"));
            return true;
        }

        plugin.getEconomyManager().toggleBaltopHidden(player.getUniqueId());
        boolean hidden = plugin.getEconomyManager().isBaltopHidden(player.getUniqueId());

        player.sendMessage(plugin.getMessageManager().success(hidden ? "economy.baltophide.hidden-self" : "economy.baltophide.visible-self"));

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
