package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class ScoreboardCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ScoreboardCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // /scoreboard reload — requires reload permission
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("justplugin.scoreboard.reload")) {
                sender.sendMessage(CC.error("You don't have permission to reload the scoreboard."));
                return true;
            }
            plugin.getScoreboardManager().reload();
            sender.sendMessage(CC.success("Scoreboard configuration reloaded."));
            plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> reloaded the scoreboard configuration");
            return true;
        }

        // /scoreboard — toggle for player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can toggle the scoreboard. Use <yellow>/scoreboard reload</yellow> from console."));
            return true;
        }

        if (!plugin.getScoreboardManager().isEnabled()) {
            player.sendMessage(CC.error("The scoreboard system is currently disabled."));
            return true;
        }

        boolean nowVisible = plugin.getScoreboardManager().toggle(player);
        if (nowVisible) {
            player.sendMessage(CC.success("Scoreboard <green>enabled</green>."));
        } else {
            player.sendMessage(CC.success("Scoreboard <red>disabled</red>."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new java.util.ArrayList<>();
            if (sender.hasPermission("justplugin.scoreboard.reload")) {
                completions.add("reload");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

