package org.justme.justPlugin.gui.kits;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.KitManager;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read-only preview GUI for a kit's contents.
 * Top row: armor slots (helmet, chestplate, leggings, boots, offhand).
 * Remaining slots: kit items in their configured positions.
 * Empty slots: gray glass panes. Bottom row has a cancel button.
 */
public class KitPreviewGui implements Listener {

    public static final String TITLE_PREFIX = "Kit Preview: ";
    private final JustPlugin plugin;

    public KitPreviewGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, KitManager.KitData kit) {
        Inventory inv = Bukkit.createInventory(null, 54,
                CC.translate("<dark_gray>" + TITLE_PREFIX + kit.displayName));

        // Fill all slots with gray glass
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        // Top row: armor slot markers
        // Slot 0: Helmet, Slot 1: Chestplate, Slot 2: Leggings, Slot 3: Boots, Slot 4: Offhand
        setArmorMarker(inv, 2, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Helmet Slot", kit);
        setArmorMarker(inv, 3, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Chestplate Slot", kit);
        setArmorMarker(inv, 4, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Leggings Slot", kit);
        setArmorMarker(inv, 5, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Boots Slot", kit);
        setArmorMarker(inv, 6, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Offhand Slot", kit);

        // Find armor items from the kit and place them in the top row
        for (Map.Entry<Integer, ItemStack> entry : kit.items.entrySet()) {
            ItemStack item = entry.getValue();
            Material mat = item.getType();
            String name = mat.name();

            if (isHelmet(name)) {
                inv.setItem(2, item.clone());
            } else if (isChestplate(name)) {
                inv.setItem(3, item.clone());
            } else if (isLeggings(name)) {
                inv.setItem(4, item.clone());
            } else if (isBoots(name)) {
                inv.setItem(5, item.clone());
            }
        }

        // Place non-armor items in rows 2-5 (slots 9-44)
        int slotIndex = 9;
        for (Map.Entry<Integer, ItemStack> entry : kit.items.entrySet()) {
            ItemStack item = entry.getValue();
            String name = item.getType().name();
            // Skip armor items (already shown in top row)
            if (isHelmet(name) || isChestplate(name) || isLeggings(name) || isBoots(name)) {
                continue;
            }
            if (slotIndex < 45) {
                inv.setItem(slotIndex, item.clone());
                slotIndex++;
            }
        }

        // Bottom row (slots 45-53): cancel button in center
        ItemStack cancelBtn = makeItem(Material.BARRIER, "<red>Close");
        setLore(cancelBtn, "<gray>Click to close this preview.");
        inv.setItem(49, cancelBtn);

        player.openInventory(inv);
    }

    private void setArmorMarker(Inventory inv, int slot, Material material, String name, KitManager.KitData kit) {
        ItemStack marker = makeItem(material, name);
        setLore(marker, "<dark_gray>Place armor in this slot");
        inv.setItem(slot, marker);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!plainTitle.startsWith("Kit Preview: ")) return;

        event.setCancelled(true);

        // Close button
        if (event.getRawSlot() == 49) {
            player.closeInventory();
        }
    }

    // === Armor detection ===

    private boolean isHelmet(String name) {
        return name.endsWith("_HELMET") || name.endsWith("_CAP") || name.equals("TURTLE_HELMET")
                || name.equals("CARVED_PUMPKIN") || name.equals("PLAYER_HEAD")
                || name.equals("CREEPER_HEAD") || name.equals("ZOMBIE_HEAD")
                || name.equals("SKELETON_SKULL") || name.equals("WITHER_SKELETON_SKULL")
                || name.equals("DRAGON_HEAD") || name.equals("PIGLIN_HEAD");
    }

    private boolean isChestplate(String name) {
        return name.endsWith("_CHESTPLATE") || name.endsWith("_TUNIC") || name.equals("ELYTRA");
    }

    private boolean isLeggings(String name) {
        return name.endsWith("_LEGGINGS") || name.endsWith("_PANTS");
    }

    private boolean isBoots(String name) {
        return name.endsWith("_BOOTS");
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

    private void setLore(ItemStack item, String... lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(CC.translate(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
    }
}
