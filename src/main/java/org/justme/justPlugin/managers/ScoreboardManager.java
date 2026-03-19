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
 */
public class ScoreboardManager {

    private final JustPlugin plugin;
    private final File scoreboardFile;
    private YamlConfiguration config;

    private boolean enabled;
    private int updateInterval; // ticks
    private boolean globalEmojis;
    private String title;
    private List<LineEntry> lines = new ArrayList<>();

    private BukkitTask updateTask;
    private final Set<UUID> disabled = new HashSet<>(); // players who toggled it off
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();

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
    }

    /**
     * Stops the update task and clears all scoreboards.
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        // Reset all players to default scoreboard
        for (var entry : playerBoards.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerBoards.clear();
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
        title = config.getString("title", "<gradient:#00aaff:#00ffaa><bold>JustPlugin</bold></gradient>");

        // Load lines
        lines.clear();
        var linesList = config.getMapList("lines");
        for (var rawLine : linesList) {
            Object textObj = rawLine.get("text");
            Object emojiObj = rawLine.get("emoji");
            Object showObj = rawLine.get("show-emoji");
            String text = textObj != null ? String.valueOf(textObj) : "";
            String emoji = emojiObj != null ? String.valueOf(emojiObj) : "";
            boolean showEmoji = showObj instanceof Boolean ? (Boolean) showObj : true;
            lines.add(new LineEntry(text, emoji, showEmoji));
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
        // Re-load disabled states from player data
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!disabled.contains(p.getUniqueId())) {
                show(p);
            }
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

        // Check if player has disabled it
        if (disabled.contains(uuid)) return;

        // Load preference from disk if not in cache
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (data.getBoolean("scoreboard-disabled", false)) {
            disabled.add(uuid);
            return;
        }

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
     * Toggle the scoreboard on/off for a player.
     * @return true if the scoreboard is now visible.
     */
    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabled.contains(uuid)) {
            disabled.remove(uuid);
            // Persist
            YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
            data.set("scoreboard-disabled", false);
            plugin.getDataManager().savePlayerData(uuid, data);
            show(player);
            return true;
        } else {
            disabled.add(uuid);
            // Persist
            YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
            data.set("scoreboard-disabled", true);
            plugin.getDataManager().savePlayerData(uuid, data);
            hide(player);
            return false;
        }
    }

    /**
     * Clean up when player quits.
     */
    public void handleQuit(UUID uuid) {
        playerBoards.remove(uuid);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisabledForPlayer(UUID uuid) {
        return disabled.contains(uuid);
    }

    // =========== Update Logic ===========

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

        // Create new objective
        String resolvedTitle = PlaceholderResolver.resolve(player, plugin, title);
        Component titleComponent = CC.translate(resolvedTitle);
        Objective objective = board.registerNewObjective("jp_sidebar", Criteria.DUMMY, titleComponent);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Bukkit sidebar shows lines in descending score order.
        // We assign scores from lines.size() down to 1.
        int maxLines = Math.min(lines.size(), 15); // Sidebar supports max 15 lines
        for (int i = 0; i < maxLines; i++) {
            LineEntry entry = lines.get(i);
            String lineText = entry.text;

            // Resolve emoji
            if (globalEmojis && entry.showEmoji && !entry.emoji.isEmpty()) {
                lineText = lineText.replace("{emoji}", entry.emoji);
            } else {
                lineText = lineText.replace("{emoji}", "");
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

        public LineEntry(String text, String emoji, boolean showEmoji) {
            this.text = text;
            this.emoji = emoji;
            this.showEmoji = showEmoji;
        }
    }
}


