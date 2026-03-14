package org.justme.justPlugin.commands.teleport;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class TprCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TprCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        player.sendMessage(CC.info("Finding a safe random location..."));
        Location loc = plugin.getTeleportManager().getRandomLocation(player.getWorld());
        // Load the chunk first, then use getSafeLocation for full block safety
        loc.getWorld().getChunkAtAsync(loc).thenAccept(chunk -> {
            Location safeLoc = plugin.getTeleportManager().getSafeLocation(loc);
            plugin.getTeleportManager().teleport(player, safeLoc);
            player.sendMessage(CC.success("Teleported to <yellow>" + (int) safeLoc.getX() + ", " + (int) safeLoc.getY() + ", " + (int) safeLoc.getZ() + "</yellow>."));
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

