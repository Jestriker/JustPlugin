package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.MaintenanceManager;
import org.justme.justPlugin.util.CC;

import java.util.*;

/**
 * /maintenance command - manages the server maintenance mode.
 * <p>
 * Subcommands:
 * <ul>
 *   <li><b>mode on</b> - activate maintenance mode (blocks non-whitelisted joins)</li>
 *   <li><b>mode off</b> - deactivate maintenance mode (open server)</li>
 *   <li><b>allowed-users list</b> - list all whitelisted maintenance users</li>
 *   <li><b>allowed-users add &lt;player&gt;</b> - add a player to the maintenance whitelist</li>
 *   <li><b>allowed-users remove &lt;player&gt;</b> - remove a player from the maintenance whitelist</li>
 *   <li><b>allowed-groups list</b> - list all bypass LuckPerms groups</li>
 *   <li><b>allowed-groups add &lt;group&gt;</b> - add a LuckPerms group to maintenance bypass</li>
 *   <li><b>allowed-groups remove &lt;group&gt;</b> - remove a LuckPerms group from maintenance bypass</li>
 *   <li><b>cooldown &lt;duration | clear&gt;</b> - set estimated maintenance end time (shown in MOTD/kick)</li>
 * </ul>
 */
public class MaintenanceCommand implements TabExecutor {

    private final JustPlugin plugin;

    public MaintenanceCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MaintenanceManager mm = plugin.getMaintenanceManager();

        if (args.length == 0) {
            sendStatus(sender, mm);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "mode" -> {
                if (args.length < 2) {
                    sender.sendMessage(CC.prefixed("Usage: <yellow>/maintenance mode <on | off>"));
                    return true;
                }
                String mode = args[1].toLowerCase();
                if ("on".equals(mode)) {
                    if (mm.isActive()) {
                        sender.sendMessage(CC.warning("Maintenance mode is already <red>active</red><yellow>."));
                        return true;
                    }
                    mm.setActive(true);
                    sender.sendMessage(CC.success("Maintenance mode is now <red>active</red><green>. Non-whitelisted players have been kicked."));
                    plugin.getLogManager().log("admin",
                            "<yellow>" + sender.getName() + "</yellow> <gray>enabled <red>maintenance mode</red><gray>.");
                    if (plugin.getTabCommand() != null) plugin.getTabCommand().applyTabToAll();
                } else if ("off".equals(mode)) {
                    if (!mm.isActive()) {
                        sender.sendMessage(CC.warning("Maintenance mode is already <green>inactive</green><yellow>."));
                        return true;
                    }
                    mm.setActive(false);
                    sender.sendMessage(CC.success("Maintenance mode is now <green>inactive</green><green>. Server is open to all players."));
                    plugin.getLogManager().log("admin",
                            "<yellow>" + sender.getName() + "</yellow> <gray>disabled <green>maintenance mode</green><gray>.");
                    if (plugin.getTabCommand() != null) plugin.getTabCommand().applyTabToAll();
                } else {
                    sender.sendMessage(CC.error("Invalid mode. Use <yellow>on</yellow> or <yellow>off</yellow>."));
                }
            }

            case "allowed-users", "allowedusers", "whitelist", "users" -> {
                if (args.length < 2) {
                    sender.sendMessage(CC.prefixed("Usage: <yellow>/maintenance allowed-users <list | add | remove> [player]"));
                    return true;
                }
                String action = args[1].toLowerCase();
                switch (action) {
                    case "list" -> {
                        Map<UUID, String> allowed = mm.getAllowedUsers();
                        if (allowed.isEmpty()) {
                            sender.sendMessage(CC.info("No players on the maintenance whitelist."));
                        } else {
                            sender.sendMessage(CC.prefixed("<gray>Maintenance whitelist <dark_gray>(" + allowed.size() + " players):"));
                            for (var entry : allowed.entrySet()) {
                                sender.sendMessage(CC.line("<white>" + entry.getValue() + " <dark_gray>(" + entry.getKey() + ")"));
                            }
                        }
                    }
                    case "add" -> {
                        if (args.length < 3) {
                            sender.sendMessage(CC.error("Usage: <yellow>/maintenance allowed-users add <player>"));
                            return true;
                        }
                        String targetName = args[2];
                        // Try online first, then offline
                        OfflinePlayer target = Bukkit.getPlayer(targetName);
                        if (target == null) {
                            //noinspection deprecation
                            target = Bukkit.getOfflinePlayer(targetName);
                        }
                        if (mm.addAllowed(target.getUniqueId(), target.getName() != null ? target.getName() : targetName)) {
                            sender.sendMessage(CC.success("Added <yellow>" + (target.getName() != null ? target.getName() : targetName) +
                                    "</yellow> to the maintenance whitelist."));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>added <white>" + targetName + "</white> to maintenance whitelist.");
                        } else {
                            sender.sendMessage(CC.warning("That player is already on the maintenance whitelist."));
                        }
                    }
                    case "remove" -> {
                        if (args.length < 3) {
                            sender.sendMessage(CC.error("Usage: <yellow>/maintenance allowed-users remove <player>"));
                            return true;
                        }
                        String targetName = args[2];
                        // Find UUID by name in whitelist
                        UUID found = null;
                        for (var entry : mm.getAllowedUsers().entrySet()) {
                            if (entry.getValue().equalsIgnoreCase(targetName)) {
                                found = entry.getKey();
                                break;
                            }
                        }
                        if (found == null) {
                            // Try offline lookup
                            //noinspection deprecation
                            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
                            if (mm.getAllowedUsers().containsKey(op.getUniqueId())) {
                                found = op.getUniqueId();
                            }
                        }
                        if (found != null && mm.removeAllowed(found)) {
                            sender.sendMessage(CC.success("Removed <yellow>" + targetName + "</yellow> from the maintenance whitelist."));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>removed <white>" + targetName + "</white> from maintenance whitelist.");
                        } else {
                            sender.sendMessage(CC.error("That player is not on the maintenance whitelist."));
                        }
                    }
                    default -> sender.sendMessage(CC.error("Unknown action. Use <yellow>list</yellow>, <yellow>add</yellow>, or <yellow>remove</yellow>."));
                }
            }

            case "allowed-groups", "allowedgroups", "groups" -> {
                if (args.length < 2) {
                    sender.sendMessage(CC.prefixed("Usage: <yellow>/maintenance allowed-groups <list | add | remove> [group]"));
                    return true;
                }
                String action = args[1].toLowerCase();
                switch (action) {
                    case "list" -> {
                        List<String> groups = mm.getAllowedGroups();
                        if (groups.isEmpty()) {
                            sender.sendMessage(CC.info("No LuckPerms groups configured for maintenance bypass."));
                        } else {
                            sender.sendMessage(CC.prefixed("<gray>Maintenance bypass groups <dark_gray>(" + groups.size() + "):"));
                            for (String group : groups) {
                                sender.sendMessage(CC.line("<white>" + group));
                            }
                        }
                        if (!plugin.isLuckPermsAvailable()) {
                            sender.sendMessage(CC.warning("LuckPerms is not installed - group bypass is inactive."));
                        }
                    }
                    case "add" -> {
                        if (args.length < 3) {
                            sender.sendMessage(CC.error("Usage: <yellow>/maintenance allowed-groups add <group>"));
                            return true;
                        }
                        String groupName = args[2].toLowerCase();
                        // Validate group exists in LuckPerms if available
                        if (plugin.isLuckPermsAvailable()) {
                            try {
                                net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
                                net.luckperms.api.model.group.Group group = lp.getGroupManager().getGroup(groupName);
                                if (group == null) {
                                    sender.sendMessage(CC.warning("LuckPerms group <yellow>" + groupName +
                                            "</yellow> was not found. Adding anyway - it will work if the group is created later."));
                                }
                            } catch (Exception ignored) {}
                        }
                        if (mm.addAllowedGroup(groupName)) {
                            sender.sendMessage(CC.success("Added group <yellow>" + groupName +
                                    "</yellow> to the maintenance bypass list."));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>added group <white>" + groupName + "</white> to maintenance bypass.");
                        } else {
                            sender.sendMessage(CC.warning("Group <yellow>" + groupName + "</yellow> is already in the bypass list."));
                        }
                    }
                    case "remove" -> {
                        if (args.length < 3) {
                            sender.sendMessage(CC.error("Usage: <yellow>/maintenance allowed-groups remove <group>"));
                            return true;
                        }
                        String groupName = args[2].toLowerCase();
                        if (mm.removeAllowedGroup(groupName)) {
                            sender.sendMessage(CC.success("Removed group <yellow>" + groupName +
                                    "</yellow> from the maintenance bypass list."));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>removed group <white>" + groupName + "</white> from maintenance bypass.");
                        } else {
                            sender.sendMessage(CC.error("Group <yellow>" + groupName + "</yellow> is not in the bypass list."));
                        }
                    }
                    default -> sender.sendMessage(CC.error("Unknown action. Use <yellow>list</yellow>, <yellow>add</yellow>, or <yellow>remove</yellow>."));
                }
            }

            case "cooldown", "timer", "eta" -> {
                if (args.length < 2) {
                    String cd = mm.getCooldownText();
                    if (cd != null) {
                        sender.sendMessage(CC.info("Maintenance estimated to end in: <yellow>" + cd));
                    } else {
                        sender.sendMessage(CC.info("No maintenance cooldown is set."));
                    }
                    sender.sendMessage(CC.line("Usage: <yellow>/maintenance cooldown <duration | clear>"));
                    sender.sendMessage(CC.line("Duration examples: <white>30m<gray>, <white>1h<gray>, <white>2d<gray>, <white>1d12h"));
                    return true;
                }
                String val = args[1].toLowerCase();
                if ("clear".equals(val) || "none".equals(val) || "off".equals(val)) {
                    mm.setCooldownEnd(-1);
                    sender.sendMessage(CC.success("Maintenance cooldown cleared. MOTD and kick screen will say \"try again later\"."));
                } else {
                    long duration = MaintenanceManager.parseDuration(val);
                    if (duration <= 0) {
                        sender.sendMessage(CC.error("Invalid duration. Examples: <yellow>30m<gray>, <yellow>1h<gray>, <yellow>2d<gray>, <yellow>1d12h"));
                        return true;
                    }
                    mm.setCooldownEnd(System.currentTimeMillis() + duration);
                    sender.sendMessage(CC.success("Maintenance cooldown set to <yellow>" + mm.getCooldownText() +
                            "</yellow>. This will be shown in the MOTD and kick screen."));
                }
            }

            default -> {
                sender.sendMessage(CC.prefixed("<gray>Unknown subcommand. Usage:"));
                sendUsage(sender);
            }
        }

        return true;
    }

    private void sendStatus(CommandSender sender, MaintenanceManager mm) {
        sender.sendMessage(CC.prefixed("<gray>Maintenance System:"));
        sender.sendMessage(CC.line("Status: " + (mm.isActive() ? "<red>Active (maintenance on)" : "<green>Inactive (server open)")));
        String cd = mm.getCooldownText();
        sender.sendMessage(CC.line("Cooldown: " + (cd != null ? "<yellow>" + cd : "<dark_gray>Not set")));
        sender.sendMessage(CC.line("Whitelisted: <white>" + mm.getAllowedUsers().size() + " players<gray>, <white>" + mm.getAllowedGroups().size() + " groups<gray>."));
        List<String> groups = mm.getAllowedGroups();
        sender.sendMessage(CC.line("Bypass groups: " + (groups.isEmpty() ? "<dark_gray>None" : "<white>" + String.join(", ", groups))));
        sender.sendMessage(CC.line("OP bypass: " + (mm.isOpsBypass() ? "<green>Enabled" : "<red>Disabled")));
        sender.sendMessage(CC.line("Custom icon: " + (mm.getCachedIcon() != null ? "<green>Loaded" : "<dark_gray>Not set")));
        sender.sendMessage(net.kyori.adventure.text.Component.empty());
        sendUsage(sender);
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(CC.line("<yellow>/maintenance mode <on | off>"));
        sender.sendMessage(CC.line("<yellow>/maintenance allowed-users <list | add | remove> [player]"));
        sender.sendMessage(CC.line("<yellow>/maintenance allowed-groups <list | add | remove> [group]"));
        sender.sendMessage(CC.line("<yellow>/maintenance cooldown <duration | clear>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(args[0], List.of("mode", "allowed-users", "allowed-groups", "cooldown"));
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "mode" -> filterStartsWith(args[1], List.of("on", "off"));
                case "allowed-users", "allowedusers", "whitelist", "users" ->
                        filterStartsWith(args[1], List.of("list", "add", "remove"));
                case "allowed-groups", "allowedgroups", "groups" ->
                        filterStartsWith(args[1], List.of("list", "add", "remove"));
                case "cooldown", "timer", "eta" ->
                        filterStartsWith(args[1], List.of("30m", "1h", "2h", "6h", "12h", "1d", "clear"));
                default -> List.of();
            };
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            if ("allowed-users".equals(sub) || "allowedusers".equals(sub) || "whitelist".equals(sub) || "users".equals(sub)) {
                if ("add".equals(action)) {
                    // Suggest online players not already whitelisted
                    List<String> names = new ArrayList<>();
                    MaintenanceManager mm = plugin.getMaintenanceManager();
                    for (var p : Bukkit.getOnlinePlayers()) {
                        if (!mm.isAllowed(p.getUniqueId()) && p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            names.add(p.getName());
                        }
                    }
                    return names;
                }
                if ("remove".equals(action)) {
                    // Suggest whitelisted names
                    List<String> names = new ArrayList<>();
                    for (String n : plugin.getMaintenanceManager().getAllowedUsers().values()) {
                        if (n.toLowerCase().startsWith(args[2].toLowerCase())) {
                            names.add(n);
                        }
                    }
                    return names;
                }
            }
            if ("allowed-groups".equals(sub) || "allowedgroups".equals(sub) || "groups".equals(sub)) {
                if ("add".equals(action)) {
                    // Suggest LuckPerms groups not already in bypass list
                    List<String> suggestions = new ArrayList<>();
                    if (plugin.isLuckPermsAvailable()) {
                        try {
                            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
                            MaintenanceManager mm = plugin.getMaintenanceManager();
                            List<String> existing = mm.getAllowedGroups();
                            for (net.luckperms.api.model.group.Group group : lp.getGroupManager().getLoadedGroups()) {
                                String name = group.getName().toLowerCase();
                                if (!existing.contains(name) && name.startsWith(args[2].toLowerCase())) {
                                    suggestions.add(name);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    return suggestions;
                }
                if ("remove".equals(action)) {
                    // Suggest from configured bypass groups
                    List<String> suggestions = new ArrayList<>();
                    for (String g : plugin.getMaintenanceManager().getAllowedGroups()) {
                        if (g.toLowerCase().startsWith(args[2].toLowerCase())) {
                            suggestions.add(g);
                        }
                    }
                    return suggestions;
                }
            }
        }
        return List.of();
    }

    private static List<String> filterStartsWith(String input, List<String> options) {
        String lower = input.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();
    }
}

