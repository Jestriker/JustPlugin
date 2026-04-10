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

    // --- Jails ---

    Map<String, Map<String, Object>> getAllJails();

    void saveJail(String name, Map<String, Object> data);

    void deleteJail(String name);

    // --- Kits ---

    Map<String, Map<String, Object>> getAllKits();

    void saveKit(String name, Map<String, Object> data);

    void deleteKit(String name);

    // --- Mutes ---

    Map<String, Map<String, Object>> getAllMutes();

    void saveMute(String key, Map<String, Object> data);

    void deleteMute(String key);

    // --- Warns ---

    Map<String, Map<String, Object>> getAllWarns();

    void saveWarn(String key, Map<String, Object> data);

    void deleteWarn(String key);

    // --- Mail ---

    Map<String, Map<String, Object>> getAllMail();

    void saveMail(String key, Map<String, Object> data);

    void deleteMail(String key);

    // --- Homes ---

    Map<String, Map<String, Object>> getAllHomes();

    void saveHome(String key, Map<String, Object> data);

    void deleteHome(String key);

    // --- Nicknames ---

    Map<String, Map<String, Object>> getAllNicknames();

    void saveNickname(String key, Map<String, Object> data);

    void deleteNickname(String key);

    // --- Tags ---

    Map<String, Map<String, Object>> getAllTags();

    void saveTag(String key, Map<String, Object> data);

    void deleteTag(String key);

    // --- Transactions ---

    Map<String, Map<String, Object>> getAllTransactions();

    void saveTransaction(String key, Map<String, Object> data);

    void deleteTransaction(String key);

    // --- Vaults ---

    Map<String, Map<String, Object>> getAllVaults();

    void saveVault(String key, Map<String, Object> data);

    void deleteVault(String key);

    // --- Ignores ---

    Map<String, Map<String, Object>> getAllIgnores();

    void saveIgnore(String key, Map<String, Object> data);

    void deleteIgnore(String key);

    /**
     * Returns the human-readable name of this storage type (e.g. "yaml", "sqlite", "mysql").
     */
    String getType();
}
