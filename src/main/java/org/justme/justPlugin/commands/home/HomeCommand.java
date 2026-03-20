package org.justme.justPlugin.commands.home;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.util.CC;

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
            sender.sendMessage(CC.error("Only players can use this command."));
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
            // Home not found — open GUI instead
            player.sendMessage(CC.error("Home <yellow>" + name + "</yellow> not found! Opening homes menu..."));
            plugin.getHomeGui().open(player);
            return true;
        }

        // Delay check (time between uses) — OPs auto-skip, or explicit delaybypass permission
        if (!player.isOp() && !player.hasPermission("justplugin.home.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "home")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "home");
            player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
            return true;
        }

        boolean teleported = plugin.getTeleportManager().teleportWithSafety(player, loc, "justplugin.home.cooldownbypass", "home", "justplugin.home.unsafetp");
        if (teleported) {
            plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "home");
            player.sendMessage(CC.success("Teleporting to home <yellow>" + name + "</yellow>."));
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

