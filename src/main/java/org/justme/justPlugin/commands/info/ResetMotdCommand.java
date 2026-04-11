package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.MotdManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class ResetMotdCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ResetMotdCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("server")) {
            plugin.getMotdManager().resetServerMotd();
            sender.sendMessage(plugin.getMessageManager().success("info.resetmotd.server-reset"));
            sender.sendMessage(plugin.getMessageManager().info("info.motd.preview"));
            sender.sendMessage(CC.translate(MotdManager.DEFAULT_SERVER_MOTD));
            plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> reset the <white>server list MOTD</white> to default");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("join")) {
            plugin.getMotdManager().resetJoinMotd();
            sender.sendMessage(plugin.getMessageManager().success("info.resetmotd.join-reset"));
            sender.sendMessage(plugin.getMessageManager().info("info.motd.preview"));
            sender.sendMessage(CC.translate(MotdManager.DEFAULT_JOIN_MOTD.replace("{player}", sender.getName())));
            plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> reset the <white>join MOTD</white> to default");
            return true;
        }

        // No args or anything else = reset both
        plugin.getMotdManager().resetServerMotd();
        plugin.getMotdManager().resetJoinMotd();
        sender.sendMessage(plugin.getMessageManager().success("info.resetmotd.both-reset"));
        sender.sendMessage(CC.translate(""));
        sender.sendMessage(plugin.getMessageManager().info("info.resetmotd.server-label"));
        sender.sendMessage(CC.translate(" " + MotdManager.DEFAULT_SERVER_MOTD));
        sender.sendMessage(CC.translate(""));
        sender.sendMessage(plugin.getMessageManager().info("info.resetmotd.join-label"));
        sender.sendMessage(CC.translate(" " + MotdManager.DEFAULT_JOIN_MOTD.replace("{player}", sender.getName())));
        plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> reset <white>both MOTDs</white> to default");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("server", "join").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

