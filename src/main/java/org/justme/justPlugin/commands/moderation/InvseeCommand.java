package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InvseeCommand implements TabExecutor, Listener {

    private final JustPlugin plugin;

    // viewer UUID -> target UUID
    private final Map<UUID, UUID> openSessions = new ConcurrentHashMap<>();
    // viewer UUID -> refresh task
    private final Map<UUID, BukkitTask> refreshTasks = new ConcurrentHashMap<>();
    // viewer UUID -> GUI inventory
    private final Map<UUID, Inventory> sessionGuis = new ConcurrentHashMap<>();
    // viewer UUID -> whether the session is offline (read-only)
    private final Set<UUID> offlineSessions = ConcurrentHashMap.newKeySet();

    // Armor slot mappings in the GUI
    private static final int SLOT_HELMET = 45;
    private static final int SLOT_CHESTPLATE = 46;
    private static final int SLOT_LEGGINGS = 47;
    private static final int SLOT_BOOTS = 48;
    private static final int SLOT_OFFHAND = 50;

    public InvseeCommand(JustPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /invsee <player>"));
            return true;
        }

        // Try online player first
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            openOnlineSession(player, target);
            plugin.getLogManager().log("moderation", "<yellow>" + player.getName() + "</yellow> opened <yellow>" + target.getName() + "</yellow>'s inventory");
            return true;
        }

        // Try offline player
        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        if (!offline.hasPlayedBefore() && !offline.isOnline()) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }

        String offName = offline.getName() != null ? offline.getName() : args[0];
        plugin.getLogManager().log("moderation", "<yellow>" + player.getName() + "</yellow> opened <yellow>" + offName + "</yellow>'s inventory <gray>(offline)");
        openOfflineSession(player, offline);
        return true;
    }

    private void openOnlineSession(Player viewer, Player target) {
        Inventory gui = Bukkit.createInventory(null, 54, CC.translate("<gold>" + target.getName() + "'s Inventory</gold>"));
        refreshGui(gui, target);

        openSessions.put(viewer.getUniqueId(), target.getUniqueId());
        sessionGuis.put(viewer.getUniqueId(), gui);

        viewer.openInventory(gui);

        // Start periodic refresh task (every 20 ticks = 1 second)
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player t = Bukkit.getPlayer(target.getUniqueId());
            if (t == null || !viewer.isOnline()) {
                cancelSession(viewer.getUniqueId());
                return;
            }
            Inventory g = sessionGuis.get(viewer.getUniqueId());
            if (g != null) {
                refreshGui(g, t);
            }
        }, 20L, 20L);
        refreshTasks.put(viewer.getUniqueId(), task);
    }

    private void openOfflineSession(Player viewer, OfflinePlayer offline) {
        String name = offline.getName() != null ? offline.getName() : offline.getUniqueId().toString();
        YamlConfiguration data = plugin.getDataManager().getPlayerData(offline.getUniqueId());

        if (!data.contains("inventory")) {
            viewer.sendMessage(CC.error("No saved inventory data found for <yellow>" + name + "</yellow>."));
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, CC.translate("<gold>" + name + "'s Inventory <gray>(Offline)</gray></gold>"));

        // Load main inventory (slots 0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = data.getItemStack("inventory.slot_" + i);
            gui.setItem(i, item);
        }

        // Separator row
        ItemStack separator = createPane(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray>───────");
        for (int i = 36; i <= 44; i++) {
            gui.setItem(i, separator);
        }

        // Armor
        ItemStack helmet = data.getItemStack("inventory.helmet");
        ItemStack chestplate = data.getItemStack("inventory.chestplate");
        ItemStack leggings = data.getItemStack("inventory.leggings");
        ItemStack boots = data.getItemStack("inventory.boots");
        ItemStack offhand = data.getItemStack("inventory.offhand");

        gui.setItem(SLOT_HELMET, helmet != null ? helmet : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Helmet <gray>(Empty)"));
        gui.setItem(SLOT_CHESTPLATE, chestplate != null ? chestplate : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Chestplate <gray>(Empty)"));
        gui.setItem(SLOT_LEGGINGS, leggings != null ? leggings : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Leggings <gray>(Empty)"));
        gui.setItem(SLOT_BOOTS, boots != null ? boots : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Boots <gray>(Empty)"));

        gui.setItem(49, createPane(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray>│"));

        gui.setItem(SLOT_OFFHAND, offhand != null ? offhand : createPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<aqua>Offhand <gray>(Empty)"));

        // Fill remaining
        ItemStack filler = createPane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 51; i <= 53; i++) {
            gui.setItem(i, filler);
        }

        openSessions.put(viewer.getUniqueId(), offline.getUniqueId());
        sessionGuis.put(viewer.getUniqueId(), gui);
        offlineSessions.add(viewer.getUniqueId());

        viewer.openInventory(gui);
        viewer.sendMessage(CC.info("Viewing <yellow>" + name + "</yellow>'s saved inventory <gray>(offline, read-only)."));
    }

    private void refreshGui(Inventory gui, Player target) {
        // Row 0-3 (slots 0-35): Target's main inventory
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            gui.setItem(i, i < contents.length && contents[i] != null ? contents[i].clone() : null);
        }

        // Row 4 (slots 36-44): Separator panes
        ItemStack separator = createPane(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray>───────");
        for (int i = 36; i <= 44; i++) {
            gui.setItem(i, separator);
        }

        // Row 5: Armor + Offhand
        ItemStack helmet = target.getInventory().getHelmet();
        ItemStack chestplate = target.getInventory().getChestplate();
        ItemStack leggings = target.getInventory().getLeggings();
        ItemStack boots = target.getInventory().getBoots();
        ItemStack offhand = target.getInventory().getItemInOffHand();

        gui.setItem(SLOT_HELMET, helmet != null ? helmet.clone() : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Helmet <gray>(Empty)"));
        gui.setItem(SLOT_CHESTPLATE, chestplate != null ? chestplate.clone() : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Chestplate <gray>(Empty)"));
        gui.setItem(SLOT_LEGGINGS, leggings != null ? leggings.clone() : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Leggings <gray>(Empty)"));
        gui.setItem(SLOT_BOOTS, boots != null ? boots.clone() : createPane(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Boots <gray>(Empty)"));

        // Separator
        gui.setItem(49, createPane(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray>│"));

        // Offhand
        gui.setItem(SLOT_OFFHAND, offhand.getType() != Material.AIR ? offhand.clone() : createPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<aqua>Offhand <gray>(Empty)"));

        // Fill remaining with dark panes
        ItemStack filler = createPane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 51; i <= 53; i++) {
            gui.setItem(i, filler);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        UUID viewerUuid = viewer.getUniqueId();
        if (!openSessions.containsKey(viewerUuid)) return;
        Inventory gui = sessionGuis.get(viewerUuid);
        if (gui == null || !event.getInventory().equals(gui)) return;

        // Offline sessions are fully read-only
        if (offlineSessions.contains(viewerUuid)) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();

        // Block separator row and filler slots
        if (slot >= 36 && slot <= 44) {
            event.setCancelled(true);
            return;
        }
        if (slot == 49 || (slot >= 51 && slot <= 53)) {
            event.setCancelled(true);
            return;
        }

        // Handle armor slot clicks - allow placing correct armor type
        if (slot == SLOT_HELMET || slot == SLOT_CHESTPLATE || slot == SLOT_LEGGINGS || slot == SLOT_BOOTS) {
            event.setCancelled(true);
            UUID targetUuid = openSessions.get(viewerUuid);
            Player target = targetUuid != null ? Bukkit.getPlayer(targetUuid) : null;
            if (target == null) return;

            ItemStack cursor = event.getCursor();
            if (cursor.getType() == Material.AIR) {
                // Clicking with empty hand on an equipped armor piece - remove it
                ItemStack current = getArmorPiece(target, slot);
                if (current != null && current.getType() != Material.AIR) {
                    // Give the armor piece to the viewer's cursor
                    setArmorPiece(target, slot, null);
                    viewer.setItemOnCursor(current.clone());
                    // Refresh will pick up the change
                }
                return;
            }

            // Validate armor type matches slot
            String typeName = cursor.getType().name();
            boolean valid = switch (slot) {
                case SLOT_HELMET -> typeName.endsWith("_HELMET") || typeName.endsWith("_HEAD") || typeName.endsWith("_SKULL")
                        || cursor.getType() == Material.CARVED_PUMPKIN || cursor.getType() == Material.TURTLE_HELMET;
                case SLOT_CHESTPLATE -> typeName.endsWith("_CHESTPLATE") || cursor.getType() == Material.ELYTRA;
                case SLOT_LEGGINGS -> typeName.endsWith("_LEGGINGS");
                case SLOT_BOOTS -> typeName.endsWith("_BOOTS");
                default -> false;
            };

            if (!valid) {
                viewer.sendMessage(CC.error("That item doesn't go in this armor slot!"));
                return;
            }

            // Place the armor on the target
            ItemStack existing = getArmorPiece(target, slot);
            setArmorPiece(target, slot, cursor.clone());
            viewer.setItemOnCursor(null);
            // If there was existing armor, give it back to cursor
            if (existing != null && existing.getType() != Material.AIR) {
                viewer.setItemOnCursor(existing.clone());
            }
            return;
        }

        // Offhand slot
        if (slot == SLOT_OFFHAND) {
            event.setCancelled(true);
            UUID targetUuid = openSessions.get(viewerUuid);
            Player target = targetUuid != null ? Bukkit.getPlayer(targetUuid) : null;
            if (target == null) return;

            ItemStack cursor = event.getCursor();
            ItemStack currentOffhand = target.getInventory().getItemInOffHand();

            if (cursor.getType() == Material.AIR) {
                // Picking up the offhand item
                if (currentOffhand.getType() != Material.AIR) {
                    viewer.setItemOnCursor(currentOffhand.clone());
                    target.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
            } else {
                // Placing item in offhand
                target.getInventory().setItemInOffHand(cursor.clone());
                viewer.setItemOnCursor(null);
                if (currentOffhand.getType() != Material.AIR) {
                    viewer.setItemOnCursor(currentOffhand.clone());
                }
            }
            return;
        }

        // Allow interaction with main inventory slots (0-35) - read only by default
        // Block all interaction to keep it read-only except armor
        if (slot >= 0 && slot < 36) {
            event.setCancelled(true);
        }
    }

    private ItemStack getArmorPiece(Player target, int guiSlot) {
        return switch (guiSlot) {
            case SLOT_HELMET -> target.getInventory().getHelmet();
            case SLOT_CHESTPLATE -> target.getInventory().getChestplate();
            case SLOT_LEGGINGS -> target.getInventory().getLeggings();
            case SLOT_BOOTS -> target.getInventory().getBoots();
            default -> null;
        };
    }

    private void setArmorPiece(Player target, int guiSlot, ItemStack item) {
        switch (guiSlot) {
            case SLOT_HELMET -> target.getInventory().setHelmet(item);
            case SLOT_CHESTPLATE -> target.getInventory().setChestplate(item);
            case SLOT_LEGGINGS -> target.getInventory().setLeggings(item);
            case SLOT_BOOTS -> target.getInventory().setBoots(item);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;
        cancelSession(viewer.getUniqueId());
    }

    private void cancelSession(UUID viewerUuid) {
        openSessions.remove(viewerUuid);
        sessionGuis.remove(viewerUuid);
        offlineSessions.remove(viewerUuid);
        BukkitTask task = refreshTasks.remove(viewerUuid);
        if (task != null) task.cancel();
    }

    private ItemStack createPane(Material mat, String name) {
        ItemStack pane = new ItemStack(mat);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(CC.translate(name));
        pane.setItemMeta(meta);
        return pane;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

