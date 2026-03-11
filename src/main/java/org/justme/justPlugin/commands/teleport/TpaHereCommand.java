package org.justme.justPlugin.commands.teleport;

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

public class TpaHereCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpaHereCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /tpahere <player>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(CC.error("Player not found!"));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.error("You can't request yourself!"));
            return true;
        }
        // Check if target is ignoring sender
        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(CC.error("This player is not accepting teleport requests from you."));
            return true;
        }
        String result = plugin.getTeleportManager().sendTpaHereRequest(player.getUniqueId(), target.getUniqueId());
        if (result != null) {
            if (result.equals("already_same")) {
                player.sendMessage(CC.error("You already have a pending request for <yellow>" + target.getName() + "</yellow>."));
            } else if (result.startsWith("already_other:")) {
                String otherName = result.substring("already_other:".length());
                player.sendMessage(CC.error("You already have a pending request to <yellow>" + otherName + "</yellow>."));
                player.sendMessage(CC.info("Use <yellow>/tpacancel</yellow> to cancel it and try again."));
            }
            return true;
        }
        int timeout = plugin.getTeleportManager().getRequestTimeout();
        player.sendMessage(CC.success("TPA Here request sent to <yellow>" + target.getName() + "</yellow>. <gray>Expires in " + timeout + "s."));
        player.sendMessage(CC.info("Type <yellow>/tpacancel</yellow> to cancel."));
        target.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> has requested you to teleport to them."));
        target.sendMessage(CC.info("Type <green>/tpaccept</green> to accept or <red>/tpreject</red> to deny. <gray>Expires in " + timeout + "s."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

