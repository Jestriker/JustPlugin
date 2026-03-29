package org.justme.justPlugin.commands.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class TeamMsgCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TeamMsgCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /teammsg <message>"));
            return true;
        }
        String teamName = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (teamName == null) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("team.general.not-in-team")));
            return true;
        }
        String message = String.join(" ", args);
        // Send directly to team without changing chat mode
        plugin.getChatManager().sendTeamMessage(player, message);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

