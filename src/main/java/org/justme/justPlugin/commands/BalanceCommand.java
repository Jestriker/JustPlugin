package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.EconomyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economyManager;

    public BalanceCommand(JustPlugin plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            double balance = economyManager.getBalance(player.getUniqueId());
            player.sendMessage("§aYour balance: §e$" + String.format("%.2f", balance));
        } else {
            if (!player.hasPermission("justplugin.balance.others")) {
                player.sendMessage("§cYou don't have permission to view others' balances.");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
                return true;
            }
            double balance = economyManager.getBalance(target.getUniqueId());
            player.sendMessage("§e" + target.getName() + "§a's balance: §e$" + String.format("%.2f", balance));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.balance.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
