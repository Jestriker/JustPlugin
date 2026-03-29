package org.justme.justPlugin.commands.economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class BaltopCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BaltopCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            // Console fallback: show text-based output
            showTextBaltop(sender);
            return true;
        }

        // Open the Baltop GUI for players
        plugin.getBaltopGui().open(player);
        return true;
    }

    private void showTextBaltop(CommandSender sender) {
        boolean isOp = sender.hasPermission("justplugin.baltop.viewhidden");
        var sorted = plugin.getEconomyManager().getAllBalancesSorted();

        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(CC.translate("  <gold><bold>💰 Balance Top</bold></gold>"));
        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        int rank = 0;
        int displayCount = 0;
        for (var entry : sorted) {
            if (displayCount >= 10) break;
            java.util.UUID uuid = entry.getKey();
            double balance = entry.getValue();
            boolean hidden = plugin.getEconomyManager().isBaltopHidden(uuid);

            org.bukkit.OfflinePlayer offP = org.bukkit.Bukkit.getOfflinePlayer(uuid);
            String name = offP.getName() != null ? offP.getName() : uuid.toString().substring(0, 8);

            rank++;

            if (hidden) {
                if (isOp) {
                    String medal = getMedal(rank);
                    sender.sendMessage(CC.translate("  " + medal + " <gray>" + name + " <dark_gray>(hidden) <gray>- <yellow>" + plugin.getEconomyManager().format(balance)));
                    displayCount++;
                }
                continue;
            }

            String medal = getMedal(rank);
            sender.sendMessage(CC.translate("  " + medal + " <white>" + name + " <gray>- <yellow>" + plugin.getEconomyManager().format(balance)));
            displayCount++;
        }

        if (displayCount == 0) {
            sender.sendMessage(CC.translate("  <gray>No players found."));
        }

        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private String getMedal(int rank) {
        return switch (rank) {
            case 1 -> "<gold>🥇 #1";
            case 2 -> "<gray>🥈 #2";
            case 3 -> "<#cd7f32>🥉 #3";
            default -> "<dark_gray>#" + rank;
        };
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
