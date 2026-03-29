package org.justme.justPlugin.commands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeleportManager;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class TpaCancelCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpaCancelCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        TeleportManager tm = plugin.getTeleportManager();
        // Cancel pending teleport delay (after accept)
        if (tm.isWaitingToTeleport(player.getUniqueId())) {
            tm.cancelPendingTeleport(player.getUniqueId());
            player.sendMessage(CC.success(plugin.getMessageManager().raw("teleport.tpacancel.tp-cancelled")));
            return true;
        }
        // Cancel outgoing request
        TeleportManager.TpaRequest req = tm.getOutgoingRequest(player.getUniqueId());
        if (req == null) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tpaccept.no-pending")));
            return true;
        }
        tm.cancelOutgoingRequest(player.getUniqueId());
        player.sendMessage(CC.success(plugin.getMessageManager().raw("teleport.tpacancel.request-cancelled")));
        Player target = Bukkit.getPlayer(req.target);
        if (target != null) {
            target.sendMessage(CC.warning("<yellow>" + player.getName() + "</yellow> cancelled their teleport request."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
