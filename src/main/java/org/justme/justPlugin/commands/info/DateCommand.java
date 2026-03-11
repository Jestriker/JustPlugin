package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.List;

public class DateCommand implements TabExecutor {

    private final JustPlugin plugin;

    public DateCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String timezone = plugin.getConfig().getString("timezone", "UTC");
        String realDate = TimeUtil.getRealDate(timezone);
        String realTime = TimeUtil.getRealTime(timezone);
        sender.sendMessage(CC.info("<gold>Date & Time:"));
        sender.sendMessage(CC.line("IRL Date: <yellow>" + realDate));
        sender.sendMessage(CC.line("IRL Time: <yellow>" + realTime + " (" + timezone + ")"));
        if (sender instanceof Player player) {
            long ticks = player.getWorld().getTime();
            long fullTime = player.getWorld().getFullTime();
            long days = fullTime / 24000;
            String gameTime = TimeUtil.getGameTime(ticks);
            sender.sendMessage(CC.line("Game Day: <yellow>" + days));
            sender.sendMessage(CC.line("Game Time: <yellow>" + gameTime));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

