package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackupManager;
import org.justme.justPlugin.managers.MessageManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command handler for /jpbackup (export, import, list, delete).
 * All backup I/O runs asynchronously.
 */
@SuppressWarnings("NullableProblems")
public class BackupCommand implements TabExecutor {

    private final JustPlugin plugin;
    private final MessageManager mm;

    /** Tracks players who have been prompted to confirm an import. */
    private final Map<UUID, String> pendingImports = new HashMap<>();

    public BackupCommand(JustPlugin plugin) {
        this.plugin = plugin;
        this.mm = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.info("misc.backup.usage"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "export" -> handleExport(sender);
            case "import" -> handleImport(sender, args);
            case "list" -> handleList(sender);
            case "delete" -> handleDelete(sender, args);
            default -> sender.sendMessage(mm.info("misc.backup.usage"));
        }
        return true;
    }

    // ==================== Subcommands ====================

    private void handleExport(CommandSender sender) {
        if (!sender.hasPermission("justplugin.backup.export")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        sender.sendMessage(mm.info("misc.backup.exporting"));
        plugin.getBackupManager().createBackupAsync(result -> {
            if (result.success()) {
                sender.sendMessage(mm.success("misc.backup.export-success",
                        "{filename}", result.filename(),
                        "{size}", result.size()));
            } else {
                sender.sendMessage(mm.error("misc.backup.export-failed",
                        "{error}", result.error()));
            }
        });
    }

    private void handleImport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("justplugin.backup.import")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        UUID senderId = getSenderId(sender);

        // Check if this is a confirmation of a pending import
        if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
            String pendingFile = pendingImports.remove(senderId);
            if (pendingFile == null) {
                sender.sendMessage(mm.error("misc.backup.not-found"));
                return;
            }

            sender.sendMessage(mm.info("misc.backup.exporting"));
            plugin.getBackupManager().importBackupAsync(pendingFile, result -> {
                if (result.success()) {
                    sender.sendMessage(mm.success("misc.backup.import-success"));
                } else {
                    sender.sendMessage(mm.error("misc.backup.export-failed",
                            "{error}", result.error()));
                }
            });
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(mm.info("misc.backup.usage"));
            return;
        }

        String filename = args[1];
        if (!plugin.getBackupManager().backupExists(filename)) {
            sender.sendMessage(mm.error("misc.backup.not-found"));
            return;
        }

        // Store pending import and prompt for confirmation
        pendingImports.put(senderId, filename);
        sender.sendMessage(mm.warning("misc.backup.import-confirm"));
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("justplugin.backup.list")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        List<BackupManager.BackupInfo> backups = plugin.getBackupManager().listBackups();
        if (backups.isEmpty()) {
            sender.sendMessage(mm.info("misc.backup.list-empty"));
            return;
        }

        sender.sendMessage(mm.info("misc.backup.list-header"));
        for (BackupManager.BackupInfo info : backups) {
            sender.sendMessage(mm.info("misc.backup.list-entry",
                    "{filename}", info.filename(),
                    "{size}", info.size(),
                    "{date}", info.date()));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("justplugin.backup.delete")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(mm.info("misc.backup.usage"));
            return;
        }

        String filename = args[1];
        if (plugin.getBackupManager().deleteBackup(filename)) {
            sender.sendMessage(mm.success("misc.backup.deleted", "{filename}", filename));
        } else {
            sender.sendMessage(mm.error("misc.backup.not-found"));
        }
    }

    // ==================== Tab Completion ====================

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("justplugin.backup.export")) subs.add("export");
            if (sender.hasPermission("justplugin.backup.import")) subs.add("import");
            if (sender.hasPermission("justplugin.backup.list")) subs.add("list");
            if (sender.hasPermission("justplugin.backup.delete")) subs.add("delete");
            return filterStartsWith(subs, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ("import".equals(sub) || "delete".equals(sub)) {
                // Suggest backup filenames
                List<String> names = plugin.getBackupManager().listBackups().stream()
                        .map(BackupManager.BackupInfo::filename)
                        .collect(Collectors.toList());
                if ("import".equals(sub)) names.add("confirm");
                return filterStartsWith(names, args[1]);
            }
        }

        return List.of();
    }

    // ==================== Utilities ====================

    private UUID getSenderId(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player player) {
            return player.getUniqueId();
        }
        // Console gets a fixed UUID
        return new UUID(0, 0);
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
