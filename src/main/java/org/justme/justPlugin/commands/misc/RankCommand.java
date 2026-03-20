package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

/**
 * /rank — opens the Ranks management GUI.
 * Requires LuckPerms integration to be enabled in config.
 * If LuckPerms is not installed, players are notified.
 */
@SuppressWarnings("NullableProblems")
public class RankCommand implements TabExecutor {

    private final JustPlugin plugin;

    public RankCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        // Check if ranks system is enabled
        if (!plugin.getCommandSettings().isEnabled("rank")) {
            player.sendMessage(CC.error("The ranks system is currently disabled."));
            return true;
        }

        // Check if LuckPerms is available
        if (!plugin.isLuckPermsAvailable()) {
            player.sendMessage(CC.error("The ranks system requires <yellow>LuckPerms</yellow> to be installed on this server."));
            player.sendMessage(CC.info("Please ask a server administrator to install LuckPerms."));
            return true;
        }

        plugin.getRankGuiManager().open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}


