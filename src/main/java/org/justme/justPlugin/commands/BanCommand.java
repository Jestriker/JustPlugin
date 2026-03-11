package org.justme.justPlugin.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.ban")) {
            sender.sendMessage("§cYou don't have permission to ban players.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ban <player> [reason]");
            return true;
        }
        String targetName = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banned by an operator.";
        Bukkit.getBanList(BanList.Type.NAME).addBan(targetName, reason, null, sender.getName());
        Player target = Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            target.kickPlayer("§cYou have been banned.\n§7Reason: §e" + reason);
        }
        Bukkit.broadcastMessage("§c" + targetName + " §7has been banned. Reason: §e" + reason);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
