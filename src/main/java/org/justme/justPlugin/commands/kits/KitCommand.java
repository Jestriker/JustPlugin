package org.justme.justPlugin.commands.kits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.managers.KitManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /kit - opens kit selection GUI
 * /kit <name> - claim a specific kit directly
 */
@SuppressWarnings("NullableProblems")
public class KitCommand implements TabExecutor {

    private final JustPlugin plugin;

    public KitCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        KitManager km = plugin.getKitManager();

        if (args.length == 0) {
            // Open kit selection GUI
            List<KitManager.KitData> available = km.getAvailableKits(player);
            if (km.getAllKits().isEmpty()) {
                player.sendMessage(plugin.getMessageManager().info("kits.no-kits-exist"));
                return true;
            }
            if (available.isEmpty()) {
                player.sendMessage(plugin.getMessageManager().error("kits.no-kits-available"));
                return true;
            }
            plugin.getKitSelectionGui().open(player);
            return true;
        }

        // /kit <name> - claim directly
        String kitName = args[0].toLowerCase();
        KitManager.KitData kit = km.getKit(kitName);

        if (kit == null || !"published".equals(kit.status)) {
            player.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", args[0]));
            return true;
        }

        if (!kit.enabled) {
            player.sendMessage(plugin.getMessageManager().error("kits.kit-disabled", "{kit}", kit.displayName));
            return true;
        }

        if (!player.hasPermission(kit.permission)) {
            player.sendMessage(plugin.getMessageManager().error("kits.no-permission", "{kit}", kit.displayName));
            return true;
        }

        // Check cooldown (bypass with justplugin.kit.cooldownbypass)
        if (!player.hasPermission("justplugin.kit.cooldownbypass") && !km.canClaim(player.getUniqueId(), kitName)) {
            int remaining = km.getRemainingCooldown(player.getUniqueId(), kitName);
            player.sendMessage(plugin.getMessageManager().error("kits.on-cooldown",
                    "{kit}", kit.displayName,
                    "{time}", CooldownManager.formatTime(remaining)));
            return true;
        }

        km.claimKit(player, kitName);
        player.sendMessage(plugin.getMessageManager().success("kits.kit-claimed", "{kit}", kit.displayName));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return plugin.getKitManager().getAvailableKits(player).stream()
                    .map(k -> k.name)
                    .filter(n -> n.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
