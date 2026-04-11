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
import org.justme.justPlugin.managers.MessageManager;
import org.justme.justPlugin.managers.TeamManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.InputValidator;

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
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        TeamManager tm = plugin.getTeamManager();
        MessageManager mm = plugin.getMessageManager();

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.error("team.create.usage"));
                    return true;
                }
                if (!InputValidator.isValidName(args[1])) {
                    player.sendMessage(CC.error(mm.raw("general.invalid-name")));
                    return true;
                }
                if (tm.getPlayerTeam(player.getUniqueId()) != null) {
                    player.sendMessage(mm.error("team.create.already-in-team"));
                    return true;
                }
                if (tm.createTeam(args[1], player.getUniqueId())) {
                    player.sendMessage(mm.success("team.create.success", "{team}", args[1]));
                } else {
                    player.sendMessage(mm.error("team.create.name-taken"));
                }
            }
            case "disband" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.disband.not-leader"));
                    return true;
                }
                TeamManager.TeamData team = tm.getTeam(teamName);
                if (team != null) {
                    for (UUID memberUuid : team.members) {
                        Player member = Bukkit.getPlayer(memberUuid);
                        if (member != null) {
                            member.sendMessage(mm.warning("team.disband.team-disbanded"));
                        }
                    }
                }
                tm.disbandTeam(teamName);
                player.sendMessage(mm.success("team.disband.success"));
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.error("team.invite.usage"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.invite.not-leader"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error(mm.raw("general.player-not-found")));
                    return true;
                }
                if (tm.getPlayerTeam(target.getUniqueId()) != null) {
                    player.sendMessage(mm.error("team.invite.target-already-in-team"));
                    return true;
                }
                tm.invitePlayer(teamName, target.getUniqueId());
                player.sendMessage(mm.success("team.invite.sent", "{player}", target.getName()));
                target.sendMessage(mm.info("team.invite.received", "{sender}", player.getName(), "{team}", teamName));
                boolean clickable = plugin.getConfig().getBoolean("clickable-commands.team", true);
                String joinCmd = CC.clickCmd("<green>/team join " + teamName + "</green>", "/team join " + teamName, clickable);
                target.sendMessage(mm.info("team.invite.received-hint", "{accept}", joinCmd));
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.error("team.join.usage"));
                    return true;
                }
                if (tm.getPlayerTeam(player.getUniqueId()) != null) {
                    player.sendMessage(mm.error("team.join.already-in-team"));
                    return true;
                }
                if (!tm.hasInvite(player.getUniqueId(), args[1])) {
                    player.sendMessage(mm.error("team.join.no-invite"));
                    return true;
                }
                if (tm.joinTeam(args[1], player.getUniqueId())) {
                    player.sendMessage(mm.success("team.join.success", "{team}", args[1]));
                    // Notify team
                    tm.notifyTeam(args[1], mm.raw("team.join.team-notify", "{player}", player.getName()), player.getUniqueId());
                } else {
                    player.sendMessage(mm.error("team.join.failed"));
                }
            }
            case "leave" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                // Notify team before leaving (so the player's UUID is still in the team)
                tm.notifyTeam(teamName, mm.raw("team.leave.team-notify", "{player}", player.getName()), player.getUniqueId());
                String result = tm.leaveTeam(player.getUniqueId());
                if (result == null) {
                    player.sendMessage(mm.error("team.leave.failed"));
                } else if ("disbanded".equals(result)) {
                    player.sendMessage(mm.success("team.leave.disbanded"));
                } else {
                    player.sendMessage(mm.success("team.leave.success"));
                }
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.error("team.kick.usage"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.kick.not-leader"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error(mm.raw("general.player-not-found")));
                    return true;
                }
                String targetTeam = tm.getPlayerTeam(target.getUniqueId());
                if (targetTeam == null || !targetTeam.equals(teamName)) {
                    player.sendMessage(mm.error("team.kick.player-not-member"));
                    return true;
                }
                if (tm.isLeader(target.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.kick.cannot-kick-leader"));
                    return true;
                }
                if (tm.kickPlayer(teamName, target.getUniqueId())) {
                    player.sendMessage(mm.success("team.kick.success", "{player}", target.getName()));
                    target.sendMessage(mm.error("team.kick.kicked-notify"));
                    tm.notifyTeam(teamName, mm.raw("team.kick.team-notify", "{player}", target.getName()));
                } else {
                    player.sendMessage(mm.error("team.kick.failed"));
                }
            }
            case "promote" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.error("team.promote.usage"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.promote.not-leader"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error(mm.raw("general.player-not-found")));
                    return true;
                }
                String result = tm.promoteToLeader(teamName, target.getUniqueId());
                if (result == null) {
                    player.sendMessage(mm.error("team.promote.team-not-found"));
                } else switch (result) {
                    case "not_in_team" -> player.sendMessage(mm.error("team.promote.not-in-team", "{player}", target.getName()));
                    case "already_leader" -> player.sendMessage(mm.error("team.promote.already-leader", "{player}", target.getName()));
                    case "success" -> {
                        player.sendMessage(mm.success("team.promote.success", "{player}", target.getName()));
                        target.sendMessage(mm.success("team.promote.promoted-notify"));
                        tm.notifyTeam(teamName, mm.raw("team.promote.team-notify", "{player}", target.getName()), player.getUniqueId(), target.getUniqueId());
                    }
                }
            }
            case "demote" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.error("team.demote.usage"));
                    return true;
                }
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.demote.not-leader"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.error(mm.raw("general.player-not-found")));
                    return true;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(mm.error("team.demote.cannot-demote-self"));
                    return true;
                }
                String result = tm.demoteFromLeader(teamName, target.getUniqueId());
                if (result == null) {
                    player.sendMessage(mm.error("team.demote.team-not-found"));
                } else switch (result) {
                    case "not_in_team" -> player.sendMessage(mm.error("team.demote.not-in-team", "{player}", target.getName()));
                    case "already_member" -> player.sendMessage(mm.error("team.demote.already-member", "{player}", target.getName()));
                    case "disbanded" -> player.sendMessage(mm.warning("team.demote.disbanded"));
                    case "success" -> {
                        player.sendMessage(mm.success("team.demote.success", "{player}", target.getName()));
                        target.sendMessage(mm.warning("team.demote.demoted-notify"));
                        tm.notifyTeam(teamName, mm.raw("team.demote.team-notify", "{player}", target.getName()), player.getUniqueId(), target.getUniqueId());
                    }
                }
            }
            case "sethome" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.sethome.not-leader"));
                    return true;
                }
                tm.setTeamHome(teamName, player.getLocation());
                player.sendMessage(mm.success("team.sethome.success"));
                tm.notifyTeam(teamName, mm.raw("team.sethome.team-notify", "{player}", player.getName()), player.getUniqueId());
            }
            case "delhome" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                if (!tm.isLeader(player.getUniqueId(), teamName)) {
                    player.sendMessage(mm.error("team.delhome.not-leader"));
                    return true;
                }
                if (tm.getTeamHome(teamName) == null) {
                    player.sendMessage(mm.error("team.delhome.not-set"));
                    return true;
                }
                tm.deleteTeamHome(teamName);
                player.sendMessage(mm.success("team.delhome.success"));
                tm.notifyTeam(teamName, mm.raw("team.delhome.team-notify", "{player}", player.getName()), player.getUniqueId());
            }
            case "home" -> {
                String teamName = tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.general.not-in-team"));
                    return true;
                }
                Location homeLoc = tm.getTeamHome(teamName);
                if (homeLoc == null) {
                    player.sendMessage(mm.error("team.home.not-set"));
                    if (tm.isLeader(player.getUniqueId(), teamName)) {
                        player.sendMessage(mm.info("team.home.sethome-hint"));
                    }
                    return true;
                }

                // Delay check (time between uses) - requires explicit delaybypass permission
                if (!player.hasPermission("justplugin.teamhome.delaybypass")
                        && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "teamhome")) {
                    int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "teamhome");
                    player.sendMessage(mm.error("general.cooldown-wait", "{time}", CooldownManager.formatTime(remaining)));
                    return true;
                }

                boolean initiated = plugin.getTeleportManager().teleportWithSafety(
                        player, homeLoc, "justplugin.teamhome.cooldownbypass", "teamhome", "justplugin.teamhome.unsafetp");
                if (initiated) {
                    plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "teamhome");
                    TeamManager.TeamData teamData = tm.getTeam(teamName);
                    player.sendMessage(mm.success("team.home.teleporting", "{team}", teamData != null ? teamData.name : teamName));
                }
            }
            case "info" -> {
                String teamName = args.length >= 2 ? args[1] : tm.getPlayerTeam(player.getUniqueId());
                if (teamName == null) {
                    player.sendMessage(mm.error("team.info.not-in-team"));
                    return true;
                }
                TeamManager.TeamData team = tm.getTeam(teamName);
                if (team == null) {
                    player.sendMessage(mm.error("team.info.team-not-found"));
                    return true;
                }
                player.sendMessage(mm.info("team.info.header", "{team}", team.name));
                // Show leaders
                StringBuilder leaderNames = new StringBuilder();
                for (UUID leaderUuid : team.leaders) {
                    Player leader = Bukkit.getPlayer(leaderUuid);
                    String leaderName = leader != null ? leader.getName() : Bukkit.getOfflinePlayer(leaderUuid).getName();
                    if (leaderName == null) leaderName = leaderUuid.toString();
                    if (!leaderNames.isEmpty()) leaderNames.append(", ");
                    leaderNames.append(leaderName);
                }
                player.sendMessage(mm.info("team.info.leaders", "{players}", leaderNames.toString()));
                player.sendMessage(mm.info("team.info.members-header", "{count}", String.valueOf(team.members.size())));
                for (UUID memberUuid : team.members) {
                    Player member = Bukkit.getPlayer(memberUuid);
                    String memberName = member != null ? member.getName() : Bukkit.getOfflinePlayer(memberUuid).getName();
                    if (memberName == null) memberName = memberUuid.toString();
                    String status = member != null ? mm.raw("team.info.member-online") : mm.raw("team.info.member-offline");
                    String role = team.leaders.contains(memberUuid) ? mm.raw("team.info.member-role-leader") : "";
                    player.sendMessage(mm.info("team.info.member-entry", "{player}", memberName, "{status}", status, "{role}", role));
                }
                if (team.home != null) {
                    player.sendMessage(mm.info("team.info.home-location",
                            "{x}", String.valueOf(team.home.getBlockX()),
                            "{y}", String.valueOf(team.home.getBlockY()),
                            "{z}", String.valueOf(team.home.getBlockZ()),
                            "{world}", team.home.getWorld() != null ? team.home.getWorld().getName() : "?"));
                }
            }
            case "list" -> {
                if (!player.hasPermission("justplugin.team.list")) {
                    player.sendMessage(mm.error("team.list.no-permission"));
                    return true;
                }
                var teamNames = tm.getTeamNames();
                if (teamNames.isEmpty()) {
                    player.sendMessage(mm.info("team.list.empty"));
                } else {
                    boolean c = plugin.getConfig().getBoolean("clickable-commands.team", true);
                    String teamList = teamNames.stream()
                            .map(n -> CC.clickCmd("<yellow>" + n + "</yellow>", "/team info " + n, c))
                            .collect(Collectors.joining("<gray>, "));
                    player.sendMessage(mm.info("team.list.header", "{teams}", teamList));
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        MessageManager mm = plugin.getMessageManager();
        boolean c = plugin.getConfig().getBoolean("clickable-commands.team", true);
        player.sendMessage(mm.info("team.help.header"));
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
