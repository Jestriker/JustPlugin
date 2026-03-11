package org.justme.justPlugin.managers;

import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    public static class Team {
        private final String name;
        private UUID owner;
        private final Set<UUID> members = new HashSet<>();
        private final Set<UUID> invited = new HashSet<>();

        public Team(String name, UUID owner) {
            this.name = name;
            this.owner = owner;
            this.members.add(owner);
        }

        public String getName() { return name; }
        public UUID getOwner() { return owner; }
        public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
        public Set<UUID> getInvited() { return invited; }

        public void addMember(UUID id) { members.add(id); invited.remove(id); }
        public void removeMember(UUID id) { members.remove(id); }
        public boolean isMember(UUID id) { return members.contains(id); }
        public void setOwner(UUID id) { this.owner = id; }
        public void addInvite(UUID id) { invited.add(id); }
        public void removeInvite(UUID id) { invited.remove(id); }
        public boolean isInvited(UUID id) { return invited.contains(id); }
    }

    // teamName -> Team
    private final Map<String, Team> teams = new HashMap<>();
    // playerUUID -> teamName
    private final Map<UUID, String> playerTeam = new HashMap<>();

    public boolean createTeam(String name, Player owner) {
        if (teams.containsKey(name.toLowerCase())) return false;
        if (playerTeam.containsKey(owner.getUniqueId())) return false;
        Team team = new Team(name.toLowerCase(), owner.getUniqueId());
        teams.put(name.toLowerCase(), team);
        playerTeam.put(owner.getUniqueId(), name.toLowerCase());
        return true;
    }

    public boolean disbandTeam(String name) {
        Team team = teams.remove(name.toLowerCase());
        if (team == null) return false;
        for (UUID id : team.getMembers()) {
            playerTeam.remove(id);
        }
        return true;
    }

    public Team getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    public Team getPlayerTeam(UUID playerId) {
        String name = playerTeam.get(playerId);
        if (name == null) return null;
        return teams.get(name);
    }

    public boolean joinTeam(String name, UUID playerId) {
        Team team = teams.get(name.toLowerCase());
        if (team == null || !team.isInvited(playerId)) return false;
        if (playerTeam.containsKey(playerId)) return false;
        team.addMember(playerId);
        playerTeam.put(playerId, name.toLowerCase());
        return true;
    }

    public boolean leaveTeam(UUID playerId) {
        String teamName = playerTeam.remove(playerId);
        if (teamName == null) return false;
        Team team = teams.get(teamName);
        if (team == null) return false;
        team.removeMember(playerId);
        if (team.getMembers().isEmpty()) {
            teams.remove(teamName);
        } else if (team.getOwner().equals(playerId)) {
            UUID newOwner = team.getMembers().iterator().next();
            team.setOwner(newOwner);
        }
        return true;
    }

    public Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public boolean isInTeam(UUID playerId) {
        return playerTeam.containsKey(playerId);
    }
}
