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
import java.util.UUID;
import java.util.stream.Collectors;

public class AddCashCommand implements TabExecutor {

    private final JustPlugin plugin;

    public AddCashCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /addcash <amount> or /addcash <player> <amount>"));
            return true;
        }

        // /addcash <amount> - self
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(CC.error("Console must specify a player: /addcash <player> <amount>"));
                return true;
            }
            if (!player.hasPermission("justplugin.addcash")) {
                player.sendMessage(CC.error("You don't have permission to add cash to yourself."));
                return true;
            }
            try {
                double amount = Double.parseDouble(args[0]);
                if (amount <= 0) {
                    player.sendMessage(CC.error("Amount must be positive!"));
                    return true;
                }
                plugin.getEconomyManager().addBalance(player.getUniqueId(), amount);
                player.sendMessage(CC.success("Added <green>" + plugin.getEconomyManager().format(amount) + "</green> to your balance. New balance: <green>" + plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player.getUniqueId())) + "</green>"));
                plugin.getLogManager().log("economy", "<yellow>" + player.getName() + "</yellow> added <green>" + plugin.getEconomyManager().format(amount) + "</green> to their own balance");
            } catch (NumberFormatException e) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("general.invalid-number")));
            }
            return true;
        }

        // /addcash <player> <amount> - others
        if (sender instanceof Player p && !p.hasPermission("justplugin.addcash.others")) {
            sender.sendMessage(CC.error("You don't have permission to add cash to other players."));
            return true;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage(CC.error("Amount must be positive!"));
                return true;
            }

            // Try online player first
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                sender.sendMessage(CC.success("Added <green>" + plugin.getEconomyManager().format(amount) + "</green> to <yellow>" + target.getName() + "</yellow>'s balance."));
                target.sendMessage(CC.success("<green>" + plugin.getEconomyManager().format(amount) + "</green> has been added to your balance."));
                String senderName = sender instanceof Player ? sender.getName() : "Console";
                plugin.getLogManager().log("economy", "<yellow>" + senderName + "</yellow> added <green>" + plugin.getEconomyManager().format(amount) + "</green> to <yellow>" + target.getName() + "</yellow>'s balance");
                return true;
            }

            // Try offline player
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            UUID uuid = offP.getUniqueId();
            String name = offP.getName() != null ? offP.getName() : args[0];

            // Check if player has ever joined (has player data)
            if (!offP.hasPlayedBefore() && !offP.isOnline()) {
                sender.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> has never joined the server."));
                return true;
            }

            plugin.getEconomyManager().addBalance(uuid, amount);
            sender.sendMessage(CC.success("Added <green>" + plugin.getEconomyManager().format(amount) + "</green> to <yellow>" + name + "</yellow>'s balance (offline)."));
            String senderNameOff = sender instanceof Player ? sender.getName() : "Console";
            plugin.getLogManager().log("economy", "<yellow>" + senderNameOff + "</yellow> added <green>" + plugin.getEconomyManager().format(amount) + "</green> to <yellow>" + name + "</yellow>'s balance (offline)");
        } catch (NumberFormatException e) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.invalid-number")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Could be amount (self) or player name (others)
            List<String> suggestions = Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            suggestions.add("<amount>");
            return suggestions;
        }
        if (args.length == 2) return List.of("<amount>");
        return List.of();
    }
}

