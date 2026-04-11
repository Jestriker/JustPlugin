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
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        // Targeting another player
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.getdeathpos.others")) {
                player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getMessageManager().error("general.player-not-found"));
                return true;
            }
            if (!plugin.getPlayerListener().hasDeathLocation(target.getUniqueId())) {
                player.sendMessage(plugin.getMessageManager().error("player.getdeathpos.no-death-other", "{player}", target.getName()));
                return true;
            }
            showDeathPos(player, target, true);
            return true;
        }

        // Self - check configurable permission
        boolean requirePermSelf = plugin.getConfig().getBoolean("commands.getdeathpos.require-permission-self", false);
        if (requirePermSelf && !player.hasPermission("justplugin.getdeathpos")) {
            player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
            return true;
        }

        if (!plugin.getPlayerListener().hasDeathLocation(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().error("player.getdeathpos.no-death"));
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
            viewer.sendMessage(plugin.getMessageManager().info("player.getdeathpos.header-other", "{player}", target.getName()));
        } else {
            viewer.sendMessage(plugin.getMessageManager().info("player.getdeathpos.header-self"));
        }
        viewer.sendMessage(plugin.getMessageManager().line("player.getdeathpos.world-line", "{world}", world));
        viewer.sendMessage(plugin.getMessageManager().line("player.getdeathpos.coords-line", "{x}", String.valueOf(x), "{y}", String.valueOf(y), "{z}", String.valueOf(z)));

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
