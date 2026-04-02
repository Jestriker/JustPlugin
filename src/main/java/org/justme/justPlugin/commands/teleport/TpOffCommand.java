package org.justme.justPlugin.commands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class TpOffCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpOffCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /tpoff <player>"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        if (!offline.hasPlayedBefore() && !offline.isOnline()) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }

        YamlConfiguration data = plugin.getDataManager().getPlayerData(offline.getUniqueId());
        if (!data.contains("last-location.world")) {
            String name = offline.getName() != null ? offline.getName() : args[0];
            player.sendMessage(plugin.getMessageManager().error("moderation.offline.tpoff-no-data",
                    "{player}", name));
            return true;
        }

        String worldName = data.getString("last-location.world", "");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(CC.error("World '" + worldName + "' is not loaded."));
            return true;
        }

        double x = data.getDouble("last-location.x");
        double y = data.getDouble("last-location.y");
        double z = data.getDouble("last-location.z");
        float yaw = (float) data.getDouble("last-location.yaw");
        float pitch = (float) data.getDouble("last-location.pitch");

        Location loc = new Location(world, x, y, z, yaw, pitch);
        player.teleport(loc);

        String name = offline.getName() != null ? offline.getName() : args[0];
        player.sendMessage(plugin.getMessageManager().success("moderation.offline.tpoff-success",
                "{player}", name));
        plugin.getLogManager().log("teleport",
                "<yellow>" + player.getName() + "</yellow> teleported to <yellow>" + name + "</yellow>'s last location (offline)");
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
