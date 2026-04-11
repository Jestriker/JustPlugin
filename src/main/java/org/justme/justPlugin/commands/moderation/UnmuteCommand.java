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

public class UnmuteCommand implements TabExecutor {

    private final JustPlugin plugin;

    public UnmuteCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.unmute.usage")));
            return true;
        }

        String executedBy = sender instanceof Player ? sender.getName() : "Console";

        // Try UUID
        UUID uuid = null;
        String name = args[0];
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            uuid = offP.getUniqueId();
            name = offP.getName() != null ? offP.getName() : args[0];
        }

        if (!plugin.getMuteManager().isMuted(uuid)) {
            // Try by name
            if (!plugin.getMuteManager().isMutedByName(name)) {
                sender.sendMessage(plugin.getMessageManager().error("moderation.unmute.not-muted",
                        "{player}", name));
                return true;
            }
            plugin.getMuteManager().unmuteByName(name);
        } else {
            plugin.getMuteManager().unmute(uuid);
        }

        sender.sendMessage(plugin.getMessageManager().success("moderation.unmute.success",
                "{player}", name));
        plugin.getLogManager().log("mute", "<yellow>" + executedBy + "</yellow> unmuted <yellow>" + name + "</yellow>");
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

