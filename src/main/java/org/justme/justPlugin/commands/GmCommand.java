package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GmCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.gamemode")) {
            sender.sendMessage("§cYou don't have permission to change game modes.");
            return true;
        }
        GameMode mode = resolveMode(label, args);
        if (mode == null) {
            sender.sendMessage("§cUsage: /gm <survival|creative|adventure|spectator> [player]");
            return true;
        }
        Player target;
        int playerArgIndex = modeFromLabel(label) != null ? 0 : 1;
        if (args.length > playerArgIndex) {
            target = Bukkit.getPlayer(args[playerArgIndex]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cPlayer §e" + args[playerArgIndex] + " §cis not online.");
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) { sender.sendMessage("§cSpecify a player."); return true; }
            target = p;
        }
        target.setGameMode(mode);
        sender.sendMessage("§aSet §e" + target.getName() + "§a's gamemode to §e" + mode.name() + "§a.");
        if (!target.equals(sender)) target.sendMessage("§aYour gamemode was set to §e" + mode.name() + "§a.");
        return true;
    }

    private GameMode modeFromLabel(String label) {
        return switch (label.toLowerCase()) {
            case "gms" -> GameMode.SURVIVAL;
            case "gmc" -> GameMode.CREATIVE;
            case "gmsp" -> GameMode.SPECTATOR;
            case "gma" -> GameMode.ADVENTURE;
            default -> null;
        };
    }

    private GameMode resolveMode(String label, String[] args) {
        GameMode fromLabel = modeFromLabel(label);
        if (fromLabel != null) return fromLabel;
        if (args.length < 1) return null;
        return switch (args[0].toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (modeFromLabel(alias) != null) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else {
            if (args.length == 1) {
                return Arrays.asList("survival", "creative", "adventure", "spectator").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
