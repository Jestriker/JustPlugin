package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final DatabaseManager databaseManager;
    private final double startingBalance;
    private final double maxBalance;

    // Vault bridge - null if not using Vault
    private VaultEconomyBridge vaultBridge;
    private boolean usingVault = false;

    // Cache (used only when provider is "justplugin") - thread-safe for async access
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> playerLocks = new ConcurrentHashMap<>();
    private final Set<UUID> payToggleOff = ConcurrentHashMap.newKeySet();
    private final Set<UUID> baltopHidden = ConcurrentHashMap.newKeySet();

    public EconomyManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
        this.maxBalance = plugin.getConfig().getDouble("economy.max-balance", 1_000_000_000_000.0);
    }

    /**
     * Returns true if a database StorageProvider (sqlite/mysql) is active,
     * meaning we should route data through StorageProvider instead of DataManager.
     */
    private boolean isUsingDatabase() {
        if (databaseManager == null) return false;
        StorageProvider provider = databaseManager.getProvider();
        if (provider == null) return false;
        String type = provider.getType();
        return "sqlite".equals(type) || "mysql".equals(type);
    }

    /**
     * Gets the active StorageProvider, or null if using YAML.
     */
    private StorageProvider getStorageProvider() {
        return databaseManager != null ? databaseManager.getProvider() : null;
    }

    /**
     * Helper: read a double value from the StorageProvider's player data map.
     */
    private double getDbDouble(UUID uuid, String key, double defaultValue) {
        StorageProvider provider = getStorageProvider();
        if (provider == null) return defaultValue;
        Map<String, Object> data = provider.getPlayerData(uuid);
        Object val = data.get(key);
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    /**
     * Helper: read a boolean value from the StorageProvider's player data map.
     */
    private boolean getDbBoolean(UUID uuid, String key, boolean defaultValue) {
        StorageProvider provider = getStorageProvider();
        if (provider == null) return defaultValue;
        Map<String, Object> data = provider.getPlayerData(uuid);
        Object val = data.get(key);
        if (val instanceof Boolean b) return b;
        if (val instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }

    /**
     * Helper: save a single key-value pair via the StorageProvider, merging with existing data.
     */
    private void saveDbValue(UUID uuid, String key, Object value) {
        StorageProvider provider = getStorageProvider();
        if (provider == null) return;
        Map<String, Object> data = provider.getPlayerData(uuid);
        data.put(key, value);
        provider.savePlayerData(uuid, data);
    }

    /**
     * Attempt to hook into Vault if configured.
     * Call this AFTER the plugin is fully enabled (so Vault is loaded).
     * @return true if Vault was hooked successfully.
     */
    public boolean setupVault() {
        String provider = plugin.getConfig().getString("economy.provider", "justplugin").toLowerCase();
        if (!"vault".equals(provider)) {
            usingVault = false;
            return false;
        }
        vaultBridge = new VaultEconomyBridge(plugin);
        if (vaultBridge.setup()) {
            usingVault = true;
            return true;
        } else {
            plugin.getLogger().warning("[Economy] Vault provider configured but Vault or an economy plugin was not found. Falling back to JustPlugin's built-in economy.");
            vaultBridge = null;
            usingVault = false;
            return false;
        }
    }

    /**
     * @return true if the economy is currently using Vault.
     */
    public boolean isUsingVault() {
        return usingVault;
    }

    /**
     * @return the name of the active economy provider ("JustPlugin" or the Vault provider name).
     */
    public String getProviderName() {
        if (usingVault && vaultBridge != null) {
            return "Vault (" + vaultBridge.getProviderName() + ")";
        }
        return "JustPlugin";
    }

    public double getBalance(UUID uuid) {
        if (usingVault && vaultBridge != null) {
            return vaultBridge.getBalance(uuid);
        }
        return balances.computeIfAbsent(uuid, id -> {
            if (isUsingDatabase()) {
                return getDbDouble(id, "balance", startingBalance);
            }
            YamlConfiguration data = dataManager.getPlayerData(id);
            return data.getDouble("balance", startingBalance);
        });
    }

    private Object getLock(UUID uuid) {
        return playerLocks.computeIfAbsent(uuid, k -> new Object());
    }

    public void setBalance(UUID uuid, double amount) {
        if (usingVault && vaultBridge != null) {
            vaultBridge.setBalance(uuid, amount);
            return;
        }
        // Sanitize: reject NaN/Infinite, clamp to [0, maxBalance]
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            amount = 0;
        }
        amount = Math.max(0, Math.min(amount, maxBalance));
        balances.put(uuid, amount);
        saveBalanceAsync(uuid);
    }

    public void addBalance(UUID uuid, double amount) {
        if (usingVault && vaultBridge != null) {
            vaultBridge.addBalance(uuid, amount);
            return;
        }
        double current = getBalance(uuid);
        // Overflow protection: check if adding would exceed max balance
        double newBalance = current + amount;
        if (newBalance > maxBalance) {
            newBalance = maxBalance;
        }
        if (Double.isNaN(newBalance) || Double.isInfinite(newBalance)) {
            newBalance = current; // keep unchanged on overflow
        }
        setBalance(uuid, newBalance);
    }

    /**
     * @return the configured maximum balance.
     */
    public double getMaxBalance() {
        return maxBalance;
    }

    public boolean removeBalance(UUID uuid, double amount) {
        if (usingVault && vaultBridge != null) {
            return vaultBridge.removeBalance(uuid, amount);
        }
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    public boolean pay(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        if (isPayToggleOff(to)) return false;
        if (usingVault && vaultBridge != null) {
            return vaultBridge.pay(from, to, amount);
        }
        // Lock both players in consistent order to prevent deadlock
        UUID first = from.compareTo(to) < 0 ? from : to;
        UUID second = from.compareTo(to) < 0 ? to : from;
        synchronized (getLock(first)) {
            synchronized (getLock(second)) {
                double fromBalance = getBalance(from);
                if (fromBalance < amount) return false;
                balances.put(from, fromBalance - amount);
                double toBalance = getBalance(to);
                double newToBalance = Math.min(toBalance + amount, maxBalance);
                balances.put(to, newToBalance);
            }
        }
        saveBalanceAsync(from);
        saveBalanceAsync(to);
        return true;
    }

    public boolean isPayToggleOff(UUID uuid) {
        if (payToggleOff.contains(uuid)) return true;
        // For offline players not in cache, check storage
        if (!balances.containsKey(uuid)) {
            if (isUsingDatabase()) {
                return getDbBoolean(uuid, "paytoggle", false);
            }
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            return data.getBoolean("paytoggle", false);
        }
        return false;
    }

    public void togglePay(UUID uuid) {
        if (payToggleOff.contains(uuid)) {
            payToggleOff.remove(uuid);
        } else {
            payToggleOff.add(uuid);
        }
        boolean toggled = payToggleOff.contains(uuid);
        if (isUsingDatabase()) {
            SchedulerUtil.runAsync(plugin, () -> saveDbValue(uuid, "paytoggle", toggled));
        } else {
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            data.set("paytoggle", toggled);
            dataManager.savePlayerData(uuid, data);
        }
    }

    public void loadPlayer(UUID uuid) {
        if (isUsingDatabase()) {
            // Load from database StorageProvider
            if (!usingVault) {
                balances.put(uuid, getDbDouble(uuid, "balance", startingBalance));
            }
            if (getDbBoolean(uuid, "paytoggle", false)) {
                payToggleOff.add(uuid);
            }
            if (getDbBoolean(uuid, "baltopHidden", false)) {
                baltopHidden.add(uuid);
            }
        } else {
            // Load from YAML DataManager
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            if (!usingVault) {
                balances.put(uuid, data.getDouble("balance", startingBalance));
            }
            if (data.getBoolean("paytoggle", false)) {
                payToggleOff.add(uuid);
            }
            if (data.getBoolean("baltopHidden", false)) {
                baltopHidden.add(uuid);
            }
        }
    }

    public void unloadPlayer(UUID uuid) {
        // Only save balance locally when using built-in economy
        if (!usingVault) {
            saveBalance(uuid);
        }
        balances.remove(uuid);
        payToggleOff.remove(uuid);
        baltopHidden.remove(uuid);
        playerLocks.remove(uuid);
    }

    private void saveBalance(UUID uuid) {
        double balance = balances.getOrDefault(uuid, startingBalance);
        if (isUsingDatabase()) {
            saveDbValue(uuid, "balance", balance);
        } else {
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            data.set("balance", balance);
            dataManager.savePlayerData(uuid, data);
        }
    }

    private void saveBalanceAsync(UUID uuid) {
        double balance = balances.getOrDefault(uuid, startingBalance);
        SchedulerUtil.runAsync(plugin, () -> {
            if (isUsingDatabase()) {
                saveDbValue(uuid, "balance", balance);
            } else {
                YamlConfiguration data = dataManager.getPlayerData(uuid);
                data.set("balance", balance);
                dataManager.savePlayerData(uuid, data);
            }
        });
    }

    public String format(double amount) {
        if (usingVault && vaultBridge != null) {
            return vaultBridge.format(amount);
        }
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "$");
        return symbol + String.format("%,.2f", amount);
    }

    // --- Baltop Hidden ---
    public boolean isBaltopHidden(UUID uuid) {
        if (baltopHidden.contains(uuid)) return true;
        if (!balances.containsKey(uuid)) {
            if (isUsingDatabase()) {
                return getDbBoolean(uuid, "baltopHidden", false);
            }
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            return data.getBoolean("baltopHidden", false);
        }
        return false;
    }

    public void toggleBaltopHidden(UUID uuid) {
        if (baltopHidden.contains(uuid)) {
            baltopHidden.remove(uuid);
        } else {
            baltopHidden.add(uuid);
        }
        boolean hidden = baltopHidden.contains(uuid);
        if (isUsingDatabase()) {
            SchedulerUtil.runAsync(plugin, () -> saveDbValue(uuid, "baltopHidden", hidden));
        } else {
            YamlConfiguration data = dataManager.getPlayerData(uuid);
            data.set("baltopHidden", hidden);
            dataManager.savePlayerData(uuid, data);
        }
    }

    /**
     * Scans all player data and returns a sorted list of (UUID, balance) entries.
     * When using Vault, queries Vault for each known player's balance.
     * When using a database StorageProvider, queries the database for offline players.
     * When using YAML, scans player data files on disk.
     */
    public List<Map.Entry<UUID, Double>> getAllBalancesSorted() {
        if (usingVault && vaultBridge != null) {
            return vaultBridge.getAllBalancesSorted(dataManager);
        }
        Map<UUID, Double> all = new HashMap<>(balances);

        if (isUsingDatabase()) {
            // Query database for all player UUIDs and load balances for offline players
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Set<UUID> allUuids = provider.getAllPlayerUUIDs();
                for (UUID uuid : allUuids) {
                    if (!all.containsKey(uuid)) {
                        Map<String, Object> data = provider.getPlayerData(uuid);
                        Object val = data.get("balance");
                        double balance = startingBalance;
                        if (val instanceof Number n) {
                            balance = n.doubleValue();
                        } else if (val instanceof String s) {
                            try { balance = Double.parseDouble(s); } catch (NumberFormatException ignored) {}
                        }
                        all.put(uuid, balance);
                    }
                }
            }
        } else {
            // Scan YAML files on disk for offline players
            File folder = dataManager.getPlayerDataFolder();
            if (folder.exists()) {
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName().replace(".yml", "");
                        try {
                            UUID uuid = UUID.fromString(fileName);
                            if (!all.containsKey(uuid)) {
                                YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
                                all.put(uuid, data.getDouble("balance", startingBalance));
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }

        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(all.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return sorted;
    }
}
