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

public class TempMuteCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TempMuteCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.tempmute.usage"));
            return true;
        }

        long durationMs = TimeUtil.parseDuration(args[1]);
        if (durationMs <= 0) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.tempmute.invalid-duration"));
            return true;
        }

        String defaultReason = plugin.getConfig().getString("default-reasons.tempmute", "Temporarily muted");
        String reason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : defaultReason;
        String mutedBy = sender instanceof Player ? sender.getName() : "Console";

        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[0];

        if (plugin.getMuteManager().isMuted(uuid)) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.tempmute.already-muted", "{player}", name));
            return true;
        }

        plugin.getMuteManager().tempMute(uuid, name, reason, mutedBy, durationMs);
        sender.sendMessage(plugin.getMessageManager().success("moderation.tempmute.success", "{player}", name, "{duration}", TimeUtil.formatDuration(durationMs)));
        sender.sendMessage(plugin.getMessageManager().line("moderation.tempmute.success-reason", "{reason}", reason));

        // Configurable announcement
        net.kyori.adventure.text.Component announcement = plugin.getMessageManager().warning("moderation.tempmute.announce", "{player}", name, "{staff}", mutedBy, "{duration}", TimeUtil.formatDuration(durationMs), "{reason}", reason);
        if (plugin.getConfig().getBoolean("punishment-announcements.tempmute", false)) {
            Bukkit.broadcast(announcement);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.announce.tempmute")) {
                    p.sendMessage(announcement);
                }
            }
        }
        plugin.getLogManager().log("mute", "<yellow>" + mutedBy + "</yellow> temp-muted <yellow>" + name + "</yellow> for <yellow>" + TimeUtil.formatDuration(durationMs) + "</yellow>. Reason: <gray>" + reason);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return List.of("5m", "15m", "30m", "1h", "6h", "1d", "7d", "30d");
        }
        return List.of();
    }
}

