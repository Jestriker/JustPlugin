package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SudoCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SudoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.error("Usage: /sudo <player> <command | message>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(CC.error("Player not found!"));
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (message.startsWith("/")) {
            target.performCommand(message.substring(1));
            sender.sendMessage(CC.success("Forced <yellow>" + target.getName() + "</yellow> to run: <gray>" + message));
        } else {
            target.chat(message);
            sender.sendMessage(CC.success("Forced <yellow>" + target.getName() + "</yellow> to say: <gray>" + message));
        }
        return true;
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

