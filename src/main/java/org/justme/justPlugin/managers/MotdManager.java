package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages two separate MOTDs from a dedicated motd.yml file:
 * <ul>
 *   <li><b>server-motd</b> - shown in the Minecraft server list (supports profiles)</li>
 *   <li><b>join-motd</b> - shown to players in chat when they join</li>
 * </ul>
 * <p>
 * Server MOTD supports multiple profiles that cycle on a delay or change randomly on each refresh.
 */
public class MotdManager {

    public static final String DEFAULT_SERVER_MOTD =
            "<gradient:#00aaff:#00ffaa><bold>JustPlugin Server</bold></gradient>\n<gray>Powered by JustPlugin";

    public static final String DEFAULT_JOIN_MOTD =
            "<gradient:#00aaff:#00ffaa><bold>Welcome to the server, {player}!</bold></gradient>\n<gray>Type <yellow>/help</yellow> for commands.";

    private final JustPlugin plugin;
    private final File motdFile;
    private YamlConfiguration motdConfig;

    // MOTD profiles
    private List<String> serverMotdProfiles = new ArrayList<>();
    private String motdMode = "static"; // "static", "cycle", "random"
    private long motdDelayMs = 30_000;
    private int currentProfileIndex = 0;
    private BukkitTask cycleTask;

    public MotdManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.motdFile = new File(plugin.getDataFolder(), "motd.yml");
        load();
    }

    private void load() {
        if (!motdFile.exists()) {
            String legacyMotd = plugin.getConfig().getString("motd", null);
            if (legacyMotd != null && !legacyMotd.isEmpty()) {
                motdConfig = createFreshConfig();
                motdConfig.set("join-motd", legacyMotd);
                save();
                plugin.getConfig().set("motd", null);
                plugin.saveConfig();
                plugin.getLogger().info("[MOTD] Migrated legacy motd from config.yml to motd.yml (as join-motd)");
            } else {
                InputStream defaultStream = plugin.getResource("motd.yml");
                if (defaultStream != null) {
                    motdConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                } else {
                    motdConfig = createFreshConfig();
                }
                save();
            }
        } else {
            motdConfig = YamlConfiguration.loadConfiguration(motdFile);
        }

        // Migrate old single "motd" key
        if (motdConfig.contains("motd") && !motdConfig.contains("join-motd")) {
            motdConfig.set("join-motd", motdConfig.getString("motd", DEFAULT_JOIN_MOTD));
            motdConfig.set("motd", null);
            save();
        }

        // Migrate old single server-motd string to profiles format
        if (motdConfig.contains("server-motd") && motdConfig.isString("server-motd")) {
            String oldServerMotd = motdConfig.getString("server-motd", DEFAULT_SERVER_MOTD);
            motdConfig.set("server-motd", null);
            motdConfig.set("server-motd.profiles", List.of(oldServerMotd));
            motdConfig.set("server-motd.mode", "static");
            motdConfig.set("server-motd.delay", "30s");
            save();
            plugin.getLogger().info("[MOTD] Migrated server-motd to profiles format.");
        }

        if (!motdConfig.contains("join-motd")) {
            motdConfig.set("join-motd", DEFAULT_JOIN_MOTD);
            save();
        }
        if (!motdConfig.contains("server-motd.profiles")) {
            motdConfig.set("server-motd.profiles", List.of(DEFAULT_SERVER_MOTD));
            motdConfig.set("server-motd.mode", "static");
            motdConfig.set("server-motd.delay", "30s");
            save();
        }

        serverMotdProfiles = new ArrayList<>(motdConfig.getStringList("server-motd.profiles"));
        if (serverMotdProfiles.isEmpty()) serverMotdProfiles.add(DEFAULT_SERVER_MOTD);

        motdMode = motdConfig.getString("server-motd.mode", "static").toLowerCase();
        motdDelayMs = parseDuration(motdConfig.getString("server-motd.delay", "30s"));
        if (motdDelayMs <= 0) motdDelayMs = 30_000;
        currentProfileIndex = 0;

        startCycleTask();
    }

    private YamlConfiguration createFreshConfig() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("server-motd.profiles", List.of(DEFAULT_SERVER_MOTD));
        config.set("server-motd.mode", "static");
        config.set("server-motd.delay", "30s");
        config.set("join-motd", DEFAULT_JOIN_MOTD);
        return config;
    }

    // ===== Server MOTD =====

    public String getServerMotd() {
        if (serverMotdProfiles.isEmpty()) return DEFAULT_SERVER_MOTD;
        return switch (motdMode) {
            case "random" -> serverMotdProfiles.get(ThreadLocalRandom.current().nextInt(serverMotdProfiles.size()));
            case "cycle" -> serverMotdProfiles.get(currentProfileIndex % serverMotdProfiles.size());
            default -> serverMotdProfiles.get(0);
        };
    }

    public List<String> getServerMotdProfiles() { return Collections.unmodifiableList(serverMotdProfiles); }
    public String getMotdMode() { return motdMode; }

    public void setServerMotd(String motd) {
        serverMotdProfiles = new ArrayList<>(List.of(motd));
        motdConfig.set("server-motd.profiles", serverMotdProfiles);
        save();
    }

    public void resetServerMotd() {
        serverMotdProfiles = new ArrayList<>(List.of(DEFAULT_SERVER_MOTD));
        motdConfig.set("server-motd.profiles", serverMotdProfiles);
        save();
    }

    // ===== Join MOTD =====

    public String getJoinMotd() { return motdConfig.getString("join-motd", DEFAULT_JOIN_MOTD); }

    public void setJoinMotd(String motd) {
        motdConfig.set("join-motd", motd);
        save();
    }

    public void resetJoinMotd() {
        motdConfig.set("join-motd", DEFAULT_JOIN_MOTD);
        save();
    }

    // ===== Legacy compat =====

    @Deprecated public String getMotd() { return getJoinMotd(); }
    @Deprecated public void setMotd(String motd) { setJoinMotd(motd); }
    @Deprecated public void resetMotd() { resetJoinMotd(); resetServerMotd(); }

    // ===== Cycle Task =====

    private void startCycleTask() {
        stopCycleTask();
        if ("cycle".equals(motdMode) && serverMotdProfiles.size() > 1) {
            long ticks = Math.max(20, motdDelayMs / 50);
            cycleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                currentProfileIndex = (currentProfileIndex + 1) % serverMotdProfiles.size();
            }, ticks, ticks);
        }
    }

    private void stopCycleTask() {
        if (cycleTask != null) { cycleTask.cancel(); cycleTask = null; }
    }

    // ===== Reload / Save =====

    public void reload() {
        stopCycleTask();
        if (motdFile.exists()) motdConfig = YamlConfiguration.loadConfiguration(motdFile);
        serverMotdProfiles = new ArrayList<>(motdConfig.getStringList("server-motd.profiles"));
        if (serverMotdProfiles.isEmpty()) serverMotdProfiles.add(DEFAULT_SERVER_MOTD);
        motdMode = motdConfig.getString("server-motd.mode", "static").toLowerCase();
        motdDelayMs = parseDuration(motdConfig.getString("server-motd.delay", "30s"));
        if (motdDelayMs <= 0) motdDelayMs = 30_000;
        currentProfileIndex = 0;
        startCycleTask();
    }

    public void shutdown() { stopCycleTask(); }

    private void save() {
        try { motdConfig.save(motdFile); }
        catch (IOException e) { plugin.getLogger().severe("Failed to save motd.yml: " + e.getMessage()); }
    }

    private static long parseDuration(String input) {
        if (input == null || input.isEmpty()) return -1;
        input = input.toLowerCase().trim();
        if (input.endsWith("ms")) {
            try { return Long.parseLong(input.replace("ms", "")); }
            catch (NumberFormatException e) { return -1; }
        }
        long total = 0;
        StringBuilder num = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) { num.append(c); }
            else {
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
        if (!num.isEmpty()) total += Long.parseLong(num.toString()) * 1000L;
        return total > 0 ? total : -1;
    }
}
