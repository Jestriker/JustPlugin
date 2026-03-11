package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.BackManager;
import org.justme.justPlugin.managers.TpaManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TpaCommand implements CommandExecutor, TabCompleter {

    private final JustPlugin plugin;
    private final TpaManager tpaManager;
    private final BackManager backManager;

    public TpaCommand(JustPlugin plugin) {
        this.plugin = plugin;
        this.tpaManager = plugin.getTpaManager();
        this.backManager = plugin.getBackManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /tpa <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot send a TPA request to yourself.");
            return true;
        }
        tpaManager.sendRequest(player, target, TpaManager.RequestType.TPA);
        player.sendMessage("§aTPA request sent to §e" + target.getName() + "§a. It will expire in 60 seconds.");
        target.sendMessage("§e" + player.getName() + " §awants to teleport to you. Use §e/tpaccept §aor §e/tpreject§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
