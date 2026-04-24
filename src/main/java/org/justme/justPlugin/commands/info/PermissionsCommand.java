package org.justme.justPlugin.commands.info;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /permissions &lt;player&gt; [filter] [page]
 * Shows every effective permission for an online player, 10 per page,
 * with clickable prev/next navigation and a hover tooltip that identifies
 * the source of each permission (plugin / OP default / attachment).
 */
public class PermissionsCommand implements TabExecutor {

    private static final int PAGE_SIZE = 10;

    private final JustPlugin plugin;

    public PermissionsCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("justplugin.permissions")) {
            sender.sendMessage(plugin.getMessageManager().error("info.permissions.no-permission"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageManager().error("info.permissions.usage"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessageManager().error(
                    "info.permissions.not-online", "{player}", args[0]));
            return true;
        }

        String filter = null;
        int page = 1;
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            try {
                int parsed = Integer.parseInt(a);
                if (parsed >= 1) {
                    page = parsed;
                    continue;
                }
            } catch (NumberFormatException ignored) { }
            filter = a.toLowerCase(Locale.ROOT);
        }

        displayPage(sender, target, filter, page);
        plugin.getLogManager().log("admin", "<yellow>" + sender.getName()
                + "</yellow> inspected permissions of <yellow>" + target.getName() + "</yellow>"
                + (filter == null ? "" : " <gray>(filter: <yellow>" + filter + "</yellow>)")
                + " <gray>page " + page);
        return true;
    }

    private void displayPage(CommandSender sender, Player target, String filter, int requestedPage) {
        List<Row> rows = buildRows(target, filter);

        if (rows.isEmpty()) {
            if (filter == null) {
                sender.sendMessage(plugin.getMessageManager().info(
                        "info.permissions.empty", "{player}", target.getName()));
            } else {
                sender.sendMessage(plugin.getMessageManager().info(
                        "info.permissions.empty-filtered",
                        "{player}", target.getName(), "{filter}", filter));
            }
            return;
        }

        int totalPages = (int) Math.ceil(rows.size() / (double) PAGE_SIZE);
        int page = Math.max(1, Math.min(requestedPage, totalPages));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, rows.size());

        String headerKey = filter == null ? "info.permissions.header" : "info.permissions.header-filtered";
        sender.sendMessage(plugin.getMessageManager().prefixed(
                headerKey,
                "{player}", target.getName(),
                "{count}", String.valueOf(rows.size()),
                "{page}", String.valueOf(page),
                "{max_page}", String.valueOf(totalPages),
                "{filter}", filter == null ? "" : filter));

        for (int i = start; i < end; i++) {
            Row r = rows.get(i);
            String icon = r.granted ? "<green>✔</green>" : "<red>✘</red>";
            String colour = r.granted ? "<white>" : "<gray>";
            String hover = escape(r.source);
            String line = " <dark_gray>></dark_gray> " + icon + " <hover:show_text:'<gray>Source: <yellow>" + hover
                    + "</yellow>'>" + colour + escape(r.node) + "</hover>";
            sender.sendMessage(CC.translate(line));
        }

        if (totalPages > 1) {
            String base = "/permissions " + target.getName() + (filter == null ? "" : " " + filter);
            StringBuilder nav = new StringBuilder(" <dark_gray>");
            if (page > 1) nav.append(CC.clickCmd("<aqua>[← Prev]</aqua>", base + " " + (page - 1), true)).append(" ");
            nav.append("<gray>Page ").append(page).append("/").append(totalPages).append("</gray>");
            if (page < totalPages) nav.append(" ").append(CC.clickCmd("<aqua>[Next →]</aqua>", base + " " + (page + 1), true));
            sender.sendMessage(CC.translate(nav.toString()));
        }
    }

    private List<Row> buildRows(Player target, String filter) {
        Map<String, Row> byNode = new HashMap<>();
        for (PermissionAttachmentInfo info : target.getEffectivePermissions()) {
            String node = info.getPermission();
            if (filter != null && !node.toLowerCase(Locale.ROOT).contains(filter)) continue;
            String source = resolveSource(info);
            byNode.merge(node, new Row(node, info.getValue(), source),
                    (a, b) -> b);
        }
        List<Row> rows = new ArrayList<>(byNode.values());
        rows.sort(Comparator.comparing((Row r) -> !r.granted)
                .thenComparing(r -> r.node));
        return rows;
    }

    private String resolveSource(PermissionAttachmentInfo info) {
        if (info.getAttachment() == null) {
            return "OP default or plugin.yml";
        }
        Plugin p = info.getAttachment().getPlugin();
        if (p == null) return "attachment";
        PluginDescriptionFile desc = p.getDescription();
        return desc == null ? p.getName() : desc.getName();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    matches.add(p.getName());
                }
            }
            return matches;
        }
        return List.of();
    }

    private record Row(String node, boolean granted, String source) {}
}
