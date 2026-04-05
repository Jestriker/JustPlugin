package org.justme.justPlugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired <b>before</b> a player is jailed.
 * <p>
 * Cancelling this event will prevent the player from being jailed.
 * All fields are read-only except cancellation state.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onJail(PlayerJailEvent e) {
 *       getLogger().info(e.getTargetName() + " is being jailed at " + e.getJailName());
 *   }
 * </pre>
 */
public class PlayerJailEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID targetUUID;
    private final String targetName;
    private final String staffName;
    private final String reason;
    private final String jailName;
    private final long duration;
    private boolean cancelled;

    /**
     * Constructs a new {@code PlayerJailEvent}.
     *
     * @param targetUUID the UUID of the player being jailed
     * @param targetName the name of the player being jailed
     * @param staffName  the name of the staff member issuing the jail
     * @param reason     the reason for the jail
     * @param jailName   the name of the jail location
     * @param duration   the duration in milliseconds ({@code 0} for permanent)
     */
    public PlayerJailEvent(@NotNull UUID targetUUID, @NotNull String targetName,
                           @NotNull String staffName, @NotNull String reason,
                           @NotNull String jailName, long duration) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.staffName = staffName;
        this.reason = reason;
        this.jailName = jailName;
        this.duration = duration;
    }

    /**
     * Returns the UUID of the player being jailed.
     *
     * @return target UUID, never {@code null}
     */
    @NotNull
    public UUID getTarget() {
        return targetUUID;
    }

    /**
     * Returns the name of the player being jailed.
     *
     * @return target name, never {@code null}
     */
    @NotNull
    public String getTargetName() {
        return targetName;
    }

    /**
     * Returns the name of the staff member issuing the jail.
     *
     * @return staff name, never {@code null}
     */
    @NotNull
    public String getStaff() {
        return staffName;
    }

    /**
     * Returns the reason for the jail.
     *
     * @return the reason, never {@code null}
     */
    @NotNull
    public String getReason() {
        return reason;
    }

    /**
     * Returns the name of the jail location.
     *
     * @return jail name, never {@code null}
     */
    @NotNull
    public String getJailName() {
        return jailName;
    }

    /**
     * Returns the duration of the jail in milliseconds.
     * A value of {@code 0} indicates a permanent jail.
     *
     * @return the duration in milliseconds, or {@code 0} for permanent
     */
    public long getDuration() {
        return duration;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
