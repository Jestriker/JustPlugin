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
import java.util.stream.Collectors;

public class DelwarpCommand implements CommandExecutor, TabCompleter {

    private final WarpManager warpManager;

    public DelwarpCommand(JustPlugin plugin) {
        this.warpManager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("justplugin.delwarp")) {
            player.sendMessage("§cYou don't have permission to delete warps.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /delwarp <name>");
            return true;
        }
        if (warpManager.deleteWarp(args[0])) {
            player.sendMessage("§aWarp §e" + args[0] + " §adeleted.");
        } else {
            player.sendMessage("§cWarp §e" + args[0] + " §cdoes not exist.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return warpManager.getWarpNames().stream()
                    .filter(w -> w.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
