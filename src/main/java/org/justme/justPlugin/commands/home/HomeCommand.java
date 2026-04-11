package org.justme.justPlugin.commands.home;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class HomeCommand implements TabExecutor {

    private final JustPlugin plugin;

    public HomeCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        // If no args, open the Home GUI
        if (args.length == 0) {
            plugin.getHomeGui().open(player);
            return true;
        }

        // If arg provided, try to teleport directly to that home
        String name = args[0];
        Location loc = plugin.getHomeManager().getHome(player.getUniqueId(), name);
        if (loc == null) {
            // Home not found - open GUI instead
            player.sendMessage(plugin.getMessageManager().error("home.home.not-found", "{home}", name));
            plugin.getHomeGui().open(player);
            return true;
        }

        // Delay check (time between uses) - requires explicit delaybypass permission
        if (!player.hasPermission("justplugin.home.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "home")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "home");
            player.sendMessage(plugin.getMessageManager().error("general.cooldown-wait", "{time}", CooldownManager.formatTime(remaining)));
            return true;
        }

        boolean teleported = plugin.getTeleportManager().teleportWithSafety(player, loc, "justplugin.home.cooldownbypass", "home", "justplugin.home.unsafetp");
        if (teleported) {
            plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "home");
            player.sendMessage(plugin.getMessageManager().success("home.home.teleporting", "{home}", name));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            Set<String> homes = plugin.getHomeManager().getHomeNames(player.getUniqueId());
            return homes.stream()
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

