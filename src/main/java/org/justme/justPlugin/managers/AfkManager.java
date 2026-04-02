package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages AFK (Away From Keyboard) status for players.
 * Tracks activity timestamps, auto-sets AFK after idle timeout,
 * optionally kicks long-idle players, and updates the tab list prefix.
 */
public class AfkManager {

    private final JustPlugin plugin;
    private final ConcurrentHashMap<UUID, Boolean> afkPlayers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private int taskId = -1;

    public AfkManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the repeating idle-check task (runs every second).
     */
    public void start() {
        int checkInterval = 20; // every second (20 ticks)
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::checkIdlePlayers, 100L, checkInterval).getTaskId();
    }

    /**
     * Stop the idle-check task.
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Check if a player is currently AFK.
     */
    public boolean isAfk(UUID uuid) {
        return afkPlayers.getOrDefault(uuid, false);
    }

    /**
     * Toggle AFK status for a player.
     */
    public void toggleAfk(Player player) {
        UUID uuid = player.getUniqueId();
        setAfk(player, !isAfk(uuid));
    }

    /**
     * Set AFK status for a player. Sends broadcast and personal messages,
     * and updates the tab list name.
     */
    public void setAfk(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        boolean wasAfk = isAfk(uuid);
        afkPlayers.put(uuid, afk);

        if (afk && !wasAfk) {
            // Player went AFK
            boolean broadcast = plugin.getConfig().getBoolean("afk.broadcast", true);
            String tabPrefix = plugin.getConfig().getString("afk.tab-prefix", "<gray>[AFK] </gray>");

            if (broadcast) {
                Bukkit.broadcast(plugin.getMessageManager().info("player.afk.broadcast-afk", "{player}", player.getName()));
            }
            player.sendMessage(plugin.getMessageManager().info("player.afk.now-afk"));
            player.playerListName(CC.translate(tabPrefix + player.getName()));
        } else if (!afk && wasAfk) {
            // Player returned from AFK
            boolean broadcast = plugin.getConfig().getBoolean("afk.broadcast", true);

            if (broadcast) {
                Bukkit.broadcast(plugin.getMessageManager().info("player.afk.broadcast-return", "{player}", player.getName()));
            }
            player.sendMessage(plugin.getMessageManager().info("player.afk.no-longer-afk"));
            player.playerListName(null); // reset to default
        }

        if (!afk) {
            lastActivity.put(uuid, System.currentTimeMillis());
        }
    }

    /**
     * Record player activity (movement, chat, interaction, etc.).
     * If the player is AFK, this will automatically un-AFK them.
     */
    public void recordActivity(UUID uuid) {
        lastActivity.put(uuid, System.currentTimeMillis());
        if (isAfk(uuid)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                setAfk(p, false);
            }
        }
    }

    /**
     * Handle player join - initialize activity tracking.
     */
    public void handleJoin(UUID uuid) {
        lastActivity.put(uuid, System.currentTimeMillis());
        afkPlayers.put(uuid, false);
    }

    /**
     * Handle player quit - clean up tracking data.
     */
    public void handleQuit(UUID uuid) {
        lastActivity.remove(uuid);
        afkPlayers.remove(uuid);
    }

    /**
     * Periodic check for idle players. Auto-sets AFK after configured timeout,
     * and kicks players who exceed the kick timeout (if configured).
     */
    private void checkIdlePlayers() {
        int autoAfkSeconds = plugin.getConfig().getInt("afk.auto-afk-seconds", 300);
        int kickAfterSeconds = plugin.getConfig().getInt("afk.kick-after-seconds", 0);
        if (autoAfkSeconds <= 0) return;

        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            long last = lastActivity.getOrDefault(uuid, now);
            long idleSeconds = (now - last) / 1000;

            if (!isAfk(uuid) && idleSeconds >= autoAfkSeconds) {
                setAfk(player, true);
            }

            if (isAfk(uuid) && kickAfterSeconds > 0 && idleSeconds >= kickAfterSeconds) {
                String bypassPerm = plugin.getConfig().getString("afk.kick-bypass-permission", "justplugin.afk.kickbypass");
                if (!player.hasPermission(bypassPerm)) {
                    player.kick(plugin.getMessageManager().translate("player.afk.kicked"));
                }
            }
        }
    }
}
