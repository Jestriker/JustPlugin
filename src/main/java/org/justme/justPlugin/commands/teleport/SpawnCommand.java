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

public class SpawnCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SpawnCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        // Cooldown check (applies even to OPs unless explicit bypass)
        if (!player.hasPermission("justplugin.spawn.nocooldown")
                && plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "spawn")) {
            int remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), "spawn");
            player.sendMessage(CC.error("You must wait <yellow>" + remaining + "</yellow> seconds before using this command again."));
            return true;
        }

        // Check for custom spawn in config
        String worldName = plugin.getConfig().getString("spawn.world");
        Location spawnLoc;
        if (worldName != null && plugin.getConfig().contains("spawn.x")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(CC.error("Spawn world not found!"));
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
            plugin.getCooldownManager().setCooldown(player.getUniqueId(), "spawn");
            player.sendMessage(CC.success("Teleporting to spawn."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
