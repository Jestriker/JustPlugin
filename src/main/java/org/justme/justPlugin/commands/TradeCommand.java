package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.justme.justPlugin.JustPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class TradeCommand implements CommandExecutor, TabCompleter {

    // Trade sessions: key=initiator UUID, value=target UUID
    private final Map<UUID, UUID> pendingTrades = new HashMap<>();
    // Trade inventories: key=session key (sorted UUIDs), value=[initiatorInv, targetInv, bothConfirmed]
    private final Map<String, Object[]> tradeData = new HashMap<>();

    public TradeCommand(JustPlugin plugin) {}

    private String sessionKey(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a + ":" + b : b + ":" + a;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§cUsage: /trade <player>  OR /trade confirm  OR /trade cancel");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "confirm" -> {
                UUID partnerId = pendingTrades.get(player.getUniqueId());
                if (partnerId == null) {
                    // Check if we are target
                    for (Map.Entry<UUID, UUID> e : pendingTrades.entrySet()) {
                        if (e.getValue().equals(player.getUniqueId())) {
                            partnerId = e.getKey();
                            break;
                        }
                    }
                }
                if (partnerId == null) { player.sendMessage("§cYou have no active trade."); return true; }
                String key = sessionKey(player.getUniqueId(), partnerId);
                Object[] data = tradeData.get(key);
                if (data == null) { player.sendMessage("§cNo trade session found."); return true; }
                Set<UUID> confirmed = (Set<UUID>) data[2];
                confirmed.add(player.getUniqueId());
                Player partner = Bukkit.getPlayer(partnerId);
                player.sendMessage("§aTrade confirmed. Waiting for partner...");
                if (partner != null) partner.sendMessage("§e" + player.getName() + " §aconfirmed the trade.");
                if (confirmed.size() == 2) {
                    completeTrade(player.getUniqueId(), partnerId, data);
                    pendingTrades.remove(player.getUniqueId());
                    pendingTrades.remove(partnerId);
                    tradeData.remove(key);
                }
            }
            case "cancel" -> {
                UUID partnerId = pendingTrades.remove(player.getUniqueId());
                if (partnerId == null) {
                    for (Iterator<Map.Entry<UUID, UUID>> it = pendingTrades.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<UUID, UUID> e = it.next();
                        if (e.getValue().equals(player.getUniqueId())) { partnerId = e.getKey(); it.remove(); break; }
                    }
                }
                if (partnerId == null) { player.sendMessage("§cYou have no active trade."); return true; }
                String key = sessionKey(player.getUniqueId(), partnerId);
                Object[] data = tradeData.remove(key);
                if (data != null) {
                    returnItems(player.getUniqueId(), (Inventory) data[0]);
                    Player partner = Bukkit.getPlayer(partnerId);
                    if (partner != null) {
                        returnItems(partnerId, (Inventory) data[1]);
                        partner.sendMessage("§cTrade cancelled by §e" + player.getName() + "§c.");
                    }
                }
                player.sendMessage("§cTrade cancelled.");
            }
            default -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) { player.sendMessage("§cPlayer not found."); return true; }
                if (target.equals(player)) { player.sendMessage("§cYou cannot trade with yourself."); return true; }
                if (pendingTrades.containsKey(player.getUniqueId())) { player.sendMessage("§cYou already have an active trade."); return true; }
                Inventory initiatorInv = Bukkit.createInventory(null, 27, "Trade - " + player.getName());
                Inventory targetInv = Bukkit.createInventory(null, 27, "Trade - " + target.getName());
                pendingTrades.put(player.getUniqueId(), target.getUniqueId());
                String key = sessionKey(player.getUniqueId(), target.getUniqueId());
                tradeData.put(key, new Object[]{initiatorInv, targetInv, new HashSet<UUID>()});
                player.openInventory(initiatorInv);
                target.openInventory(targetInv);
                player.sendMessage("§aTrade started with §e" + target.getName() + "§a. Put items in inventory, then /trade confirm");
                target.sendMessage("§e" + player.getName() + " §awants to trade. Put items in inventory, then /trade confirm. Cancel with /trade cancel");
            }
        }
        return true;
    }

    private void completeTrade(UUID a, UUID b, Object[] data) {
        Inventory aInv = (Inventory) data[0];
        Inventory bInv = (Inventory) data[1];
        Player pA = Bukkit.getPlayer(a);
        Player pB = Bukkit.getPlayer(b);
        if (pA != null) {
            for (ItemStack item : bInv.getContents()) {
                if (item != null && !item.getType().isAir()) {
                    java.util.HashMap<Integer, ItemStack> leftover = pA.getInventory().addItem(item);
                    leftover.values().forEach(i -> pA.getWorld().dropItemNaturally(pA.getLocation(), i));
                }
            }
            pA.sendMessage("§aTrade complete!");
        }
        if (pB != null) {
            for (ItemStack item : aInv.getContents()) {
                if (item != null && !item.getType().isAir()) {
                    java.util.HashMap<Integer, ItemStack> leftover = pB.getInventory().addItem(item);
                    leftover.values().forEach(i -> pB.getWorld().dropItemNaturally(pB.getLocation(), i));
                }
            }
            pB.sendMessage("§aTrade complete!");
        }
    }

    private void returnItems(UUID playerId, Inventory inv) {
        if (inv == null) return;
        Player p = Bukkit.getPlayer(playerId);
        if (p == null) return;
        for (ItemStack item : inv.getContents()) {
            if (item != null && !item.getType().isAir()) p.getInventory().addItem(item);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("confirm", "cancel"));
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            return completions;
        }
        return new ArrayList<>();
    }
}
