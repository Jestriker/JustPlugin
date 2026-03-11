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

public class ClockCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ClockCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String timezone = plugin.getConfig().getString("timezone", "UTC");
        String realTime = TimeUtil.getRealTime(timezone);
        sender.sendMessage(CC.info("<gold>Clock:"));
        sender.sendMessage(CC.line("IRL Time: <yellow>" + realTime + " (" + timezone + ")"));
        if (sender instanceof Player player) {
            String gameTime = TimeUtil.getGameTime(player.getWorld().getTime());
            sender.sendMessage(CC.line("Game Time: <yellow>" + gameTime + " <gray>(tick " + player.getWorld().getTime() + ")"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

