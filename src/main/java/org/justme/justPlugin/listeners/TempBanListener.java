package org.justme.justPlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.TemporaryBanManager;

public class TempBanListener implements Listener {

    private final TemporaryBanManager tempBanManager;

    public TempBanListener(JustPlugin plugin) {
        this.tempBanManager = plugin.getTemporaryBanManager();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        String ip = event.getAddress().getHostAddress();
        TemporaryBanManager.TempBan nameBan = tempBanManager.getTempBan(playerName);
        if (nameBan != null) {
            long remaining = nameBan.expireTime - System.currentTimeMillis();
            String timeLeft = TemporaryBanManager.formatDuration(remaining);
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                    "§cYou are temporarily banned.\n§7Reason: §e" + nameBan.reason + "\n§7Expires in: §e" + timeLeft);
            return;
        }
        TemporaryBanManager.TempBan ipBan = tempBanManager.getTempBan(ip);
        if (ipBan != null) {
            long remaining = ipBan.expireTime - System.currentTimeMillis();
            String timeLeft = TemporaryBanManager.formatDuration(remaining);
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                    "§cYour IP is temporarily banned.\n§7Reason: §e" + ipBan.reason + "\n§7Expires in: §e" + timeLeft);
        }
    }
}
