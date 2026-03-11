package org.justme.justPlugin.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WeatherCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.weather")) {
            sender.sendMessage("§cYou don't have permission to change weather.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /weather <sun|rain|thunder>");
            return true;
        }
        World world = sender instanceof Player p ? p.getWorld() : org.bukkit.Bukkit.getWorlds().get(0);
        switch (args[0].toLowerCase()) {
            case "sun", "clear", "sunny" -> {
                world.setStorm(false);
                world.setThundering(false);
                sender.sendMessage("§aWeather set to §esunny§a.");
            }
            case "rain", "rainy", "storm" -> {
                world.setStorm(true);
                world.setThundering(false);
                sender.sendMessage("§aWeather set to §erainy§a.");
            }
            case "thunder", "thunderstorm" -> {
                world.setStorm(true);
                world.setThundering(true);
                sender.sendMessage("§aWeather set to §ethunderstorm§a.");
            }
            default -> sender.sendMessage("§cUnknown weather: §e" + args[0] + "§c. Use: sun, rain, thunder");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("sun", "rain", "thunder").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
