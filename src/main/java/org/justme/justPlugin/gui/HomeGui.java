package org.justme.justPlugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.managers.TeamManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.*;

/**
 * Homes GUI - 9×4 grid.
 * Row 1 (top):    black glass panes
 * Row 2:          spawn flag (slot 10), team home shield (slot 11), 3 bed slots (slots 12-14)
 * Row 3:          spawn info (slot 19), team home dye (slot 20), dyes below each bed (slots 21-23)
 * Row 4 (bottom): black glass panes
 */
public class HomeGui implements Listener {

    public static final String TITLE = "Homes";
    private final JustPlugin plugin;
    // Track pending delete confirmations: player UUID -> home name
    private final Map<UUID, String> pendingDeletes = new HashMap<>();

    public HomeGui(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, CC.translate("<dark_gray>" + TITLE));

        // Fill borders (row 0 and row 3) with black glass panes
        ItemStack pane = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pane);        // row 0
            inv.setItem(27 + i, pane);   // row 3
        }

        // Fill empty slots in rows 1-2 with dark gray panes
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 9; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // --- Spawn slot (row 1, col 1 = slot 10) ---
        boolean spawnDefined = plugin.getConfig().contains("spawn.world") && plugin.getConfig().contains("spawn.x");
        boolean hasSpawnPerm = player.hasPermission("justplugin.spawn");

        if (spawnDefined && hasSpawnPerm) {
            ItemStack spawnItem = makeItem(Material.LIME_BANNER, "<green>Spawn");
            setLore(spawnItem,
                    "<gray>Click to teleport to spawn.",
                    "<dark_gray>Server spawn point.");
            inv.setItem(10, spawnItem);
        } else if (spawnDefined) {
            ItemStack spawnItem = makeItem(Material.WHITE_BANNER, "<red>Spawn");
            setLore(spawnItem, "<red>You don't have permission to teleport to spawn.");
            inv.setItem(10, spawnItem);
        } else {
            ItemStack spawnItem = makeItem(Material.WHITE_BANNER, "<gray>Spawn");
            setLore(spawnItem, "<gray>No spawn has been set on this server.");
            inv.setItem(10, spawnItem);
        }

        // Info below spawn (slot 19)
        if (spawnDefined && hasSpawnPerm) {
            ItemStack spawnDye = makeItem(Material.LIGHT_BLUE_DYE, "<aqua>Teleport to Spawn");
            setLore(spawnDye, "<gray>Click the banner above to teleport.");
            inv.setItem(19, spawnDye);
        } else {
            inv.setItem(19, filler);
        }

        // --- Team Home slot (row 1, col 2 = slot 11) ---
        TeamManager tm = plugin.getTeamManager();
        String teamName = tm.getPlayerTeam(player.getUniqueId());

        if (teamName == null) {
            // Not in a team
            ItemStack shield = makeItem(Material.SHIELD, "<gray>Team Home");
            setLore(shield, "<gray>You are not in a team.");
            inv.setItem(11, shield);
            ItemStack dye = makeItem(Material.GRAY_DYE, "<gray>No Team");
            setLore(dye, "<gray>Join a team to unlock.");
            inv.setItem(20, dye);
        } else {
            TeamManager.TeamData teamData = tm.getTeam(teamName);
            String displayName = teamData != null ? teamData.name : teamName;
            Location teamHome = tm.getTeamHome(teamName);

            if (teamHome == null) {
                // In a team but no home set
                ItemStack shield = makeItem(Material.SHIELD, "<gray>Team " + displayName + "'s Home");
                if (tm.isLeader(player.getUniqueId(), teamName)) {
                    setLore(shield,
                            "<gray>No team home set.",
                            "",
                            "<yellow>Use <gold>/team sethome</gold> to set one.");
                } else {
                    setLore(shield,
                            "<gray>No team home set.",
                            "<gray>Ask a team leader to set one.");
                }
                inv.setItem(11, shield);
                ItemStack dye = makeItem(Material.GRAY_DYE, "<gray>No Team Home");
                setLore(dye, "<gray>Click on the shield above to teleport.");
                inv.setItem(20, dye);
            } else {
                // Team has home - enchanted shield
                ItemStack shield = new ItemStack(Material.SHIELD);
                ItemMeta shieldMeta = shield.getItemMeta();
                if (shieldMeta != null) {
                    shieldMeta.displayName(CC.translate("<green>Team " + displayName + "'s Home"));
                    List<Component> lore = new ArrayList<>();
                    lore.add(CC.translate("<gray>World: <white>" + (teamHome.getWorld() != null ? teamHome.getWorld().getName() : "?")));
                    lore.add(CC.translate("<gray>Coords: <white>" + teamHome.getBlockX() + ", " + teamHome.getBlockY() + ", " + teamHome.getBlockZ()));
                    lore.add(CC.translate(""));
                    lore.add(CC.translate("<yellow>Click to teleport."));
                    shieldMeta.lore(lore);
                    shieldMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    shieldMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    shield.setItemMeta(shieldMeta);
                }
                inv.setItem(11, shield);
                ItemStack dye = makeItem(Material.LIGHT_BLUE_DYE, "<aqua>Team Home");
                setLore(dye, "<gray>Click on the shield above to teleport.");
                inv.setItem(20, dye);
            }
        }

        // --- Home beds (slots 12-14 = row 1, cols 3-5) ---
        int maxHomes = plugin.getHomeManager().getMaxHomes();
        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        // Build an ordered list of homes (fill up to maxHomes slots, max 3 displayed)
        List<String> homeNames = new ArrayList<>(homes.keySet());

        for (int i = 0; i < 5; i++) {
            int slot = 12 + i;
            int actionSlot = 21 + i;

            if (i < maxHomes) {
                if (i < homeNames.size()) {
                    // Defined home = lime bed
                    String name = homeNames.get(i);
                    Location loc = homes.get(name);
                    ItemStack bed = makeItem(Material.LIME_BED, "<green>" + name);
                    setLore(bed,
                            "<gray>World: <white>" + (loc.getWorld() != null ? loc.getWorld().getName() : "?"),
                            "<gray>Coords: <white>" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(),
                            "",
                            "<yellow>Click to teleport.");
                    inv.setItem(slot, bed);

                    // Red dye below = delete
                    if (player.hasPermission("justplugin.delhome")) {
                        ItemStack dye = makeItem(Material.RED_DYE, "<red>Delete " + name);
                        setLore(dye, "<gray>Click to delete this home.", "<red>A confirmation will be shown.");
                        inv.setItem(actionSlot, dye);
                    } else {
                        inv.setItem(actionSlot, filler);
                    }
                } else {
                    // Undefined but available = gray bed
                    int slotNum = i + 1;
                    ItemStack bed = makeItem(Material.GRAY_BED, "<gray>Empty Slot #" + slotNum);
                    setLore(bed, "<gray>Available home slot.", "<yellow>Use the dye below to set a home here.");
                    inv.setItem(slot, bed);

                    // Yellow dye below = set home
                    if (player.hasPermission("justplugin.sethome")) {
                        ItemStack dye = makeItem(Material.YELLOW_DYE, "<yellow>Set Home #" + slotNum);
                        setLore(dye, "<gray>Click to set a home at your current location.");
                        inv.setItem(actionSlot, dye);
                    } else {
                        inv.setItem(actionSlot, filler);
                    }
                }
            } else {
                // Over max limit = red bed
                ItemStack bed = makeItem(Material.RED_BED, "<red>Locked");
                setLore(bed,
                        "<gray>Maximum homes on this server: <yellow>" + maxHomes,
                        "<red>This slot is not available.");
                inv.setItem(slot, bed);

                // Black dye below
                ItemStack dye = makeItem(Material.BLACK_DYE, "<dark_gray>Unavailable");
                setLore(dye, "<gray>Max homes limit: <yellow>" + maxHomes);
                inv.setItem(actionSlot, dye);
            }
        }

        // Clear any pending deletes
        pendingDeletes.remove(player.getUniqueId());

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
        if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE
                || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 36) return;

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        List<String> homeNames = new ArrayList<>(homes.keySet());
        int maxHomes = plugin.getHomeManager().getMaxHomes();

        // --- Spawn click (slot 10) ---
        if (slot == 10) {
            if (!player.hasPermission("justplugin.spawn")) {
                player.sendMessage(CC.error("You don't have permission to teleport to spawn."));
                return;
            }
            boolean spawnDefined = plugin.getConfig().contains("spawn.world") && plugin.getConfig().contains("spawn.x");
            if (!spawnDefined) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.spawn.not-set")));
                return;
            }
            player.closeInventory();

            // Delay check (time between uses) - requires explicit delaybypass permission
            if (!player.hasPermission("justplugin.spawn.delaybypass")
                    && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "spawn")) {
                int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "spawn");
                player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
                return;
            }

            String worldName = plugin.getConfig().getString("spawn.world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.spawn.world-not-found")));
                return;
            }

            Location spawnLoc = new Location(
                    Bukkit.getWorld(worldName),
                    plugin.getConfig().getDouble("spawn.x"),
                    plugin.getConfig().getDouble("spawn.y"),
                    plugin.getConfig().getDouble("spawn.z"),
                    (float) plugin.getConfig().getDouble("spawn.yaw"),
                    (float) plugin.getConfig().getDouble("spawn.pitch"));

            boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                    player, spawnLoc, "justplugin.teleport.bypass", "spawn", "justplugin.spawn.unsafetp");
            if (initiated) {
                plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "spawn");
                player.sendMessage(CC.success(plugin.getMessageManager().raw("teleport.spawn.teleporting")));
            }
            return;
        }

        // --- Team Home click (slot 11) ---
        if (slot == 11) {
            TeamManager tm = plugin.getTeamManager();
            String teamName = tm.getPlayerTeam(player.getUniqueId());
            if (teamName == null) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("team.general.not-in-team")));
                return;
            }
            Location teamHome = tm.getTeamHome(teamName);
            if (teamHome == null) {
                player.sendMessage(CC.error("Your team does not have a home set!"));
                if (tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.info("Use <yellow>/team sethome</yellow> to set one."));
                }
                return;
            }
            player.closeInventory();

            // Delay check (time between uses) - requires explicit delaybypass permission
            if (!player.hasPermission("justplugin.teamhome.delaybypass")
                    && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "teamhome")) {
                int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "teamhome");
                player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
                return;
            }

            boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                    player, teamHome, "justplugin.teamhome.cooldownbypass", "teamhome", "justplugin.teamhome.unsafetp");
            if (initiated) {
                plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "teamhome");
                TeamManager.TeamData teamData = tm.getTeam(teamName);
                player.sendMessage(CC.success("Teleporting to team <yellow>" + (teamData != null ? teamData.name : teamName) + "</yellow>'s home."));
            }
            return;
        }

        // --- Team Home dye click (slot 20) - informational only ---
        if (slot == 20) {
            // Do nothing - the dye is informational
            return;
        }

        // --- Bed click (slots 12-16) = teleport to home ---
        if (slot >= 12 && slot <= 16) {
            int index = slot - 12;
            if (index < homeNames.size()) {
                // Defined home - teleport
                String name = homeNames.get(index);
                Location loc = homes.get(name);
                if (loc == null) return;

                if (!player.hasPermission("justplugin.home")) {
                    player.sendMessage(CC.error("You don't have permission to teleport to homes."));
                    return;
                }

                player.closeInventory();

                // Delay check (time between uses) - requires explicit delaybypass permission
                if (!player.hasPermission("justplugin.home.delaybypass")
                        && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "home")) {
                    int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "home");
                    player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
                    return;
                }

                boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                        player, loc, "justplugin.home.cooldownbypass", "home", "justplugin.home.unsafetp");
                if (initiated) {
                    plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "home");
                    player.sendMessage(CC.success("Teleporting to home <yellow>" + name + "</yellow>."));
                }
            } else if (index < maxHomes) {
                // Gray bed - say it's empty
                player.sendMessage(CC.info("This home slot is empty. Use the <yellow>yellow dye</yellow> below to set a home here."));
            } else {
                // Red bed - say it's locked
                player.sendMessage(CC.error("This slot is locked. Maximum homes: <yellow>" + maxHomes));
            }
            return;
        }

        // --- Action dyes (slots 21-25) ---
        if (slot >= 21 && slot <= 25) {
            int index = slot - 21;

            if (clicked.getType() == Material.RED_DYE) {
                // Delete home
                if (index >= homeNames.size()) return;
                String name = homeNames.get(index);

                String pendingName = pendingDeletes.get(player.getUniqueId());
                if (name.equals(pendingName)) {
                    // Second click = confirmed
                    pendingDeletes.remove(player.getUniqueId());
                    if (plugin.getHomeManager().deleteHome(player.getUniqueId(), name)) {
                        player.sendMessage(CC.success("Home <yellow>" + name + "</yellow> has been deleted."));
                        player.closeInventory();
                        // Re-open refreshed GUI
                        SchedulerUtil.runForEntityLater(plugin, player, () -> open(player), 2L);
                    } else {
                        player.sendMessage(CC.error("Failed to delete home <yellow>" + name + "</yellow>."));
                    }
                } else {
                    // First click = show confirmation
                    pendingDeletes.put(player.getUniqueId(), name);
                    // Update the dye to show confirm state
                    ItemStack confirm = makeItem(Material.RED_DYE, "<red><bold>Confirm Delete?");
                    setLore(confirm,
                            "<gray>Home: <yellow>" + name,
                            "",
                            "<red>Click again to confirm deletion.",
                            "<gray>Click anything else to cancel.");
                    event.getInventory().setItem(slot, confirm);
                }
            } else if (clicked.getType() == Material.YELLOW_DYE) {
                // Set home
                if (!player.hasPermission("justplugin.sethome")) {
                    player.sendMessage(CC.error("You don't have permission to set homes."));
                    return;
                }
                int homeNumber = homeNames.size() + 1;
                String name = "home" + homeNumber;
                // Find an unused name
                while (homes.containsKey(name)) {
                    homeNumber++;
                    name = "home" + homeNumber;
                }

                if (plugin.getHomeManager().setHome(player.getUniqueId(), name, player.getLocation())) {
                    player.sendMessage(CC.success("Home <yellow>" + name + "</yellow> has been set at your current location."));
                    player.closeInventory();
                    SchedulerUtil.runForEntityLater(plugin, player, () -> open(player), 2L);
                } else {
                    player.sendMessage(CC.error("You have reached the maximum number of homes!"));
                }
            } else if (clicked.getType() == Material.BLACK_DYE) {
                player.sendMessage(CC.error("This slot is locked. Maximum homes on this server: <yellow>" + maxHomes));
            }
            return;
        }

        // Any other click clears pending delete
        pendingDeletes.remove(player.getUniqueId());
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



