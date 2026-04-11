package org.justme.justPlugin.commands.kits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.managers.KitManager;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all kit admin commands:
 * /kitcreate - opens creation GUI
 * /kitedit <name> - opens edit GUI
 * /kitrename <old> <new> - rename a kit
 * /kitdelete <name> [permanent] - archive or permanently delete a kit
 * /kitpublish <name> - publish a kit
 * /kitdisable <name> - disable a kit
 * /kitenable <name> - enable a kit
 * /kitarchive [restore|delete|deleteall] [name] - manage archived kits
 * /kitlist - list all kits with statuses
 */
@SuppressWarnings("NullableProblems")
public class KitAdminCommand implements TabExecutor {

    private final JustPlugin plugin;
    private final String commandType;

    /**
     * @param commandType the specific command: "kitcreate", "kitedit", "kitrename",
     *                    "kitdelete", "kitpublish", "kitdisable", "kitenable",
     *                    "kitarchive", "kitlist"
     */
    public KitAdminCommand(JustPlugin plugin, String commandType) {
        this.plugin = plugin;
        this.commandType = commandType;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        KitManager km = plugin.getKitManager();

        switch (commandType) {
            case "kitcreate" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
                    return true;
                }
                plugin.getKitEditGui().openCreate(player);
                return true;
            }
            case "kitedit" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitedit <name>"));
                    return true;
                }
                KitManager.KitData kit = km.getKit(args[0]);
                if (kit == null) {
                    player.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", args[0]));
                    return true;
                }
                plugin.getKitEditGui().openEdit(player, kit);
                return true;
            }
            case "kitrename" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitrename <old> <new>"));
                    return true;
                }
                String oldName = args[0];
                String newName = args[1];
                if (km.getKit(oldName) == null) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", oldName));
                    return true;
                }
                if (km.getKit(newName) != null) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-already-exists", "{kit}", newName));
                    return true;
                }
                if (km.renameKit(oldName, newName)) {
                    sender.sendMessage(plugin.getMessageManager().success("kits.kit-renamed",
                            "{old}", oldName, "{new}", newName));
                } else {
                    sender.sendMessage(plugin.getMessageManager().error("kits.rename-failed"));
                }
                return true;
            }
            case "kitdelete" -> {
                if (args.length < 1) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitdelete <name> [permanent]"));
                    return true;
                }
                String kitName = args[0];
                KitManager.KitData kit = km.getKit(kitName);
                if (kit == null) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", kitName));
                    return true;
                }
                boolean permanent = args.length >= 2 && args[1].equalsIgnoreCase("permanent");
                if (permanent) {
                    if (!sender.hasPermission("justplugin.kit.delete.permanent")) {
                        sender.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                        return true;
                    }
                    km.deleteKit(kitName);
                    sender.sendMessage(plugin.getMessageManager().success("kits.kit-deleted-permanent", "{kit}", kit.displayName));
                } else {
                    km.archiveKit(kitName);
                    sender.sendMessage(plugin.getMessageManager().success("kits.kit-archived", "{kit}", kit.displayName));
                }
                return true;
            }
            case "kitpublish" -> {
                if (args.length < 1) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitpublish <name>"));
                    return true;
                }
                KitManager.KitData kit = km.getKit(args[0]);
                if (kit == null) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", args[0]));
                    return true;
                }
                km.publishKit(args[0]);
                sender.sendMessage(plugin.getMessageManager().success("kits.kit-published", "{kit}", kit.displayName));
                return true;
            }
            case "kitdisable" -> {
                if (args.length < 1) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitdisable <name>"));
                    return true;
                }
                KitManager.KitData kit = km.getKit(args[0]);
                if (kit == null) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", args[0]));
                    return true;
                }
                km.disableKit(args[0]);
                sender.sendMessage(plugin.getMessageManager().success("kits.kit-disabled-msg", "{kit}", kit.displayName));
                return true;
            }
            case "kitenable" -> {
                if (args.length < 1) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitenable <name>"));
                    return true;
                }
                KitManager.KitData kit = km.getKit(args[0]);
                if (kit == null) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", args[0]));
                    return true;
                }
                km.enableKit(args[0]);
                sender.sendMessage(plugin.getMessageManager().success("kits.kit-enabled-msg", "{kit}", kit.displayName));
                return true;
            }
            case "kitarchive" -> {
                return handleArchiveCommand(sender, args, km);
            }
            case "kitlist" -> {
                return handleListCommand(sender, km);
            }
        }
        return true;
    }

    private boolean handleArchiveCommand(CommandSender sender, String[] args, KitManager km) {
        if (args.length == 0) {
            // List archived kits
            List<KitManager.KitData> archived = km.getAllKits().stream()
                    .filter(k -> "archived".equals(k.status))
                    .toList();
            if (archived.isEmpty()) {
                sender.sendMessage(plugin.getMessageManager().info("kits.no-archived-kits"));
                return true;
            }
            sender.sendMessage(plugin.getMessageManager().info("kits.archived-header"));
            for (KitManager.KitData kit : archived) {
                long daysAgo = kit.archiveDate > 0
                        ? (System.currentTimeMillis() - kit.archiveDate) / 86400000L
                        : 0;
                sender.sendMessage(CC.line("<gray>" + kit.name + " <dark_gray>(" + kit.displayName + ") <gray>- archived " + daysAgo + " day(s) ago"));
            }
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "restore" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitarchive restore <name>"));
                    return true;
                }
                if (!sender.hasPermission("justplugin.kit.archive.restore")) {
                    sender.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }
                if (km.restoreKit(args[1])) {
                    sender.sendMessage(plugin.getMessageManager().success("kits.kit-restored", "{kit}", args[1]));
                } else {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-archived", "{kit}", args[1]));
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitarchive delete <name>"));
                    return true;
                }
                if (!sender.hasPermission("justplugin.kit.archive.delete")) {
                    sender.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }
                KitManager.KitData kit = km.getKit(args[1]);
                if (kit == null || !"archived".equals(kit.status)) {
                    sender.sendMessage(plugin.getMessageManager().error("kits.kit-not-archived", "{kit}", args[1]));
                    return true;
                }
                km.deleteKit(args[1]);
                sender.sendMessage(plugin.getMessageManager().success("kits.kit-deleted-permanent", "{kit}", args[1]));
            }
            case "deleteall" -> {
                if (!sender.hasPermission("justplugin.kit.archive.delete")) {
                    sender.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }
                List<String> toDelete = km.getAllKits().stream()
                        .filter(k -> "archived".equals(k.status))
                        .map(k -> k.name)
                        .toList();
                if (toDelete.isEmpty()) {
                    sender.sendMessage(plugin.getMessageManager().info("kits.no-archived-kits"));
                    return true;
                }
                for (String name : toDelete) {
                    km.deleteKit(name);
                }
                sender.sendMessage(plugin.getMessageManager().success("kits.all-archives-deleted",
                        "{count}", String.valueOf(toDelete.size())));
            }
            default -> sender.sendMessage(plugin.getMessageManager().error("general.invalid-usage",
                    "{usage}", "/kitarchive [restore|delete|deleteall] [name]"));
        }
        return true;
    }

    private boolean handleListCommand(CommandSender sender, KitManager km) {
        var allKits = km.getAllKits();
        if (allKits.isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().info("kits.no-kits-exist"));
            return true;
        }

        sender.sendMessage(plugin.getMessageManager().info("kits.all-kits-header", "{count}", String.valueOf(allKits.size())));
        for (KitManager.KitData kit : allKits) {
            String statusColor = switch (kit.status) {
                case "published" -> kit.enabled ? "<green>" : "<yellow>";
                case "pending" -> "<gold>";
                case "archived" -> "<red>";
                default -> "<gray>";
            };
            String enabledTag = kit.enabled ? "" : " <red>[DISABLED]";
            String cooldownStr = CooldownManager.formatTime(kit.cooldownSeconds);
            sender.sendMessage(CC.line(statusColor + kit.name
                    + " <dark_gray>(" + kit.displayName + ")"
                    + " <dark_gray>[" + kit.status + "]"
                    + enabledTag
                    + " <dark_gray>- CD: " + cooldownStr
                    + " <dark_gray>- " + kit.items.size() + " items"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        KitManager km = plugin.getKitManager();

        switch (commandType) {
            case "kitedit", "kitpublish", "kitdisable", "kitenable" -> {
                if (args.length == 1) {
                    return km.getKitNames().stream()
                            .filter(n -> n.startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
            case "kitrename" -> {
                if (args.length == 1) {
                    return km.getKitNames().stream()
                            .filter(n -> n.startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
            case "kitdelete" -> {
                if (args.length == 1) {
                    return km.getKitNames().stream()
                            .filter(n -> n.startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                }
                if (args.length == 2) {
                    return List.of("permanent").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
            case "kitarchive" -> {
                if (args.length == 1) {
                    return List.of("restore", "delete", "deleteall").stream()
                            .filter(s -> s.startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                }
                if (args.length == 2 && (args[0].equalsIgnoreCase("restore") || args[0].equalsIgnoreCase("delete"))) {
                    return km.getAllKits().stream()
                            .filter(k -> "archived".equals(k.status))
                            .map(k -> k.name)
                            .filter(n -> n.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        return List.of();
    }
}
