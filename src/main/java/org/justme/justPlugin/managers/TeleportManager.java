package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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

    public TeleportManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.requestTimeout = plugin.getConfig().getInt("teleport.request-timeout", 60);
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
    @SuppressWarnings("unused")
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
    public void cancelOutgoingRequest(UUID sender) {
        TpaRequest req = outgoingRequests.remove(sender);
        if (req != null) {
            incomingRequests.remove(req.target);
        }
    }

    public void removeRequest(UUID sender) {
        TpaRequest req = outgoingRequests.remove(sender);
        if (req != null) {
            incomingRequests.remove(req.target);
        }
    }

    // --- Accept: delayed teleport with move-cancel, damage-cancel, and safe landing ---
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

        String featureKey = req.senderGoesToTarget ? "tpa" : "tpahere";
        boolean safetyEnabled = plugin.getConfig().getBoolean("teleport.safe-teleport-" + featureKey, true);
        String safetyBypassPerm = "justplugin." + featureKey + ".unsafetp";
        String delayBypassPerm = req.senderGoesToTarget ? "justplugin.tpa.cooldownbypass" : "justplugin.tpahere.cooldownbypass";

        if (safetyEnabled) {
            boolean playerFlying = destination.isFlying();
            boolean unsafeLocation = !isLocationSafe(destination.getLocation());
            boolean isUnsafe = playerFlying || unsafeLocation;

            if (isUnsafe) {
                if (teleporter.hasPermission(safetyBypassPerm)) {
                    // Store pending unsafe confirmation - do NOT teleport yet
                    pendingUnsafeTps.put(teleporter.getUniqueId(),
                            new PendingUnsafeTp(destination.getLocation(), delayBypassPerm, featureKey));
                    sendUnsafeWarningWithConfirm(teleporter);
                    destination.sendMessage(CC.warning("Teleport request cancelled - your current location is unsafe for teleportation."));
                    return;
                } else {
                    String reason = playerFlying
                            ? "the destination player is flying"
                            : "hazardous blocks detected at the destination";
                    teleporter.sendMessage(CC.error("Teleportation cancelled - " + reason + "!"));
                    destination.sendMessage(CC.warning("Teleport request cancelled - your current location is unsafe for teleportation."));
                    return;
                }
            }
        }

        // If we reach here, the location is safe (or safety is disabled)
        // Check bypass permission for teleport cooldown (pre-TP countdown)
        double cooldown = roundToHalf(plugin.getCooldownManager().getCooldownSeconds(featureKey));
        if (cooldown <= 0 || teleporter.hasPermission(delayBypassPerm)) {
            // Instant teleport
            executeTeleport(teleporter, destination.getLocation(), true);
            return;
        }

        int countdownSecs = Math.max(1, (int) Math.ceil(cooldown));

        boolean clickable = plugin.getConfig().getBoolean("clickable-commands.tpa", true);
        String cancelCmd = CC.clickCmd("<yellow>/tpacancel</yellow>", "/tpacancel", clickable);

        teleporter.sendMessage(CC.info("Don't move or take damage! Type " + cancelCmd + " to cancel."));
        destination.sendMessage(CC.success("<yellow>" + teleporter.getName() + "</yellow> is teleporting to you in <yellow>" + countdownSecs + "</yellow> seconds."));

        final boolean finalSafetyEnabled = safetyEnabled;

        startCountdown(teleporter, countdownSecs, () -> {
            if (!teleporter.isOnline()) return;

            // Re-check safety before teleporting
            Location destLoc = destination.isOnline() ? destination.getLocation() : teleporter.getLocation();
            if (finalSafetyEnabled) {
                boolean destFlying = destination.isOnline() && destination.isFlying();
                boolean destUnsafe = !isLocationSafe(destLoc);
                if (destFlying || destUnsafe) {
                    if (teleporter.hasPermission(safetyBypassPerm)) {
                        pendingUnsafeTps.put(teleporter.getUniqueId(),
                                new PendingUnsafeTp(destLoc, delayBypassPerm, featureKey));
                        sendUnsafeWarningWithConfirm(teleporter);
                    } else {
                        teleporter.sendMessage(CC.error("Teleportation cancelled - the destination became unsafe!"));
                    }
                    return;
                }
            }

            executeTeleport(teleporter, destLoc, true);
        });
    }

    /**
     * Called from PlayerMoveEvent - cancels teleport if the player moved.
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

    /**
     * Called from EntityDamageEvent - cancels pending teleport if damaged during warmup.
     */
    public void handleDamageDuringTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        if (!pendingTeleports.containsKey(uuid)) return;
        cancelPendingTeleport(uuid);
        player.sendMessage(CC.error("Teleportation cancelled. You took damage!"));
    }

    public boolean isWaitingToTeleport(UUID uuid) {
        return pendingTeleports.containsKey(uuid);
    }

    public void cancelPendingTeleport(UUID uuid) {
        BukkitTask task = pendingTeleports.remove(uuid);
        if (task != null) task.cancel();
        teleportStartLocations.remove(uuid);
    }

    /**
     * Starts a per-second countdown for a player waiting to teleport.
     * Sends a chat message each second ("Teleporting in 3...", "2...", "1...").
     * When the countdown reaches 0, executes the onComplete callback.
     * The task is stored in pendingTeleports so it can be cancelled by move/damage.
     *
     * @param player     the player teleporting
     * @param seconds    total countdown seconds (must be >= 1)
     * @param onComplete called on the main thread when countdown hits 0
     */
    private void startCountdown(Player player, int seconds, Runnable onComplete) {
        UUID uuid = player.getUniqueId();
        Location startLoc = player.getLocation().clone();
        teleportStartLocations.put(uuid, startLoc);

        final int[] remaining = {seconds};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelPendingTeleport(uuid);
                return;
            }
            if (remaining[0] > 0) {
                player.sendMessage(CC.info("Teleporting in <yellow>" + remaining[0] + "</yellow>..."));
                remaining[0]--;
            } else {
                // Countdown complete - cancel the repeating task and run the teleport
                cancelPendingTeleport(uuid);
                onComplete.run();
            }
        }, 0L, 20L); // start immediately, repeat every second

        pendingTeleports.put(uuid, task);
    }

    // --- Teleport Execution ---
    private void executeTeleport(Player player, Location destination, boolean useSafeLocation) {
        Location finalLoc = useSafeLocation ? getSafeLocation(destination) : destination;
        setBackLocation(player.getUniqueId(), player.getLocation());
        player.teleportAsync(finalLoc).thenAccept(success -> {
            // Schedule back on the main thread since thenAccept may run asynchronously
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!success || !player.isOnline()) return;
                player.sendMessage(CC.success("Teleported!"));
                // Post-teleport safety check: verify destination is still safe 1 tick later
                // This guards against race conditions where the environment changes during teleport
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!player.isOnline()) return;
                    Location currentLoc = player.getLocation();
                    if (!isLocationSafe(currentLoc)) {
                        Location safeLoc = getSafeLocation(currentLoc);
                        if (safeLoc != null && isLocationSafe(safeLoc)) {
                            player.teleportAsync(safeLoc);
                            player.sendMessage(CC.warning("You were moved to a safe location nearby."));
                        }
                    }
                }, 1L);
            });
        });
    }

    /**
     * Sends an unsafe destination warning with clickable creative/god mode buttons
     * AND a "TP Anyway" confirmation button that calls /tpunsafeconfirm.
     * Only shows Creative/God buttons if the player has the appropriate permissions.
     */
    private void sendUnsafeWarningWithConfirm(Player player) {
        boolean hasGm = player.hasPermission("justplugin.gamemode");
        boolean hasGod = player.hasPermission("justplugin.god");

        if (hasGm || hasGod) {
            player.sendMessage(CC.warning("⚠ The destination is unsafe! It is suggested to enable protection before teleporting:"));
        } else {
            player.sendMessage(CC.warning("⚠ The destination is unsafe!"));
        }

        Component buttons = CC.translate("  ");
        if (hasGm) {
            buttons = buttons.append(CC.translate("<click:run_command:'/gmc'><hover:show_text:'<gray>Click to switch to <green>Creative Mode'><gold><bold>[Creative Mode]</bold></gold></hover></click> "));
        }
        if (hasGod) {
            buttons = buttons.append(CC.translate("<click:run_command:'/god'><hover:show_text:'<gray>Click to enable <green>God Mode'><gold><bold>[God Mode]</bold></gold></hover></click> "));
        }
        buttons = buttons.append(CC.translate("<click:run_command:'/tpunsafeconfirm'><hover:show_text:'<gray>Click to teleport anyway\n<red>⚠ The destination is unsafe!'><red><bold>[TP Anyway]</bold></red></hover></click>"));
        player.sendMessage(buttons);
    }

    // ==========================================
    // Safety Check - 3×3 area around destination
    // ==========================================

    /**
     * Checks if a location is safe for teleportation using a 3×3 area check.
     * Validates: standing block is solid+safe, 3 blocks below + 2 blocks player occupies
     * are free of hazardous blocks in a 3×3 footprint.
     */
    public boolean isLocationSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        // Standing block (directly below) must be solid and safe
        Block standingBlock = world.getBlockAt(cx, cy - 1, cz);
        if (!standingBlock.getType().isSolid() || isDangerousBlock(standingBlock.getType())) {
            return false;
        }

        // Check 3×3 area around the destination
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = cx + dx;
                int z = cz + dz;

                // 3 blocks below the player (y-1, y-2, y-3)
                for (int y = cy - 3; y <= cy - 1; y++) {
                    if (y < world.getMinHeight()) continue;
                    if (isDangerousBlock(world.getBlockAt(x, y, z).getType())) return false;
                }

                // 2 blocks the player occupies (y, y+1)
                for (int y = cy; y <= cy + 1; y++) {
                    if (isDangerousBlock(world.getBlockAt(x, y, z).getType())) return false;
                }
            }
        }
        return true;
    }

    /**
     * Expanded dangerous block check - includes tripwire, pressure plates, sculk sensors,
     * pointed dripstone, and other hazardous blocks.
     */
    private boolean isDangerousBlock(Material mat) {
        return mat == Material.LAVA || mat == Material.MAGMA_BLOCK || mat == Material.FIRE
                || mat == Material.SOUL_FIRE || mat == Material.CACTUS
                || mat == Material.SWEET_BERRY_BUSH || mat == Material.WITHER_ROSE
                || mat == Material.CAMPFIRE || mat == Material.SOUL_CAMPFIRE
                || mat == Material.POWDER_SNOW || mat == Material.POINTED_DRIPSTONE
                || mat == Material.TRIPWIRE || mat == Material.TRIPWIRE_HOOK
                || mat == Material.STRING
                || mat == Material.SCULK_SENSOR || mat == Material.SCULK_SHRIEKER
                || mat.name().contains("PRESSURE_PLATE");
    }

    public Location getSafeLocation(Location loc) {
        Location safe = loc.clone();
        World world = safe.getWorld();
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
        for (int y = startY; y <= world.getMaxHeight() - 2; y++) {
            Location check = new Location(world, safe.getX(), y, safe.getZ());
            Material below = new Location(world, safe.getX(), y - 1, safe.getZ()).getBlock().getType();
            Material at = check.getBlock().getType();
            Material above = new Location(world, safe.getX(), y + 1, safe.getZ()).getBlock().getType();
            if (isSolidSafe(below) && isPassable(at) && isPassable(above)) {
                return new Location(world, safe.getX(), y, safe.getZ(), safe.getYaw(), safe.getPitch());
            }
        }
        int highY = world.getHighestBlockYAt(safe.getBlockX(), safe.getBlockZ()) + 1;
        return new Location(world, safe.getX(), highY, safe.getZ(), safe.getYaw(), safe.getPitch());
    }

    private boolean isSolidSafe(Material mat) {
        if (!mat.isSolid()) return false;
        return !isDangerousBlock(mat);
    }

    private boolean isPassable(Material mat) {
        return !mat.isSolid() && mat != Material.LAVA && mat != Material.FIRE && mat != Material.SOUL_FIRE
                && mat != Material.WATER && mat != Material.POWDER_SNOW;
    }

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

    // --- Pending unsafe override confirmations ---
    private final Map<UUID, PendingUnsafeTp> pendingUnsafeTps = new ConcurrentHashMap<>();

    public static class PendingUnsafeTp {
        public final Location destination;
        public final String delayBypassPerm;
        public final String featureKey;
        public final long timestamp;

        public PendingUnsafeTp(Location destination, String delayBypassPerm, String featureKey) {
            this.destination = destination;
            this.delayBypassPerm = delayBypassPerm;
            this.featureKey = featureKey;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Confirms an unsafe teleport after the player clicked "TP Anyway".
     * Performs the teleport with delay (if applicable), skipping safety checks.
     */
    public void confirmUnsafeTeleport(Player player) {
        PendingUnsafeTp pending = pendingUnsafeTps.remove(player.getUniqueId());
        if (pending == null) {
            player.sendMessage(CC.error("No pending unsafe teleport to confirm."));
            return;
        }
        // Expire after 30 seconds
        if (System.currentTimeMillis() - pending.timestamp > 30_000) {
            player.sendMessage(CC.error("The unsafe teleport confirmation has expired."));
            return;
        }

        setBackLocation(player.getUniqueId(), player.getLocation());
        double cooldown = roundToHalf(plugin.getCooldownManager().getCooldownSeconds(pending.featureKey));
        if (cooldown <= 0 || player.hasPermission(pending.delayBypassPerm)) {
            player.teleportAsync(pending.destination);
            player.sendMessage(CC.success("Teleported! <red>(unsafe destination)"));
            return;
        }

        long delayTicks = (long) (cooldown * 20);
        Location startLoc = player.getLocation().clone();
        teleportStartLocations.put(player.getUniqueId(), startLoc);
        String delayStr = cooldown == (int) cooldown ? String.valueOf((int) cooldown) : String.valueOf(cooldown);
        player.sendMessage(CC.info("Teleporting in <yellow>" + delayStr + "</yellow> seconds. <gray>Don't move or take damage!"));

        final Location dest = pending.destination;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(player.getUniqueId());
            teleportStartLocations.remove(player.getUniqueId());
            if (!player.isOnline()) return;
            setBackLocation(player.getUniqueId(), player.getLocation());
            player.teleportAsync(dest);
            player.sendMessage(CC.success("Teleported! <red>(unsafe destination)"));
        }, delayTicks);
        pendingTeleports.put(player.getUniqueId(), task);
    }

    // --- Teleport with delay (non-TPA: /warp, /home, /spawn, /back) ---

    /**
     * Teleport with safety check, delay, and bypass permissions.
     * When the destination is unsafe:
     *   - If the player has the safety bypass permission, a clickable "TP Anyway" confirmation
     *     is shown instead of teleporting immediately.
     *   - If not, teleportation is simply cancelled.
     *
     * @param player            The player to teleport
     * @param location          Destination location
     * @param delayBypassPerm   Permission to bypass warmup delay
     * @param featureKey        Feature key for config lookup (e.g., "tpa", "spawn", "warp")
     * @param safetyBypassPerm  Permission to bypass safety check (null to skip safety)
     * @return true if teleport was initiated, false if blocked by safety
     */
    public boolean teleportWithSafety(Player player, Location location, String delayBypassPerm,
                                       String featureKey, String safetyBypassPerm) {
        boolean safetyEnabled = plugin.getConfig().getBoolean("teleport.safe-teleport-" + featureKey, true);

        if (safetyEnabled && !isLocationSafe(location)) {
            if (safetyBypassPerm != null && player.hasPermission(safetyBypassPerm)) {
                // Store pending confirmation - do NOT teleport yet
                pendingUnsafeTps.put(player.getUniqueId(), new PendingUnsafeTp(location, delayBypassPerm, featureKey));
                sendUnsafeWarningWithConfirm(player);
            } else {
                player.sendMessage(CC.error("Teleportation cancelled - the destination is unsafe!"));
            }
            return false;
        }

        setBackLocation(player.getUniqueId(), player.getLocation());
        double cooldown = roundToHalf(plugin.getCooldownManager().getCooldownSeconds(featureKey));
        if (cooldown <= 0 || player.hasPermission(delayBypassPerm)) {
            Location finalLoc = getSafeLocation(location);
            player.teleportAsync(finalLoc);
            player.sendMessage(CC.success("Teleported!"));
            return true;
        }

        int countdownSecs = Math.max(1, (int) Math.ceil(cooldown));
        player.sendMessage(CC.info("Don't move or take damage!"));
        final boolean finalSafetyEnabled = safetyEnabled;
        startCountdown(player, countdownSecs, () -> {
            if (!player.isOnline()) return;

            // Re-check safety before actually teleporting
            if (finalSafetyEnabled && !isLocationSafe(location)) {
                if (safetyBypassPerm != null && player.hasPermission(safetyBypassPerm)) {
                    pendingUnsafeTps.put(player.getUniqueId(), new PendingUnsafeTp(location, delayBypassPerm, featureKey));
                    sendUnsafeWarningWithConfirm(player);
                } else {
                    player.sendMessage(CC.error("Teleportation cancelled - the destination became unsafe!"));
                }
                return;
            }

            Location finalLoc = getSafeLocation(location);
            setBackLocation(player.getUniqueId(), player.getLocation());
            player.teleportAsync(finalLoc);
            player.sendMessage(CC.success("Teleported!"));
        });
        return true;
    }

    /**
     * Teleport with bypass permission - uses specific bypass permission for cooldown.
     * No safety check (legacy method for backward compatibility).
     */
    public void teleportWithBypass(Player player, Location location, String bypassPermission) {
        teleportWithBypass(player, location, bypassPermission, "tpa");
    }

    /**
     * Teleport with bypass permission and feature key for per-command cooldown lookup.
     */
    public void teleportWithBypass(Player player, Location location, String bypassPermission, String featureKey) {
        setBackLocation(player.getUniqueId(), player.getLocation());
        double cooldown = roundToHalf(plugin.getCooldownManager().getCooldownSeconds(featureKey));
        if (cooldown <= 0 || player.hasPermission(bypassPermission)) {
            Location safeLoc = getSafeLocation(location);
            player.teleportAsync(safeLoc);
            player.sendMessage(CC.success("Teleported!"));
            return;
        }
        int countdownSecs = Math.max(1, (int) Math.ceil(cooldown));
        player.sendMessage(CC.info("Don't move or take damage!"));
        startCountdown(player, countdownSecs, () -> {
            if (!player.isOnline()) return;
            Location safeLoc = getSafeLocation(location);
            setBackLocation(player.getUniqueId(), player.getLocation());
            player.teleportAsync(safeLoc);
            player.sendMessage(CC.success("Teleported!"));
        });
    }

    /**
     * Legacy teleport method - uses justplugin.teleport.bypass as the bypass permission.
     */
    public void teleport(Player player, Location location) {
        teleportWithBypass(player, location, "justplugin.teleport.bypass");
    }

    // --- Wild/Random TP ---
    public Location getRandomLocation(World world) {
        Random random = new Random();
        int range = plugin.getConfig().getInt("teleport.wild-range", 5000);
        int minRange = plugin.getConfig().getInt("teleport.wild-min-range", 500);
        int attempts = 0;
        while (attempts < 50) {
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
            Block groundBlock = world.getBlockAt(x, y, z);
            Material groundMat = groundBlock.getType();

            // Validate ground block is appropriate for this world type
            if (!isValidRtpGround(world, groundMat, x, y, z)) {
                attempts++;
                continue;
            }

            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (!loc.getBlock().getType().isAir()) {
                attempts++;
                continue;
            }
            // Also check head space
            if (!world.getBlockAt(x, y + 2, z).getType().isAir()) {
                attempts++;
                continue;
            }
            return loc;
        }
        // Fallback: keep trying with less strict attempts
        int x = random.nextInt(range * 2) - range;
        int z = random.nextInt(range * 2) - range;
        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    /**
     * Checks if the ground block is valid for RTP based on the world environment.
     * <ul>
     *   <li>Overworld: grass_block, podzol, mycelium, sand (non-floating only), dirt variants</li>
     *   <li>Nether: netherrack, soul_sand, soul_soil</li>
     *   <li>End: end_stone only</li>
     * </ul>
     * All worlds reject ice types, chorus blocks, end rods, water, lava, magma.
     */
    private boolean isValidRtpGround(World world, Material mat, int x, int y, int z) {
        // Universal rejects: ice types, chorus, end rods, water, lava, magma
        if (isRtpBannedBlock(mat)) return false;

        World.Environment env = world.getEnvironment();
        return switch (env) {
            case NORMAL -> {
                // Only allow grass-type blocks, podzol, mycelium, sand (non-floating), dirt variants
                if (mat == Material.GRASS_BLOCK || mat == Material.PODZOL || mat == Material.MYCELIUM
                        || mat == Material.DIRT || mat == Material.COARSE_DIRT || mat == Material.ROOTED_DIRT
                        || mat == Material.DIRT_PATH || mat == Material.MUD || mat == Material.MUDDY_MANGROVE_ROOTS
                        || mat == Material.MOSS_BLOCK) {
                    yield true;
                }
                // Sand is allowed only if not floating (block below must also be solid)
                if (mat == Material.SAND || mat == Material.RED_SAND) {
                    Material below = world.getBlockAt(x, y - 1, z).getType();
                    yield below.isSolid() && below != Material.SAND && below != Material.RED_SAND;
                }
                yield false;
            }
            case NETHER -> mat == Material.NETHERRACK || mat == Material.SOUL_SAND || mat == Material.SOUL_SOIL;
            case THE_END -> mat == Material.END_STONE;
            default -> mat.isSolid() && !isRtpBannedBlock(mat);
        };
    }

    /**
     * Blocks that should never be valid RTP ground in any world.
     */
    private boolean isRtpBannedBlock(Material mat) {
        return mat == Material.WATER || mat == Material.LAVA
                || mat == Material.MAGMA_BLOCK
                || mat == Material.ICE || mat == Material.PACKED_ICE || mat == Material.BLUE_ICE
                || mat == Material.FROSTED_ICE
                || mat == Material.CHORUS_PLANT || mat == Material.CHORUS_FLOWER
                || mat == Material.END_ROD
                || mat == Material.FIRE || mat == Material.SOUL_FIRE
                || mat == Material.CACTUS || mat == Material.SWEET_BERRY_BUSH
                || mat == Material.POWDER_SNOW;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }
}

