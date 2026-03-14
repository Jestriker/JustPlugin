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

public class BanCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BanCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /ban <player | uuid> [reason]"));
            return true;
        }

        String reason = args.length >= 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : plugin.getConfig().getString("default-reasons.ban", "Banned by an operator");
        String bannedBy = sender instanceof Player ? sender.getName() : "Console";

        // Try UUID first
        UUID uuid;
        String name;
        try {
            uuid = UUID.fromString(args[0]);
            OfflinePlayer offP = Bukkit.getOfflinePlayer(uuid);
            name = offP.getName() != null ? offP.getName() : args[0];
        } catch (IllegalArgumentException e) {
            // Try by name
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            uuid = offP.getUniqueId();
            name = args[0];
        }

        if (plugin.getBanManager().isBanned(uuid)) {
            sender.sendMessage(CC.error("<yellow>" + name + "</yellow> is already banned!"));
            return true;
        }

        plugin.getBanManager().ban(uuid, name, reason, bannedBy);
        sender.sendMessage(CC.success("Banned <yellow>" + name + "</yellow>. Reason: <gray>" + reason));
        Bukkit.broadcast(CC.warning("<yellow>" + name + "</yellow> has been banned by <yellow>" + bannedBy + "</yellow>. Reason: <gray>" + reason));
        plugin.getLogManager().log("moderation", "<yellow>" + bannedBy + "</yellow> banned <yellow>" + name + "</yellow>. Reason: <gray>" + reason);
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

