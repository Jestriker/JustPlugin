package org.justme.justPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.IgnoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MsgCommand implements CommandExecutor, TabCompleter {

    private final IgnoreManager ignoreManager;
    // last PM partner: sender -> target
    public static final Map<UUID, UUID> lastMsg = new HashMap<>();

    public MsgCommand(JustPlugin plugin) {
        this.ignoreManager = plugin.getIgnoreManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /msg <player> <message>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer §e" + args[0] + " §cis not online.");
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot message yourself.");
            return true;
        }
        if (ignoreManager.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage("§cThat player is ignoring you.");
            return true;
        }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        player.sendMessage("§7[§dMe §7-> §d" + target.getName() + "§7]: §f" + message);
        target.sendMessage("§7[§d" + player.getName() + " §7-> §dMe§7]: §f" + message);
        lastMsg.put(player.getUniqueId(), target.getUniqueId());
        lastMsg.put(target.getUniqueId(), player.getUniqueId());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
