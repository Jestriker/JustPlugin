package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class AnnounceCommand implements TabExecutor {

    private final JustPlugin plugin;

    public AnnounceCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageManager().error("chat.announce.usage"));
            return true;
        }
        String message = String.join(" ", args);
        Bukkit.broadcast(CC.translate(plugin.getMessageManager().raw("chat.announce.format").replace("{message}", message)));
        String senderName = sender instanceof org.bukkit.entity.Player ? sender.getName() : "Console";
        plugin.getLogManager().log("admin", "<yellow>" + senderName + "</yellow> announced: <gray>" + message);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

