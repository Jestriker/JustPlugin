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
                sender.sendMessage(CC.info("No skin names are currently banned."));
            } else {
                sender.sendMessage(CC.prefixed("<gray>Banned skins <dark_gray>(" + banned.size() + "):"));
                for (String name : banned) {
                    sender.sendMessage(CC.line("<white>" + name));
                }
            }
            sender.sendMessage(CC.line("<dark_gray>Usage: <yellow>/skinban <name></yellow> to ban a skin."));
            return true;
        }

        String name = args[0].toLowerCase();
        if (plugin.getSkinManager().banSkin(name)) {
            sender.sendMessage(CC.success("Banned skin name <yellow>" + name + "</yellow>. Players can no longer use this skin."));
            plugin.getLogManager().log("admin",
                    "<yellow>" + sender.getName() + "</yellow> <gray>banned skin name <white>" + name + "</white>.");
        } else {
            sender.sendMessage(CC.warning("Skin name <yellow>" + name + "</yellow> is already banned."));
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

