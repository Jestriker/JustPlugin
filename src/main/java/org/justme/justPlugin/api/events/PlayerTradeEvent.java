package org.justme.justPlugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Fired <b>before</b> a trade between two players completes.
 * <p>
 * Cancelling this event will prevent the trade from going through.
 * Item lists are unmodifiable defensive copies — they cannot be used
 * to manipulate internal plugin state.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onTrade(PlayerTradeEvent e) {
 *       if (e.getMoneyAmount() &gt; 100000) {
 *           getLogger().info("Large trade detected!");
 *       }
 *   }
 * </pre>
 */
public class PlayerTradeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID player1UUID;
    private final UUID player2UUID;
    private double moneyAmount;
    private final List<ItemStack> items1;
    private final List<ItemStack> items2;
    private boolean cancelled;

    /**
     * Constructs a new {@code PlayerTradeEvent}.
     *
     * @param player1UUID the UUID of the first player
     * @param player2UUID the UUID of the second player
     * @param moneyAmount the amount of money being traded
     * @param items1      items offered by player 1 (a defensive copy is made)
     * @param items2      items offered by player 2 (a defensive copy is made)
     */
    public PlayerTradeEvent(@NotNull UUID player1UUID, @NotNull UUID player2UUID,
                            double moneyAmount,
                            @NotNull List<ItemStack> items1, @NotNull List<ItemStack> items2) {
        this.player1UUID = player1UUID;
        this.player2UUID = player2UUID;
        this.moneyAmount = moneyAmount;
        this.items1 = Collections.unmodifiableList(List.copyOf(items1));
        this.items2 = Collections.unmodifiableList(List.copyOf(items2));
    }

    /**
     * Returns the UUID of the first player in the trade.
     *
     * @return player 1 UUID, never {@code null}
     */
    @NotNull
    public UUID getPlayer1() {
        return player1UUID;
    }

    /**
     * Returns the UUID of the second player in the trade.
     *
     * @return player 2 UUID, never {@code null}
     */
    @NotNull
    public UUID getPlayer2() {
        return player2UUID;
    }

    /**
     * Returns the amount of money involved in the trade.
     *
     * @return the money amount
     */
    public double getMoneyAmount() {
        return moneyAmount;
    }

    /**
     * Sets the amount of money involved in the trade.
     * Values are clamped to a minimum of {@code 0}.
     *
     * @param moneyAmount the new money amount
     */
    public void setMoneyAmount(double moneyAmount) {
        this.moneyAmount = Math.max(0, moneyAmount);
    }

    /**
     * Returns an unmodifiable list of items offered by player 1.
     * This is a defensive copy — modifying individual ItemStack objects
     * will not affect the actual trade.
     *
     * @return unmodifiable list of player 1's items, never {@code null}
     */
    @NotNull
    public List<ItemStack> getItems1() {
        return items1;
    }

    /**
     * Returns an unmodifiable list of items offered by player 2.
     * This is a defensive copy — modifying individual ItemStack objects
     * will not affect the actual trade.
     *
     * @return unmodifiable list of player 2's items, never {@code null}
     */
    @NotNull
    public List<ItemStack> getItems2() {
        return items2;
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
