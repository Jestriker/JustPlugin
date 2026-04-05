package org.justme.justPlugin.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player's AFK status changes.
 * <p>
 * This event is <b>not cancellable</b> — it is informational only.
 * Use {@link #isAfk()} to determine whether the player is going AFK
 * or returning from AFK.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onAfkChange(PlayerAfkEvent e) {
 *       if (e.isAfk()) {
 *           getLogger().info(e.getPlayer() + " went AFK");
 *       }
 *   }
 * </pre>
 */
public class PlayerAfkEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID playerUUID;
    private final boolean afk;

    /**
     * Constructs a new {@code PlayerAfkEvent}.
     *
     * @param playerUUID the UUID of the player whose AFK status changed
     * @param afk        {@code true} if the player is going AFK,
     *                   {@code false} if returning from AFK
     */
    public PlayerAfkEvent(@NotNull UUID playerUUID, boolean afk) {
        this.playerUUID = playerUUID;
        this.afk = afk;
    }

    /**
     * Returns the UUID of the player whose AFK status changed.
     *
     * @return player UUID, never {@code null}
     */
    @NotNull
    public UUID getPlayer() {
        return playerUUID;
    }

    /**
     * Returns whether the player is going AFK or returning.
     *
     * @return {@code true} if the player is now AFK, {@code false} if returning
     */
    public boolean isAfk() {
        return afk;
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
