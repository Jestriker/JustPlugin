package org.justme.justPlugin.managers.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * SQLite storage provider - uses the SQLite JDBC driver bundled with Paper.
 */
public class SQLiteStorageProvider implements StorageProvider {

    private final JustPlugin plugin;
    private final String fileName;
    private final Gson gson = new Gson();
    private Connection connection;

    public SQLiteStorageProvider(JustPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    @Override
    public void init() throws Exception {
        File dbFile = new File(plugin.getDataFolder(), fileName);
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        // Enable WAL mode for better concurrency
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        }

        createTables();
        plugin.getLogger().info("[Database] SQLite storage provider initialized (" + fileName + ").");
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

            stmt.execute("CREATE TABLE IF NOT EXISTS teams (" +
                    "name VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS jails (" +
                    "name VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS kits (" +
                    "name VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS mutes (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS warns (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS mail (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS homes (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nicknames (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS tags (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS vaults (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS ignores (" +
                    "id VARCHAR(255) PRIMARY KEY, " +
                    "data TEXT)");
        }
    }

    @Override
    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("[Database] Failed to close SQLite connection: " + e.getMessage());
            }
        }
        plugin.getLogger().info("[Database] SQLite storage provider shut down.");
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            File dbFile = new File(plugin.getDataFolder(), fileName);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
        return connection;
    }

    // --- Player Data ---

    @Override
    public synchronized Map<String, Object> getPlayerData(UUID uuid) {
        Map<String, Object> data = new LinkedHashMap<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT data_key, data_value FROM player_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String key = rs.getString("data_key");
                String value = rs.getString("data_value");
                data.put(key, deserializeValue(value));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to load player data for " + uuid + ": " + e.getMessage());
        }
        return data;
    }

    @Override
    public synchronized void savePlayerData(UUID uuid, Map<String, Object> data) {
        String uuidStr = uuid.toString();
        try {
            Connection conn = getConnection();
            boolean prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM player_data WHERE uuid = ?")) {
                    del.setString(1, uuidStr);
                    del.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO player_data (uuid, data_key, data_value) VALUES (?, ?, ?)")) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        ps.setString(1, uuidStr);
                        ps.setString(2, entry.getKey());
                        ps.setString(3, serializeValue(entry.getValue()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(prevAutoCommit);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }

    @Override
    public Set<UUID> getAllPlayerUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT uuid FROM player_data")) {
            while (rs.next()) {
                try {
                    uuids.add(UUID.fromString(rs.getString("uuid")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to get all player UUIDs: " + e.getMessage());
        }
        return uuids;
    }

    // --- Warps ---

    @Override
    public Map<String, Map<String, Object>> getAllWarps() {
        Map<String, Map<String, Object>> warps = new LinkedHashMap<>();
        try (Statement stmt = getConnection().createStatement();
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
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to load warps: " + e.getMessage());
        }
        return warps;
    }

    @Override
    public void saveWarp(String name, Map<String, Object> data) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, String.valueOf(data.getOrDefault("world", "")));
            ps.setDouble(3, toDouble(data.get("x")));
            ps.setDouble(4, toDouble(data.get("y")));
            ps.setDouble(5, toDouble(data.get("z")));
            ps.setFloat(6, toFloat(data.get("yaw")));
            ps.setFloat(7, toFloat(data.get("pitch")));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to save warp '" + name + "': " + e.getMessage());
        }
    }

    @Override
    public void deleteWarp(String name) {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM warps WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to delete warp '" + name + "': " + e.getMessage());
        }
    }

    // --- Bans ---

    @Override
    public Map<String, Map<String, Object>> getAllBans() {
        Map<String, Map<String, Object>> bans = new LinkedHashMap<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bans")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String json = rs.getString("data");
                Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                if (data == null) data = new LinkedHashMap<>();
                bans.put(id, data);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to load bans: " + e.getMessage());
        }
        return bans;
    }

    @Override
    public void saveBan(String key, Map<String, Object> data) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO bans (id, data) VALUES (?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, gson.toJson(data));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to save ban '" + key + "': " + e.getMessage());
        }
    }

    @Override
    public void deleteBan(String key) {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM bans WHERE id = ?")) {
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to delete ban '" + key + "': " + e.getMessage());
        }
    }

    // --- Teams ---

    @Override
    public Map<String, Map<String, Object>> getAllTeams() {
        Map<String, Map<String, Object>> teams = new LinkedHashMap<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teams")) {
            while (rs.next()) {
                String name = rs.getString("name");
                String json = rs.getString("data");
                Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                if (data == null) data = new LinkedHashMap<>();
                teams.put(name, data);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to load teams: " + e.getMessage());
        }
        return teams;
    }

    @Override
    public void saveTeam(String name, Map<String, Object> data) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO teams (name, data) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, gson.toJson(data));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to save team '" + name + "': " + e.getMessage());
        }
    }

    @Override
    public void deleteTeam(String name) {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to delete team '" + name + "': " + e.getMessage());
        }
    }

    // --- Jails ---

    @Override
    public Map<String, Map<String, Object>> getAllJails() {
        return getAllFromJsonTable("jails", "name");
    }

    @Override
    public void saveJail(String name, Map<String, Object> data) {
        saveToJsonTable("jails", "name", name, data);
    }

    @Override
    public void deleteJail(String name) {
        deleteFromJsonTable("jails", "name", name);
    }

    // --- Kits ---

    @Override
    public Map<String, Map<String, Object>> getAllKits() {
        return getAllFromJsonTable("kits", "name");
    }

    @Override
    public void saveKit(String name, Map<String, Object> data) {
        saveToJsonTable("kits", "name", name, data);
    }

    @Override
    public void deleteKit(String name) {
        deleteFromJsonTable("kits", "name", name);
    }

    // --- Mutes ---

    @Override
    public Map<String, Map<String, Object>> getAllMutes() {
        return getAllFromJsonTable("mutes", "id");
    }

    @Override
    public void saveMute(String key, Map<String, Object> data) {
        saveToJsonTable("mutes", "id", key, data);
    }

    @Override
    public void deleteMute(String key) {
        deleteFromJsonTable("mutes", "id", key);
    }

    // --- Warns ---

    @Override
    public Map<String, Map<String, Object>> getAllWarns() {
        return getAllFromJsonTable("warns", "id");
    }

    @Override
    public void saveWarn(String key, Map<String, Object> data) {
        saveToJsonTable("warns", "id", key, data);
    }

    @Override
    public void deleteWarn(String key) {
        deleteFromJsonTable("warns", "id", key);
    }

    // --- Mail ---

    @Override
    public Map<String, Map<String, Object>> getAllMail() {
        return getAllFromJsonTable("mail", "id");
    }

    @Override
    public void saveMail(String key, Map<String, Object> data) {
        saveToJsonTable("mail", "id", key, data);
    }

    @Override
    public void deleteMail(String key) {
        deleteFromJsonTable("mail", "id", key);
    }

    // --- Homes ---

    @Override
    public Map<String, Map<String, Object>> getAllHomes() {
        return getAllFromJsonTable("homes", "id");
    }

    @Override
    public void saveHome(String key, Map<String, Object> data) {
        saveToJsonTable("homes", "id", key, data);
    }

    @Override
    public void deleteHome(String key) {
        deleteFromJsonTable("homes", "id", key);
    }

    // --- Nicknames ---

    @Override
    public Map<String, Map<String, Object>> getAllNicknames() {
        return getAllFromJsonTable("nicknames", "id");
    }

    @Override
    public void saveNickname(String key, Map<String, Object> data) {
        saveToJsonTable("nicknames", "id", key, data);
    }

    @Override
    public void deleteNickname(String key) {
        deleteFromJsonTable("nicknames", "id", key);
    }

    // --- Tags ---

    @Override
    public Map<String, Map<String, Object>> getAllTags() {
        return getAllFromJsonTable("tags", "id");
    }

    @Override
    public void saveTag(String key, Map<String, Object> data) {
        saveToJsonTable("tags", "id", key, data);
    }

    @Override
    public void deleteTag(String key) {
        deleteFromJsonTable("tags", "id", key);
    }

    // --- Transactions ---

    @Override
    public Map<String, Map<String, Object>> getAllTransactions() {
        return getAllFromJsonTable("transactions", "id");
    }

    @Override
    public void saveTransaction(String key, Map<String, Object> data) {
        saveToJsonTable("transactions", "id", key, data);
    }

    @Override
    public void deleteTransaction(String key) {
        deleteFromJsonTable("transactions", "id", key);
    }

    // --- Vaults ---

    @Override
    public Map<String, Map<String, Object>> getAllVaults() {
        return getAllFromJsonTable("vaults", "id");
    }

    @Override
    public void saveVault(String key, Map<String, Object> data) {
        saveToJsonTable("vaults", "id", key, data);
    }

    @Override
    public void deleteVault(String key) {
        deleteFromJsonTable("vaults", "id", key);
    }

    // --- Ignores ---

    @Override
    public Map<String, Map<String, Object>> getAllIgnores() {
        return getAllFromJsonTable("ignores", "id");
    }

    @Override
    public void saveIgnore(String key, Map<String, Object> data) {
        saveToJsonTable("ignores", "id", key, data);
    }

    @Override
    public void deleteIgnore(String key) {
        deleteFromJsonTable("ignores", "id", key);
    }

    // --- Generic JSON table helpers ---

    private synchronized Map<String, Map<String, Object>> getAllFromJsonTable(String table, String keyColumn) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
            while (rs.next()) {
                String key = rs.getString(keyColumn);
                String json = rs.getString("data");
                Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                if (data == null) data = new LinkedHashMap<>();
                result.put(key, data);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to load " + table + ": " + e.getMessage());
        }
        return result;
    }

    private synchronized void saveToJsonTable(String table, String keyColumn, String key, Map<String, Object> data) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO " + table + " (" + keyColumn + ", data) VALUES (?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, gson.toJson(data));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to save " + table + " '" + key + "': " + e.getMessage());
        }
    }

    private synchronized void deleteFromJsonTable(String table, String keyColumn, String key) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM " + table + " WHERE " + keyColumn + " = ?")) {
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Failed to delete " + table + " '" + key + "': " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "sqlite";
    }

    // --- Helpers ---

    private String serializeValue(Object value) {
        if (value == null) return null;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return gson.toJson(value);
    }

    private Object deserializeValue(String value) {
        if (value == null) return null;
        // Try to parse as number or boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        try {
            if (value.contains(".")) return Double.parseDouble(value);
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        // Try as JSON object/array
        if ((value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[") && value.endsWith("]"))) {
            try {
                return gson.fromJson(value, Object.class);
            } catch (Exception ignored) {
            }
        }
        return value;
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
