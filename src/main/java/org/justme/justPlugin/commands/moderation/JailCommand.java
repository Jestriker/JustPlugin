package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.JailManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JailCommand implements TabExecutor {

    private final JustPlugin plugin;

    public JailCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jail.usage")));
            return true;
        }

        JailManager jailManager = plugin.getJailManager();

        // Check if any jail locations exist
        if (jailManager.getJailNames().isEmpty()) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jail.no-jails")));
            return true;
        }

        // Resolve target player (must be online)
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(CC.error("Player not found or not online."));
            return true;
        }

        // Check if already jailed
        if (jailManager.isJailed(target.getUniqueId())) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jail.already-jailed",
                    "{player}", target.getName())));
            return true;
        }

        String staff = sender instanceof Player ? sender.getName() : "Console";
        String defaultReason = plugin.getConfig().getString("jail.default-reason", "Jailed by an operator");
        long duration = -1L; // permanent by default
        String reason = defaultReason;

        // Parse optional duration and reason
        if (args.length >= 2) {
            // Try to parse second arg as duration
            long parsedDuration = TimeUtil.parseDuration(args[1]);
            if (parsedDuration > 0) {
                duration = parsedDuration;
                // Rest is reason
                if (args.length >= 3) {
                    reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                }
            } else {
                // No duration - everything after player name is reason
                reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
        }

        // Use the default (first) jail location
        String jailName = jailManager.getDefaultJailName();

        jailManager.jail(target, jailName, duration, reason, staff);

        // Send success message
        if (duration == -1L) {
            sender.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.jail.success",
                    "{player}", target.getName(), "{jail}", jailName, "{reason}", reason)));
        } else {
            sender.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.jail.success-duration",
                    "{player}", target.getName(), "{duration}", TimeUtil.formatDuration(duration), "{reason}", reason)));
        }

        // Announce to staff
        net.kyori.adventure.text.Component announcement = CC.translate(
                plugin.getMessageManager().raw("moderation.jail.announce",
                        "{player}", target.getName(), "{staff}", staff, "{reason}", reason));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("justplugin.announce.jail")) {
                p.sendMessage(announcement);
            }
        }

        // Log
        plugin.getLogManager().log("jail", "<yellow>" + staff + "</yellow> jailed <yellow>" + target.getName()
                + "</yellow>" + (duration != -1L ? " for " + TimeUtil.formatDuration(duration) : " permanently")
                + ". Reason: <gray>" + reason);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return List.of("1h", "1d", "7d", "30d", "permanent");
        }
        return List.of();
    }
}
