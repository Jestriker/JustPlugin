package org.justme.justPlugin.commands.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

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
                p.sendMessage(CC.error("You don't have permission to check other players' balances."));
                return true;
            }
            // Try online player first
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                double bal = plugin.getEconomyManager().getBalance(target.getUniqueId());
                sender.sendMessage(CC.info("<yellow>" + target.getName() + "</yellow>'s balance: <green>" + plugin.getEconomyManager().format(bal)));
                return true;
            }
            // Try offline player
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            if (!offP.hasPlayedBefore() && !offP.isOnline()) {
                sender.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> has never joined the server."));
                return true;
            }
            String name = offP.getName() != null ? offP.getName() : args[0];
            double bal = plugin.getEconomyManager().getBalance(offP.getUniqueId());
            sender.sendMessage(CC.info("<yellow>" + name + "</yellow>'s balance: <green>" + plugin.getEconomyManager().format(bal) + " <dark_gray>(offline)"));
            return true;
        }

        // Self balance
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Console must specify a player: /balance <player>"));
            return true;
        }
        double bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
        player.sendMessage(CC.info("Your balance: <green>" + plugin.getEconomyManager().format(bal)));
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

