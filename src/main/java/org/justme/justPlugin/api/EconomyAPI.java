package org.justme.justPlugin.api;

import java.util.UUID;

/**
 * Economy API for external plugins.
 * Allows reading and modifying player balances.
 */
public interface EconomyAPI {
    double getBalance(UUID uuid);
    void setBalance(UUID uuid, double amount);
    void addBalance(UUID uuid, double amount);
    boolean removeBalance(UUID uuid, double amount);
    boolean pay(UUID from, UUID to, double amount);
    String format(double amount);
    boolean hasBalance(UUID uuid, double amount);
}

