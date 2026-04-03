package org.justme.justPlugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Folia-compatible scheduler utility. Detects Folia at runtime and routes
 * scheduler calls to the appropriate API (Bukkit scheduler for Paper/Purpur,
 * region/entity/async schedulers for Folia).
 */
public final class SchedulerUtil {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    private SchedulerUtil() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Run a task on the next tick (global context).
     * On Folia: uses global region scheduler.
     * On Paper: uses Bukkit scheduler.
     */
    public static void runTask(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task after a delay (global context).
     * On Folia: uses global region scheduler.
     * On Paper: uses Bukkit scheduler.
     */
    public static void runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /**
     * Run a repeating task (global context).
     * On Folia: uses global region scheduler.
     * On Paper: uses Bukkit scheduler.
     * Returns a Cancellable wrapper.
     */
    public static CancellableTask runTaskTimer(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            var handle = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(),
                Math.max(1, delayTicks), periodTicks);
            return handle::cancel;
        } else {
            var bukkit = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
            return bukkit::cancel;
        }
    }

    /**
     * Run an async task (not tied to any region).
     * On Folia: uses async scheduler.
     * On Paper: uses Bukkit async scheduler.
     */
    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Run a repeating async task.
     * On Folia: uses async scheduler.
     * On Paper: uses Bukkit async scheduler.
     */
    public static CancellableTask runAsyncTimer(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            var handle = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(),
                Math.max(1, delayTicks) * 50L, periodTicks * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
            return handle::cancel;
        } else {
            var bukkit = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
            return bukkit::cancel;
        }
    }

    /**
     * Run a task tied to a specific entity (e.g., player).
     * On Folia: uses entity scheduler (runs in entity's region).
     * On Paper: uses Bukkit scheduler (same as global).
     */
    public static void runForEntity(JavaPlugin plugin, Entity entity, Runnable task) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, t -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a delayed task tied to a specific entity.
     */
    public static void runForEntityLater(JavaPlugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (FOLIA) {
            entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /**
     * Run a repeating task tied to a specific entity.
     */
    public static CancellableTask runForEntityTimer(JavaPlugin plugin, Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            var handle = entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null,
                Math.max(1, delayTicks), periodTicks);
            return () -> { if (handle != null) handle.cancel(); };
        } else {
            var bukkit = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
            return bukkit::cancel;
        }
    }

    /**
     * Run a task at a specific location's region.
     * On Folia: uses region scheduler.
     * On Paper: uses Bukkit scheduler.
     */
    public static void runAtLocation(JavaPlugin plugin, Location location, Runnable task) {
        if (FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, location, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Simple functional interface for cancelling scheduled tasks.
     */
    @FunctionalInterface
    public interface CancellableTask {
        void cancel();
    }
}
