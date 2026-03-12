package org.justme.justPlugin.commands.info;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerListCommand implements TabExecutor {

    private final JustPlugin plugin;
    private static final int PAGE_SIZE = 10;

    public PlayerListCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(CC.error("Invalid page number!"));
                return true;
            }
        }

        // Gather visible players
        List<Player> allPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            // Hide vanished from non-permitted
            if (plugin.getVanishManager().isVanished(p.getUniqueId()) && !(sender instanceof Player sp && sp.hasPermission("justplugin.vanish.see"))) {
                continue;
            }
            // Hide playerlist-hidden from non-permitted
            if (plugin.getVanishManager().isPlayerListHidden(p.getUniqueId()) && !(sender instanceof Player sp && sp.hasPermission("justplugin.playerlist.hide"))) {
                continue;
            }
            allPlayers.add(p);
        }

        // Sort: staff first, then alphabetically
        allPlayers.sort((a, b) -> {
            boolean aStaff = a.hasPermission("justplugin.staff");
            boolean bStaff = b.hasPermission("justplugin.staff");
            if (aStaff != bStaff) return aStaff ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        int totalPages = Math.max(1, (int) Math.ceil((double) allPlayers.size() / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int startIdx = (page - 1) * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, allPlayers.size());

        int max = Bukkit.getMaxPlayers();
        int count = allPlayers.size();

        sender.sendMessage(CC.translate(""));
        sender.sendMessage(CC.info("<gold><bold>Player List</bold></gold> <dark_gray>(<green>" + count + "<dark_gray>/<green>" + max + "<dark_gray>) <gray>Page <yellow>" + page + "<gray>/<yellow>" + totalPages));

        if (allPlayers.isEmpty()) {
            sender.sendMessage(CC.line("<gray>No players online."));
        } else {
            for (int i = startIdx; i < endIdx; i++) {
                Player p = allPlayers.get(i);
                boolean isStaff = p.hasPermission("justplugin.staff");
                boolean isVanished = plugin.getVanishManager().isVanished(p.getUniqueId());
                boolean isSuperVanished = plugin.getVanishManager().isSuperVanished(p.getUniqueId());
                String world = p.getWorld().getName();

                StringBuilder entry = new StringBuilder();
                if (isStaff) {
                    entry.append("<red>[Staff]</red> ");
                }
                entry.append("<yellow>").append(p.getName()).append("</yellow>");
                entry.append(" <dark_gray>- <gray>").append(world);
                if (isVanished && sender.hasPermission("justplugin.vanish.see")) {
                    if (isSuperVanished) {
                        entry.append(" <dark_gray>[<dark_purple>SV</dark_purple>]");
                    } else {
                        entry.append(" <dark_gray>[<gray>V</gray>]");
                    }
                }
                sender.sendMessage(CC.line(entry.toString()));
            }
        }

        // Navigation
        if (totalPages > 1) {
            StringBuilder nav = new StringBuilder("<gray>");
            if (page > 1) {
                nav.append("<click:run_command:'/playerlist ").append(page - 1).append("'><hover:show_text:'<gray>Previous page'><gold>« Prev</gold></hover></click>");
            }
            nav.append(" <dark_gray>| <gray>Page <yellow>").append(page).append("</yellow>/<yellow>").append(totalPages).append("</yellow> <dark_gray>| ");
            if (page < totalPages) {
                nav.append("<click:run_command:'/playerlist ").append(page + 1).append("'><hover:show_text:'<gray>Next page'><gold>Next »</gold></hover></click>");
            }
            sender.sendMessage(CC.translate(" " + nav));
        }
        sender.sendMessage(CC.translate(""));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3");
        }
        return List.of();
    }
}

