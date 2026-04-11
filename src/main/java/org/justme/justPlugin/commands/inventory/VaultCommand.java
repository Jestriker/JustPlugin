package org.justme.justPlugin.commands.inventory;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class VaultCommand implements TabExecutor {

    private final JustPlugin plugin;

    public VaultCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        if (!plugin.getConfig().getBoolean("vaults.enabled", false)) {
            player.sendMessage(plugin.getMessageManager().error("inventory.vault.disabled"));
            return true;
        }

        // /pv <player> <number> - staff viewing another player's vault
        if (args.length >= 2) {
            if (!player.hasPermission("justplugin.vault.others")) {
                player.sendMessage(plugin.getMessageManager().error("inventory.vault.no-permission-others"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getMessageManager().error("inventory.vault.player-not-online", "{player}", args[0]));
                return true;
            }

            int vaultNumber;
            try {
                vaultNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessageManager().error("inventory.vault.invalid-number"));
                return true;
            }

            int targetMax = plugin.getVaultManager().getMaxVaults(target);
            if (vaultNumber < 1 || vaultNumber > targetMax) {
                player.sendMessage(plugin.getMessageManager().error("inventory.vault.target-max", "{max}", String.valueOf(targetMax)));
                return true;
            }

            plugin.getVaultManager().openVault(player, target, vaultNumber);
            player.sendMessage(plugin.getMessageManager().success("inventory.vault.opening-other", "{player}", target.getName(), "{number}", String.valueOf(vaultNumber)));
            return true;
        }

        // /pv [number] - open own vault
        int vaultNumber = 1;
        if (args.length == 1) {
            try {
                vaultNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // Maybe they typed a player name without a number - check staff perm
                if (player.hasPermission("justplugin.vault.others")) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        plugin.getVaultManager().openVault(player, target, 1);
                        player.sendMessage(plugin.getMessageManager().success("inventory.vault.opening-other", "{player}", target.getName(), "{number}", "1"));
                        return true;
                    }
                }
                player.sendMessage(plugin.getMessageManager().error("inventory.vault.invalid-number"));
                return true;
            }
        }

        int maxVaults = plugin.getVaultManager().getMaxVaults(player);
        if (vaultNumber < 1 || vaultNumber > maxVaults) {
            player.sendMessage(plugin.getMessageManager().error("inventory.vault.self-max", "{max}", String.valueOf(maxVaults)));
            return true;
        }

        plugin.getVaultManager().openVault(player, vaultNumber);
        player.sendMessage(plugin.getMessageManager().success("inventory.vault.opening", "{number}", String.valueOf(vaultNumber)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (!plugin.getConfig().getBoolean("vaults.enabled", false)) return List.of();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            // Add vault numbers
            int max = plugin.getVaultManager().getMaxVaults(player);
            for (int i = 1; i <= max; i++) {
                completions.add(String.valueOf(i));
            }

            // Add player names if staff
            if (player.hasPermission("justplugin.vault.others")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    completions.add(online.getName());
                }
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && player.hasPermission("justplugin.vault.others")) {
            // Second arg for staff: vault number for the target
            Player target = Bukkit.getPlayer(args[0]);
            int max = target != null ? plugin.getVaultManager().getMaxVaults(target) : 3;
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i <= max; i++) {
                numbers.add(String.valueOf(i));
            }
            return numbers.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
