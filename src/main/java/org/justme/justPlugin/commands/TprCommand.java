package org.justme.justPlugin.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TprCommand implements CommandExecutor, TabCompleter {

    private final BackManager backManager;
    private final Random random = new Random();

    public TprCommand(JustPlugin plugin) {
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        player.sendMessage("§aSearching for a safe random location...");
        World world = player.getWorld();
        Location safe = findSafeLocation(world, player.getLocation());
        if (safe == null) {
            player.sendMessage("§cCould not find a safe location after many attempts. Try again.");
            return true;
        }
        backManager.setTeleportLocation(player.getUniqueId(), player.getLocation());
        player.teleport(safe);
        player.sendMessage("§aRandomly teleported to §e" + safe.getBlockX() + ", " + safe.getBlockY() + ", " + safe.getBlockZ() + "§a.");
        return true;
    }

    private Location findSafeLocation(World world, Location origin) {
        int maxAttempts = 20;
        int range = 2000;
        for (int i = 0; i < maxAttempts; i++) {
            int x = origin.getBlockX() + random.nextInt(range * 2) - range;
            int z = origin.getBlockZ() + random.nextInt(range * 2) - range;
            int y = world.getHighestBlockYAt(x, z);
            if (y < 0 || y >= 320) continue;
            Location candidate = new Location(world, x + 0.5, y + 1, z + 0.5);
            Material ground = world.getBlockAt(x, y, z).getType();
            if (ground == Material.WATER || ground == Material.LAVA) continue;
            if (ground == Material.VOID_AIR || ground == Material.AIR) continue;
            if (ground.name().contains("MAGMA") || ground.name().contains("FIRE")
                    || ground.name().contains("CACTUS") || ground.name().contains("BERRY_BUSH")) continue;
            Material feetBlock = world.getBlockAt(x, y + 1, z).getType();
            Material headBlock = world.getBlockAt(x, y + 2, z).getType();
            if (feetBlock != Material.AIR && !feetBlock.isTransparent()) continue;
            if (headBlock != Material.AIR && !headBlock.isTransparent()) continue;
            return candidate;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
