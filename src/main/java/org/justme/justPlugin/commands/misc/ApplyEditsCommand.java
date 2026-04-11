package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.WebEditorManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * /applyedits <code> - Applies pending config changes from the web editor.
 * Requires the highest permission: justplugin.applyedits
 */
@SuppressWarnings("NullableProblems")
public class ApplyEditsCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ApplyEditsCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        WebEditorManager webEditor = plugin.getWebEditorManager();

        if (webEditor == null || !webEditor.isRunning()) {
            sender.sendMessage(plugin.getMessageManager().error("misc.applyedits.not-running"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageManager().error("misc.applyedits.usage"));
            sender.sendMessage(plugin.getMessageManager().info("misc.applyedits.open-hint", "{port}", String.valueOf(webEditor.getPort())));
            sender.sendMessage(plugin.getMessageManager().info("misc.applyedits.make-changes-hint"));
            return true;
        }

        String code = args[0].toUpperCase();
        WebEditorManager.PendingSession session = webEditor.getSession(code);

        if (session == null) {
            sender.sendMessage(plugin.getMessageManager().error("misc.applyedits.invalid-session", "{code}", code));
            sender.sendMessage(plugin.getMessageManager().info("misc.applyedits.session-expire-hint"));
            return true;
        }

        // Apply the changes
        int applied = webEditor.applySession(session);
        webEditor.removeSession(code);

        // Show summary
        String targetFile = WebEditorManager.getConfigFiles().getOrDefault(session.fileId, session.fileId);
        sender.sendMessage(plugin.getMessageManager().success("misc.applyedits.applied", "{count}", String.valueOf(applied), "{file}", targetFile, "{code}", code));

        // List what changed
        int shown = 0;
        for (Map.Entry<String, Object> entry : session.changes.entrySet()) {
            if (shown >= 15) {
                sender.sendMessage(plugin.getMessageManager().info("misc.applyedits.more-changes", "{count}", String.valueOf(session.changes.size() - 15)));
                break;
            }
            String val = String.valueOf(entry.getValue());
            if (val.length() > 50) val = val.substring(0, 47) + "...";
            sender.sendMessage(CC.line("<aqua>" + entry.getKey() + "</aqua> <dark_gray>→ <white>" + val));
            shown++;
        }

        sender.sendMessage(plugin.getMessageManager().info("misc.applyedits.saved"));

        // Log it
        String executedBy = sender instanceof Player p ? p.getName() : "Console";
        plugin.getLogManager().log("admin", "<yellow>" + executedBy + "</yellow> applied <yellow>" + applied + "</yellow> config changes from web editor (session: <gray>" + code + "</gray>)");

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            WebEditorManager webEditor = plugin.getWebEditorManager();
            if (webEditor != null && webEditor.isRunning()) {
                return webEditor.getActiveCodes().stream()
                        .filter(c -> c.startsWith(args[0].toUpperCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}


