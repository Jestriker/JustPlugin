package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerinfoCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.playerinfo")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cUsage: /playerinfo <player>");
            return true;
        }
        sender.sendMessage("§8§m---§8[ §6Player Info: §e" + target.getName() + " §8]§m---");
        sender.sendMessage("§7UUID: §e" + target.getUniqueId());
        String ip = target.getAddress() != null ? target.getAddress().getAddress().getHostAddress() : "Unknown";
        sender.sendMessage("§7IP: §e" + ip);
        sender.sendMessage("§7Gamemode: §e" + target.getGameMode().name());
        sender.sendMessage("§7Health: §e" + String.format("%.1f", target.getHealth()) + "§7/§e" + target.getMaxHealth());
        sender.sendMessage("§7Food: §e" + target.getFoodLevel());
        sender.sendMessage("§7Location: §eX=" + target.getLocation().getBlockX() + " Y=" + target.getLocation().getBlockY() + " Z=" + target.getLocation().getBlockZ() + " W=" + target.getWorld().getName());
        sender.sendMessage("§7Online: §atrue");
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
