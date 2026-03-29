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
            sender.sendMessage(CC.success("Server list MOTD updated!"));
            sender.sendMessage(CC.line("Preview:"));
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
            sender.sendMessage(CC.success("Join MOTD updated!"));
            sender.sendMessage(CC.line("Preview:"));
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
        sender.sendMessage(CC.translate("  <gold><bold>MOTD Configuration</bold></gold>"));
        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        // Server MOTD
        String serverMotd = plugin.getMotdManager().getServerMotd();
        sender.sendMessage(CC.translate(" <white><bold>Server List MOTD:</bold></white>"));
        if (serverMotd == null || serverMotd.isEmpty()) {
            sender.sendMessage(CC.line("<dark_gray>(using vanilla default)"));
        } else {
            String resolvedServer = serverMotd
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            sender.sendMessage(CC.translate(" " + resolvedServer));
        }

        sender.sendMessage(CC.translate(""));

        // Join MOTD
        String joinMotd = plugin.getMotdManager().getJoinMotd();
        sender.sendMessage(CC.translate(" <white><bold>Join MOTD:</bold></white>"));
        if (joinMotd == null || joinMotd.isEmpty()) {
            sender.sendMessage(CC.line("<dark_gray>(disabled)"));
        } else {
            String resolvedJoin = joinMotd
                    .replace("{player}", sender.getName())
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            sender.sendMessage(CC.translate(" " + resolvedJoin));
        }

        sender.sendMessage(CC.translate("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        if (sender.hasPermission("justplugin.motd.set")) {
            sender.sendMessage(CC.line("<dark_gray>Set: <yellow>/motd server <text></yellow> | <yellow>/motd join <text>"));
            sender.sendMessage(CC.line("<dark_gray>Reset: <yellow>/resetmotd [server | join]"));
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

