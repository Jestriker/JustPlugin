package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;
import org.justme.justPlugin.managers.HomeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final HomeManager homeManager;
    private final BackManager backManager;

    public HomeCommand(JustPlugin plugin) {
        this.homeManager = plugin.getHomeManager();
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        // List homes if no args or if "homes"
        if (args.length == 0 || label.equalsIgnoreCase("homes")) {
            java.util.Set<String> homes = homeManager.getHomes(player.getUniqueId());
            if (homes.isEmpty()) {
                player.sendMessage("§cYou have no homes set.");
            } else {
                player.sendMessage("§aYour homes: §e" + String.join("§a, §e", homes));
            }
            return true;
        }
        String homeName = args[0];
        org.bukkit.Location loc = homeManager.getHome(player.getUniqueId(), homeName);
        if (loc == null) {
            player.sendMessage("§cHome §e" + homeName + " §cdoes not exist.");
            return true;
        }
        backManager.setTeleportLocation(player.getUniqueId(), player.getLocation());
        player.teleport(loc);
        player.sendMessage("§aTeleported to home §e" + homeName + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player player && args.length == 1) {
            return homeManager.getHomes(player.getUniqueId()).stream()
                    .filter(h -> h.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
