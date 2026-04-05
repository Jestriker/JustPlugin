package org.justme.justPlugin.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a warp is deleted.
 * <p>
 * This event is <b>not cancellable</b> — it is informational only.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onWarpDelete(WarpDeleteEvent e) {
 *       getLogger().info("Warp '" + e.getWarpName() + "' deleted by " + e.getDeletedBy());
 *   }
 * </pre>
 */
public class WarpDeleteEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String warpName;
    private final String deletedBy;

    /**
     * Constructs a new {@code WarpDeleteEvent}.
     *
     * @param warpName  the name of the warp being deleted
     * @param deletedBy the name of the player or console that deleted the warp
     */
    public WarpDeleteEvent(@NotNull String warpName, @NotNull String deletedBy) {
        this.warpName = warpName;
        this.deletedBy = deletedBy;
    }

    /**
     * Returns the name of the warp being deleted.
     *
     * @return warp name, never {@code null}
     */
    @NotNull
    public String getWarpName() {
        return warpName;
    }

    /**
     * Returns the name of the player or console that deleted the warp.
     *
     * @return deleter name, never {@code null}
     */
    @NotNull
    public String getDeletedBy() {
        return deletedBy;
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
