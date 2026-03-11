package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TemporaryBanManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TempbanCommand implements CommandExecutor, TabCompleter {

    private final TemporaryBanManager tempBanManager;

    public TempbanCommand(JustPlugin plugin) {
        this.tempBanManager = plugin.getTemporaryBanManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.tempban")) {
            sender.sendMessage("§cYou don't have permission to temp-ban players.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tempban <player> <duration> [reason]");
            sender.sendMessage("§cDuration format: 1d2h30m or 1h etc.");
            return true;
        }
        String targetName = args[0];
        String durationStr = args[1];
        long duration = TemporaryBanManager.parseDuration(durationStr);
        if (duration <= 0) {
            sender.sendMessage("§cInvalid duration: §e" + durationStr);
            return true;
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Temporarily banned.";
        tempBanManager.addTempBan(targetName, reason, duration, sender.getName(), false);
        Player target = Bukkit.getPlayer(targetName);
        String durationFormatted = TemporaryBanManager.formatDuration(duration);
        if (target != null && target.isOnline()) {
            target.kickPlayer("§cYou have been temporarily banned for §e" + durationFormatted + "§c.\n§7Reason: §e" + reason);
        }
        Bukkit.broadcastMessage("§c" + targetName + " §7has been temp-banned for §e" + durationFormatted + "§7. Reason: §e" + reason);
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
        if (args.length == 2) {
            return Arrays.asList("1h", "2h", "1d", "7d", "30d").stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
