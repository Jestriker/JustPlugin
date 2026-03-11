package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.speed")) {
            sender.sendMessage("§cYou don't have permission to change speed.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /speed <0-10> [walk|fly] [player]");
            return true;
        }
        float speed;
        try {
            float raw = Float.parseFloat(args[0]);
            if (raw < 0 || raw > 10) { sender.sendMessage("§cSpeed must be between 0 and 10."); return true; }
            speed = raw / 10f;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid speed value.");
            return true;
        }
        boolean flyMode = args.length >= 2 && args[1].equalsIgnoreCase("fly");
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
        } else if (args.length >= 2 && !args[1].equalsIgnoreCase("fly") && !args[1].equalsIgnoreCase("walk")) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null && sender instanceof Player p) target = p;
        } else {
            target = sender instanceof Player p ? p : null;
        }
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        if (flyMode) {
            target.setFlySpeed(speed);
            sender.sendMessage("§aSet §e" + target.getName() + "§a's fly speed to §e" + args[0] + "§a.");
        } else {
            target.setWalkSpeed(speed);
            sender.sendMessage("§aSet §e" + target.getName() + "§a's walk speed to §e" + args[0] + "§a.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) return Arrays.asList("walk", "fly").stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        if (args.length == 3) return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        return new ArrayList<>();
    }
}
