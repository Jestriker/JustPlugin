package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.HomeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DelhomeCommand implements CommandExecutor, TabCompleter {

    private final HomeManager homeManager;

    public DelhomeCommand(JustPlugin plugin) {
        this.homeManager = plugin.getHomeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /delhome <name>");
            return true;
        }
        if (homeManager.deleteHome(player.getUniqueId(), args[0])) {
            player.sendMessage("§aHome §e" + args[0] + " §adeleted.");
        } else {
            player.sendMessage("§cHome §e" + args[0] + " §cdoes not exist.");
        }
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
