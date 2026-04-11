package org.justme.justPlugin.commands.home;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;

import org.justme.justPlugin.util.InputValidator;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class SetHomeCommand implements TabExecutor {

    private final JustPlugin plugin;

    public SetHomeCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        String name = args.length >= 1 ? args[0] : "home";
        if (!InputValidator.isValidName(name)) {
            player.sendMessage(plugin.getMessageManager().error("general.invalid-name"));
            return true;
        }
        if (plugin.getHomeManager().setHome(player.getUniqueId(), name, player.getLocation())) {
            player.sendMessage(plugin.getMessageManager().success("home.sethome.success", "{home}", name));
        } else {
            player.sendMessage(plugin.getMessageManager().error("home.sethome.max-reached", "{max}", String.valueOf(plugin.getHomeManager().getMaxHomes())));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("<name>");
        return List.of();
    }
}

