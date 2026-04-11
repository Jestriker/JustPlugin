package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class SuperVanishCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SuperVanishCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        Player target = player;
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.supervanish.others")) {
                player.sendMessage(plugin.getMessageManager().error("moderation.supervanish.no-permission-others"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
                return true;
            }
        }

        boolean wasSuperVanished = plugin.getVanishManager().isSuperVanished(target.getUniqueId());

        if (wasSuperVanished) {
            plugin.getVanishManager().unsuperVanish(target);
        } else {
            plugin.getVanishManager().superVanish(target);
        }

        plugin.getPlayerStateManager().saveState(target);

        // Log
        String action = wasSuperVanished ? "un-super-vanished" : "super-vanished";
        plugin.getLogManager().log("vanish", "<yellow>" + (sender instanceof Player ? sender.getName() : "Console") + "</yellow> " + action + " <yellow>" + target.getName() + "</yellow>");

        if (!target.equals(player)) {
            String status = wasSuperVanished ? "<green>un-super-vanished" : "<red>super-vanished";
            player.sendMessage(plugin.getMessageManager().success("moderation.supervanish.toggled-other",
                    "{player}", target.getName(), "{status}", status));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.supervanish.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

