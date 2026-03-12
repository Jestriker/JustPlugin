package org.justme.justPlugin.commands.world;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class FreezeGameCommand implements TabExecutor {

    private final JustPlugin plugin;

    public FreezeGameCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
        sender.sendMessage(CC.success("Game has been <yellow>frozen</yellow>! All tick processing is paused."));
        Bukkit.broadcast(CC.warning("The game has been <red>frozen</red> by <yellow>" + sender.getName() + "</yellow>."));
        plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> froze the game");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
