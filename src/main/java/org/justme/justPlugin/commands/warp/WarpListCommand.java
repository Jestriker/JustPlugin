package org.justme.justPlugin.commands.warp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class WarpListCommand implements TabExecutor {

    private final JustPlugin plugin;

    public WarpListCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Set<String> names = plugin.getWarpManager().getWarpNames();
        if (names.isEmpty()) {
            sender.sendMessage(CC.info("No warps available."));
        } else {
            boolean clickable = plugin.getConfig().getBoolean("clickable-commands.warp-list", true);
            String warpList = names.stream()
                    .map(n -> CC.clickCmd("<yellow>" + n + "</yellow>", "/warp " + n, clickable))
                    .collect(Collectors.joining("<gray>, "));
            sender.sendMessage(CC.translate(CC.PREFIX + "<gold>Warps (" + names.size() + "):</gold> " + warpList));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

