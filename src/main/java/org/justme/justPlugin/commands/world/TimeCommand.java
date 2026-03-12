package org.justme.justPlugin.commands.world;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TimeCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            long ticks = player.getWorld().getTime();
            player.sendMessage(CC.info("Current game time: <yellow>" + TimeUtil.getGameTime(ticks) + " <gray>(tick " + ticks + ")"));
            return true;
        }
        if (args.length < 2 && !args[0].equalsIgnoreCase("query")) {
            player.sendMessage(CC.error("Usage: /time <set | add> <value>"));
            player.sendMessage(CC.info("Values: day, night, noon, midnight, sunrise, sunset, or tick number"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                long ticks = parseTime(args[1]);
                if (ticks < 0) {
                    player.sendMessage(CC.error("Invalid time value!"));
                    return true;
                }
                player.getWorld().setTime(ticks);
                player.sendMessage(CC.success("Time set to <yellow>" + args[1] + "</yellow> (" + ticks + " ticks)."));
                plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> set time to <yellow>" + args[1] + "</yellow> (" + ticks + " ticks)");
            }
            case "add" -> {
                try {
                    long ticks = Long.parseLong(args[1]);
                    player.getWorld().setTime(player.getWorld().getTime() + ticks);
                    player.sendMessage(CC.success("Added <yellow>" + ticks + "</yellow> ticks to the time."));
                    plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> added <yellow>" + ticks + "</yellow> ticks to the time");
                } catch (NumberFormatException e) {
                    player.sendMessage(CC.error("Invalid tick amount!"));
                }
            }
            case "query" -> {
                long ticks = player.getWorld().getTime();
                player.sendMessage(CC.info("Current game time: <yellow>" + TimeUtil.getGameTime(ticks) + " <gray>(tick " + ticks + ")"));
            }
            default -> player.sendMessage(CC.error("Usage: /time <set | add | query> <value>"));
        }
        return true;
    }

    private long parseTime(String input) {
        return switch (input.toLowerCase()) {
            case "day" -> 1000;
            case "noon", "midday" -> 6000;
            case "sunset", "dusk" -> 12000;
            case "night" -> 13000;
            case "midnight" -> 18000;
            case "sunrise", "dawn" -> 23000;
            default -> {
                try {
                    yield Long.parseLong(input);
                } catch (NumberFormatException e) {
                    yield -1;
                }
            }
        };
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("set", "add", "query")
                    .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Stream.of("day", "night", "noon", "midnight", "sunrise", "sunset")
                    .filter(n -> n.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
