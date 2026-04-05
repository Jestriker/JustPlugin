package org.justme.justPlugin.api.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a warp is created.
 * <p>
 * This event is <b>not cancellable</b> — it is informational only.
 * The location returned is a defensive copy.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onWarpCreate(WarpCreateEvent e) {
 *       getLogger().info("Warp '" + e.getWarpName() + "' created by " + e.getCreatedBy());
 *   }
 * </pre>
 */
public class WarpCreateEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String warpName;
    private final Location location;
    private final String createdBy;

    /**
     * Constructs a new {@code WarpCreateEvent}.
     *
     * @param warpName  the name of the warp being created
     * @param location  the location of the warp (a defensive copy is made)
     * @param createdBy the name of the player or console that created the warp
     */
    public WarpCreateEvent(@NotNull String warpName, @NotNull Location location,
                           @NotNull String createdBy) {
        this.warpName = warpName;
        this.location = location.clone();
        this.createdBy = createdBy;
    }

    /**
     * Returns the name of the warp being created.
     *
     * @return warp name, never {@code null}
     */
    @NotNull
    public String getWarpName() {
        return warpName;
    }

    /**
     * Returns a copy of the warp location.
     * The returned location is a defensive copy — modifying it will not
     * affect the actual warp.
     *
     * @return a clone of the warp location, never {@code null}
     */
    @NotNull
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Returns the name of the player or console that created the warp.
     *
     * @return creator name, never {@code null}
     */
    @NotNull
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
