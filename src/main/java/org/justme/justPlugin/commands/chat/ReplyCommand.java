package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class ReplyCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ReplyCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error("Usage: /r <message>"));
            return true;
        }

        // Mute check
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            player.sendMessage(CC.error("You are muted and cannot send private messages."));
            return true;
        }
        UUID lastUuid = plugin.getChatManager().getLastMessaged(player.getUniqueId());
        if (lastUuid == null) {
            player.sendMessage(CC.error("No one to reply to!"));
            return true;
        }
        Player target = Bukkit.getPlayer(lastUuid);
        if (target == null) {
            player.sendMessage(CC.error("That player is no longer online!"));
            return true;
        }
        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(CC.error("This player is ignoring you."));
            return true;
        }
        String message = String.join(" ", args);
        player.sendMessage(CC.translate("<gray>[<gold>me <gray>→ <gold>" + target.getName() + "<gray>] <white>" + message));

        boolean replyClickable = plugin.getConfig().getBoolean("clickable-commands.msg-reply", true);
        String replyBtn = replyClickable
                ? " " + CC.suggestCmd("<dark_gray>[<aqua>↩ Reply<dark_gray>]", "/r ", true)
                : "";
        target.sendMessage(CC.translate("<gray>[<gold>" + player.getName() + " <gray>→ <gold>me<gray>] <white>" + message + replyBtn));
        plugin.getChatManager().setLastMessaged(player.getUniqueId(), target.getUniqueId());
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
