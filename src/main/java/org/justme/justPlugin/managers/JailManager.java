package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;
import org.justme.justPlugin.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages jail locations and jailed players.
 * Data is persisted in jails.yml or via StorageProvider when a database is configured.
 */
public class JailManager {

    private final JustPlugin plugin;
    private final DatabaseManager databaseManager;
    private final File jailsFile;
    private YamlConfiguration jailsConfig;

    // Jail locations: name -> Location
    private final Map<String, Location> jailLocations = new ConcurrentHashMap<>();

    // Jailed players: UUID -> JailEntry
    private final Map<UUID, JailEntry> jailedPlayers = new ConcurrentHashMap<>();

    private SchedulerUtil.CancellableTask expiryTask;

    public static class JailEntry {
        public final UUID uuid;
        public final String playerName;
        public final String jailName;
        public final String reason;
        public final String jailedBy;
        public final long startTime;
        public final long expiryTime; // -1 = permanent
        public final String previousGameMode;

        public JailEntry(UUID uuid, String playerName, String jailName, String reason,
                         String jailedBy, long startTime, long expiryTime, String previousGameMode) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.jailName = jailName;
            this.reason = reason;
            this.jailedBy = jailedBy;
            this.startTime = startTime;
            this.expiryTime = expiryTime;
            this.previousGameMode = previousGameMode;
        }

        public boolean isExpired() {
            return expiryTime != -1L && System.currentTimeMillis() > expiryTime;
        }

        public long getRemainingMs() {
            if (expiryTime == -1L) return -1L;
            return Math.max(0, expiryTime - System.currentTimeMillis());
        }
    }

    public JailManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.jailsFile = new File(plugin.getDataFolder(), "jails.yml");
        this.jailsConfig = YamlConfiguration.loadConfiguration(jailsFile);
        loadLocations();
        loadPrisoners();
        startExpiryTask();
    }

    private boolean isUsingDatabase() {
        if (databaseManager == null) return false;
        StorageProvider provider = databaseManager.getProvider();
        if (provider == null) return false;
        String type = provider.getType();
        return "sqlite".equals(type) || "mysql".equals(type);
    }

    private StorageProvider getStorageProvider() {
        return databaseManager != null ? databaseManager.getProvider() : null;
    }

    // ==================== Loading ====================

    private void loadLocations() {
        jailLocations.clear();
        if (isUsingDatabase()) {
            loadLocationsFromDatabase();
        } else {
            loadLocationsFromYaml();
        }
    }

    private void loadLocationsFromDatabase() {
        StorageProvider provider = getStorageProvider();
        if (provider == null) return;

        Map<String, Map<String, Object>> allJails = provider.getAllJails();
        for (Map.Entry<String, Map<String, Object>> entry : allJails.entrySet()) {
            String name = entry.getKey();
            // Skip prisoner entries (they have a "jail" key indicating they are prisoner data)
            Map<String, Object> data = entry.getValue();
            if (data.containsKey("jail")) continue; // This is a prisoner entry, not a location

            String world = data.getOrDefault("world", "").toString();
            if (world.isEmpty() || Bukkit.getWorld(world) == null) continue;

            double x = data.containsKey("x") ? ((Number) data.get("x")).doubleValue() : 0;
            double y = data.containsKey("y") ? ((Number) data.get("y")).doubleValue() : 0;
            double z = data.containsKey("z") ? ((Number) data.get("z")).doubleValue() : 0;
            float yaw = data.containsKey("yaw") ? ((Number) data.get("yaw")).floatValue() : 0;
            float pitch = data.containsKey("pitch") ? ((Number) data.get("pitch")).floatValue() : 0;

            jailLocations.put(name.toLowerCase(), new Location(
                    Bukkit.getWorld(world), x, y, z, yaw, pitch
            ));
        }
    }

    private void loadLocationsFromYaml() {
        ConfigurationSection section = jailsConfig.getConfigurationSection("locations");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            ConfigurationSection loc = section.getConfigurationSection(name);
            if (loc == null) continue;
            String world = loc.getString("world");
            if (world == null || Bukkit.getWorld(world) == null) continue;
            jailLocations.put(name.toLowerCase(), new Location(
                    Bukkit.getWorld(world),
                    loc.getDouble("x"), loc.getDouble("y"), loc.getDouble("z"),
                    (float) loc.getDouble("yaw"), (float) loc.getDouble("pitch")
            ));
        }
    }

    private void loadPrisoners() {
        jailedPlayers.clear();
        if (isUsingDatabase()) {
            loadPrisonersFromDatabase();
        } else {
            loadPrisonersFromYaml();
        }
    }

    private void loadPrisonersFromDatabase() {
        StorageProvider provider = getStorageProvider();
        if (provider == null) return;

        Map<String, Map<String, Object>> allJails = provider.getAllJails();
        for (Map.Entry<String, Map<String, Object>> entry : allJails.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> data = entry.getValue();
            // Prisoner entries have a "jail" key
            if (!data.containsKey("jail")) continue;

            // Key format for prisoners: "prisoner.<uuid>"
            if (!key.startsWith("prisoner.")) continue;

            try {
                UUID uuid = UUID.fromString(key.substring("prisoner.".length()));
                JailEntry jailEntry = new JailEntry(
                        uuid,
                        data.getOrDefault("name", "Unknown").toString(),
                        data.getOrDefault("jail", "default").toString(),
                        data.getOrDefault("reason", "No reason").toString(),
                        data.getOrDefault("jailedBy", "Unknown").toString(),
                        data.containsKey("startTime") ? ((Number) data.get("startTime")).longValue() : System.currentTimeMillis(),
                        data.containsKey("expiryTime") ? ((Number) data.get("expiryTime")).longValue() : -1L,
                        data.getOrDefault("previousGameMode", "SURVIVAL").toString()
                );

                if (!jailEntry.isExpired()) {
                    jailedPlayers.put(uuid, jailEntry);
                } else {
                    provider.deleteJail(key);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void loadPrisonersFromYaml() {
        ConfigurationSection section = jailsConfig.getConfigurationSection("prisoners");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection ps = section.getConfigurationSection(key);
                if (ps == null) continue;

                JailEntry entry = new JailEntry(
                        uuid,
                        ps.getString("name", "Unknown"),
                        ps.getString("jail", "default"),
                        ps.getString("reason", "No reason"),
                        ps.getString("jailedBy", "Unknown"),
                        ps.getLong("startTime", System.currentTimeMillis()),
                        ps.getLong("expiryTime", -1L),
                        ps.getString("previousGameMode", "SURVIVAL")
                );

                if (!entry.isExpired()) {
                    jailedPlayers.put(uuid, entry);
                } else {
                    // Clean up expired prisoner
                    jailsConfig.set("prisoners." + key, null);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        save();
    }

    // ==================== Expiry Task ====================

    private void startExpiryTask() {
        expiryTask = SchedulerUtil.runTaskTimer(plugin, () -> {
            List<UUID> expired = new ArrayList<>();
            for (Map.Entry<UUID, JailEntry> entry : jailedPlayers.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expired.add(entry.getKey());
                }
            }
            for (UUID uuid : expired) {
                unjail(uuid, true);
            }
        }, 20L, 20L); // Check every second (20 ticks)
    }

    // ==================== Core Methods ====================

    /**
     * Jail a player at the specified jail location.
     *
     * @param target   The player to jail (must be online)
     * @param jailName The jail location name
     * @param duration Duration in milliseconds, or -1 for permanent
     * @param reason   The reason for jailing
     * @param staff    The staff member who jailed the player
     */
    public void jail(Player target, String jailName, long duration, String reason, String staff) {
        Location jailLoc = jailLocations.get(jailName.toLowerCase());
        if (jailLoc == null) return;

        long expiryTime = duration == -1L ? -1L : System.currentTimeMillis() + duration;
        String previousGm = target.getGameMode().name();

        JailEntry entry = new JailEntry(
                target.getUniqueId(),
                target.getName(),
                jailName.toLowerCase(),
                reason,
                staff,
                System.currentTimeMillis(),
                expiryTime,
                previousGm
        );

        jailedPlayers.put(target.getUniqueId(), entry);

        // Teleport to jail
        target.teleportAsync(jailLoc);

        // Set adventure mode if configured
        if (plugin.getConfig().getBoolean("jail.adventure-mode", true)) {
            target.setGameMode(GameMode.ADVENTURE);
        }

        // Notify the player
        target.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jail.you-are-jailed",
                "{reason}", reason)));

        savePrisoner(entry);
    }

    /**
     * Unjail a player by UUID.
     *
     * @param uuid    The player's UUID
     * @param expired Whether this was an automatic expiry
     */
    public void unjail(UUID uuid, boolean expired) {
        JailEntry entry = jailedPlayers.remove(uuid);
        if (entry == null) return;

        // Remove from storage
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                SchedulerUtil.runAsync(plugin, () -> provider.deleteJail("prisoner." + uuid.toString()));
            }
        } else {
            jailsConfig.set("prisoners." + uuid.toString(), null);
            save();
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            // Restore game mode
            try {
                GameMode prevGm = GameMode.valueOf(entry.previousGameMode);
                player.setGameMode(prevGm);
            } catch (IllegalArgumentException e) {
                player.setGameMode(GameMode.SURVIVAL);
            }

            // Teleport to spawn
            Location spawn = player.getWorld().getSpawnLocation();
            // Try plugin spawn first
            if (plugin.getConfig().contains("spawn.world")) {
                String world = plugin.getConfig().getString("spawn.world");
                if (world != null && Bukkit.getWorld(world) != null) {
                    spawn = new Location(
                            Bukkit.getWorld(world),
                            plugin.getConfig().getDouble("spawn.x"),
                            plugin.getConfig().getDouble("spawn.y"),
                            plugin.getConfig().getDouble("spawn.z"),
                            (float) plugin.getConfig().getDouble("spawn.yaw"),
                            (float) plugin.getConfig().getDouble("spawn.pitch")
                    );
                }
            }
            player.teleportAsync(spawn);

            // Notify
            if (expired) {
                player.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.jail.expired")));
            } else {
                player.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.jail.released")));
            }
        }
    }

    /**
     * Unjail a player (manual unjail by staff).
     */
    public void unjail(UUID uuid) {
        unjail(uuid, false);
    }

    public boolean isJailed(UUID uuid) {
        JailEntry entry = jailedPlayers.get(uuid);
        if (entry == null) return false;
        if (entry.isExpired()) {
            unjail(uuid, true);
            return false;
        }
        return true;
    }

    public JailEntry getJailInfo(UUID uuid) {
        return jailedPlayers.get(uuid);
    }

    /**
     * Handle a player joining while jailed - teleport to jail and set game mode.
     */
    public void handleJoin(Player player) {
        JailEntry entry = jailedPlayers.get(player.getUniqueId());
        if (entry == null) return;

        if (entry.isExpired()) {
            unjail(player.getUniqueId(), true);
            return;
        }

        // Teleport to jail location
        Location jailLoc = jailLocations.get(entry.jailName);
        if (jailLoc != null) {
            // Delay by 1 tick to ensure player is fully loaded
            SchedulerUtil.runForEntityLater(plugin, player, () -> {
                player.teleportAsync(jailLoc);
                if (plugin.getConfig().getBoolean("jail.adventure-mode", true)) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
                player.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.jail.you-are-jailed",
                        "{reason}", entry.reason)));
            }, 1L);
        }
    }

    /**
     * Check if a command is blocked for jailed players.
     */
    public boolean isCommandBlocked(String command) {
        if (!plugin.getConfig().getBoolean("jail.block-commands", true)) return false;

        String cmd = command.toLowerCase().split(" ")[0];
        if (!cmd.startsWith("/")) cmd = "/" + cmd;

        List<String> allowed = plugin.getConfig().getStringList("jail.allowed-commands");
        for (String allowedCmd : allowed) {
            if (cmd.equalsIgnoreCase(allowedCmd) || cmd.equalsIgnoreCase(allowedCmd.replace("/", ""))) {
                return false;
            }
        }
        return true;
    }

    // ==================== Jail Locations ====================

    public boolean jailExists(String name) {
        return jailLocations.containsKey(name.toLowerCase());
    }

    public Location getJailLocation(String name) {
        return jailLocations.get(name.toLowerCase());
    }

    public Set<String> getJailNames() {
        return Collections.unmodifiableSet(jailLocations.keySet());
    }

    public void setJailLocation(String name, Location location) {
        jailLocations.put(name.toLowerCase(), location);
        saveLocation(name.toLowerCase(), location);
    }

    public boolean deleteJailLocation(String name) {
        if (!jailLocations.containsKey(name.toLowerCase())) return false;
        jailLocations.remove(name.toLowerCase());
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                SchedulerUtil.runAsync(plugin, () -> provider.deleteJail(name.toLowerCase()));
            }
        } else {
            jailsConfig.set("locations." + name.toLowerCase(), null);
            save();
        }
        return true;
    }

    /**
     * Get a random jail name, or the first one if only one exists.
     * Returns null if no jails are set.
     */
    public String getDefaultJailName() {
        if (jailLocations.isEmpty()) return null;
        List<String> names = new ArrayList<>(jailLocations.keySet());
        return names.get(0);
    }

    public int getJailedCount() {
        return jailedPlayers.size();
    }

    public Collection<JailEntry> getAllJailedPlayers() {
        return Collections.unmodifiableCollection(jailedPlayers.values());
    }

    // ==================== Persistence ====================

    private void saveLocation(String name, Location loc) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("world", loc.getWorld().getName());
                data.put("x", loc.getX());
                data.put("y", loc.getY());
                data.put("z", loc.getZ());
                data.put("yaw", (double) loc.getYaw());
                data.put("pitch", (double) loc.getPitch());
                SchedulerUtil.runAsync(plugin, () -> provider.saveJail(name, data));
            }
        } else {
            String path = "locations." + name;
            jailsConfig.set(path + ".world", loc.getWorld().getName());
            jailsConfig.set(path + ".x", loc.getX());
            jailsConfig.set(path + ".y", loc.getY());
            jailsConfig.set(path + ".z", loc.getZ());
            jailsConfig.set(path + ".yaw", loc.getYaw());
            jailsConfig.set(path + ".pitch", loc.getPitch());
            save();
        }
    }

    private void savePrisoner(JailEntry entry) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("name", entry.playerName);
                data.put("jail", entry.jailName);
                data.put("reason", entry.reason);
                data.put("jailedBy", entry.jailedBy);
                data.put("startTime", entry.startTime);
                data.put("expiryTime", entry.expiryTime);
                data.put("previousGameMode", entry.previousGameMode);
                SchedulerUtil.runAsync(plugin, () -> provider.saveJail("prisoner." + entry.uuid.toString(), data));
            }
        } else {
            String path = "prisoners." + entry.uuid.toString();
            jailsConfig.set(path + ".name", entry.playerName);
            jailsConfig.set(path + ".jail", entry.jailName);
            jailsConfig.set(path + ".reason", entry.reason);
            jailsConfig.set(path + ".jailedBy", entry.jailedBy);
            jailsConfig.set(path + ".startTime", entry.startTime);
            jailsConfig.set(path + ".expiryTime", entry.expiryTime);
            jailsConfig.set(path + ".previousGameMode", entry.previousGameMode);
            save();
        }
    }

    private void save() {
        try {
            jailsConfig.save(jailsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save jails.yml: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }
}
