package org.justme.justPlugin.commands.warp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

public class RenameWarpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public RenameWarpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(CC.error("Usage: /renamewarp <old> <new>"));
            return true;
        }
        if (plugin.getWarpManager().renameWarp(args[0], args[1])) {
            player.sendMessage(CC.success("Warp <yellow>" + args[0] + "</yellow> renamed to <yellow>" + args[1] + "</yellow>."));
            plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> renamed warp <yellow>" + args[0] + "</yellow> to <yellow>" + args[1] + "</yellow>");
        } else {
            player.sendMessage(CC.error("Warp <yellow>" + args[0] + "</yellow> not found!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getWarpManager().getWarpNames().stream()
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) return List.of("<new-name>");
        return List.of();
    }
}

