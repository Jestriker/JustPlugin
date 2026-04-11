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
import java.util.UUID;
import java.util.stream.Collectors;

public class TempBanCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TempBanCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.tempban.usage"));
            sender.sendMessage(plugin.getMessageManager().info("moderation.tempban.duration-hint"));
            return true;
        }

        String bannedBy = sender instanceof Player ? sender.getName() : "Console";
        long duration = TimeUtil.parseDuration(args[1]);
        if (duration <= 0) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.tempban.invalid-duration"));
            return true;
        }

        String reason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : plugin.getConfig().getString("default-reasons.tempban", "Temporarily banned");

        UUID uuid;
        String name;
        try {
            uuid = UUID.fromString(args[0]);
            OfflinePlayer offP = Bukkit.getOfflinePlayer(uuid);
            name = offP.getName() != null ? offP.getName() : args[0];
        } catch (IllegalArgumentException e) {
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            uuid = offP.getUniqueId();
            name = args[0];
        }

        if (plugin.getBanManager().isBanned(uuid)) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.tempban.already-banned", "{player}", name));
            return true;
        }

        plugin.getBanManager().tempBan(uuid, name, reason, bannedBy, duration);
        sender.sendMessage(plugin.getMessageManager().success("moderation.tempban.success", "{player}", name, "{duration}", TimeUtil.formatDuration(duration), "{reason}", reason));

        // Configurable announcement
        net.kyori.adventure.text.Component announcement = plugin.getMessageManager().warning("moderation.tempban.announce", "{player}", name, "{staff}", bannedBy, "{duration}", TimeUtil.formatDuration(duration), "{reason}", reason);
        if (plugin.getConfig().getBoolean("punishment-announcements.tempban", false)) {
            Bukkit.broadcast(announcement);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.announce.tempban")) {
                    p.sendMessage(announcement);
                }
            }
        }
        plugin.getLogManager().log("moderation", "<yellow>" + bannedBy + "</yellow> temp-banned <yellow>" + name + "</yellow> for <yellow>" + TimeUtil.formatDuration(duration) + "</yellow>. Reason: <gray>" + reason);
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

