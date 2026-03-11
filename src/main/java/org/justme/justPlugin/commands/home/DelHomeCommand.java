package org.justme.justPlugin.commands.home;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DelHomeCommand implements TabExecutor {

    private final JustPlugin plugin;

    public DelHomeCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /delhome <name>"));
            return true;
        }
        if (plugin.getHomeManager().deleteHome(player.getUniqueId(), args[0])) {
            player.sendMessage(CC.success("Home <yellow>" + args[0] + "</yellow> has been deleted."));
        } else {
            player.sendMessage(CC.error("Home <yellow>" + args[0] + "</yellow> not found!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            Set<String> homes = plugin.getHomeManager().getHomeNames(player.getUniqueId());
            return homes.stream()
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

