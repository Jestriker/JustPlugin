package org.justme.justPlugin.managers;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.justme.justPlugin.JustPlugin;

import java.util.*;

/**
 * Bridge that delegates all economy operations to Vault's Economy API.
 * Used when config economy.provider is set to "vault".
 * <p>
 * Vault must be installed and an economy provider plugin (e.g. EssentialsX, CMI)
 * must be registered. If Vault or the provider is missing, {@link #setup()} returns false.
 */
public class VaultEconomyBridge {

    private final JustPlugin plugin;
    private Economy vaultEconomy;

    public VaultEconomyBridge(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to hook into Vault's economy.
     * @return true if Vault economy was found and hooked successfully.
     */
    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    /**
     * @return true if the Vault economy is hooked and usable.
     */
    public boolean isHooked() {
        return vaultEconomy != null;
    }

    /**
     * @return the name of the Vault economy provider plugin, or "Unknown".
     */
    public String getProviderName() {
        return vaultEconomy != null ? vaultEconomy.getName() : "Unknown";
    }

    // ========================
    // Economy operations
    // ========================

    public double getBalance(UUID uuid) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (!vaultEconomy.hasAccount(op)) {
            vaultEconomy.createPlayerAccount(op);
        }
        return vaultEconomy.getBalance(op);
    }

    public void setBalance(UUID uuid, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (!vaultEconomy.hasAccount(op)) {
            vaultEconomy.createPlayerAccount(op);
        }
        double current = vaultEconomy.getBalance(op);
        double diff = amount - current;
        if (diff > 0) {
            vaultEconomy.depositPlayer(op, diff);
        } else if (diff < 0) {
            vaultEconomy.withdrawPlayer(op, -diff);
        }
    }

    public void addBalance(UUID uuid, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (!vaultEconomy.hasAccount(op)) {
            vaultEconomy.createPlayerAccount(op);
        }
        vaultEconomy.depositPlayer(op, amount);
    }

    public boolean removeBalance(UUID uuid, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (!vaultEconomy.hasAccount(op)) {
            vaultEconomy.createPlayerAccount(op);
        }
        if (vaultEconomy.getBalance(op) < amount) {
            return false;
        }
        EconomyResponse resp = vaultEconomy.withdrawPlayer(op, amount);
        return resp.transactionSuccess();
    }

    public boolean pay(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        OfflinePlayer fromP = Bukkit.getOfflinePlayer(from);
        OfflinePlayer toP = Bukkit.getOfflinePlayer(to);
        if (!vaultEconomy.hasAccount(fromP)) vaultEconomy.createPlayerAccount(fromP);
        if (!vaultEconomy.hasAccount(toP)) vaultEconomy.createPlayerAccount(toP);

        if (vaultEconomy.getBalance(fromP) < amount) return false;

        EconomyResponse withdrawResp = vaultEconomy.withdrawPlayer(fromP, amount);
        if (!withdrawResp.transactionSuccess()) return false;

        EconomyResponse depositResp = vaultEconomy.depositPlayer(toP, amount);
        if (!depositResp.transactionSuccess()) {
            // Rollback the withdrawal
            vaultEconomy.depositPlayer(fromP, amount);
            return false;
        }
        return true;
    }

    public String format(double amount) {
        return vaultEconomy.format(amount);
    }

    public boolean hasBalance(UUID uuid, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (!vaultEconomy.hasAccount(op)) {
            vaultEconomy.createPlayerAccount(op);
        }
        return vaultEconomy.has(op, amount);
    }

    /**
     * Vault does not natively provide a "get all balances sorted" method.
     * We scan known player data files (from JustPlugin's data manager) to find UUIDs,
     * then query Vault for each one. This won't find players that ONLY exist in Vault
     * and never joined while JustPlugin was active, but it covers all practical cases.
     */
    public List<Map.Entry<UUID, Double>> getAllBalancesSorted(DataManager dataManager) {
        Map<UUID, Double> all = new HashMap<>();

        // Online players
        for (var player : Bukkit.getOnlinePlayers()) {
            all.put(player.getUniqueId(), getBalance(player.getUniqueId()));
        }

        // Offline players from JustPlugin's data folder
        java.io.File folder = dataManager.getPlayerDataFolder();
        if (folder.exists()) {
            java.io.File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (java.io.File file : files) {
                    String fileName = file.getName().replace(".yml", "");
                    try {
                        UUID uuid = UUID.fromString(fileName);
                        if (!all.containsKey(uuid)) {
                            all.put(uuid, getBalance(uuid));
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

