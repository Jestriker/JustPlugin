package org.justme.justPlugin.commands.teleport;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

/**
 * Internal command used by /getpos "TP to Player" button.
 * Checks the target's current (live) location safety before teleporting.
 * If unsafe, warns the staff member and offers god/creative mode.
 */
@SuppressWarnings("NullableProblems")
public class TpSafeCheckCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpSafeCheckCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.safe-teleport.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.safe-teleport.player-not-found")));
            return true;
        }

        Location destLoc = target.getLocation();
        boolean safe = plugin.getTeleportManager().isLocationSafe(destLoc);
        boolean destFlying = target.isFlying();

        if (!safe || destFlying) {
            // Destination is unsafe - warn, offer options based on permissions
            player.sendMessage(CC.warning("⚠ <yellow>" + target.getName() + "</yellow>'s current location is unsafe!"));
            if (destFlying) {
                player.sendMessage(CC.line("<gray>The player is currently flying."));
            }

            boolean hasGm = player.hasPermission("justplugin.gamemode");
            boolean hasGod = player.hasPermission("justplugin.god");

            if (hasGm || hasGod) {
                player.sendMessage(CC.info("Consider enabling protection before teleporting:"));
            }

            Component buttons = CC.translate("  ");
            if (hasGm) {
                buttons = buttons.append(CC.translate("<click:run_command:'/gmc'><hover:show_text:'<gray>Click to switch to <green>Creative Mode'><gold><bold>[Creative Mode]</bold></gold></hover></click> "));
            }
            if (hasGod) {
                buttons = buttons.append(CC.translate("<click:run_command:'/god'><hover:show_text:'<gray>Click to enable <green>God Mode'><gold><bold>[God Mode]</bold></gold></hover></click> "));
            }
            buttons = buttons.append(CC.translate("<click:run_command:'/tp " + target.getName() + "'><hover:show_text:'<gray>Click to teleport anyway'><red><bold>[TP Anyway]</bold></red></hover></click>"));
            player.sendMessage(buttons);
        } else {
            // Safe - teleport directly
            plugin.getTeleportManager().setBackLocation(player.getUniqueId(), player.getLocation());
            player.teleportAsync(destLoc);
            player.sendMessage(CC.success("Teleported to <yellow>" + target.getName() + "</yellow>."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

