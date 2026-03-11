package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private static final double STARTING_BALANCE = 1000.0;

    private final JustPlugin plugin;
    private final File dataFile;
    private YamlConfiguration config;
    private final Map<UUID, Double> balances = new HashMap<>();
    // players who disabled receiving payments
    private final java.util.Set<UUID> payDisabled = new java.util.HashSet<>();

    public EconomyManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "economy.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create economy.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        if (config.getConfigurationSection("balances") != null) {
            for (String key : config.getConfigurationSection("balances").getKeys(false)) {
                balances.put(UUID.fromString(key), config.getDouble("balances." + key));
            }
        }
        if (config.getStringList("payDisabled") != null) {
            for (String s : config.getStringList("payDisabled")) {
                try { payDisabled.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            config.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        config.set("payDisabled", payDisabled.stream().map(UUID::toString).collect(java.util.stream.Collectors.toList()));
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save economy.yml: " + e.getMessage());
        }
    }

    public double getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, STARTING_BALANCE);
    }

    public void setBalance(UUID playerId, double amount) {
        balances.put(playerId, amount);
        config.set("balances." + playerId.toString(), amount);
        trySave();
    }

    public boolean deposit(UUID playerId, double amount) {
        if (amount <= 0) return false;
        setBalance(playerId, getBalance(playerId) + amount);
        return true;
    }

    public boolean withdraw(UUID playerId, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(playerId);
        if (current < amount) return false;
        setBalance(playerId, current - amount);
        return true;
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (!withdraw(from, amount)) return false;
        deposit(to, amount);
        return true;
    }

    public boolean isPayDisabled(UUID playerId) {
        return payDisabled.contains(playerId);
    }

    public boolean togglePay(UUID playerId) {
        if (payDisabled.contains(playerId)) {
            payDisabled.remove(playerId);
            trySave();
            return false; // now enabled
        } else {
            payDisabled.add(playerId);
            trySave();
            return true; // now disabled
        }
    }

    private void trySave() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save economy.yml: " + e.getMessage());
        }
    }
}
