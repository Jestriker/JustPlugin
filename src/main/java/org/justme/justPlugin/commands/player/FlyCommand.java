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
public class FlyCommand implements TabExecutor {

    private final JustPlugin plugin;

    public FlyCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        Player target = player;
        if (args.length >= 1 && player.hasPermission("justplugin.fly.others")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player not found!"));
                return true;
            }
        }
        target.setAllowFlight(!target.getAllowFlight());
        target.setFlying(target.getAllowFlight());
        plugin.getPlayerStateManager().saveState(target);
        String status = target.getAllowFlight() ? "<green>enabled" : "<red>disabled";
        target.sendMessage(CC.success("Flight " + status + "."));
        if (!target.equals(player)) {
            player.sendMessage(CC.success("Flight " + status + " for <yellow>" + target.getName() + "</yellow>."));
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> " + (target.getAllowFlight() ? "enabled" : "disabled") + " flight for <yellow>" + target.getName() + "</yellow>");
        } else {
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> " + (target.getAllowFlight() ? "enabled" : "disabled") + " flight");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.fly.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

