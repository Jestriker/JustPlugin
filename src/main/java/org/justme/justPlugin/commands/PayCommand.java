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

public class PayCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economyManager;

    public PayCommand(JustPlugin plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot pay yourself.");
            return true;
        }
        if (economyManager.isPayDisabled(target.getUniqueId())) {
            player.sendMessage("§e" + target.getName() + " §chas payments disabled.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount: §e" + args[1]);
            return true;
        }
        if (amount <= 0) {
            player.sendMessage("§cAmount must be positive.");
            return true;
        }
        if (!economyManager.transfer(player.getUniqueId(), target.getUniqueId(), amount)) {
            player.sendMessage("§cInsufficient funds. Your balance: §e$" + String.format("%.2f", economyManager.getBalance(player.getUniqueId())));
            return true;
        }
        player.sendMessage("§aYou paid §e" + target.getName() + " §a$" + String.format("%.2f", amount) + "§a.");
        target.sendMessage("§e" + player.getName() + " §apaid you §e$" + String.format("%.2f", amount) + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
