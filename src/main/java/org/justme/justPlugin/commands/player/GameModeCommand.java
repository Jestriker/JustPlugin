package org.justme.justPlugin.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class GameModeCommand implements TabExecutor {

    private final JustPlugin plugin;

    public GameModeCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        boolean isPlayer = sender instanceof Player;

        // Handle shortcut labels: /gmc, /gms, /gma, /gmsp
        GameMode mode = switch (label.toLowerCase()) {
            case "gmc" -> GameMode.CREATIVE;
            case "gms" -> GameMode.SURVIVAL;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> null;
        };

        // If shortcut was used, the first arg (if any) is the target player
        int targetArgIndex;
        if (mode != null) {
            targetArgIndex = 0;
        } else {
            // Standard /gm <mode> usage
            if (args.length < 1) {
                sender.sendMessage(plugin.getMessageManager().error("player.gamemode.usage"));
                return true;
            }
            mode = parseGameMode(args[0]);
            if (mode == null) {
                sender.sendMessage(plugin.getMessageManager().error("player.gamemode.invalid-mode"));
                return true;
            }
            targetArgIndex = 1;
        }

        // Determine target player
        Player target = isPlayer ? (Player) sender : null;

        if (args.length > targetArgIndex) {
            // Trying to change another player's game mode
            if (!sender.hasPermission("justplugin.gamemode.others")) {
                sender.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                return true;
            }
            target = Bukkit.getPlayer(args[targetArgIndex]);
            if (target == null) {
                sender.sendMessage(plugin.getMessageManager().error("general.player-not-found"));
                return true;
            }
        }

        if (target == null) {
            sender.sendMessage(plugin.getMessageManager().error("player.gamemode.console-specify"));
            return true;
        }

        target.setGameMode(mode);
        target.sendMessage(plugin.getMessageManager().success("player.gamemode.changed-self", "{mode}", mode.name().toLowerCase()));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getMessageManager().success("player.gamemode.changed-other", "{player}", target.getName(), "{mode}", mode.name().toLowerCase()));
            String senderName = sender instanceof Player ? sender.getName() : "Console";
            plugin.getLogManager().log("gamemode", "<yellow>" + senderName + "</yellow> set <yellow>" + target.getName() + "</yellow>'s game mode to <yellow>" + mode.name().toLowerCase() + "</yellow>");
        } else {
            String senderName = sender instanceof Player ? sender.getName() : "Console";
            plugin.getLogManager().log("gamemode", "<yellow>" + senderName + "</yellow> changed their game mode to <yellow>" + mode.name().toLowerCase() + "</yellow>");
        }
        return true;
    }

    private GameMode parseGameMode(String input) {
        return switch (input.toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        boolean isShortcut = List.of("gmc", "gms", "gma", "gmsp").contains(label.toLowerCase());

        if (isShortcut) {
            // Shortcut: /gmc [player] - arg 1 is the target
            if (args.length == 1 && sender.hasPermission("justplugin.gamemode.others")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
        } else {
            // Standard: /gm <mode> [player]
            if (args.length == 1) {
                return List.of("survival", "creative", "adventure", "spectator", "0", "1", "2", "3").stream()
                        .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (args.length == 2 && sender.hasPermission("justplugin.gamemode.others")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }
        return List.of();
    }
}

