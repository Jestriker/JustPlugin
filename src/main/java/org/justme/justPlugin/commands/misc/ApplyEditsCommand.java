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
            sender.sendMessage(CC.error("The web editor is not running. Enable it in config.yml under <yellow>web-editor.enabled</yellow>."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /applyedits <code>"));
            sender.sendMessage(CC.line("Open the web editor at <aqua>http://localhost:" + webEditor.getPort() + "</aqua>"));
            sender.sendMessage(CC.line("Make your changes and use the generated code here."));
            return true;
        }

        String code = args[0].toUpperCase();
        WebEditorManager.PendingSession session = webEditor.getSession(code);

        if (session == null) {
            sender.sendMessage(CC.error("Invalid or expired session code: <yellow>" + code + "</yellow>"));
            sender.sendMessage(CC.line("Session codes expire after <yellow>10 minutes</yellow>. Generate a new one from the web editor."));
            return true;
        }

        // Apply the changes
        int applied = webEditor.applySession(session);
        webEditor.removeSession(code);

        // Show summary
        String targetFile = WebEditorManager.getConfigFiles().getOrDefault(session.fileId, session.fileId);
        sender.sendMessage(CC.success("Applied <yellow>" + applied + "</yellow> change" + (applied != 1 ? "s" : "") + " to <aqua>" + targetFile + "</aqua> from session <yellow>" + code + "</yellow>."));

        // List what changed
        int shown = 0;
        for (Map.Entry<String, Object> entry : session.changes.entrySet()) {
            if (shown >= 15) {
                sender.sendMessage(CC.line("<dark_gray>... and " + (session.changes.size() - 15) + " more changes."));
                break;
            }
            String val = String.valueOf(entry.getValue());
            if (val.length() > 50) val = val.substring(0, 47) + "...";
            sender.sendMessage(CC.line("<aqua>" + entry.getKey() + "</aqua> <dark_gray>→ <white>" + val));
            shown++;
        }

        sender.sendMessage(CC.info("Config saved. Some changes may require a server restart to take full effect."));

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


