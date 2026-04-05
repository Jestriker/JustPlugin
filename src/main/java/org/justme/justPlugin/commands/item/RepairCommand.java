package org.justme.justPlugin.commands.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class RepairCommand implements TabExecutor {

    private final JustPlugin plugin;

    public RepairCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        Player target;
        if (args.length >= 1) {
            if (!player.hasPermission("justplugin.repair.others")) {
                player.sendMessage(CC.error("You don't have permission to repair other players' items."));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.error("Player not found."));
                return true;
            }
        } else {
            target = player;
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            if (target.equals(player)) {
                player.sendMessage(CC.error("You must be holding an item to repair."));
            } else {
                player.sendMessage(CC.error(target.getName() + " is not holding an item."));
            }
            return true;
        }

        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            player.sendMessage(CC.error("This item cannot be repaired."));
            return true;
        }

        if (damageable.getDamage() == 0) {
            player.sendMessage(CC.error("This item is already fully repaired."));
            return true;
        }

        damageable.setDamage(0);
        item.setItemMeta(damageable);

        String itemName = item.getType().name().toLowerCase().replace("_", " ");
        if (target.equals(player)) {
            player.sendMessage(CC.success("Repaired <yellow>" + itemName + "</yellow>!"));
            plugin.getLogManager().log("item", "<yellow>" + player.getName() + "</yellow> repaired <yellow>" + itemName + "</yellow>");
        } else {
            player.sendMessage(CC.success("Repaired <yellow>" + target.getName() + "</yellow>'s <yellow>" + itemName + "</yellow>!"));
            target.sendMessage(CC.success("Your <yellow>" + itemName + "</yellow> was repaired by <yellow>" + player.getName() + "</yellow>."));
            plugin.getLogManager().log("item", "<yellow>" + player.getName() + "</yellow> repaired <yellow>" + target.getName() + "</yellow>'s <yellow>" + itemName + "</yellow>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.repair.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
