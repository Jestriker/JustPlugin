package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all trade logic: requests, GUI sessions, item syncing, coin trading, and countdown.
 * <p>
 * Hypixel Skyblock-style layout (54 slots, 6 rows × 9 cols):
 * <pre>
 *   Cols: 0  1  2  3 | 4 | 5  6  7  8
 *   Row0: Y  Y  Y  Y | S | O  O  O  O    (item slots)
 *   Row1: Y  Y  Y  Y | S | O  O  O  O
 *   Row2: Y  Y  Y  Y | S | O  O  O  O
 *   Row3: Y  Y  Y  Y | S | O  O  O  O
 *   Row4: Y  Y  Y  Y | S | O  O  O  O    (5 rows × 4 cols = 20 slots each)
 *   Row5: G  R  _  C | S | c  _  _  A    (control row)
 * </pre>
 * Y = your item slots, O = other's mirrored slots (read-only),
 * S = separator (gray glass), G = green wool (confirm), R = red wool (un-confirm),
 * C = coin dye (your coins), c = coin display (other's coins), A = other's acceptance indicator,
 * _ = locked glass pane filler
 */
public class TradeManager implements Listener {

    private final JustPlugin plugin;
    private final int requestTimeout;

    /* ---------- request maps ---------- */
    private final Map<UUID, UUID> pendingRequests  = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> outgoingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> requestTimestamps = new ConcurrentHashMap<>();

    /* ---------- active sessions ---------- */
    private final Map<UUID, TradeSession> activeSessions = new ConcurrentHashMap<>();

    /* ---------- coin-input sign mode ---------- */
    private final Set<UUID> awaitingCoinInput = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Location> signInputLocations = new ConcurrentHashMap<>();

    /* ---------- constants ---------- */
    private static final int SEP_COL = 4;

    // Bottom-row slot indices (row 5)
    private static final int SLOT_CONFIRM   = 45;  // col 0  - green wool
    private static final int SLOT_UNCONFIRM = 46;  // col 1  - red wool
    private static final int SLOT_FILLER_47 = 47;  // col 2  - glass filler
    private static final int SLOT_COIN_YOU  = 48;  // col 3  - gray dye / coin offer
    private static final int SLOT_SEP_BOT   = 49;  // col 4  - separator
    private static final int SLOT_COIN_OTHER= 50;  // col 5  - other's coin display
    private static final int SLOT_FILLER_51 = 51;  // col 6  - glass filler
    private static final int SLOT_FILLER_52 = 52;  // col 7  - glass filler
    private static final int SLOT_ACCEPT_OTHER = 53; // col 8 - other's acceptance indicator

    public TradeManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.requestTimeout = plugin.getConfig().getInt("trade.request-timeout", 60);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startExpiryTask();
    }

    // ====================================================================
    //  Request management
    // ====================================================================

    public void sendRequest(Player sender, Player target) {
        UUID sUuid = sender.getUniqueId();
        UUID tUuid = target.getUniqueId();

        UUID existingTarget = outgoingRequests.get(sUuid);
        if (existingTarget != null) {
            if (existingTarget.equals(tUuid)) {
                sender.sendMessage(CC.error("You already have a pending trade request to <yellow>" + target.getName() + "</yellow>."));
            } else {
                Player existing = Bukkit.getPlayer(existingTarget);
                String name = existing != null ? existing.getName() : "another player";
                boolean clickable = plugin.getConfig().getBoolean("clickable-commands.trade", true);
                String cancelCmd = CC.clickCmd("<yellow>/trade cancel</yellow>", "/trade cancel", clickable);
                sender.sendMessage(CC.error("You already have a pending trade request to <yellow>" + name
                        + "</yellow>. Cancel it first with " + cancelCmd + "."));
            }
            return;
        }
        if (activeSessions.containsKey(sUuid)) {
            sender.sendMessage(CC.error("You are already in an active trade."));
            return;
        }
        if (plugin.getIgnoreManager().isIgnoring(tUuid, sUuid)) {
            sender.sendMessage(CC.error("This player is not accepting trade requests from you."));
            return;
        }
        if (activeSessions.containsKey(tUuid)) {
            sender.sendMessage(CC.error("<yellow>" + target.getName() + "</yellow> is already in a trade."));
            return;
        }

        pendingRequests.put(tUuid, sUuid);
        outgoingRequests.put(sUuid, tUuid);
        requestTimestamps.put(tUuid, System.currentTimeMillis());

        sender.sendMessage(CC.success("Trade request sent to <yellow>" + target.getName() + "</yellow>. <gray>Expires in " + requestTimeout + "s."));
        target.sendMessage(CC.info("<yellow>" + sender.getName() + "</yellow> wants to trade with you!"));
        boolean clickable = plugin.getConfig().getBoolean("clickable-commands.trade", true);
        String acceptCmd = CC.clickCmd("<green>/trade accept</green>", "/trade accept", clickable);
        String denyCmd = CC.clickCmd("<red>/trade deny</red>", "/trade deny", clickable);
        target.sendMessage(CC.info("Type " + acceptCmd + " or " + denyCmd + ". <gray>Expires in " + requestTimeout + "s."));
    }

    public void acceptRequest(Player target) {
        UUID tUuid = target.getUniqueId();
        UUID sUuid = pendingRequests.get(tUuid);
        if (sUuid == null) { target.sendMessage(CC.error("You have no pending trade requests.")); return; }
        Player sender = Bukkit.getPlayer(sUuid);
        clearRequest(tUuid);
        if (sender == null) { target.sendMessage(CC.error("The requesting player is no longer online.")); return; }
        openTradeGUI(sender, target);
    }

    public void denyRequest(Player target) {
        UUID tUuid = target.getUniqueId();
        UUID sUuid = pendingRequests.get(tUuid);
        if (sUuid == null) { target.sendMessage(CC.error("You have no pending trade requests.")); return; }
        Player sender = Bukkit.getPlayer(sUuid);
        clearRequest(tUuid);
        target.sendMessage(CC.success("Trade request denied."));
        if (sender != null) sender.sendMessage(CC.error("<yellow>" + target.getName() + "</yellow> denied your trade request."));
    }

    public void cancelRequest(Player sender) {
        UUID sUuid = sender.getUniqueId();
        UUID tUuid = outgoingRequests.get(sUuid);
        if (tUuid == null) { sender.sendMessage(CC.error("You have no pending outgoing trade request.")); return; }
        clearRequest(tUuid);
        sender.sendMessage(CC.success("Trade request cancelled."));
        Player target = Bukkit.getPlayer(tUuid);
        if (target != null) target.sendMessage(CC.warning("Trade request from <yellow>" + sender.getName() + "</yellow> was cancelled."));
    }

    private void clearRequest(UUID targetUuid) {
        UUID senderUuid = pendingRequests.remove(targetUuid);
        requestTimestamps.remove(targetUuid);
        if (senderUuid != null) outgoingRequests.remove(senderUuid);
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long timeout = requestTimeout * 1000L;
            var it = requestTimestamps.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                UUID targetUuid = entry.getKey();
                if (now - entry.getValue() > timeout) {
                    it.remove();
                    UUID senderUuid = pendingRequests.remove(targetUuid);
                    if (senderUuid != null) {
                        outgoingRequests.remove(senderUuid);
                        Player sP = Bukkit.getPlayer(senderUuid);
                        Player tP = Bukkit.getPlayer(targetUuid);
                        if (sP != null) sP.sendMessage(CC.warning("Your trade request has expired."));
                        if (tP != null) tP.sendMessage(CC.warning("The trade request has expired."));
                    }
                }
            }
        }, 20L * 5, 20L * 5);
    }

    // ====================================================================
    //  Trade GUI - normal (two real players)
    // ====================================================================

    public void openTradeGUI(Player p1, Player p2) {
        Inventory inv1 = Bukkit.createInventory(null, 54, CC.translate("<gold><bold>Trade</bold></gold>"));
        Inventory inv2 = Bukkit.createInventory(null, 54, CC.translate("<gold><bold>Trade</bold></gold>"));

        TradeSession session = new TradeSession(p1.getUniqueId(), p2.getUniqueId(), inv1, inv2);
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);

        setupInventory(inv1, p2.getName());
        setupInventory(inv2, p1.getName());

        p1.openInventory(inv1);
        p2.openInventory(inv2);

        p1.sendMessage(CC.info("Trade opened with <yellow>" + p2.getName() + "</yellow>. Place items on <green>your side</green> (left)."));
        p2.sendMessage(CC.info("Trade opened with <yellow>" + p1.getName() + "</yellow>. Place items on <green>your side</green> (left)."));
    }

    // ====================================================================
    //  Inventory setup
    // ====================================================================

    private void setupInventory(Inventory inv, String otherName) {
        ItemStack separator = glass(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack filler    = glass(Material.BLACK_STAINED_GLASS_PANE, " ");

        // Separator column (col 4, all rows)
        for (int row = 0; row < 6; row++) {
            inv.setItem(row * 9 + SEP_COL, separator.clone());
        }

        // ---- Bottom control row (row 5) ----
        inv.setItem(SLOT_CONFIRM,   wool(Material.LIME_WOOL, "<green><bold>CONFIRM</bold></green>", "<gray>Click to confirm the trade."));
        inv.setItem(SLOT_UNCONFIRM, wool(Material.RED_WOOL,  "<red><bold>CANCEL</bold></red>",     "<gray>Click to un-confirm."));
        inv.setItem(SLOT_FILLER_47, filler.clone());
        inv.setItem(SLOT_COIN_YOU,  coinDye());
        // sep bot already set above
        inv.setItem(SLOT_COIN_OTHER, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(SLOT_FILLER_51, filler.clone());
        inv.setItem(SLOT_FILLER_52, filler.clone());
        // Other's acceptance indicator - starts as red wool
        inv.setItem(SLOT_ACCEPT_OTHER, wool(Material.RED_WOOL, "<red><bold>Not Confirmed</bold></red>",
                "<gray>" + otherName + " has not confirmed."));
    }

    // ====================================================================
    //  Item-creation helpers
    // ====================================================================

    private ItemStack glass(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(CC.translate(name));
        i.setItemMeta(m);
        return i;
    }

    private ItemStack wool(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(CC.translate(name));
        if (lore != null) m.lore(List.of(CC.translate(lore)));
        i.setItemMeta(m);
        return i;
    }

    private ItemStack coinDye() {
        ItemStack i = new ItemStack(Material.GRAY_DYE);
        ItemMeta m = i.getItemMeta();
        m.displayName(CC.translate("<gold><bold>Offer Coins</bold></gold>"));
        m.lore(List.of(
                CC.translate("<gray>Click to type a coin amount."),
                CC.translate("<gray>Supports: <yellow>100</yellow>, <yellow>5k</yellow>, <yellow>1m</yellow>")
        ));
        i.setItemMeta(m);
        return i;
    }

    private ItemStack coinDisplay(long amount) {
        if (amount <= 0) return coinDye();
        Material mat = amount < 1_000 ? Material.GOLD_NUGGET
                     : amount < 1_000_000 ? Material.GOLD_INGOT
                     : Material.GOLD_BLOCK;
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(CC.translate("<gold><bold>" + fmtCoins(amount) + "</bold></gold>"));
        m.lore(List.of(CC.translate("<gray>Coins offered in this trade.")));
        m.addEnchant(Enchantment.UNBREAKING, 1, true);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(m);
        return i;
    }

    private String fmtCoins(long amount) {
        return plugin.getEconomyManager().format(amount);
    }

    // ====================================================================
    //  Slot helpers
    // ====================================================================

    private boolean isYouSlot(int slot) {
        if (slot < 0 || slot >= 54) return false;
        int row = slot / 9, col = slot % 9;
        return row < 5 && col < 4;
    }

    private boolean isOtherSlot(int slot) {
        if (slot < 0 || slot >= 54) return false;
        int row = slot / 9, col = slot % 9;
        return row < 5 && col > 4;
    }

    private int youToOther(int slot) {
        return (slot / 9) * 9 + (slot % 9) + 5;
    }

    // ====================================================================
    //  Syncing (mirror "You" → other's "Other")
    // ====================================================================

    private void syncAfterClick(TradeSession s, UUID clickerUuid) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!activeSessions.containsKey(clickerUuid)) return;
            boolean isP1 = clickerUuid.equals(s.player1);
            Inventory src = isP1 ? s.inv1 : s.inv2;
            Inventory dst = isP1 ? s.inv2 : s.inv1;
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 4; col++) {
                    int ySlot = row * 9 + col;
                    int oSlot = youToOther(ySlot);
                    ItemStack item = src.getItem(ySlot);
                    dst.setItem(oSlot, item != null ? item.clone() : null);
                }
            }
        }, 1L);
    }

    // ====================================================================
    //  Accept / countdown
    // ====================================================================

    private void updateAcceptDisplay(TradeSession s) {
        // Separator: top half shows YOUR acceptance, bottom half shows OTHER's
        Material p1Mat = s.player1Accepted ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
        Material p2Mat = s.player2Accepted ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
        String p1Label = s.player1Accepted ? "<green>✔ Confirmed" : " ";
        String p2Label = s.player2Accepted ? "<green>✔ Confirmed" : " ";

        ItemStack g1 = glass(p1Mat, p1Label);
        ItemStack g2 = glass(p2Mat, p2Label);

        // inv1 view: top 3 sep rows = p1 (you), bottom 2 = p2 (other)
        for (int r = 0; r < 3; r++) s.inv1.setItem(r * 9 + SEP_COL, g1.clone());
        for (int r = 3; r < 5; r++) s.inv1.setItem(r * 9 + SEP_COL, g2.clone());

        // inv2 view: top 3 = p2 (you), bottom 2 = p1 (other)
        for (int r = 0; r < 3; r++) s.inv2.setItem(r * 9 + SEP_COL, g2.clone());
        for (int r = 3; r < 5; r++) s.inv2.setItem(r * 9 + SEP_COL, g1.clone());

        // Row 5 separator always gray
        ItemStack sepBot = glass(Material.GRAY_STAINED_GLASS_PANE, " ");
        s.inv1.setItem(SLOT_SEP_BOT, sepBot.clone());
        s.inv2.setItem(SLOT_SEP_BOT, sepBot.clone());

        // Other-side acceptance indicator (slot 53)
        // inv1: shows p2's acceptance status
        s.inv1.setItem(SLOT_ACCEPT_OTHER, s.player2Accepted
                ? wool(Material.LIME_WOOL, "<green><bold>Confirmed</bold></green>", "<gray>Other player confirmed.")
                : wool(Material.RED_WOOL,  "<red><bold>Not Confirmed</bold></red>", "<gray>Waiting for other player..."));
        // inv2: shows p1's acceptance status
        s.inv2.setItem(SLOT_ACCEPT_OTHER, s.player1Accepted
                ? wool(Material.LIME_WOOL, "<green><bold>Confirmed</bold></green>", "<gray>Other player confirmed.")
                : wool(Material.RED_WOOL,  "<red><bold>Not Confirmed</bold></red>", "<gray>Waiting for other player..."));
    }

    private void checkBothAccepted(TradeSession s) {
        if (s.player1Accepted && s.player2Accepted) startCountdown(s);
    }

    private void startCountdown(TradeSession s) {
        if (s.countdownTask != null) return;
        s.countdownValue = 5;
        s.countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!activeSessions.containsKey(s.player1)) { cancelCountdown(s); return; }
            if (!s.player1Accepted || !s.player2Accepted) { cancelCountdown(s); return; }

            int val = s.countdownValue;
            if (val <= 0) {
                cancelCountdown(s);
                commitTrade(s);
                return;
            }

            ItemStack ci = glass(Material.LIME_STAINED_GLASS_PANE, "<green><bold>" + val);
            ci.setAmount(Math.max(1, val));
            for (int row = 0; row < 6; row++) {
                s.inv1.setItem(row * 9 + SEP_COL, ci.clone());
                s.inv2.setItem(row * 9 + SEP_COL, ci.clone());
            }

            // Play countdown tick sound
            Player cp1 = Bukkit.getPlayer(s.player1);
            if (cp1 != null) playTradeSound(cp1, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, val <= 2 ? 1.5f : 1.0f);
            Player cp2 = Bukkit.getPlayer(s.player2);
            if (cp2 != null) playTradeSound(cp2, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, val <= 2 ? 1.5f : 1.0f);

            s.countdownValue--;
        }, 0L, 20L);
    }

    private void cancelCountdown(TradeSession s) {
        if (s.countdownTask != null) { s.countdownTask.cancel(); s.countdownTask = null; }
        s.countdownValue = 5;
        updateAcceptDisplay(s);
    }

    private void playTradeSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    // ====================================================================
    //  Coin input (via sign editor - Hypixel-style)
    // ====================================================================

    public boolean isAwaitingCoinInput(UUID uuid) { return awaitingCoinInput.contains(uuid); }

    /**
     * Opens a sign editor for the player to type a coin amount.
     * Lines 1, 3, 4 show plugin text; line 2 is free-form input.
     */
    private void openCoinSignEditor(Player player) {
        UUID uuid = player.getUniqueId();
        awaitingCoinInput.add(uuid);
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!activeSessions.containsKey(uuid)) {
                awaitingCoinInput.remove(uuid);
                return;
            }

            // Place a temporary sign at the bottom of the world
            Location signLoc = new Location(player.getWorld(),
                    player.getLocation().getBlockX(),
                    player.getWorld().getMinHeight(),
                    player.getLocation().getBlockZ());

            signInputLocations.put(uuid, signLoc.clone());

            Block block = signLoc.getBlock();
            block.setType(Material.OAK_SIGN, false);

            Sign sign = (Sign) block.getState();
            sign.getSide(Side.FRONT).line(0, Component.text("^^^^^^^^^^^^^^^"));
            sign.getSide(Side.FRONT).line(1, Component.text(""));           // Player types here
            sign.getSide(Side.FRONT).line(2, Component.text("Enter coin"));
            sign.getSide(Side.FRONT).line(3, Component.text("amount (e.g. 5k)"));
            sign.update(true, false);

            player.openSign(sign, Side.FRONT);
        }, 2L);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Location signLoc = signInputLocations.remove(uuid);
        if (signLoc == null) return; // Not our sign

        // Restore original block
        Bukkit.getScheduler().runTaskLater(plugin, () -> signLoc.getBlock().setType(Material.AIR, false), 1L);

        awaitingCoinInput.remove(uuid);

        TradeSession s = activeSessions.get(uuid);
        if (s == null) return;

        // Read line 2 (index 1) - the player's input
        Component lineComp = event.line(1);
        String input = lineComp != null
                ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(lineComp)
                : "";

        if (input.trim().isEmpty()) {
            // No input - treat as cancel, just reopen
            reopen(player, s);
            return;
        }

        processCoinInput(player, s, input.trim());
    }

    private void processCoinInput(Player player, TradeSession s, String input) {
        long amount = parseCoinAmount(input);
        if (amount < 0) {
            player.sendMessage(CC.error("Invalid amount. Use numbers with optional <yellow>k</yellow> or <yellow>m</yellow> suffix."));
            reopen(player, s);
            return;
        }

        boolean isP1 = player.getUniqueId().equals(s.player1);

        if (amount == 0) {
            if (isP1) s.player1Coins = 0; else s.player2Coins = 0;
            updateCoins(s);
            reopen(player, s);
            player.sendMessage(CC.info("Coin offer cleared."));
            playTradeSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.8f);
            return;
        }

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        if (amount > balance) {
            player.sendMessage(CC.error("Not enough balance. You have <yellow>" + fmtCoins((long) balance) + "</yellow>."));
            reopen(player, s);
            return;
        }

        if (isP1) s.player1Coins = amount; else s.player2Coins = amount;
        resetAcceptance(s);
        updateCoins(s);
        reopen(player, s);
        player.sendMessage(CC.success("Offering <yellow>" + fmtCoins(amount) + "</yellow> in this trade."));
        playTradeSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }

    private void reopen(Player player, TradeSession s) {
        boolean isP1 = player.getUniqueId().equals(s.player1);
        Inventory inv = isP1 ? s.inv1 : s.inv2;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeSessions.containsKey(player.getUniqueId())) player.openInventory(inv);
        }, 1L);
    }

    private void updateCoins(TradeSession s) {
        ItemStack p1You   = s.player1Coins > 0 ? coinDisplay(s.player1Coins) : coinDye();
        ItemStack p2You   = s.player2Coins > 0 ? coinDisplay(s.player2Coins) : coinDye();
        ItemStack p1Other = s.player1Coins > 0 ? coinDisplay(s.player1Coins) : glass(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack p2Other = s.player2Coins > 0 ? coinDisplay(s.player2Coins) : glass(Material.GRAY_STAINED_GLASS_PANE, " ");

        s.inv1.setItem(SLOT_COIN_YOU,   p1You);    // p1 sees own coins
        s.inv1.setItem(SLOT_COIN_OTHER, p2Other);   // p1 sees p2's coins
        s.inv2.setItem(SLOT_COIN_YOU,   p2You);    // p2 sees own coins
        s.inv2.setItem(SLOT_COIN_OTHER, p1Other);   // p2 sees p1's coins
    }

    private long parseCoinAmount(String input) {
        if (input.isEmpty()) return -1;
        input = input.toLowerCase().replace(",", "").replace("$", "").replace(" ", "");
        try {
            double mult = 1;
            if (input.endsWith("m")) { mult = 1_000_000; input = input.substring(0, input.length() - 1); }
            else if (input.endsWith("k")) { mult = 1_000; input = input.substring(0, input.length() - 1); }
            if (input.isEmpty()) return -1;
            double val = Double.parseDouble(input) * mult;
            if (val < 0 || val > 999_999_999_999L) return -1;
            return (long) Math.floor(val);
        } catch (NumberFormatException e) { return -1; }
    }

    private void resetAcceptance(TradeSession s) {
        boolean was = s.player1Accepted || s.player2Accepted;
        s.player1Accepted = false;
        s.player2Accepted = false;
        if (s.countdownTask != null) cancelCountdown(s);
        if (was) {
            updateAcceptDisplay(s);
            Player p1 = Bukkit.getPlayer(s.player1);
            if (p1 != null) {
                p1.sendMessage(CC.warning("Trade contents changed - confirmations reset."));
                playTradeSound(p1, Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
            }
            Player p2 = Bukkit.getPlayer(s.player2);
            if (p2 != null) {
                p2.sendMessage(CC.warning("Trade contents changed - confirmations reset."));
                playTradeSound(p2, Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
            }
        }
    }

    // ====================================================================
    //  Trade commit
    // ====================================================================

    private void commitTrade(TradeSession s) {
        Player p1 = Bukkit.getPlayer(s.player1);
        if (p1 == null) { cancelTrade(s, "You disconnected."); return; }

        Player p2 = Bukkit.getPlayer(s.player2);
        if (p2 == null) { cancelTrade(s, "The other player disconnected."); return; }

        // Verify balances
        if (s.player1Coins > 0 && plugin.getEconomyManager().getBalance(p1.getUniqueId()) < s.player1Coins) {
            cancelTrade(s, p1.getName() + " doesn't have enough coins."); return;
        }
        if (s.player2Coins > 0 && plugin.getEconomyManager().getBalance(p2.getUniqueId()) < s.player2Coins) {
            cancelTrade(s, p2.getName() + " doesn't have enough coins."); return;
        }

        List<ItemStack> p1Items = collectYou(s.inv1);
        List<ItemStack> p2Items = collectYou(s.inv2);

        // Transfer coins
        if (s.player1Coins > 0) {
            plugin.getEconomyManager().removeBalance(p1.getUniqueId(), s.player1Coins);
            plugin.getEconomyManager().addBalance(p2.getUniqueId(), s.player1Coins);
        }
        if (s.player2Coins > 0) {
            plugin.getEconomyManager().removeBalance(p2.getUniqueId(), s.player2Coins);
            plugin.getEconomyManager().addBalance(p1.getUniqueId(), s.player2Coins);
        }

        s.committed = true;
        p1.closeInventory();
        p2.closeInventory();

        // Swap items
        for (ItemStack item : p1Items) {
            var over = p2.getInventory().addItem(item);
            for (ItemStack left : over.values()) p2.getWorld().dropItemNaturally(p2.getLocation(), left);
        }
        for (ItemStack item : p2Items) {
            var over = p1.getInventory().addItem(item);
            for (ItemStack left : over.values()) p1.getWorld().dropItemNaturally(p1.getLocation(), left);
        }

        String c1 = s.player1Coins > 0 ? " + <gold>" + fmtCoins(s.player1Coins) + "</gold>" : "";
        String c2 = s.player2Coins > 0 ? " + <gold>" + fmtCoins(s.player2Coins) + "</gold>" : "";

        p1.sendMessage(CC.success("Trade completed with <yellow>" + p2.getName() + "</yellow>!"));
        playTradeSound(p1, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f);
        if (!c1.isEmpty()) p1.sendMessage(CC.line("You sent" + c1));
        if (!c2.isEmpty()) p1.sendMessage(CC.line("You received" + c2));
        p1.sendMessage(CC.line("Items given: <yellow>" + p1Items.size() + "</yellow> | Items received: <yellow>" + p2Items.size()));

        p2.sendMessage(CC.success("Trade completed with <yellow>" + p1.getName() + "</yellow>!"));
        playTradeSound(p2, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f);
        if (!c2.isEmpty()) p2.sendMessage(CC.line("You sent" + c2));
        if (!c1.isEmpty()) p2.sendMessage(CC.line("You received" + c1));
        p2.sendMessage(CC.line("Items given: <yellow>" + p2Items.size() + "</yellow> | Items received: <yellow>" + p1Items.size()));

        activeSessions.remove(s.player1);
        activeSessions.remove(s.player2);
    }

    private List<ItemStack> collectYou(Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (int row = 0; row < 5; row++)
            for (int col = 0; col < 4; col++) {
                ItemStack item = inv.getItem(row * 9 + col);
                if (item != null && item.getType() != Material.AIR) items.add(item.clone());
            }
        return items;
    }

    private void cancelTrade(TradeSession s, String reason) {
        if (s.countdownTask != null) { s.countdownTask.cancel(); s.countdownTask = null; }
        s.committed = true;

        Player p1 = Bukkit.getPlayer(s.player1);
        Player p2 = Bukkit.getPlayer(s.player2);

        returnYou(s.inv1, p1);
        returnYou(s.inv2, p2);

        if (p1 != null) { p1.closeInventory(); p1.sendMessage(CC.warning("Trade cancelled. " + reason + " Items returned.")); }
        if (p2 != null) { p2.closeInventory(); p2.sendMessage(CC.warning("Trade cancelled. " + reason + " Items returned.")); }

        activeSessions.remove(s.player1);
        activeSessions.remove(s.player2);
    }

    private void returnYou(Inventory inv, Player player) {
        if (player == null) return;
        for (int row = 0; row < 5; row++)
            for (int col = 0; col < 4; col++) {
                ItemStack item = inv.getItem(row * 9 + col);
                if (item != null && item.getType() != Material.AIR) {
                    var over = player.getInventory().addItem(item);
                    for (ItemStack left : over.values()) player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
    }

    // ====================================================================
    //  Event handlers
    // ====================================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        TradeSession s = activeSessions.get(player.getUniqueId());
        if (s == null) return;

        boolean isP1 = player.getUniqueId().equals(s.player1);
        Inventory tradeInv = isP1 ? s.inv1 : s.inv2;
        if (!event.getView().getTopInventory().equals(tradeInv)) return;

        int raw = event.getRawSlot();

        // Bottom-half (player inventory) - only allow shift-click into "You" slots
        if (raw >= 54) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == Material.AIR) return;
                for (int row = 0; row < 5; row++)
                    for (int col = 0; col < 4; col++) {
                        int slot = row * 9 + col;
                        ItemStack ex = tradeInv.getItem(slot);
                        if (ex == null || ex.getType() == Material.AIR) {
                            tradeInv.setItem(slot, clicked.clone());
                            event.setCurrentItem(null);
                            resetAcceptance(s);
                            syncAfterClick(s, player.getUniqueId());
                            playTradeSound(player, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                            return;
                        }
                    }
                player.sendMessage(CC.error("No available trade slots."));
            }
            return;
        }

        // Separator column - block
        if (raw % 9 == SEP_COL) { event.setCancelled(true); return; }

        // Other side - block
        if (isOtherSlot(raw)) { event.setCancelled(true); return; }

        // Bottom control row
        if (raw >= 45 && raw <= 53) {
            event.setCancelled(true);

            if (raw == SLOT_CONFIRM) {
                if (isP1) s.player1Accepted = true; else s.player2Accepted = true;
                player.sendMessage(CC.success("You confirmed the trade!"));
                playTradeSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                Player other = Bukkit.getPlayer(isP1 ? s.player2 : s.player1);
                if (other != null) {
                    other.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> confirmed the trade."));
                    playTradeSound(other, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                }
                updateAcceptDisplay(s);
                checkBothAccepted(s);
                return;
            }
            if (raw == SLOT_UNCONFIRM) {
                boolean wasAccepted = isP1 ? s.player1Accepted : s.player2Accepted;
                if (wasAccepted) {
                    if (isP1) s.player1Accepted = false; else s.player2Accepted = false;
                    cancelCountdown(s);
                    updateAcceptDisplay(s);
                    player.sendMessage(CC.warning("You removed your confirmation."));
                    playTradeSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    Player other = Bukkit.getPlayer(isP1 ? s.player2 : s.player1);
                    if (other != null) {
                        other.sendMessage(CC.warning("<yellow>" + player.getName() + "</yellow> removed their confirmation."));
                        playTradeSound(other, Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    }
                }
                return;
            }
            if (raw == SLOT_COIN_YOU) {
                openCoinSignEditor(player);
                return;
            }
            return; // all other bottom slots are locked
        }

        // "You" item slot - allowed, but reset acceptance & sync
        if (isYouSlot(raw)) {
            resetAcceptance(s);
            syncAfterClick(s, player.getUniqueId());
            playTradeSound(player, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        TradeSession s = activeSessions.get(player.getUniqueId());
        if (s == null) return;

        boolean isP1 = player.getUniqueId().equals(s.player1);
        Inventory tradeInv = isP1 ? s.inv1 : s.inv2;
        if (!event.getView().getTopInventory().equals(tradeInv)) return;

        for (int slot : event.getRawSlots()) {
            if (slot < 54 && !isYouSlot(slot)) { event.setCancelled(true); return; }
        }
        resetAcceptance(s);
        syncAfterClick(s, player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        TradeSession s = activeSessions.get(player.getUniqueId());
        if (s == null) return;
        if (awaitingCoinInput.contains(player.getUniqueId())) return;
        if (s.committed) return;

        boolean isP1 = player.getUniqueId().equals(s.player1);
        Inventory tradeInv = isP1 ? s.inv1 : s.inv2;
        if (!event.getInventory().equals(tradeInv)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeSessions.containsKey(player.getUniqueId())) {
                cancelTrade(s, player.getName() + " closed the trade window.");
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String name = event.getPlayer().getName();

        UUID targetUuid = outgoingRequests.get(uuid);
        if (targetUuid != null) {
            clearRequest(targetUuid);
            Player tP = Bukkit.getPlayer(targetUuid);
            if (tP != null) tP.sendMessage(CC.warning("Trade request from <yellow>" + name + "</yellow> was cancelled - they logged off."));
        }
        UUID senderUuid = pendingRequests.get(uuid);
        if (senderUuid != null) {
            clearRequest(uuid);
            Player sP = Bukkit.getPlayer(senderUuid);
            if (sP != null) sP.sendMessage(CC.warning("Your trade request was cancelled - <yellow>" + name + "</yellow> logged off."));
        }

        TradeSession s = activeSessions.get(uuid);
        if (s != null && !s.committed) cancelTrade(s, name + " logged off.");
        awaitingCoinInput.remove(uuid);
        // Clean up any temporary sign block
        Location signLoc = signInputLocations.remove(uuid);
        if (signLoc != null) signLoc.getBlock().setType(Material.AIR, false);
    }

    // ====================================================================
    //  Session model
    // ====================================================================

    private static class TradeSession {
        final UUID player1;
        final UUID player2;
        final Inventory inv1;
        final Inventory inv2;
        boolean player1Accepted = false;
        boolean player2Accepted = false;
        long player1Coins = 0;
        long player2Coins = 0;
        BukkitTask countdownTask = null;
        int countdownValue = 5;
        boolean committed = false;

        TradeSession(UUID p1, UUID p2, Inventory inv1, Inventory inv2) {
            this.player1 = p1;
            this.player2 = p2;
            this.inv1 = inv1;
            this.inv2 = inv2;
        }
    }
}
































