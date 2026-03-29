package org.justme.justPlugin.listeners.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.ChatManager;
import org.justme.justPlugin.managers.MuteManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.PAPIHook;
import org.justme.justPlugin.util.PlaceholderResolver;
import org.justme.justPlugin.util.TimeUtil;

import java.util.List;

/**
 * Handles async chat events including mute checks, chat formatting
 * with LuckPerms prefixes/suffixes, hover stats, team chat mode,
 * and ignore filtering.
 */
public class ChatListener implements Listener {

    private final JustPlugin plugin;

    public ChatListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // --- Rank GUI chat input callback ---
        if (plugin.getRankGuiManager() != null && plugin.getRankGuiManager().hasPendingInput(player.getUniqueId())) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getRankGuiManager().handleChatInput(player, message)
            );
            return;
        }

        // --- Mute check ---
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            MuteManager.MuteEntry muteEntry = plugin.getMuteManager().getMuteEntry(player.getUniqueId());
            if (muteEntry != null) {
                player.sendMessage(CC.error("You are muted!"));
                player.sendMessage(CC.line("Reason: <white>" + muteEntry.reason));
                if (muteEntry.expires != -1L) {
                    long remaining = muteEntry.getRemainingMs();
                    player.sendMessage(CC.line("Remaining: <yellow>" + TimeUtil.formatDuration(remaining)));
                } else {
                    player.sendMessage(CC.line("Duration: <red>Permanent"));
                }
            }
            return;
        }

        // --- Chat format with hover stats ---
        String separator = plugin.getConfig().getString("chat.separator", "<dark_gray> > <reset>");
        boolean hoverEnabled = plugin.getConfig().getBoolean("chat.hover.enabled", true);

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            String prefix = getLuckPermsPrefix(source);
            String suffix = getLuckPermsSuffix(source);
            Component prefixComponent = (prefix != null && !prefix.isEmpty())
                    ? CC.colorize(prefix) : Component.empty();
            Component suffixComponent = (suffix != null && !suffix.isEmpty())
                    ? CC.colorize(suffix) : Component.empty();

            Component nameBlock = prefixComponent.append(sourceDisplayName).append(suffixComponent);

            if (hoverEnabled) {
                List<String> hoverLines = plugin.getConfig().getStringList("chat.hover.lines");
                if (hoverLines.isEmpty()) {
                    hoverLines = List.of(
                            "{prefix} {name} {suffix}",
                            "<dark_gray>----------------",
                            "<green>$ <white>Money: <yellow>{balance_short}",
                            "<red>Kills: <red>{kills_short}",
                            "<#ff8800>Deaths: <#ff8800>{deaths_short}",
                            "<yellow>Playtime: <yellow>{playtime_compact}",
                            "<dark_gray>----------------"
                    );
                }
                StringBuilder hoverText = new StringBuilder();
                for (int idx = 0; idx < hoverLines.size(); idx++) {
                    String line = hoverLines.get(idx);
                    line = PlaceholderResolver.resolve(source, plugin, line);
                    line = PAPIHook.setPlaceholders(source, line);
                    if (idx > 0) hoverText.append("\n");
                    hoverText.append(line);
                }

                String clickPerm = plugin.getConfig().getString("chat.hover.click-to-view-permission", "justplugin.stats.others");
                if (viewer instanceof Player viewerPlayer && viewerPlayer.hasPermission(clickPerm)) {
                    String clickText = plugin.getConfig().getString("chat.hover.click-to-view", "<gray>Click to view statistics");
                    hoverText.append("\n").append(clickText);
                    nameBlock = nameBlock
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CC.translate(hoverText.toString())))
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/stats " + source.getName()));
                } else {
                    nameBlock = nameBlock
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CC.translate(hoverText.toString())));
                }
            }

            return nameBlock
                    .append(CC.translate(separator))
                    .append(message);
        });

        ChatManager.ChatMode mode = plugin.getChatManager().getChatMode(player.getUniqueId());

        if (mode == ChatManager.ChatMode.TEAM) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getChatManager().sendTeamMessage(player, message)
            );
        } else {
            // Filter out ignored players from seeing this message
            event.viewers().removeIf(viewer -> {
                if (viewer instanceof Player viewerPlayer) {
                    return plugin.getIgnoreManager().isIgnoring(viewerPlayer.getUniqueId(), player.getUniqueId());
                }
                return false;
            });
        }
    }

    private String getLuckPermsPrefix(Player player) {
        if (!plugin.isLuckPermsAvailable()) return null;
        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) user = lp.getUserManager().loadUser(player.getUniqueId()).join();
            if (user != null) return user.getCachedData().getMetaData().getPrefix();
        } catch (Throwable ignored) {}
        return null;
    }

    private String getLuckPermsSuffix(Player player) {
        if (!plugin.isLuckPermsAvailable()) return null;
        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) user = lp.getUserManager().loadUser(player.getUniqueId()).join();
            if (user != null) return user.getCachedData().getMetaData().getSuffix();
        } catch (Throwable ignored) {}
        return null;
    }
}

