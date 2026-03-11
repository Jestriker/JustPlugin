package org.justme.justPlugin.commands.world;

import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeatherCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /weather <sun | rain | thunder>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "sun", "clear" -> {
                player.getWorld().setStorm(false);
                player.getWorld().setThundering(false);
                player.sendMessage(CC.success("Weather set to <yellow>clear</yellow>."));
            }
            case "rain", "storm" -> {
                player.getWorld().setStorm(true);
                player.getWorld().setThundering(false);
                player.sendMessage(CC.success("Weather set to <yellow>rain</yellow>."));
            }
            case "thunder", "thunderstorm" -> {
                player.getWorld().setStorm(true);
                player.getWorld().setThundering(true);
                player.sendMessage(CC.success("Weather set to <yellow>thunder</yellow>."));
            }
            default -> player.sendMessage(CC.error("Usage: /weather <sun | rain | thunder>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("sun", "rain", "thunder")
                    .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

