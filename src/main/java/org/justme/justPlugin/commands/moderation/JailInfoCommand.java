package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.JailManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JailInfoCommand implements TabExecutor {

    private final JustPlugin plugin;

    public JailInfoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jailinfo.usage")));
            return true;
        }

        // Resolve player
        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[0];

        JailManager jailManager = plugin.getJailManager();

        if (!jailManager.isJailed(uuid)) {
            sender.sendMessage(CC.info(plugin.getMessageManager().raw("moderation.jailinfo.not-jailed",
                    "{player}", name)));
            return true;
        }

        JailManager.JailEntry entry = jailManager.getJailInfo(uuid);
        if (entry == null) {
            sender.sendMessage(CC.info(plugin.getMessageManager().raw("moderation.jailinfo.not-jailed",
                    "{player}", name)));
            return true;
        }

        sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jailinfo.header",
                "{player}", name)));
        sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jailinfo.jail-name",
                "{jail}", entry.jailName)));
        sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jailinfo.reason",
                "{reason}", entry.reason)));
        sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jailinfo.jailed-by",
                "{staff}", entry.jailedBy)));

        if (entry.expiryTime == -1L) {
            sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jailinfo.permanent")));
        } else {
            long remaining = entry.getRemainingMs();
            sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jailinfo.time-remaining",
                    "{duration}", TimeUtil.formatDuration(remaining))));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Suggest jailed players + online players
            List<String> suggestions = plugin.getJailManager().getAllJailedPlayers().stream()
                    .map(e -> e.playerName)
                    .collect(Collectors.toList());
            Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(n -> {
                if (!suggestions.contains(n)) suggestions.add(n);
            });
            return suggestions.stream()
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
