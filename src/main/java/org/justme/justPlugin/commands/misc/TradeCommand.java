package org.justme.justPlugin.commands.misc;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TradeCommand implements TabExecutor, Listener {

    private final JustPlugin plugin;
    private final int requestTimeout;

    // Pending trade requests: target -> sender
    private final Map<UUID, UUID> tradeRequests = new ConcurrentHashMap<>();
    // Timestamps for trade request expiry: target -> timestamp
    private final Map<UUID, Long> tradeRequestTimes = new ConcurrentHashMap<>();
    // Active trade sessions
    private final Map<UUID, TradeSession> activeTrades = new ConcurrentHashMap<>();

    public TradeCommand(JustPlugin plugin) {
        this.plugin = plugin;
        this.requestTimeout = plugin.getConfig().getInt("trade.request-timeout", 60);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startExpiryTask();
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long timeout = requestTimeout * 1000L;
            Iterator<Map.Entry<UUID, Long>> it = tradeRequestTimes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                UUID targetUuid = entry.getKey();
                if (now - entry.getValue() > timeout) {
                    it.remove();
                    UUID senderUuid = tradeRequests.remove(targetUuid);
                    if (senderUuid != null) {
                        Player senderP = Bukkit.getPlayer(senderUuid);
                        Player targetP = Bukkit.getPlayer(targetUuid);
                        if (senderP != null) senderP.sendMessage(CC.warning("Your trade request has expired."));
                        if (targetP != null) targetP.sendMessage(CC.warning("The trade request has expired."));
                    }
                }
            }
        }, 20L * 5, 20L * 5);
    }

    private static class TradeSession {
        UUID player1, player2;
        Inventory gui;
        boolean player1Accepted = false;
        boolean player2Accepted = false;

        TradeSession(UUID p1, UUID p2, Inventory gui) {
            this.player1 = p1;
            this.player2 = p2;
            this.gui = gui;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /trade <player>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            UUID requesterUuid = tradeRequests.get(player.getUniqueId());
            if (requesterUuid == null) {
                player.sendMessage(CC.error("You have no pending trade requests."));
                return true;
            }
            Player requester = Bukkit.getPlayer(requesterUuid);
            tradeRequests.remove(player.getUniqueId());
            tradeRequestTimes.remove(player.getUniqueId());
            if (requester == null) {
                player.sendMessage(CC.error("The requesting player is no longer online."));
                return true;
            }
            openTradeGui(requester, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("deny")) {
            UUID requesterUuid = tradeRequests.remove(player.getUniqueId());
            tradeRequestTimes.remove(player.getUniqueId());
            if (requesterUuid == null) {
                player.sendMessage(CC.error("You have no pending trade requests."));
                return true;
            }
            Player requester = Bukkit.getPlayer(requesterUuid);
            player.sendMessage(CC.success("Trade request denied."));
            if (requester != null) {
                requester.sendMessage(CC.error("<yellow>" + player.getName() + "</yellow> denied your trade request."));
            }
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(CC.error("Player not found or not online!"));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.error("You can't trade with yourself!"));
            return true;
        }
        // Check if target is ignoring sender
        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(CC.error("This player is not accepting trade requests from you."));
            return true;
        }
        tradeRequests.put(target.getUniqueId(), player.getUniqueId());
        tradeRequestTimes.put(target.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(CC.success("Trade request sent to <yellow>" + target.getName() + "</yellow>. <gray>Expires in " + requestTimeout + "s."));
        target.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> wants to trade with you!"));
        target.sendMessage(CC.info("Type <green>/trade accept</green> or <red>/trade deny</red>. <gray>Expires in " + requestTimeout + "s."));
        return true;
    }

    // Cancel trade requests and active trades when a player logs off
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String name = event.getPlayer().getName();

        // Cancel any pending trade request sent BY this player
        Iterator<Map.Entry<UUID, UUID>> it = tradeRequests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            if (entry.getValue().equals(uuid)) {
                UUID targetUuid = entry.getKey();
                it.remove();
                tradeRequestTimes.remove(targetUuid);
                Player targetP = Bukkit.getPlayer(targetUuid);
                if (targetP != null) {
                    targetP.sendMessage(CC.warning("Trade request from <yellow>" + name + "</yellow> was cancelled — they logged off."));
                }
            }
        }

        // Cancel any pending trade request sent TO this player
        UUID senderUuid = tradeRequests.remove(uuid);
        tradeRequestTimes.remove(uuid);
        if (senderUuid != null) {
            Player senderP = Bukkit.getPlayer(senderUuid);
            if (senderP != null) {
                senderP.sendMessage(CC.warning("Your trade request was cancelled — <yellow>" + name + "</yellow> logged off."));
            }
        }

        // Cancel any active trade session
        TradeSession session = activeTrades.get(uuid);
        if (session != null) {
            returnItems(session);
        }
    }

    private void openTradeGui(Player p1, Player p2) {
        Inventory gui = Bukkit.createInventory(null, 54, CC.translate("<gold><bold>Trade</bold></gold>"));

        // Fill separator column (column 4, index 4,13,22,31,40,49)
        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        sepMeta.displayName(Component.empty());
        separator.setItemMeta(sepMeta);
        for (int row = 0; row < 6; row++) {
            gui.setItem(row * 9 + 4, separator);
        }

        // Accept buttons (slot 45 for p1, slot 53 for p2)
        ItemStack acceptButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta accMeta = acceptButton.getItemMeta();
        accMeta.displayName(CC.translate("<green><bold>ACCEPT TRADE</bold></green>"));
        acceptButton.setItemMeta(accMeta);
        gui.setItem(45, acceptButton);
        gui.setItem(53, acceptButton);

        // Player name indicators
        ItemStack p1Head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta p1Meta = p1Head.getItemMeta();
        p1Meta.displayName(CC.translate("<aqua>" + p1.getName() + "'s Items"));
        p1Head.setItemMeta(p1Meta);
        gui.setItem(49 - 4, p1Head);

        TradeSession session = new TradeSession(p1.getUniqueId(), p2.getUniqueId(), gui);
        activeTrades.put(p1.getUniqueId(), session);
        activeTrades.put(p2.getUniqueId(), session);

        p1.openInventory(gui);
        p2.openInventory(gui);
        p1.sendMessage(CC.info("Place items on your side (left). Click <green>ACCEPT</green> when ready."));
        p2.sendMessage(CC.info("Place items on your side (right). Click <green>ACCEPT</green> when ready."));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        TradeSession session = activeTrades.get(player.getUniqueId());
        if (session == null || !event.getInventory().equals(session.gui)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        int col = slot % 9;

        // Separator column - can't touch
        if (col == 4) {
            event.setCancelled(true);
            return;
        }

        // Determine which side this player is on
        boolean isPlayer1 = player.getUniqueId().equals(session.player1);

        // Player1 uses left side (cols 0-3), Player2 uses right side (cols 5-8)
        if (isPlayer1 && col > 4) {
            event.setCancelled(true);
            return;
        }
        if (!isPlayer1 && col < 4) {
            event.setCancelled(true);
            return;
        }

        // Accept button clicks
        if (slot == 45 || slot == 53) {
            event.setCancelled(true);
            if (isPlayer1) {
                session.player1Accepted = true;
                player.sendMessage(CC.success("You accepted the trade!"));
            } else {
                session.player2Accepted = true;
                player.sendMessage(CC.success("You accepted the trade!"));
            }
            if (session.player1Accepted && session.player2Accepted) {
                completeTrade(session);
            }
            return;
        }
    }

    private void completeTrade(TradeSession session) {
        Player p1 = Bukkit.getPlayer(session.player1);
        Player p2 = Bukkit.getPlayer(session.player2);
        if (p1 == null || p2 == null) return;

        List<ItemStack> p1Items = new ArrayList<>();
        List<ItemStack> p2Items = new ArrayList<>();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                ItemStack item = session.gui.getItem(row * 9 + col);
                if (item != null && item.getType() != Material.AIR) {
                    p1Items.add(item.clone());
                }
            }
            for (int col = 5; col < 9; col++) {
                int slot = row * 9 + col;
                if (slot == 53) continue;
                ItemStack item = session.gui.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    p2Items.add(item.clone());
                }
            }
        }

        session.gui.clear();

        for (ItemStack item : p1Items) {
            p2.getInventory().addItem(item);
        }
        for (ItemStack item : p2Items) {
            p1.getInventory().addItem(item);
        }

        p1.closeInventory();
        p2.closeInventory();
        activeTrades.remove(session.player1);
        activeTrades.remove(session.player2);

        p1.sendMessage(CC.success("Trade completed!"));
        p2.sendMessage(CC.success("Trade completed!"));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        TradeSession session = activeTrades.get(player.getUniqueId());
        if (session == null || !event.getInventory().equals(session.gui)) return;

        if (!session.player1Accepted || !session.player2Accepted) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (activeTrades.containsKey(session.player1) || activeTrades.containsKey(session.player2)) {
                    returnItems(session);
                }
            }, 1L);
        }
    }

    private void returnItems(TradeSession session) {
        Player p1 = Bukkit.getPlayer(session.player1);
        Player p2 = Bukkit.getPlayer(session.player2);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                ItemStack item = session.gui.getItem(row * 9 + col);
                if (item != null && item.getType() != Material.AIR && p1 != null) {
                    p1.getInventory().addItem(item.clone());
                }
            }
            for (int col = 5; col < 9; col++) {
                int slot = row * 9 + col;
                if (slot == 53) continue;
                ItemStack item = session.gui.getItem(slot);
                if (item != null && item.getType() != Material.AIR && p2 != null) {
                    p2.getInventory().addItem(item.clone());
                }
            }
        }

        session.gui.clear();
        activeTrades.remove(session.player1);
        activeTrades.remove(session.player2);

        if (p1 != null) {
            p1.closeInventory();
            p1.sendMessage(CC.warning("Trade cancelled. Items returned."));
        }
        if (p2 != null) {
            p2.closeInventory();
            p2.sendMessage(CC.warning("Trade cancelled. Items returned."));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            suggestions.addAll(List.of("accept", "deny"));
            return suggestions;
        }
        return List.of();
    }
}

