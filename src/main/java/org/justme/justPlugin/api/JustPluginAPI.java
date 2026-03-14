package org.justme.justPlugin.api;

/**
 * Main entry point for the JustPlugin API.
 * External plugins can access economy, punishment, and vanish systems.
 * 
 * Usage:
 * <pre>
 *   JustPluginAPI api = JustPluginProvider.get();
 *   if (api != null) {
 *       double balance = api.getEconomyAPI().getBalance(playerUuid);
 *   }
 * </pre>
 */
public interface JustPluginAPI {
    EconomyAPI getEconomyAPI();
    PunishmentAPI getPunishmentAPI();
    VanishAPI getVanishAPI();
}

