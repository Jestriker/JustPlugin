package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;
import org.justme.justPlugin.managers.TpaManager;

import java.util.ArrayList;
import java.util.List;

public class TpacceptCommand implements CommandExecutor, TabCompleter {

    private final TpaManager tpaManager;
    private final BackManager backManager;

    public TpacceptCommand(JustPlugin plugin) {
        this.tpaManager = plugin.getTpaManager();
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        TpaManager.TpaRequest request = tpaManager.getPendingRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage("§cYou have no pending TPA request.");
            return true;
        }
        Player requester = Bukkit.getPlayer(request.sender);
        if (requester == null || !requester.isOnline()) {
            tpaManager.removeRequest(player.getUniqueId());
            player.sendMessage("§cThe requesting player is no longer online.");
            return true;
        }
        tpaManager.removeRequest(player.getUniqueId());
        if (request.type == TpaManager.RequestType.TPA) {
            backManager.setTeleportLocation(requester.getUniqueId(), requester.getLocation());
            requester.teleport(player.getLocation());
            requester.sendMessage("§aTeleported to §e" + player.getName() + "§a.");
            player.sendMessage("§e" + requester.getName() + " §ahas been teleported to you.");
        } else {
            backManager.setTeleportLocation(player.getUniqueId(), player.getLocation());
            player.teleport(requester.getLocation());
            player.sendMessage("§aTeleported to §e" + requester.getName() + "§a.");
            requester.sendMessage("§e" + player.getName() + " §ahas teleported to you.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
