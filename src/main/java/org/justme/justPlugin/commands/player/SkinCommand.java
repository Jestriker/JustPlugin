package org.justme.justPlugin.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /skin command - set, clear, or reset player skins.
 * <p>
 * Subcommands:
 * <ul>
 *   <li><b>set &lt;name&gt;</b> - Set your skin to another player's skin</li>
 *   <li><b>set &lt;name&gt; &lt;target&gt;</b> - Set someone else's skin (staff)</li>
 *   <li><b>clear</b> / <b>reset</b> - Reset your skin to your username's default</li>
 *   <li><b>clear &lt;target&gt;</b> - Reset someone else's skin (staff)</li>
 * </ul>
 */
public class SkinCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SkinCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMessageManager().info("player.skin.help-header"));
            player.sendMessage(plugin.getMessageManager().line("player.skin.help-set"));
            player.sendMessage(plugin.getMessageManager().line("player.skin.help-clear"));
            if (player.hasPermission("justplugin.skin.others")) {
                player.sendMessage(plugin.getMessageManager().line("player.skin.help-set-other"));
                player.sendMessage(plugin.getMessageManager().line("player.skin.help-clear-other"));
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        var skinManager = plugin.getSkinManager();

        switch (sub) {
            case "set" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().error("player.skin.usage-set"));
                    return true;
                }
                String skinName = args[1];

                // Check if skin is banned
                if (skinManager.isSkinBanned(skinName) && !player.hasPermission("justplugin.skin.bypassban")) {
                    player.sendMessage(plugin.getMessageManager().error("player.skin.banned", "{skin}", skinName));
                    return true;
                }

                if (args.length >= 3) {
                    // Set for another player
                    if (!player.hasPermission("justplugin.skin.others")) {
                        player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage(plugin.getMessageManager().error("player.skin.not-online", "{player}", args[2]));
                        return true;
                    }
                    player.sendMessage(plugin.getMessageManager().info("player.skin.fetching", "{skin}", skinName));
                    skinManager.setSkin(target, skinName, player);
                } else {
                    // Set for self
                    player.sendMessage(plugin.getMessageManager().info("player.skin.fetching", "{skin}", skinName));
                    skinManager.setSkin(player, skinName, player);
                }
            }

            case "clear", "reset" -> {
                if (args.length >= 2) {
                    // Clear for another player
                    if (!player.hasPermission("justplugin.skin.others")) {
                        player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(plugin.getMessageManager().error("player.skin.not-online", "{player}", args[1]));
                        return true;
                    }
                    player.sendMessage(plugin.getMessageManager().info("player.skin.resetting-other", "{player}", target.getName()));
                    skinManager.clearSkin(target, player);
                } else {
                    player.sendMessage(plugin.getMessageManager().info("player.skin.resetting"));
                    skinManager.clearSkin(player, player);
                }
            }

            default -> {
                player.sendMessage(plugin.getMessageManager().error("player.skin.unknown-subcommand"));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("set", "clear", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
            // Suggest online player names as skin names
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0]) && sender.hasPermission("justplugin.skin.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && ("clear".equalsIgnoreCase(args[0]) || "reset".equalsIgnoreCase(args[0]))
                && sender.hasPermission("justplugin.skin.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

