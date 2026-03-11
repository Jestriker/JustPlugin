package org.justme.justPlugin.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.justme.justPlugin.JustPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final JustPlugin plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggleVanish(Player player) {
        if (vanished.contains(player.getUniqueId())) {
            unvanish(player);
            return false;
        } else {
            vanish(player);
            return true;
        }
    }

    public void vanish(Player player) {
        vanished.add(player.getUniqueId());
        for (Player online : player.getServer().getOnlinePlayers()) {
            if (!online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        for (Player online : player.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }

    public boolean isVanished(UUID playerId) {
        return vanished.contains(playerId);
    }

    public Set<UUID> getVanished() {
        return java.util.Collections.unmodifiableSet(vanished);
    }
}
