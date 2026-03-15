package org.justme.justPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.justme.justPlugin.JustPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Logs vanilla Minecraft commands to staff chat and Discord webhook.
 * Listens on MONITOR priority so commands have already been processed
 * and we only log them - never cancel or modify them.
 * The list of logged commands is configurable in config.yml under vanilla-command-log.commands.
 */
public class VanillaCommandLogger implements Listener {

    private final JustPlugin plugin;

    /**
     * Fallback set used when config has no list defined.
     */
    private static final Set<String> DEFAULT_LOGGED = Set.of(
            "op", "deop", "stop", "restart", "reload",
            "kick", "ban", "ban-ip", "pardon", "pardon-ip", "banlist",
            "whitelist",
            "give", "item", "gamemode", "xp", "experience",
            "tp", "teleport",
            "summon", "kill",
            "gamerule", "time", "weather",
            "setworldspawn", "setblock", "fill", "fillbiome",
            "clearspawnpoint",
            "setmaxplayers", "seed",
            "tick",
            "say", "locate",
            "advancement",
            "execute",
            "ops"
    );

    public VanillaCommandLogger(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the set of commands to log, reading from config each time
     * (so config changes via web editor or reload take effect immediately).
     */
    private Set<String> getLoggedCommands() {
        if (!plugin.getConfig().getBoolean("vanilla-command-log.enabled", true)) {
            return Set.of(); // disabled
        }
        List<String> configList = plugin.getConfig().getStringList("vanilla-command-log.commands");
        if (configList.isEmpty()) {
            return DEFAULT_LOGGED;
        }
        Set<String> result = new HashSet<>();
        for (String cmd : configList) {
            result.add(cmd.toLowerCase().trim());
        }
        return result;
    }

    /**
     * Logs vanilla commands executed by players.
     * MONITOR priority = fires after all other handlers, command already processed.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String fullCommand = event.getMessage();

        // Strip leading slash
        String cmd = fullCommand.startsWith("/") ? fullCommand.substring(1) : fullCommand;
        String baseCmd = cmd.split("\\s+")[0].toLowerCase();

        // Remove namespace prefix (e.g., "minecraft:give" -> "give")
        if (baseCmd.contains(":")) {
            baseCmd = baseCmd.substring(baseCmd.indexOf(':') + 1);
        }

        if (getLoggedCommands().contains(baseCmd)) {
            String logMessage = "<yellow>" + player.getName() + "</yellow> ran vanilla command: <white>" + fullCommand;
            plugin.getLogManager().log("vanilla", logMessage);
        }
    }

    /**
     * Logs vanilla commands executed by the console.
     * MONITOR priority = fires after all other handlers, command already processed.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsoleCommand(ServerCommandEvent event) {
        String cmd = event.getCommand();
        String baseCmd = cmd.split("\\s+")[0].toLowerCase();

        // Remove namespace prefix
        if (baseCmd.contains(":")) {
            baseCmd = baseCmd.substring(baseCmd.indexOf(':') + 1);
        }

        if (getLoggedCommands().contains(baseCmd)) {
            String logMessage = "<yellow>Console</yellow> ran command: <white>/" + cmd;
            plugin.getLogManager().log("vanilla", logMessage);
        }
    }
}


