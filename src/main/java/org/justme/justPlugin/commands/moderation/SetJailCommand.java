package org.justme.justPlugin.commands.moderation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class SetJailCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SetJailCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("This command can only be used by players."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.setjail.usage")));
            return true;
        }

        String name = args[0].toLowerCase();

        if (plugin.getJailManager().jailExists(name)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("moderation.setjail.already-exists",
                    "{name}", name)));
            return true;
        }

        plugin.getJailManager().setJailLocation(name, player.getLocation());
        sender.sendMessage(CC.success(plugin.getMessageManager().raw("moderation.setjail.success",
                "{name}", name)));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("<name>");
        }
        return List.of();
    }
}
