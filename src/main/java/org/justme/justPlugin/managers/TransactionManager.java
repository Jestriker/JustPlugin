package org.justme.justPlugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages transaction history for the economy system.
 * Stores transaction records in player data YAML files under the "transactions" section.
 * Supports configurable retention periods and max entries per player.
 */
public class TransactionManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;

    // Cache of loaded transactions per player - avoids repeated YAML parsing
    private final Map<UUID, List<TransactionEntry>> cache = new ConcurrentHashMap<>();

    private SchedulerUtil.CancellableTask purgeTask;

    public TransactionManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();

        // Schedule periodic purge every 30 minutes (36000 ticks)
        if (isEnabled()) {
            purgeTask = SchedulerUtil.runAsyncTimer(plugin, this::purgeExpired, 36000L, 36000L);
        }
    }

    /**
     * Represents a single transaction entry.
     */
    public static class TransactionEntry {
        public final String id;
        public final String type;
        public final UUID player;
        public final double amount;
        public final long timestamp;
        public final Map<String, String> details;

        public TransactionEntry(String id, String type, UUID player, double amount, long timestamp, Map<String, String> details) {
            this.id = id;
            this.type = type;
            this.player = player;
            this.amount = amount;
            this.timestamp = timestamp;
            this.details = details != null ? details : new HashMap<>();
        }
    }

    /**
     * @return true if transaction history is enabled in config.
     */
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("economy.transaction-history.enabled", true);
    }

    /**
     * @return the configured retention period in days.
     */
    public int getRetentionDays() {
        return plugin.getConfig().getInt("economy.transaction-history.retention-days", 30);
    }

    /**
     * @return the maximum number of entries per player.
     */
    public int getMaxEntries() {
        return plugin.getConfig().getInt("economy.transaction-history.max-entries", 500);
    }

    /**
     * @return true if addcash transactions should show the staff name to the target player.
     */
    public boolean showAddcashToPlayer() {
        return plugin.getConfig().getBoolean("economy.transaction-history.addcash.show-to-player", true);
    }

    /**
     * @return true if paynote redemption should show the creator's name.
     */
    public boolean showPaynoteCreator() {
        return plugin.getConfig().getBoolean("economy.transaction-history.paynote.show-creator", true);
    }

    /**
     * @return the paynote notify mode: "name", "anonymous", or "none".
     */
    public String getPaynoteNotifyMode() {
        return plugin.getConfig().getString("economy.transaction-history.paynote.notify-creator", "name");
    }

    /**
     * Log a transaction for a player.
     *
     * @param player  the player this transaction belongs to
     * @param type    the transaction type (PAY, PAYNOTE_CREATE, PAYNOTE_REDEEM, ADDCASH, TRADE, API)
     * @param amount  the transaction amount
     * @param details flexible key-value pairs (from, to, creator, staff, hidden, etc.)
     */
    public void logTransaction(UUID player, String type, double amount, Map<String, String> details) {
        if (!isEnabled()) return;

        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        TransactionEntry entry = new TransactionEntry(id, type, player, amount, timestamp, details);

        // Update cache
        cache.computeIfAbsent(player, k -> loadFromDisk(k));
        List<TransactionEntry> list = cache.get(player);
        if (list == null) {
            list = new ArrayList<>();
            cache.put(player, list);
        }
        list.addFirst(entry); // newest first

        // Trim to max entries
        int max = getMaxEntries();
        while (list.size() > max) {
            list.removeLast();
        }

        // Save to disk async
        saveToDiskAsync(player);
    }

    /**
     * Get all transactions for a player, sorted newest first.
     */
    public List<TransactionEntry> getTransactions(UUID player) {
        if (!isEnabled()) return List.of();

        List<TransactionEntry> list = cache.get(player);
        if (list == null) {
            list = loadFromDisk(player);
            cache.put(player, list);
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Get a specific transaction by ID.
     */
    public TransactionEntry getTransaction(UUID player, String id) {
        List<TransactionEntry> list = getTransactions(player);
        for (TransactionEntry entry : list) {
            if (entry.id.equals(id)) return entry;
        }
        return null;
    }

    /**
     * Purge expired transactions for a specific player.
     */
    public void purgeExpired(UUID player) {
        if (!isEnabled()) return;

        int retentionDays = getRetentionDays();
        if (retentionDays <= 0) return; // 0 = keep forever

        long cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);

        List<TransactionEntry> list = cache.get(player);
        if (list == null) {
            list = loadFromDisk(player);
            cache.put(player, list);
        }

        boolean changed = list.removeIf(e -> e.timestamp < cutoff);
        if (changed) {
            saveToDiskAsync(player);
        }
    }

    /**
     * Purge expired transactions for all cached players.
     */
    public void purgeExpired() {
        for (UUID player : cache.keySet()) {
            purgeExpired(player);
        }
    }

    /**
     * Load transactions into cache when a player joins.
     */
    public void loadPlayer(UUID player) {
        if (!isEnabled()) return;
        List<TransactionEntry> list = loadFromDisk(player);
        cache.put(player, list);

        // Purge expired on load
        purgeExpired(player);
    }

    /**
     * Save and remove transactions from cache when a player quits.
     */
    public void unloadPlayer(UUID player) {
        if (!isEnabled()) return;
        List<TransactionEntry> list = cache.remove(player);
        if (list != null) {
            saveToDisk(player, list);
        }
    }

    /**
     * Shutdown the manager - cancel tasks and save all cached data.
     */
    public void shutdown() {
        if (purgeTask != null) {
            purgeTask.cancel();
            purgeTask = null;
        }
        // Save all cached transactions
        for (Map.Entry<UUID, List<TransactionEntry>> entry : cache.entrySet()) {
            saveToDisk(entry.getKey(), entry.getValue());
        }
        cache.clear();
    }

    // --- Disk I/O ---

    private List<TransactionEntry> loadFromDisk(UUID player) {
        List<TransactionEntry> list = new ArrayList<>();
        YamlConfiguration data = dataManager.getPlayerData(player);
        ConfigurationSection section = data.getConfigurationSection("transactions");
        if (section == null) return list;

        long cutoff = 0;
        int retentionDays = getRetentionDays();
        if (retentionDays > 0) {
            cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection txSection = section.getConfigurationSection(key);
            if (txSection == null) continue;

            long timestamp = txSection.getLong("timestamp", 0L);
            if (retentionDays > 0 && timestamp < cutoff) continue; // Skip expired

            String id = txSection.getString("id", key);
            String type = txSection.getString("type", "UNKNOWN");
            double amount = txSection.getDouble("amount", 0.0);

            Map<String, String> details = new HashMap<>();
            ConfigurationSection detailsSection = txSection.getConfigurationSection("details");
            if (detailsSection != null) {
                for (String detailKey : detailsSection.getKeys(false)) {
                    details.put(detailKey, detailsSection.getString(detailKey, ""));
                }
            }

            list.add(new TransactionEntry(id, type, player, amount, timestamp, details));
        }

        // Sort newest first
        list.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        // Trim to max
        int max = getMaxEntries();
        while (list.size() > max) {
            list.removeLast();
        }

        return list;
    }

    private void saveToDisk(UUID player, List<TransactionEntry> list) {
        YamlConfiguration data = dataManager.getPlayerData(player);

        // Clear existing transactions section
        data.set("transactions", null);

        if (list != null && !list.isEmpty()) {
            int index = 0;
            for (TransactionEntry entry : list) {
                String path = "transactions." + index;
                data.set(path + ".id", entry.id);
                data.set(path + ".type", entry.type);
                data.set(path + ".amount", entry.amount);
                data.set(path + ".timestamp", entry.timestamp);
                for (Map.Entry<String, String> detail : entry.details.entrySet()) {
                    data.set(path + ".details." + detail.getKey(), detail.getValue());
                }
                index++;
            }
        }

        dataManager.savePlayerData(player, data);
    }

    private void saveToDiskAsync(UUID player) {
        List<TransactionEntry> list = cache.get(player);
        if (list == null) return;
        // Make a snapshot to avoid concurrent modification
        List<TransactionEntry> snapshot = new ArrayList<>(list);
        SchedulerUtil.runAsync(plugin, () -> saveToDisk(player, snapshot));
    }

    /**
     * Format a timestamp as a human-readable date string.
     */
    public String formatDate(long timestamp) {
        if (timestamp == 0) return "Unknown";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    /**
     * Format a timestamp as a relative "X ago" string.
     */
    public String formatRelative(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 0) return "just now";

        long seconds = diff / 1000;
        if (seconds < 60) return seconds + "s ago";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";

        long days = hours / 24;
        if (days == 1) return "1 day ago";
        return days + " days ago";
    }
}
