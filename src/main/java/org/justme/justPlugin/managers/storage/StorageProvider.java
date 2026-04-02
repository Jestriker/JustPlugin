package org.justme.justPlugin.managers.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Abstraction layer for all persistent data storage.
 * Implementations may use flat files (YAML), SQLite, or MySQL.
 */
public interface StorageProvider {

    /**
     * Initialize the storage backend (create files/tables, open connections).
     */
    void init() throws Exception;

    /**
     * Gracefully shut down the storage backend (close connections, flush buffers).
     */
    void shutdown();

    // --- Player Data ---

    Map<String, Object> getPlayerData(UUID uuid);

    void savePlayerData(UUID uuid, Map<String, Object> data);

    Set<UUID> getAllPlayerUUIDs();

    // --- Warps ---

    Map<String, Map<String, Object>> getAllWarps();

    void saveWarp(String name, Map<String, Object> data);

    void deleteWarp(String name);

    // --- Bans ---

    Map<String, Map<String, Object>> getAllBans();

    void saveBan(String key, Map<String, Object> data);

    void deleteBan(String key);

    // --- Teams ---

    Map<String, Map<String, Object>> getAllTeams();

    void saveTeam(String name, Map<String, Object> data);

    void deleteTeam(String name);

    /**
     * Returns the human-readable name of this storage type (e.g. "yaml", "sqlite", "mysql").
     */
    String getType();
}
