package org.justme.justPlugin.commands.home;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Map;
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

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        if (homes.isEmpty()) {
            boolean c = plugin.getConfig().getBoolean("clickable-commands.home-list", true);
            String setHomeCmd = CC.suggestCmd("<yellow>/sethome <name></yellow>", "/sethome ", c);
            player.sendMessage(CC.error("You have no homes set! Use " + setHomeCmd + " to set one."));
            return true;
        }

        String name = args.length >= 1 ? args[0] : homes.keySet().iterator().next();
        Location loc = plugin.getHomeManager().getHome(player.getUniqueId(), name);
        if (loc == null) {
            player.sendMessage(CC.error("Home <yellow>" + name + "</yellow> not found!"));
            boolean clickable = plugin.getConfig().getBoolean("clickable-commands.home-list", true);
            String homeList = homes.keySet().stream()
                    .map(n -> CC.clickCmd("<yellow>" + n + "</yellow>", "/home " + n, clickable))
                    .collect(java.util.stream.Collectors.joining("<gray>, "));
            player.sendMessage(CC.translate(CC.PREFIX + "<gray>Your homes: " + homeList));
            return true;
        }

        // Cooldown check (applies even to OPs unless explicit bypass)
        if (!player.hasPermission("justplugin.home.nocooldown")
                && plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), "home")) {
            int remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), "home");
            player.sendMessage(CC.error("You must wait <yellow>" + remaining + "</yellow> seconds before using this command again."));
            return true;
        }

        boolean teleported = plugin.getTeleportManager().teleportWithSafety(player, loc, "justplugin.home.cooldownbypass", "home", "justplugin.home.unsafetp");
        if (teleported) {
            plugin.getCooldownManager().setCooldown(player.getUniqueId(), "home");
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

