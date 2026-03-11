package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class InfoCommand implements TabExecutor {

    private final JustPlugin plugin;

    public InfoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(CC.translate("<gradient:#00aaff:#00ffaa><bold>JustPlugin</bold></gradient>"));
        sender.sendMessage(CC.info("Version: <yellow>" + plugin.getDescription().getVersion()));
        sender.sendMessage(CC.info("Author: <yellow>JustMe"));
        sender.sendMessage(CC.info("An EssentialsX-like plugin for your server."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

