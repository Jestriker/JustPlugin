package org.justme.justPlugin.api;

import java.util.UUID;

/**
 * Vanish API for external plugins.
 * Allows checking if a player is vanished.
 */
public interface VanishAPI {
    boolean isVanished(UUID uuid);
    boolean isSuperVanished(UUID uuid);
}

