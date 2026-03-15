package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * /oplist - Lists all server operators. Staff only.
 */
public class OpListCommand implements TabExecutor {

    private final JustPlugin plugin;

    public OpListCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Set<OfflinePlayer> ops = Bukkit.getOperators();

        if (ops.isEmpty()) {
            sender.sendMessage(CC.info("There are no operators on this server."));
            return true;
        }

        sender.sendMessage(CC.prefixed("<yellow>Server Operators <gray>(" + ops.size() + ")"));

        StringBuilder list = new StringBuilder();
        int count = 0;
        for (OfflinePlayer op : ops) {
            String name = op.getName() != null ? op.getName() : op.getUniqueId().toString();
            boolean online = op.isOnline();
            if (count > 0) list.append("<gray>, ");
            list.append(online ? "<green>" + name + "</green>" : "<gray>" + name);
            count++;
        }

        sender.sendMessage(CC.line(list.toString()));

        plugin.getLogManager().log("admin", "<yellow>" + (sender instanceof org.bukkit.entity.Player ? sender.getName() : "Console") + "</yellow> viewed the operator list.");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

