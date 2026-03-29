package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class EchestSeeCommand implements TabExecutor, Listener {

    private final JustPlugin plugin;

    // viewer UUID -> target UUID
    private final Map<UUID, UUID> openSessions = new ConcurrentHashMap<>();
    // viewer UUID -> refresh task
    private final Map<UUID, BukkitTask> refreshTasks = new ConcurrentHashMap<>();

    public EchestSeeCommand(JustPlugin plugin) {
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
            player.sendMessage(CC.error("Usage: /echestsee <player>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }

        // Open the actual ender chest inventory (live reference)
        Inventory echest = target.getEnderChest();
        openSessions.put(player.getUniqueId(), target.getUniqueId());
        player.openInventory(echest);
        player.sendMessage(CC.success("Viewing <yellow>" + target.getName() + "</yellow>'s ender chest."));
        plugin.getLogManager().log("moderation", "<yellow>" + player.getName() + "</yellow> opened <yellow>" + target.getName() + "</yellow>'s ender chest");

        // Start periodic refresh task (every 20 ticks = 1 second)
        // Since we're using the real ender chest inventory object, changes sync automatically
        // but we add a task to detect if target logs off
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player t = Bukkit.getPlayer(target.getUniqueId());
            if (t == null || !player.isOnline()) {
                cancelSession(player.getUniqueId());
                if (player.isOnline()) {
                    player.closeInventory();
                    player.sendMessage(CC.warning("Ender chest view closed - player logged off."));
                }
            }
        }, 20L, 20L);
        refreshTasks.put(player.getUniqueId(), task);

        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;
        cancelSession(viewer.getUniqueId());
    }

    private void cancelSession(UUID viewerUuid) {
        openSessions.remove(viewerUuid);
        BukkitTask task = refreshTasks.remove(viewerUuid);
        if (task != null) task.cancel();
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
