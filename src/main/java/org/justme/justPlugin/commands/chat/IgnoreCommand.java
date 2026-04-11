package org.justme.justPlugin.commands.chat;

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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class IgnoreCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list", "clearlist");
    private final JustPlugin plugin;

    public IgnoreCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        if (args.length < 1) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            case "clearlist" -> handleClearList(player);
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().error("chat.ignore.add-usage"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().error("general.player-not-found"));
            return;
        }
        if (target.equals(player)) {
            player.sendMessage(plugin.getMessageManager().error("chat.ignore.cannot-self"));
            return;
        }
        if (plugin.getIgnoreManager().isIgnoring(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().error("chat.ignore.already-ignored", "{player}", target.getName()));
            return;
        }
        plugin.getIgnoreManager().addIgnore(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(plugin.getMessageManager().success("chat.ignore.added", "{player}", target.getName()));
        target.sendMessage(plugin.getMessageManager().warning("chat.ignore.ignored-by", "{player}", player.getName()));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().error("chat.ignore.remove-usage"));
            return;
        }
        // Try online player first, then search ignored list by name
        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUuid = target != null ? target.getUniqueId() : null;
        String targetName = target != null ? target.getName() : args[1];

        // If player is offline, try to find them by name in the ignore list
        if (targetUuid == null) {
            Set<UUID> ignored = plugin.getIgnoreManager().getIgnoredPlayers(player.getUniqueId());
            for (UUID uuid : ignored) {
                OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
                if (off.getName() != null && off.getName().equalsIgnoreCase(args[1])) {
                    targetUuid = uuid;
                    targetName = off.getName();
                    break;
                }
            }
        }

        if (targetUuid == null) {
            player.sendMessage(plugin.getMessageManager().error("chat.ignore.not-in-list"));
            return;
        }
        if (!plugin.getIgnoreManager().removeIgnore(player.getUniqueId(), targetUuid)) {
            player.sendMessage(plugin.getMessageManager().error("chat.ignore.not-ignored", "{player}", targetName));
            return;
        }
        player.sendMessage(plugin.getMessageManager().success("chat.ignore.removed", "{player}", targetName));
        if (target != null) {
            target.sendMessage(plugin.getMessageManager().info("chat.ignore.unignored-by", "{player}", player.getName()));
        }
    }

    private void handleList(Player player) {
        Set<UUID> ignored = plugin.getIgnoreManager().getIgnoredPlayers(player.getUniqueId());
        if (ignored.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().info("chat.ignore.list-empty"));
            return;
        }
        boolean clickable = plugin.getConfig().getBoolean("clickable-commands.ignore", true);
        player.sendMessage(CC.translate(""));
        player.sendMessage(plugin.getMessageManager().info("chat.ignore.list-display-header", "{count}", String.valueOf(ignored.size())));
        int index = 1;
        for (UUID uuid : ignored) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            String name = off.getName() != null ? off.getName() : uuid.toString();
            boolean online = off.isOnline();
            String status = online ? "<green>online</green>" : "<red>offline</red>";
            String removeBtn = CC.clickCmd(" <dark_gray>[<red>✕ Remove<dark_gray>]", "/ignore remove " + name, clickable);
            player.sendMessage(CC.translate(" <dark_gray>></dark_gray> <gray>" + index + ".</gray> <yellow>" + name + "</yellow> <dark_gray>- " + status + removeBtn));
            index++;
        }
        player.sendMessage(CC.translate(""));
    }

    private void handleClearList(Player player) {
        Set<UUID> ignored = plugin.getIgnoreManager().getIgnoredPlayers(player.getUniqueId());
        if (ignored.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().info("chat.ignore.list-already-empty"));
            return;
        }
        int count = ignored.size();
        plugin.getIgnoreManager().clearIgnoreList(player.getUniqueId());
        player.sendMessage(plugin.getMessageManager().success("chat.ignore.list-cleared-count", "{count}", count + " player" + (count == 1 ? "" : "s")));
    }

    private void sendUsage(Player player) {
        boolean c = plugin.getConfig().getBoolean("clickable-commands.ignore", true);
        String add = CC.suggestCmd("<yellow>/ignore add <player></yellow>", "/ignore add ", c);
        String remove = CC.suggestCmd("<yellow>/ignore remove <player></yellow>", "/ignore remove ", c);
        String list = CC.clickCmd("<yellow>/ignore list</yellow>", "/ignore list", c);
        String clear = CC.clickCmd("<yellow>/ignore clearlist</yellow>", "/ignore clearlist", c);
        player.sendMessage(CC.info("<gold><bold>Ignore Commands:</bold></gold>"));
        player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + add + " <gray>- Add a player to your ignore list"));
        player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + remove + " <gray>- Remove a player from your ignore list"));
        player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + list + " <gray>- View your ignore list"));
        player.sendMessage(CC.translate(" <dark_gray>></dark_gray> " + clear + " <gray>- Clear your entire ignore list"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add")) {
                // Show visible online players (excluding vanished)
                if (sender instanceof Player player) {
                    return plugin.getVanishManager().getVisiblePlayers(player).stream()
                            .filter(p -> !p.equals(player))
                            .map(Player::getName)
                            .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (sub.equals("remove") && sender instanceof Player player) {
                // Show names from the player's ignore list
                Set<UUID> ignored = plugin.getIgnoreManager().getIgnoredPlayers(player.getUniqueId());
                return ignored.stream()
                        .map(uuid -> {
                            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
                            return off.getName() != null ? off.getName() : uuid.toString();
                        })
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}


