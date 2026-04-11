package org.justme.justPlugin.commands.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;

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
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        // Rate limiting - cooldown between pay commands
        CooldownManager cm = plugin.getCooldownManager();
        int payCooldown = plugin.getConfig().getInt("economy.pay-cooldown", 5);
        if (payCooldown > 0 && cm.isOnDelay(player.getUniqueId(), "pay", payCooldown)) {
            player.sendMessage(plugin.getMessageManager().error("general.cooldown-wait",
                "{time}", CooldownManager.formatTime(cm.getRemainingDelaySeconds(player.getUniqueId(), "pay", payCooldown))));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().error("economy.pay.usage"));
            return true;
        }

        // Resolve target - online or offline
        UUID targetUuid;
        String targetName;
        boolean targetOnline = false;

        Player onlineTarget = Bukkit.getPlayer(args[0]);
        if (onlineTarget != null) {
            if (onlineTarget.equals(player)) {
                player.sendMessage(plugin.getMessageManager().error("economy.pay.cannot-self"));
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
                player.sendMessage(plugin.getMessageManager().error("general.never-joined", "{player}", args[0]));
                return true;
            }
            targetUuid = offP.getUniqueId();
            targetName = offP.getName() != null ? offP.getName() : args[0];
            if (targetUuid.equals(player.getUniqueId())) {
                player.sendMessage(plugin.getMessageManager().error("economy.pay.cannot-self"));
                return true;
            }
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage(plugin.getMessageManager().error("economy.pay.invalid-amount"));
                return true;
            }

            // Check pay toggle
            if (plugin.getEconomyManager().isPayToggleOff(targetUuid)) {
                player.sendMessage(plugin.getMessageManager().error("economy.pay.target-disabled", "{player}", targetName));
                return true;
            }

            // Check sufficient funds
            double currentBalance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            if (currentBalance < amount) {
                player.sendMessage(plugin.getMessageManager().error("economy.pay.insufficient-funds", "{balance}", plugin.getEconomyManager().format(currentBalance)));
                return true;
            }

            if (plugin.getEconomyManager().pay(player.getUniqueId(), targetUuid, amount)) {
                String formatted = plugin.getEconomyManager().format(amount);
                if (targetOnline) {
                    player.sendMessage(plugin.getMessageManager().success("economy.pay.success", "{player}", targetName, "{amount}", formatted));
                    onlineTarget.sendMessage(plugin.getMessageManager().success("economy.pay.received", "{player}", player.getName(), "{amount}", formatted));
                } else {
                    player.sendMessage(plugin.getMessageManager().success("economy.pay.success-offline", "{player}", targetName, "{amount}", formatted));
                }
                plugin.getLogManager().log("economy", "<yellow>" + player.getName() + "</yellow> paid <yellow>" + targetName + "</yellow> <green>" + formatted + "</green>");
                // Set cooldown after successful pay
                cm.setDelayStart(player.getUniqueId(), "pay");
            } else {
                player.sendMessage(plugin.getMessageManager().error("economy.pay.payment-failed"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessageManager().error("general.invalid-number"));
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
