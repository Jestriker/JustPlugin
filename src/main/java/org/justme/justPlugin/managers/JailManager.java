package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;
import org.justme.justPlugin.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages jail locations and jailed players.
 * Data is persisted in jails.yml with two sections: "locations" and "prisoners".
 */
public class JailManager {

    private final JustPlugin plugin;
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
        this.jailsFile = new File(plugin.getDataFolder(), "jails.yml");
        this.jailsConfig = YamlConfiguration.loadConfiguration(jailsFile);
        loadLocations();
        loadPrisoners();
        startExpiryTask();
    }

    // ==================== Loading ====================

    private void loadLocations() {
        jailLocations.clear();
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

        // Remove from config
        jailsConfig.set("prisoners." + uuid.toString(), null);
        save();

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
        jailsConfig.set("locations." + name.toLowerCase(), null);
        save();
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
        String path = "locations." + name;
        jailsConfig.set(path + ".world", loc.getWorld().getName());
        jailsConfig.set(path + ".x", loc.getX());
        jailsConfig.set(path + ".y", loc.getY());
        jailsConfig.set(path + ".z", loc.getZ());
        jailsConfig.set(path + ".yaw", loc.getYaw());
        jailsConfig.set(path + ".pitch", loc.getPitch());
        save();
    }

    private void savePrisoner(JailEntry entry) {
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
