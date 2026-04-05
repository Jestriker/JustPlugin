package org.justme.justPlugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TransactionManager;
import org.justme.justPlugin.util.CC;

import java.util.*;

/**
 * Transaction History GUI - 6 rows (54 slots).
 * Row 1: Border + title player head center
 * Rows 2-5: Transaction items (7 per row = 28 items per page)
 * Row 6: Border + navigation
 */
public class TransactionHistoryGui implements Listener {

    public static final String LIST_TITLE = "Transaction History";
    public static final String DETAIL_TITLE = "Transaction Detail";
    private final JustPlugin plugin;

    // Track which page and target each viewer is on
    private final Map<UUID, Integer> viewerPages = new HashMap<>();
    private final Map<UUID, UUID> viewerTargets = new HashMap<>();
    // Track which transaction entries are displayed per viewer (for click handling)
    private final Map<UUID, List<TransactionManager.TransactionEntry>> viewerEntries = new HashMap<>();

    public TransactionHistoryGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the transaction history list GUI for a player.
     *
     * @param viewer the player viewing the GUI
     * @param target the player whose transactions to display
     * @param page   the page number (1-based)
     */
    public void open(Player viewer, UUID target, int page) {
        List<TransactionManager.TransactionEntry> transactions = plugin.getTransactionManager().getTransactions(target);
        int totalEntries = transactions.size();
        int itemsPerPage = 28; // 7 per row * 4 rows
        int totalPages = Math.max(1, (int) Math.ceil((double) totalEntries / itemsPerPage));

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        viewerPages.put(viewer.getUniqueId(), page);
        viewerTargets.put(viewer.getUniqueId(), target);

        Inventory inv = Bukkit.createInventory(null, 54, CC.translate("<dark_gray>" + LIST_TITLE));

        // Fill all with border
        ItemStack pane = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, pane);
        }

        // Title item in center of row 1 (slot 4) - player head
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
        String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : target.toString().substring(0, 8);
        ItemStack titleHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) titleHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(targetPlayer);
            skullMeta.displayName(CC.translate("<gold>Transaction History"));
            List<Component> lore = new ArrayList<>();
            lore.add(CC.translate("<gray>Player: <white>" + targetName));
            lore.add(CC.translate("<gray>Total: <yellow>" + totalEntries + " transactions"));
            skullMeta.lore(lore);
            titleHead.setItemMeta(skullMeta);
        }
        inv.setItem(4, titleHead);

        // Fill rows 2-5 with gray panes first, then overlay transactions
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                inv.setItem(row * 9 + col, filler);
            }
        }

        // Place transaction items
        int startIndex = (page - 1) * itemsPerPage;
        List<TransactionManager.TransactionEntry> pageEntries = new ArrayList<>();
        int itemIndex = 0;
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                int txIndex = startIndex + itemIndex;
                if (txIndex >= totalEntries) break;
                TransactionManager.TransactionEntry entry = transactions.get(txIndex);
                int slot = row * 9 + col;
                inv.setItem(slot, createTransactionItem(entry, target));
                pageEntries.add(entry);
                itemIndex++;
            }
            if (startIndex + itemIndex >= totalEntries) break;
        }
        viewerEntries.put(viewer.getUniqueId(), pageEntries);

        // Navigation row (row 6 = slots 45-53)
        // Previous page (slot 45)
        if (page > 1) {
            inv.setItem(45, makeItem(Material.ARROW, "<yellow>Previous Page"));
        } else {
            inv.setItem(45, makeItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Page indicator (slot 49)
        ItemStack pageItem = makeItem(Material.PAPER, "<white>Page " + page + "/" + totalPages);
        inv.setItem(49, pageItem);

        // Next page (slot 53)
        if (page < totalPages) {
            inv.setItem(53, makeItem(Material.ARROW, "<yellow>Next Page"));
        } else {
            inv.setItem(53, makeItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        viewer.openInventory(inv);
    }

    /**
     * Open the detail view for a specific transaction.
     */
    public void openDetail(Player viewer, TransactionManager.TransactionEntry entry) {
        Inventory inv = Bukkit.createInventory(null, 27, CC.translate("<dark_gray>" + DETAIL_TITLE));

        // Fill with gray glass panes
        ItemStack pane = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }

        // Slot 4: Clock with timestamp
        ItemStack clockItem = makeItem(Material.CLOCK, "<gold>Timestamp");
        setLore(clockItem,
                "<gray>Date: <white>" + plugin.getTransactionManager().formatDate(entry.timestamp),
                "<gray>Relative: <white>" + plugin.getTransactionManager().formatRelative(entry.timestamp));
        inv.setItem(4, clockItem);

        // Slot 10: Type indicator
        Material typeMat = getTypeMaterial(entry.type);
        ItemStack typeItem = makeItem(typeMat, "<gold>Type: <white>" + formatTypeName(entry.type));
        if ("PAYNOTE_REDEEM".equals(entry.type)) {
            ItemMeta meta = typeItem.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                typeItem.setItemMeta(meta);
            }
        }
        inv.setItem(10, typeItem);

        // Slot 12: Amount item
        ItemStack amountItem = makeItem(Material.GOLD_INGOT, "<gold>Amount");
        setLore(amountItem, "<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
        inv.setItem(12, amountItem);

        // Slot 14: Player info (player head of other party)
        String otherPlayerName = getOtherPartyName(entry);
        if (otherPlayerName != null && !otherPlayerName.isEmpty()) {
            ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) headItem.getItemMeta();
            if (headMeta != null) {
                @SuppressWarnings("deprecation")
                OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerName);
                headMeta.setOwningPlayer(otherPlayer);
                headMeta.displayName(CC.translate("<gold>Other Party"));
                List<Component> lore = new ArrayList<>();
                lore.add(CC.translate("<gray>Player: <white>" + otherPlayerName));
                headMeta.lore(lore);
                headItem.setItemMeta(headMeta);
            }
            inv.setItem(14, headItem);
        } else {
            inv.setItem(14, makeItem(Material.PLAYER_HEAD, "<gray>No other party"));
        }

        // Slot 16: Details book with all transaction details
        ItemStack bookItem = makeItem(Material.BOOK, "<gold>Details");
        List<String> detailLines = new ArrayList<>();
        detailLines.add("<gray>ID: <dark_gray>" + entry.id.substring(0, 8) + "...");
        detailLines.add("<gray>Type: <white>" + formatTypeName(entry.type));
        detailLines.add("<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
        for (Map.Entry<String, String> detail : entry.details.entrySet()) {
            if ("hidden".equals(detail.getKey()) && "true".equals(detail.getValue())) continue;
            detailLines.add("<gray>" + capitalize(detail.getKey()) + ": <white>" + detail.getValue());
        }
        setLore(bookItem, detailLines.toArray(new String[0]));
        inv.setItem(16, bookItem);

        // Slot 22: Back button
        inv.setItem(22, makeItem(Material.ARROW, "<yellow>Back to List"));

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);

        if (plainTitle.equals(LIST_TITLE)) {
            event.setCancelled(true);
            handleListClick(player, event.getSlot());
        } else if (plainTitle.equals(DETAIL_TITLE)) {
            event.setCancelled(true);
            handleDetailClick(player, event.getSlot());
        }
    }

    private void handleListClick(Player player, int slot) {
        UUID viewerId = player.getUniqueId();
        UUID target = viewerTargets.get(viewerId);
        if (target == null) return;

        Integer currentPage = viewerPages.get(viewerId);
        if (currentPage == null) currentPage = 1;

        // Previous page
        if (slot == 45 && currentPage > 1) {
            open(player, target, currentPage - 1);
            return;
        }

        // Next page
        if (slot == 53) {
            List<TransactionManager.TransactionEntry> transactions = plugin.getTransactionManager().getTransactions(target);
            int totalPages = Math.max(1, (int) Math.ceil((double) transactions.size() / 28));
            if (currentPage < totalPages) {
                open(player, target, currentPage + 1);
            }
            return;
        }

        // Check if clicked slot is a transaction item (rows 2-5, cols 1-7)
        int row = slot / 9;
        int col = slot % 9;
        if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
            int itemIndex = (row - 1) * 7 + (col - 1);
            List<TransactionManager.TransactionEntry> entries = viewerEntries.get(viewerId);
            if (entries != null && itemIndex < entries.size()) {
                openDetail(player, entries.get(itemIndex));
            }
        }
    }

    private void handleDetailClick(Player player, int slot) {
        // Back button
        if (slot == 22) {
            UUID viewerId = player.getUniqueId();
            UUID target = viewerTargets.get(viewerId);
            Integer page = viewerPages.get(viewerId);
            if (target != null) {
                open(player, target, page != null ? page : 1);
            }
        }
    }

    /**
     * Cleanup viewer data when they close the inventory (handled by garbage collection since
     * the maps use UUID keys and players reconnecting get fresh entries).
     */
    public void cleanup(UUID viewer) {
        viewerPages.remove(viewer);
        viewerTargets.remove(viewer);
        viewerEntries.remove(viewer);
    }

    // --- Item creation helpers ---

    private ItemStack createTransactionItem(TransactionManager.TransactionEntry entry, UUID viewerTarget) {
        Material material = getTypeMaterial(entry.type);
        String displayName;
        List<String> loreLines = new ArrayList<>();

        switch (entry.type) {
            case "PAY" -> {
                String from = entry.details.getOrDefault("from", "");
                String to = entry.details.getOrDefault("to", "");
                boolean isReceiver = !from.isEmpty();
                if (isReceiver) {
                    displayName = "<green>+" + plugin.getEconomyManager().format(entry.amount);
                    loreLines.add("<gray>From: <white>" + from);
                } else {
                    displayName = "<red>-" + plugin.getEconomyManager().format(entry.amount);
                    loreLines.add("<gray>To: <white>" + to);
                }
            }
            case "PAYNOTE_CREATE" -> {
                displayName = "<yellow>PayNote Created";
                loreLines.add("<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
            }
            case "PAYNOTE_REDEEM" -> {
                displayName = "<green>PayNote Redeemed";
                loreLines.add("<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
                String creator = entry.details.getOrDefault("creator", "Unknown");
                if (!plugin.getTransactionManager().showPaynoteCreator()) {
                    creator = "Anonymous";
                }
                loreLines.add("<gray>Created by: <white>" + creator);
                // Add enchant glow
                material = Material.PAPER; // Will add enchant after
            }
            case "ADDCASH" -> {
                displayName = "<green>+" + plugin.getEconomyManager().format(entry.amount) + " <gray>(Admin)";
                if (plugin.getTransactionManager().showAddcashToPlayer()) {
                    String staff = entry.details.getOrDefault("staff", "Unknown");
                    loreLines.add("<gray>By: <white>" + staff);
                } else {
                    boolean isHidden = "true".equals(entry.details.getOrDefault("hidden", "false"));
                    if (!isHidden) {
                        String staff = entry.details.getOrDefault("staff", "Unknown");
                        loreLines.add("<gray>By: <white>" + staff);
                    } else {
                        loreLines.add("<gray>By: <white>Staff");
                    }
                }
            }
            case "TRADE" -> {
                displayName = "<gold>Trade Completed";
                String with = entry.details.getOrDefault("with", "Unknown");
                loreLines.add("<gray>With: <white>" + with);
                loreLines.add("<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
            }
            case "API" -> {
                displayName = "<aqua>API Transaction";
                String source = entry.details.getOrDefault("source", "External");
                loreLines.add("<gray>Source: <white>" + source);
                loreLines.add("<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
            }
            default -> {
                displayName = "<gray>Unknown Transaction";
                loreLines.add("<gray>Amount: <yellow>" + plugin.getEconomyManager().format(entry.amount));
            }
        }

        // Add timestamp to all items
        loreLines.add("");
        loreLines.add("<dark_gray>" + plugin.getTransactionManager().formatDate(entry.timestamp));
        loreLines.add("<dark_gray>" + plugin.getTransactionManager().formatRelative(entry.timestamp));

        ItemStack item = makeItem(material, displayName);
        setLore(item, loreLines.toArray(new String[0]));

        // Add enchant glow for PAYNOTE_REDEEM
        if ("PAYNOTE_REDEEM".equals(entry.type)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    private Material getTypeMaterial(String type) {
        return switch (type) {
            case "PAY" -> Material.GOLD_NUGGET;
            case "PAYNOTE_CREATE" -> Material.PAPER;
            case "PAYNOTE_REDEEM" -> Material.PAPER;
            case "ADDCASH" -> Material.EMERALD;
            case "TRADE" -> Material.CHEST;
            case "API" -> Material.ENDER_PEARL;
            default -> Material.BARRIER;
        };
    }

    private String formatTypeName(String type) {
        return switch (type) {
            case "PAY" -> "Payment";
            case "PAYNOTE_CREATE" -> "PayNote Created";
            case "PAYNOTE_REDEEM" -> "PayNote Redeemed";
            case "ADDCASH" -> "Admin Cash";
            case "TRADE" -> "Trade";
            case "API" -> "API Transaction";
            default -> "Unknown";
        };
    }

    private String getOtherPartyName(TransactionManager.TransactionEntry entry) {
        return switch (entry.type) {
            case "PAY" -> {
                String from = entry.details.get("from");
                String to = entry.details.get("to");
                yield from != null ? from : to;
            }
            case "PAYNOTE_REDEEM" -> entry.details.getOrDefault("creator", null);
            case "ADDCASH" -> entry.details.getOrDefault("staff", null);
            case "TRADE" -> entry.details.getOrDefault("with", null);
            case "API" -> entry.details.getOrDefault("source", null);
            default -> null;
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
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
