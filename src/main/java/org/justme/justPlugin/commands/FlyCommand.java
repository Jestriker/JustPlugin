package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.FlyToggleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlyCommand implements CommandExecutor, TabCompleter {

    private final FlyToggleManager flyManager;

    public FlyCommand(JustPlugin plugin) {
        this.flyManager = plugin.getFlyToggleManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cThis command can only be used by players when no target is specified.");
                return true;
            }
            if (!player.hasPermission("justplugin.fly")) {
                player.sendMessage("§cYou don't have permission to fly.");
                return true;
            }
            boolean flyOn = flyManager.toggleFly(player);
            player.sendMessage(flyOn ? "§aFlight enabled." : "§cFlight disabled.");
        } else {
            if (!sender.hasPermission("justplugin.fly.others")) {
                sender.sendMessage("§cYou don't have permission to toggle fly for others.");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
                return true;
            }
            boolean flyOn = flyManager.toggleFly(target);
            sender.sendMessage(flyOn ? "§aFlight enabled for §e" + target.getName() + "§a." : "§cFlight disabled for §e" + target.getName() + "§c.");
            target.sendMessage(flyOn ? "§aFlight enabled." : "§cFlight disabled.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.fly.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
