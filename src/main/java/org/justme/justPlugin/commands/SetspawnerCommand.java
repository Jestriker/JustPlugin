package org.justme.justPlugin.commands;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetspawnerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("justplugin.setspawner")) {
            player.sendMessage("§cYou don't have permission to change spawners.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /setspawner <entity_type>");
            return true;
        }
        Block target = player.getTargetBlock(null, 5);
        if (target.getType() != Material.SPAWNER) {
            player.sendMessage("§cYou are not looking at a spawner.");
            return true;
        }
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid entity type: §e" + args[0]);
            return true;
        }
        CreatureSpawner spawner = (CreatureSpawner) target.getState();
        spawner.setSpawnedType(entityType);
        spawner.update();
        player.sendMessage("§aSpawner changed to §e" + entityType.name() + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(EntityType.values())
                    .map(e -> e.name().toLowerCase())
                    .filter(n -> n.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
