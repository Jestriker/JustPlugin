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
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class InvseeOffCommand implements TabExecutor, Listener {

    private final JustPlugin plugin;
    private final Set<UUID> openSessions = ConcurrentHashMap.newKeySet();

    public InvseeOffCommand(JustPlugin plugin) {
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
            player.sendMessage(plugin.getMessageManager().error("moderation.invsee.usage"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        if (!offline.hasPlayedBefore() && !offline.isOnline()) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }

        String name = offline.getName() != null ? offline.getName() : args[0];
        YamlConfiguration data = plugin.getDataManager().getPlayerData(offline.getUniqueId());

        if (!data.contains("inventory")) {
            player.sendMessage(plugin.getMessageManager().error("moderation.offline.no-snapshot",
                    "{player}", name));
            return true;
        }

        String title = plugin.getMessageManager().raw("moderation.offline.invseeoff-title",
                "{player}", name);
        Inventory gui = Bukkit.createInventory(null, 54, CC.translate(title));

        // Load main inventory (slots 0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = data.getItemStack("inventory.slot_" + i);
            gui.setItem(i, item);
        }

        // Separator row
        ItemStack separator = createPane(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray>-------");
        for (int i = 36; i <= 44; i++) {
            gui.setItem(i, separator);
        }

        // Armor
        gui.setItem(45, orPane(data.getItemStack("inventory.helmet"), Material.ORANGE_STAINED_GLASS_PANE, "<gold>Helmet <gray>(Empty)"));
        gui.setItem(46, orPane(data.getItemStack("inventory.chestplate"), Material.ORANGE_STAINED_GLASS_PANE, "<gold>Chestplate <gray>(Empty)"));
        gui.setItem(47, orPane(data.getItemStack("inventory.leggings"), Material.ORANGE_STAINED_GLASS_PANE, "<gold>Leggings <gray>(Empty)"));
        gui.setItem(48, orPane(data.getItemStack("inventory.boots"), Material.ORANGE_STAINED_GLASS_PANE, "<gold>Boots <gray>(Empty)"));

        gui.setItem(49, createPane(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray>|"));

        // Offhand
        gui.setItem(50, orPane(data.getItemStack("inventory.offhand"), Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<aqua>Offhand <gray>(Empty)"));

        // Fill remaining
        ItemStack filler = createPane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 51; i <= 53; i++) {
            gui.setItem(i, filler);
        }

        openSessions.add(player.getUniqueId());
        player.openInventory(gui);
        player.sendMessage(plugin.getMessageManager().info("moderation.offline.viewing-inventory",
                "{player}", name));
        plugin.getLogManager().log("moderation",
                "<yellow>" + player.getName() + "</yellow> opened <yellow>" + name + "</yellow>'s inventory <gray>(offline)");
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        if (!openSessions.contains(viewer.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;
        openSessions.remove(viewer.getUniqueId());
    }

    private ItemStack orPane(ItemStack item, Material paneMat, String paneName) {
        return item != null ? item : createPane(paneMat, paneName);
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
