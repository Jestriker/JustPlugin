package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;

public class UnbanCommand implements TabExecutor {

    private final JustPlugin plugin;

    public UnbanCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /unban <player | uuid>"));
            return true;
        }
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

        if (plugin.getBanManager().unban(uuid)) {
            sender.sendMessage(CC.success("<yellow>" + name + "</yellow> has been unbanned."));
        } else {
            sender.sendMessage(CC.error("<yellow>" + name + "</yellow> is not banned!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

