package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final JustPlugin plugin;

    /**
     * Request data: stores a pending TPA or TPAHere request.
     */
    public static class TpaRequest {
        public final UUID sender;
        public final UUID target;
        /** true = sender wants to go to target (TPA), false = sender wants target to come to them (TPAHere) */
        public final boolean senderGoesToTarget;
        public final long timestamp;

        public TpaRequest(UUID sender, UUID target, boolean senderGoesToTarget) {
            this.sender = sender;
            this.target = target;
            this.senderGoesToTarget = senderGoesToTarget;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // One pending request per sender: sender UUID -> request
    private final Map<UUID, TpaRequest> outgoingRequests = new ConcurrentHashMap<>();
    // Reverse lookup: target UUID -> sender UUID (for accept/reject)
    private final Map<UUID, UUID> incomingRequests = new ConcurrentHashMap<>();

    // Players currently in a teleport delay (waiting to tp after accept)
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    // Starting location for move-cancel detection
    private final Map<UUID, Location> teleportStartLocations = new ConcurrentHashMap<>();

    // Back locations: player -> last location before tp/death
    private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();

    private final int requestTimeout;
    private final double teleportDelay;

    public TeleportManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.requestTimeout = plugin.getConfig().getInt("teleport.request-timeout", 60);
        this.teleportDelay = plugin.getConfig().getDouble("teleport.delay", 3.0);
        startExpiryTask();
    }

    // --- Expiry ---
    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long timeout = requestTimeout * 1000L;
            Iterator<Map.Entry<UUID, TpaRequest>> it = outgoingRequests.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, TpaRequest> entry = it.next();
                TpaRequest req = entry.getValue();
                if (now - req.timestamp > timeout) {
                    it.remove();
                    incomingRequests.remove(req.target);
                    // Notify both sides
                    Player senderP = Bukkit.getPlayer(req.sender);
                    Player targetP = Bukkit.getPlayer(req.target);
                    if (senderP != null) senderP.sendMessage(CC.warning("Your teleport request to <yellow>" + (targetP != null ? targetP.getName() : "?") + "</yellow> has expired."));
                    if (targetP != null) targetP.sendMessage(CC.warning("The teleport request from <yellow>" + (senderP != null ? senderP.getName() : "?") + "</yellow> has expired."));
                }
            }
        }, 20L * 5, 20L * 5);
    }

    // --- Send Requests ---
    /**
     * @return null if request sent successfully, or an error message string
     */
    public String sendTpaRequest(UUID sender, UUID target) {
        return sendRequest(sender, target, true);
    }

    public String sendTpaHereRequest(UUID sender, UUID target) {
        return sendRequest(sender, target, false);
    }

    private String sendRequest(UUID sender, UUID target, boolean senderGoesToTarget) {
        TpaRequest existing = outgoingRequests.get(sender);
        if (existing != null) {
            if (existing.target.equals(target)) {
                return "already_same";
            } else {
                Player existingTarget = Bukkit.getPlayer(existing.target);
                String existingName = existingTarget != null ? existingTarget.getName() : "someone";
                return "already_other:" + existingName;
            }
        }
        TpaRequest req = new TpaRequest(sender, target, senderGoesToTarget);
        outgoingRequests.put(sender, req);
        incomingRequests.put(target, sender);
        return null; // success
    }

    // --- Query ---
    public boolean hasOutgoingRequest(UUID sender) {
        return outgoingRequests.containsKey(sender);
    }

    public TpaRequest getOutgoingRequest(UUID sender) {
        return outgoingRequests.get(sender);
    }

    public boolean hasIncomingRequest(UUID target) {
        return incomingRequests.containsKey(target);
    }

    public TpaRequest getIncomingRequest(UUID target) {
        UUID sender = incomingRequests.get(target);
        return sender != null ? outgoingRequests.get(sender) : null;
    }

    // --- Cancel / Remove ---
    public boolean cancelOutgoingRequest(UUID sender) {
        TpaRequest req = outgoingRequests.remove(sender);
        if (req != null) {
            incomingRequests.remove(req.target);
            return true;
        }
        return false;
    }

    public void removeRequest(UUID sender) {
        TpaRequest req = outgoingRequests.remove(sender);
        if (req != null) {
            incomingRequests.remove(req.target);
        }
    }

    // --- Accept: delayed teleport with move-cancel and safe landing ---
    public void acceptRequest(UUID targetUuid) {
        TpaRequest req = getIncomingRequest(targetUuid);
        if (req == null) return;

        removeRequest(req.sender);

        Player senderP = Bukkit.getPlayer(req.sender);
        Player targetP = Bukkit.getPlayer(req.target);
        if (senderP == null || targetP == null) return;

        // Determine who teleports and where
        Player teleporter;
        Player destination;
        if (req.senderGoesToTarget) {
            teleporter = senderP; // sender goes to target
            destination = targetP;
        } else {
            teleporter = targetP; // target goes to sender (TPAHere)
            destination = senderP;
        }

        double delay = roundToHalf(teleportDelay);
        if (delay <= 0 || teleporter.hasPermission("justplugin.teleport.bypass")) {
            // Instant teleport
            executeSafeTeleport(teleporter, destination.getLocation());
            return;
        }

        long delayTicks = (long) (delay * 20);
        Location startLoc = teleporter.getLocation().clone();
        teleportStartLocations.put(teleporter.getUniqueId(), startLoc);

        String delayStr = delay == (int) delay ? String.valueOf((int) delay) : String.valueOf(delay);
        teleporter.sendMessage(CC.info("Teleporting in <yellow>" + delayStr + "</yellow> seconds. <gray>Don't move!"));
        teleporter.sendMessage(CC.info("Type <yellow>/tpacancel</yellow> to cancel."));
        destination.sendMessage(CC.success("<yellow>" + teleporter.getName() + "</yellow> is teleporting to you in <yellow>" + delayStr + "</yellow> seconds."));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(teleporter.getUniqueId());
            teleportStartLocations.remove(teleporter.getUniqueId());
            if (!teleporter.isOnline()) return;
            executeSafeTeleport(teleporter, destination.isOnline() ? destination.getLocation() : teleporter.getLocation());
        }, delayTicks);
        pendingTeleports.put(teleporter.getUniqueId(), task);
    }

    /**
     * Called from PlayerMoveEvent — cancels teleport if the player moved.
     */
    public void handleMoveDuringTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        Location startLoc = teleportStartLocations.get(uuid);
        if (startLoc == null) return;
        // Only cancel on actual block movement (not just head rotation)
        if (player.getLocation().getBlockX() == startLoc.getBlockX()
                && player.getLocation().getBlockY() == startLoc.getBlockY()
                && player.getLocation().getBlockZ() == startLoc.getBlockZ()) {
            return;
        }
        cancelPendingTeleport(uuid);
        player.sendMessage(CC.error("Teleportation cancelled. You moved!"));
    }

    public boolean isWaitingToTeleport(UUID uuid) {
        return pendingTeleports.containsKey(uuid);
    }

    public void cancelPendingTeleport(UUID uuid) {
        BukkitTask task = pendingTeleports.remove(uuid);
        if (task != null) task.cancel();
        teleportStartLocations.remove(uuid);
    }

    // --- Safe Teleport ---
    private void executeSafeTeleport(Player player, Location destination) {
        Location safeLoc = getSafeLocation(destination);
        setBackLocation(player.getUniqueId(), player.getLocation());
        player.teleportAsync(safeLoc);
        player.sendMessage(CC.success("Teleported!"));
    }

    /**
     * Ensures the destination has a solid non-damaging block below.
     */
    public Location getSafeLocation(Location loc) {
        Location safe = loc.clone();
        World world = safe.getWorld();
        // Search downward from current Y, then upward, to find safe ground
        int startY = safe.getBlockY();
        for (int y = startY; y >= world.getMinHeight() + 1; y--) {
            Location check = new Location(world, safe.getX(), y, safe.getZ());
            Material below = new Location(world, safe.getX(), y - 1, safe.getZ()).getBlock().getType();
            Material at = check.getBlock().getType();
            Material above = new Location(world, safe.getX(), y + 1, safe.getZ()).getBlock().getType();
            if (isSolidSafe(below) && isPassable(at) && isPassable(above)) {
                return new Location(world, safe.getX(), y, safe.getZ(), safe.getYaw(), safe.getPitch());
            }
        }
        // Search upward
        for (int y = startY; y <= world.getMaxHeight() - 2; y++) {
            Location check = new Location(world, safe.getX(), y, safe.getZ());
            Material below = new Location(world, safe.getX(), y - 1, safe.getZ()).getBlock().getType();
            Material at = check.getBlock().getType();
            Material above = new Location(world, safe.getX(), y + 1, safe.getZ()).getBlock().getType();
            if (isSolidSafe(below) && isPassable(at) && isPassable(above)) {
                return new Location(world, safe.getX(), y, safe.getZ(), safe.getYaw(), safe.getPitch());
            }
        }
        // Fallback: highest block
        int highY = world.getHighestBlockYAt(safe.getBlockX(), safe.getBlockZ()) + 1;
        return new Location(world, safe.getX(), highY, safe.getZ(), safe.getYaw(), safe.getPitch());
    }

    private boolean isSolidSafe(Material mat) {
        if (!mat.isSolid()) return false;
        // Damaging or unsafe blocks
        return mat != Material.LAVA && mat != Material.MAGMA_BLOCK && mat != Material.FIRE
                && mat != Material.SOUL_FIRE && mat != Material.CACTUS
                && mat != Material.SWEET_BERRY_BUSH && mat != Material.WITHER_ROSE
                && mat != Material.CAMPFIRE && mat != Material.SOUL_CAMPFIRE;
    }

    private boolean isPassable(Material mat) {
        return !mat.isSolid() && mat != Material.LAVA && mat != Material.FIRE && mat != Material.SOUL_FIRE
                && mat != Material.WATER && mat != Material.POWDER_SNOW;
    }

    // --- Round delay to nearest 0.5 ---
    private double roundToHalf(double value) {
        if (value <= 0) return 0;
        return Math.round(value * 2.0) / 2.0;
    }

    // --- Back ---
    public void setBackLocation(UUID uuid, Location location) {
        backLocations.put(uuid, location.clone());
    }

    public Location getBackLocation(UUID uuid) {
        return backLocations.get(uuid);
    }

    // --- Teleport with delay (non-TPA, e.g. /warp, /home, /spawn) ---
    public void teleport(Player player, Location location) {
        setBackLocation(player.getUniqueId(), player.getLocation());
        double delay = roundToHalf(teleportDelay);
        if (delay <= 0 || player.hasPermission("justplugin.teleport.bypass")) {
            Location safeLoc = getSafeLocation(location);
            player.teleportAsync(safeLoc);
            return;
        }
        long delayTicks = (long) (delay * 20);
        Location startLoc = player.getLocation().clone();
        teleportStartLocations.put(player.getUniqueId(), startLoc);
        String delayStr = delay == (int) delay ? String.valueOf((int) delay) : String.valueOf(delay);
        player.sendMessage(CC.info("Teleporting in <yellow>" + delayStr + "</yellow> seconds. Don't move!"));
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(player.getUniqueId());
            teleportStartLocations.remove(player.getUniqueId());
            if (!player.isOnline()) return;
            Location safeLoc = getSafeLocation(location);
            setBackLocation(player.getUniqueId(), player.getLocation());
            player.teleportAsync(safeLoc);
            player.sendMessage(CC.success("Teleported!"));
        }, delayTicks);
        pendingTeleports.put(player.getUniqueId(), task);
    }

    // --- Wild/Random TP ---
    public Location getRandomLocation(World world) {
        Random random = new Random();
        int range = plugin.getConfig().getInt("teleport.wild-range", 5000);
        int minRange = plugin.getConfig().getInt("teleport.wild-min-range", 500);
        int attempts = 0;
        while (attempts < 20) {
            int x = random.nextInt(range * 2) - range;
            int z = random.nextInt(range * 2) - range;
            if (Math.abs(x) < minRange && Math.abs(z) < minRange) {
                attempts++;
                continue;
            }
            int y = world.getHighestBlockYAt(x, z);
            if (y < world.getMinHeight() + 5) {
                attempts++;
                continue;
            }
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (!loc.getBlock().getType().isAir()) {
                attempts++;
                continue;
            }
            return loc;
        }
        int x = random.nextInt(range * 2) - range;
        int z = random.nextInt(range * 2) - range;
        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }
}

