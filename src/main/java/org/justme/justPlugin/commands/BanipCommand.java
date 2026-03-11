package org.justme.justPlugin.commands;

import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BanipCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.banip")) {
            sender.sendMessage("§cYou don't have permission to IP ban.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /banip <ip> [reason]");
            return true;
        }
        String ip = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "IP banned by an operator.";
        org.bukkit.Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, null, sender.getName());
        sender.sendMessage("§aIP §e" + ip + " §ahas been banned. Reason: §e" + reason);
        // Kick any players with that IP
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(ip)) {
                p.kickPlayer("§cYour IP has been banned.\n§7Reason: §e" + reason);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
