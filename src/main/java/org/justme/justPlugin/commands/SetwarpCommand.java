package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.WarpManager;

import java.util.ArrayList;
import java.util.List;

public class SetwarpCommand implements CommandExecutor, TabCompleter {

    private final WarpManager warpManager;

    public SetwarpCommand(JustPlugin plugin) {
        this.warpManager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("justplugin.setwarp")) {
            player.sendMessage("§cYou don't have permission to set warps.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /setwarp <name>");
            return true;
        }
        warpManager.setWarp(args[0], player.getLocation());
        player.sendMessage("§aWarp §e" + args[0] + " §aset at your current location.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
