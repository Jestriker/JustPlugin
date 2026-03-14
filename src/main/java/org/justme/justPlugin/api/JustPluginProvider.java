package org.justme.justPlugin.api;

/**
 * Static accessor for the JustPlugin API.
 * External plugins use this to access JustPlugin's systems.
 * 
 * Usage:
 * <pre>
 *   JustPluginAPI api = JustPluginProvider.get();
 *   if (api != null) {
 *       // Plugin is loaded and API is available
 *       double bal = api.getEconomyAPI().getBalance(uuid);
 *   }
 * </pre>
 */
public final class JustPluginProvider {

    private static JustPluginAPI instance;

    private JustPluginProvider() {}

    /**
     * Get the JustPlugin API instance.
     * @return the API instance, or null if JustPlugin is not loaded.
     */
    public static JustPluginAPI get() {
        return instance;
    }

    /**
     * Set the API instance. Called internally by JustPlugin on enable.
     */
    public static void set(JustPluginAPI api) {
        instance = api;
    }

    /**
     * Clear the API instance. Called internally by JustPlugin on disable.
     */
    public static void clear() {
        instance = null;
    }
}

