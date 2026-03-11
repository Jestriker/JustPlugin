package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;

import java.util.UUID;

/**
 * Manages persistent player states: fly, walk/fly speed, god mode, and vanish.
 * States are saved to each player's data YAML file and restored on join.
 */
public class PlayerStateManager {

    private final JustPlugin plugin;

    public PlayerStateManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Saves the current state of a player (fly, speed, god, vanish) to their data file.
     */
    public void saveState(Player player) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);

        // Fly
        data.set("state.fly", player.getAllowFlight());

        // Speeds (store as 0-10 scale for readability)
        data.set("state.flySpeed", Math.round(player.getFlySpeed() * 10f * 100f) / 100f);
        data.set("state.walkSpeed", Math.round(player.getWalkSpeed() * 10f * 100f) / 100f);

        // God mode
        data.set("state.god", plugin.getPlayerListener().isGodMode(uuid));

        // Vanish
        data.set("state.vanish", plugin.getVanishManager().isVanished(uuid));

        plugin.getDataManager().savePlayerData(uuid, data);
    }

    /**
     * Loads and restores a player's saved state (fly, speed, god, vanish).
     */
    public void loadState(Player player) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);

        // Only restore if states section exists (player has saved data)
        if (!data.contains("state")) return;

        // Fly
        if (data.contains("state.fly")) {
            boolean fly = data.getBoolean("state.fly");
            player.setAllowFlight(fly);
            if (fly) {
                player.setFlying(true);
            }
        }

        // Fly speed
        if (data.contains("state.flySpeed")) {
            float flySpeed = (float) data.getDouble("state.flySpeed") / 10f;
            flySpeed = Math.max(0f, Math.min(1f, flySpeed));
            player.setFlySpeed(flySpeed);
        }

        // Walk speed
        if (data.contains("state.walkSpeed")) {
            float walkSpeed = (float) data.getDouble("state.walkSpeed") / 10f;
            walkSpeed = Math.max(0f, Math.min(1f, walkSpeed));
            player.setWalkSpeed(walkSpeed);
        }

        // God mode
        if (data.contains("state.god") && data.getBoolean("state.god")) {
            if (!plugin.getPlayerListener().isGodMode(uuid)) {
                plugin.getPlayerListener().toggleGodMode(uuid);
            }
        }

        // Vanish
        if (data.contains("state.vanish") && data.getBoolean("state.vanish")) {
            if (!plugin.getVanishManager().isVanished(uuid)) {
                plugin.getVanishManager().vanishSilent(player);
            }
        }
    }

    /**
     * Saves the state of all currently online players.
     */
    public void saveAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            saveState(player);
        }
    }
}

