package org.justme.justPlugin.commands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class GetPosOffCommand implements TabExecutor {

    private final JustPlugin plugin;

    public GetPosOffCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("general.only-players"));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().error("moderation.offline.getposoff-usage"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        if (!offline.hasPlayedBefore() && !offline.isOnline()) {
            player.sendMessage(plugin.getMessageManager().error("general.player-not-found"));
            return true;
        }

        String name = offline.getName() != null ? offline.getName() : args[0];
        YamlConfiguration data = plugin.getDataManager().getPlayerData(offline.getUniqueId());
        if (!data.contains("last-location.world")) {
            player.sendMessage(plugin.getMessageManager().error("moderation.offline.no-data",
                    "{player}", name));
            return true;
        }

        String world = data.getString("last-location.world", "");
        int x = (int) data.getDouble("last-location.x");
        int y = (int) data.getDouble("last-location.y");
        int z = (int) data.getDouble("last-location.z");

        String msg = plugin.getMessageManager().raw("moderation.offline.getposoff-location",
                "{player}", name, "{world}", world,
                "{x}", String.valueOf(x), "{y}", String.valueOf(y), "{z}", String.valueOf(z));
        player.sendMessage(CC.translate(msg));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
