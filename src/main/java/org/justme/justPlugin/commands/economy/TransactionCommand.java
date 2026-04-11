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
import java.util.UUID;
import java.util.stream.Collectors;

public class TransactionCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TransactionCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        if (!plugin.getTransactionManager().isEnabled()) {
            player.sendMessage(plugin.getMessageManager().error("economy.transaction.disabled"));
            return true;
        }

        if (args.length == 0) {
            // View own transactions
            plugin.getTransactionHistoryGui().open(player, player.getUniqueId(), 1);
            return true;
        }

        // View another player's transactions
        if (!player.hasPermission("justplugin.transactions.others")) {
            player.sendMessage(plugin.getMessageManager().error("economy.transaction.no-permission-others"));
            return true;
        }

        // Try online player first
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            plugin.getTransactionHistoryGui().open(player, target.getUniqueId(), 1);
            return true;
        }

        // Try offline player
        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
        if (!offP.hasPlayedBefore() && !offP.isOnline()) {
            player.sendMessage(plugin.getMessageManager().error("general.never-joined", "{player}", args[0]));
            return true;
        }

        plugin.getTransactionHistoryGui().open(player, offP.getUniqueId(), 1);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.transactions.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
