package org.justme.justPlugin.commands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
public class TpPosCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpPosCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tppos.usage")));
            return true;
        }
        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            World world = player.getWorld();
            if (args.length >= 4) {
                world = Bukkit.getWorld(args[3]);
                if (world == null) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tppos.world-not-found")));
                    return true;
                }
            }
            Location loc = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
            plugin.getTeleportManager().teleport(player, loc);
            player.sendMessage(plugin.getMessageManager().success("teleport.tppos.teleporting", "{x}", String.valueOf(x), "{y}", String.valueOf(y), "{z}", String.valueOf(z)));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tppos.invalid-coords")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            return switch (args.length) {
                case 1 -> List.of(String.valueOf((int) player.getLocation().getX()));
                case 2 -> List.of(String.valueOf((int) player.getLocation().getY()));
                case 3 -> List.of(String.valueOf((int) player.getLocation().getZ()));
                case 4 -> Bukkit.getWorlds().stream().map(World::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[3].toLowerCase())).collect(Collectors.toList());
                default -> List.of();
            };
        }
        return List.of();
    }
}

