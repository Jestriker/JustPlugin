package org.justme.justPlugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired <b>before</b> a player's balance is changed.
 * <p>
 * This event is cancellable — cancelling it will prevent the balance change
 * from taking effect. You may also modify the new balance via
 * {@link #setNewBalance(double)}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onBalanceChange(PlayerBalanceChangeEvent e) {
 *       if (e.getReason() == PlayerBalanceChangeEvent.Reason.PAY) {
 *           // tax 10%
 *           e.setNewBalance(e.getNewBalance() * 0.9);
 *       }
 *   }
 * </pre>
 */
public class PlayerBalanceChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The reason a balance change is occurring.
     */
    public enum Reason {
        /** Player-to-player payment. */
        PAY,
        /** Administrative cash addition (e.g. /addcash). */
        ADDCASH,
        /** Pay-note redemption. */
        PAYNOTE,
        /** Trade transaction. */
        TRADE,
        /** Changed via the API. */
        API,
        /** Direct set (e.g. /setbalance). */
        SET
    }

    private final UUID playerUUID;
    private final String playerName;
    private final double oldBalance;
    private double newBalance;
    private final Reason reason;
    private boolean cancelled;

    /**
     * Constructs a new {@code PlayerBalanceChangeEvent}.
     *
     * @param playerUUID the UUID of the affected player
     * @param playerName the name of the affected player
     * @param oldBalance the player's current balance before the change
     * @param newBalance the proposed new balance
     * @param reason     the reason for the balance change
     */
    public PlayerBalanceChangeEvent(@NotNull UUID playerUUID, @NotNull String playerName,
                                    double oldBalance, double newBalance, @NotNull Reason reason) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.reason = reason;
    }

    /**
     * Returns the UUID of the player whose balance is changing.
     *
     * @return player UUID, never {@code null}
     */
    @NotNull
    public UUID getPlayer() {
        return playerUUID;
    }

    /**
     * Returns the name of the player whose balance is changing.
     *
     * @return player name, never {@code null}
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the player's balance before the change.
     *
     * @return the old balance
     */
    public double getOldBalance() {
        return oldBalance;
    }

    /**
     * Returns the proposed new balance.
     *
     * @return the new balance (may have been modified by other listeners)
     */
    public double getNewBalance() {
        return newBalance;
    }

    /**
     * Sets the new balance that will be applied.
     * <p>
     * Values are clamped to {@code [0, Double.MAX_VALUE]} to prevent
     * negative or overflowed balances. The main plugin may apply
     * additional config-based clamping when processing the result.
     * </p>
     *
     * @param newBalance the desired new balance
     */
    public void setNewBalance(double newBalance) {
        this.newBalance = Math.max(0, newBalance);
    }

    /**
     * Returns the reason for this balance change.
     *
     * @return the reason, never {@code null}
     */
    @NotNull
    public Reason getReason() {
        return reason;
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
