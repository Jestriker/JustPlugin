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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor, TabCompleter {

    private final IgnoreManager ignoreManager;

    public ReplyCommand(JustPlugin plugin) {
        this.ignoreManager = plugin.getIgnoreManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUsage: /reply <message>");
            return true;
        }
        UUID targetId = MsgCommand.lastMsg.get(player.getUniqueId());
        if (targetId == null) {
            player.sendMessage("§cYou have nobody to reply to.");
            return true;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cThat player is no longer online.");
            return true;
        }
        if (ignoreManager.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage("§cThat player is ignoring you.");
            return true;
        }
        String message = String.join(" ", args);
        player.sendMessage("§7[§dMe §7-> §d" + target.getName() + "§7]: §f" + message);
        target.sendMessage("§7[§d" + player.getName() + " §7-> §dMe§7]: §f" + message);
        MsgCommand.lastMsg.put(target.getUniqueId(), player.getUniqueId());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
