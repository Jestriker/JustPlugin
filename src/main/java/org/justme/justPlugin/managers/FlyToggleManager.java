package org.justme.justPlugin.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyToggleManager {

    private final Set<UUID> flyingPlayers = new HashSet<>();

    public boolean toggleFly(org.bukkit.entity.Player player) {
        if (flyingPlayers.contains(player.getUniqueId())) {
            flyingPlayers.remove(player.getUniqueId());
            player.setAllowFlight(false);
            player.setFlying(false);
            return false; // fly off
        } else {
            flyingPlayers.add(player.getUniqueId());
            player.setAllowFlight(true);
            player.setFlying(true);
            return true; // fly on
        }
    }

    public boolean isFlyEnabled(UUID playerId) {
        return flyingPlayers.contains(playerId);
    }

    public void setFly(org.bukkit.entity.Player player, boolean fly) {
        if (fly) {
            flyingPlayers.add(player.getUniqueId());
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            flyingPlayers.remove(player.getUniqueId());
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }
}
