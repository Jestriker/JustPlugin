package org.justme.justPlugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.util.*;

public class TeamManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;

    // In-memory cache
    // teamName -> TeamData
    private final Map<String, TeamData> teams = new LinkedHashMap<>();
    // player UUID -> team name
    private final Map<UUID, String> playerTeams = new HashMap<>();

    public TeamManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        loadTeams();
    }

    public static class TeamData {
        public String name;
        public UUID leader;
        public Set<UUID> members = new HashSet<>();
        public Set<UUID> invites = new HashSet<>();

        public TeamData(String name, UUID leader) {
            this.name = name;
            this.leader = leader;
            this.members.add(leader);
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
            UUID leader = UUID.fromString(ts.getString("leader", ""));
            TeamData team = new TeamData(name, leader);
            List<String> memberList = ts.getStringList("members");
            for (String m : memberList) {
                UUID memberUuid = UUID.fromString(m);
                team.members.add(memberUuid);
                playerTeams.put(memberUuid, name);
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

    public boolean leaveTeam(UUID player) {
        String teamName = playerTeams.get(player);
        if (teamName == null) return false;
        TeamData team = teams.get(teamName);
        if (team == null) return false;
        if (team.leader.equals(player)) return false; // Leader can't leave, must disband
        team.members.remove(player);
        playerTeams.remove(player);
        saveTeams();
        return true;
    }

    public boolean kickPlayer(String teamName, UUID player) {
        TeamData team = teams.get(teamName.toLowerCase());
        if (team == null) return false;
        if (team.leader.equals(player)) return false;
        team.members.remove(player);
        playerTeams.remove(player);
        saveTeams();
        return true;
    }

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
        return team != null && team.leader.equals(player);
    }

    public boolean hasInvite(UUID player, String teamName) {
        TeamData team = teams.get(teamName.toLowerCase());
        return team != null && team.invites.contains(player);
    }

    private void saveTeams() {
        YamlConfiguration config = dataManager.getTeamsConfig();
        config.set("teams", null);
        for (Map.Entry<String, TeamData> entry : teams.entrySet()) {
            String path = "teams." + entry.getKey();
            TeamData team = entry.getValue();
            config.set(path + ".name", team.name);
            config.set(path + ".leader", team.leader.toString());
            List<String> members = new ArrayList<>();
            for (UUID member : team.members) {
                members.add(member.toString());
            }
            config.set(path + ".members", members);
        }
        dataManager.saveTeams();
    }

    public void reload() {
        dataManager.reloadTeams();
        loadTeams();
    }
}

