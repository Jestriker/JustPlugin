package org.justme.justPlugin.commands.info;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class MotdCommand implements TabExecutor {

    private final JustPlugin plugin;

    public MotdCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // /motd server <text> - set the server list MOTD
        if (args.length >= 2 && args[0].equalsIgnoreCase("server") && sender.hasPermission("justplugin.motd.set")) {
            String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            plugin.getMotdManager().setServerMotd(text);
            sender.sendMessage(plugin.getMessageManager().success("info.motd.server-updated"));
            sender.sendMessage(plugin.getMessageManager().info("info.motd.preview"));
            String preview = text
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            sender.sendMessage(CC.translate(preview));
            plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> changed the <white>server list MOTD</white>");
            return true;
        }

        // /motd join <text> - set the join MOTD
        if (args.length >= 2 && args[0].equalsIgnoreCase("join") && sender.hasPermission("justplugin.motd.set")) {
            String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            plugin.getMotdManager().setJoinMotd(text);
            sender.sendMessage(plugin.getMessageManager().success("info.motd.join-updated"));
            sender.sendMessage(plugin.getMessageManager().info("info.motd.preview"));
            String preview = text
                    .replace("{player}", sender.getName())
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            sender.sendMessage(CC.translate(preview));
            plugin.getLogManager().log("admin", "<yellow>" + sender.getName() + "</yellow> changed the <white>join MOTD</white>");
            return true;
        }

        // /motd - view both MOTDs
        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(plugin.getMessageManager().info("info.motd.config-header"));
        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        // Server MOTD
        String serverMotd = plugin.getMotdManager().getServerMotd();
        sender.sendMessage(plugin.getMessageManager().info("info.motd.server-label"));
        if (serverMotd == null || serverMotd.isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().info("info.motd.server-default"));
        } else {
            String resolvedServer = serverMotd
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            sender.sendMessage(CC.translate(" " + resolvedServer));
        }

        sender.sendMessage(CC.translate(""));

        // Join MOTD
        String joinMotd = plugin.getMotdManager().getJoinMotd();
        sender.sendMessage(plugin.getMessageManager().info("info.motd.join-label"));
        if (joinMotd == null || joinMotd.isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().info("info.motd.join-default"));
        } else {
            String resolvedJoin = joinMotd
                    .replace("{player}", sender.getName())
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            sender.sendMessage(CC.translate(" " + resolvedJoin));
        }

        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        if (sender.hasPermission("justplugin.motd.set")) {
            sender.sendMessage(plugin.getMessageManager().info("info.motd.set-hint"));
            sender.sendMessage(plugin.getMessageManager().info("info.motd.reset-hint"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.motd.set")) {
            return List.of("server", "join").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

