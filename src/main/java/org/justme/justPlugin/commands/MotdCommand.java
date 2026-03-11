package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;

public class MotdCommand implements CommandExecutor, TabCompleter {

    private final JustPlugin plugin;

    public MotdCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> motd = plugin.getConfig().getStringList("motd");
        if (motd.isEmpty()) {
            String single = plugin.getConfig().getString("motd", null);
            if (single != null && !single.isEmpty()) {
                sender.sendMessage(single.replace("&", "§"));
            } else {
                sender.sendMessage("§7No MOTD configured.");
            }
        } else {
            sender.sendMessage("§8§m---§8[ §6Message of the Day §8]§m---");
            for (String line : motd) {
                sender.sendMessage(line.replace("&", "§"));
            }
            sender.sendMessage("§8§m--------------------");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
