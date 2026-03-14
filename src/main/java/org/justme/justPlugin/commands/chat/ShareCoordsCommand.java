package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.ChatManager;
import org.justme.justPlugin.managers.TeamManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class ShareCoordsCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ShareCoordsCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        Location loc = player.getLocation();
        String coordMsg = "<yellow>" + player.getName() + "</yellow> <gray>is at</gray> <gold>"
                + (int) loc.getX() + ", " + (int) loc.getY() + ", " + (int) loc.getZ()
                + "</gold> <gray>in</gray> <gold>" + loc.getWorld().getName() + "</gold>";

        String channel = args.length >= 1 ? args[0].toLowerCase() : "all";

        switch (channel) {
            case "team" -> {
                String teamName = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                TeamManager.TeamData team = plugin.getTeamManager().getTeam(teamName);
                if (team != null) {
                    var msg = CC.translate("<dark_aqua>[Team] </dark_aqua>" + coordMsg);
                    for (UUID memberUuid : team.members) {
                        Player member = Bukkit.getPlayer(memberUuid);
                        if (member != null) member.sendMessage(msg);
                    }
                }
            }
            default -> {
                // Global
                Bukkit.broadcast(CC.translate("<dark_green>[Coords] </dark_green>" + coordMsg));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("all", "team");
        return List.of();
    }
}

