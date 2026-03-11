package org.justme.justPlugin.commands.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.ChatManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ChatCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            ChatManager.ChatMode current = plugin.getChatManager().getChatMode(player.getUniqueId());
            player.sendMessage(CC.info("Current chat mode: <yellow>" + current.name()));
            player.sendMessage(CC.info("Usage: /chat <all | team>"));
            return true;
        }
        ChatManager.ChatMode mode = switch (args[0].toLowerCase()) {
            case "all", "global", "g" -> ChatManager.ChatMode.ALL;
            case "team", "t", "party", "p" -> ChatManager.ChatMode.TEAM;
            default -> null;
        };
        if (mode == null) {
            player.sendMessage(CC.error("Invalid chat mode! Use: all, team"));
            return true;
        }
        if (mode == ChatManager.ChatMode.TEAM
                && plugin.getTeamManager().getPlayerTeam(player.getUniqueId()) == null) {
            player.sendMessage(CC.error("You are not in a team!"));
            return true;
        }
        plugin.getChatManager().setChatMode(player.getUniqueId(), mode);
        player.sendMessage(CC.success("Chat mode set to <yellow>" + mode.name() + "</yellow>."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("all", "team")
                    .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

