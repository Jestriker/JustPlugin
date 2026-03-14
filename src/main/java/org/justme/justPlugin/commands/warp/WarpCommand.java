package org.justme.justPlugin.commands.warp;

import org.bukkit.Location;
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
public class WarpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public WarpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            // Show warp list
            var names = plugin.getWarpManager().getWarpNames();
            if (names.isEmpty()) {
                player.sendMessage(CC.info("No warps available."));
            } else {
                boolean clickable = plugin.getConfig().getBoolean("clickable-commands.warp-list", true);
                String warpList = names.stream()
                        .map(n -> CC.clickCmd("<yellow>" + n + "</yellow>", "/warp " + n, clickable))
                        .collect(java.util.stream.Collectors.joining("<gray>, "));
                player.sendMessage(CC.translate(CC.PREFIX + "Warps: " + warpList));
            }
            return true;
        }

        // Cooldown check (applies even to OPs unless explicit bypass)
        if (!player.hasPermission("justplugin.warp.nocooldown")
                && plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "warp")) {
            int remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), "warp");
            player.sendMessage(CC.error("You must wait <yellow>" + remaining + "</yellow> seconds before using this command again."));
            return true;
        }

        Location loc = plugin.getWarpManager().getWarp(args[0]);
        if (loc == null) {
            player.sendMessage(CC.error("Warp <yellow>" + args[0] + "</yellow> not found!"));
            return true;
        }
        boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                player, loc, "justplugin.warp.cooldownbypass", "warp", "justplugin.warp.unsafetp");
        if (initiated) {
            plugin.getCooldownManager().setCooldown(player.getUniqueId(), "warp");
            player.sendMessage(CC.success("Warping to <yellow>" + args[0] + "</yellow>."));
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

