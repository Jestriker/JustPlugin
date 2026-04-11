package org.justme.justPlugin.commands.moderation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /skinban [name] - Ban a skin name from being used, or list all banned skins.
 */
public class SkinBanCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SkinBanCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
            var banned = plugin.getSkinManager().getBannedSkins();
            if (banned.isEmpty()) {
                sender.sendMessage(plugin.getMessageManager().info("moderation.skinban.list-empty"));
            } else {
                sender.sendMessage(plugin.getMessageManager().prefixed("moderation.skinban.list-header", "{count}", String.valueOf(banned.size())));
                for (String name : banned) {
                    sender.sendMessage(plugin.getMessageManager().line("moderation.skinban.list-entry", "{name}", name));
                }
            }
            sender.sendMessage(plugin.getMessageManager().line("moderation.skinban.list-usage"));
            return true;
        }

        String name = args[0].toLowerCase();
        if (plugin.getSkinManager().banSkin(name)) {
            sender.sendMessage(plugin.getMessageManager().success("moderation.skinban.success", "{name}", name));
            plugin.getLogManager().log("admin",
                    "<yellow>" + sender.getName() + "</yellow> <gray>banned skin name <white>" + name + "</white>.");
        } else {
            sender.sendMessage(plugin.getMessageManager().warning("moderation.skinban.already-banned", "{name}", name));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(List.of("list"));
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

