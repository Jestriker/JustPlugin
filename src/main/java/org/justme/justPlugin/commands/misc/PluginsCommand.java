package org.justme.justPlugin.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@SuppressWarnings("NullableProblems")
public class PluginsCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        String pluginList = Arrays.stream(plugins)
                .map(p -> (p.isEnabled() ? "<green>" : "<red>") + p.getName())
                .collect(Collectors.joining("<gray>, "));
        sender.sendMessage(CC.prefixed("<gray>Plugins (" + plugins.length + "): " + pluginList));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

