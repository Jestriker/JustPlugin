package org.justme.justPlugin.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.EconomyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaynoteCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economyManager;

    public PaynoteCommand(JustPlugin plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        // Check if player is holding a pay note
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand.getType() == Material.PAPER && inHand.hasItemMeta()
                && inHand.getItemMeta().hasDisplayName()
                && inHand.getItemMeta().getDisplayName().startsWith("Pay Note -")) {
            // Redeem pay note
            String displayName = inHand.getItemMeta().getDisplayName();
            try {
                String amountStr = displayName.replace("Pay Note -", "").replace("coins", "").trim();
                double amount = Double.parseDouble(amountStr);
                economyManager.deposit(player.getUniqueId(), amount);
                player.getInventory().removeItem(new ItemStack(Material.PAPER, 1));
                player.sendMessage("§aRedeemed pay note for §e$" + String.format("%.2f", amount) + "§a. New balance: §e$" + String.format("%.2f", economyManager.getBalance(player.getUniqueId())));
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid pay note.");
            }
            return true;
        }
        // Create pay note
        if (args.length < 1) {
            player.sendMessage("§cUsage: /paynote <amount>  OR hold a Pay Note to redeem it.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }
        if (amount <= 0) {
            player.sendMessage("§cAmount must be positive.");
            return true;
        }
        if (!economyManager.withdraw(player.getUniqueId(), amount)) {
            player.sendMessage("§cInsufficient funds.");
            return true;
        }
        ItemStack note = new ItemStack(Material.PAPER);
        ItemMeta meta = note.getItemMeta();
        meta.setDisplayName("§6Pay Note - " + String.format("%.2f", amount) + " coins");
        meta.setLore(Arrays.asList("§7Hold and use /paynote to redeem", "§7Value: §e$" + String.format("%.2f", amount)));
        note.setItemMeta(meta);
        player.getInventory().addItem(note);
        player.sendMessage("§aCreated a Pay Note worth §e$" + String.format("%.2f", amount) + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
