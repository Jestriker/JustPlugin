package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class TempBanIpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TempBanIpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.error("Usage: /tempbanip <ip | player> <duration> [reason]"));
            return true;
        }

        String bannedBy = sender instanceof Player ? sender.getName() : "Console";
        long duration = TimeUtil.parseDuration(args[1]);
        if (duration <= 0) {
            sender.sendMessage(CC.error("Invalid duration!"));
            return true;
        }

        String reason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : plugin.getConfig().getString("default-reasons.tempban", "Temporarily IP banned");
        String ip = args[0];
        String resolvedFrom = null;

        // Check if it's a player name rather than a raw IP
        if (!ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.getAddress() != null) {
                ip = target.getAddress().getAddress().getHostAddress();
                resolvedFrom = target.getName();
            } else {
                OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
                String lastIp = plugin.getBanManager().getLastIp(offP.getUniqueId());
                if (lastIp != null) {
                    ip = lastIp;
                    resolvedFrom = offP.getName() != null ? offP.getName() : args[0];
                } else {
                    sender.sendMessage(CC.error("Could not find an IP for <yellow>" + args[0] + "</yellow>. They may have never joined."));
                    return true;
                }
            }
        }

        // Check if already IP banned
        if (plugin.getBanManager().isIpBanned(ip)) {
            sender.sendMessage(CC.error("IP <yellow>" + ip + "</yellow> is already IP banned!"));
            return true;
        }

        plugin.getBanManager().tempBanIp(ip, reason, bannedBy, duration);
        if (resolvedFrom != null) {
            sender.sendMessage(CC.success("Temporarily IP banned <yellow>" + ip + "</yellow> (resolved from <yellow>" + resolvedFrom + "</yellow>) for <yellow>" + TimeUtil.formatDuration(duration) + "</yellow>."));
        } else {
            sender.sendMessage(CC.success("Temporarily IP banned <yellow>" + ip + "</yellow> for <yellow>" + TimeUtil.formatDuration(duration) + "</yellow>."));
        }

        // Configurable announcement
        net.kyori.adventure.text.Component announcement = CC.warning("IP <yellow>" + ip + "</yellow> has been temporarily IP banned by <yellow>" + bannedBy + "</yellow> for <yellow>" + TimeUtil.formatDuration(duration) + "</yellow>.");
        if (plugin.getConfig().getBoolean("punishment-announcements.tempbanip", false)) {
            Bukkit.broadcast(announcement);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.announce.tempbanip")) {
                    p.sendMessage(announcement);
                }
            }
        }
        plugin.getLogManager().log("moderation", "<yellow>" + bannedBy + "</yellow> temp-IP-banned <yellow>" + ip + "</yellow>" + (resolvedFrom != null ? " (from <yellow>" + resolvedFrom + "</yellow>)" : "") + " for <yellow>" + TimeUtil.formatDuration(duration) + "</yellow>. Reason: <gray>" + reason);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) return List.of("1h", "1d", "7d", "30d");
        return List.of();
    }
}

