package org.justme.justPlugin.commands.moderation;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.JailManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Set;

public class JailListCommand implements TabExecutor {

    private final JustPlugin plugin;

    public JailListCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        JailManager jailManager = plugin.getJailManager();
        Set<String> names = jailManager.getJailNames();

        if (names.isEmpty()) {
            sender.sendMessage(CC.info(plugin.getMessageManager().raw("moderation.jails.empty")));
            return true;
        }

        sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jails.header")));

        for (String name : names) {
            Location loc = jailManager.getJailLocation(name);
            if (loc == null || loc.getWorld() == null) continue;
            sender.sendMessage(CC.translate(plugin.getMessageManager().raw("moderation.jails.entry",
                    "{name}", name,
                    "{world}", loc.getWorld().getName(),
                    "{x}", String.valueOf(loc.getBlockX()),
                    "{y}", String.valueOf(loc.getBlockY()),
                    "{z}", String.valueOf(loc.getBlockZ())
            )));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
