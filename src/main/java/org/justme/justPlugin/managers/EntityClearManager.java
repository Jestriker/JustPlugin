package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;

/**
 * Manages automatic entity clearing (similar to ClearLag).
 * Clears ground items and optionally mobs at configurable intervals.
 * Sends warnings before clearing and staff notifications for excessive entities.
 */
public class EntityClearManager {

    private final JustPlugin plugin;
    private SchedulerUtil.CancellableTask clearTask;
    private SchedulerUtil.CancellableTask warningTask;

    public EntityClearManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!isEnabled()) return;

        int intervalSeconds = plugin.getConfig().getInt("entity-clear.interval", 900);
        int warningSeconds = plugin.getConfig().getInt("entity-clear.warning-before", 30);

        long intervalTicks = intervalSeconds * 20L;
        long warningTicks = (intervalSeconds - warningSeconds) * 20L;

        // Warning task
        if (warningSeconds > 0 && warningSeconds < intervalSeconds) {
            warningTask = SchedulerUtil.runTaskTimer(plugin, () -> {
                boolean announcePublic = plugin.getConfig().getBoolean("entity-clear.announce-to-all", true);
                if (announcePublic) {
                    String msg = plugin.getConfig().getString("entity-clear.warning-message",
                            "<yellow>⚠ Ground items will be cleared in %seconds% seconds!</yellow>");
                    msg = msg.replace("%seconds%", String.valueOf(warningSeconds));
                    Bukkit.broadcast(CC.translate(msg));
                }
            }, warningTicks, intervalTicks);
        }

        // Clear task
        clearTask = SchedulerUtil.runTaskTimer(plugin, this::clearNow, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (clearTask != null) {
            clearTask.cancel();
            clearTask = null;
        }
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("entity-clear.enabled", true);
    }

    /**
     * Perform an entity clear now. Returns the count of removed entities.
     */
    public ClearResult clearNow() {
        int itemsRemoved = 0;
        int mobsRemoved = 0;

        boolean clearItems = plugin.getConfig().getBoolean("entity-clear.clear-items", true);
        boolean clearHostile = plugin.getConfig().getBoolean("entity-clear.clear-hostile", false);
        boolean clearFriendly = plugin.getConfig().getBoolean("entity-clear.clear-friendly", false);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                // Skip players
                if (entity instanceof Player) continue;

                // Skip named entities (custom-named mobs shouldn't be cleared)
                if (entity.customName() != null) continue;

                // Clear dropped items
                if (clearItems && entity instanceof Item item) {
                    // Don't clear items in item frames
                    if (item.getPickupDelay() != Short.MAX_VALUE) {
                        entity.remove();
                        itemsRemoved++;
                    }
                    continue;
                }

                // Clear hostile mobs
                if (clearHostile && entity instanceof Monster) {
                    if (!entity.isPersistent() && entity.getPassengers().isEmpty()) {
                        entity.remove();
                        mobsRemoved++;
                    }
                    continue;
                }

                // Clear friendly mobs
                if (clearFriendly && entity instanceof Animals) {
                    if (!entity.isPersistent() && entity.getPassengers().isEmpty()
                            && !(entity instanceof Tameable tameable && tameable.isTamed())) {
                        entity.remove();
                        mobsRemoved++;
                    }
                }
            }
        }

        ClearResult result = new ClearResult(itemsRemoved, mobsRemoved);

        // Announce
        boolean announcePublic = plugin.getConfig().getBoolean("entity-clear.announce-to-all", true);
        if (announcePublic) {
            String msg = plugin.getConfig().getString("entity-clear.clear-message",
                    "<gray>[<gradient:#00aaff:#00ffaa>EntityClear</gradient>] <green>Cleared %items% items and %mobs% mobs.</green>");
            msg = msg.replace("%items%", String.valueOf(itemsRemoved))
                    .replace("%mobs%", String.valueOf(mobsRemoved))
                    .replace("%total%", String.valueOf(itemsRemoved + mobsRemoved));
            Bukkit.broadcast(CC.translate(msg));
        }

        // Log
        plugin.getLogManager().log("admin", "Entity clear: removed <yellow>" + itemsRemoved + "</yellow> items and <yellow>" + mobsRemoved + "</yellow> mobs.");

        // Check for excessive entities and notify staff
        checkExcessiveEntities();

        return result;
    }

    /**
     * Check for excessive entities in chunks and notify staff.
     */
    public void checkExcessiveEntities() {
        int mobThreshold = plugin.getConfig().getInt("entity-clear.excessive-mob-threshold", 50);
        int armorStandThreshold = plugin.getConfig().getInt("entity-clear.excessive-armorstand-threshold", 30);
        int itemFrameThreshold = plugin.getConfig().getInt("entity-clear.excessive-itemframe-threshold", 30);

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                int mobCount = 0;
                int armorStandCount = 0;
                int itemFrameCount = 0;

                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Mob) mobCount++;
                    if (entity instanceof ArmorStand) armorStandCount++;
                    if (entity instanceof ItemFrame) itemFrameCount++;
                }

                String chunkId = world.getName() + " [" + chunk.getX() + ", " + chunk.getZ() + "]";

                if (mobCount >= mobThreshold) {
                    notifyStaff("<yellow>⚠ Excessive mobs</yellow> <gray>(" + mobCount + ") in chunk <white>" + chunkId);
                }
                if (armorStandCount >= armorStandThreshold) {
                    notifyStaff("<yellow>⚠ Excessive armor stands</yellow> <gray>(" + armorStandCount + ") in chunk <white>" + chunkId);
                }
                if (itemFrameCount >= itemFrameThreshold) {
                    notifyStaff("<yellow>⚠ Excessive item frames</yellow> <gray>(" + itemFrameCount + ") in chunk <white>" + chunkId);
                }
            }
        }
    }

    private void notifyStaff(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("justplugin.log.admin")) {
                player.sendMessage(CC.translate("<dark_gray>[<gradient:#ff6b6b:#ee5a24>LOG</gradient>] <gray>[<yellow>ENTITY</yellow>] " + message));
            }
        }
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(CC.translate(message));
        plugin.getLogger().info("[ENTITY] " + plain);
    }

    public record ClearResult(int itemsRemoved, int mobsRemoved) {
        public int total() { return itemsRemoved + mobsRemoved; }
    }
}


