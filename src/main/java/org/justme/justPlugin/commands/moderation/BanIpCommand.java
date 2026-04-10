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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BanIpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BanIpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /banip <ip | player> [reason]"));
            return true;
        }

        String reason = args.length >= 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : plugin.getConfig().getString("default-reasons.ban", "IP Banned by an operator");
        String bannedBy = sender instanceof Player ? sender.getName() : "Console";
        String ip = args[0];
        String resolvedFrom = null;
        UUID associatedUuid = null;
        String associatedName = null;

        // Check if it's a player name rather than a raw IP
        if (!ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") && !ip.contains(":")) {
            // Try online player first
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.getAddress() != null) {
                ip = target.getAddress().getAddress().getHostAddress();
                resolvedFrom = target.getName();
                associatedUuid = target.getUniqueId();
                associatedName = target.getName();
            } else {
                // Try offline player - look up last recorded IP
                @SuppressWarnings("deprecation")
                OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
                String lastIp = plugin.getBanManager().getLastIp(offP.getUniqueId());
                if (lastIp != null) {
                    ip = lastIp;
                    resolvedFrom = offP.getName() != null ? offP.getName() : args[0];
                    associatedUuid = offP.getUniqueId();
                    associatedName = resolvedFrom;
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

        plugin.getBanManager().banIp(ip, reason, bannedBy, associatedUuid, associatedName);
        if (resolvedFrom != null) {
            sender.sendMessage(CC.success("IP Banned <yellow>" + ip + "</yellow> (resolved from <yellow>" + resolvedFrom + "</yellow>). Reason: <gray>" + reason));
        } else {
            sender.sendMessage(CC.success("IP Banned <yellow>" + ip + "</yellow>. Reason: <gray>" + reason));
        }
        net.kyori.adventure.text.Component announcement = CC.warning("IP <yellow>" + ip + "</yellow> has been IP banned by <yellow>" + bannedBy + "</yellow>. Reason: <gray>" + reason);
        if (plugin.getConfig().getBoolean("punishment-announcements.banip", false)) {
            Bukkit.broadcast(announcement);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.announce.banip")) {
                    p.sendMessage(announcement);
                }
            }
        }
        plugin.getLogManager().log("moderation", "<yellow>" + bannedBy + "</yellow> IP-banned <yellow>" + ip + "</yellow>" + (resolvedFrom != null ? " (from <yellow>" + resolvedFrom + "</yellow>)" : "") + ". Reason: <gray>" + reason);
        return true;
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
