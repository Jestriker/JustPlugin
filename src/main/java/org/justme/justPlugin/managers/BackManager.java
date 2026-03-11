package org.justme.justPlugin.managers;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackManager {

    private final Map<UUID, Location> lastDeathLocations = new HashMap<>();
    private final Map<UUID, Location> lastTeleportLocations = new HashMap<>();

    public void setDeathLocation(UUID playerId, Location location) {
        lastDeathLocations.put(playerId, location.clone());
    }

    public Location getDeathLocation(UUID playerId) {
        return lastDeathLocations.get(playerId);
    }

    public void setTeleportLocation(UUID playerId, Location location) {
        lastTeleportLocations.put(playerId, location.clone());
    }

    public Location getTeleportLocation(UUID playerId) {
        return lastTeleportLocations.get(playerId);
    }

    public Location getLastLocation(UUID playerId) {
        Location tp = lastTeleportLocations.get(playerId);
        Location death = lastDeathLocations.get(playerId);
        if (tp == null) return death;
        if (death == null) return tp;
        // return most recent — in this simple implementation, just return tp first
        return tp;
    }

    public void clearLocations(UUID playerId) {
        lastDeathLocations.remove(playerId);
        lastTeleportLocations.remove(playerId);
    }
}
