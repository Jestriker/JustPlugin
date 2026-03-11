package org.justme.justPlugin.commands.chat;

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

public class IgnoreCommand implements TabExecutor {

    private final JustPlugin plugin;

    public IgnoreCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /ignore <player>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(CC.error("Player not found!"));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.error("You can't ignore yourself!"));
            return true;
        }
        plugin.getIgnoreManager().toggleIgnore(player.getUniqueId(), target.getUniqueId());
        boolean ignoring = plugin.getIgnoreManager().isIgnoring(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(CC.success("You are " + (ignoring ? "now ignoring" : "no longer ignoring") + " <yellow>" + target.getName() + "</yellow>."));
        if (ignoring) {
            target.sendMessage(CC.warning("<yellow>" + player.getName() + "</yellow> is now ignoring you."));
        } else {
            target.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> is no longer ignoring you."));
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


