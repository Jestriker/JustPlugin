package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TpaManager;

import java.util.ArrayList;
import java.util.List;

public class TprejectCommand implements CommandExecutor, TabCompleter {

    private final TpaManager tpaManager;

    public TprejectCommand(JustPlugin plugin) {
        this.tpaManager = plugin.getTpaManager();
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
        tpaManager.removeRequest(player.getUniqueId());
        player.sendMessage("§aYou rejected the TPA request.");
        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§e" + player.getName() + " §crejected your TPA request.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
