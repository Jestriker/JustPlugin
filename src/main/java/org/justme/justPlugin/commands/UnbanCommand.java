package org.justme.justPlugin.commands;

import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.unban")) {
            sender.sendMessage("§cYou don't have permission to unban players.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unban <player>");
            return true;
        }
        String targetName = args[0];
        if (!org.bukkit.Bukkit.getBanList(BanList.Type.NAME).isBanned(targetName)) {
            sender.sendMessage("§e" + targetName + " §cis not banned.");
            return true;
        }
        org.bukkit.Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        sender.sendMessage("§aUnbanned §e" + targetName + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return org.bukkit.Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream()
                    .map(entry -> entry.getTarget())
                    .filter(n -> n != null && n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return new ArrayList<>();
    }
}
