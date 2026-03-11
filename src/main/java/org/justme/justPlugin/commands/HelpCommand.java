package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.justme.justPlugin.JustPlugin;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements CommandExecutor, TabCompleter {

    private final JustPlugin plugin;

    public HelpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try { page = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        List<String> commands = new ArrayList<>(plugin.getDescription().getCommands().keySet());
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) commands.size() / pageSize);
        page = Math.max(1, Math.min(page, totalPages));
        sender.sendMessage("§8§m---§8[ §6JustPlugin Help §7- Page " + page + "/" + totalPages + " §8]§m---");
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, commands.size());
        for (int i = start; i < end; i++) {
            String cmd = commands.get(i);
            org.bukkit.command.PluginCommand pc = plugin.getCommand(cmd);
            String desc = (pc != null && pc.getDescription() != null && !pc.getDescription().isEmpty()) ? pc.getDescription() : "No description.";
            sender.sendMessage("§e/" + cmd + " §7- " + desc);
        }
        if (page < totalPages) sender.sendMessage("§7Use §e/help " + (page + 1) + " §7for the next page.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
