package org.justme.justPlugin.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.MessageManager;
import org.justme.justPlugin.managers.NickManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public class NickCommand implements TabExecutor {

    private final JustPlugin plugin;

    public NickCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }

        MessageManager mm = plugin.getMessageManager();
        NickManager nm = plugin.getNickManager();

        if (args.length == 0) {
            player.sendMessage(mm.error("nick.usage"));
            return true;
        }

        // /nick off or /nick reset - remove nickname
        if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("reset")) {
            if (!nm.hasNickname(player.getUniqueId())) {
                player.sendMessage(mm.error("nick.no-nickname"));
                return true;
            }
            nm.removeNickname(player.getUniqueId());
            nm.applyDisplayName(player);
            player.sendMessage(mm.success("nick.removed"));
            return true;
        }

        // Join all args as the nickname (supports spaces in MiniMessage)
        String nickname = String.join(" ", args);

        // Strip tags the player doesn't have permission for
        nickname = nm.stripUnpermitted(player, nickname);

        // Validate plain-text length
        int plainLen = nm.getPlainLength(nickname);
        if (plainLen == 0) {
            player.sendMessage(mm.error("nick.empty"));
            return true;
        }
        if (plainLen > nm.getMaxLength()) {
            player.sendMessage(mm.error("nick.too-long", "{max}", String.valueOf(nm.getMaxLength())));
            return true;
        }

        // Check blocked words
        if (nm.containsBlockedWord(nickname)) {
            player.sendMessage(mm.error("nick.blocked-word"));
            return true;
        }

        // Check duplicates
        if (nm.isDuplicate(player.getUniqueId(), nickname)) {
            player.sendMessage(mm.error("nick.duplicate"));
            return true;
        }

        // Set the nickname
        nm.setNickname(player.getUniqueId(), nickname);
        nm.applyDisplayName(player);
        player.sendMessage(mm.success("nick.set", "{nick}", nickname));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("off", "reset")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
