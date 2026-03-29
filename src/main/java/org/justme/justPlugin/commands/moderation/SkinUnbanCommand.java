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
            sender.sendMessage(CC.error("Usage: <yellow>/skinunban <name>"));
            return true;
        }

        String name = args[0].toLowerCase();
        if (plugin.getSkinManager().unbanSkin(name)) {
            sender.sendMessage(CC.success("Unbanned skin name <yellow>" + name + "</yellow>. Players can now use this skin."));
            plugin.getLogManager().log("admin",
                    "<yellow>" + sender.getName() + "</yellow> <gray>unbanned skin name <white>" + name + "</white>.");
        } else {
            sender.sendMessage(CC.error("Skin name <yellow>" + name + "</yellow> is not banned."));
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

