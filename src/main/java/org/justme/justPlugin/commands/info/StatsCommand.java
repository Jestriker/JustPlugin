package org.justme.justPlugin.commands.info;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;

public class StatsCommand implements TabExecutor {

    private final JustPlugin plugin;

    public StatsCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        if (args.length == 0) {
            // Self stats
            if (!player.hasPermission("justplugin.stats")) {
                player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                return true;
            }
            plugin.getStatsGui().open(player, player);
        } else {
            // Others stats
            if (!player.hasPermission("justplugin.stats.others")) {
                player.sendMessage(plugin.getMessageManager().error("info.stats.no-permission-others"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getMessageManager().error("info.stats.player-not-found", "{player}", args[0]));
                return true;
            }
            plugin.getStatsGui().open(player, target);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("justplugin.stats.others")) {
            String prefix = args[0].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    // Don't suggest vanished players to non-staff
                    if (plugin.getVanishManager().isVanished(p.getUniqueId())
                            && !sender.hasPermission("justplugin.vanish.see")) {
                        continue;
                    }
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}

