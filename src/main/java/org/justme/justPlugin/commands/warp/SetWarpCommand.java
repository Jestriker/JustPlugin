package org.justme.justPlugin.commands.warp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import org.justme.justPlugin.util.InputValidator;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class SetWarpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SetWarpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().error("warp.setwarp.usage"));
            return true;
        }
        String name = args[0];
        if (!InputValidator.isValidName(name)) {
            player.sendMessage(plugin.getMessageManager().error("general.invalid-name"));
            return true;
        }
        plugin.getWarpManager().setWarp(name, player.getLocation());
        player.sendMessage(plugin.getMessageManager().success("warp.setwarp.success", "{warp}", name));
        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> set warp <yellow>" + name + "</yellow>");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("<name>");
        return List.of();
    }
}

