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
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerListHideCommand implements TabExecutor {

    private final JustPlugin plugin;

    public PlayerListHideCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        // Targeting another player
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.playerlist.hide.others")) {
                player.sendMessage(CC.error("You don't have permission to hide other players from the player list."));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            UUID targetUuid;
            String targetName;
            if (target != null) {
                targetUuid = target.getUniqueId();
                targetName = target.getName();
            } else {
                @SuppressWarnings("deprecation")
                OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
                if (!offP.hasPlayedBefore()) {
                    player.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> has never joined the server."));
                    return true;
                }
                targetUuid = offP.getUniqueId();
                targetName = offP.getName() != null ? offP.getName() : args[0];
            }

            plugin.getVanishManager().togglePlayerListHidden(targetUuid);
            boolean hidden = plugin.getVanishManager().isPlayerListHidden(targetUuid);

            player.sendMessage(CC.success("<yellow>" + targetName + "</yellow> is now " + (hidden ? "<red>hidden" : "<green>visible") + " on the player list."));
            if (target != null && !target.equals(player)) {
                target.sendMessage(CC.info("You have been " + (hidden ? "<red>hidden from" : "<green>made visible on") + " the player list by <yellow>" + player.getName() + "</yellow>."));
            }

            // Log and notify
            plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> " + (hidden ? "hid" : "unhid") + " <yellow>" + targetName + "</yellow> from the player list");

            // Notify players with notification permission
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.playerlist.hide.notify") && !p.equals(player)) {
                    p.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> " + (hidden ? "hid" : "unhid") + " <yellow>" + targetName + "</yellow> from the player list."));
                }
            }
            return true;
        }

        // Self-hide
        plugin.getVanishManager().togglePlayerListHidden(player.getUniqueId());
        boolean hidden = plugin.getVanishManager().isPlayerListHidden(player.getUniqueId());

        player.sendMessage(CC.success("You are now " + (hidden ? "<red>hidden from" : "<green>visible on") + " the player list."));
        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> " + (hidden ? "hid" : "unhid") + " themselves from the player list");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.playerlist.hide.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

