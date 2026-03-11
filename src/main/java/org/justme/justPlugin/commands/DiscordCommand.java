package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;

public class DiscordCommand implements CommandExecutor, TabCompleter {

    private final JustPlugin plugin;

    public DiscordCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String discordLink = plugin.getConfig().getString("discord-link", "§cNo Discord link configured.");
        sender.sendMessage("§9Discord: §b" + discordLink);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
