package org.justme.justPlugin.commands.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaltopCommand implements TabExecutor {

    private final JustPlugin plugin;

    public BaltopCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        boolean isOp = sender.isOp();
        List<Map.Entry<UUID, Double>> sorted = plugin.getEconomyManager().getAllBalancesSorted();

        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(CC.translate("  <gold><bold>💰 Balance Top</bold></gold>"));
        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        int rank = 0;
        int displayCount = 0;
        for (Map.Entry<UUID, Double> entry : sorted) {
            if (displayCount >= 10) break;
            UUID uuid = entry.getKey();
            double balance = entry.getValue();
            boolean hidden = plugin.getEconomyManager().isBaltopHidden(uuid);

            OfflinePlayer offP = Bukkit.getOfflinePlayer(uuid);
            String name = offP.getName() != null ? offP.getName() : uuid.toString().substring(0, 8);

            rank++;

            if (hidden) {
                if (isOp) {
                    // OPs see hidden players but with an indicator
                    String medal = getMedal(rank);
                    sender.sendMessage(CC.translate("  " + medal + " <gray>" + name + " <dark_gray>(hidden) <gray>- <yellow>" + plugin.getEconomyManager().format(balance)));
                    displayCount++;
                }
                // Non-OPs skip hidden players entirely — don't count toward displayed
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

        // Show sender's own rank if they're a player
        if (sender instanceof Player player) {
            int playerRank = 1;
            for (Map.Entry<UUID, Double> entry : sorted) {
                if (entry.getKey().equals(player.getUniqueId())) break;
                playerRank++;
            }
            double myBal = plugin.getEconomyManager().getBalance(player.getUniqueId());
            sender.sendMessage(CC.translate("  <gray>Your rank: <gold>#" + playerRank + " <gray>- <yellow>" + plugin.getEconomyManager().format(myBal)));
        }

        return true;
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

