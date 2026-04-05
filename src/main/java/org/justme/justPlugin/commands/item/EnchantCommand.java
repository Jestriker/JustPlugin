package org.justme.justPlugin.commands.item;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("NullableProblems")
public class EnchantCommand implements TabExecutor {

    private final JustPlugin plugin;

    public EnchantCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /enchant <enchantment> [level]"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(CC.error("You must be holding an item."));
            return true;
        }

        // Look up enchantment
        Enchantment enchantment = lookupEnchantment(args[0]);
        if (enchantment == null) {
            player.sendMessage(CC.error("Unknown enchantment: <yellow>" + args[0] + "</yellow>"));
            return true;
        }

        // Parse level
        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(CC.error("Level must be a number."));
                return true;
            }
        }

        boolean canBypass = canBypass(player);
        String enchantName = enchantment.getKey().getKey().toLowerCase();
        String itemName = item.getType().name().toLowerCase().replace("_", " ");

        // Level 0 = remove enchantment
        if (level == 0) {
            item.removeEnchantment(enchantment);
            player.sendMessage(CC.success("Removed <yellow>" + enchantName + "</yellow> from <yellow>" + itemName + "</yellow>."));
            plugin.getLogManager().log("item", "<yellow>" + player.getName() + "</yellow> removed <yellow>" + enchantName + "</yellow> from <yellow>" + itemName + "</yellow>");
            return true;
        }

        // Check restrictions (unless bypass)
        if (!canBypass) {
            if (!enchantment.canEnchantItem(item)) {
                player.sendMessage(CC.error("This enchantment cannot be applied to this item."));
                return true;
            }
            if (level > enchantment.getMaxLevel()) {
                player.sendMessage(CC.error("Maximum level for <yellow>" + enchantName + "</yellow> is <yellow>" + enchantment.getMaxLevel() + "</yellow>."));
                return true;
            }
        }

        // Apply enchantment (unsafe to allow bypass levels)
        item.addUnsafeEnchantment(enchantment, level);
        player.sendMessage(CC.success("Applied <yellow>" + enchantName + " " + level + "</yellow> to <yellow>" + itemName + "</yellow>."));
        plugin.getLogManager().log("item", "<yellow>" + player.getName() + "</yellow> enchanted <yellow>" + itemName + "</yellow> with <yellow>" + enchantName + " " + level + "</yellow>");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        boolean canBypass = canBypass(player);
        ItemStack item = player.getInventory().getItemInMainHand();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return StreamSupport.stream(Registry.ENCHANTMENT.spliterator(), false)
                    .filter(e -> canBypass || item.getType() == Material.AIR || e.canEnchantItem(item))
                    .map(e -> e.getKey().getKey().toLowerCase())
                    .filter(n -> n.startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            Enchantment enchantment = lookupEnchantment(args[0]);
            if (enchantment == null) return List.of();
            int maxLevel = canBypass ? 10 : enchantment.getMaxLevel();
            List<String> levels = new ArrayList<>();
            for (int i = 1; i <= maxLevel; i++) {
                levels.add(String.valueOf(i));
            }
            return levels;
        }

        return List.of();
    }

    private Enchantment lookupEnchantment(String name) {
        String key = name.toLowerCase();
        // Strip "minecraft:" prefix if present
        if (key.startsWith("minecraft:")) {
            key = key.substring("minecraft:".length());
        }
        String finalKey = key;
        return StreamSupport.stream(Registry.ENCHANTMENT.spliterator(), false)
                .filter(e -> e.getKey().getKey().equalsIgnoreCase(finalKey))
                .findFirst()
                .orElse(null);
    }

    private boolean canBypass(Player player) {
        boolean configBypass = plugin.getConfig().getBoolean("enchant.bypass-restrictions", false);
        return configBypass || player.hasPermission("justplugin.enchant.bypass");
    }
}
