package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

/**
 * Manages custom join/leave messages with support for multiple modes:
 * "none", "all", "staff-only", "op-only", "group-based".
 * <p>
 * Supports MiniMessage formatting and placeholders: {player}, {online}, {max}.
 * Includes first-join detection and per-group messages via LuckPerms.
 */
public class JoinLeaveManager {

    private final JustPlugin plugin;

    public JoinLeaveManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the configured mode from config.yml.
     */
    public String getMode() {
        return plugin.getConfig().getString("join-leave.mode", "all").toLowerCase();
    }

    /**
     * Handles a player join - sends the appropriate custom message.
     *
     * @param player the joining player
     * @param firstJoin true if the player has never played before
     */
    public void handleJoin(Player player, boolean firstJoin) {
        String mode = getMode();
        if ("none".equals(mode)) return;

        String format = resolveJoinFormat(player, firstJoin, mode);
        if (format == null || format.isEmpty()) return;

        Component message = CC.translate(applyPlaceholders(format, player));
        broadcastByMode(message, mode);
    }

    /**
     * Handles a player quit - sends the appropriate custom message.
     *
     * @param player the leaving player
     */
    public void handleQuit(Player player) {
        String mode = getMode();
        if ("none".equals(mode)) return;

        String format = resolveLeaveFormat(player, mode);
        if (format == null || format.isEmpty()) return;

        Component message = CC.translate(applyPlaceholders(format, player));
        broadcastByMode(message, mode);
    }

    // ==================== Format Resolution ====================

    private String resolveJoinFormat(Player player, boolean firstJoin, String mode) {
        // First-join message takes priority (unless group-based has one)
        if (firstJoin) {
            String firstJoinMsg = plugin.getConfig().getString("join-leave.first-join-message", "");
            if (firstJoinMsg != null && !firstJoinMsg.isEmpty()) {
                return firstJoinMsg;
            }
        }

        if ("group-based".equals(mode)) {
            String groupFormat = getGroupFormat(player, "join");
            if (groupFormat != null) return groupFormat;
        }

        return plugin.getConfig().getString("join-leave.join-message",
                "<green>+</green> <yellow>{player}</yellow> <gray>joined the server.");
    }

    private String resolveLeaveFormat(Player player, String mode) {
        if ("group-based".equals(mode)) {
            String groupFormat = getGroupFormat(player, "leave");
            if (groupFormat != null) return groupFormat;
        }

        return plugin.getConfig().getString("join-leave.leave-message",
                "<red>-</red> <yellow>{player}</yellow> <gray>left the server.");
    }

    /**
     * Looks up a group-specific message format using LuckPerms primary group.
     */
    private String getGroupFormat(Player player, String type) {
        if (!plugin.isLuckPermsAvailable()) return null;

        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) return null;

            String group = user.getPrimaryGroup();
            String path = "join-leave.group-messages." + group + "." + type;
            return plugin.getConfig().getString(path);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Broadcasting ====================

    private void broadcastByMode(Component message, String mode) {
        switch (mode) {
            case "all", "group-based" -> {
                // Send to all online players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(message);
                }
                Bukkit.getConsoleSender().sendMessage(message);
            }
            case "staff-only" -> {
                // Only announce for players in configured staff groups
                List<String> staffGroups = plugin.getConfig().getStringList("staff-groups");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isInStaffGroup(p, staffGroups) || p.hasPermission("justplugin.staff")) {
                        p.sendMessage(message);
                    }
                }
                Bukkit.getConsoleSender().sendMessage(message);
            }
            case "op-only" -> {
                // Only OPs and console see join/leave messages
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.isOp()) {
                        p.sendMessage(message);
                    }
                }
                Bukkit.getConsoleSender().sendMessage(message);
            }
        }
    }

    /**
     * Checks whether a player belongs to any of the configured staff groups
     * using LuckPerms.
     */
    private boolean isInStaffGroup(Player player, List<String> staffGroups) {
        if (!plugin.isLuckPermsAvailable() || staffGroups.isEmpty()) return false;

        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) return false;

            String primary = user.getPrimaryGroup();
            if (staffGroups.contains(primary)) return true;

            // Also check inherited groups
            for (net.luckperms.api.node.Node node : user.getNodes()) {
                if (node instanceof net.luckperms.api.node.types.InheritanceNode inheritNode) {
                    if (staffGroups.contains(inheritNode.getGroupName())) return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    // ==================== Placeholders ====================

    private String applyPlaceholders(String format, Player player) {
        return format
                .replace("{player}", player.getName())
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
    }
}
