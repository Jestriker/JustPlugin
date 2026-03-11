package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.EconomyManager;

import java.util.ArrayList;
import java.util.List;

public class PaytoggleCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economyManager;

    public PaytoggleCommand(JustPlugin plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        boolean disabled = economyManager.togglePay(player.getUniqueId());
        if (disabled) {
            player.sendMessage("§cPayments are now §edisabled§c. Others cannot pay you.");
        } else {
            player.sendMessage("§aPayments are now §eenabled§a. Others can pay you.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
