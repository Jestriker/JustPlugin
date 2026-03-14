package org.justme.justPlugin.commands.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;

@SuppressWarnings("NullableProblems")
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
        boolean canSeeVanished = sender instanceof Player sp && sp.hasPermission("justplugin.vanish.see");
        boolean canSeeHidden = sender instanceof Player sp2 && sp2.hasPermission("justplugin.playerlist.seeHiddenPlayers");
        for (Player p : Bukkit.getOnlinePlayers()) {
            // Hide vanished from non-permitted
            if (plugin.getVanishManager().isVanished(p.getUniqueId()) && !canSeeVanished) {
                continue;
            }
            // Hide playerlist-hidden from non-permitted
            if (plugin.getVanishManager().isPlayerListHidden(p.getUniqueId()) && !canSeeHidden) {
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
                boolean isListHidden = plugin.getVanishManager().isPlayerListHidden(p.getUniqueId());
                String world = p.getWorld().getName();

                StringBuilder entry = new StringBuilder();
                if (isStaff) {
                    entry.append("<red>[Staff]</red> ");
                }
                entry.append("<yellow>").append(p.getName()).append("</yellow>");
                entry.append(" <dark_gray>- <gray>").append(world);
                if (isVanished && canSeeVanished) {
                    if (isSuperVanished) {
                        entry.append(" <dark_gray>[<dark_purple>SV</dark_purple>]");
                    } else {
                        entry.append(" <dark_gray>[<gray>V</gray>]");
                    }
                }

                if (isListHidden && canSeeHidden) {
                    // Show hidden indicator with hover text
                    Component entryComponent = CC.translate(" <dark_gray>></dark_gray> <gray>" + entry);
                    Component hiddenTag = CC.translate(" <dark_gray>[<red>Hidden</red>]")
                            .hoverEvent(HoverEvent.showText(CC.translate(
                                    "<gray>This player used <yellow>/playerlisthide</yellow> to hide themselves.\n<gray>Players without <yellow>justplugin.playerlist.seeHiddenPlayers</yellow>\n<gray>cannot see this entry.")));
                    sender.sendMessage(entryComponent.append(hiddenTag));
                } else {
                    sender.sendMessage(CC.line(entry.toString()));
                }
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

