package org.justme.justPlugin.commands;

import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class UnbanipCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.unbanip")) {
            sender.sendMessage("§cYou don't have permission to unban IPs.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unbanip <ip>");
            return true;
        }
        String ip = args[0];
        if (!org.bukkit.Bukkit.getBanList(BanList.Type.IP).isBanned(ip)) {
            sender.sendMessage("§eIP " + ip + " §cis not banned.");
            return true;
        }
        org.bukkit.Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        sender.sendMessage("§aUnbanned IP §e" + ip + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return org.bukkit.Bukkit.getBanList(BanList.Type.IP).getBanEntries().stream()
                    .map(entry -> entry.getTarget())
                    .filter(n -> n != null && n.startsWith(args[0]))
                    .collect(java.util.stream.Collectors.toList());
        }
        return new ArrayList<>();
    }
}
