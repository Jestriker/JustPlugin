package org.justme.justPlugin.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TradeManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class TradeCommand implements TabExecutor {

    private final JustPlugin plugin;
    private final TradeManager tradeManager;

    public TradeCommand(JustPlugin plugin) {
        this.plugin = plugin;
        this.tradeManager = plugin.getTradeManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            boolean c = plugin.getConfig().getBoolean("clickable-commands.trade", true);
            String tradePlayer = CC.suggestCmd("<yellow>/trade <player></yellow>", "/trade ", c);
            String accept = CC.clickCmd("<green>/trade accept</green>", "/trade accept", c);
            String deny = CC.clickCmd("<red>/trade deny</red>", "/trade deny", c);
            String cancel = CC.clickCmd("<yellow>/trade cancel</yellow>", "/trade cancel", c);
            player.sendMessage(CC.info("<gold><bold>Trade Commands:</bold></gold>"));
            player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + tradePlayer + " <gray>- Send a trade request"));
            player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + accept + " <gray>- Accept a trade request"));
            player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + deny + " <gray>- Deny a trade request"));
            player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + cancel + " <gray>- Cancel your trade request"));
            return true;
        }

        String sub = args[0].toLowerCase();

        // Sub-commands
        if (sub.equals("accept")) {
            tradeManager.acceptRequest(player);
            return true;
        }
        if (sub.equals("deny") || sub.equals("reject")) {
            tradeManager.denyRequest(player);
            return true;
        }
        if (sub.equals("cancel")) {
            tradeManager.cancelRequest(player);
            return true;
        }

        // Normal: target must be online
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || (plugin.getVanishManager().isVanished(target.getUniqueId()) && !player.hasPermission("justplugin.vanish.see"))) {
            player.sendMessage(CC.error("Player not found or not online!"));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.error("You can't trade with yourself!"));
            return true;
        }

        tradeManager.sendRequest(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = plugin.getVanishManager().getVisiblePlayers(sender).stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            for (String s : List.of("accept", "deny", "cancel")) {
                if (s.startsWith(input)) suggestions.add(s);
            }
            return suggestions;
        }
        return List.of();
    }
}
