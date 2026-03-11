package org.justme.justPlugin.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;

import java.util.ArrayList;
import java.util.List;

public class BackCommand implements CommandExecutor, TabCompleter {

    private final BackManager backManager;

    public BackCommand(JustPlugin plugin) {
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        Location lastLoc = backManager.getLastLocation(player.getUniqueId());
        if (lastLoc == null) {
            player.sendMessage("§cNo previous location found.");
            return true;
        }
        Location current = player.getLocation();
        backManager.setTeleportLocation(player.getUniqueId(), current);
        player.teleport(lastLoc);
        player.sendMessage("§aTeleported to your previous location.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
