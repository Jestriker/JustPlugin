package org.justme.justPlugin.commands.warp;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class WarpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public WarpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        if (args.length < 1) {
            // Show warp list
            var names = plugin.getWarpManager().getWarpNames();
            if (names.isEmpty()) {
                player.sendMessage(plugin.getMessageManager().info("warp.warps.none"));
            } else {
                boolean clickable = plugin.getConfig().getBoolean("clickable-commands.warp-list", true);
                String warpList = names.stream()
                        .map(n -> CC.clickCmd("<yellow>" + n + "</yellow>", "/warp " + n, clickable))
                        .collect(java.util.stream.Collectors.joining("<gray>, "));
                player.sendMessage(CC.translate(CC.PREFIX + "Warps: " + warpList));
            }
            return true;
        }

        // Delay check (time between uses) - requires explicit delaybypass permission
        if (!player.hasPermission("justplugin.warp.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "warp")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "warp");
            player.sendMessage(plugin.getMessageManager().error("general.cooldown-wait", "{time}", CooldownManager.formatTime(remaining)));
            return true;
        }

        Location loc = plugin.getWarpManager().getWarp(args[0]);
        if (loc == null) {
            player.sendMessage(plugin.getMessageManager().error("warp.warp.not-found", "{warp}", args[0]));
            return true;
        }
        boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                player, loc, "justplugin.warp.cooldownbypass", "warp", "justplugin.warp.unsafetp");
        if (initiated) {
            plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "warp");
            player.sendMessage(plugin.getMessageManager().success("warp.warp.teleporting", "{warp}", args[0]));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getWarpManager().getWarpNames().stream()
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

