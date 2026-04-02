package org.justme.justPlugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TagManager;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;

/**
 * Tag selection GUI.
 * Shows all tags the player has permission for as NAME_TAG items.
 * Equipped tag has an enchant glow. Click to equip/unequip.
 */
public class TagGui implements Listener {

    public static final String TITLE = "Tags";
    private final JustPlugin plugin;

    public TagGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        TagManager tm = plugin.getTagManager();

        // Collect tags the player has permission for
        List<TagManager.TagData> available = new ArrayList<>();
        for (TagManager.TagData tag : tm.getAllTags()) {
            if (player.hasPermission(tag.permission)) {
                available.add(tag);
            }
        }

        // Calculate inventory size (rows of 9, minimum 9, max 54)
        int size = Math.max(9, (int) Math.ceil((available.size() + 1) / 9.0) * 9);
        if (size > 54) size = 54;
        // Ensure at least 2 rows for a nice look
        if (size < 18) size = 18;

        Inventory inv = Bukkit.createInventory(null, size, CC.translate("<dark_gray>" + TITLE));

        // Fill with glass panes
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < size; i++) {
            inv.setItem(i, filler);
        }

        String equippedId = tm.getEquippedTagId(player.getUniqueId());

        // Place tag items
        int slot = 0;
        for (TagManager.TagData tag : available) {
            if (slot >= size) break;

            // Skip filler positions on first and last row borders if inventory is large
            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(CC.translate(tag.display));

                List<Component> lore = new ArrayList<>();
                lore.add(CC.translate("<gray>ID: <yellow>" + tag.id));
                lore.add(CC.translate("<gray>Type: <yellow>" + tag.type));
                lore.add(CC.translate(""));

                boolean isEquipped = tag.id.equals(equippedId);
                if (isEquipped) {
                    lore.add(CC.translate("<green>Currently equipped!"));
                    lore.add(CC.translate("<yellow>Click to unequip."));
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else {
                    lore.add(CC.translate("<yellow>Click to equip."));
                }

                meta.lore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            slot++;
        }

        // If player has no available tags, show a message item
        if (available.isEmpty()) {
            ItemStack noTags = makeItem(Material.BARRIER, "<red>No Tags Available");
            ItemMeta meta = noTags.getItemMeta();
            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(CC.translate("<gray>You don't have permission"));
                lore.add(CC.translate("<gray>for any tags."));
                meta.lore(lore);
                noTags.setItemMeta(meta);
            }
            inv.setItem(size / 2 - 1, noTags);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!plainTitle.equals(TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        if (clicked.getType() == Material.BARRIER) return;

        if (clicked.getType() != Material.NAME_TAG) return;

        // Find the tag by matching the slot to our ordered list
        TagManager tm = plugin.getTagManager();
        List<TagManager.TagData> available = new ArrayList<>();
        for (TagManager.TagData tag : tm.getAllTags()) {
            if (player.hasPermission(tag.permission)) {
                available.add(tag);
            }
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= available.size()) return;

        TagManager.TagData tag = available.get(slot);
        String equippedId = tm.getEquippedTagId(player.getUniqueId());

        if (tag.id.equals(equippedId)) {
            // Unequip
            tm.unequipTag(player.getUniqueId());
            player.sendMessage(plugin.getMessageManager().success("nick.tag-removed"));
        } else {
            // Equip
            tm.equipTag(player.getUniqueId(), tag.id);
            player.sendMessage(plugin.getMessageManager().success("nick.tag-equipped", "{tag}", tag.display));
        }

        // Refresh the GUI
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> open(player), 2L);
    }

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
