package org.justme.justPlugin.listeners.connection;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeleportManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;
import org.justme.justPlugin.util.PlaceholderResolver;

/**
 * Handles player login, join, and quit events.
 * Includes ban checks, maintenance mode, join MOTD, data loading/saving,
 * vanish handling, scoreboard display, and startup warning notifications.
 */
public class ConnectionListener implements Listener {

    private final JustPlugin plugin;

    public ConnectionListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String ip = event.getAddress().getHostAddress();

        // Check ban first
        Component kickMsg = plugin.getBanManager().checkJoinBan(
                player.getUniqueId(), player.getName(), ip);
        if (kickMsg != null) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMsg);
            return;
        }

        // Check maintenance mode - block non-whitelisted, non-bypass players
        if (plugin.getCommandSettings().isEnabled("maintenance")
                && plugin.getMaintenanceManager().isActive()
                && !plugin.getMaintenanceManager().canBypass(player)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    plugin.getMaintenanceManager().buildKickMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data into cache early so subsequent reads are fast
        plugin.getDataManager().loadPlayerDataToCache(player.getUniqueId());

        plugin.getEconomyManager().loadPlayer(player.getUniqueId());
        plugin.getTransactionManager().loadPlayer(player.getUniqueId());
        plugin.getIgnoreManager().loadPlayer(player.getUniqueId());
        plugin.getVanishManager().handleJoin(player);
        plugin.getAfkManager().handleJoin(player.getUniqueId());

        // Load nickname and tag data, apply display name
        if (plugin.getNickManager() != null) {
            plugin.getNickManager().loadPlayer(player.getUniqueId());
            plugin.getNickManager().applyDisplayName(player);
        }
        if (plugin.getTagManager() != null) {
            plugin.getTagManager().loadPlayer(player.getUniqueId());
        }

        // Restore persistent player states (fly, speed, god, vanish)
        plugin.getPlayerStateManager().loadState(player);

        // Save player's IP for offline IP-ban lookups
        if (player.getAddress() != null) {
            YamlConfiguration data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            data.set("last-ip", player.getAddress().getAddress().getHostAddress());
            data.set("last-name", player.getName());
            plugin.getDataManager().savePlayerData(player.getUniqueId(), data);
        }

        // Restore persistent back location
        plugin.getPlayerListener().loadBackLocation(player.getUniqueId());

        // Restore persistent death location
        plugin.getPlayerListener().loadDeathLocation(player.getUniqueId());

        // If joining player is vanished, suppress real join message
        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            event.joinMessage(null);
            plugin.getVanishManager().handleVanishedPlayerJoin(player);
        } else {
            // Suppress vanilla join message and send custom one via JoinLeaveManager
            event.joinMessage(null);
            plugin.getJoinLeaveManager().handleJoin(player, !player.hasPlayedBefore());
        }

        // Join MOTD (player join message)
        String joinMotd = plugin.getMotdManager().getJoinMotd();
        if (joinMotd != null && !joinMotd.isEmpty()) {
            String resolved = joinMotd
                    .replace("{player}", player.getName())
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            player.sendMessage(CC.translate(resolved));
        }

        // Track session playtime for scoreboard placeholders
        PlaceholderResolver.recordJoin(player.getUniqueId());

        // Apply stored skin override on join
        if (plugin.getSkinManager() != null && plugin.getSkinManager().hasSkinOverride(player.getUniqueId())) {
            plugin.getSkinManager().applyOnJoin(player);
        }

        // Show scoreboard (delayed slightly so the player is fully loaded)
        SchedulerUtil.runForEntityLater(plugin, player, () -> {
            if (player.isOnline()) {
                plugin.getScoreboardManager().show(player);
            }
        }, 10L);

        // Notify staff about startup dependency warnings (once per session)
        if (!plugin.getStartupWarnings().isEmpty()
                && (player.hasPermission("justplugin.admin"))
                && plugin.getWarnedStaff().add(player.getUniqueId())) {
            SchedulerUtil.runForEntityLater(plugin, player, () -> {
                if (!player.isOnline()) return;
                player.sendMessage(Component.empty());
                player.sendMessage(CC.translate("<gray>[<gradient:#ff6b6b:#ee5a24>JustPlugin</gradient><gray>] <yellow><bold>Startup Warnings:</bold></yellow>"));
                for (String warning : plugin.getStartupWarnings()) {
                    player.sendMessage(CC.translate("  <gray>- " + warning));
                }
                player.sendMessage(CC.translate("  <dark_gray>Check config.yml for details."));
                player.sendMessage(Component.empty());
            }, 40L);
        }

        // Notify about unread mail (delayed so it appears after other join messages)
        if (plugin.getCommandSettings().isEnabled("mail")
                && player.hasPermission("justplugin.mail")) {
            SchedulerUtil.runForEntityLater(plugin, player, () -> {
                if (!player.isOnline()) return;
                int unread = plugin.getMailManager().getUnreadCount(player.getUniqueId());
                if (unread > 0) {
                    player.sendMessage(CC.info(plugin.getMessageManager().raw("chat.mail.join-unread",
                            "{count}", String.valueOf(unread))));
                }
            }, 80L);
        }

        // Maintenance mode join warning - sent last so it's noticeable
        if (plugin.getCommandSettings().isEnabled("maintenance")
                && plugin.getMaintenanceManager().isActive()) {
            SchedulerUtil.runForEntityLater(plugin, player, () -> {
                if (!player.isOnline()) return;
                String warning = plugin.getMaintenanceManager().getJoinWarning();
                if (warning != null && !warning.isEmpty()) {
                    player.sendMessage(Component.empty());
                    player.sendMessage(CC.translate(warning));
                    String cd = plugin.getMaintenanceManager().getCooldownText();
                    if (cd != null) {
                        player.sendMessage(CC.line("Estimated time remaining: <yellow>" + cd));
                    }
                    player.sendMessage(Component.empty());
                }
            }, 60L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        java.util.UUID uuid = player.getUniqueId();

        // Clean up AFK state
        plugin.getAfkManager().handleQuit(uuid);

        // Save persistent player states before cleanup
        plugin.getPlayerStateManager().saveState(player);

        // Persist back and death locations
        plugin.getPlayerListener().saveBackLocation(uuid);
        plugin.getPlayerListener().saveDeathLocation(uuid);

        // Persist inventory snapshot for offline /invsee and /echestsee
        plugin.getPlayerListener().saveInventorySnapshot(player);

        // Save last location for offline commands
        org.bukkit.configuration.file.YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("last-location.world", player.getLocation().getWorld().getName());
        data.set("last-location.x", player.getLocation().getX());
        data.set("last-location.y", player.getLocation().getY());
        data.set("last-location.z", player.getLocation().getZ());
        data.set("last-location.yaw", player.getLocation().getYaw());
        data.set("last-location.pitch", player.getLocation().getPitch());
        // Save XP
        data.set("xp-level", player.getLevel());
        data.set("xp-progress", player.getExp());
        plugin.getDataManager().savePlayerData(uuid, data);

        plugin.getEconomyManager().unloadPlayer(uuid);
        plugin.getTransactionManager().unloadPlayer(uuid);
        plugin.getIgnoreManager().unloadPlayer(uuid);
        plugin.getChatManager().removePlayer(uuid);
        plugin.getScoreboardManager().handleQuit(uuid);
        PlaceholderResolver.recordQuit(uuid);
        plugin.getPlayerListener().removeGodMode(uuid);

        // Cancel TPA requests and notify counterpart
        TeleportManager tpManager = plugin.getTeleportManager();
        TeleportManager.TpaRequest outgoing = tpManager.getOutgoingRequest(uuid);
        if (outgoing != null) {
            Player other = Bukkit.getPlayer(outgoing.target);
            tpManager.cancelOutgoingRequest(uuid);
            if (other != null) {
                other.sendMessage(CC.warning("Teleport request from <yellow>" + player.getName() + "</yellow> was cancelled - they logged off."));
            }
        }
        TeleportManager.TpaRequest incoming = tpManager.getIncomingRequest(uuid);
        if (incoming != null) {
            Player sender = Bukkit.getPlayer(incoming.sender);
            tpManager.removeRequest(incoming.sender);
            if (sender != null) {
                sender.sendMessage(CC.warning("Your teleport request was cancelled - <yellow>" + player.getName() + "</yellow> logged off."));
            }
        }

        tpManager.cancelPendingTeleport(uuid);

        // If quitting player is vanished, suppress real quit message
        if (plugin.getVanishManager().isVanished(uuid)) {
            event.quitMessage(null);
        } else {
            // Suppress vanilla quit message and send custom one via JoinLeaveManager
            event.quitMessage(null);
            plugin.getJoinLeaveManager().handleQuit(player);
        }

        // Save and unload player data from cache
        plugin.getDataManager().unloadPlayerData(uuid);
    }
}

