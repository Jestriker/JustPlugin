package org.justme.justPlugin.commands.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;

public class PayNoteCommand implements TabExecutor {

    private final JustPlugin plugin;
    private final NamespacedKey noteKey;

    public PayNoteCommand(JustPlugin plugin) {
        this.plugin = plugin;
        this.noteKey = new NamespacedKey(plugin, "paynote_value");
    }

    public NamespacedKey getNoteKey() {
        return noteKey;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(CC.info("Usage: <yellow>/paynote <amount></yellow>"));
            player.sendMessage(CC.info("Hold exactly <yellow>1 paper</yellow> to create a balance note."));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(CC.error("Invalid amount! Use a number."));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(CC.error("Amount must be greater than zero."));
            return true;
        }

        // Check player is holding exactly 1 paper
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.PAPER || hand.getAmount() != 1) {
            player.sendMessage(CC.error("You must be holding exactly <yellow>1 paper</yellow> (no more, no less)."));
            return true;
        }

        // Check if it's already a pay note
        ItemMeta meta = hand.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(noteKey, PersistentDataType.DOUBLE)) {
            player.sendMessage(CC.error("This paper is already a balance note!"));
            return true;
        }

        // Check player has enough balance
        if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), amount)) {
            player.sendMessage(CC.error("You don't have enough balance! Your balance: <yellow>"
                    + plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player.getUniqueId()))));
            return true;
        }

        // Convert paper into a balance note
        if (meta == null) meta = plugin.getServer().getItemFactory().getItemMeta(Material.PAPER);

        meta.getPersistentDataContainer().set(noteKey, PersistentDataType.DOUBLE, amount);

        String formatted = plugin.getEconomyManager().format(amount);
        meta.displayName(Component.text("Balance Note", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Value: " + formatted, NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Right-click to redeem", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);

        hand.setItemMeta(meta);

        player.sendMessage(CC.success("Created a balance note worth <yellow>" + formatted + "</yellow>. Right-click to redeem!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("<amount>");
        return List.of();
    }
}

