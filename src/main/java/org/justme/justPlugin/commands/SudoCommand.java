package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SudoCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.sudo")) {
            sender.sendMessage("§cYou don't have permission to use sudo.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /sudo <player> <command>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
            return true;
        }
        String cmd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        sender.sendMessage("§aSudo: forcing §e" + target.getName() + " §ato run: §e/" + cmd);
        Bukkit.getServer().dispatchCommand(target, cmd);
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
