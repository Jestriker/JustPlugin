package org.justme.justPlugin.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player is unjailed.
 * <p>
 * This event is <b>not cancellable</b> — it is informational only.
 * It is fired after the unjail has been committed.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onUnjail(PlayerUnjailEvent e) {
 *       getLogger().info(e.getTargetName() + " was unjailed by " + e.getStaff());
 *   }
 * </pre>
 */
public class PlayerUnjailEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID targetUUID;
    private final String targetName;
    private final String staffName;

    /**
     * Constructs a new {@code PlayerUnjailEvent}.
     *
     * @param targetUUID the UUID of the player being unjailed
     * @param targetName the name of the player being unjailed
     * @param staffName  the name of the staff member who unjailed the player
     */
    public PlayerUnjailEvent(@NotNull UUID targetUUID, @NotNull String targetName,
                             @NotNull String staffName) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.staffName = staffName;
    }

    /**
     * Returns the UUID of the player who was unjailed.
     *
     * @return target UUID, never {@code null}
     */
    @NotNull
    public UUID getTarget() {
        return targetUUID;
    }

    /**
     * Returns the name of the player who was unjailed.
     *
     * @return target name, never {@code null}
     */
    @NotNull
    public String getTargetName() {
        return targetName;
    }

    /**
     * Returns the name of the staff member who unjailed the player.
     *
     * @return staff name, never {@code null}
     */
    @NotNull
    public String getStaff() {
        return staffName;
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
