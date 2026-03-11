package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.VanishManager;

import java.util.ArrayList;
import java.util.List;

public class VanishCommand implements CommandExecutor, TabCompleter {

    private final VanishManager vanishManager;

    public VanishCommand(JustPlugin plugin) {
        this.vanishManager = plugin.getVanishManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("justplugin.vanish")) {
            player.sendMessage("§cYou don't have permission to vanish.");
            return true;
        }
        boolean nowVanished = vanishManager.toggleVanish(player);
        if (nowVanished) {
            player.sendMessage("§aYou are now §einvisible §ato other players.");
        } else {
            player.sendMessage("§aYou are now §evisible §ato other players.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
