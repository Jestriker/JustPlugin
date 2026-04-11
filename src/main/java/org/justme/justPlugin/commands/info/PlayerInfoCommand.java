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
            sender.sendMessage(plugin.getMessageManager().error("info.playerinfo.usage"));
            return true;
        }

        Player online = Bukkit.getPlayer(args[0]);
        if (online != null) {
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.header-online", "{player}", online.getName()));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.uuid", "{uuid}", online.getUniqueId().toString()));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.display-name", "{display}", CC.legacy(online.displayName())));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.health", "{health}", String.format("%.1f", online.getHealth()), "{max_health}", String.format("%.1f", online.getMaxHealth())));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.food", "{food}", String.format("%.1f", (double) online.getFoodLevel())));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.gamemode", "{gamemode}", online.getGameMode().name()));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.world", "{world}", online.getWorld().getName()));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.location", "{x}", String.valueOf((int)online.getLocation().getX()), "{y}", String.valueOf((int)online.getLocation().getY()), "{z}", String.valueOf((int)online.getLocation().getZ())));
            if (online.getAddress() != null && sender.hasPermission("justplugin.playerinfo.ip")) {
                String ip = online.getAddress().getAddress().getHostAddress();
                sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.ip", "{ip}", ip));
            }
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.flying", "{flying}", String.valueOf(online.isFlying())));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.op", "{op}", String.valueOf(online.isOp())));
            double bal = plugin.getEconomyManager().getBalance(online.getUniqueId());
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.balance", "{balance}", plugin.getEconomyManager().format(bal)));
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.header-offline", "{player}", args[0]));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.uuid", "{uuid}", offP.getUniqueId().toString()));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.last-seen", "{last_seen}", offP.getLastSeen() > 0 ? new java.util.Date(offP.getLastSeen()).toString() : "Unknown"));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.first-played", "{first_played}", offP.getFirstPlayed() > 0 ? new java.util.Date(offP.getFirstPlayed()).toString() : "Unknown"));
            sender.sendMessage(plugin.getMessageManager().info("info.playerinfo.banned", "{banned}", String.valueOf(offP.isBanned())));
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
