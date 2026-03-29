package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.CachedServerIcon;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Manages the server maintenance mode system.
 * <p>
 * Settings are stored in two files:
 * <ul>
 *   <li><b>maintenance/config.yml</b> - configurable messages, MOTD, icon, groups, OP bypass</li>
 *   <li><b>maintenance.yml</b> - runtime state (active, cooldown, allowed-users)</li>
 * </ul>
 */
public class MaintenanceManager {

    private final JustPlugin plugin;
    private final File dataFile;
    private final File configFolder;
    private final File configFile;
    private YamlConfiguration data;
    private YamlConfiguration maintenanceConfig;

    private boolean active;
    private long cooldownEnd; // epoch millis when maintenance ends, -1 = no estimate
    private final Set<UUID> allowedUsers = new HashSet<>();
    private final Map<UUID, String> allowedNames = new HashMap<>(); // uuid -> last known name

    // Configurable messages (loaded from maintenance/config.yml)
    private String kickMessage;
    private String joinWarningMessage;
    private String maintenanceMotd;

    // Icon
    private CachedServerIcon cachedIcon;

    // Bypass settings
    private boolean opsBypass;
    private boolean autoDisableOnExpire;
    private final List<String> allowedGroups = new ArrayList<>();

    public MaintenanceManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "maintenance.yml");
        this.configFolder = new File(plugin.getDataFolder(), "maintenance");
        this.configFile = new File(configFolder, "config.yml");
        load();
    }

    // ==================== Persistence ====================

    private void load() {
        // Create maintenance folder if needed
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        // Save default maintenance/config.yml from resources if not present
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("maintenance/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                    plugin.getLogger().info("[Maintenance] Created default maintenance/config.yml");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("[Maintenance] Failed to save default maintenance/config.yml: " + e.getMessage());
            }
        }

        // Migrate old config.yml maintenance keys if they exist
        migrateOldConfig();

        // Load maintenance/config.yml
        maintenanceConfig = YamlConfiguration.loadConfiguration(configFile);

        // Load state file (maintenance.yml)
        if (!dataFile.exists()) {
            data = new YamlConfiguration();
            data.set("active", false);
            data.set("cooldown-end", -1L);
            saveState();
        } else {
            data = YamlConfiguration.loadConfiguration(dataFile);
        }

        active = data.getBoolean("active", false);
        cooldownEnd = data.getLong("cooldown-end", -1L);

        allowedUsers.clear();
        allowedNames.clear();
        if (data.contains("allowed-users")) {
            for (String key : data.getConfigurationSection("allowed-users").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = data.getString("allowed-users." + key, "Unknown");
                    allowedUsers.add(uuid);
                    allowedNames.put(uuid, name);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Load configurable messages
        reloadMessages();

        // Load icon
        loadIcon();
    }

    /**
     * Migrate old maintenance keys from config.yml to maintenance/config.yml.
     */
    private void migrateOldConfig() {
        var mainConfig = plugin.getConfig();
        boolean migrated = false;

        if (mainConfig.contains("maintenance.kick-message")) {
            maintenanceConfig = configFile.exists()
                    ? YamlConfiguration.loadConfiguration(configFile)
                    : new YamlConfiguration();

            if (mainConfig.contains("maintenance.kick-message")) {
                maintenanceConfig.set("kick-message", mainConfig.getString("maintenance.kick-message"));
                migrated = true;
            }
            if (mainConfig.contains("maintenance.join-warning")) {
                maintenanceConfig.set("join-warning", mainConfig.getString("maintenance.join-warning"));
                migrated = true;
            }
            if (mainConfig.contains("maintenance.motd-line")) {
                // Convert old single-line motd-line to full motd
                String oldLine = mainConfig.getString("maintenance.motd-line",
                        "<red><bold>⚠ MAINTENANCE</bold></red> <dark_gray>- <gray>{cooldown_text}");
                maintenanceConfig.set("motd",
                        "<red><bold>⚠ SERVER MAINTENANCE ⚠</bold></red>\n" + oldLine);
                migrated = true;
            }

            if (migrated) {
                try {
                    maintenanceConfig.save(configFile);
                    // Remove old keys from main config
                    mainConfig.set("maintenance.kick-message", null);
                    mainConfig.set("maintenance.join-warning", null);
                    mainConfig.set("maintenance.motd-line", null);
                    // Clean up empty section
                    if (mainConfig.contains("maintenance") && mainConfig.isConfigurationSection("maintenance")
                            && mainConfig.getConfigurationSection("maintenance").getKeys(false).isEmpty()) {
                        mainConfig.set("maintenance", null);
                    }
                    plugin.saveConfig();
                    plugin.getLogger().info("[Maintenance] Migrated maintenance settings from config.yml to maintenance/config.yml");
                } catch (IOException e) {
                    plugin.getLogger().severe("[Maintenance] Failed to save migrated maintenance/config.yml: " + e.getMessage());
                }
            }
        }
    }

    public void reloadMessages() {
        if (maintenanceConfig == null) {
            maintenanceConfig = YamlConfiguration.loadConfiguration(configFile);
        }

        kickMessage = maintenanceConfig.getString("kick-message",
                "\n<red><bold>⚠ Server Maintenance ⚠</bold></red>\n\n" +
                "<gray>The server is currently undergoing maintenance.\n" +
                "<yellow>{cooldown_line}\n\n" +
                "<gray>Only authorized staff can join at this time.");

        joinWarningMessage = maintenanceConfig.getString("join-warning",
                "<yellow><bold>⚠</bold></yellow> <gray>The server is currently running in <red>maintenance mode</red><gray>.");

        maintenanceMotd = maintenanceConfig.getString("motd",
                "<red><bold>⚠ MAINTENANCE</bold></red>\n<gray>Server is currently under maintenance, <yellow>{cooldown_text}<gray>.");

        opsBypass = maintenanceConfig.getBoolean("ops-bypass", false);
        autoDisableOnExpire = maintenanceConfig.getBoolean("auto-disable-on-expire", false);

        allowedGroups.clear();
        List<String> groups = maintenanceConfig.getStringList("allowed-groups");
        for (String g : groups) {
            allowedGroups.add(g.toLowerCase());
        }

        // Load allowed-players from config (by name, resolved to UUID at runtime)
        loadAllowedPlayersFromConfig();
    }

    /**
     * Resolves player names from allowed-players config list to UUIDs and merges
     * them into the runtime allowed-users set.
     */
    @SuppressWarnings("deprecation")
    private void loadAllowedPlayersFromConfig() {
        List<String> configPlayers = maintenanceConfig.getStringList("allowed-players");
        if (configPlayers == null || configPlayers.isEmpty()) return;

        for (String name : configPlayers) {
            if (name == null || name.trim().isEmpty()) continue;
            String trimmed = name.trim();
            // Try to find the player - first check online, then offline cache
            org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayerExact(trimmed);
            if (online != null) {
                allowedUsers.add(online.getUniqueId());
                allowedNames.put(online.getUniqueId(), online.getName());
            } else {
                org.bukkit.OfflinePlayer offP = org.bukkit.Bukkit.getOfflinePlayer(trimmed);
                if (offP.hasPlayedBefore() || offP.isOnline()) {
                    allowedUsers.add(offP.getUniqueId());
                    allowedNames.put(offP.getUniqueId(), offP.getName() != null ? offP.getName() : trimmed);
                } else {
                    plugin.getLogger().warning("[Maintenance] Could not resolve player '" + trimmed +
                            "' from allowed-players list - they may not have joined before.");
                }
            }
        }
    }

    /**
     * Load the custom maintenance server icon from the maintenance/ folder.
     */
    private void loadIcon() {
        cachedIcon = null;
        String iconName = maintenanceConfig.getString("icon", "");
        if (iconName == null || iconName.isEmpty()) return;

        File iconFile = new File(configFolder, iconName);
        if (!iconFile.exists()) {
            plugin.getLogger().warning("[Maintenance] Icon file not found: maintenance/" + iconName +
                    " - using default server icon during maintenance.");
            return;
        }

        try {
            BufferedImage image = ImageIO.read(iconFile);
            if (image == null) {
                plugin.getLogger().warning("[Maintenance] Could not read icon file: maintenance/" + iconName +
                        " - not a valid image.");
                return;
            }
            if (image.getWidth() != 64 || image.getHeight() != 64) {
                plugin.getLogger().warning("[Maintenance] Icon file maintenance/" + iconName +
                        " must be exactly 64x64 pixels (found " + image.getWidth() + "x" + image.getHeight() + ").");
                return;
            }
            cachedIcon = Bukkit.loadServerIcon(image);
            plugin.getLogger().info("[Maintenance] Loaded custom maintenance icon: maintenance/" + iconName);
        } catch (Exception e) {
            plugin.getLogger().warning("[Maintenance] Failed to load icon maintenance/" + iconName + ": " + e.getMessage());
        }
    }

    private void saveState() {
        data.set("active", active);
        data.set("cooldown-end", cooldownEnd);
        // Save allowed users
        data.set("allowed-users", null);
        for (UUID uuid : allowedUsers) {
            data.set("allowed-users." + uuid.toString(), allowedNames.getOrDefault(uuid, "Unknown"));
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save maintenance.yml: " + e.getMessage());
        }
    }

    private void saveMaintenanceConfig() {
        try {
            maintenanceConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Maintenance] Failed to save maintenance/config.yml: " + e.getMessage());
        }
    }

    // ==================== State ====================

    /** Whether maintenance mode is currently active (server blocks non-whitelisted joins). */
    public boolean isActive() {
        if (active && cooldownEnd > 0 && System.currentTimeMillis() >= cooldownEnd) {
            if (autoDisableOnExpire) {
                // Fully turn off maintenance - server reopens
                setActive(false);
                plugin.getLogger().info("[Maintenance] Cooldown expired - maintenance has been automatically disabled.");
                Bukkit.broadcast(CC.success("Maintenance mode has ended. The server is now open!"),
                        "justplugin.maintenance");
            } else {
                // Keep maintenance active, just clear the expired cooldown
                cooldownEnd = -1L;
                saveState();
                plugin.getLogger().info("[Maintenance] Cooldown expired - maintenance is still active (auto-disable is off). Use /maintenance mode off to end it.");
                Bukkit.broadcast(CC.warning("Maintenance cooldown has expired, but maintenance is still active. Use <yellow>/maintenance mode off</yellow> <gray>to end it."),
                        "justplugin.maintenance");
            }
        }
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            cooldownEnd = -1L; // Clear cooldown when turning off
        }
        saveState();

        // Kick non-whitelisted players if turning ON
        if (active) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!canBypass(p)) {
                    p.kick(buildKickMessage());
                }
            }
        }
    }

    /**
     * Checks if a player can bypass maintenance mode.
     * Checks: allowed-users list, bypass permission, OP status (if enabled), LuckPerms groups.
     */
    public boolean canBypass(Player player) {
        if (isAllowed(player.getUniqueId())) return true;
        if (player.hasPermission("justplugin.maintenance.bypass")) return true;
        if (opsBypass && player.isOp()) return true;
        if (isGroupBypassed(player.getUniqueId())) return true;
        return false;
    }

    /** Get the cooldown end timestamp (-1 means no cooldown / no estimate). */
    public long getCooldownEnd() {
        return cooldownEnd;
    }

    /** Set estimated end time. Pass -1 to clear. */
    public void setCooldownEnd(long epochMillis) {
        this.cooldownEnd = epochMillis;
        saveState();
    }

    /**
     * Returns a human-readable remaining time string, or null if no cooldown is set.
     */
    public String getCooldownText() {
        if (cooldownEnd <= 0) return null;
        long remaining = cooldownEnd - System.currentTimeMillis();
        if (remaining <= 0) return "Ending soon";
        return formatDuration(remaining);
    }

    // ==================== Whitelist ====================

    public boolean isAllowed(UUID uuid) {
        return allowedUsers.contains(uuid);
    }

    public boolean addAllowed(UUID uuid, String name) {
        if (allowedUsers.add(uuid)) {
            allowedNames.put(uuid, name);
            saveState();
            return true;
        }
        return false;
    }

    public boolean removeAllowed(UUID uuid) {
        if (allowedUsers.remove(uuid)) {
            allowedNames.remove(uuid);
            saveState();
            return true;
        }
        return false;
    }

    /** Returns an unmodifiable map of UUID -> name for all whitelisted users. */
    public Map<UUID, String> getAllowedUsers() {
        return Collections.unmodifiableMap(allowedNames);
    }

    // ==================== Group Bypass ====================

    /**
     * Checks if a player belongs to any of the configured LuckPerms bypass groups.
     */
    public boolean isGroupBypassed(UUID uuid) {
        if (allowedGroups.isEmpty() || !plugin.isLuckPermsAvailable()) return false;
        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().loadUser(uuid).join();
            if (user == null) return false;
            for (net.luckperms.api.node.Node node : user.getNodes()) {
                if (node instanceof net.luckperms.api.node.types.InheritanceNode inheritanceNode) {
                    if (allowedGroups.contains(inheritanceNode.getGroupName().toLowerCase())) {
                        return true;
                    }
                }
            }
            // Also check inherited groups (e.g., admin inherits from mod)
            Collection<net.luckperms.api.model.group.Group> inheritedGroups = user.getInheritedGroups(
                    user.getQueryOptions());
            for (net.luckperms.api.model.group.Group group : inheritedGroups) {
                if (allowedGroups.contains(group.getName().toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Maintenance] Failed to check LuckPerms groups for " + uuid + ": " + e.getMessage());
        }
        return false;
    }

    public boolean isOpsBypass() {
        return opsBypass;
    }

    /** Returns an unmodifiable copy of the allowed LuckPerms group names. */
    public List<String> getAllowedGroups() {
        return Collections.unmodifiableList(allowedGroups);
    }

    public boolean addAllowedGroup(String group) {
        String lower = group.toLowerCase();
        if (allowedGroups.contains(lower)) return false;
        allowedGroups.add(lower);
        maintenanceConfig.set("allowed-groups", new ArrayList<>(allowedGroups));
        saveMaintenanceConfig();
        return true;
    }

    public boolean removeAllowedGroup(String group) {
        String lower = group.toLowerCase();
        if (!allowedGroups.remove(lower)) return false;
        maintenanceConfig.set("allowed-groups", new ArrayList<>(allowedGroups));
        saveMaintenanceConfig();
        return true;
    }

    // ==================== Icon ====================

    /** Returns the cached maintenance server icon, or null if not configured/loaded. */
    public CachedServerIcon getCachedIcon() {
        return cachedIcon;
    }

    // ==================== Messages ====================

    /**
     * Build the kick screen Component shown to players denied access during maintenance.
     */
    public net.kyori.adventure.text.Component buildKickMessage() {
        String msg = kickMessage;
        String cooldownText = getCooldownText();
        if (cooldownText != null) {
            msg = msg.replace("{cooldown_line}", "Back in ~" + cooldownText + ".");
        } else {
            msg = msg.replace("{cooldown_line}", "Please try again later.");
        }
        return CC.translate(msg);
    }

    /**
     * Get the join warning message sent to whitelisted players when they join during maintenance.
     */
    public String getJoinWarning() {
        return joinWarningMessage;
    }

    /**
     * Get the full maintenance MOTD with placeholders resolved.
     * This REPLACES the normal server MOTD entirely when maintenance is active.
     */
    public String getMaintenanceMotd() {
        String motd = maintenanceMotd;
        String cooldownText = getCooldownText();
        if (cooldownText != null) {
            motd = motd.replace("{cooldown_text}", "back in: ~" + cooldownText);
        } else {
            motd = motd.replace("{cooldown_text}", "try again later");
        }
        return motd;
    }

    // ==================== Helpers ====================

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m " + (seconds % 60) + "s";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h " + (minutes % 60) + "m";
        long days = hours / 24;
        return days + "d " + (hours % 24) + "h";
    }

    /**
     * Parse a duration string like "1h", "30m", "2d", "1d12h" into milliseconds.
     * @return millis, or -1 if unparseable
     */
    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) return -1;
        input = input.toLowerCase().trim();
        long total = 0;
        StringBuilder num = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                num.append(c);
            } else {
                if (num.isEmpty()) return -1;
                long val = Long.parseLong(num.toString());
                num.setLength(0);
                switch (c) {
                    case 's' -> total += val * 1000L;
                    case 'm' -> total += val * 60_000L;
                    case 'h' -> total += val * 3_600_000L;
                    case 'd' -> total += val * 86_400_000L;
                    default -> { return -1; }
                }
            }
        }
        // Handle trailing number with no unit (treat as minutes)
        if (!num.isEmpty()) {
            total += Long.parseLong(num.toString()) * 60_000L;
        }
        return total > 0 ? total : -1;
    }
}
