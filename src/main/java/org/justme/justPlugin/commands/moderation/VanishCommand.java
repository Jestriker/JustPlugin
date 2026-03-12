package org.justme.justPlugin.commands.moderation;

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

public class VanishCommand implements TabExecutor {

    private final JustPlugin plugin;

    public VanishCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        Player target = player;
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.vanish.others")) {
                player.sendMessage(CC.error("You don't have permission to vanish other players."));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player not found!"));
                return true;
            }
        }
        // If super vanished, toggle to regular unvanish via unsuperVanish
        boolean wasSuperVanished = plugin.getVanishManager().isSuperVanished(target.getUniqueId());
        boolean wasVanished = plugin.getVanishManager().isVanished(target) || wasSuperVanished;

        if (wasSuperVanished) {
            plugin.getVanishManager().unsuperVanish(target);
        } else {
            plugin.getVanishManager().toggleVanish(target);
        }
        plugin.getPlayerStateManager().saveState(target);

        // Log
        String action = wasVanished ? "unvanished" : "vanished";
        plugin.getLogManager().log("vanish", "<yellow>" + (sender instanceof Player ? sender.getName() : "Console") + "</yellow> " + action + " <yellow>" + target.getName() + "</yellow>");

        if (!target.equals(player)) {
            String status = wasVanished ? "<green>unvanished" : "<red>vanished";
            player.sendMessage(CC.success("<yellow>" + target.getName() + "</yellow> has been " + status + "."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.vanish.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
