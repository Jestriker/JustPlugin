package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class MotdCommand implements TabExecutor {

    private final JustPlugin plugin;

    public MotdCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1 && sender.hasPermission("justplugin.motd.set")) {
            String motd = String.join(" ", args);
            plugin.getConfig().set("motd", motd);
            plugin.saveConfig();
            sender.sendMessage(CC.success("MOTD updated!"));
            sender.sendMessage(CC.translate(motd));
            return true;
        }
        String motd = plugin.getConfig().getString("motd", "<yellow>Welcome to the server!");
        sender.sendMessage(CC.translate(motd.replace("{player}", sender.getName())));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

