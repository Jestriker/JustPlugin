package org.justme.justPlugin.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;

import java.util.ArrayList;
import java.util.List;

public class TpposCommand implements CommandExecutor, TabCompleter {

    private final BackManager backManager;

    public TpposCommand(JustPlugin plugin) {
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 3) {
            player.sendMessage("§cUsage: /tppos <x> <y> <z>");
            return true;
        }
        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            World world = player.getWorld();
            if (args.length >= 4) {
                world = player.getServer().getWorld(args[3]);
                if (world == null) {
                    player.sendMessage("§cWorld §e" + args[3] + " §cnot found.");
                    return true;
                }
            }
            backManager.setTeleportLocation(player.getUniqueId(), player.getLocation());
            player.teleport(new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch()));
            player.sendMessage("§aTeleported to §e" + x + ", " + y + ", " + z + "§a.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid coordinates. Usage: /tppos <x> <y> <z>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
