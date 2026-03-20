package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;

public class TeamManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;

    // In-memory cache
    // teamName (lowercase) -> TeamData
    private final Map<String, TeamData> teams = new LinkedHashMap<>();
    // player UUID -> team name (lowercase)
    private final Map<UUID, String> playerTeams = new HashMap<>();

    public TeamManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        loadTeams();
    }

    public static class TeamData {
        public String name;
        public Set<UUID> leaders = new HashSet<>();
        public Set<UUID> members = new HashSet<>();
        public Set<UUID> invites = new HashSet<>();
        public Location home; // nullable

        public TeamData(String name, UUID creator) {
            this.name = name;
            this.leaders.add(creator);
            this.members.add(creator);
        }
    }

    private void loadTeams() {
        teams.clear();
        playerTeams.clear();
        YamlConfiguration config = dataManager.getTeamsConfig();
        ConfigurationSection section = config.getConfigurationSection("teams");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            ConfigurationSection ts = section.getConfigurationSection(name);
            if (ts == null) continue;

            // Backward compatibility: single "leader" key or new "leaders" list
            TeamData team;
            if (ts.contains("leaders")) {
                List<String> leaderList = ts.getStringList("leaders");
                team = new TeamData(name, UUID.fromString(leaderList.get(0)));
                team.leaders.clear();
                for (String l : leaderList) {
                    team.leaders.add(UUID.fromString(l));
                }
            } else {
                UUID leader = UUID.fromString(ts.getString("leader", ""));
                team = new TeamData(name, leader);
            }

            team.members.clear();
            List<String> memberList = ts.getStringList("members");
            for (String m : memberList) {
                UUID memberUuid = UUID.fromString(m);
                team.members.add(memberUuid);
                playerTeams.put(memberUuid, name.toLowerCase());
            }

            // Load team home
            ConfigurationSection homeSection = ts.getConfigurationSection("home");
            if (homeSection != null) {
                String worldName = homeSection.getString("world");
                if (worldName != null && Bukkit.getWorld(worldName) != null) {
                    team.home = new Location(
                            Bukkit.getWorld(worldName),
                            homeSection.getDouble("x"),
                            homeSection.getDouble("y"),
                            homeSection.getDouble("z"),
                            (float) homeSection.getDouble("yaw"),
                            (float) homeSection.getDouble("pitch")
                    );
                }
            }

            teams.put(name.toLowerCase(), team);
        }
    }

    public boolean createTeam(String name, UUID leader) {
        if (teams.containsKey(name.toLowerCase())) return false;
        if (playerTeams.containsKey(leader)) return false;
        TeamData team = new TeamData(name, leader);
        teams.put(name.toLowerCase(), team);
        playerTeams.put(leader, name.toLowerCase());
        saveTeams();
        return true;
    }

    /**
     * Disbands a team.
     */
    public boolean disbandTeam(String name) {
        TeamData team = teams.remove(name.toLowerCase());
        if (team == null) return false;
        for (UUID member : team.members) {
            playerTeams.remove(member);
        }
        saveTeams();
        return true;
    }

    public boolean invitePlayer(String teamName, UUID player) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return false;
        team.invites.add(player);
        return true;
    }

    public boolean joinTeam(String teamName, UUID player) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return false;
        if (!team.invites.contains(player)) return false;
        if (playerTeams.containsKey(player)) return false;
        team.invites.remove(player);
        team.members.add(player);
        playerTeams.put(player, teamName.toLowerCase());
        saveTeams();
        return true;
    }

    /**
     * Player leaves the team. If they were the last leader and members remain,
     * the team is auto-disbanded.
     * @return "left" if successfully left, "disbanded" if the team was auto-disbanded, null if failed
     */
    public String leaveTeam(UUID player) {
        String teamName = playerTeams.get(player);
        if (teamName == null) return null;
        TeamData team = teams.get(teamName);
        if (team == null) return null;

        team.members.remove(player);
        team.leaders.remove(player);
        playerTeams.remove(player);

        // If no members left, just remove the team silently
        if (team.members.isEmpty()) {
            teams.remove(teamName);
            saveTeams();
            return "left";
        }

        // If no leaders remain, auto-disband
        if (team.leaders.isEmpty()) {
            for (UUID memberUuid : team.members) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null) {
                    member.sendMessage(CC.warning("Your team has been automatically disbanded because the last leader left."));
                }
            }
            for (UUID memberUuid : team.members) {
                playerTeams.remove(memberUuid);
            }
            teams.remove(teamName);
            saveTeams();
            return "disbanded";
        }

        saveTeams();
        return "left";
    }

    public boolean kickPlayer(String teamName, UUID player) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return false;
        if (team.leaders.contains(player)) return false; // Can't kick leaders
        team.members.remove(player);
        playerTeams.remove(player);
        saveTeams();
        return true;
    }

    /**
     * Promote a member to leader.
     * @return "success", "already_leader", "not_in_team", or null if team doesn't exist
     */
    public String promoteToLeader(String teamName, UUID player) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return null;
        if (!team.members.contains(player)) return "not_in_team";
        if (team.leaders.contains(player)) return "already_leader";
        team.leaders.add(player);
        saveTeams();
        return "success";
    }

    /**
     * Demote a leader back to member.
     * @return "success", "already_member", "not_in_team", "disbanded" (if no leaders remain), or null
     */
    public String demoteFromLeader(String teamName, UUID player) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return null;
        if (!team.members.contains(player)) return "not_in_team";
        if (!team.leaders.contains(player)) return "already_member";
        team.leaders.remove(player);

        // If no leaders remain, auto-disband
        if (team.leaders.isEmpty()) {
            for (UUID memberUuid : team.members) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null) {
                    member.sendMessage(CC.warning("Your team has been automatically disbanded because there are no remaining leaders."));
                }
            }
            for (UUID memberUuid : team.members) {
                playerTeams.remove(memberUuid);
            }
            teams.remove(teamName.toLowerCase());
            saveTeams();
            return "disbanded";
        }

        saveTeams();
        return "success";
    }

    // --- Team Home ---
    public void setTeamHome(String teamName, Location loc) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return;
        team.home = loc.clone();
        saveTeams();
    }

    public void deleteTeamHome(String teamName) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return;
        team.home = null;
        saveTeams();
    }

    public Location getTeamHome(String teamName) {
        TeamData team = teams.get(teamName.toLowerCase());
        return team != null ? team.home : null;
    }

    // --- Queries ---
    public String getPlayerTeam(UUID player) {
        return playerTeams.get(player);
    }

    public TeamData getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    public Set<String> getTeamNames() {
        return Collections.unmodifiableSet(teams.keySet());
    }

    public boolean isLeader(UUID player, String teamName) {
        TeamData team = teams.get(teamName.toLowerCase());
        return team != null && team.leaders.contains(player);
    }

    public boolean hasInvite(UUID player, String teamName) {
        TeamData team = teams.get(teamName.toLowerCase());
        return team != null && team.invites.contains(player);
    }

    /**
     * Notify all online team members (optionally excluding some UUIDs).
     */
    public void notifyTeam(String teamName, String miniMessage, UUID... exclude) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return;
        Set<UUID> excluded = Set.of(exclude);
        for (UUID memberUuid : team.members) {
            if (excluded.contains(memberUuid)) continue;
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null) {
                member.sendMessage(CC.info(miniMessage));
            }
        }
    }

    private void saveTeams() {
        YamlConfiguration config = dataManager.getTeamsConfig();
        config.set("teams", null);
        for (Map.Entry<String, TeamData> entry : teams.entrySet()) {
            String path = "teams." + entry.getKey();
            TeamData team = entry.getValue();
            config.set(path + ".name", team.name);

            // Save leaders as list
            List<String> leaders = new ArrayList<>();
            for (UUID leader : team.leaders) {
                leaders.add(leader.toString());
            }
            config.set(path + ".leaders", leaders);

            // Save members as list
            List<String> members = new ArrayList<>();
            for (UUID member : team.members) {
                members.add(member.toString());
            }
            config.set(path + ".members", members);

            // Save home
            if (team.home != null && team.home.getWorld() != null) {
                config.set(path + ".home.world", team.home.getWorld().getName());
                config.set(path + ".home.x", team.home.getX());
                config.set(path + ".home.y", team.home.getY());
                config.set(path + ".home.z", team.home.getZ());
                config.set(path + ".home.yaw", team.home.getYaw());
                config.set(path + ".home.pitch", team.home.getPitch());
            } else {
                config.set(path + ".home", null);
            }
        }
        dataManager.saveTeams();
    }

    public void reload() {
        dataManager.reloadTeams();
        loadTeams();
    }
}
