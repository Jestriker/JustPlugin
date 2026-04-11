package org.justme.justPlugin.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.PAPIHook;
import org.justme.justPlugin.util.PlaceholderResolver;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class TabCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TabCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        applyTabToPlayer(player);
        player.sendMessage(plugin.getMessageManager().success("misc.tab.updated"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    /**
     * Apply tab header/footer to a single player, resolving all placeholders.
     */
    public void applyTabToPlayer(Player player) {
        String header = plugin.getConfig().getString("tab.header",
                "<gradient:#00aaff:#00ffaa><bold>JustPlugin Server</bold></gradient>");
        String footer = plugin.getConfig().getString("tab.footer",
                "<gray>Players: <yellow>{online}<gray>/<yellow>{max} <gray>| <gray>Ping: <yellow>{ping}ms <gray>| <gray>TPS: <yellow>{tps}");

        // Resolve animations if available
        if (plugin.getScoreboardManager() != null) {
            header = plugin.getScoreboardManager().resolveAnimations(header);
            footer = plugin.getScoreboardManager().resolveAnimations(footer);
        }

        // Resolve all 50+ placeholders via PlaceholderResolver + PAPI
        String h = PlaceholderResolver.resolve(player, plugin, header);
        h = PAPIHook.setPlaceholders(player, h);
        String f = PlaceholderResolver.resolve(player, plugin, footer);
        f = PAPIHook.setPlaceholders(player, f);

        // Append maintenance footer line if enabled and maintenance is active
        if (plugin.getConfig().getBoolean("tab.maintenance-footer-enabled", false)
                && plugin.getMaintenanceManager() != null
                && plugin.getMaintenanceManager().isActive()) {
            String maintenanceLine = plugin.getConfig().getString("tab.maintenance-footer-line",
                    "\n<red><bold>⚠</bold> Server is in maintenance mode</red>");
            f = f + maintenanceLine;
        }

        player.sendPlayerListHeaderAndFooter(CC.translate(h), CC.translate(f));
    }

    /**
     * Apply tab header/footer to all online players. Called on join and periodically.
     */
    public void applyTabToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyTabToPlayer(player);
        }
    }

    /**
     * Get the configured tab refresh interval in seconds (default 5).
     * Returns 0 if periodic updates are disabled.
     */
    public int getRefreshInterval() {
        return plugin.getConfig().getInt("tab.refresh-interval", 5);
    }
}

