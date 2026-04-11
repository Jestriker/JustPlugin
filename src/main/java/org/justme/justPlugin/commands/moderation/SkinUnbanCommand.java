package org.justme.justPlugin.commands.moderation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /skinunban <name> - Unban a skin name so players can use it again.
 */
public class SkinUnbanCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SkinUnbanCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.skinunban.usage"));
            return true;
        }

        String name = args[0].toLowerCase();
        if (plugin.getSkinManager().unbanSkin(name)) {
            sender.sendMessage(plugin.getMessageManager().success("moderation.skinunban.success", "{name}", name));
            plugin.getLogManager().log("admin",
                    "<yellow>" + sender.getName() + "</yellow> <gray>unbanned skin name <white>" + name + "</white>.");
        } else {
            sender.sendMessage(plugin.getMessageManager().error("moderation.skinunban.not-banned", "{name}", name));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return plugin.getSkinManager().getBannedSkins().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

