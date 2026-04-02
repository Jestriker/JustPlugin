package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.MailManager;
import org.justme.justPlugin.util.CC;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class MailCommand implements TabExecutor {

    private final JustPlugin plugin;
    private static final int MAIL_PER_PAGE = 10;

    public MailCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.usage")));
            return true;
        }

        String sub = args[0].toLowerCase();
        MailManager mailManager = plugin.getMailManager();

        switch (sub) {
            case "send" -> {
                if (!player.hasPermission("justplugin.mail.send")) {
                    player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.send-usage")));
                    return true;
                }

                // Resolve target - supports offline players via cached data
                String targetName = args[1];
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
                    return true;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.self-send")));
                    return true;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                boolean sent = mailManager.sendMail(player.getUniqueId(), player.getName(), target.getUniqueId(), message);

                if (!sent) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.mailbox-full", "{player}", target.getName() != null ? target.getName() : targetName)));
                    return true;
                }

                player.sendMessage(CC.success(plugin.getMessageManager().raw("chat.mail.sent", "{player}", target.getName() != null ? target.getName() : targetName)));

                // Notify target if online
                if (target.isOnline()) {
                    Player onlineTarget = target.getPlayer();
                    if (onlineTarget != null) {
                        onlineTarget.sendMessage(CC.info(plugin.getMessageManager().raw("chat.mail.received-notify", "{sender}", player.getName())));
                    }
                }
            }

            case "read" -> {
                if (!player.hasPermission("justplugin.mail")) {
                    player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }

                List<Map<String, Object>> mail = mailManager.getMail(player.getUniqueId());
                if (mail.isEmpty()) {
                    player.sendMessage(CC.info(plugin.getMessageManager().raw("chat.mail.empty")));
                    return true;
                }

                int page = 1;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.invalid-page")));
                        return true;
                    }
                }

                int totalPages = (int) Math.ceil((double) mail.size() / MAIL_PER_PAGE);
                if (page < 1 || page > totalPages) {
                    player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.invalid-page")));
                    return true;
                }

                int startIndex = (page - 1) * MAIL_PER_PAGE;
                int endIndex = Math.min(startIndex + MAIL_PER_PAGE, mail.size());

                player.sendMessage(CC.translate(plugin.getMessageManager().raw("chat.mail.header",
                        "{page}", String.valueOf(page),
                        "{total}", String.valueOf(totalPages),
                        "{count}", String.valueOf(mail.size()))));

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm");
                for (int i = startIndex; i < endIndex; i++) {
                    Map<String, Object> entry = mail.get(i);
                    String senderName = (String) entry.getOrDefault("sender-name", "Unknown");
                    String msg = (String) entry.getOrDefault("message", "");
                    long timestamp = entry.get("timestamp") instanceof Number n ? n.longValue() : 0L;
                    boolean read = Boolean.TRUE.equals(entry.get("read"));

                    String dateStr = dateFormat.format(new Date(timestamp));
                    String readMarker = read ? "<gray>" : "<yellow>";
                    String readIcon = read ? "<dark_gray>[Read]" : "<yellow>[New]";

                    player.sendMessage(CC.translate(
                            " " + readIcon + " " + readMarker + "<gold>" + senderName + "</gold> <dark_gray>(" + dateStr + "): " + readMarker + msg));
                }

                if (totalPages > 1) {
                    String navText = "<dark_gray>Page " + page + "/" + totalPages;
                    if (page < totalPages) {
                        navText += " " + CC.clickCmd("<aqua>[Next]", "/mail read " + (page + 1), true);
                    }
                    if (page > 1) {
                        navText += " " + CC.clickCmd("<aqua>[Prev]", "/mail read " + (page - 1), true);
                    }
                    player.sendMessage(CC.translate(navText));
                }

                // Mark all as read after viewing
                mailManager.markAllRead(player.getUniqueId());
            }

            case "clear" -> {
                if (!player.hasPermission("justplugin.mail")) {
                    player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }
                int removed = mailManager.clearRead(player.getUniqueId());
                player.sendMessage(CC.success(plugin.getMessageManager().raw("chat.mail.cleared", "{count}", String.valueOf(removed))));
            }

            case "clearall" -> {
                if (!player.hasPermission("justplugin.mail")) {
                    player.sendMessage(plugin.getMessageManager().error("general.no-permission"));
                    return true;
                }
                int removed = mailManager.clearAll(player.getUniqueId());
                player.sendMessage(CC.success(plugin.getMessageManager().raw("chat.mail.cleared-all", "{count}", String.valueOf(removed))));
            }

            default -> player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.mail.usage")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("justplugin.mail.send")) subs.add("send");
            if (sender.hasPermission("justplugin.mail")) {
                subs.add("read");
                subs.add("clear");
                subs.add("clearall");
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return plugin.getVanishManager().getVisiblePlayers(sender).stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
