package org.justme.justPlugin.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.justme.justPlugin.JustPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaManager {

    public enum RequestType { TPA, TPAHERE }

    public static class TpaRequest {
        public final UUID sender;
        public final UUID target;
        public final RequestType type;

        public TpaRequest(UUID sender, UUID target, RequestType type) {
            this.sender = sender;
            this.target = target;
            this.type = type;
        }
    }

    private final JustPlugin plugin;
    // key = target UUID, value = pending request
    private final Map<UUID, TpaRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, Integer> taskIds = new HashMap<>();

    public TpaManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player sender, Player target, RequestType type) {
        cancelExistingRequest(target.getUniqueId());
        TpaRequest request = new TpaRequest(sender.getUniqueId(), target.getUniqueId(), type);
        pendingRequests.put(target.getUniqueId(), request);

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingRequests.remove(target.getUniqueId()) != null) {
                    taskIds.remove(target.getUniqueId());
                    if (sender.isOnline()) {
                        sender.sendMessage("§cYour TPA request to §e" + target.getName() + " §chas expired.");
                    }
                    if (target.isOnline()) {
                        target.sendMessage("§cThe TPA request from §e" + sender.getName() + " §chas expired.");
                    }
                }
            }
        }.runTaskLater(plugin, 20L * 60).getTaskId();
        taskIds.put(target.getUniqueId(), taskId);
    }

    public TpaRequest getPendingRequest(UUID targetId) {
        return pendingRequests.get(targetId);
    }

    public void removeRequest(UUID targetId) {
        cancelExistingRequest(targetId);
        pendingRequests.remove(targetId);
    }

    private void cancelExistingRequest(UUID targetId) {
        Integer oldTask = taskIds.remove(targetId);
        if (oldTask != null) {
            plugin.getServer().getScheduler().cancelTask(oldTask);
        }
    }

    public boolean hasOutgoingRequest(UUID senderId) {
        for (TpaRequest req : pendingRequests.values()) {
            if (req.sender.equals(senderId)) return true;
        }
        return false;
    }

    public TpaRequest getOutgoingRequest(UUID senderId) {
        for (TpaRequest req : pendingRequests.values()) {
            if (req.sender.equals(senderId)) return req;
        }
        return null;
    }

    public void cancelOutgoingRequest(UUID senderId) {
        UUID toRemove = null;
        for (Map.Entry<UUID, TpaRequest> entry : pendingRequests.entrySet()) {
            if (entry.getValue().sender.equals(senderId)) {
                toRemove = entry.getKey();
                break;
            }
        }
        if (toRemove != null) {
            cancelExistingRequest(toRemove);
            pendingRequests.remove(toRemove);
        }
    }
}
