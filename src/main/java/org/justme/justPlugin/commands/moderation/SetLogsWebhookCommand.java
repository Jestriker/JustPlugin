package org.justme.justPlugin.commands.moderation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("NullableProblems")
public class SetLogsWebhookCommand implements TabExecutor {

    private final JustPlugin plugin;
    private final Map<UUID, Long> lastTestTime = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingUrls = new ConcurrentHashMap<>();

    public SetLogsWebhookCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.usage"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "disable" -> {
                plugin.getWebhookManager().disable();
                sender.sendMessage(plugin.getMessageManager().success("moderation.setlogswebhook.disabled"));
                plugin.getLogManager().log("admin", sender.getName() + " disabled Discord webhook logging.");
            }
            case "confirm" -> handleConfirm(sender);
            case "cancel" -> handleCancel(sender);
            case "tryagain" -> handleTryAgain(sender);
            default -> handleSetUrl(sender, args[0]);
        }
        return true;
    }

    private void handleSetUrl(CommandSender sender, String url) {
        // Basic URL validation
        if (!url.startsWith("https://discord.com/api/webhooks/") && !url.startsWith("https://discordapp.com/api/webhooks/")) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.invalid-url"));
            return;
        }

        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes());
        pendingUrls.put(senderUuid, url);

        sender.sendMessage(plugin.getMessageManager().info("moderation.setlogswebhook.testing"));
        sender.sendMessage(plugin.getMessageManager().line("moderation.setlogswebhook.testing-hint"));

        plugin.getWebhookManager().sendTest(url).thenAccept(status -> {
            SchedulerUtil.runTask(plugin, () -> {
                if (status >= 200 && status < 300) {
                    sender.sendMessage(plugin.getMessageManager().success("moderation.setlogswebhook.test-success", "{status}", String.valueOf(status)));
                    sender.sendMessage(plugin.getMessageManager().line("moderation.setlogswebhook.test-success-hint"));

                    boolean clickable = true;
                    String confirmBtn = CC.clickCmd("<green>[✔ Confirm]</green>", "/setlogswebhook confirm", clickable);
                    String cancelBtn = CC.clickCmd("<red>[✕ Cancel]</red>", "/setlogswebhook cancel", clickable);
                    String tryAgainBtn = CC.clickCmd("<dark_purple>[↻ Try Again]</dark_purple>", "/setlogswebhook tryagain", clickable);
                    sender.sendMessage(CC.translate(" " + confirmBtn + "  " + cancelBtn + "  " + tryAgainBtn));
                } else {
                    sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.test-failed", "{status}", String.valueOf(status)));
                    sender.sendMessage(plugin.getMessageManager().line("moderation.setlogswebhook.test-failed-hint"));

                    boolean clickable = true;
                    String cancelBtn = CC.clickCmd("<red>[✕ Cancel]</red>", "/setlogswebhook cancel", clickable);
                    String tryAgainBtn = CC.clickCmd("<dark_purple>[↻ Try Again]</dark_purple>", "/setlogswebhook tryagain", clickable);
                    sender.sendMessage(CC.translate(" " + cancelBtn + "  " + tryAgainBtn));
                }
            });
        });
    }

    private void handleConfirm(CommandSender sender) {
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes());
        String url = pendingUrls.remove(senderUuid);
        if (url == null) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.no-pending"));
            return;
        }

        plugin.getWebhookManager().setWebhookUrl(url);
        sender.sendMessage(plugin.getMessageManager().success("moderation.setlogswebhook.confirm-success"));
        sender.sendMessage(plugin.getMessageManager().line("moderation.setlogswebhook.confirm-success-hint"));
        plugin.getLogManager().log("admin", sender.getName() + " configured and enabled Discord webhook logging.");
    }

    private void handleCancel(CommandSender sender) {
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes());
        pendingUrls.remove(senderUuid);
        sender.sendMessage(plugin.getMessageManager().info("moderation.setlogswebhook.cancel"));
    }

    private void handleTryAgain(CommandSender sender) {
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes());

        // Rate limit: 10 seconds between retries
        Long lastTime = lastTestTime.get(senderUuid);
        if (lastTime != null && System.currentTimeMillis() - lastTime < 10000) {
            long remaining = (10000 - (System.currentTimeMillis() - lastTime)) / 1000;
            sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.retry-cooldown", "{time}", String.valueOf(remaining)));
            return;
        }

        String url = pendingUrls.get(senderUuid);
        if (url == null) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.no-pending-url"));
            return;
        }

        lastTestTime.put(senderUuid, System.currentTimeMillis());
        sender.sendMessage(plugin.getMessageManager().info("moderation.setlogswebhook.retrying"));

        plugin.getWebhookManager().sendTest(url).thenAccept(status -> {
            SchedulerUtil.runTask(plugin, () -> {
                if (status >= 200 && status < 300) {
                    sender.sendMessage(plugin.getMessageManager().success("moderation.setlogswebhook.test-success", "{status}", String.valueOf(status)));

                    boolean clickable = true;
                    String confirmBtn = CC.clickCmd("<green>[✔ Confirm]</green>", "/setlogswebhook confirm", clickable);
                    String cancelBtn = CC.clickCmd("<red>[✕ Cancel]</red>", "/setlogswebhook cancel", clickable);
                    String tryAgainBtn = CC.clickCmd("<dark_purple>[↻ Try Again]</dark_purple>", "/setlogswebhook tryagain", clickable);
                    sender.sendMessage(CC.translate(" " + confirmBtn + "  " + cancelBtn + "  " + tryAgainBtn));
                } else {
                    sender.sendMessage(plugin.getMessageManager().error("moderation.setlogswebhook.retry-failed", "{status}", String.valueOf(status)));

                    boolean clickable = true;
                    String cancelBtn = CC.clickCmd("<red>[✕ Cancel]</red>", "/setlogswebhook cancel", clickable);
                    String tryAgainBtn = CC.clickCmd("<dark_purple>[↻ Try Again]</dark_purple>", "/setlogswebhook tryagain", clickable);
                    sender.sendMessage(CC.translate(" " + cancelBtn + "  " + tryAgainBtn));
                }
            });
        });
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("disable", "confirm", "cancel", "tryagain").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}

