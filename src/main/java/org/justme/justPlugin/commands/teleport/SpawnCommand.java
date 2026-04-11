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
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class SpawnCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SpawnCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        // Delay check (time between uses) - requires explicit delaybypass permission
        if (!player.hasPermission("justplugin.spawn.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "spawn")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "spawn");
            player.sendMessage(plugin.getMessageManager().error("general.cooldown-wait", "{time}", CooldownManager.formatTime(remaining)));
            return true;
        }

        // Check for custom spawn in config
        String worldName = plugin.getConfig().getString("spawn.world");
        Location spawnLoc;
        if (worldName != null && plugin.getConfig().contains("spawn.x")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.spawn.world-not-found")));
                return true;
            }
            spawnLoc = new Location(world,
                    plugin.getConfig().getDouble("spawn.x"),
                    plugin.getConfig().getDouble("spawn.y"),
                    plugin.getConfig().getDouble("spawn.z"),
                    (float) plugin.getConfig().getDouble("spawn.yaw"),
                    (float) plugin.getConfig().getDouble("spawn.pitch"));
        } else {
            spawnLoc = Bukkit.getWorlds().getFirst().getSpawnLocation();
        }

        boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                player, spawnLoc, "justplugin.teleport.bypass", "spawn", "justplugin.spawn.unsafetp");
        if (initiated) {
            plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "spawn");
            player.sendMessage(CC.success(plugin.getMessageManager().raw("teleport.spawn.teleporting")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
