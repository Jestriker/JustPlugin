package org.justme.justPlugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a TPA or TPAHere request is sent.
 * <p>
 * Cancelling this event will prevent the teleport request from being delivered
 * to the target player.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onTpaRequest(PlayerTeleportRequestEvent e) {
 *       if (isInCombat(e.getSender())) {
 *           e.setCancelled(true);
 *       }
 *   }
 * </pre>
 */
public class PlayerTeleportRequestEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The type of teleport request.
     */
    public enum TeleportType {
        /** Sender requests to teleport to the target. */
        TPA,
        /** Sender requests the target to teleport to them. */
        TPAHERE
    }

    private final UUID senderUUID;
    private final UUID targetUUID;
    private final TeleportType type;
    private boolean cancelled;

    /**
     * Constructs a new {@code PlayerTeleportRequestEvent}.
     *
     * @param senderUUID the UUID of the player sending the request
     * @param targetUUID the UUID of the player receiving the request
     * @param type       the type of teleport request
     */
    public PlayerTeleportRequestEvent(@NotNull UUID senderUUID, @NotNull UUID targetUUID,
                                      @NotNull TeleportType type) {
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.type = type;
    }

    /**
     * Returns the UUID of the player who sent the teleport request.
     *
     * @return sender UUID, never {@code null}
     */
    @NotNull
    public UUID getSender() {
        return senderUUID;
    }

    /**
     * Returns the UUID of the player who is the target of the request.
     *
     * @return target UUID, never {@code null}
     */
    @NotNull
    public UUID getTarget() {
        return targetUUID;
    }

    /**
     * Returns the type of teleport request.
     *
     * @return the teleport type, never {@code null}
     */
    @NotNull
    public TeleportType getType() {
        return type;
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
