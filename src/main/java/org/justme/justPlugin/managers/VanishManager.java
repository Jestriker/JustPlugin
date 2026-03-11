package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final JustPlugin plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public void toggleVanish(Player player) {
        if (vanished.contains(player.getUniqueId())) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public void vanish(Player player) {
        vanished.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        // Hide from all non-permitted players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        // Infinite invisibility with no particles
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                PotionEffect.INFINITE_DURATION, 0, true, false, false
        ));

        // Fake quit message
        Bukkit.broadcast(CC.translate("<yellow>" + player.getName() + " left the game"));

        // Remove from player list for non-vanish-see players
        player.playerListName(Component.empty());

        player.sendMessage(CC.success("You are now vanished."));
    }

    /**
     * Silently vanish a player without broadcasting quit message.
     * Used when restoring vanish state on join.
     */
    public void vanishSilent(Player player) {
        vanished.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                PotionEffect.INFINITE_DURATION, 0, true, false, false
        ));

        player.playerListName(Component.empty());
        player.sendMessage(CC.success("Your vanish state has been restored."));
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        player.removeMetadata("vanished", plugin);

        // Show to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        // Restore tab name
        player.playerListName(null); // null = default name

        // Fake join message
        Bukkit.broadcast(CC.translate("<yellow>" + player.getName() + " joined the game"));

        player.sendMessage(CC.success("You are no longer vanished."));
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanished);
    }

    public int getVanishedCount() {
        return vanished.size();
    }

    public void handleJoin(Player player) {
        // Hide vanished players from non-permitted joining player
        for (UUID vanishedUuid : vanished) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUuid);
            if (vanishedPlayer != null && !player.hasPermission("justplugin.vanish.see")) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    /**
     * Called when a vanished player joins back (if they were vanished before quit, re-vanish them).
     * Currently vanish is not persisted, so this just handles hiding existing vanished players.
     */
    public void handleVanishedPlayerJoin(Player player) {
        if (vanished.contains(player.getUniqueId())) {
            // Re-apply vanish effects
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    PotionEffect.INFINITE_DURATION, 0, true, false, false
            ));
            player.playerListName(Component.empty());
        }
    }
}
