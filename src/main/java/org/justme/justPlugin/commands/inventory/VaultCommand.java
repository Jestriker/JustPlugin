package org.justme.justPlugin.commands.inventory;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

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
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        if (!plugin.getConfig().getBoolean("vaults.enabled", false)) {
            player.sendMessage(CC.error("Player vaults are disabled on this server."));
            return true;
        }

        // /pv <player> <number> - staff viewing another player's vault
        if (args.length >= 2) {
            if (!player.hasPermission("justplugin.vault.others")) {
                player.sendMessage(CC.error("You don't have permission to view other players' vaults."));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> is not online."));
                return true;
            }

            int vaultNumber;
            try {
                vaultNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(CC.error("Invalid vault number."));
                return true;
            }

            int targetMax = plugin.getVaultManager().getMaxVaults(target);
            if (vaultNumber < 1 || vaultNumber > targetMax) {
                player.sendMessage(CC.error("That player only has access to vaults <yellow>1-" + targetMax + "</yellow>."));
                return true;
            }

            plugin.getVaultManager().openVault(player, target, vaultNumber);
            player.sendMessage(CC.success("Opening <yellow>" + target.getName() + "</yellow>'s Vault <yellow>#" + vaultNumber + "</yellow>."));
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
                        player.sendMessage(CC.success("Opening <yellow>" + target.getName() + "</yellow>'s Vault <yellow>#1</yellow>."));
                        return true;
                    }
                }
                player.sendMessage(CC.error("Invalid vault number."));
                return true;
            }
        }

        int maxVaults = plugin.getVaultManager().getMaxVaults(player);
        if (vaultNumber < 1 || vaultNumber > maxVaults) {
            player.sendMessage(CC.error("You only have access to vaults <yellow>1-" + maxVaults + "</yellow>."));
            return true;
        }

        plugin.getVaultManager().openVault(player, vaultNumber);
        player.sendMessage(CC.success("Opening Vault <yellow>#" + vaultNumber + "</yellow>."));
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
