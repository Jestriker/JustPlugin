package org.justme.justPlugin.commands.world;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /clearentities - Manually trigger an entity clear.
 */
public class ClearEntitiesCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ClearEntitiesCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String executedBy = sender instanceof Player ? sender.getName() : "Console";

        var result = plugin.getEntityClearManager().clearNow();

        sender.sendMessage(CC.success("Entity clear complete!"));
        sender.sendMessage(CC.line("Items removed: <yellow>" + result.itemsRemoved()));
        sender.sendMessage(CC.line("Mobs removed: <yellow>" + result.mobsRemoved()));
        sender.sendMessage(CC.line("Total: <yellow>" + result.total()));

        plugin.getLogManager().log("admin", "<yellow>" + executedBy + "</yellow> manually triggered entity clear. Removed <yellow>" + result.total() + "</yellow> entities.");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

