package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /clearchat [reason] - Clears the chat for all players.
 * Logs to staff and webhook. Configurable post-clear message.
 */
public class ClearChatCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ClearChatCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String executedBy = sender instanceof Player ? sender.getName() : "Console";
        String reason = args.length >= 1 ? String.join(" ", Arrays.copyOfRange(args, 0, args.length)) : null;

        // Send blank lines to clear chat
        net.kyori.adventure.text.Component blank = net.kyori.adventure.text.Component.empty();
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Skip staff with log permission - they'll see the log
            for (int i = 0; i < 100; i++) {
                player.sendMessage(blank);
            }
        }

        // Post-clear message
        boolean showPostMessage = plugin.getConfig().getBoolean("clear-chat.show-message", true);
        if (showPostMessage) {
            String postMsg = plugin.getConfig().getString("clear-chat.message",
                    "<gray>[<gradient:#00aaff:#00ffaa>JustPlugin</gradient>] <yellow>Chat has been cleared.");
            Bukkit.broadcast(CC.translate(postMsg));
        }

        // Notify sender
        sender.sendMessage(plugin.getMessageManager().success("chat.clearchat.success"));

        // Log - after clearing so it appears after the blank lines
        String logMsg = "<yellow>" + executedBy + "</yellow> cleared the chat.";
        if (reason != null) {
            logMsg += " Reason: <gray>" + reason;
        }
        plugin.getLogManager().log("admin", logMsg);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

