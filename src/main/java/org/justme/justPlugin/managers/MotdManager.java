package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Manages two separate MOTDs from a dedicated motd.yml file:
 * <ul>
 *   <li><b>server-motd</b> - shown in the Minecraft server list (multiplayer screen)</li>
 *   <li><b>join-motd</b> - shown to players in chat when they join</li>
 * </ul>
 * Handles creation, migration from old config.yml / old motd.yml format, loading, saving, and resetting.
 */
public class MotdManager {

    public static final String DEFAULT_SERVER_MOTD =
            "<gradient:#00aaff:#00ffaa><bold>JustPlugin Server</bold></gradient>\n<gray>Powered by JustPlugin";

    public static final String DEFAULT_JOIN_MOTD =
            "<gradient:#00aaff:#00ffaa><bold>Welcome to the server, {player}!</bold></gradient>\n<gray>Type <yellow>/help</yellow> for commands.";

    private final JustPlugin plugin;
    private final File motdFile;
    private YamlConfiguration motdConfig;

    public MotdManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.motdFile = new File(plugin.getDataFolder(), "motd.yml");
        load();
    }

    private void load() {
        if (!motdFile.exists()) {
            // --- Migration from old formats ---
            // 1. Check for legacy "motd" key in config.yml
            String legacyMotd = plugin.getConfig().getString("motd", null);
            if (legacyMotd != null && !legacyMotd.isEmpty()) {
                motdConfig = createFreshConfig();
                motdConfig.set("join-motd", legacyMotd);
                motdConfig.set("server-motd", DEFAULT_SERVER_MOTD);
                save();
                plugin.getConfig().set("motd", null);
                plugin.saveConfig();
                plugin.getLogger().info("[MOTD] Migrated legacy motd from config.yml to motd.yml (as join-motd)");
                return;
            }
            // 2. Create fresh from JAR resource
            InputStream defaultStream = plugin.getResource("motd.yml");
            if (defaultStream != null) {
                motdConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            } else {
                motdConfig = createFreshConfig();
            }
            save();
        } else {
            motdConfig = YamlConfiguration.loadConfiguration(motdFile);
            // Migrate old single "motd" key to the new split format
            if (motdConfig.contains("motd") && !motdConfig.contains("join-motd")) {
                String oldMotd = motdConfig.getString("motd", DEFAULT_JOIN_MOTD);
                motdConfig.set("join-motd", oldMotd);
                motdConfig.set("motd", null);
                if (!motdConfig.contains("server-motd")) {
                    motdConfig.set("server-motd", DEFAULT_SERVER_MOTD);
                }
                save();
                plugin.getLogger().info("[MOTD] Migrated old motd.yml format to new split format (server-motd + join-motd)");
            }
            // Ensure both keys exist
            boolean changed = false;
            if (!motdConfig.contains("server-motd")) {
                motdConfig.set("server-motd", DEFAULT_SERVER_MOTD);
                changed = true;
            }
            if (!motdConfig.contains("join-motd")) {
                motdConfig.set("join-motd", DEFAULT_JOIN_MOTD);
                changed = true;
            }
            if (changed) save();
        }
    }

    private YamlConfiguration createFreshConfig() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("server-motd", DEFAULT_SERVER_MOTD);
        config.set("join-motd", DEFAULT_JOIN_MOTD);
        return config;
    }

    // ===== Server MOTD (server list) =====

    /** Get the server list MOTD. Empty string means use vanilla default. */
    public String getServerMotd() {
        return motdConfig.getString("server-motd", DEFAULT_SERVER_MOTD);
    }

    public void setServerMotd(String motd) {
        motdConfig.set("server-motd", motd);
        save();
    }

    public void resetServerMotd() {
        motdConfig.set("server-motd", DEFAULT_SERVER_MOTD);
        save();
    }

    // ===== Join MOTD (player join message) =====

    /** Get the join MOTD shown to players when they log in. Empty string means disabled. */
    public String getJoinMotd() {
        return motdConfig.getString("join-motd", DEFAULT_JOIN_MOTD);
    }

    public void setJoinMotd(String motd) {
        motdConfig.set("join-motd", motd);
        save();
    }

    public void resetJoinMotd() {
        motdConfig.set("join-motd", DEFAULT_JOIN_MOTD);
        save();
    }

    // ===== Legacy compat (kept for any code that still calls getMotd/setMotd) =====

    /** @deprecated Use {@link #getJoinMotd()} instead. */
    @Deprecated
    public String getMotd() {
        return getJoinMotd();
    }

    /** @deprecated Use {@link #setJoinMotd(String)} instead. */
    @Deprecated
    public void setMotd(String motd) {
        setJoinMotd(motd);
    }

    /** @deprecated Use {@link #resetJoinMotd()} and/or {@link #resetServerMotd()} instead. */
    @Deprecated
    public void resetMotd() {
        resetJoinMotd();
        resetServerMotd();
    }

    // ===== Reload / Save =====

    public void reload() {
        if (motdFile.exists()) {
            motdConfig = YamlConfiguration.loadConfiguration(motdFile);
        }
    }

    private void save() {
        try {
            motdConfig.save(motdFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save motd.yml: " + e.getMessage());
        }
    }
}

