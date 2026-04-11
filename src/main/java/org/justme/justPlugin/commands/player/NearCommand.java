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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class NearCommand implements TabExecutor {

    private final JustPlugin plugin;

    public NearCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        int defaultRadius = plugin.getConfig().getInt("commands.near.default-radius", 1000);
        int maxRadius = plugin.getConfig().getInt("commands.near.max-radius", 5000);

        int radius = defaultRadius;
        if (args.length >= 1) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessageManager().error("player.near.invalid-radius"));
                return true;
            }
            if (radius < 1) {
                player.sendMessage(plugin.getMessageManager().error("player.near.min-radius"));
                return true;
            }
            if (radius > maxRadius) {
                player.sendMessage(plugin.getMessageManager().error("player.near.max-radius", "{max}", String.valueOf(maxRadius)));
                return true;
            }
        }

        Location playerLoc = player.getLocation();
        boolean canSeeVanished = player.hasPermission("justplugin.vanish.see");

        List<NearbyPlayer> nearbyPlayers = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            if (!online.getWorld().equals(playerLoc.getWorld())) continue;
            if (plugin.getVanishManager().isVanished(online.getUniqueId()) && !canSeeVanished) continue;

            double distance = playerLoc.distance(online.getLocation());
            if (distance <= radius) {
                nearbyPlayers.add(new NearbyPlayer(online, distance));
            }
        }

        nearbyPlayers.sort(Comparator.comparingDouble(np -> np.distance));

        if (nearbyPlayers.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().info("player.near.no-players", "{radius}", String.valueOf(radius)));
            return true;
        }

        player.sendMessage(plugin.getMessageManager().info("player.near.header", "{radius}", String.valueOf(radius), "{count}", String.valueOf(nearbyPlayers.size())));

        for (NearbyPlayer np : nearbyPlayers) {
            Location loc = np.player.getLocation();
            String direction = getDirection(playerLoc, loc);
            String distStr = String.format("%.1f", np.distance);
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            String world = loc.getWorld().getName();

            Component line = CC.translate(" <dark_gray>></dark_gray> <yellow>" + np.player.getName()
                    + " <dark_gray>- <gray>" + distStr + " blocks <dark_gray>(<aqua>" + direction
                    + "</aqua>) <dark_gray>[<gray>" + x + ", " + y + ", " + z + "<dark_gray>]");

            if (player.hasPermission("justplugin.tppos")) {
                String tpCmd = "/tppos " + x + " " + y + " " + z + " " + world;
                Component tpButton = CC.translate(" <green>[TP]")
                        .clickEvent(ClickEvent.runCommand(tpCmd))
                        .hoverEvent(HoverEvent.showText(CC.translate("<gray>Click to teleport")));
                line = line.append(tpButton);
            }

            player.sendMessage(line);
        }

        return true;
    }

    private String getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360;

        if (angle >= 337.5 || angle < 22.5) return "N";
        if (angle >= 22.5 && angle < 67.5) return "NE";
        if (angle >= 67.5 && angle < 112.5) return "E";
        if (angle >= 112.5 && angle < 157.5) return "SE";
        if (angle >= 157.5 && angle < 202.5) return "S";
        if (angle >= 202.5 && angle < 247.5) return "SW";
        if (angle >= 247.5 && angle < 292.5) return "W";
        if (angle >= 292.5 && angle < 337.5) return "NW";
        return "N";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("100", "500", "1000", "2000", "5000").stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private record NearbyPlayer(Player player, double distance) {}
}
