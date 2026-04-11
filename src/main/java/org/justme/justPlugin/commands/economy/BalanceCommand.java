package org.justme.justPlugin.commands.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class BalanceCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BalanceCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1) {
            // Checking another player's balance
            if (sender instanceof Player p && !p.hasPermission("justplugin.balance.others")) {
                p.sendMessage(plugin.getMessageManager().error("economy.balance.no-permission-others"));
                return true;
            }
            // Try online player first
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                double bal = plugin.getEconomyManager().getBalance(target.getUniqueId());
                sender.sendMessage(plugin.getMessageManager().info("economy.balance.other", "{player}", target.getName(), "{balance}", plugin.getEconomyManager().format(bal)));
                return true;
            }
            // Try offline player
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            if (!offP.hasPlayedBefore() && !offP.isOnline()) {
                sender.sendMessage(plugin.getMessageManager().error("general.never-joined", "{player}", args[0]));
                return true;
            }
            String name = offP.getName() != null ? offP.getName() : args[0];
            double bal = plugin.getEconomyManager().getBalance(offP.getUniqueId());
            sender.sendMessage(plugin.getMessageManager().info("economy.balance.other-offline", "{player}", name, "{balance}", plugin.getEconomyManager().format(bal)));
            return true;
        }

        // Self balance
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("economy.balance.console-usage"));
            return true;
        }
        double bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
        player.sendMessage(plugin.getMessageManager().info("economy.balance.self", "{balance}", plugin.getEconomyManager().format(bal)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.balance.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

