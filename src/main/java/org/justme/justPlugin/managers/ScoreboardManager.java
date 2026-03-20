package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.PlaceholderResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Manages a configurable sidebar scoreboard with per-player placeholder resolution.
 * Configuration lives in scoreboard.yml (separate from config.yml).
 * Supports 50+ placeholder variables, configurable emojis, title, lines, and footer.
 * Features animated wave gradient title and fast ping refresh.
 */
public class ScoreboardManager {

    private final JustPlugin plugin;
    private final File scoreboardFile;
    private YamlConfiguration config;

    private boolean enabled;
    private int updateInterval; // ticks
    private boolean globalEmojis;
    private String title;
    private String timeFormat;
    private String playtimeFormat;
    private String defaultPlaytimeMode; // "total" or "session"
    private List<LineEntry> lines = new ArrayList<>();

    // Wave gradient animation settings
    private boolean waveEnabled;
    private String waveColor1;
    private String waveColor2;
    private int waveSpeed; // ticks between wave frame changes
    private int waveFrame = 0; // Current animation frame (0-19)

    // Ping refresh settings
    private int pingRefreshInterval; // ticks

    private BukkitTask updateTask;
    private BukkitTask waveTask;
    private BukkitTask pingTask;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private final Map<UUID, Integer> lastPing = new HashMap<>();

    public ScoreboardManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        loadConfig();
    }

    /**
     * Starts the repeating update task. Call this after construction.
     */
    public void start() {
        if (!enabled) return;
        long interval = Math.max(1, updateInterval);
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L * 2, interval);

        // Wave animation task - advances the wave frame every waveSpeed ticks (default: 20 = 1 second)
        if (waveEnabled) {
            long waveInterval = Math.max(1, waveSpeed);
            waveTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                waveFrame = (waveFrame + 1) % 20; // 20 frames for smooth wave
            }, waveInterval, waveInterval);
        }

        // Ping refresh task - checks every 5 seconds (100 ticks) by default
        long pingInterval = Math.max(20, pingRefreshInterval);
        pingTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshPingIfChanged, pingInterval, pingInterval);
    }

    /**
     * Stops the update task and clears all scoreboards.
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        if (waveTask != null) {
            waveTask.cancel();
            waveTask = null;
        }
        if (pingTask != null) {
            pingTask.cancel();
            pingTask = null;
        }
        // Reset all players to default scoreboard
        for (var entry : playerBoards.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerBoards.clear();
        lastPing.clear();
    }

    /**
     * Check if ping changed for any player and trigger a scoreboard update if so.
     * This runs on a faster interval than the main update to keep ping responsive.
     */
    private void refreshPingIfChanged() {
        for (var entry : new HashMap<>(playerBoards).entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;
            int currentPing = p.getPing();
            Integer previous = lastPing.get(entry.getKey());
            if (previous == null || previous != currentPing) {
                lastPing.put(entry.getKey(), currentPing);
                updatePlayer(p);
            }
        }
    }

    // =========== Configuration ===========

    private void loadConfig() {
        // Save default if it doesn't exist
        if (!scoreboardFile.exists()) {
            InputStream defaultStream = plugin.getResource("scoreboard.yml");
            if (defaultStream != null) {
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            } else {
                config = new YamlConfiguration();
                config.set("enabled", false);
            }
            saveConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(scoreboardFile);
        }

        enabled = config.getBoolean("enabled", true);
        updateInterval = config.getInt("update-interval", 20);
        globalEmojis = config.getBoolean("global-emojis", true);
        title = config.getString("title", "<gradient:#00aaff:#00ffaa><bold>JustServer</bold></gradient>");
        timeFormat = config.getString("time-format", "HH:mm");
        playtimeFormat = config.getString("playtime-format", "compact");

        // Wave gradient animation settings
        waveEnabled = config.getBoolean("wave-title.enabled", true);
        waveColor1 = config.getString("wave-title.color1", "#00aaff");
        waveColor2 = config.getString("wave-title.color2", "#00ffaa");
        waveSpeed = config.getInt("wave-title.speed", 20); // ticks between frames (20 = 1 second)

        // Ping refresh interval (ticks) - default 100 (5 seconds)
        pingRefreshInterval = config.getInt("ping-refresh-interval", 100);

        // Default playtime mode: "total" or "session"
        defaultPlaytimeMode = config.getString("default-playtime", "total");

        // Load lines
        lines.clear();
        var linesList = config.getMapList("lines");
        for (var rawLine : linesList) {
            Object textObj = rawLine.get("text");
            Object emojiObj = rawLine.get("emoji");
            Object showObj = rawLine.get("show-emoji");
            Object condObj = rawLine.get("condition");
            String text = textObj != null ? String.valueOf(textObj) : "";
            String emoji = emojiObj != null ? String.valueOf(emojiObj) : "";
            boolean showEmoji = showObj instanceof Boolean ? (Boolean) showObj : true;
            String condition = condObj != null ? String.valueOf(condObj) : "";
            lines.add(new LineEntry(text, emoji, showEmoji, condition));
        }
    }

    private void saveConfig() {
        try {
            config.save(scoreboardFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save scoreboard.yml: " + e.getMessage());
        }
    }

    /**
     * Reload the config from disk and restart the task.
     */
    public void reload() {
        stop();
        loadConfig();
        // Show scoreboard to all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            show(p);
        }
        start();
    }

    // =========== Player Management ===========

    /**
     * Show the scoreboard to a player (called on join).
     */
    public void show(Player player) {
        if (!enabled) return;
        UUID uuid = player.getUniqueId();


        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        playerBoards.put(uuid, board);
        player.setScoreboard(board);
        updatePlayer(player);
    }

    /**
     * Hide the scoreboard from a player.
     */
    public void hide(Player player) {
        UUID uuid = player.getUniqueId();
        playerBoards.remove(uuid);
        if (player.isOnline()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    /**
     * Clean up when player quits.
     */
    public void handleQuit(UUID uuid) {
        playerBoards.remove(uuid);
        lastPing.remove(uuid);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPlaytimeFormat() {
        return playtimeFormat;
    }

    /**
     * Returns "total" or "session" — controls what {playtime_display} resolves to.
     */
    public String getDefaultPlaytimeMode() {
        return defaultPlaytimeMode;
    }


    // =========== Update Logic ===========

    /**
     * Build a wave-animated gradient title.
     * The gradient shifts its phase each frame (0-19), creating a wave effect.
     * Each frame uses a different gradient start/end based on the current phase.
     */
    private String buildWaveTitle() {
        // Parse the base colors
        int r1, g1, b1, r2, g2, b2;
        try {
            java.awt.Color c1 = java.awt.Color.decode(waveColor1);
            java.awt.Color c2 = java.awt.Color.decode(waveColor2);
            r1 = c1.getRed(); g1 = c1.getGreen(); b1 = c1.getBlue();
            r2 = c2.getRed(); g2 = c2.getGreen(); b2 = c2.getBlue();
        } catch (Exception e) {
            return title; // Fallback to static title on parse error
        }

        // Phase shift: 0.0 to 1.0 based on current frame
        double phase = waveFrame / 20.0;

        // Generate shifted colors by interpolating along the spectrum
        // Wave color 1 shifts forward, wave color 2 shifts backward
        int sr1 = clampColor((int)(r1 + (r2 - r1) * phase));
        int sg1 = clampColor((int)(g1 + (g2 - g1) * phase));
        int sb1 = clampColor((int)(b1 + (b2 - b1) * phase));

        int sr2 = clampColor((int)(r2 + (r1 - r2) * phase));
        int sg2 = clampColor((int)(g2 + (g1 - g2) * phase));
        int sb2 = clampColor((int)(b2 + (b1 - b2) * phase));

        String shiftedColor1 = String.format("#%02x%02x%02x", sr1, sg1, sb1);
        String shiftedColor2 = String.format("#%02x%02x%02x", sr2, sg2, sb2);

        // Extract the text content from the title template, preserving formatting tags
        // Replace gradient colors in the title dynamically
        // If title contains a gradient tag, replace its colors
        String animated = title;
        // Try to replace gradient colors in the existing title
        animated = animated.replaceAll(
                "<gradient:[^>]*>",
                "<gradient:" + shiftedColor1 + ":" + shiftedColor2 + ">"
        );
        return animated;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void updateAll() {
        for (var entry : new HashMap<>(playerBoards).entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                updatePlayer(p);
            } else {
                playerBoards.remove(entry.getKey());
            }
        }
    }

    private void updatePlayer(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;

        // Remove old objective
        Objective old = board.getObjective("jp_sidebar");
        if (old != null) old.unregister();

        // Build title - apply wave gradient if enabled
        String currentTitle;
        if (waveEnabled) {
            currentTitle = buildWaveTitle();
        } else {
            currentTitle = title;
        }

        // Create new objective
        String resolvedTitle = PlaceholderResolver.resolve(player, plugin, currentTitle);
        Component titleComponent = CC.translate(resolvedTitle);
        Objective objective = board.registerNewObjective("jp_sidebar", Criteria.DUMMY, titleComponent);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());

        // Filter lines based on conditions
        List<LineEntry> visibleLines = new ArrayList<>();
        for (LineEntry entry : lines) {
            if (!entry.condition.isEmpty()) {
                String condValue = PlaceholderResolver.resolve(player, plugin, "{" + entry.condition + "}");
                if ("false".equalsIgnoreCase(condValue) || condValue.equals("{" + entry.condition + "}")) {
                    continue; // Condition not met, skip this line
                }
            }
            visibleLines.add(entry);
        }

        // Bukkit sidebar shows lines in descending score order.
        // We assign scores from visibleLines.size() down to 1.
        int maxLines = Math.min(visibleLines.size(), 15); // Sidebar supports max 15 lines
        for (int i = 0; i < maxLines; i++) {
            LineEntry entry = visibleLines.get(i);
            String lineText = entry.text;

            // Resolve emoji
            if (globalEmojis && entry.showEmoji && !entry.emoji.isEmpty()) {
                lineText = lineText.replace("{emoji}", entry.emoji);
            } else {
                lineText = lineText.replace("{emoji}", "");
            }

            // Replace {real_time} with custom format before general resolution
            if (lineText.contains("{real_time}")) {
                try {
                    String tz = plugin.getConfig().getString("timezone", "UTC");
                    String formattedTime = java.time.LocalTime.now(java.time.ZoneId.of(tz))
                            .format(java.time.format.DateTimeFormatter.ofPattern(timeFormat));
                    lineText = lineText.replace("{real_time}", formattedTime);
                } catch (Exception e) {
                    lineText = lineText.replace("{real_time}", "??:??");
                }
            }

            // Resolve placeholders
            lineText = PlaceholderResolver.resolve(player, plugin, lineText);

            // Convert to Component
            Component lineComponent = CC.translate(lineText);

            // Use a unique fake player entry for each line
            // We use a team approach to avoid flicker and support duplicates
            String teamName = "jp_l" + i;
            var team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            // Use invisible unique entries (color codes that are unique per line)
            String fakeEntry = getUniqueEntry(i);
            if (!team.hasEntry(fakeEntry)) {
                team.addEntry(fakeEntry);
            }
            team.prefix(lineComponent);
            team.suffix(Component.empty());

            objective.getScore(fakeEntry).setScore(maxLines - i);
        }

        // Clean up extra teams from previous renders that are no longer needed
        for (int i = maxLines; i < 15; i++) {
            String teamName = "jp_l" + i;
            var team = board.getTeam(teamName);
            if (team != null) {
                team.unregister();
            }
        }
    }

    /**
     * Generate a unique, invisible entry for each line index.
     * Uses color codes combinations that are unique but display as blank.
     */
    private String getUniqueEntry(int index) {
        // Use section sign + hex combinations for unique invisible entries
        String[] colors = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7",
                           "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"};
        if (index < colors.length) {
            return colors[index] + "§r";
        }
        // For index >= 16, combine two codes
        return colors[index / colors.length] + colors[index % colors.length] + "§r";
    }

    // =========== Data Classes ===========

    public static class LineEntry {
        public final String text;
        public final String emoji;
        public final boolean showEmoji;
        public final String condition;

        public LineEntry(String text, String emoji, boolean showEmoji, String condition) {
            this.text = text;
            this.emoji = emoji;
            this.showEmoji = showEmoji;
            this.condition = condition != null ? condition : "";
        }

        public LineEntry(String text, String emoji, boolean showEmoji) {
            this(text, emoji, showEmoji, "");
        }
    }
}


