package org.justme.justPlugin.commands.teleport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

/**
 * Internal command triggered by the "TP Anyway" button when a destination is unsafe.
 * Confirms the pending unsafe teleport stored in TeleportManager.
 */
@SuppressWarnings("NullableProblems")
public class TpUnsafeConfirmCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpUnsafeConfirmCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        plugin.getTeleportManager().confirmUnsafeTeleport(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

