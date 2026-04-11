package org.justme.justPlugin.commands.warp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class DelWarpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public DelWarpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().error("warp.delwarp.usage"));
            return true;
        }
        if (plugin.getWarpManager().deleteWarp(args[0])) {
            player.sendMessage(plugin.getMessageManager().success("warp.delwarp.success", "{warp}", args[0]));
            plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> deleted warp <yellow>" + args[0] + "</yellow>");
        } else {
            player.sendMessage(plugin.getMessageManager().error("warp.delwarp.not-found", "{warp}", args[0]));
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
        return List.of();
    }
}

