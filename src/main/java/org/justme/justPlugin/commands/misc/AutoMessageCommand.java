package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.AutoMessageManager;
import org.justme.justPlugin.managers.MessageManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command handler for /automessage (reload, list, toggle, send).
 */
@SuppressWarnings("NullableProblems")
public class AutoMessageCommand implements TabExecutor {

    private final JustPlugin plugin;
    private final MessageManager mm;

    public AutoMessageCommand(JustPlugin plugin) {
        this.plugin = plugin;
        this.mm = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.info("misc.automessage.usage"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender);
            case "toggle" -> handleToggle(sender, args);
            case "send" -> handleSend(sender, args);
            default -> sender.sendMessage(mm.info("misc.automessage.usage"));
        }
        return true;
    }

    // ==================== Subcommands ====================

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("justplugin.automessage.reload")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        AutoMessageManager manager = plugin.getAutoMessageManager();
        manager.reload();
        int count = manager.getMessageCount();
        sender.sendMessage(mm.success("misc.automessage.reloaded", "{count}", String.valueOf(count)));
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("justplugin.automessage.list")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        AutoMessageManager manager = plugin.getAutoMessageManager();
        Map<String, AutoMessageManager.AutoMessage> messages = manager.getMessages();

        if (messages.isEmpty()) {
            sender.sendMessage(mm.info("misc.automessage.none-configured"));
            return;
        }

        sender.sendMessage(mm.info("misc.automessage.list-header", "{count}", String.valueOf(messages.size())));

        for (AutoMessageManager.AutoMessage msg : messages.values()) {
            String key = msg.enabled ? "misc.automessage.list-entry-enabled" : "misc.automessage.list-entry-disabled";
            sender.sendMessage(mm.info(key,
                    "{id}", msg.id,
                    "{mode}", msg.mode,
                    "{detail}", msg.getDetail()));
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("justplugin.automessage.toggle")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(mm.info("misc.automessage.usage"));
            return;
        }

        String id = args[1];
        AutoMessageManager manager = plugin.getAutoMessageManager();
        Boolean newState = manager.toggle(id);

        if (newState == null) {
            sender.sendMessage(mm.error("misc.automessage.not-found", "{id}", id));
        } else if (newState) {
            sender.sendMessage(mm.success("misc.automessage.toggled-on", "{id}", id));
        } else {
            sender.sendMessage(mm.success("misc.automessage.toggled-off", "{id}", id));
        }
    }

    private void handleSend(CommandSender sender, String[] args) {
        if (!sender.hasPermission("justplugin.automessage.send")) {
            sender.sendMessage(mm.error("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(mm.info("misc.automessage.usage"));
            return;
        }

        String id = args[1];
        AutoMessageManager manager = plugin.getAutoMessageManager();

        if (manager.forceSend(id)) {
            sender.sendMessage(mm.success("misc.automessage.sent", "{id}", id));
        } else {
            sender.sendMessage(mm.error("misc.automessage.not-found", "{id}", id));
        }
    }

    // ==================== Tab Completion ====================

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("justplugin.automessage.reload")) subs.add("reload");
            if (sender.hasPermission("justplugin.automessage.list")) subs.add("list");
            if (sender.hasPermission("justplugin.automessage.toggle")) subs.add("toggle");
            if (sender.hasPermission("justplugin.automessage.send")) subs.add("send");
            return filterStartsWith(subs, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("toggle") || sub.equals("send")) {
                AutoMessageManager manager = plugin.getAutoMessageManager();
                return filterStartsWith(new ArrayList<>(manager.getMessages().keySet()), args[1]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
