package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class InfoCommand implements TabExecutor {

    private final JustPlugin plugin;

    public InfoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(plugin.getMessageManager().info("info.jpinfo.header", "{version}", plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.getMessageManager().info("info.jpinfo.author"));
        sender.sendMessage(plugin.getMessageManager().info("info.jpinfo.description"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

