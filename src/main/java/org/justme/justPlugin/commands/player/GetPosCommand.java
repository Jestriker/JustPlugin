package org.justme.justPlugin.commands.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

public class GetPosCommand implements TabExecutor {

    private final JustPlugin plugin;

    public GetPosCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        // Targeting another player
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.getpos.others")) {
                player.sendMessage(CC.error("You don't have permission to view other players' positions."));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player not found!"));
                return true;
            }
            showPosition(player, target, true);
            return true;
        }

        // Self — no permission needed
        showPosition(player, player, false);
        return true;
    }

    private void showPosition(Player viewer, Player target, boolean isOther) {
        Location loc = target.getLocation();
        String world = loc.getWorld().getName();
        int x = (int) loc.getX();
        int y = (int) loc.getY();
        int z = (int) loc.getZ();

        if (isOther) {
            viewer.sendMessage(CC.info("<gold>" + target.getName() + "'s Position:"));
        } else {
            viewer.sendMessage(CC.info("<gold>Your Position:"));
        }
        viewer.sendMessage(CC.line("World: <yellow>" + world));
        viewer.sendMessage(CC.line("X: <yellow>" + String.format("%.2f", loc.getX())
                + " <dark_gray>| <gray>Y: <yellow>" + String.format("%.2f", loc.getY())
                + " <dark_gray>| <gray>Z: <yellow>" + String.format("%.2f", loc.getZ())));
        viewer.sendMessage(CC.line("Yaw: <yellow>" + String.format("%.1f", loc.getYaw())
                + " <dark_gray>| <gray>Pitch: <yellow>" + String.format("%.1f", loc.getPitch())));

        // If viewing another player, show clickable teleport
        if (isOther && viewer.hasPermission("justplugin.tppos")) {
            String tpCmd = "/tppos " + x + " " + y + " " + z + " " + world;
            Component clickable = CC.translate(" <dark_gray>></dark_gray> <green><bold>[Click to Teleport]</bold></green>")
                    .clickEvent(ClickEvent.runCommand(tpCmd))
                    .hoverEvent(HoverEvent.showText(CC.translate("<gray>Click to run: <yellow>" + tpCmd)));
            viewer.sendMessage(clickable);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.getpos.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
