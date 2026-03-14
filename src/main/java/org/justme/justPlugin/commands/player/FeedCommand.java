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
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.feed.others")) {
                player.sendMessage(CC.error("You don't have permission to feed other players."));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player not found!"));
                return true;
            }
        }

        target.setFoodLevel(20);
        target.setSaturation(20f);

        if (target.equals(player)) {
            player.sendMessage(CC.success("Your hunger has been restored."));
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> fed themselves");
        } else {
            player.sendMessage(CC.success("Fed <yellow>" + target.getName() + "</yellow>."));
            target.sendMessage(CC.success("Your hunger has been restored by <yellow>" + player.getName() + "</yellow>."));
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

