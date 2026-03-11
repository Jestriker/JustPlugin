package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClockCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        // IRL time
        LocalTime realTime = LocalTime.now();
        String irlTime = realTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // Game time
        long gameTicks = player.getWorld().getTime();
        long gameHours = (gameTicks / 1000 + 6) % 24;
        long gameMinutes = (gameTicks % 1000) * 60 / 1000;
        String gameTime = String.format("%02d:%02d", gameHours, gameMinutes);
        boolean isDay = gameTicks < 12000 || gameTicks >= 23000;
        player.sendMessage("§a⏰ IRL Time: §e" + irlTime);
        player.sendMessage("§a🌍 Game Time: §e" + gameTime + " §7(" + (isDay ? "§6Day" : "§9Night") + "§7)");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
