package org.justme.justPlugin.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodManager {

    private final Set<UUID> godMode = new HashSet<>();

    public boolean toggleGod(UUID playerId) {
        if (godMode.contains(playerId)) {
            godMode.remove(playerId);
            return false; // god off
        } else {
            godMode.add(playerId);
            return true; // god on
        }
    }

    public boolean isGod(UUID playerId) {
        return godMode.contains(playerId);
    }

    public void setGod(UUID playerId, boolean god) {
        if (god) godMode.add(playerId);
        else godMode.remove(playerId);
    }
}
