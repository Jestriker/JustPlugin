package org.justme.justPlugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired <b>before</b> a punishment is applied to a player.
 * <p>
 * Cancelling this event will prevent the punishment from being applied.
 * All fields are read-only except cancellation state.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onPunish(PlayerPunishEvent e) {
 *       if (e.getType() == PlayerPunishEvent.PunishmentType.BAN) {
 *           getLogger().info(e.getTargetName() + " is about to be banned!");
 *       }
 *   }
 * </pre>
 */
public class PlayerPunishEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The type of punishment being applied.
     */
    public enum PunishmentType {
        /** Permanent ban. */
        BAN,
        /** Temporary ban. */
        TEMPBAN,
        /** Permanent IP ban. */
        BANIP,
        /** Temporary IP ban. */
        TEMPBANIP,
        /** Permanent mute. */
        MUTE,
        /** Temporary mute. */
        TEMPMUTE,
        /** Warning. */
        WARN,
        /** Kick from the server. */
        KICK,
        /** Jail. */
        JAIL
    }

    private final UUID targetUUID;
    private final String targetName;
    private final String staffName;
    private final String reason;
    private final PunishmentType type;
    private final long duration;
    private boolean cancelled;

    /**
     * Constructs a new {@code PlayerPunishEvent}.
     *
     * @param targetUUID the UUID of the player being punished
     * @param targetName the name of the player being punished
     * @param staffName  the name of the staff member issuing the punishment
     * @param reason     the reason for the punishment
     * @param type       the type of punishment
     * @param duration   the duration in milliseconds ({@code 0} for permanent)
     */
    public PlayerPunishEvent(@NotNull UUID targetUUID, @NotNull String targetName,
                             @NotNull String staffName, @NotNull String reason,
                             @NotNull PunishmentType type, long duration) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.staffName = staffName;
        this.reason = reason;
        this.type = type;
        this.duration = duration;
    }

    /**
     * Returns the UUID of the player being punished.
     *
     * @return target UUID, never {@code null}
     */
    @NotNull
    public UUID getTarget() {
        return targetUUID;
    }

    /**
     * Returns the name of the player being punished.
     *
     * @return target name, never {@code null}
     */
    @NotNull
    public String getTargetName() {
        return targetName;
    }

    /**
     * Returns the name of the staff member issuing the punishment.
     *
     * @return staff name, never {@code null}
     */
    @NotNull
    public String getStaff() {
        return staffName;
    }

    /**
     * Returns the reason for the punishment.
     *
     * @return the reason, never {@code null}
     */
    @NotNull
    public String getReason() {
        return reason;
    }

    /**
     * Returns the type of punishment.
     *
     * @return the punishment type, never {@code null}
     */
    @NotNull
    public PunishmentType getType() {
        return type;
    }

    /**
     * Returns the duration of the punishment in milliseconds.
     * A value of {@code 0} indicates a permanent punishment.
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
