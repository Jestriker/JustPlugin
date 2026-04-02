package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class EchestSeeOffCommand implements TabExecutor, Listener {

    private final JustPlugin plugin;
    private final Set<UUID> openSessions = ConcurrentHashMap.newKeySet();

    public EchestSeeOffCommand(JustPlugin plugin) {
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
            player.sendMessage(CC.error("Usage: /echestseeoff <player>"));
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

        if (!data.contains("enderchest")) {
            player.sendMessage(plugin.getMessageManager().error("moderation.offline.no-snapshot",
                    "{player}", name));
            return true;
        }

        String title = plugin.getMessageManager().raw("moderation.offline.echestseeoff-title",
                "{player}", name);
        Inventory gui = Bukkit.createInventory(null, 27, CC.translate(title));

        // Load ender chest (27 slots)
        for (int i = 0; i < 27; i++) {
            ItemStack item = data.getItemStack("enderchest.slot_" + i);
            gui.setItem(i, item);
        }

        openSessions.add(player.getUniqueId());
        player.openInventory(gui);
        player.sendMessage(CC.info("Viewing <yellow>" + name + "</yellow>'s saved ender chest <gray>(offline, read-only)."));
        plugin.getLogManager().log("moderation",
                "<yellow>" + player.getName() + "</yellow> opened <yellow>" + name + "</yellow>'s ender chest <gray>(offline)");
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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
