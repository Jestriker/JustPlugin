package org.justme.justPlugin.commands.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /friendlyfire <enable | disable> - Toggle PvP on/off server-wide.
 * Aliases: /ff
 */
public class FriendlyFireCommand implements TabExecutor {

    private final JustPlugin plugin;

    public FriendlyFireCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /friendlyfire <enable | disable>"));
            return true;
        }

        String action = args[0].toLowerCase();
        String executedBy = sender instanceof Player ? sender.getName() : "Console";

        boolean enable;
        if (action.equals("enable") || action.equals("on") || action.equals("true")) {
            enable = true;
        } else if (action.equals("disable") || action.equals("off") || action.equals("false")) {
            enable = false;
        } else {
            sender.sendMessage(CC.error("Usage: /friendlyfire <enable | disable>"));
            return true;
        }

        // Check current state
        boolean currentState = Bukkit.getWorlds().get(0).getPVP();
        if (enable == currentState) {
            sender.sendMessage(CC.error("Friendly fire is already " + (enable ? "<green>enabled" : "<red>disabled") + "<red>!"));
            return true;
        }

        String oldStatus = currentState ? "enabled" : "disabled";
        String newStatus = enable ? "enabled" : "disabled";

        // Set PvP for all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setPVP(enable);
        }

        // Save state to config
        plugin.getConfig().set("friendly-fire.current-state", enable);
        plugin.saveConfig();

        sender.sendMessage(CC.success("Friendly fire has been " + (enable ? "<green>enabled" : "<red>disabled") + "</green>."));

        // Announce to players if configured
        boolean announce = plugin.getConfig().getBoolean("friendly-fire.announce", false);
        if (announce) {
            String msg = plugin.getConfig().getString("friendly-fire.announce-message",
                    "<gray>[<gradient:#00aaff:#00ffaa>JustPlugin</gradient>] <yellow>Friendly fire has been %status%.");
            msg = msg.replace("%status%", enable ? "<green>enabled</green>" : "<red>disabled</red>");
            Bukkit.broadcast(CC.translate(msg));
        }

        // Log with old and new status
        plugin.getLogManager().log("admin", "<yellow>" + executedBy + "</yellow> " + (enable ? "enabled" : "disabled") + " friendly fire. (was: " + oldStatus + " → now: " + newStatus + ")");

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("enable", "disable").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

