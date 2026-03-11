package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeamManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShareDeathCoordsCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ShareDeathCoordsCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (!plugin.getPlayerListener().hasDeathLocation(player.getUniqueId())) {
            player.sendMessage(CC.error("No recorded death location. You haven't died yet!"));
            return true;
        }
        Location loc = plugin.getPlayerListener().getDeathLocation(player.getUniqueId());
        String coordMsg = "<yellow>" + player.getName() + "</yellow> <gray>died at</gray> <gold>"
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
                Bukkit.broadcast(CC.translate("<dark_green>[Death Coords] </dark_green>" + coordMsg));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("all", "team").filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}


