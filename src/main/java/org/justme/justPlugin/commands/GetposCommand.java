package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GetposCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        org.bukkit.Location loc = player.getLocation();
        player.sendMessage("§aPosition:");
        player.sendMessage("§7World: §e" + loc.getWorld().getName());
        player.sendMessage("§7X: §e" + String.format("%.2f", loc.getX()));
        player.sendMessage("§7Y: §e" + String.format("%.2f", loc.getY()));
        player.sendMessage("§7Z: §e" + String.format("%.2f", loc.getZ()));
        player.sendMessage("§7Yaw: §e" + String.format("%.1f", loc.getYaw()));
        player.sendMessage("§7Pitch: §e" + String.format("%.1f", loc.getPitch()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
