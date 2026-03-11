package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TemporaryBanManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TempbanipCommand implements CommandExecutor, TabCompleter {

    private final TemporaryBanManager tempBanManager;

    public TempbanipCommand(JustPlugin plugin) {
        this.tempBanManager = plugin.getTemporaryBanManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("justplugin.tempbanip")) {
            sender.sendMessage("§cYou don't have permission to temp-ban IPs.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tempbanip <ip> <duration> [reason]");
            return true;
        }
        String ip = args[0];
        String durationStr = args[1];
        long duration = TemporaryBanManager.parseDuration(durationStr);
        if (duration <= 0) {
            sender.sendMessage("§cInvalid duration: §e" + durationStr);
            return true;
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "IP temporarily banned.";
        tempBanManager.addTempBan(ip, reason, duration, sender.getName(), true);
        String durationFormatted = TemporaryBanManager.formatDuration(duration);
        // Kick any players with that IP
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(ip)) {
                p.kickPlayer("§cYour IP has been temporarily banned for §e" + durationFormatted + "§c.\n§7Reason: §e" + reason);
            }
        }
        sender.sendMessage("§aIP §e" + ip + " §ahas been temp-banned for §e" + durationFormatted + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
