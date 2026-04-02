package org.justme.justPlugin.commands.moderation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

public class DelJailCommand implements TabExecutor {

    private final JustPlugin plugin;

    public DelJailCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.deljail.usage")));
            return true;
        }

        String name = args[0].toLowerCase();

        if (!plugin.getJailManager().deleteJailLocation(name)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.deljail.not-found",
                    "{name}", name)));
            return true;
        }

        sender.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.deljail.success",
                "{name}", name)));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getJailManager().getJailNames().stream()
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
