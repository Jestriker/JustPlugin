package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Manages backup/export of the plugin data folder to ZIP archives.
 * Supports manual export/import, auto-backup on a daily schedule,
 * and configurable max backup retention.
 * <p>
 * All heavy I/O runs async to avoid blocking the main thread.
 */
public class BackupManager {

    private final JustPlugin plugin;
    private final File backupDir;
    private SchedulerUtil.CancellableTask autoBackupTask;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public BackupManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        startAutoBackup();
    }

    // ==================== Auto-Backup ====================

    /**
     * Starts the auto-backup scheduled task if enabled in config.
     * Runs once daily (every 24h = 1,728,000 ticks).
     */
    public void startAutoBackup() {
        if (autoBackupTask != null) {
            autoBackupTask.cancel();
            autoBackupTask = null;
        }

        if (!plugin.getConfig().getBoolean("backup.auto-backup", false)) return;

        // Run daily: 20 ticks/sec * 60 sec * 60 min * 24 hr = 1,728,000 ticks
        long dailyTicks = 20L * 60 * 60 * 24;
        autoBackupTask = SchedulerUtil.runAsyncTimer(plugin, () -> {
            plugin.getLogger().info("[Backup] Running scheduled auto-backup...");
            BackupResult result = createBackupSync();
            if (result.success()) {
                plugin.getLogger().info("[Backup] Auto-backup complete: " + result.filename() + " (" + result.size() + ")");
            } else {
                plugin.getLogger().warning("[Backup] Auto-backup failed: " + result.error());
            }
            enforceMaxBackups();
        }, dailyTicks, dailyTicks);
    }

    /**
     * Shuts down the auto-backup task.
     */
    public void shutdown() {
        if (autoBackupTask != null) {
            autoBackupTask.cancel();
            autoBackupTask = null;
        }
    }

    // ==================== Export ====================

    /**
     * Creates a backup asynchronously and calls the callback on the main thread.
     */
    public void createBackupAsync(java.util.function.Consumer<BackupResult> callback) {
        SchedulerUtil.runAsync(plugin, () -> {
            BackupResult result = createBackupSync();
            enforceMaxBackups();
            SchedulerUtil.runTask(plugin, () -> callback.accept(result));
        });
    }

    /**
     * Creates a backup synchronously. Should be called from an async context.
     */
    public BackupResult createBackupSync() {
        String filename = "backup-" + DATE_FORMAT.format(new Date()) + ".zip";
        File zipFile = new File(backupDir, filename);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            File dataFolder = plugin.getDataFolder();
            Path dataPath = dataFolder.toPath();

            boolean includeConfig = plugin.getConfig().getBoolean("backup.include-config", true);

            Files.walkFileTree(dataPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    // Skip the backups directory itself
                    if (dir.equals(backupDir.toPath())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String relative = dataPath.relativize(file).toString().replace('\\', '/');

                    // Skip config.yml if not including config
                    if (!includeConfig && relative.equals("config.yml")) {
                        return FileVisitResult.CONTINUE;
                    }

                    zos.putNextEntry(new ZipEntry(relative));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });

            String size = formatFileSize(zipFile.length());
            return new BackupResult(true, filename, size, null);

        } catch (IOException e) {
            // Clean up partial file
            if (zipFile.exists()) zipFile.delete();
            return new BackupResult(false, null, null, e.getMessage());
        }
    }

    // ==================== Import ====================

    /**
     * Imports a backup ZIP asynchronously and calls the callback on the main thread.
     */
    public void importBackupAsync(String filename, java.util.function.Consumer<BackupResult> callback) {
        File zipFile = new File(backupDir, filename);
        if (!zipFile.exists() || !zipFile.isFile()) {
            callback.accept(new BackupResult(false, null, null, "File not found"));
            return;
        }

        SchedulerUtil.runAsync(plugin, () -> {
            BackupResult result = importBackupSync(zipFile);
            SchedulerUtil.runTask(plugin, () -> callback.accept(result));
        });
    }

    /**
     * Imports a backup synchronously. Should be called from an async context.
     */
    private BackupResult importBackupSync(File zipFile) {
        File dataFolder = plugin.getDataFolder();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(dataFolder, entry.getName()).mkdirs();
                    continue;
                }

                // Security: prevent zip-slip attacks
                File outFile = new File(dataFolder, entry.getName());
                if (!outFile.toPath().normalize().startsWith(dataFolder.toPath().normalize())) {
                    continue; // Skip entries that escape the data folder
                }

                outFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }

            return new BackupResult(true, zipFile.getName(), null, null);

        } catch (IOException e) {
            return new BackupResult(false, null, null, e.getMessage());
        }
    }

    // ==================== List / Delete ====================

    /**
     * Returns a list of backup files sorted by modification time (newest first).
     */
    public List<BackupInfo> listBackups() {
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (files == null || files.length == 0) return List.of();

        List<BackupInfo> backups = new ArrayList<>();
        SimpleDateFormat display = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File f : files) {
            backups.add(new BackupInfo(
                    f.getName(),
                    formatFileSize(f.length()),
                    display.format(new Date(f.lastModified()))
            ));
        }
        // Sort newest first
        backups.sort((a, b) -> b.filename().compareTo(a.filename()));
        return backups;
    }

    /**
     * Deletes a backup file by name.
     *
     * @return true if deleted, false if not found
     */
    public boolean deleteBackup(String filename) {
        File file = new File(backupDir, filename);
        if (!file.exists() || !file.isFile()) return false;
        // Security: ensure the file is within the backups directory
        if (!file.toPath().normalize().startsWith(backupDir.toPath().normalize())) return false;
        return file.delete();
    }

    /**
     * Enforces the max-backups config limit by deleting the oldest backups.
     */
    private void enforceMaxBackups() {
        int max = plugin.getConfig().getInt("backup.max-backups", 7);
        if (max <= 0) return;

        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (files == null || files.length <= max) return;

        // Sort oldest first
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        int toDelete = files.length - max;
        for (int i = 0; i < toDelete; i++) {
            if (files[i].delete()) {
                plugin.getLogger().info("[Backup] Removed old backup: " + files[i].getName());
            }
        }
    }

    // ==================== Utilities ====================

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public boolean backupExists(String filename) {
        File file = new File(backupDir, filename);
        return file.exists() && file.isFile()
                && file.toPath().normalize().startsWith(backupDir.toPath().normalize());
    }

    // ==================== Records ====================

    public record BackupResult(boolean success, String filename, String size, String error) {}

    public record BackupInfo(String filename, String size, String date) {}
}
