package org.justme.justPlugin.commands.team;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.managers.TeamManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
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
                    player.sendMessage(CC.error("Only team leaders can disband!"));
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
                    player.sendMessage(CC.error("Only team leaders can invite!"));
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
                boolean clickable = plugin.getConfig().getBoolean("clickable-commands.team", true);
                String joinCmd = CC.clickCmd("<green>/team join " + teamName + "</green>", "/team join " + teamName, clickable);
                target.sendMessage(CC.info("Type " + joinCmd + " to accept."));
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
                    tm.notifyTeam(args[1], "<yellow>" + player.getName() + "</yellow> joined the team!", player.getUniqueId());
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
                // Notify team before leaving (so the player's UUID is still in the team)
                tm.notifyTeam(teamName, "<yellow>" + player.getName() + "</yellow> left the team.", player.getUniqueId());
                String result = tm.leaveTeam(player.getUniqueId());
                if (result == null) {
                    player.sendMessage(CC.error("Could not leave the team!"));
                } else if ("disbanded".equals(result)) {
                    player.sendMessage(CC.success("You left the team. The team was disbanded because no leaders remain."));
                } else {
                    player.sendMessage(CC.success("You left the team."));
                }
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
                    player.sendMessage(CC.error("Only team leaders can kick!"));
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
                if (tm.isLeader(target.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("You cannot kick a team leader. Demote them first."));
                    return true;
                }
                if (tm.kickPlayer(teamName, target.getUniqueId())) {
                    player.sendMessage(CC.success("Kicked <yellow>" + target.getName() + "</yellow> from the team."));
                    target.sendMessage(CC.error("You have been kicked from the team!"));
                    tm.notifyTeam(teamName, "<yellow>" + target.getName() + "</yellow> was kicked from the team.");
                } else {
                    player.sendMessage(CC.error("Could not kick that player!"));
                }
            }
            case "promote" -> {
                if (args.length < 2) {
                    player.sendMessage(CC.error("Usage: /team promote <player>"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only team leaders can promote members!"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error("Player not found!"));
                    return true;
                }
                String result = tm.promoteToLeader(teamName, target.getUniqueId());
                if (result == null) {
                    player.sendMessage(CC.error("Team not found!"));
                } else switch (result) {
                    case "not_in_team" -> player.sendMessage(CC.error("That player is not in your team!"));
                    case "already_leader" -> player.sendMessage(CC.error("<yellow>" + target.getName() + "</yellow> is already a team leader!"));
                    case "success" -> {
                        player.sendMessage(CC.success("Promoted <yellow>" + target.getName() + "</yellow> to team leader!"));
                        target.sendMessage(CC.success("You have been promoted to <gold>team leader</gold>!"));
                        tm.notifyTeam(teamName, "<yellow>" + target.getName() + "</yellow> was promoted to team leader!", player.getUniqueId(), target.getUniqueId());
                    }
                }
            }
            case "demote" -> {
                if (args.length < 2) {
                    player.sendMessage(CC.error("Usage: /team demote <player>"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only team leaders can demote!"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error("Player not found!"));
                    return true;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(CC.error("You cannot demote yourself! Use <yellow>/team leave</yellow> instead."));
                    return true;
                }
                String result = tm.demoteFromLeader(teamName, target.getUniqueId());
                if (result == null) {
                    player.sendMessage(CC.error("Team not found!"));
                } else switch (result) {
                    case "not_in_team" -> player.sendMessage(CC.error("That player is not in your team!"));
                    case "already_member" -> player.sendMessage(CC.error("<yellow>" + target.getName() + "</yellow> is already a regular member and cannot be demoted further."));
                    case "disbanded" -> player.sendMessage(CC.warning("The team was disbanded because there are no remaining leaders."));
                    case "success" -> {
                        player.sendMessage(CC.success("Demoted <yellow>" + target.getName() + "</yellow> to member."));
                        target.sendMessage(CC.warning("You have been demoted to regular member."));
                        tm.notifyTeam(teamName, "<yellow>" + target.getName() + "</yellow> was demoted to member.", player.getUniqueId(), target.getUniqueId());
                    }
                }
            }
            case "sethome" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only team leaders can set the team home!"));
                    return true;
                }
                tm.setTeamHome(teamName, player.getLocation());
                player.sendMessage(CC.success("Team home has been set at your current location!"));
                tm.notifyTeam(teamName, "<yellow>" + player.getName() + "</yellow> set the team home.", player.getUniqueId());
            }
            case "delhome" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(CC.error("Only team leaders can delete the team home!"));
                    return true;
                }
                if (tm.getTeamHome(teamName) == null) {
                    player.sendMessage(CC.error("Your team does not have a home set!"));
                    return true;
                }
                tm.deleteTeamHome(teamName);
                player.sendMessage(CC.success("Team home has been deleted."));
                tm.notifyTeam(teamName, "<yellow>" + player.getName() + "</yellow> deleted the team home.", player.getUniqueId());
            }
            case "home" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(CC.error("You are not in a team!"));
                    return true;
                }
                Location homeLoc = tm.getTeamHome(teamName);
                if (homeLoc == null) {
                    player.sendMessage(CC.error("Your team does not have a home set!"));
                    if (tm.isLeader(player.getUniqueId(), teamName)) {
                        player.sendMessage(CC.info("Use <yellow>/team sethome</yellow> to set one."));
                    }
                    return true;
                }

                // Delay check (time between uses) — OPs auto-skip
                if (!player.isOp() && !player.hasPermission("justplugin.teamhome.delaybypass")
                        && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "teamhome")) {
                    int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "teamhome");
                    player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
                    return true;
                }

                boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                        player, homeLoc, "justplugin.teamhome.cooldownbypass", "teamhome", "justplugin.teamhome.unsafetp");
                if (initiated) {
                    plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "teamhome");
                    TeamManager.TeamData teamData = tm.getTeam(teamName);
                    player.sendMessage(CC.success("Teleporting to team <yellow>" + (teamData != null ? teamData.name : teamName) + "</yellow>'s home."));
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
                // Show leaders
                StringBuilder leaderNames = new StringBuilder();
                for (UUID leaderUuid : team.leaders) {
                    Player leader = Bukkit.getPlayer(leaderUuid);
                    String leaderName = leader != null ? leader.getName() : Bukkit.getOfflinePlayer(leaderUuid).getName();
                    if (leaderName == null) leaderName = leaderUuid.toString();
                    if (!leaderNames.isEmpty()) leaderNames.append(", ");
                    leaderNames.append(leaderName);
                }
                player.sendMessage(CC.info("  Leaders: <yellow>" + leaderNames));
                player.sendMessage(CC.info("  Members (" + team.members.size() + "):"));
                for (UUID memberUuid : team.members) {
                    Player member = Bukkit.getPlayer(memberUuid);
                    String memberName = member != null ? member.getName() : Bukkit.getOfflinePlayer(memberUuid).getName();
                    if (memberName == null) memberName = memberUuid.toString();
                    String status = member != null ? "<green>Online" : "<red>Offline";
                    String role = team.leaders.contains(memberUuid) ? " <gold>[Leader]" : "";
                    player.sendMessage(CC.info("    <gray>- <yellow>" + memberName + " " + status + role));
                }
                if (team.home != null) {
                    player.sendMessage(CC.info("  Home: <yellow>" + team.home.getBlockX() + ", " + team.home.getBlockY() + ", " + team.home.getBlockZ()
                            + " <gray>(" + (team.home.getWorld() != null ? team.home.getWorld().getName() : "?") + ")"));
                }
            }
            case "list" -> {
                if (!player.hasPermission("justplugin.team.list")) {
                    player.sendMessage(CC.error("You don't have permission to list all teams."));
                    return true;
                }
                var teamNames = tm.getTeamNames();
                if (teamNames.isEmpty()) {
                    player.sendMessage(CC.info("No teams exist."));
                } else {
                    boolean c = plugin.getConfig().getBoolean("clickable-commands.team", true);
                    String teamList = teamNames.stream()
                            .map(n -> CC.clickCmd("<yellow>" + n + "</yellow>", "/team info " + n, c))
                            .collect(Collectors.joining("<gray>, "));
                    player.sendMessage(CC.translate(CC.PREFIX + "<gold>Teams:</gold> " + teamList));
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        boolean c = plugin.getConfig().getBoolean("clickable-commands.team", true);
        player.sendMessage(CC.info("<gold><bold>Team Commands:</bold></gold>"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team create <name></yellow>", "/team create ", c) + " <gray>- Create a team"));
        player.sendMessage(CC.translate("  " + CC.clickCmd("<yellow>/team disband</yellow>", "/team disband", c) + " <gray>- Disband your team"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team invite <player></yellow>", "/team invite ", c) + " <gray>- Invite a player"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team join <name></yellow>", "/team join ", c) + " <gray>- Join a team"));
        player.sendMessage(CC.translate("  " + CC.clickCmd("<yellow>/team leave</yellow>", "/team leave", c) + " <gray>- Leave your team"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team kick <player></yellow>", "/team kick ", c) + " <gray>- Kick a player"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team promote <player></yellow>", "/team promote ", c) + " <gray>- Promote to leader"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team demote <player></yellow>", "/team demote ", c) + " <gray>- Demote a leader"));
        player.sendMessage(CC.translate("  " + CC.clickCmd("<yellow>/team sethome</yellow>", "/team sethome", c) + " <gray>- Set team home"));
        player.sendMessage(CC.translate("  " + CC.clickCmd("<yellow>/team delhome</yellow>", "/team delhome", c) + " <gray>- Delete team home"));
        player.sendMessage(CC.translate("  " + CC.clickCmd("<yellow>/team home</yellow>", "/team home", c) + " <gray>- Teleport to team home"));
        player.sendMessage(CC.translate("  " + CC.suggestCmd("<yellow>/team info [name]</yellow>", "/team info ", c) + " <gray>- View team info"));
        player.sendMessage(CC.translate("  " + CC.clickCmd("<yellow>/team list</yellow>", "/team list", c) + " <gray>- List all teams"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("create", "disband", "invite", "join", "leave", "kick", "promote", "demote", "sethome", "delhome", "home", "info", "list")
                    .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "invite", "kick", "promote", "demote" -> Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                case "join", "info" -> plugin.getTeamManager().getTeamNames().stream()
                        .filter(n -> n.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                default -> List.of();
            };
        }
        return List.of();
    }
}

