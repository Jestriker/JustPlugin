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
                player.sendMessage(plugin.getMessageManager().error("info.playerlisthide.no-permission-others"));
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
                    player.sendMessage(plugin.getMessageManager().error("info.playerlisthide.never-joined",
                            "{player}", args[0]));
                    return true;
                }
                targetUuid = offP.getUniqueId();
                targetName = offP.getName() != null ? offP.getName() : args[0];
            }

            plugin.getVanishManager().togglePlayerListHidden(targetUuid);
            boolean hidden = plugin.getVanishManager().isPlayerListHidden(targetUuid);

            if (hidden) {
                player.sendMessage(plugin.getMessageManager().success("info.playerlisthide.hidden-other",
                        "{player}", targetName));
            } else {
                player.sendMessage(plugin.getMessageManager().success("info.playerlisthide.visible-other",
                        "{player}", targetName));
            }
            if (target != null && !target.equals(player)) {
                if (hidden) {
                    target.sendMessage(plugin.getMessageManager().info("info.playerlisthide.target-notified-hidden",
                            "{staff}", player.getName()));
                } else {
                    target.sendMessage(plugin.getMessageManager().info("info.playerlisthide.target-notified-visible",
                            "{staff}", player.getName()));
                }
            }

            // Log and notify
            plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> " + (hidden ? "hid" : "unhid") + " <yellow>" + targetName + "</yellow> from the player list");

            // Notify players with notification permission
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.playerlist.hide.notify") && !p.equals(player)) {
                    if (hidden) {
                        p.sendMessage(plugin.getMessageManager().info("info.playerlisthide.notify-hidden",
                                "{staff}", player.getName(), "{player}", targetName));
                    } else {
                        p.sendMessage(plugin.getMessageManager().info("info.playerlisthide.notify-visible",
                                "{staff}", player.getName(), "{player}", targetName));
                    }
                }
            }
            return true;
        }

        // Self-hide
        plugin.getVanishManager().togglePlayerListHidden(player.getUniqueId());
        boolean hidden = plugin.getVanishManager().isPlayerListHidden(player.getUniqueId());

        if (hidden) {
            player.sendMessage(plugin.getMessageManager().success("info.playerlisthide.hidden-self"));
        } else {
            player.sendMessage(plugin.getMessageManager().success("info.playerlisthide.visible-self"));
        }
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

