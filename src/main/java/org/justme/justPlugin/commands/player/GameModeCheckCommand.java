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

public class GameModeCheckCommand implements TabExecutor {

    private final JustPlugin plugin;

    public GameModeCheckCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // /gmcheck [player] — if no player specified, check your own (must be a player)
        if (args.length < 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(CC.error("Usage: /gmcheck <player>"));
                return true;
            }
            showGameModeInfo(sender, player);
            return true;
        }

        // Check a specific player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> is not online!"));
            return true;
        }

        showGameModeInfo(sender, target);
        return true;
    }

    private void showGameModeInfo(CommandSender sender, Player target) {
        GameMode gm = target.getGameMode();
        String gmColor = switch (gm) {
            case SURVIVAL -> "<green>";
            case CREATIVE -> "<aqua>";
            case ADVENTURE -> "<gold>";
            case SPECTATOR -> "<gray>";
        };

        sender.sendMessage(CC.info("<gold><bold>Game Mode Info</bold></gold>"));
        sender.sendMessage(CC.line("Player: <yellow>" + target.getName()));
        sender.sendMessage(CC.line("Game Mode: " + gmColor + gm.name().toLowerCase()));
        sender.sendMessage(CC.line("Flying: " + (target.isFlying() ? "<green>yes" : "<red>no")));
        sender.sendMessage(CC.line("Allow Flight: " + (target.getAllowFlight() ? "<green>yes" : "<red>no")));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}



