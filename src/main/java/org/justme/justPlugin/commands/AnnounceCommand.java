package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AnnounceCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.announce")) {
            sender.sendMessage("§cYou don't have permission to broadcast messages.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /announce <message>");
            return true;
        }
        String message = String.join(" ", args);
        String formatted = "§8[§6Announce§8] §f" + message;
        for (Player p : sender instanceof Player pl ? pl.getServer().getOnlinePlayers()
                : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.sendMessage(formatted);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
