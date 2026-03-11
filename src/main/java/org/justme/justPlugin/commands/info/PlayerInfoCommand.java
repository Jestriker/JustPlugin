package org.justme.justPlugin.commands.info;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerInfoCommand implements TabExecutor {

    private final JustPlugin plugin;

    public PlayerInfoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /whois <player>"));
            return true;
        }

        Player online = Bukkit.getPlayer(args[0]);
        if (online != null) {
            sender.sendMessage(CC.info("<gold><bold>Player Info: " + online.getName() + "</bold></gold>"));
            sender.sendMessage(CC.line("UUID: <yellow>" + online.getUniqueId()));
            sender.sendMessage(CC.line("Display Name: <yellow>" + CC.legacy(online.displayName())));
            sender.sendMessage(CC.line("Health: <yellow>" + String.format("%.1f", online.getHealth()) + "/" + String.format("%.1f", online.getMaxHealth())));
            sender.sendMessage(CC.line("Food: <yellow>" + String.format("%.1f", (double) online.getFoodLevel()) + "/20.0"));
            sender.sendMessage(CC.line("Game Mode: <yellow>" + online.getGameMode().name()));
            sender.sendMessage(CC.line("World: <yellow>" + online.getWorld().getName()));
            sender.sendMessage(CC.line("Location: <yellow>" + (int)online.getLocation().getX() + ", " + (int)online.getLocation().getY() + ", " + (int)online.getLocation().getZ()));
            if (online.getAddress() != null && sender.hasPermission("justplugin.playerinfo.ip")) {
                String ip = online.getAddress().getAddress().getHostAddress();
                sender.sendMessage(CC.line("IP: <yellow>" + ip));
            }
            sender.sendMessage(CC.line("Flying: <yellow>" + online.isFlying()));
            sender.sendMessage(CC.line("Op: <yellow>" + online.isOp()));
            double bal = plugin.getEconomyManager().getBalance(online.getUniqueId());
            sender.sendMessage(CC.line("Balance: <green>" + plugin.getEconomyManager().format(bal)));
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            sender.sendMessage(CC.info("<gold><bold>Player Info: " + args[0] + "</bold></gold> <red>(Offline)</red>"));
            sender.sendMessage(CC.line("UUID: <yellow>" + offP.getUniqueId()));
            sender.sendMessage(CC.line("Last Seen: <yellow>" + (offP.getLastSeen() > 0 ? new java.util.Date(offP.getLastSeen()).toString() : "Unknown")));
            sender.sendMessage(CC.line("First Played: <yellow>" + (offP.getFirstPlayed() > 0 ? new java.util.Date(offP.getFirstPlayed()).toString() : "Unknown")));
            sender.sendMessage(CC.line("Banned: <yellow>" + offP.isBanned()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
