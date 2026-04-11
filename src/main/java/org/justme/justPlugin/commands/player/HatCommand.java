package org.justme.justPlugin.commands.player;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class HatCommand implements TabExecutor {

    private final JustPlugin plugin;

    public HatCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            player.sendMessage(plugin.getMessageManager().error("player.hat.empty-hand"));
            return true;
        }
        ItemStack helmet = player.getInventory().getHelmet();
        player.getInventory().setHelmet(hand.clone());
        player.getInventory().setItemInMainHand(helmet != null ? helmet : new ItemStack(Material.AIR));
        String itemName = hand.getType().name().toLowerCase().replace("_", " ");
        player.sendMessage(plugin.getMessageManager().success("player.hat.success", "{item}", itemName));
        plugin.getLogManager().log("item", "<yellow>" + player.getName() + "</yellow> equipped <yellow>" + itemName + "</yellow> as a hat");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

