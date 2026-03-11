package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;
import org.justme.justPlugin.managers.WarpManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final WarpManager warpManager;
    private final BackManager backManager;

    public WarpCommand(JustPlugin plugin) {
        this.warpManager = plugin.getWarpManager();
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        Set<String> warps = warpManager.getWarpNames();
        if (args.length == 0) {
            if (warps.isEmpty()) {
                player.sendMessage("§cNo warps available.");
            } else {
                player.sendMessage("§aAvailable warps: §e" + String.join("§a, §e", warps));
            }
            return true;
        }
        org.bukkit.Location loc = warpManager.getWarp(args[0]);
        if (loc == null) {
            player.sendMessage("§cWarp §e" + args[0] + " §cdoes not exist.");
            return true;
        }
        backManager.setTeleportLocation(player.getUniqueId(), player.getLocation());
        player.teleport(loc);
        player.sendMessage("§aTeleported to warp §e" + args[0] + "§a.");
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
