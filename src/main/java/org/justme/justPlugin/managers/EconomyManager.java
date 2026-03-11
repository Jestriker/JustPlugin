package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.util.*;

public class EconomyManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final double startingBalance;

    // Cache
    private final Map<UUID, Double> balances = new HashMap<>();
    private final Set<UUID> payToggleOff = new HashSet<>();

    public EconomyManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
    }

    public double getBalance(UUID uuid) {
        if (balances.containsKey(uuid)) return balances.get(uuid);
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        double bal = data.getDouble("balance", startingBalance);
        balances.put(uuid, bal);
        return bal;
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, amount);
        saveBalance(uuid);
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    public boolean pay(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        if (getBalance(from) < amount) return false;
        if (isPayToggleOff(to)) return false;
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
        balances.put(uuid, data.getDouble("balance", startingBalance));
        if (data.getBoolean("paytoggle", false)) {
            payToggleOff.add(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        saveBalance(uuid);
        balances.remove(uuid);
        payToggleOff.remove(uuid);
    }

    private void saveBalance(UUID uuid) {
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        data.set("balance", balances.getOrDefault(uuid, startingBalance));
        dataManager.savePlayerData(uuid, data);
    }

    public String format(double amount) {
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "$");
        return symbol + String.format("%,.2f", amount);
    }
}

