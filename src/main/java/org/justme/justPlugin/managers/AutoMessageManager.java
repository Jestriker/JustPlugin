package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;
import org.justme.justPlugin.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages automated messages sent to players at configurable intervals or scheduled times.
 * Supports interval, schedule, on-the-hour, and on-the-half-hour modes.
 * All scheduling uses SchedulerUtil for Folia compatibility.
 */
@SuppressWarnings({"deprecation", "removal"})
public class AutoMessageManager {

    private final JustPlugin plugin;
    private File configFile;
    private YamlConfiguration config;

    private final Map<String, AutoMessage> messages = new LinkedHashMap<>();
    private final List<SchedulerUtil.CancellableTask> tasks = new ArrayList<>();

    // For schedule/on-the-hour/on-the-half-hour: a single 1-second tick task
    private SchedulerUtil.CancellableTask clockTask;

    // Track which scheduled times have already fired to avoid duplicates
    // Key: messageId + ":" + "HH:mm", Value: the minute timestamp when it last fired
    private final Map<String, Long> firedSchedules = new ConcurrentHashMap<>();

    public AutoMessageManager(JustPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // ==================== Config Loading ====================

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "automessages.yml");
        if (!configFile.exists()) {
            plugin.saveResource("automessages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        parseMessages();
    }

    private void parseMessages() {
        messages.clear();

        ConfigurationSection msgSection = config.getConfigurationSection("messages");
        if (msgSection == null) return;

        String globalPrefix = config.getString("prefix", "<gradient:#00aaff:#00ffaa><bold>Server</bold></gradient> <dark_gray>\u00BB <reset>");
        String globalSound = config.getString("sound", "");

        for (String id : msgSection.getKeys(false)) {
            ConfigurationSection sec = msgSection.getConfigurationSection(id);
            if (sec == null) continue;

            AutoMessage msg = new AutoMessage();
            msg.id = id;
            msg.enabled = sec.getBoolean("enabled", true);
            msg.mode = sec.getString("mode", "interval");
            msg.interval = sec.getString("interval", "10m");
            msg.times = sec.getStringList("times");
            msg.permission = sec.getString("permission", "");
            msg.worlds = sec.getStringList("worlds");
            msg.rotate = sec.getBoolean("rotate", false);

            // Text lines (single message)
            msg.text = sec.getStringList("text");

            // Text list (rotating messages)
            if (sec.isList("text-list")) {
                msg.textList = new ArrayList<>();
                List<?> rawList = sec.getList("text-list");
                if (rawList != null) {
                    for (Object entry : rawList) {
                        if (entry instanceof List<?> innerList) {
                            List<String> lines = new ArrayList<>();
                            for (Object line : innerList) {
                                lines.add(String.valueOf(line));
                            }
                            msg.textList.add(lines);
                        }
                    }
                }
            }

            // Per-message prefix override
            if (sec.contains("prefix")) {
                msg.prefix = sec.getString("prefix", globalPrefix);
            } else {
                msg.prefix = globalPrefix;
            }

            // Per-message sound override
            if (sec.contains("sound")) {
                msg.sound = sec.getString("sound", globalSound);
            } else {
                msg.sound = globalSound;
            }

            msg.currentIndex = 0;
            messages.put(id, msg);
        }
    }

    // ==================== Start / Stop ====================

    public void start() {
        if (!isEnabled()) return;

        boolean needsClockTask = false;

        for (AutoMessage msg : messages.values()) {
            if (!msg.enabled) continue;

            switch (msg.mode) {
                case "interval" -> {
                    long millis = TimeUtil.parseDuration(msg.interval);
                    if (millis <= 0) continue;
                    long ticks = millis / 50L; // convert ms to ticks
                    SchedulerUtil.CancellableTask task = SchedulerUtil.runTaskTimer(
                            plugin, () -> sendMessage(msg), ticks, ticks);
                    tasks.add(task);
                }
                case "schedule", "on-the-hour", "on-the-half-hour" -> needsClockTask = true;
            }
        }

        // Single clock task checks all schedule-based messages every second
        if (needsClockTask) {
            clockTask = SchedulerUtil.runTaskTimer(plugin, this::clockTick, 20L, 20L);
        }
    }

    public void stop() {
        for (SchedulerUtil.CancellableTask task : tasks) {
            task.cancel();
        }
        tasks.clear();

        if (clockTask != null) {
            clockTask.cancel();
            clockTask = null;
        }

        firedSchedules.clear();
    }

    public void reload() {
        stop();
        loadConfig();
        start();
    }

    // ==================== Clock Tick (1-second resolution) ====================

    private void clockTick() {
        String timezone = plugin.getConfig().getString("timezone", "UTC");
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezone);
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();

        // Only fire during the first second of each minute to avoid duplicates
        if (second > 1) return;

        // Unique key for this minute to prevent double-firing
        long minuteKey = hour * 60L + minute;

        for (AutoMessage msg : messages.values()) {
            if (!msg.enabled) continue;

            switch (msg.mode) {
                case "schedule" -> {
                    for (String timeStr : msg.times) {
                        String schedKey = msg.id + ":" + timeStr;
                        Long lastFired = firedSchedules.get(schedKey);
                        if (lastFired != null && lastFired == minuteKey) continue;

                        try {
                            LocalTime scheduled = LocalTime.parse(timeStr);
                            if (scheduled.getHour() == hour && scheduled.getMinute() == minute) {
                                firedSchedules.put(schedKey, minuteKey);
                                sendMessage(msg);
                            }
                        } catch (Exception ignored) {
                            // Invalid time format - skip
                        }
                    }
                }
                case "on-the-hour" -> {
                    if (minute == 0) {
                        String schedKey = msg.id + ":hour";
                        Long lastFired = firedSchedules.get(schedKey);
                        if (lastFired != null && lastFired == minuteKey) continue;
                        firedSchedules.put(schedKey, minuteKey);
                        sendMessage(msg);
                    }
                }
                case "on-the-half-hour" -> {
                    if (minute == 0 || minute == 30) {
                        String schedKey = msg.id + ":half";
                        Long lastFired = firedSchedules.get(schedKey);
                        if (lastFired != null && lastFired == minuteKey) continue;
                        firedSchedules.put(schedKey, minuteKey);
                        sendMessage(msg);
                    }
                }
            }
        }
    }

    // ==================== Message Sending ====================

    public void sendMessage(AutoMessage msg) {
        List<String> lines;

        if (msg.rotate && msg.textList != null && !msg.textList.isEmpty()) {
            // Rotating: pick current index and advance
            lines = msg.textList.get(msg.currentIndex % msg.textList.size());
            msg.currentIndex = (msg.currentIndex + 1) % msg.textList.size();
        } else {
            lines = msg.text;
        }

        if (lines == null || lines.isEmpty()) return;

        // Build components with prefix
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            String full = msg.prefix + line;
            components.add(CC.translate(full));
        }

        // Resolve sound
        Sound sound = null;
        if (msg.sound != null && !msg.sound.isEmpty()) {
            try {
                sound = Sound.valueOf(msg.sound.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Invalid sound name - skip
            }
        }

        // Send to matching players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Permission filter
            if (msg.permission != null && !msg.permission.isEmpty()) {
                if (!player.hasPermission(msg.permission)) continue;
            }

            // World filter
            if (msg.worlds != null && !msg.worlds.isEmpty()) {
                String worldName = player.getWorld().getName();
                if (!msg.worlds.contains(worldName)) continue;
            }

            for (Component component : components) {
                player.sendMessage(component);
            }

            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Force-send a message by ID (ignoring schedule/interval).
     * Returns true if message was found and sent.
     */
    public boolean forceSend(String id) {
        AutoMessage msg = messages.get(id);
        if (msg == null) return false;
        sendMessage(msg);
        return true;
    }

    // ==================== Toggle ====================

    /**
     * Toggle a message on/off. Saves to config file and restarts tasks.
     * Returns the new enabled state, or null if not found.
     */
    public Boolean toggle(String id) {
        AutoMessage msg = messages.get(id);
        if (msg == null) return null;

        msg.enabled = !msg.enabled;
        config.set("messages." + id + ".enabled", msg.enabled);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save automessages.yml: " + e.getMessage());
        }

        // Restart tasks to apply changes
        stop();
        start();

        return msg.enabled;
    }

    // ==================== Getters ====================

    public boolean isEnabled() {
        return config.getBoolean("enabled", false);
    }

    public Map<String, AutoMessage> getMessages() {
        return Collections.unmodifiableMap(messages);
    }

    public int getMessageCount() {
        return messages.size();
    }

    // ==================== Data Class ====================

    public static class AutoMessage {
        public String id;
        public boolean enabled;
        public String mode;
        public String interval;
        public List<String> times;
        public List<String> text;
        public List<List<String>> textList;
        public boolean rotate;
        public String permission;
        public List<String> worlds;
        public String prefix;
        public String sound;
        public int currentIndex;

        /**
         * Get a human-readable detail string for display in /automessage list.
         */
        public String getDetail() {
            return switch (mode) {
                case "interval" -> "every " + interval;
                case "schedule" -> "at " + String.join(", ", times);
                case "on-the-hour" -> "every full hour";
                case "on-the-half-hour" -> "every 30 min";
                default -> mode;
            };
        }
    }
}
