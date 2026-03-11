package org.justme.justPlugin.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IgnoreManager {

    // ignorer -> set of ignored player UUIDs
    private final Map<UUID, Set<UUID>> ignoreMap = new HashMap<>();

    public void ignore(UUID ignorer, UUID ignored) {
        ignoreMap.computeIfAbsent(ignorer, k -> new HashSet<>()).add(ignored);
    }

    public void unignore(UUID ignorer, UUID ignored) {
        Set<UUID> set = ignoreMap.get(ignorer);
        if (set != null) set.remove(ignored);
    }

    public boolean isIgnoring(UUID ignorer, UUID ignored) {
        Set<UUID> set = ignoreMap.get(ignorer);
        return set != null && set.contains(ignored);
    }

    public boolean toggleIgnore(UUID ignorer, UUID ignored) {
        if (isIgnoring(ignorer, ignored)) {
            unignore(ignorer, ignored);
            return false; // now unignored
        } else {
            ignore(ignorer, ignored);
            return true; // now ignored
        }
    }
}
