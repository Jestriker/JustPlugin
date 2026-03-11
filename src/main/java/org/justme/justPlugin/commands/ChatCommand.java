package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeamManager;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatCommand implements CommandExecutor, TabCompleter {

    public enum Channel { ALL, PARTY, TEAM }

    // player UUID -> current channel
    private static final Map<UUID, Channel> playerChannels = new HashMap<>();
    private final TeamManager teamManager;

    public ChatCommand(JustPlugin plugin) {
        this.teamManager = plugin.getTeamManager();
    }

    public static Channel getChannel(UUID playerId) {
        return playerChannels.getOrDefault(playerId, Channel.ALL);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            Channel current = getChannel(player.getUniqueId());
            player.sendMessage("§aCurrent channel: §e" + current.name() + "§a. Use /chat <all|party|team> [message]");
            return true;
        }
        String channelStr = args[0].toUpperCase();
        Channel channel;
        try {
            channel = Channel.valueOf(channelStr);
        } catch (IllegalArgumentException e) {
            // Treat as message in current channel
            String msg = String.join(" ", args);
            sendToChannel(player, getChannel(player.getUniqueId()), msg);
            return true;
        }
        if (args.length == 1) {
            playerChannels.put(player.getUniqueId(), channel);
            player.sendMessage("§aSwitched to §e" + channel.name() + " §achannel.");
        } else {
            String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            sendToChannel(player, channel, msg);
        }
        return true;
    }

    public void sendToChannel(Player player, Channel channel, String message) {
        switch (channel) {
            case ALL -> {
                String formatted = "§7[§aALL§7] §e" + player.getName() + "§7: §f" + message;
                for (Player p : player.getServer().getOnlinePlayers()) p.sendMessage(formatted);
            }
            case PARTY, TEAM -> {
                TeamManager.Team team = teamManager.getPlayerTeam(player.getUniqueId());
                if (team == null) {
                    player.sendMessage("§cYou are not in a team.");
                    return;
                }
                String prefix = channel == Channel.PARTY ? "§7[§bPARTY§7]" : "§7[§6TEAM§7]";
                String formatted = prefix + " §e" + player.getName() + "§7: §f" + message;
                for (UUID memberId : team.getMembers()) {
                    Player member = player.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline()) member.sendMessage(formatted);
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("all", "party", "team").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return new ArrayList<>();
    }
}
