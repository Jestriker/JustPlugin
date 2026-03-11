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

public class TimeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.time")) {
            sender.sendMessage("§cYou don't have permission to change time.");
            return true;
        }
        World world = sender instanceof Player p ? p.getWorld() : org.bukkit.Bukkit.getWorlds().get(0);
        if (args.length == 0) {
            long time = world.getTime();
            sender.sendMessage("§aCurrent time: §e" + time + " §7(day=" + (time < 12000 ? "day" : "night") + ")");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "day" -> { world.setTime(1000); sender.sendMessage("§aTime set to §eday§a."); }
            case "noon" -> { world.setTime(6000); sender.sendMessage("§aTime set to §enoon§a."); }
            case "night", "midnight" -> { world.setTime(18000); sender.sendMessage("§aTime set to §enight§a."); }
            case "sunset", "dusk" -> { world.setTime(12000); sender.sendMessage("§aTime set to §esunset§a."); }
            case "sunrise", "dawn" -> { world.setTime(23000); sender.sendMessage("§aTime set to §esunrise§a."); }
            case "set" -> {
                if (args.length < 2) { sender.sendMessage("§cUsage: /time set <ticks>"); return true; }
                try { world.setTime(Long.parseLong(args[1])); sender.sendMessage("§aTime set to §e" + args[1] + "§a."); }
                catch (NumberFormatException e) { sender.sendMessage("§cInvalid time value."); }
            }
            case "add" -> {
                if (args.length < 2) { sender.sendMessage("§cUsage: /time add <ticks>"); return true; }
                try { world.setTime(world.getTime() + Long.parseLong(args[1])); sender.sendMessage("§aAdded §e" + args[1] + " §aticks to time."); }
                catch (NumberFormatException e) { sender.sendMessage("§cInvalid value."); }
            }
            default -> {
                try { world.setTime(Long.parseLong(args[0])); sender.sendMessage("§aTime set to §e" + args[0] + "§a."); }
                catch (NumberFormatException e) { sender.sendMessage("§cUsage: /time <day|night|noon|sunset|sunrise|set|add> [value]"); }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("day", "night", "noon", "sunset", "sunrise", "set", "add").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
