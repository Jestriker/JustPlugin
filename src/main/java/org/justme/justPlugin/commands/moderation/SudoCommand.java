package org.justme.justPlugin.commands.moderation;

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
public class SudoCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SudoCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.sudo.usage")));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }
        // Prevent self-sudo
        if (sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.sudo.cannot-self"));
            return true;
        }
        // Prevent sudo against operators when sender is not console
        if (sender instanceof Player && target.isOp()) {
            sender.sendMessage(plugin.getMessageManager().error("moderation.sudo.cannot-op"));
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        // Block dangerous commands
        if (message.startsWith("/")) {
            String cmdLower = message.substring(1).toLowerCase();
            if (cmdLower.startsWith("op ") || cmdLower.startsWith("deop ") || cmdLower.startsWith("stop")
                    || cmdLower.startsWith("reload") || cmdLower.startsWith("sudo ")) {
                sender.sendMessage(plugin.getMessageManager().error("moderation.sudo.blocked"));
                return true;
            }
        }
        String senderName = sender instanceof Player ? sender.getName() : "Console";
        if (message.startsWith("/")) {
            target.performCommand(message.substring(1));
            sender.sendMessage(plugin.getMessageManager().success("moderation.sudo.success-command",
                    "{player}", target.getName(), "{command}", message));
            plugin.getLogManager().log("admin", "<yellow>" + senderName + "</yellow> forced <yellow>" + target.getName() + "</yellow> to run: <gray>" + message);
        } else {
            target.chat(message);
            sender.sendMessage(plugin.getMessageManager().success("moderation.sudo.success-chat",
                    "{player}", target.getName(), "{message}", message));
            plugin.getLogManager().log("admin", "<yellow>" + senderName + "</yellow> forced <yellow>" + target.getName() + "</yellow> to say: <gray>" + message);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String senderName = sender instanceof Player ? sender.getName() : "";
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> !n.equalsIgnoreCase(senderName))
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

