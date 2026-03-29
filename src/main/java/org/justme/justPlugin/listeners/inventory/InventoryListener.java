package org.justme.justPlugin.listeners.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

/**
 * Handles inventory-related interactions: PayNote redemption on right-click.
 */
public class InventoryListener implements Listener {

    private final JustPlugin plugin;

    public InventoryListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        NamespacedKey noteKey = new NamespacedKey(plugin, "paynote_value");

        // Check which hand holds the pay note (main hand or offhand)
        ItemStack item = null;
        boolean isOffhand = false;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand.getType() == Material.PAPER && mainHand.hasItemMeta()
                && mainHand.getItemMeta().getPersistentDataContainer().has(noteKey, PersistentDataType.DOUBLE)) {
            item = mainHand;
        } else if (offHand.getType() == Material.PAPER && offHand.hasItemMeta()
                && offHand.getItemMeta().getPersistentDataContainer().has(noteKey, PersistentDataType.DOUBLE)) {
            item = offHand;
            isOffhand = true;
        }

        if (item == null) return;

        event.setCancelled(true);

        ItemMeta meta = item.getItemMeta();
        Double rawValue = meta.getPersistentDataContainer().get(noteKey, PersistentDataType.DOUBLE);
        if (rawValue == null) return;
        double value = rawValue;

        // Remove the note and replace with a normal paper
        ItemStack normalPaper = new ItemStack(Material.PAPER, 1);
        if (isOffhand) {
            player.getInventory().setItemInOffHand(normalPaper);
        } else {
            player.getInventory().setItemInMainHand(normalPaper);
        }

        // Give the balance
        plugin.getEconomyManager().addBalance(player.getUniqueId(), value);
        String formatted = plugin.getEconomyManager().format(value);
        player.sendMessage(CC.success("Redeemed balance note for <yellow>" + formatted + "</yellow>!"));
    }
}

