package org.justme.justPlugin.commands.world;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class UnfreezeGameCommand implements TabExecutor {

    private final JustPlugin plugin;

    public UnfreezeGameCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
        sender.sendMessage(plugin.getMessageManager().success("world.unfreezegame.unfrozen"));
        Bukkit.broadcast(plugin.getMessageManager().warning("world.unfreezegame.broadcast",
                "{player}", sender.getName()));
        plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> unfroze the game");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
