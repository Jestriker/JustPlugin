package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final double startingBalance;
    private final double maxBalance;

    // Vault bridge - null if not using Vault
    private VaultEconomyBridge vaultBridge;
    private boolean usingVault = false;

    // Cache (used only when provider is "justplugin") - thread-safe for async access
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Set<UUID> payToggleOff = ConcurrentHashMap.newKeySet();
    private final Set<UUID> baltopHidden = ConcurrentHashMap.newKeySet();

    public EconomyManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
        this.maxBalance = plugin.getConfig().getDouble("economy.max-balance", 1_000_000_000_000.0);
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
        if (balances.containsKey(uuid)) return balances.get(uuid);
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        double bal = data.getDouble("balance", startingBalance);
        balances.put(uuid, bal);
        return bal;
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
        saveBalance(uuid);
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
        if (getBalance(from) < amount) return false;
        removeBalance(from, amount);
        addBalance(to, amount);
        return true;
    }

    public boolean isPayToggleOff(UUID uuid) {
        if (payToggleOff.contains(uuid)) return true;
        // For offline players not in cache, check disk
        if (!balances.containsKey(uuid)) {
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
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        data.set("paytoggle", payToggleOff.contains(uuid));
        dataManager.savePlayerData(uuid, data);
    }

    public void loadPlayer(UUID uuid) {
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        // Only cache balance locally when using built-in economy (Vault handles its own storage)
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

    public void unloadPlayer(UUID uuid) {
        // Only save balance locally when using built-in economy
        if (!usingVault) {
            saveBalance(uuid);
        }
        balances.remove(uuid);
        payToggleOff.remove(uuid);
        baltopHidden.remove(uuid);
    }

    private void saveBalance(UUID uuid) {
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        data.set("balance", balances.getOrDefault(uuid, startingBalance));
        dataManager.savePlayerData(uuid, data);
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
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        data.set("baltopHidden", baltopHidden.contains(uuid));
        dataManager.savePlayerData(uuid, data);
    }

    /**
     * Scans all player data files and returns a sorted list of (UUID, balance) entries.
     * When using Vault, queries Vault for each known player's balance.
     */
    public List<Map.Entry<UUID, Double>> getAllBalancesSorted() {
        if (usingVault && vaultBridge != null) {
            return vaultBridge.getAllBalancesSorted(dataManager);
        }
        Map<UUID, Double> all = new HashMap<>(balances);
        // Also scan disk for offline players
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
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(all.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return sorted;
    }
}
