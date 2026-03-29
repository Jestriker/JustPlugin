package org.justme.justPlugin.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class GodCommand implements TabExecutor {

    private final JustPlugin plugin;

    // Bad/negative potion effects
    private static final Set<PotionEffectType> BAD_EFFECTS = Set.of(
            PotionEffectType.POISON, PotionEffectType.WITHER,
            PotionEffectType.HUNGER, PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE,
            PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS,
            PotionEffectType.LEVITATION, PotionEffectType.BAD_OMEN,
            PotionEffectType.DARKNESS, PotionEffectType.INSTANT_DAMAGE
    );

    public GodCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        Player target = player;
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.god.others")) {
                player.sendMessage(CC.error("You don't have permission to toggle god mode for others."));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
                return true;
            }
        }
        plugin.getPlayerListener().toggleGodMode(target.getUniqueId());
        boolean god = plugin.getPlayerListener().isGodMode(target.getUniqueId());

        if (god) {
            // Full heal + full food
            double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();
            target.setHealth(maxHealth);
            target.setFoodLevel(20);
            target.setSaturation(20f);
            target.setFireTicks(0);

            // Remove all bad effects while in god mode
            for (PotionEffect effect : target.getActivePotionEffects()) {
                if (BAD_EFFECTS.contains(effect.getType())) {
                    target.removePotionEffect(effect.getType());
                }
            }
        } else {
            // Disabling god mode - check if player has any effects, suggest clearing
            if (!target.getActivePotionEffects().isEmpty()) {
                target.sendMessage(CC.info("You have active effects. Use <yellow>/effect clear</yellow> to remove them."));
            }
        }

        if (target.equals(player)) {
            player.sendMessage(CC.success("God mode " + (god ? "<green>enabled" : "<red>disabled") + "."));
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> " + (god ? "enabled" : "disabled") + " god mode");
        } else {
            player.sendMessage(CC.success("God mode " + (god ? "<green>enabled" : "<red>disabled") + " for <yellow>" + target.getName() + "</yellow>."));
            target.sendMessage(CC.info("God mode has been " + (god ? "<green>enabled" : "<red>disabled") + " by <yellow>" + player.getName() + "</yellow>."));
            plugin.getLogManager().log("player", "<yellow>" + player.getName() + "</yellow> " + (god ? "enabled" : "disabled") + " god mode for <yellow>" + target.getName() + "</yellow>");
        }
         plugin.getPlayerStateManager().saveState(target);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.god.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
