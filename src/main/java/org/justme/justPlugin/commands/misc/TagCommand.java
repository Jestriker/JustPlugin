package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.MessageManager;
import org.justme.justPlugin.managers.TagManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public class TagCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TagCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.players-only"));
            return true;
        }

        MessageManager mm = plugin.getMessageManager();
        TagManager tm = plugin.getTagManager();

        // No args: open tag GUI
        if (args.length == 0) {
            plugin.getTagGui().open(player);
            return true;
        }

        // /tag off - remove tag
        if (args[0].equalsIgnoreCase("off")) {
            if (!tm.hasEquippedTag(player.getUniqueId())) {
                player.sendMessage(mm.error("nick.tag-no-tag"));
                return true;
            }
            tm.unequipTag(player.getUniqueId());
            player.sendMessage(mm.success("nick.tag-removed"));
            return true;
        }

        // /tag <name> - equip tag
        String tagId = args[0].toLowerCase();
        TagManager.TagData tag = tm.getTag(tagId);

        if (tag == null) {
            player.sendMessage(mm.error("nick.tag-not-found", "{tag}", tagId));
            return true;
        }

        // Check permission
        if (!player.hasPermission(tag.permission)) {
            player.sendMessage(mm.error("nick.tag-no-permission", "{tag}", tagId));
            return true;
        }

        // Check if already equipped
        String currentTag = tm.getEquippedTagId(player.getUniqueId());
        if (tagId.equals(currentTag)) {
            // Unequip if clicking the same tag
            tm.unequipTag(player.getUniqueId());
            player.sendMessage(mm.success("nick.tag-removed"));
            return true;
        }

        tm.equipTag(player.getUniqueId(), tagId);
        player.sendMessage(mm.success("nick.tag-equipped", "{tag}", tag.display));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            TagManager tm = plugin.getTagManager();
            Stream<String> tagStream = tm.getTagIds().stream()
                    .filter(id -> {
                        TagManager.TagData tag = tm.getTag(id);
                        return tag != null && player.hasPermission(tag.permission);
                    });
            return Stream.concat(Stream.of("off"), tagStream)
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
