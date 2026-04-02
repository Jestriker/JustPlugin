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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnjailCommand implements TabExecutor {

    private final JustPlugin plugin;

    public UnjailCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.unjail.usage")));
            return true;
        }

        JailManager jailManager = plugin.getJailManager();

        // Resolve player
        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[0];

        if (!jailManager.isJailed(uuid)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.unjail.not-jailed",
                    "{player}", name)));
            return true;
        }

        String staff = sender instanceof Player ? sender.getName() : "Console";

        jailManager.unjail(uuid);

        sender.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.unjail.success",
                "{player}", name)));

        // Log
        plugin.getLogManager().log("unjail", "<yellow>" + staff + "</yellow> unjailed <yellow>" + name + "</yellow>.");

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Suggest jailed players
            return plugin.getJailManager().getAllJailedPlayers().stream()
                    .map(e -> e.playerName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
