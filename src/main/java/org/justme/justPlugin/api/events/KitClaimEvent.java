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
 * Fired <b>before</b> a player claims a kit.
 * <p>
 * Cancelling this event will prevent the kit from being given to the player.
 * The item list is an unmodifiable defensive copy and cannot be used to
 * manipulate internal plugin state.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   &#64;EventHandler
 *   public void onKitClaim(KitClaimEvent e) {
 *       if (e.getKitName().equals("vip") &amp;&amp; !hasVipRank(e.getPlayer())) {
 *           e.setCancelled(true);
 *       }
 *   }
 * </pre>
 */
public class KitClaimEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID playerUUID;
    private final String kitName;
    private final List<ItemStack> items;
    private boolean cancelled;

    /**
     * Constructs a new {@code KitClaimEvent}.
     *
     * @param playerUUID the UUID of the player claiming the kit
     * @param kitName    the name of the kit being claimed
     * @param items      the items in the kit (a defensive copy is made)
     */
    public KitClaimEvent(@NotNull UUID playerUUID, @NotNull String kitName,
                         @NotNull List<ItemStack> items) {
        this.playerUUID = playerUUID;
        this.kitName = kitName;
        this.items = Collections.unmodifiableList(List.copyOf(items));
    }

    /**
     * Returns the UUID of the player claiming the kit.
     *
     * @return player UUID, never {@code null}
     */
    @NotNull
    public UUID getPlayer() {
        return playerUUID;
    }

    /**
     * Returns the name of the kit being claimed.
     *
     * @return kit name, never {@code null}
     */
    @NotNull
    public String getKitName() {
        return kitName;
    }

    /**
     * Returns an unmodifiable list of items in the kit.
     * This is a defensive copy — modifying individual ItemStack objects
     * will not affect the actual kit contents.
     *
     * @return unmodifiable list of kit items, never {@code null}
     */
    @NotNull
    public List<ItemStack> getItems() {
        return items;
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
