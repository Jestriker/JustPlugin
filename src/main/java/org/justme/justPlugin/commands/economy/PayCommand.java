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

public class PayCommand implements TabExecutor {

    private final JustPlugin plugin;

    public PayCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(CC.error("Usage: /pay <player> <amount>"));
            return true;
        }

        // Resolve target — online or offline
        UUID targetUuid;
        String targetName;
        boolean targetOnline = false;

        Player onlineTarget = Bukkit.getPlayer(args[0]);
        if (onlineTarget != null) {
            if (onlineTarget.equals(player)) {
                player.sendMessage(CC.error("You can't pay yourself!"));
                return true;
            }
            targetUuid = onlineTarget.getUniqueId();
            targetName = onlineTarget.getName();
            targetOnline = true;
        } else {
            // Try offline player
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            if (!offP.hasPlayedBefore() && !offP.isOnline()) {
                player.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> has never joined the server."));
                return true;
            }
            targetUuid = offP.getUniqueId();
            targetName = offP.getName() != null ? offP.getName() : args[0];
            if (targetUuid.equals(player.getUniqueId())) {
                player.sendMessage(CC.error("You can't pay yourself!"));
                return true;
            }
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage(CC.error("Amount must be positive!"));
                return true;
            }

            // Check pay toggle
            if (plugin.getEconomyManager().isPayToggleOff(targetUuid)) {
                player.sendMessage(CC.error("<yellow>" + targetName + "</yellow> has payments disabled."));
                return true;
            }

            // Check sufficient funds
            double currentBalance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            if (currentBalance < amount) {
                player.sendMessage(CC.error("Insufficient funds! Your balance: <green>" + plugin.getEconomyManager().format(currentBalance) + "</green>"));
                return true;
            }

            if (plugin.getEconomyManager().pay(player.getUniqueId(), targetUuid, amount)) {
                String formatted = plugin.getEconomyManager().format(amount);
                String suffix = targetOnline ? "" : " <dark_gray>(offline)";
                player.sendMessage(CC.success("You paid <yellow>" + targetName + "</yellow> <green>" + formatted + "</green>." + suffix));
                if (targetOnline && onlineTarget != null) {
                    onlineTarget.sendMessage(CC.success("<yellow>" + player.getName() + "</yellow> paid you <green>" + formatted + "</green>."));
                }
            } else {
                player.sendMessage(CC.error("Payment failed!"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(CC.error("Invalid amount!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) return List.of("<amount>");
        return List.of();
    }
}
