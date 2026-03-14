package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class ResetMotdCommand implements TabExecutor {

    private static final String DEFAULT_MOTD = "<gradient:#00aaff:#00ffaa><bold>Welcome to the server, {player}!</bold></gradient>\n<gray>Type <yellow>/help</yellow> for commands.";

    private final JustPlugin plugin;

    public ResetMotdCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        plugin.getConfig().set("motd", DEFAULT_MOTD);
        plugin.saveConfig();
        sender.sendMessage(CC.success("MOTD has been reset to default."));
        sender.sendMessage(CC.translate(DEFAULT_MOTD.replace("{player}", sender.getName())));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

