package org.justme.justPlugin.managers.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SQLite storage operations using an in-memory database.
 * This directly tests the SQL schema and CRUD operations that
 * SQLiteStorageProvider uses, without requiring a JustPlugin instance.
 */
class StorageProviderTest {

    private Connection connection;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createTables();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "data_key VARCHAR(255) NOT NULL, " +
                    "data_value TEXT, " +
                    "PRIMARY KEY(uuid, data_key))");

            stmt.execute("CREATE TABLE IF NOT EXISTS warps (" +
                    "name VARCHAR(255) PRIMARY KEY, " +
                    "world VARCHAR(255), " +
                    "x DOUBLE, y DOUBLE, z DOUBLE, " +
                    "yaw FLOAT, pitch FLOAT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS bans (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");
        }
    }

    // ========================
    // Player Data tests
    // ========================

    @Test
    void testSaveAndGetPlayerData() throws SQLException {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("balance", 500.0);
        data.put("paytoggle", false);
        data.put("name", "TestPlayer");

        savePlayerData(uuid, data);
        Map<String, Object> loaded = getPlayerData(uuid);

        assertEquals("500.0", loaded.get("balance").toString());
        assertEquals("false", loaded.get("paytoggle").toString());
        assertEquals("TestPlayer", loaded.get("name"));
    }

    @Test
    void testGetPlayerData_NonExistentPlayer_ReturnsEmptyMap() throws SQLException {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> loaded = getPlayerData(uuid);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void testSavePlayerData_OverwritesExisting() throws SQLException {
        UUID uuid = UUID.randomUUID();

        Map<String, Object> data1 = new LinkedHashMap<>();
        data1.put("balance", 100.0);
        savePlayerData(uuid, data1);

        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("balance", 999.0);
        data2.put("newKey", "newValue");
        savePlayerData(uuid, data2);

        Map<String, Object> loaded = getPlayerData(uuid);
        assertEquals("999.0", loaded.get("balance").toString());
        assertEquals("newValue", loaded.get("newKey"));
        // Old data should be gone since savePlayerData does DELETE+INSERT
        assertEquals(2, loaded.size());
    }

    @Test
    void testSavePlayerData_TransactionAtomicity() throws SQLException {
        UUID uuid = UUID.randomUUID();

        // First save some data
        Map<String, Object> data1 = new LinkedHashMap<>();
        data1.put("key1", "value1");
        data1.put("key2", "value2");
        savePlayerData(uuid, data1);

        // Now save new data (which should delete old + insert new atomically)
        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("key3", "value3");
        savePlayerData(uuid, data2);

        Map<String, Object> loaded = getPlayerData(uuid);
        assertFalse(loaded.containsKey("key1"), "Old keys should be removed by transactional save");
        assertFalse(loaded.containsKey("key2"), "Old keys should be removed by transactional save");
        assertEquals("value3", loaded.get("key3"));
    }

    @Test
    void testSavePlayerData_EmptyMap() throws SQLException {
        UUID uuid = UUID.randomUUID();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("balance", 100.0);
        savePlayerData(uuid, data);

        // Save empty map - should clear all data
        savePlayerData(uuid, new LinkedHashMap<>());
        Map<String, Object> loaded = getPlayerData(uuid);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void testGetAllPlayerUUIDs() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        savePlayerData(uuid1, Map.of("balance", 100));
        savePlayerData(uuid2, Map.of("balance", 200));
        savePlayerData(uuid3, Map.of("balance", 300));

        Set<UUID> uuids = getAllPlayerUUIDs();
        assertEquals(3, uuids.size());
        assertTrue(uuids.contains(uuid1));
        assertTrue(uuids.contains(uuid2));
        assertTrue(uuids.contains(uuid3));
    }

    @Test
    void testGetAllPlayerUUIDs_Empty() throws SQLException {
        Set<UUID> uuids = getAllPlayerUUIDs();
        assertTrue(uuids.isEmpty());
    }

    @Test
    void testGetAllPlayerUUIDs_NoDuplicates() throws SQLException {
        UUID uuid = UUID.randomUUID();
        // Save multiple keys for the same player
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key1", "v1");
        data.put("key2", "v2");
        data.put("key3", "v3");
        savePlayerData(uuid, data);

        Set<UUID> uuids = getAllPlayerUUIDs();
        assertEquals(1, uuids.size());
    }

    // ========================
    // Warp tests
    // ========================

    @Test
    void testSaveAndGetWarp() throws SQLException {
        Map<String, Object> warpData = new LinkedHashMap<>();
        warpData.put("world", "world");
        warpData.put("x", 100.5);
        warpData.put("y", 64.0);
        warpData.put("z", -200.3);
        warpData.put("yaw", 90.0f);
        warpData.put("pitch", 0.0f);

        saveWarp("spawn", warpData);
        Map<String, Map<String, Object>> warps = getAllWarps();

        assertEquals(1, warps.size());
        assertTrue(warps.containsKey("spawn"));
        Map<String, Object> loaded = warps.get("spawn");
        assertEquals("world", loaded.get("world"));
        assertEquals(100.5, (double) loaded.get("x"), 0.01);
        assertEquals(64.0, (double) loaded.get("y"), 0.01);
    }

    @Test
    void testDeleteWarp() throws SQLException {
        saveWarp("test", Map.of("world", "world", "x", 0.0, "y", 0.0, "z", 0.0, "yaw", 0.0f, "pitch", 0.0f));
        assertEquals(1, getAllWarps().size());

        deleteWarp("test");
        assertTrue(getAllWarps().isEmpty());
    }

    @Test
    void testDeleteWarp_NonExistent() throws SQLException {
        deleteWarp("nonexistent");
        // Should not throw
        assertTrue(getAllWarps().isEmpty());
    }

    @Test
    void testSaveWarp_OverwritesExisting() throws SQLException {
        saveWarp("spawn", Map.of("world", "world", "x", 100.0, "y", 64.0, "z", 200.0, "yaw", 0.0f, "pitch", 0.0f));
        saveWarp("spawn", Map.of("world", "nether", "x", 50.0, "y", 32.0, "z", 100.0, "yaw", 45.0f, "pitch", 10.0f));

        Map<String, Map<String, Object>> warps = getAllWarps();
        assertEquals(1, warps.size());
        assertEquals("nether", warps.get("spawn").get("world"));
        assertEquals(50.0, (double) warps.get("spawn").get("x"), 0.01);
    }

    @Test
    void testMultipleWarps() throws SQLException {
        saveWarp("spawn", Map.of("world", "world", "x", 0.0, "y", 64.0, "z", 0.0, "yaw", 0.0f, "pitch", 0.0f));
        saveWarp("shop", Map.of("world", "world", "x", 100.0, "y", 64.0, "z", 100.0, "yaw", 0.0f, "pitch", 0.0f));
        saveWarp("arena", Map.of("world", "world", "x", -50.0, "y", 70.0, "z", 50.0, "yaw", 0.0f, "pitch", 0.0f));

        Map<String, Map<String, Object>> warps = getAllWarps();
        assertEquals(3, warps.size());
        assertTrue(warps.containsKey("spawn"));
        assertTrue(warps.containsKey("shop"));
        assertTrue(warps.containsKey("arena"));
    }

    // ========================
    // Ban tests (JSON storage)
    // ========================

    @Test
    void testSaveAndGetBan() throws SQLException {
        Map<String, Object> banData = new LinkedHashMap<>();
        banData.put("name", "BadPlayer");
        banData.put("reason", "Hacking");
        banData.put("bannedBy", "Admin");
        banData.put("time", System.currentTimeMillis());
        banData.put("expires", -1L);

        UUID uuid = UUID.randomUUID();
        saveBan(uuid.toString(), banData);

        Map<String, Map<String, Object>> bans = getAllBans();
        assertEquals(1, bans.size());
        assertTrue(bans.containsKey(uuid.toString()));
        assertEquals("BadPlayer", bans.get(uuid.toString()).get("name"));
        assertEquals("Hacking", bans.get(uuid.toString()).get("reason"));
    }

    @Test
    void testDeleteBan() throws SQLException {
        UUID uuid = UUID.randomUUID();
        saveBan(uuid.toString(), Map.of("name", "Test", "reason", "Test"));
        assertEquals(1, getAllBans().size());

        deleteBan(uuid.toString());
        assertTrue(getAllBans().isEmpty());
    }

    @Test
    void testDeleteBan_NonExistent() throws SQLException {
        deleteBan("nonexistent");
        assertTrue(getAllBans().isEmpty());
    }

    @Test
    void testBan_OverwritesExisting() throws SQLException {
        UUID uuid = UUID.randomUUID();
        saveBan(uuid.toString(), Map.of("reason", "first"));
        saveBan(uuid.toString(), Map.of("reason", "updated"));

        Map<String, Map<String, Object>> bans = getAllBans();
        assertEquals(1, bans.size());
        assertEquals("updated", bans.get(uuid.toString()).get("reason"));
    }

    @Test
    void testMultipleBans() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        saveBan(uuid1.toString(), Map.of("reason", "Hacking"));
        saveBan(uuid2.toString(), Map.of("reason", "Griefing"));

        Map<String, Map<String, Object>> bans = getAllBans();
        assertEquals(2, bans.size());
    }

    // ========================
    // Value serialization tests
    // ========================

    @Test
    void testPlayerData_StringValue() throws SQLException {
        UUID uuid = UUID.randomUUID();
        savePlayerData(uuid, Map.of("name", "TestPlayer"));
        Map<String, Object> loaded = getPlayerData(uuid);
        assertEquals("TestPlayer", loaded.get("name"));
    }

    @Test
    void testPlayerData_NumericValue() throws SQLException {
        UUID uuid = UUID.randomUUID();
        savePlayerData(uuid, Map.of("balance", 12345.67));
        Map<String, Object> loaded = getPlayerData(uuid);
        // Stored as string "12345.67", loaded back
        assertNotNull(loaded.get("balance"));
    }

    @Test
    void testPlayerData_BooleanValue() throws SQLException {
        UUID uuid = UUID.randomUUID();
        savePlayerData(uuid, Map.of("paytoggle", true));
        Map<String, Object> loaded = getPlayerData(uuid);
        assertEquals("true", loaded.get("paytoggle").toString());
    }

    // ========================
    // Helper methods mirroring SQLiteStorageProvider behavior
    // ========================

    private Map<String, Object> getPlayerData(UUID uuid) throws SQLException {
        Map<String, Object> data = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT data_key, data_value FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("data_key"), rs.getString("data_value"));
            }
        }
        return data;
    }

    private void savePlayerData(UUID uuid, Map<String, ?> data) throws SQLException {
        String uuidStr = uuid.toString();
        boolean prevAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement del = connection.prepareStatement("DELETE FROM player_data WHERE uuid = ?")) {
                del.setString(1, uuidStr);
                del.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO player_data (uuid, data_key, data_value) VALUES (?, ?, ?)")) {
                for (Map.Entry<String, ?> entry : data.entrySet()) {
                    ps.setString(1, uuidStr);
                    ps.setString(2, entry.getKey());
                    ps.setString(3, entry.getValue() != null ? entry.getValue().toString() : null);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(prevAutoCommit);
        }
    }

    private Set<UUID> getAllPlayerUUIDs() throws SQLException {
        Set<UUID> uuids = new HashSet<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT uuid FROM player_data")) {
            while (rs.next()) {
                try {
                    uuids.add(UUID.fromString(rs.getString("uuid")));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return uuids;
    }

    private void saveWarp(String name, Map<String, Object> data) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, String.valueOf(data.getOrDefault("world", "")));
            ps.setDouble(3, toDouble(data.get("x")));
            ps.setDouble(4, toDouble(data.get("y")));
            ps.setDouble(5, toDouble(data.get("z")));
            ps.setFloat(6, toFloat(data.get("yaw")));
            ps.setFloat(7, toFloat(data.get("pitch")));
            ps.executeUpdate();
        }
    }

    private void deleteWarp(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM warps WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    private Map<String, Map<String, Object>> getAllWarps() throws SQLException {
        Map<String, Map<String, Object>> warps = new LinkedHashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM warps")) {
            while (rs.next()) {
                String name = rs.getString("name");
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("world", rs.getString("world"));
                data.put("x", rs.getDouble("x"));
                data.put("y", rs.getDouble("y"));
                data.put("z", rs.getDouble("z"));
                data.put("yaw", rs.getFloat("yaw"));
                data.put("pitch", rs.getFloat("pitch"));
                warps.put(name, data);
            }
        }
        return warps;
    }

    private void saveBan(String key, Map<String, Object> data) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO bans (id, data) VALUES (?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, gson.toJson(data));
            ps.executeUpdate();
        }
    }

    private void deleteBan(String key) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bans WHERE id = ?")) {
            ps.setString(1, key);
            ps.executeUpdate();
        }
    }

    private Map<String, Map<String, Object>> getAllBans() throws SQLException {
        Map<String, Map<String, Object>> bans = new LinkedHashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bans")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String json = rs.getString("data");
                Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                if (data == null) data = new LinkedHashMap<>();
                bans.put(id, data);
            }
        }
        return bans;
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number n) return n.doubleValue();
        if (obj instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private float toFloat(Object obj) {
        if (obj instanceof Number n) return n.floatValue();
        if (obj instanceof String s) {
            try { return Float.parseFloat(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }
}
