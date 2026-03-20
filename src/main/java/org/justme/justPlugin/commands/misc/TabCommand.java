package org.justme.justPlugin.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

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
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }

        applyTabToPlayer(player);
        player.sendMessage(CC.success("Tab list updated!"));
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

        String h = resolveTabPlaceholders(header, player);
        String f = resolveTabPlaceholders(footer, player);
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

    /**
     * Resolve all supported tab placeholders for a player.
     */
    private String resolveTabPlaceholders(String text, Player player) {
        text = text.replace("{player}", player.getName());
        text = text.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        text = text.replace("{ping}", String.valueOf(player.getPing()));

        // TPS - use cached value
        try {
            double[] tps = Bukkit.getTPS();
            double serverTps = tps.length > 0 ? Math.min(tps[0], 20.0) : 20.0;
            text = text.replace("{tps}", String.format("%.1f", serverTps));
        } catch (Exception e) {
            text = text.replace("{tps}", "20.0");
        }

        return text;
    }
}

