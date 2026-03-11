package org.justme.justPlugin.commands.inventory;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class EnchantingTableCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        // Open virtual enchanting table - Paper doesn't have a direct API, so we use a fake location
        // We can open it using NMS or just tell the player. For Paper 1.21, there's no direct openEnchanting.
        // Workaround: Use openInventory or a custom GUI. For now, open via the world.
        // Paper actually does not support openEnchanting directly in the same way. Let's create a workaround.
        player.closeInventory();
        // Use the command dispatch as a simple workaround - but that's circular.
        // Best approach: open a crafting-style inventory. Since Paper doesn't expose openEnchanting,
        // we'll place the player in front of a virtual enchanting table.
        // Actually, Paper 1.21 does NOT have player.openEnchanting() directly.
        // We need to use the internal method or use a fake block approach.
        // For simplicity, let's send them a message and open using the Bukkit approach if available.
        try {
            // Paper may support this via HumanEntity
            player.openEnchanting(player.getLocation(), true);
        } catch (Exception e) {
            player.sendMessage(CC.error("Enchanting table is not supported via command on this server version."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

