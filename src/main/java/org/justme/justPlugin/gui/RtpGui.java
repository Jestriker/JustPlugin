package org.justme.justPlugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Random Teleport GUI - 9×3 grid.
 * Row 0 (top):    black glass panes
 * Row 1:          Grass Block (slot 11), Netherrack (slot 13), End Stone (slot 15)
 * Row 2 (bottom): black glass panes
 */
public class RtpGui implements Listener {

    public static final String TITLE = "Random Teleport";
    private final JustPlugin plugin;

    public RtpGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, CC.translate("<dark_gray>" + TITLE));

        // Fill borders
        ItemStack pane = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pane);        // row 0
            inv.setItem(18 + i, pane);   // row 2
        }

        // Fill middle row with gray panes
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, filler);
        }

        // --- Overworld (slot 11) ---
        boolean hasOverworldPerm = player.hasPermission("justplugin.wild");
        ItemStack overworld = makeItem(Material.GRASS_BLOCK, 
                hasOverworldPerm ? "<green>Overworld" : "<red>Overworld");
        if (hasOverworldPerm) {
            setLore(overworld,
                    "<gray>Click to randomly teleport",
                    "<gray>in the <green>Overworld<gray>.",
                    "",
                    "<yellow>A safe location will be found for you.");
        } else {
            setLore(overworld, "<red>You don't have permission for this.");
        }
        inv.setItem(11, overworld);

        // --- Nether (slot 13) ---
        boolean hasNetherPerm = player.hasPermission("justplugin.wild.nether");
        World netherWorld = getNetherWorld();
        boolean netherAvailable = netherWorld != null;

        ItemStack nether = makeItem(Material.NETHERRACK,
                (hasNetherPerm && netherAvailable) ? "<red>Nether" : "<dark_red>Nether");
        if (!netherAvailable) {
            setLore(nether, "<red>The Nether is not available on this server.");
        } else if (hasNetherPerm) {
            setLore(nether,
                    "<gray>Click to randomly teleport",
                    "<gray>in the <red>Nether<gray>.",
                    "",
                    "<yellow>A safe location will be found for you.");
        } else {
            setLore(nether, "<red>You don't have permission for this.");
        }
        inv.setItem(13, nether);

        // --- End (slot 15) ---
        boolean hasEndPerm = player.hasPermission("justplugin.wild.end");
        World endWorld = getEndWorld();
        boolean endAvailable = endWorld != null;

        ItemStack end = makeItem(Material.END_STONE,
                (hasEndPerm && endAvailable) ? "<dark_purple>The End" : "<dark_gray>The End");
        if (!endAvailable) {
            setLore(end, "<red>The End is not available on this server.");
        } else if (hasEndPerm) {
            setLore(end,
                    "<gray>Click to randomly teleport",
                    "<gray>in <dark_purple>The End<gray>.",
                    "",
                    "<yellow>A safe location will be found for you.");
        } else {
            setLore(end, "<red>You don't have permission for this.");
        }
        inv.setItem(15, end);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!plainTitle.equals(TITLE)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 27) return;

        World targetWorld = null;
        String worldLabel = null;
        String permRequired = null;

        switch (slot) {
            case 11 -> {
                targetWorld = Bukkit.getWorlds().getFirst(); // Overworld
                worldLabel = "Overworld";
                permRequired = "justplugin.wild";
            }
            case 13 -> {
                targetWorld = getNetherWorld();
                worldLabel = "Nether";
                permRequired = "justplugin.wild.nether";
            }
            case 15 -> {
                targetWorld = getEndWorld();
                worldLabel = "The End";
                permRequired = "justplugin.wild.end";
            }
            default -> { return; }
        }

        if (targetWorld == null) {
            player.sendMessage(CC.error(worldLabel + " is not available on this server."));
            return;
        }

        if (!player.hasPermission(permRequired)) {
            player.sendMessage(CC.error("You don't have permission to random teleport in " + worldLabel + "."));
            return;
        }

        // Delay check (time between uses) - requires explicit delaybypass permission
        if (!player.hasPermission("justplugin.wild.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "wild")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "wild");
            player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
            return;
        }

        player.closeInventory();

        final World finalWorld = targetWorld;
        final String finalLabel = worldLabel;
        player.sendMessage(CC.info("Finding a safe random location in <yellow>" + finalLabel + "</yellow>..."));

        plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "wild");

        Location loc = plugin.getTeleportManager().getRandomLocation(finalWorld);
        loc.getWorld().getChunkAtAsync(loc).thenAccept(chunk -> {
            SchedulerUtil.runTask(plugin, () -> {
                Location safeLoc = plugin.getTeleportManager().getSafeLocation(loc);

                // Verify the safe location is actually safe
                if (!plugin.getTeleportManager().isLocationSafe(safeLoc)) {
                    // Try a few more times
                    for (int attempt = 0; attempt < 10; attempt++) {
                        Location retry = plugin.getTeleportManager().getRandomLocation(finalWorld);
                        Location retrySafe = plugin.getTeleportManager().getSafeLocation(retry);
                        if (plugin.getTeleportManager().isLocationSafe(retrySafe)) {
                            safeLoc = retrySafe;
                            break;
                        }
                    }
                }

                final Location finalLoc = safeLoc;

                // Use teleportWithSafety for proper cooldown countdown + safety checks
                boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                        player, finalLoc,
                        "justplugin.wild.cooldownbypass",
                        "wild",
                        "justplugin.wild.unsafetp"
                );
                if (initiated) {
                    player.sendMessage(CC.success("Teleported to <yellow>" + finalLoc.getBlockX() + ", " + finalLoc.getBlockY() + ", " + finalLoc.getBlockZ() + "</yellow> in " + finalLabel + "."));
                }
            });
        });
    }

    private World getNetherWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NETHER) return w;
        }
        return null;
    }

    private World getEndWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.THE_END) return w;
        }
        return null;
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




