package org.justme.justPlugin.commands.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class ShareItemCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(CC.error("You must hold an item!"));
            return true;
        }

        Component itemName;
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().displayName();
        } else {
            String matName = item.getType().name().toLowerCase().replace("_", " ");
            itemName = Component.text(matName);
        }

        Component message = CC.translate("<yellow>" + player.getName() + "</yellow> <gray>is showing:</gray> ")
                .append(Component.text("[").color(net.kyori.adventure.text.format.NamedTextColor.GOLD))
                .append(itemName.color(net.kyori.adventure.text.format.NamedTextColor.AQUA))
                .append(Component.text(" x" + item.getAmount()).color(net.kyori.adventure.text.format.NamedTextColor.GRAY))
                .append(Component.text("]").color(net.kyori.adventure.text.format.NamedTextColor.GOLD))
                .hoverEvent(item.asHoverEvent());

        Bukkit.broadcast(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

