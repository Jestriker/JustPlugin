package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int count = onlinePlayers.size();
        int max = Bukkit.getMaxPlayers();
        String names = onlinePlayers.stream().map(Player::getName).collect(Collectors.joining("§7, §e"));
        sender.sendMessage("§aOnline players §7(§e" + count + "§7/§e" + max + "§7):");
        if (count > 0) {
            sender.sendMessage("§e" + names);
        } else {
            sender.sendMessage("§7No players online.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
