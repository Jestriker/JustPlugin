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
                    sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.mode.usage"));
                    return true;
                }
                String mode = args[1].toLowerCase();
                if ("on".equals(mode)) {
                    if (mm.isActive()) {
                        sender.sendMessage(plugin.getMessageManager().warning("maintenance.mode.already-on"));
                        return true;
                    }
                    mm.setActive(true);
                    sender.sendMessage(plugin.getMessageManager().success("maintenance.mode.enabled"));
                    plugin.getLogManager().log("admin",
                            "<yellow>" + sender.getName() + "</yellow> <gray>enabled <red>maintenance mode</red><gray>.");
                    if (plugin.getTabCommand() != null) plugin.getTabCommand().applyTabToAll();
                } else if ("off".equals(mode)) {
                    if (!mm.isActive()) {
                        sender.sendMessage(plugin.getMessageManager().warning("maintenance.mode.already-off"));
                        return true;
                    }
                    mm.setActive(false);
                    sender.sendMessage(plugin.getMessageManager().success("maintenance.mode.disabled"));
                    plugin.getLogManager().log("admin",
                            "<yellow>" + sender.getName() + "</yellow> <gray>disabled <green>maintenance mode</green><gray>.");
                    if (plugin.getTabCommand() != null) plugin.getTabCommand().applyTabToAll();
                } else {
                    sender.sendMessage(plugin.getMessageManager().error("maintenance.mode.invalid"));
                }
            }

            case "allowed-users", "allowedusers", "whitelist", "users" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.allowed-users.usage"));
                    return true;
                }
                String action = args[1].toLowerCase();
                switch (action) {
                    case "list" -> {
                        Map<UUID, String> allowed = mm.getAllowedUsers();
                        if (allowed.isEmpty()) {
                            sender.sendMessage(plugin.getMessageManager().info("maintenance.allowed-users.list-empty"));
                        } else {
                            sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.allowed-users.list-header", "{count}", String.valueOf(allowed.size())));
                            for (var entry : allowed.entrySet()) {
                                sender.sendMessage(plugin.getMessageManager().line("maintenance.allowed-users.list-entry", "{player}", entry.getValue(), "{uuid}", entry.getKey().toString()));
                            }
                        }
                    }
                    case "add" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-users.usage-add"));
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
                            sender.sendMessage(plugin.getMessageManager().success("maintenance.allowed-users.added", "{player}", target.getName() != null ? target.getName() : targetName));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>added <white>" + targetName + "</white> to maintenance whitelist.");
                        } else {
                            sender.sendMessage(plugin.getMessageManager().warning("maintenance.allowed-users.already-added"));
                        }
                    }
                    case "remove" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-users.usage-remove"));
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
                            sender.sendMessage(plugin.getMessageManager().success("maintenance.allowed-users.removed", "{player}", targetName));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>removed <white>" + targetName + "</white> from maintenance whitelist.");
                        } else {
                            sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-users.not-on-list"));
                        }
                    }
                    default -> sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-users.unknown-action"));
                }
            }

            case "allowed-groups", "allowedgroups", "groups" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.allowed-groups.usage"));
                    return true;
                }
                String action = args[1].toLowerCase();
                switch (action) {
                    case "list" -> {
                        List<String> groups = mm.getAllowedGroups();
                        if (groups.isEmpty()) {
                            sender.sendMessage(plugin.getMessageManager().info("maintenance.allowed-groups.list-empty"));
                        } else {
                            sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.allowed-groups.list-header", "{count}", String.valueOf(groups.size())));
                            for (String group : groups) {
                                sender.sendMessage(plugin.getMessageManager().line("maintenance.allowed-groups.list-entry", "{group}", group));
                            }
                        }
                        if (!plugin.isLuckPermsAvailable()) {
                            sender.sendMessage(plugin.getMessageManager().warning("maintenance.allowed-groups.no-luckperms"));
                        }
                    }
                    case "add" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-groups.usage-add"));
                            return true;
                        }
                        String groupName = args[2].toLowerCase();
                        // Validate group exists in LuckPerms if available
                        if (plugin.isLuckPermsAvailable()) {
                            try {
                                net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
                                net.luckperms.api.model.group.Group group = lp.getGroupManager().getGroup(groupName);
                                if (group == null) {
                                    sender.sendMessage(plugin.getMessageManager().warning("maintenance.allowed-groups.group-not-found", "{group}", groupName));
                                }
                            } catch (Exception ignored) {}
                        }
                        if (mm.addAllowedGroup(groupName)) {
                            sender.sendMessage(plugin.getMessageManager().success("maintenance.allowed-groups.added", "{group}", groupName));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>added group <white>" + groupName + "</white> to maintenance bypass.");
                        } else {
                            sender.sendMessage(plugin.getMessageManager().warning("maintenance.allowed-groups.already-added", "{group}", groupName));
                        }
                    }
                    case "remove" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-groups.usage-remove"));
                            return true;
                        }
                        String groupName = args[2].toLowerCase();
                        if (mm.removeAllowedGroup(groupName)) {
                            sender.sendMessage(plugin.getMessageManager().success("maintenance.allowed-groups.removed", "{group}", groupName));
                            plugin.getLogManager().log("admin",
                                    "<yellow>" + sender.getName() + "</yellow> <gray>removed group <white>" + groupName + "</white> from maintenance bypass.");
                        } else {
                            sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-groups.not-on-list", "{group}", groupName));
                        }
                    }
                    default -> sender.sendMessage(plugin.getMessageManager().error("maintenance.allowed-groups.unknown-action"));
                }
            }

            case "cooldown", "timer", "eta" -> {
                if (args.length < 2) {
                    String cd = mm.getCooldownText();
                    if (cd != null) {
                        sender.sendMessage(plugin.getMessageManager().info("maintenance.cooldown.current", "{cooldown}", cd));
                    } else {
                        sender.sendMessage(plugin.getMessageManager().info("maintenance.cooldown.no-cooldown"));
                    }
                    sender.sendMessage(plugin.getMessageManager().line("maintenance.cooldown.usage"));
                    sender.sendMessage(plugin.getMessageManager().line("maintenance.cooldown.usage-hint"));
                    return true;
                }
                String val = args[1].toLowerCase();
                if ("clear".equals(val) || "none".equals(val) || "off".equals(val)) {
                    mm.setCooldownEnd(-1);
                    sender.sendMessage(plugin.getMessageManager().success("maintenance.cooldown.cleared"));
                } else {
                    long duration = MaintenanceManager.parseDuration(val);
                    if (duration <= 0) {
                        sender.sendMessage(plugin.getMessageManager().error("maintenance.cooldown.invalid-duration"));
                        return true;
                    }
                    mm.setCooldownEnd(System.currentTimeMillis() + duration);
                    sender.sendMessage(plugin.getMessageManager().success("maintenance.cooldown.set", "{duration}", mm.getCooldownText()));
                }
            }

            default -> {
                sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.general.unknown-subcommand"));
                sendUsage(sender);
            }
        }

        return true;
    }

    private void sendStatus(CommandSender sender, MaintenanceManager mm) {
        sender.sendMessage(plugin.getMessageManager().prefixed("maintenance.general.status-header"));
        String statusValue = mm.isActive() ? plugin.getMessageManager().raw("maintenance.general.status-on") : plugin.getMessageManager().raw("maintenance.general.status-off");
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.status-line", "{status}", statusValue));
        String cd = mm.getCooldownText();
        String cooldownValue = cd != null ? "<yellow>" + cd : plugin.getMessageManager().raw("maintenance.general.status-cooldown-none");
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.status-cooldown", "{cooldown}", cooldownValue));
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.status-whitelisted", "{players}", String.valueOf(mm.getAllowedUsers().size()), "{groups}", String.valueOf(mm.getAllowedGroups().size())));
        List<String> groups = mm.getAllowedGroups();
        String groupsValue = groups.isEmpty() ? plugin.getMessageManager().raw("maintenance.general.status-bypass-none") : "<white>" + String.join(", ", groups);
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.status-bypass-groups", "{groups}", groupsValue));
        String opValue = mm.isOpsBypass() ? plugin.getMessageManager().raw("maintenance.general.status-op-enabled") : plugin.getMessageManager().raw("maintenance.general.status-op-disabled");
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.status-op-bypass", "{status}", opValue));
        String iconValue = mm.getCachedIcon() != null ? plugin.getMessageManager().raw("maintenance.general.status-icon-loaded") : plugin.getMessageManager().raw("maintenance.general.status-icon-none");
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.status-icon", "{status}", iconValue));
        sender.sendMessage(net.kyori.adventure.text.Component.empty());
        sendUsage(sender);
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.usage-mode"));
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.usage-users"));
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.usage-groups"));
        sender.sendMessage(plugin.getMessageManager().line("maintenance.general.usage-cooldown"));
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

