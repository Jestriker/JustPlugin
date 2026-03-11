package org.justme.justPlugin.commands.home;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeCommand implements TabExecutor {

    private final JustPlugin plugin;

    public HomeCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        if (homes.isEmpty()) {
            player.sendMessage(CC.error("You have no homes set! Use <yellow>/sethome <name></yellow> to set one."));
            return true;
        }

        String name = args.length >= 1 ? args[0] : homes.keySet().iterator().next();
        Location loc = plugin.getHomeManager().getHome(player.getUniqueId(), name);
        if (loc == null) {
            player.sendMessage(CC.error("Home <yellow>" + name + "</yellow> not found!"));
            player.sendMessage(CC.info("Your homes: <yellow>" + String.join(", ", homes.keySet())));
            return true;
        }
        plugin.getTeleportManager().teleport(player, loc);
        player.sendMessage(CC.success("Teleporting to home <yellow>" + name + "</yellow>."));
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

