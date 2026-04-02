package org.justme.justPlugin.commands.kits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.KitManager;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /kitpreview <name> - preview a kit's contents in a read-only GUI.
 * Requires justplugin.kit.preview and justplugin.kits.<name> permissions.
 */
@SuppressWarnings("NullableProblems")
public class KitPreviewCommand implements TabExecutor {

    private final JustPlugin plugin;

    public KitPreviewCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.only-players")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().error("general.invalid-usage", "{usage}", "/kitpreview <name>"));
            return true;
        }

        String kitName = args[0].toLowerCase();
        KitManager.KitData kit = plugin.getKitManager().getKit(kitName);

        if (kit == null || !"published".equals(kit.status)) {
            player.sendMessage(plugin.getMessageManager().error("kits.kit-not-found", "{kit}", args[0]));
            return true;
        }

        if (!player.hasPermission(kit.permission)) {
            player.sendMessage(plugin.getMessageManager().error("kits.no-permission", "{kit}", kit.displayName));
            return true;
        }

        plugin.getKitPreviewGui().open(player, kit);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return plugin.getKitManager().getAvailableKits(player).stream()
                    .map(k -> k.name)
                    .filter(n -> n.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
