package org.justme.justPlugin.commands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.CooldownManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class TpaHereCommand implements TabExecutor {

    private final JustPlugin plugin;

    public TpaHereCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tpahere.usage")));
            return true;
        }

        // Delay check (time between uses) - requires explicit delaybypass permission
        if (!player.hasPermission("justplugin.tpahere.delaybypass")
                && plugin.getCooldownManager().isOnDelay(player.getUniqueId(), "tpahere")) {
            int remaining = plugin.getCooldownManager().getRemainingDelaySeconds(player.getUniqueId(), "tpahere");
            player.sendMessage(CC.error("You must wait <yellow>" + CooldownManager.formatTime(remaining) + "</yellow> before using this command again."));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || (plugin.getVanishManager().isVanished(target.getUniqueId()) && !player.hasPermission("justplugin.vanish.see"))) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tpahere.cannot-self")));
            return true;
        }
        // Check if target is ignoring sender
        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(CC.error(plugin.getMessageManager().raw("teleport.tpa.blocked-by-ignore")));
            return true;
        }

        boolean clickable = plugin.getConfig().getBoolean("clickable-commands.tpa", true);

        String result = plugin.getTeleportManager().sendTpaHereRequest(player.getUniqueId(), target.getUniqueId());
        if (result != null) {
            if (result.equals("already_same")) {
                player.sendMessage(CC.error("You already have a pending request for <yellow>" + target.getName() + "</yellow>."));
            } else if (result.startsWith("already_other:")) {
                String otherName = result.substring("already_other:".length());
                player.sendMessage(CC.error("You already have a pending request to <yellow>" + otherName + "</yellow>."));
                String cancelCmd = CC.clickCmd("<yellow>/tpacancel</yellow>", "/tpacancel", clickable);
                player.sendMessage(CC.info("Use " + cancelCmd + " to cancel it and try again."));
            }
            return true;
        }

        plugin.getCooldownManager().setDelayStart(player.getUniqueId(), "tpahere");

        int timeout = plugin.getTeleportManager().getRequestTimeout();
        String cancelCmd = CC.clickCmd("<yellow>/tpacancel</yellow>", "/tpacancel", clickable);
        String acceptCmd = CC.clickCmd("<green>/tpaccept</green>", "/tpaccept", clickable);
        String rejectCmd = CC.clickCmd("<red>/tpreject</red>", "/tpreject", clickable);

        player.sendMessage(CC.success("TPA Here request sent to <yellow>" + target.getName() + "</yellow>. <gray>Expires in " + timeout + "s."));
        player.sendMessage(CC.info("Type " + cancelCmd + " to cancel."));
        target.sendMessage(CC.info("<yellow>" + player.getName() + "</yellow> has requested you to teleport to them."));
        target.sendMessage(CC.info("Type " + acceptCmd + " to accept or " + rejectCmd + " to deny. <gray>Expires in " + timeout + "s."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getVanishManager().getVisiblePlayers(sender).stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
