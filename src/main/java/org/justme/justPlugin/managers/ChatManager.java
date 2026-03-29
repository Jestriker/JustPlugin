package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;

public class ChatManager {

    public enum ChatMode {
        ALL, TEAM
    }

    private final JustPlugin plugin;
    private final Map<UUID, ChatMode> chatModes = new HashMap<>();
    private final Map<UUID, UUID> lastMessaged = new HashMap<>(); // for /r

    public ChatManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public ChatMode getChatMode(UUID player) {
        return chatModes.getOrDefault(player, ChatMode.ALL);
    }

    public void setChatMode(UUID player, ChatMode mode) {
        chatModes.put(player, mode);
    }

    public void setLastMessaged(UUID sender, UUID target) {
        lastMessaged.put(sender, target);
        lastMessaged.put(target, sender);
    }

    public UUID getLastMessaged(UUID player) {
        return lastMessaged.get(player);
    }

    public void sendTeamMessage(Player sender, String message) {
        TeamManager teamManager = plugin.getTeamManager();
        String teamName = teamManager.getPlayerTeam(sender.getUniqueId());
        if (teamName == null) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("team.general.not-in-team")));
            return;
        }
        TeamManager.TeamData team = teamManager.getTeam(teamName);
        if (team == null) return;

        var msg = CC.translate("<dark_aqua>[Team] <aqua>" + sender.getName() + " <dark_gray>» <white>" + message);
        for (UUID memberUuid : team.members) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null) {
                member.sendMessage(msg);
            }
        }
    }

    public void removePlayer(UUID uuid) {
        chatModes.remove(uuid);
        lastMessaged.remove(uuid);
    }
}

