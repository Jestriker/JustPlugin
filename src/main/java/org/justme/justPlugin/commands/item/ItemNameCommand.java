package org.justme.justPlugin.commands.item;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class ItemNameCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ItemNameCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(plugin.getMessageManager().error("misc.itemname.empty-hand"));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().error("misc.itemname.usage"));
            return true;
        }
        String name = String.join(" ", args);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(CC.colorize(name));
        item.setItemMeta(meta);
        player.sendMessage(plugin.getMessageManager().success("misc.itemname.success",
                "{name}", name));
        plugin.getLogManager().log("item", "<yellow>" + player.getName() + "</yellow> renamed <yellow>" + item.getType().name().toLowerCase().replace("_", " ") + "</yellow> to <gray>" + name + "</gray>");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
