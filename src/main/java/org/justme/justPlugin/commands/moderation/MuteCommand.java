package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MuteCommand implements TabExecutor {

    private final JustPlugin plugin;

    public MuteCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.mute.usage")));
            return true;
        }

        String defaultReason = plugin.getConfig().getString("default-reasons.mute", "Muted by an operator");
        String reason = args.length >= 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : defaultReason;
        String mutedBy = sender instanceof Player ? sender.getName() : "Console";

        // Resolve player
        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[0];

        if (plugin.getMuteManager().isMuted(uuid)) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.mute.already-muted",
                    "{player}", name));
            return true;
        }

        plugin.getMuteManager().mute(uuid, name, reason, mutedBy);
        sender.sendMessage(plugin.getMessageManager().success("moderation.mute.success-permanent",
                "{player}", name));
        sender.sendMessage(plugin.getMessageManager().line("moderation.mute.reason-line",
                "{reason}", reason));

        // Configurable announcement
        net.kyori.adventure.text.Component announcement = plugin.getMessageManager().warning("moderation.mute.announce",
                "{player}", name, "{staff}", mutedBy, "{reason}", reason);
        if (plugin.getConfig().getBoolean("punishment-announcements.mute", false)) {
            Bukkit.broadcast(announcement);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("justplugin.announce.mute")) {
                    p.sendMessage(announcement);
                }
            }
        }
        plugin.getLogManager().log("mute", "<yellow>" + mutedBy + "</yellow> muted <yellow>" + name + "</yellow>. Reason: <gray>" + reason);
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

