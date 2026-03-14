package org.justme.justPlugin.commands.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class DiscordCommand implements TabExecutor {

    private final JustPlugin plugin;

    public DiscordCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String link = plugin.getConfig().getString("discord-link", "https://discord.gg/example");

        if (args.length >= 1 && args[0].equalsIgnoreCase("set") && sender.hasPermission("justplugin.discord.set")) {
            String newLink = args[1];
            plugin.getConfig().set("discord-link", newLink);
            plugin.saveConfig();
            sender.sendMessage(CC.success("Discord link updated to: <yellow>" + newLink));
            return true;
        }

        sender.sendMessage(CC.info("<gold><bold>Join our Discord!</bold></gold>"));
        Component linkComponent = Component.text(link)
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(link));
        sender.sendMessage(linkComponent);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.discord.set")) {
            return List.of("set");
        }
        return List.of();
    }
}

