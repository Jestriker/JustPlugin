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
            sender.sendMessage(CC.error("Usage: /tempmute <player> <duration> [reason]"));
            return true;
        }

        long durationMs = TimeUtil.parseDuration(args[1]);
        if (durationMs <= 0) {
            sender.sendMessage(CC.error("Invalid duration! Examples: 5m, 1h, 1d, 1w"));
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
            sender.sendMessage(CC.error("<yellow>" + name + "</yellow> is already muted!"));
            return true;
        }

        plugin.getMuteManager().tempMute(uuid, name, reason, mutedBy, durationMs);
        sender.sendMessage(CC.success("Temporarily muted <yellow>" + name + "</yellow> for <yellow>" + TimeUtil.formatDuration(durationMs) + "</yellow>."));
        sender.sendMessage(CC.line("Reason: <white>" + reason));
        Bukkit.broadcast(CC.warning("<yellow>" + name + "</yellow> has been temporarily muted by <yellow>" + mutedBy + "</yellow> for <yellow>" + TimeUtil.formatDuration(durationMs) + "</yellow>. Reason: <gray>" + reason));
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

