package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /deathitems [player] - Retrieve or view items from a player's last death.
 * Without args: self mode (justplugin.deathitems).
 * With player arg: others mode (justplugin.deathitems.others).
 */
public class DeathItemsCommand implements TabExecutor {

    private final JustPlugin plugin;

    public DeathItemsCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("This command can only be used by players."));
            return true;
        }

        UUID targetUuid;
        String targetName;

        if (args.length >= 1) {
            // Others mode
            if (!player.hasPermission("justplugin.deathitems.others")) {
                player.sendMessage(CC.error("You don't have permission to view other players' death items."));
                return true;
            }

            @SuppressWarnings("deprecation")
            OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
            targetUuid = offP.getUniqueId();
            targetName = offP.getName() != null ? offP.getName() : args[0];

            // Load their death inventory if not already loaded
            plugin.getDeathInventoryManager().loadDeathInventory(targetUuid);
        } else {
            // Self mode
            targetUuid = player.getUniqueId();
            targetName = player.getName();
        }

        if (!plugin.getDeathInventoryManager().hasDeathInventory(targetUuid)) {
            if (args.length >= 1) {
                player.sendMessage(CC.error("No recorded death inventory for <yellow>" + targetName + "</yellow>."));
            } else {
                player.sendMessage(CC.error("You have no recorded death inventory. You either haven't died, or your items were kept on death."));
            }
            return true;
        }

        ItemStack[] deathItems = plugin.getDeathInventoryManager().getDeathInventory(targetUuid);
        if (deathItems == null) {
            player.sendMessage(CC.error("No death inventory data available."));
            return true;
        }

        // Open a read-only GUI showing the death items
        Inventory gui = Bukkit.createInventory(null, 54, CC.translate("<dark_gray>Death Items: <white>" + targetName));

        // Fill main inventory items (slots 0-35) into GUI slots 0-35
        for (int i = 0; i < 36; i++) {
            if (deathItems[i] != null) {
                gui.setItem(i, deathItems[i].clone());
            }
        }

        // Separator row (slot 36-44 in GUI = row 5)
        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var sepMeta = separator.getItemMeta();
        sepMeta.displayName(CC.translate("<dark_gray> "));
        separator.setItemMeta(sepMeta);
        for (int i = 36; i < 45; i++) {
            gui.setItem(i, separator);
        }

        // Armor in slots 45-48 (boots, leggings, chestplate, helmet)
        if (deathItems[36] != null) gui.setItem(45, deathItems[36].clone()); // boots
        if (deathItems[37] != null) gui.setItem(46, deathItems[37].clone()); // leggings
        if (deathItems[38] != null) gui.setItem(47, deathItems[38].clone()); // chestplate
        if (deathItems[39] != null) gui.setItem(48, deathItems[39].clone()); // helmet

        // Separator
        gui.setItem(49, separator);

        // Offhand in slot 50
        if (deathItems[40] != null) gui.setItem(50, deathItems[40].clone());

        // Restore button (green wool) at slot 53 - only for self or if has permission
        boolean canRestore;
        if (targetUuid.equals(player.getUniqueId())) {
            canRestore = player.hasPermission("justplugin.deathitems");
        } else {
            canRestore = player.hasPermission("justplugin.deathitems.others");
        }

        if (canRestore) {
            ItemStack restoreBtn = new ItemStack(Material.LIME_WOOL);
            var restoreMeta = restoreBtn.getItemMeta();
            restoreMeta.displayName(CC.translate("<green><bold>Restore Items</bold></green>"));
            restoreMeta.lore(List.of(
                    CC.translate("<gray>Click to give these items"),
                    CC.translate("<gray>to <yellow>" + targetName + "</yellow>.")
            ));
            restoreBtn.setItemMeta(restoreMeta);
            gui.setItem(53, restoreBtn);
        }

        player.openInventory(gui);

        String executedBy = player.getName();
        plugin.getLogManager().log("admin", "<yellow>" + executedBy + "</yellow> viewed death items of <yellow>" + targetName + "</yellow>.");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.deathitems.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

