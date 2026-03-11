 package org.justme.justPlugin.commands.team;

import org.bukkit.Bukkit;
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

public class TeamCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TeamCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        TeamManager tm = plugin.getTeamManager();

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(CC.error("Usage: /team create <name>"));
                    return true;
                }
                if (tm.getPlayerTeam(player.getUniqueId()) != null) {
                    player.sendMessage(CC.error("You are already in a team! Leave first."));
                    return true;
                }
                if (tm.createTeam(args[1], player.getUniqueId())) {
                    player.sendMessage(CC.success("Team <yellow>" + args[1] + "</yellow> created!"));
                } else {
                    player.sendMessage(CC.error("A team with that name already exists!"));
                }
            }
            case "disband" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only the team leader can disband!"));
                    return true;
                }
                TeamManager.TeamData team = tm.getTeam(teamName);
                if (team != null) {
                    for (UUID memberUuid : team.members) {
                        Player member = Bukkit.getPlayer(memberUuid);
                        if (member != null) {
                            member.sendMessage(CC.warning("Your team has been disbanded!"));
                        }
                    }
                }
                tm.disbandTeam(teamName);
                player.sendMessage(CC.success("Team disbanded."));
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(CC.error("Usage: /team invite <player>"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only the team leader can invite!"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error("Player not found!"));
                    return true;
                }
                if (tm.getPlayerTeam(target.getUniqueId()) != null) {
                    player.sendMessage(CC.error("That player is already in a team!"));
                    return true;
                }
                tm.invitePlayer(teamName, target.getUniqueId());
                player.sendMessage(CC.success("Invited <yellow>" + target.getName() + "</yellow> to your team."));
                target.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> invited you to team <yellow>" + teamName + "</yellow>!"));
                target.sendMessage(CC.info("Type <green>/team join " + teamName + "</green> to accept."));
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(CC.error("Usage: /team join <name>"));
                    return true;
                }
                if (tm.getPlayerTeam(player.getUniqueId()) != null) {
                    player.sendMessage(CC.error("You are already in a team! Leave first."));
                    return true;
                }
                if (!tm.hasInvite(player.getUniqueId(), args[1])) {
                    player.sendMessage(CC.error("You don't have an invite to that team!"));
                    return true;
                }
                if (tm.joinTeam(args[1], player.getUniqueId())) {
                    player.sendMessage(CC.success("You joined team <yellow>" + args[1] + "</yellow>!"));
                    // Notify team
                    TeamManager.TeamData team = tm.getTeam(args[1]);
                    if (team != null) {
                        for (UUID memberUuid : team.members) {
                            Player member = Bukkit.getPlayer(memberUuid);
                            if (member != null && !member.equals(player)) {
                                member.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> joined the team!"));
                            }
                        }
                    }
                } else {
                    player.sendMessage(CC.error("Could not join that team!"));
                }
            }
            case "leave" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Leaders must disband the team. Use /team disband"));
                    return true;
                }
                tm.leaveTeam(player.getUniqueId());
                player.sendMessage(CC.success("You left the team."));
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage(CC.error("Usage: /team kick <player>"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only the team leader can kick!"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error("Player not found!"));
                    return true;
                }
                String targetTeam = tm.getPlayerTeam(target.getUniqueId());
                if (targetTeam == null || !targetTeam.equals(teamName)) {
                    player.sendMessage(CC.error("That player is not in your team!"));
                    return true;
                }
                if (tm.kickPlayer(teamName, target.getUniqueId())) {
                    player.sendMessage(CC.success("Kicked <yellow>" + target.getName() + "</yellow> from the team."));
                    target.sendMessage(CC.error("You have been kicked from the team!"));
                } else {
                    player.sendMessage(CC.error("Could not kick that player!"));
                }
            }
            case "info" -> {
                String teamName = args.length >= 2 ? args[1] : tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team! Specify a team name."));
                    return true;
                }
                TeamManager.TeamData team = tm.getTeam(teamName);
                if (team == null) {
                    player.sendMessage(CC.error("Team not found!"));
                    return true;
                }
                player.sendMessage(CC.info("<gold><bold>Team: " + team.name + "</bold></gold>"));
                Player leader = Bukkit.getPlayer(team.leader);
                player.sendMessage(CC.info("  Leader: <yellow>" + (leader != null ? leader.getName() : team.leader.toString())));
                player.sendMessage(CC.info("  Members (" + team.members.size() + "):"));
                for (UUID memberUuid : team.members) {
                    Player member = Bukkit.getPlayer(memberUuid);
                    String memberName = member != null ? member.getName() : memberUuid.toString();
                    String status = member != null ? "<green>Online" : "<red>Offline";
                    player.sendMessage(CC.info("    <gray>- <yellow>" + memberName + " " + status));
                }
            }
            case "list" -> {
                var teamNames = tm.getTeamNames();
                if (teamNames.isEmpty()) {
                    player.sendMessage(CC.info("No teams exist."));
                } else {
                    player.sendMessage(CC.info("<gold>Teams:</gold> <yellow>" + String.join(", ", teamNames)));
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.info("<gold><bold>Team Commands:</bold></gold>"));
        player.sendMessage(CC.info("  <yellow>/team create <name></yellow> <gray>- Create a team"));
        player.sendMessage(CC.info("  <yellow>/team disband</yellow> <gray>- Disband your team"));
        player.sendMessage(CC.info("  <yellow>/team invite <player></yellow> <gray>- Invite a player"));
        player.sendMessage(CC.info("  <yellow>/team join <name></yellow> <gray>- Join a team"));
        player.sendMessage(CC.info("  <yellow>/team leave</yellow> <gray>- Leave your team"));
        player.sendMessage(CC.info("  <yellow>/team kick <player></yellow> <gray>- Kick a player"));
        player.sendMessage(CC.info("  <yellow>/team info [name]</yellow> <gray>- View team info"));
        player.sendMessage(CC.info("  <yellow>/team list</yellow> <gray>- List all teams"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("create", "disband", "invite", "join", "leave", "kick", "info", "list")
                    .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "invite", "kick" -> Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                case "join", "info" -> plugin.getTeamManager().getTeamNames().stream()
                        .filter(n -> n.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                default -> List.of();
            };
        }
        return List.of();
    }
}

