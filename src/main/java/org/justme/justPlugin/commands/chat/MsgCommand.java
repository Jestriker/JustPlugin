package org.justme.justPlugin.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class MsgCommand implements TabExecutor {

    private final JustPlugin plugin;

    public MsgCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.msg.usage")));
            return true;
        }

        // Mute check
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            player.sendMessage(CC.error("You are muted and cannot send private messages."));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || (plugin.getVanishManager().isVanished(target.getUniqueId()) && !player.hasPermission("justplugin.vanish.see"))) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("chat.msg.self-message")));
            return true;
        }
        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(CC.error("This player is ignoring you."));
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
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
        if (args.length == 1) {
            return plugin.getVanishManager().getVisiblePlayers(sender).stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
