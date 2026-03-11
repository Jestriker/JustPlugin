package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;

public class ShareitemCommand implements CommandExecutor, TabCompleter {

    public ShareitemCommand(JustPlugin plugin) {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage("§cYou are not holding anything.");
            return true;
        }
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name().replace("_", " ").toLowerCase();
        String msg = "§e" + player.getName() + " §ais showing: §f[" + itemName + " x" + item.getAmount() + "]";
        for (Player p : player.getServer().getOnlinePlayers()) {
            p.sendMessage(msg);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
