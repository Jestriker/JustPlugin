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

public class ExpCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§aYour XP: §eLevel " + player.getLevel() + " (Total: " + player.getTotalExperience() + ")");
            return true;
        }
        if (!player.hasPermission("justplugin.exp.modify")) {
            player.sendMessage("§cYou don't have permission to modify XP.");
            return true;
        }
        // /exp give <player> <amount>  OR  /exp set <player> <amount>  OR /exp show <player>
        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 3) { player.sendMessage("§cUsage: /exp give <player> <amount>"); return true; }
                Player target = getTarget(sender, args[1]);
                if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }
                try {
                    int amount = Integer.parseInt(args[2]);
                    target.giveExp(amount);
                    sender.sendMessage("§aGave §e" + amount + " XP §ato §e" + target.getName() + "§a.");
                    if (!target.equals(player)) target.sendMessage("§aYou received §e" + amount + " XP§a.");
                } catch (NumberFormatException e) { sender.sendMessage("§cInvalid amount."); }
            }
            case "set" -> {
                if (args.length < 3) { player.sendMessage("§cUsage: /exp set <player> <amount>"); return true; }
                Player target = getTarget(sender, args[1]);
                if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }
                try {
                    int level = Integer.parseInt(args[2]);
                    target.setLevel(0);
                    target.setExp(0);
                    target.giveExpLevels(level);
                    sender.sendMessage("§aSet §e" + target.getName() + "§a's XP to level §e" + level + "§a.");
                } catch (NumberFormatException e) { sender.sendMessage("§cInvalid amount."); }
            }
            case "show" -> {
                if (args.length < 2) { player.sendMessage("§cUsage: /exp show <player>"); return true; }
                Player target = getTarget(sender, args[1]);
                if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }
                sender.sendMessage("§e" + target.getName() + "§a: Level §e" + target.getLevel() + " §a(Total: §e" + target.getTotalExperience() + "§a)");
            }
            default -> {
                // Try /exp <amount> for self
                try {
                    int amount = Integer.parseInt(args[0]);
                    player.giveExp(amount);
                    player.sendMessage("§aGave yourself §e" + amount + " XP§a.");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cUsage: /exp [give|set|show] <player> <amount>");
                }
            }
        }
        return true;
    }

    private Player getTarget(CommandSender sender, String name) {
        Player p = Bukkit.getPlayer(name);
        if (p != null) return p;
        if (sender instanceof Player pl) return pl;
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "set", "show").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("show"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
