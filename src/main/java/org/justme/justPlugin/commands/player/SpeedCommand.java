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
public class SpeedCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SpeedCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("player.speed.usage")));
            return true;
        }
        try {
            float speed = Float.parseFloat(args[0]);
            if (speed < 0 || speed > 10) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("player.speed.invalid-speed")));
                return true;
            }
            Player target = player;
            if (args.length >= 2 && player.hasPermission("justplugin.speed.others")) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
                    return true;
                }
            }
            float normalized = speed / 10f;
            String lbl = label.toLowerCase();

            // Determine which speed to set based on command label
            if (lbl.equals("flyspeed") || lbl.equals("fspeed")) {
                target.setFlySpeed(normalized);
                target.sendMessage(CC.success("Fly speed set to <yellow>" + speed + "</yellow>."));
            } else if (lbl.equals("walkspeed") || lbl.equals("wspeed")) {
                target.setWalkSpeed(normalized);
                target.sendMessage(CC.success("Walk speed set to <yellow>" + speed + "</yellow>."));
            } else {
                // /speed - dynamic based on current state
                if (target.isFlying()) {
                    target.setFlySpeed(normalized);
                    target.sendMessage(CC.success("Fly speed set to <yellow>" + speed + "</yellow>."));
                } else {
                    target.setWalkSpeed(normalized);
                    target.sendMessage(CC.success("Walk speed set to <yellow>" + speed + "</yellow>."));
                }
            }
            if (!target.equals(player)) {
                player.sendMessage(CC.success("Speed set for <yellow>" + target.getName() + "</yellow>."));
                plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> set <yellow>" + target.getName() + "</yellow>'s speed to <yellow>" + speed + "</yellow>");
            } else {
                plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> set their speed to <yellow>" + speed + "</yellow>");
            }
            plugin.getPlayerStateManager().saveState(target);
        } catch (NumberFormatException e) {
            player.sendMessage(CC.error("Invalid speed value!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("1", "2", "3", "5", "10");
        if (args.length == 2 && sender.hasPermission("justplugin.speed.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
