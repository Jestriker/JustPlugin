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

public class TpacancelCommand implements CommandExecutor, TabCompleter {

    private final TpaManager tpaManager;

    public TpacancelCommand(JustPlugin plugin) {
        this.tpaManager = plugin.getTpaManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        TpaManager.TpaRequest req = tpaManager.getOutgoingRequest(player.getUniqueId());
        if (req == null) {
            player.sendMessage("§cYou have no outgoing TPA request.");
            return true;
        }
        Player target = Bukkit.getPlayer(req.target);
        tpaManager.cancelOutgoingRequest(player.getUniqueId());
        player.sendMessage("§aYour TPA request has been cancelled.");
        if (target != null && target.isOnline()) {
            target.sendMessage("§e" + player.getName() + " §acancelled their TPA request.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
