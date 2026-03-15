package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class KickCommand implements TabExecutor {

    private final JustPlugin plugin;

    public KickCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /kick <player> [reason]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(CC.error("Player <yellow>" + args[0] + "</yellow> not found or not online!"));
            return true;
        }

        String defaultReason = plugin.getConfig().getString("default-reasons.kick", "Kicked by an operator");
        String reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : defaultReason;
        String kickedBy = sender instanceof Player ? sender.getName() : "Console";

        StringBuilder screen = new StringBuilder();
        screen.append("\n<red><bold>You have been kicked!</bold></red>\n\n");
        screen.append("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>\n\n");
        screen.append("<gray>Reason: <white>").append(reason).append("</white></gray>\n");
        screen.append("<gray>Kicked by: <white>").append(kickedBy).append("</white></gray>\n\n");
        screen.append("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        target.kick(CC.translate(screen.toString()));

        sender.sendMessage(CC.success("Kicked <yellow>" + target.getName() + "</yellow>. Reason: <gray>" + reason));

        // Configurable announcement
        net.kyori.adventure.text.Component announcement = CC.warning("<yellow>" + target.getName() + "</yellow> has been kicked by <yellow>" + kickedBy + "</yellow>. Reason: <gray>" + reason);
        if (plugin.getConfig().getBoolean("punishment-announcements.kick", false)) {
            Bukkit.broadcast(announcement);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.announce.kick")) {
                    p.sendMessage(announcement);
                }
            }
        }
        plugin.getLogManager().log("moderation", "<yellow>" + kickedBy + "</yellow> kicked <yellow>" + target.getName() + "</yellow>. Reason: <gray>" + reason);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

