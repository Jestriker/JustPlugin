package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand implements CommandExecutor, TabCompleter {

    private final JustPlugin plugin;

    public InfoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§8§m----§8[ §6JustPlugin §8]§m----");
        sender.sendMessage("§7Version: §e" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Author: §eJustMe");
        sender.sendMessage("§7Description: §eEssentialsX-like plugin for Paper 1.21");
        sender.sendMessage("§7Commands: §e" + plugin.getDescription().getCommands().size());
        sender.sendMessage("§8§m------------------");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
