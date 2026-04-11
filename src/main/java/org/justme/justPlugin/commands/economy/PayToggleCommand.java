package org.justme.justPlugin.commands.economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class PayToggleCommand implements TabExecutor {

    private final JustPlugin plugin;

    public PayToggleCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        plugin.getEconomyManager().togglePay(player.getUniqueId());
        boolean off = plugin.getEconomyManager().isPayToggleOff(player.getUniqueId());
        player.sendMessage(off
                ? plugin.getMessageManager().success("economy.paytoggle.disabled")
                : plugin.getMessageManager().success("economy.paytoggle.enabled"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

