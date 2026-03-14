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

@SuppressWarnings("NullableProblems")
public class GetDeathPosCommand implements TabExecutor {

    private final JustPlugin plugin;

    public GetDeathPosCommand(JustPlugin plugin) {
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
            if (!player.hasPermission("justplugin.getdeathpos.others")) {
                player.sendMessage(CC.error("You don't have permission to view other players' death locations."));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player not found!"));
                return true;
            }
            if (!plugin.getPlayerListener().hasDeathLocation(target.getUniqueId())) {
                player.sendMessage(CC.error("<yellow>" + target.getName() + "</yellow> has no recorded death location."));
                return true;
            }
            showDeathPos(player, target, true);
            return true;
        }

        // Self — check configurable permission
        boolean requirePermSelf = plugin.getConfig().getBoolean("commands.getdeathpos.require-permission-self", false);
        if (requirePermSelf && !player.hasPermission("justplugin.getdeathpos")) {
            player.sendMessage(CC.error("You don't have permission to use this command."));
            return true;
        }

        if (!plugin.getPlayerListener().hasDeathLocation(player.getUniqueId())) {
            player.sendMessage(CC.error("No recorded death location. You haven't died yet!"));
            return true;
        }
        showDeathPos(player, player, false);
        return true;
    }

    private void showDeathPos(Player viewer, Player target, boolean isOther) {
        Location loc = plugin.getPlayerListener().getDeathLocation(target.getUniqueId());
        String world = loc.getWorld().getName();
        int x = (int) loc.getX();
        int y = (int) loc.getY();
        int z = (int) loc.getZ();

        if (isOther) {
            viewer.sendMessage(CC.info("<gold>" + target.getName() + "'s Last Death Location:"));
        } else {
            viewer.sendMessage(CC.info("<gold>Last Death Location:"));
        }
        viewer.sendMessage(CC.line("World: <yellow>" + world));
        viewer.sendMessage(CC.line("X: <yellow>" + x + " <dark_gray>| <gray>Y: <yellow>" + y + " <dark_gray>| <gray>Z: <yellow>" + z));

        // Clickable teleport for viewing others (or self with tppos permission)
        if (viewer.hasPermission("justplugin.tppos")) {
            boolean safe = plugin.getTeleportManager().isLocationSafe(loc);
            String safetyTag = safe ? "" : " <red><bold>⚠ UNSAFE</bold></red>";
            String tpCmd = "/tppos " + x + " " + y + " " + z + " " + world;
            Component clickable = CC.translate(" <dark_gray>></dark_gray> <green><bold>[Click to Teleport]</bold></green>" + safetyTag)
                    .clickEvent(ClickEvent.runCommand(tpCmd))
                    .hoverEvent(HoverEvent.showText(CC.translate("<gray>Click to run: <yellow>" + tpCmd + (safe ? "" : "\n<red>⚠ Destination appears unsafe!"))));
            viewer.sendMessage(clickable);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.getdeathpos.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
