package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.List;

/**
 * /rank - opens the Ranks management GUI.
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
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        // Check if ranks system is enabled
        if (!plugin.getCommandSettings().isEnabled("rank")) {
            player.sendMessage(plugin.getMessageManager().error("misc.rank.disabled-msg"));
            return true;
        }

        // Check if LuckPerms is available
        if (!plugin.isLuckPermsAvailable()) {
            player.sendMessage(plugin.getMessageManager().error("misc.rank.requires-luckperms"));
            player.sendMessage(plugin.getMessageManager().info("misc.rank.install-hint"));
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


