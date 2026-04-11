package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class ReloadScoreboardCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ReloadScoreboardCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("justplugin.scoreboard.reload")) {
            sender.sendMessage(plugin.getMessageManager().error("general.no-permission"));
            return true;
        }

        plugin.getScoreboardManager().reload();
        sender.sendMessage(plugin.getMessageManager().success("misc.reloadscoreboard.reloaded"));
        plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> reloaded the scoreboard configuration");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

