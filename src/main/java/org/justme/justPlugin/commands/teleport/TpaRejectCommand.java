package org.justme.justPlugin.commands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeleportManager;

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
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        TeleportManager tm = plugin.getTeleportManager();
        TeleportManager.TpaRequest req = tm.getIncomingRequest(player.getUniqueId());
        if (req == null) {
            player.sendMessage(plugin.getMessageManager().error("teleport.tpreject.no-pending"));
            return true;
        }
        tm.removeRequest(req.sender);
        player.sendMessage(plugin.getMessageManager().success("teleport.tpreject.rejected"));
        Player requester = Bukkit.getPlayer(req.sender);
        if (requester != null) {
            requester.sendMessage(plugin.getMessageManager().error("teleport.tpreject.notify-requester", "{player}", player.getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
