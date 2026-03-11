package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.IgnoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IgnoreCommand implements CommandExecutor, TabCompleter {

    private final IgnoreManager ignoreManager;

    public IgnoreCommand(JustPlugin plugin) {
        this.ignoreManager = plugin.getIgnoreManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /ignore <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot ignore yourself.");
            return true;
        }
        boolean nowIgnoring = ignoreManager.toggleIgnore(player.getUniqueId(), target.getUniqueId());
        if (nowIgnoring) {
            player.sendMessage("§aYou are now ignoring §e" + target.getName() + "§a.");
        } else {
            player.sendMessage("§aYou are no longer ignoring §e" + target.getName() + "§a.");
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
