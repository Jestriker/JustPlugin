package org.justme.justPlugin.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

public class KillCommand implements TabExecutor {

    private final JustPlugin plugin;

    public KillCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // Targeting another player
        if (args.length >= 1) {
            if (!sender.hasPermission("justplugin.kill.others")) {
                sender.sendMessage(CC.error("You don't have permission to kill other players."));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(CC.error("Player not found!"));
                return true;
            }
            target.setHealth(0);
            sender.sendMessage(CC.success("Killed <yellow>" + target.getName() + "</yellow>."));
            target.sendMessage(CC.error("You have been killed by <yellow>" + sender.getName() + "</yellow>."));
            return true;
        }

        // Self
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Usage: /kill <player>"));
            return true;
        }
        player.setHealth(0);
        player.sendMessage(CC.info("You killed yourself."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.kill.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

