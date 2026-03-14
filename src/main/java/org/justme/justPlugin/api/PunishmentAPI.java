package org.justme.justPlugin.api;

import java.util.List;
import java.util.UUID;

/**
 * Punishment API for external plugins.
 * Allows checking and managing bans, mutes, and warnings.
 */
public interface PunishmentAPI {
    // --- Bans ---
    boolean isBanned(UUID uuid);
    void ban(UUID uuid, String playerName, String reason, String bannedBy);
    void tempBan(UUID uuid, String playerName, String reason, String bannedBy, long durationMs);
    boolean unban(UUID uuid);

    // --- Mutes ---
    boolean isMuted(UUID uuid);
    void mute(UUID uuid, String playerName, String reason, String mutedBy);
    void tempMute(UUID uuid, String playerName, String reason, String mutedBy, long durationMs);
    boolean unmute(UUID uuid);
    String getMuteReason(UUID uuid);

    // --- Warnings ---
    int getActiveWarnCount(UUID uuid);
    int getTotalWarnCount(UUID uuid);
    void addWarn(UUID uuid, String playerName, String reason, String warnedBy);
    boolean liftWarn(UUID uuid, int index, String liftedBy, String reason);
}

