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
            sender.sendMessage(CC.error("Usage: /tempban <player> <duration> [reason]"));
            sender.sendMessage(CC.info("Duration format: 1d2h30m (days, hours, minutes, seconds)"));
            return true;
        }

        String bannedBy = sender instanceof Player ? sender.getName() : "Console";
        long duration = TimeUtil.parseDuration(args[1]);
        if (duration <= 0) {
            sender.sendMessage(CC.error("Invalid duration! Example: 1d2h30m"));
            return true;
        }

        String reason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "Temporarily banned";

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
            sender.sendMessage(CC.error("<yellow>" + name + "</yellow> is already banned!"));
            return true;
        }

        plugin.getBanManager().tempBan(uuid, name, reason, bannedBy, duration);
        sender.sendMessage(CC.success("Temporarily banned <yellow>" + name + "</yellow> for <yellow>" + TimeUtil.formatDuration(duration) + "</yellow>. Reason: <gray>" + reason));
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

