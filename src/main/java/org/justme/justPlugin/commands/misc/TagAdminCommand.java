package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.MessageManager;
import org.justme.justPlugin.managers.TagManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles /tagcreate, /tagdelete, and /taglist admin commands.
 * The actual command name determines which action is taken.
 */
@SuppressWarnings("NullableProblems")
public class TagAdminCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TagAdminCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        MessageManager mm = plugin.getMessageManager();
        TagManager tm = plugin.getTagManager();

        switch (cmd.getName().toLowerCase()) {
            case "tagcreate" -> {
                // /tagcreate <id> <prefix|suffix> <display...>
                if (args.length < 3) {
                    sender.sendMessage(mm.error("nick.tagcreate-usage"));
                    return true;
                }

                String id = args[0].toLowerCase();
                String type = args[1].toLowerCase();

                if (!type.equals("prefix") && !type.equals("suffix")) {
                    sender.sendMessage(mm.error("nick.tagcreate-invalid-type"));
                    return true;
                }

                if (tm.tagExists(id)) {
                    sender.sendMessage(mm.error("nick.tagcreate-exists", "{tag}", id));
                    return true;
                }

                // Join remaining args as the display string
                StringBuilder display = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i > 2) display.append(" ");
                    display.append(args[i]);
                }

                tm.createTag(id, display.toString(), type);
                sender.sendMessage(mm.success("nick.tagcreate-success", "{tag}", id, "{display}", display.toString()));
                return true;
            }
            case "tagdelete" -> {
                // /tagdelete <id>
                if (args.length < 1) {
                    sender.sendMessage(mm.error("nick.tagdelete-usage"));
                    return true;
                }

                String id = args[0].toLowerCase();
                if (!tm.tagExists(id)) {
                    sender.sendMessage(mm.error("nick.tag-not-found", "{tag}", id));
                    return true;
                }

                tm.deleteTag(id);
                sender.sendMessage(mm.success("nick.tagdelete-success", "{tag}", id));
                return true;
            }
            case "taglist" -> {
                // /taglist
                var allTags = tm.getAllTags();
                if (allTags.isEmpty()) {
                    sender.sendMessage(mm.info("nick.taglist-empty"));
                    return true;
                }

                sender.sendMessage(mm.info("nick.taglist-header", "{count}", String.valueOf(allTags.size())));
                for (TagManager.TagData tag : allTags) {
                    sender.sendMessage(CC.line("<yellow>" + tag.id + " <dark_gray>(" + tag.type + ") <gray>- " + tag.display
                            + " <dark_gray>[" + tag.permission + "]"));
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        TagManager tm = plugin.getTagManager();

        switch (cmd.getName().toLowerCase()) {
            case "tagcreate" -> {
                if (args.length == 2) {
                    return Stream.of("prefix", "suffix")
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
            case "tagdelete" -> {
                if (args.length == 1) {
                    return tm.getTagIds().stream()
                            .filter(s -> s.startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        return List.of();
    }
}
