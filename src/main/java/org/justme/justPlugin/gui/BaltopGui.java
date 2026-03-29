package org.justme.justPlugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;

/**
 * Balance Top GUI - 9×4 grid.
 * Row 0 (top):    black glass panes
 * Row 1:          top 5 player heads (slots 11-15)
 * Row 2:          next 5 player heads (slots 20-24)
 * Row 3 (bottom): black glass panes + player's own rank item
 */
public class BaltopGui implements Listener {

    public static final String TITLE = "Top Balances";
    private final JustPlugin plugin;

    public BaltopGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        Inventory inv = Bukkit.createInventory(null, 36, CC.translate("<dark_gray>" + TITLE));

        // Fill borders
        ItemStack pane = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pane);        // row 0
            inv.setItem(27 + i, pane);   // row 3
        }

        // Fill remaining slots with gray panes
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 9; i < 27; i++) {
            inv.setItem(i, filler);
        }

        boolean isOp = viewer.hasPermission("justplugin.baltop.viewhidden");
        List<Map.Entry<UUID, Double>> sorted = plugin.getEconomyManager().getAllBalancesSorted();

        // Slots for top 10: row 1 slots 11-15 (ranks 1-5), row 2 slots 20-24 (ranks 6-10)
        int[] row1Slots = {11, 12, 13, 14, 15};
        int[] row2Slots = {20, 21, 22, 23, 24};

        int rank = 0;
        int displayIndex = 0;
        for (Map.Entry<UUID, Double> entry : sorted) {
            if (displayIndex >= 10) break;

            UUID uuid = entry.getKey();
            double balance = entry.getValue();
            boolean hidden = plugin.getEconomyManager().isBaltopHidden(uuid);

            rank++;

            if (hidden && !isOp) {
                // Hidden for non-staff: skip entirely (don't count toward display)
                continue;
            }

            int slot;
            if (displayIndex < 5) {
                slot = row1Slots[displayIndex];
            } else {
                slot = row2Slots[displayIndex - 5];
            }

            OfflinePlayer offP = Bukkit.getOfflinePlayer(uuid);
            String name = offP.getName() != null ? offP.getName() : uuid.toString().substring(0, 8);

            if (hidden) {
                // Staff can see but with hidden indicator
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                if (skullMeta != null) {
                    skullMeta.setOwningPlayer(offP);
                    skullMeta.displayName(CC.translate(getMedal(rank) + " <strikethrough>" + name + "</strikethrough> <red>[Hidden]"));
                    List<Component> lore = new ArrayList<>();
                    lore.add(CC.translate("<gray>Balance: <yellow>" + plugin.getEconomyManager().format(balance)));
                    lore.add(CC.translate("<red>This player is hidden from public view."));
                    skullMeta.lore(lore);
                    head.setItemMeta(skullMeta);
                }
                inv.setItem(slot, head);
            } else {
                // Normal display
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                if (skullMeta != null) {
                    skullMeta.setOwningPlayer(offP);
                    skullMeta.displayName(CC.translate(getMedal(rank) + " <white>" + name));
                    List<Component> lore = new ArrayList<>();
                    lore.add(CC.translate("<gray>Balance: <yellow>" + plugin.getEconomyManager().format(balance)));
                    lore.add(CC.translate("<gray>Rank: <gold>#" + rank));
                    skullMeta.lore(lore);
                    head.setItemMeta(skullMeta);
                }
                inv.setItem(slot, head);
            }

            displayIndex++;
        }

        // Show viewer's own rank in bottom row center (slot 31)
        int playerRank = 1;
        for (Map.Entry<UUID, Double> entry : sorted) {
            if (entry.getKey().equals(viewer.getUniqueId())) break;
            playerRank++;
        }
        double myBal = plugin.getEconomyManager().getBalance(viewer.getUniqueId());
        ItemStack selfItem = makeItem(Material.GOLD_INGOT, "<gold>Your Rank");
        setLore(selfItem,
                "<gray>Rank: <gold>#" + playerRank,
                "<gray>Balance: <yellow>" + plugin.getEconomyManager().format(myBal));
        inv.setItem(31, selfItem);

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!plainTitle.equals(TITLE)) return;
        event.setCancelled(true);
    }

    private String getMedal(int rank) {
        return switch (rank) {
            case 1 -> "<gold>🥇 #1";
            case 2 -> "<gray>🥈 #2";
            case 3 -> "<#cd7f32>🥉 #3";
            default -> "<dark_gray>#" + rank;
        };
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


