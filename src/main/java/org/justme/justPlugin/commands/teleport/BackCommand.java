package org.justme.justPlugin.commands.teleport;

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

@SuppressWarnings("NullableProblems")
public class BackCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BackCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        Location back = plugin.getTeleportManager().getBackLocation(player.getUniqueId());
        if (back == null) {
            player.sendMessage(CC.error("No back location found!"));
            return true;
        }

        // Delay check (time between uses) — OPs auto-skip, or explicit delaybypass permission
        if (!player.isOp() && !player.hasPermission("justplugin.back.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "back")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "back");
            player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
            return true;
        }

        boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                player, back, "justplugin.back.cooldownbypass", "back", "justplugin.back.unsafetp");
        if (initiated) {
            plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "back");
            player.sendMessage(CC.success("Teleporting to your previous location."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

