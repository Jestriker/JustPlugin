package org.justme.justPlugin.commands.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class SetSpawnCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SetSpawnCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        Location loc = player.getLocation();

        // Check if the block below is safe
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (!below.isSolid() || below == Material.LAVA || below == Material.MAGMA_BLOCK
                || below == Material.CACTUS || below == Material.FIRE || below == Material.SOUL_FIRE
                || below == Material.CAMPFIRE || below == Material.SOUL_CAMPFIRE) {
            player.sendMessage(CC.error("Unsafe spawn location! Stand on a solid, safe block."));
            return true;
        }

        // Save exact coords with yaw and pitch
        plugin.getConfig().set("spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spawn.x", loc.getX());
        plugin.getConfig().set("spawn.y", loc.getY());
        plugin.getConfig().set("spawn.z", loc.getZ());
        plugin.getConfig().set("spawn.yaw", (double) loc.getYaw());
        plugin.getConfig().set("spawn.pitch", (double) loc.getPitch());
        plugin.saveConfig();

        // Also set the world spawn
        loc.getWorld().setSpawnLocation(loc);

        player.sendMessage(CC.success("Spawn has been set to your exact location."));
        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> set the spawn to <yellow>" + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()) + "</yellow> in <yellow>" + loc.getWorld().getName() + "</yellow>");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

