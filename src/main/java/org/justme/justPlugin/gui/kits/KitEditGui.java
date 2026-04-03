package org.justme.justPlugin.gui.kits;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.managers.KitManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kit creation and editing GUI.
 * 54-slot inventory:
 * - Top row (0-8): armor slot markers showing where to place armor items
 * - Rows 1-4 (9-44): item slots for kit contents
 * - Bottom row (45-53): Save (green wool), Cancel (red wool), Settings (nether star)
 */
public class KitEditGui implements Listener {

    public static final String CREATE_TITLE = "Create New Kit";
    public static final String EDIT_TITLE_PREFIX = "Edit Kit: ";
    private final JustPlugin plugin;

    // Track which players are in edit mode and what kit they're editing (null = creating new)
    private final Map<UUID, String> editingSessions = new ConcurrentHashMap<>();

    // Track players in chat input mode for settings
    private final Map<UUID, String> chatInputMode = new ConcurrentHashMap<>(); // UUID -> "displayname" | "cooldown" | "kitname"

    // Store pending kit names for new kits being created
    private final Map<UUID, String> pendingKitNames = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingDisplayNames = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> pendingCooldowns = new ConcurrentHashMap<>();

    public KitEditGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the GUI in create mode. First asks for a kit name via chat.
     */
    public void openCreate(Player player) {
        player.sendMessage(CC.prefixed("<yellow>Enter a name for the new kit in chat:"));
        player.sendMessage(CC.line("<gray>Type <red>cancel</red> to cancel."));
        chatInputMode.put(player.getUniqueId(), "kitname");
    }

    /**
     * Open the GUI in edit mode for an existing kit.
     */
    public void openEdit(Player player, KitManager.KitData kit) {
        editingSessions.put(player.getUniqueId(), kit.name);
        pendingDisplayNames.put(player.getUniqueId(), kit.displayName);
        pendingCooldowns.put(player.getUniqueId(), kit.cooldownSeconds);

        Inventory inv = Bukkit.createInventory(null, 54,
                CC.translate("<dark_gray>" + EDIT_TITLE_PREFIX + kit.displayName));

        setupInventory(inv);

        // Load existing kit items
        for (Map.Entry<Integer, ItemStack> entry : kit.items.entrySet()) {
            int slot = entry.getKey();
            // Offset items into rows 1-4 (slots 9-44)
            int guiSlot = slot + 9;
            if (guiSlot >= 9 && guiSlot < 45) {
                inv.setItem(guiSlot, entry.getValue().clone());
            }
        }

        player.openInventory(inv);
    }

    private void openCreateInventory(Player player, String kitName) {
        editingSessions.put(player.getUniqueId(), null); // null = creating new
        pendingKitNames.put(player.getUniqueId(), kitName);
        if (!pendingDisplayNames.containsKey(player.getUniqueId())) {
            pendingDisplayNames.put(player.getUniqueId(), kitName);
        }
        if (!pendingCooldowns.containsKey(player.getUniqueId())) {
            pendingCooldowns.put(player.getUniqueId(), 3600);
        }

        Inventory inv = Bukkit.createInventory(null, 54,
                CC.translate("<dark_gray>" + CREATE_TITLE));
        setupInventory(inv);
        player.openInventory(inv);
    }

    private void setupInventory(Inventory inv) {
        // Top row: armor slot markers
        ItemStack helmetMarker = makeMarker(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Helmet Slot", "<gray>Place helmet here");
        ItemStack chestMarker = makeMarker(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Chestplate Slot", "<gray>Place chestplate here");
        ItemStack legsMarker = makeMarker(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Leggings Slot", "<gray>Place leggings here");
        ItemStack bootsMarker = makeMarker(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Boots Slot", "<gray>Place boots here");
        ItemStack offhandMarker = makeMarker(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<white>Offhand Slot", "<gray>Place offhand item here");

        ItemStack topFiller = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, topFiller);
        inv.setItem(2, helmetMarker);
        inv.setItem(3, chestMarker);
        inv.setItem(4, legsMarker);
        inv.setItem(5, bootsMarker);
        inv.setItem(6, offhandMarker);

        // Bottom row
        ItemStack bottomFiller = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) inv.setItem(i, bottomFiller);

        // Save button (slot 48)
        ItemStack saveBtn = makeItem(Material.GREEN_WOOL, "<green><bold>Save Kit");
        setLore(saveBtn, "<gray>Click to save the kit.", "<gray>Items in rows 1-4 will be saved.");
        inv.setItem(48, saveBtn);

        // Cancel button (slot 50)
        ItemStack cancelBtn = makeItem(Material.RED_WOOL, "<red><bold>Cancel");
        setLore(cancelBtn, "<gray>Click to cancel without saving.");
        inv.setItem(50, cancelBtn);

        // Settings button (slot 49)
        ItemStack settingsBtn = makeItem(Material.NETHER_STAR, "<gold><bold>Settings");
        setLore(settingsBtn,
                "<gray>Click to configure:",
                "<yellow>- Display Name",
                "<yellow>- Cooldown",
                "",
                "<gray>Opens chat input.");
        inv.setItem(49, settingsBtn);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);

        boolean isCreate = plainTitle.equals(CREATE_TITLE);
        boolean isEdit = plainTitle.startsWith("Edit Kit: ");
        if (!isCreate && !isEdit) return;

        int slot = event.getRawSlot();

        // Allow item manipulation in rows 1-4 (slots 9-44)
        if (slot >= 9 && slot < 45) {
            // Allow normal item movement here (don't cancel)
            return;
        }

        // Allow player inventory interaction (slot >= 54)
        if (slot >= 54) {
            return;
        }

        // Cancel clicks on top row and bottom row
        event.setCancelled(true);

        // Top row armor markers (slots 0-8) - allow dropping items onto them
        if (slot >= 0 && slot < 9) {
            // The markers are informational
            return;
        }

        // Bottom row buttons
        if (slot == 48) {
            // Save
            saveKit(player, event.getInventory());
            return;
        }
        if (slot == 50) {
            // Cancel
            cleanup(player);
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().info("kits.edit-cancelled"));
            return;
        }
        if (slot == 49) {
            // Settings - open chat input
            player.closeInventory();
            player.sendMessage(CC.prefixed("<yellow>Kit Settings:"));
            player.sendMessage(CC.line("<yellow>1. <gray>Type <yellow>name <value></yellow> to set display name"));
            player.sendMessage(CC.line("<yellow>2. <gray>Type <yellow>cooldown <seconds></yellow> to set cooldown"));
            player.sendMessage(CC.line("<gray>Type <red>done</red> to return to the editor."));
            chatInputMode.put(player.getUniqueId(), "settings");
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // We don't cleanup on close because settings mode requires closing the inventory
        // Cleanup happens on save/cancel/chat cancel
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String mode = chatInputMode.get(uuid);
        if (mode == null) return;

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (message.equalsIgnoreCase("cancel")) {
            chatInputMode.remove(uuid);
            cleanup(player);
            player.sendMessage(plugin.getMessageManager().info("kits.edit-cancelled"));
            return;
        }

        switch (mode) {
            case "kitname" -> {
                if (message.contains(" ") || message.isEmpty()) {
                    player.sendMessage(CC.error("Kit names cannot contain spaces. Try again or type <yellow>cancel</yellow>."));
                    return;
                }
                if (plugin.getKitManager().getKit(message) != null) {
                    player.sendMessage(plugin.getMessageManager().error("kits.kit-already-exists", "{kit}", message));
                    return;
                }
                chatInputMode.remove(uuid);
                pendingKitNames.put(uuid, message.toLowerCase());
                // Open the edit GUI on the main thread
                SchedulerUtil.runTask(plugin, () -> openCreateInventory(player, message.toLowerCase()));
            }
            case "settings" -> {
                if (message.equalsIgnoreCase("done")) {
                    chatInputMode.remove(uuid);
                    // Re-open the edit GUI
                    String kitName = editingSessions.get(uuid);
                    SchedulerUtil.runTask(plugin, () -> {
                        if (kitName == null) {
                            // Creating new - reopen create GUI
                            String newName = pendingKitNames.get(uuid);
                            if (newName != null) {
                                openCreateInventory(player, newName);
                            }
                        } else {
                            KitManager.KitData kit = plugin.getKitManager().getKit(kitName);
                            if (kit != null) {
                                openEdit(player, kit);
                            }
                        }
                    });
                    return;
                }

                if (message.toLowerCase().startsWith("name ")) {
                    String displayName = message.substring(5).trim();
                    if (displayName.isEmpty()) {
                        player.sendMessage(CC.error("Display name cannot be empty."));
                        return;
                    }
                    pendingDisplayNames.put(uuid, displayName);
                    player.sendMessage(CC.success("Display name set to: <yellow>" + displayName));
                    player.sendMessage(CC.line("<gray>Type <yellow>cooldown <seconds></yellow> or <red>done</red> to return."));
                } else if (message.toLowerCase().startsWith("cooldown ")) {
                    try {
                        int seconds = Integer.parseInt(message.substring(9).trim());
                        if (seconds < 0) seconds = 0;
                        pendingCooldowns.put(uuid, seconds);
                        player.sendMessage(CC.success("Cooldown set to: <yellow>" + CooldownManager.formatTime(seconds)));
                        player.sendMessage(CC.line("<gray>Type <yellow>name <value></yellow> or <red>done</red> to return."));
                    } catch (NumberFormatException e) {
                        player.sendMessage(CC.error("Invalid number. Use: <yellow>cooldown <seconds>"));
                    }
                } else {
                    player.sendMessage(CC.error("Unknown setting. Use <yellow>name <value></yellow>, <yellow>cooldown <seconds></yellow>, or <red>done</red>."));
                }
            }
        }
    }

    private void saveKit(Player player, Inventory inv) {
        UUID uuid = player.getUniqueId();
        String existingKit = editingSessions.get(uuid);
        KitManager km = plugin.getKitManager();

        // Collect items from rows 1-4 (slots 9-44)
        Map<Integer, ItemStack> items = new LinkedHashMap<>();
        for (int i = 9; i < 45; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.put(i - 9, item.clone()); // Store with 0-based slot index
            }
        }

        String displayName = pendingDisplayNames.getOrDefault(uuid, "Kit");
        int cooldown = pendingCooldowns.getOrDefault(uuid, 3600);

        if (existingKit == null) {
            // Creating new kit
            String kitName = pendingKitNames.getOrDefault(uuid, "kit" + System.currentTimeMillis());
            if (km.createKit(kitName, displayName, items)) {
                km.setCooldown(kitName, cooldown);
                player.closeInventory();
                cleanup(player);
                player.sendMessage(plugin.getMessageManager().success("kits.kit-created", "{kit}", displayName));
                player.sendMessage(CC.line("<gray>Use <yellow>/kitpublish " + kitName + "</yellow> to make it available to players."));
            } else {
                player.sendMessage(plugin.getMessageManager().error("kits.kit-already-exists", "{kit}", kitName));
            }
        } else {
            // Updating existing kit
            km.updateKit(existingKit, items);
            km.setDisplayName(existingKit, displayName);
            km.setCooldown(existingKit, cooldown);
            player.closeInventory();
            cleanup(player);
            player.sendMessage(plugin.getMessageManager().success("kits.kit-updated", "{kit}", displayName));
        }
    }

    private void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        editingSessions.remove(uuid);
        chatInputMode.remove(uuid);
        pendingKitNames.remove(uuid);
        pendingDisplayNames.remove(uuid);
        pendingCooldowns.remove(uuid);
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

    private ItemStack makeMarker(Material material, String name, String loreText) {
        ItemStack item = makeItem(material, name);
        setLore(item, loreText);
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
