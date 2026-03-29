package org.justme.justPlugin.listeners.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.justme.justPlugin.JustPlugin;

/**
 * Handles combat-related events: damage (god mode), hunger drain prevention,
 * bad potion effect blocking, and teleport cancellation on damage.
 */
public class CombatListener implements Listener {

    private final JustPlugin plugin;

    public CombatListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getPlayerListener().isGodMode(player.getUniqueId())) {
                event.setCancelled(true);
                player.setFireTicks(0);
            }
            // Cancel pending teleport if player takes damage during warmup
            if (!event.isCancelled()) {
                plugin.getTeleportManager().handleDamageDuringTeleport(player);
            }
        }
    }

    // Prevent hunger drain in god mode
    @EventHandler
    public void onFoodChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getPlayerListener().isGodMode(player.getUniqueId())) {
                event.setCancelled(true);
                player.setFoodLevel(20);
                player.setSaturation(20f);
            }
        }
    }

    // Block bad potion effects while in god mode
    @EventHandler
    public void onPotionEffect(org.bukkit.event.entity.EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getPlayerListener().isGodMode(player.getUniqueId()) && event.getNewEffect() != null) {
                var type = event.getNewEffect().getType();
                if (isBadEffect(type)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isBadEffect(org.bukkit.potion.PotionEffectType type) {
        return type == org.bukkit.potion.PotionEffectType.POISON
                || type == org.bukkit.potion.PotionEffectType.WITHER
                || type == org.bukkit.potion.PotionEffectType.HUNGER
                || type == org.bukkit.potion.PotionEffectType.WEAKNESS
                || type == org.bukkit.potion.PotionEffectType.SLOWNESS
                || type == org.bukkit.potion.PotionEffectType.MINING_FATIGUE
                || type == org.bukkit.potion.PotionEffectType.NAUSEA
                || type == org.bukkit.potion.PotionEffectType.BLINDNESS
                || type == org.bukkit.potion.PotionEffectType.LEVITATION
                || type == org.bukkit.potion.PotionEffectType.BAD_OMEN
                || type == org.bukkit.potion.PotionEffectType.DARKNESS
                || type == org.bukkit.potion.PotionEffectType.INSTANT_DAMAGE;
    }
}

