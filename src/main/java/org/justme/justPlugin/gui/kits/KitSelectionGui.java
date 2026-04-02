package org.justme.justPlugin.gui.kits;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.managers.KitManager;
import org.justme.justPlugin.util.CC;

import java.util.*;

/**
 * Kit selection GUI - shows all available kits the player has permission for.
 * Left-click to claim, right-click to preview.
 */
public class KitSelectionGui implements Listener {

    public static final String TITLE = "Kit Selection";
    private final JustPlugin plugin;

    public KitSelectionGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        List<KitManager.KitData> available = plugin.getKitManager().getAvailableKits(player);
        // Calculate inventory size (multiples of 9, min 27, max 54)
        int size = Math.max(27, Math.min(54, ((available.size() / 7) + 1) * 9 + 18));

        Inventory inv = Bukkit.createInventory(null, size, CC.translate("<dark_gray>" + TITLE));

        // Fill border with glass panes
        ItemStack pane = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < size; i++) {
            inv.setItem(i, pane);
        }

        // Place kits in inner slots (avoid borders)
        int kitIndex = 0;
        for (int row = 1; row < (size / 9) - 1; row++) {
            for (int col = 1; col <= 7; col++) {
                if (kitIndex >= available.size()) break;
                int slot = row * 9 + col;
                KitManager.KitData kit = available.get(kitIndex);
                inv.setItem(slot, createKitItem(player, kit));
                kitIndex++;
            }
            if (kitIndex >= available.size()) break;
        }

        player.openInventory(inv);
    }

    private ItemStack createKitItem(Player player, KitManager.KitData kit) {
        // Use first item in kit as the display material, or chest if empty
        Material displayMat = Material.CHEST;
        if (!kit.items.isEmpty()) {
            ItemStack first = kit.items.values().iterator().next();
            if (first != null && first.getType() != Material.AIR) {
                displayMat = first.getType();
            }
        }

        ItemStack item = new ItemStack(displayMat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(CC.translate("<yellow>" + kit.displayName));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        // Contents summary
        lore.add(CC.translate("<white>Contents:"));
        List<String> summary = plugin.getKitManager().getContentsSummary(kit, 5);
        for (String line : summary) {
            lore.add(CC.translate(line));
        }
        lore.add(Component.empty());

        // Cooldown info
        if (kit.cooldownSeconds > 0) {
            lore.add(CC.translate("<white>Cooldown: <yellow>" + CooldownManager.formatTime(kit.cooldownSeconds)));
        } else {
            lore.add(CC.translate("<white>Cooldown: <green>None"));
        }

        // Status for this player
        boolean canClaim = player.hasPermission("justplugin.kit.cooldownbypass")
                || plugin.getKitManager().canClaim(player.getUniqueId(), kit.name);
        if (canClaim) {
            lore.add(CC.translate("<green>Available to claim!"));
        } else {
            int remaining = plugin.getKitManager().getRemainingCooldown(player.getUniqueId(), kit.name);
            lore.add(CC.translate("<red>On cooldown: <yellow>" + CooldownManager.formatTime(remaining)));
        }

        lore.add(Component.empty());
        lore.add(CC.translate("<yellow>Left-click <gray>to claim"));
        lore.add(CC.translate("<yellow>Right-click <gray>to preview"));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!plainTitle.equals(TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        int slot = event.getRawSlot();
        int invSize = event.getInventory().getSize();
        if (slot < 0 || slot >= invSize) return;

        // Find which kit this slot corresponds to
        List<KitManager.KitData> available = plugin.getKitManager().getAvailableKits(player);
        int kitIndex = 0;
        KitManager.KitData targetKit = null;

        for (int row = 1; row < (invSize / 9) - 1; row++) {
            for (int col = 1; col <= 7; col++) {
                if (kitIndex >= available.size()) break;
                int kitSlot = row * 9 + col;
                if (kitSlot == slot) {
                    targetKit = available.get(kitIndex);
                    break;
                }
                kitIndex++;
            }
            if (targetKit != null) break;
        }

        if (targetKit == null) return;

        final KitManager.KitData kit = targetKit;
        if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
            // Preview
            if (player.hasPermission("justplugin.kit.preview") && player.hasPermission(kit.permission)) {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        plugin.getKitPreviewGui().open(player, kit), 1L);
            }
            return;
        }

        // Left-click - claim
        KitManager km = plugin.getKitManager();
        if (!player.hasPermission("justplugin.kit.cooldownbypass") && !km.canClaim(player.getUniqueId(), kit.name)) {
            int remaining = km.getRemainingCooldown(player.getUniqueId(), kit.name);
            player.sendMessage(plugin.getMessageManager().error("kits.on-cooldown",
                    "{kit}", kit.displayName,
                    "{time}", CooldownManager.formatTime(remaining)));
            return;
        }

        player.closeInventory();
        km.claimKit(player, kit.name);
        player.sendMessage(plugin.getMessageManager().success("kits.kit-claimed", "{kit}", kit.displayName));
    }

    // === Utility ===

    private ItemStack makeItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(CC.translate(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
