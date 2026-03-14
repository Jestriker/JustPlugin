package org.justme.justPlugin.api;

import org.justme.justPlugin.JustPlugin;

import java.util.UUID;

/**
 * Implementation of the JustPlugin API that delegates to internal managers.
 */
public class JustPluginAPIImpl implements JustPluginAPI {

    private final JustPlugin plugin;
    private final EconomyAPI economyAPI;
    private final PunishmentAPI punishmentAPI;
    private final VanishAPI vanishAPI;

    public JustPluginAPIImpl(JustPlugin plugin) {
        this.plugin = plugin;
        this.economyAPI = new EconomyAPIImpl(plugin);
        this.punishmentAPI = new PunishmentAPIImpl(plugin);
        this.vanishAPI = new VanishAPIImpl(plugin);
    }

    @Override public EconomyAPI getEconomyAPI() { return economyAPI; }
    @Override public PunishmentAPI getPunishmentAPI() { return punishmentAPI; }
    @Override public VanishAPI getVanishAPI() { return vanishAPI; }

    // --- Economy Implementation ---
    private static class EconomyAPIImpl implements EconomyAPI {
        private final JustPlugin plugin;
        EconomyAPIImpl(JustPlugin plugin) { this.plugin = plugin; }

        @Override public double getBalance(UUID uuid) { return plugin.getEconomyManager().getBalance(uuid); }
        @Override public void setBalance(UUID uuid, double amount) { plugin.getEconomyManager().setBalance(uuid, amount); }
        @Override public void addBalance(UUID uuid, double amount) { plugin.getEconomyManager().addBalance(uuid, amount); }
        @Override public boolean removeBalance(UUID uuid, double amount) { return plugin.getEconomyManager().removeBalance(uuid, amount); }
        @Override public boolean pay(UUID from, UUID to, double amount) { return plugin.getEconomyManager().pay(from, to, amount); }
        @Override public String format(double amount) { return plugin.getEconomyManager().format(amount); }
        @Override public boolean hasBalance(UUID uuid, double amount) { return plugin.getEconomyManager().getBalance(uuid) >= amount; }
    }

    // --- Punishment Implementation ---
    private static class PunishmentAPIImpl implements PunishmentAPI {
        private final JustPlugin plugin;
        PunishmentAPIImpl(JustPlugin plugin) { this.plugin = plugin; }

        @Override public boolean isBanned(UUID uuid) { return plugin.getBanManager().isBanned(uuid); }
        @Override public void ban(UUID uuid, String name, String reason, String by) { plugin.getBanManager().ban(uuid, name, reason, by); }
        @Override public void tempBan(UUID uuid, String name, String reason, String by, long ms) { plugin.getBanManager().tempBan(uuid, name, reason, by, ms); }
        @Override public boolean unban(UUID uuid) { return plugin.getBanManager().unban(uuid); }

        @Override public boolean isMuted(UUID uuid) { return plugin.getMuteManager().isMuted(uuid); }
        @Override public void mute(UUID uuid, String name, String reason, String by) { plugin.getMuteManager().mute(uuid, name, reason, by); }
        @Override public void tempMute(UUID uuid, String name, String reason, String by, long ms) { plugin.getMuteManager().tempMute(uuid, name, reason, by, ms); }
        @Override public boolean unmute(UUID uuid) { return plugin.getMuteManager().unmute(uuid); }
        @Override public String getMuteReason(UUID uuid) { return plugin.getMuteManager().getMuteReason(uuid); }

        @Override public int getActiveWarnCount(UUID uuid) { return plugin.getWarnManager().getActiveWarnCount(uuid); }
        @Override public int getTotalWarnCount(UUID uuid) { return plugin.getWarnManager().getTotalWarnCount(uuid); }
        @Override public void addWarn(UUID uuid, String name, String reason, String by) { plugin.getWarnManager().addWarn(uuid, name, reason, by); }
        @Override public boolean liftWarn(UUID uuid, int idx, String by, String reason) { return plugin.getWarnManager().liftWarn(uuid, idx, by, reason); }
    }

    // --- Vanish Implementation ---
    private static class VanishAPIImpl implements VanishAPI {
        private final JustPlugin plugin;
        VanishAPIImpl(JustPlugin plugin) { this.plugin = plugin; }

        @Override public boolean isVanished(UUID uuid) { return plugin.getVanishManager().isVanished(uuid); }
        @Override public boolean isSuperVanished(UUID uuid) { return plugin.getVanishManager().isSuperVanished(uuid); }
    }
}

