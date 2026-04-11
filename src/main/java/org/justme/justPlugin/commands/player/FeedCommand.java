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

@SuppressWarnings("NullableProblems")
public class FeedCommand implements TabExecutor {

    private final JustPlugin plugin;

    public FeedCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.feed.others")) {
                player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getMessageManager().error("general.player-not-found"));
                return true;
            }
        }

        target.setFoodLevel(20);
        target.setSaturation(20f);

        if (target.equals(player)) {
            player.sendMessage(plugin.getMessageManager().success("player.feed.fed-self"));
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> fed themselves");
        } else {
            player.sendMessage(plugin.getMessageManager().success("player.feed.fed-other", "{player}", target.getName()));
            target.sendMessage(plugin.getMessageManager().success("player.feed.fed-notify", "{player}", player.getName()));
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> fed <yellow>" + target.getName() + "</yellow>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.feed.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

