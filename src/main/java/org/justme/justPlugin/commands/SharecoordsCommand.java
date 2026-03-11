package org.justme.justPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TeamManager;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SharecoordsCommand implements CommandExecutor, TabCompleter {

    private final TeamManager teamManager;

    public SharecoordsCommand(JustPlugin plugin) {
        this.teamManager = plugin.getTeamManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        String channel = args.length > 0 ? args[0].toLowerCase() : "global";
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        String worldName = player.getWorld().getName();
        String coords = "§e" + player.getName() + " §ashares coords: §e[" + worldName + ": " + x + ", " + y + ", " + z + "]";

        switch (channel) {
            case "team", "party" -> {
                TeamManager.Team team = teamManager.getPlayerTeam(player.getUniqueId());
                if (team == null) { player.sendMessage("§cYou are not in a team."); return true; }
                for (UUID memberId : team.getMembers()) {
                    Player member = player.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline()) member.sendMessage(coords);
                }
            }
            default -> {
                for (Player p : player.getServer().getOnlinePlayers()) p.sendMessage(coords);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("global", "team", "party").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
