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
public class TpaRejectCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpaRejectCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        TeleportManager tm = plugin.getTeleportManager();
        TeleportManager.TpaRequest req = tm.getIncomingRequest(player.getUniqueId());
        if (req == null) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tpaccept.no-pending")));
            return true;
        }
        tm.removeRequest(req.sender);
        player.sendMessage(CC.success(plugin.getMessageManager().raw("teleport.tpreject.rejected")));
        Player requester = Bukkit.getPlayer(req.sender);
        if (requester != null) {
            requester.sendMessage(CC.error("<yellow>" + player.getName() + "</yellow> rejected your teleport request."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
