package org.justme.justPlugin.commands.info;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ListCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        var online = Bukkit.getOnlinePlayers();
        int max = Bukkit.getMaxPlayers();

        // Filter out vanished players for non-permitted users
        List<String> visiblePlayers = online.stream()
                .filter(p -> !plugin.getVanishManager().isVanished(p) || (sender instanceof Player sp && sp.hasPermission("justplugin.vanish.see")))
                .map(Player::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        int count = visiblePlayers.size();

        sender.sendMessage(CC.translate(""));
        sender.sendMessage(CC.info("<gold><bold>Online Players</bold></gold> <dark_gray>(<green>" + count + "<dark_gray>/<green>" + max + "<dark_gray>)"));

        if (visiblePlayers.isEmpty()) {
            sender.sendMessage(CC.line("<gray>No players online."));
        } else {
            for (String name : visiblePlayers) {
                Player p = Bukkit.getPlayerExact(name);
                if (p != null) {
                    String world = p.getWorld().getName();
                    sender.sendMessage(CC.line("<yellow>" + name + " <dark_gray>- <gray>" + world));
                } else {
                    sender.sendMessage(CC.line("<yellow>" + name));
                }
            }
        }

        sender.sendMessage(CC.translate(""));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

