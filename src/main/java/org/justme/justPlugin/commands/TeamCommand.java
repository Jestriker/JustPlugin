package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeamManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {

    private final TeamManager teamManager;

    public TeamCommand(JustPlugin plugin) {
        this.teamManager = plugin.getTeamManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§aTeam commands: §ecreate, invite, join, leave, disband, info, list");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) { player.sendMessage("§cUsage: /team create <name>"); return true; }
                if (teamManager.isInTeam(player.getUniqueId())) { player.sendMessage("§cYou are already in a team."); return true; }
                if (teamManager.createTeam(args[1], player)) {
                    player.sendMessage("§aTeam §e" + args[1] + " §acreated!");
                } else {
                    player.sendMessage("§cA team with that name already exists.");
                }
            }
            case "invite" -> {
                TeamManager.Team team = teamManager.getPlayerTeam(player.getUniqueId());
                if (team == null) { player.sendMessage("§cYou are not in a team."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { player.sendMessage("§cOnly the team owner can invite players."); return true; }
                if (args.length < 2) { player.sendMessage("§cUsage: /team invite <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) { player.sendMessage("§cPlayer not found."); return true; }
                team.addInvite(target.getUniqueId());
                player.sendMessage("§aInvited §e" + target.getName() + " §ato your team.");
                target.sendMessage("§e" + player.getName() + " §ainvited you to team §e" + team.getName() + "§a. Use §e/team join " + team.getName() + " §ato accept.");
            }
            case "join" -> {
                if (args.length < 2) { player.sendMessage("§cUsage: /team join <name>"); return true; }
                if (teamManager.isInTeam(player.getUniqueId())) { player.sendMessage("§cYou are already in a team."); return true; }
                TeamManager.Team team = teamManager.getTeam(args[1]);
                if (team == null) { player.sendMessage("§cTeam not found."); return true; }
                if (!team.isInvited(player.getUniqueId())) { player.sendMessage("§cYou have not been invited to this team."); return true; }
                teamManager.joinTeam(args[1], player.getUniqueId());
                for (java.util.UUID memberId : team.getMembers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null) member.sendMessage("§e" + player.getName() + " §ajoined the team!");
                }
            }
            case "leave" -> {
                if (!teamManager.isInTeam(player.getUniqueId())) { player.sendMessage("§cYou are not in a team."); return true; }
                TeamManager.Team team = teamManager.getPlayerTeam(player.getUniqueId());
                String teamName = team != null ? team.getName() : "Unknown";
                teamManager.leaveTeam(player.getUniqueId());
                player.sendMessage("§aYou left team §e" + teamName + "§a.");
                if (team != null) {
                    for (java.util.UUID memberId : team.getMembers()) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null) member.sendMessage("§e" + player.getName() + " §cleft the team.");
                    }
                }
            }
            case "disband" -> {
                TeamManager.Team team = teamManager.getPlayerTeam(player.getUniqueId());
                if (team == null) { player.sendMessage("§cYou are not in a team."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { player.sendMessage("§cOnly the team owner can disband the team."); return true; }
                String teamName = team.getName();
                for (java.util.UUID memberId : new java.util.HashSet<>(team.getMembers())) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null) member.sendMessage("§cTeam §e" + teamName + " §chas been disbanded.");
                }
                teamManager.disbandTeam(teamName);
            }
            case "info" -> {
                TeamManager.Team team = teamManager.getPlayerTeam(player.getUniqueId());
                if (team == null) { player.sendMessage("§cYou are not in a team."); return true; }
                Player owner = Bukkit.getPlayer(team.getOwner());
                String ownerName = owner != null ? owner.getName() : team.getOwner().toString();
                player.sendMessage("§aTeam: §e" + team.getName());
                player.sendMessage("§aOwner: §e" + ownerName);
                List<String> members = team.getMembers().stream()
                        .map(id -> { Player p = Bukkit.getPlayer(id); return p != null ? p.getName() : id.toString(); })
                        .collect(Collectors.toList());
                player.sendMessage("§aMembers (" + members.size() + "): §e" + String.join(", ", members));
            }
            case "list" -> {
                if (teamManager.getAllTeams().isEmpty()) {
                    player.sendMessage("§cNo teams exist.");
                } else {
                    player.sendMessage("§aTeams:");
                    for (TeamManager.Team t : teamManager.getAllTeams()) {
                        player.sendMessage("§e - " + t.getName() + " §a(" + t.getMembers().size() + " members)");
                    }
                }
            }
            default -> player.sendMessage("§cUnknown subcommand. Use: create, invite, join, leave, disband, info, list");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "join", "leave", "disband", "info", "list").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("invite"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
