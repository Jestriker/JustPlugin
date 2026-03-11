package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.GodManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GodCommand implements CommandExecutor, TabCompleter {

    private final GodManager godManager;

    public GodCommand(JustPlugin plugin) {
        this.godManager = plugin.getGodManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.god")) {
            sender.sendMessage("§cYou don't have permission to use god mode.");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player player)) { sender.sendMessage("§cSpecify a player."); return true; }
            boolean godOn = godManager.toggleGod(player.getUniqueId());
            player.sendMessage(godOn ? "§aGod mode §eenabled§a." : "§cGod mode §edisabled§c.");
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
                return true;
            }
            boolean godOn = godManager.toggleGod(target.getUniqueId());
            sender.sendMessage(godOn ? "§aGod mode enabled for §e" + target.getName() + "§a." : "§cGod mode disabled for §e" + target.getName() + "§c.");
            if (!target.equals(sender)) target.sendMessage(godOn ? "§aGod mode §eenabled§a." : "§cGod mode §edisabled§c.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
