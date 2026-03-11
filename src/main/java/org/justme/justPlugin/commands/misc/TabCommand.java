package org.justme.justPlugin.commands.misc;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

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

        // Set tab header/footer from config
        String header = plugin.getConfig().getString("tab.header", "<gradient:#00aaff:#00ffaa><bold>JustPlugin Server</bold></gradient>");
        String footer = plugin.getConfig().getString("tab.footer", "<gray>Players Online: <yellow>{online}<gray>/<yellow>{max}");

        footer = footer.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        footer = footer.replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        header = header.replace("{player}", player.getName());
        footer = footer.replace("{player}", player.getName());

        player.sendPlayerListHeaderAndFooter(CC.translate(header), CC.translate(footer));
        player.sendMessage(CC.success("Tab list updated!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    /**
     * Apply tab header/footer to all online players. Called on join and periodically.
     */
    public void applyTabToAll() {
        String header = plugin.getConfig().getString("tab.header", "<gradient:#00aaff:#00ffaa><bold>JustPlugin Server</bold></gradient>");
        String footer = plugin.getConfig().getString("tab.footer", "<gray>Players Online: <yellow>{online}<gray>/<yellow>{max}");

        for (Player player : Bukkit.getOnlinePlayers()) {
            String h = header.replace("{player}", player.getName());
            String f = footer.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()))
                    .replace("{player}", player.getName());
            player.sendPlayerListHeaderAndFooter(CC.translate(h), CC.translate(f));
        }
    }
}

