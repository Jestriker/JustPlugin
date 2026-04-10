package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.storage.StorageProvider;
import org.justme.justPlugin.util.SchedulerUtil;

import java.util.*;

public class IgnoreManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Set<UUID>> ignoreMap = new HashMap<>();

    public IgnoreManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.databaseManager = plugin.getDatabaseManager();
    }

    private boolean isUsingDatabase() {
        if (databaseManager == null) return false;
        StorageProvider provider = databaseManager.getProvider();
        if (provider == null) return false;
        String type = provider.getType();
        return "sqlite".equals(type) || "mysql".equals(type);
    }

    private StorageProvider getStorageProvider() {
        return databaseManager != null ? databaseManager.getProvider() : null;
    }

    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.contains(target);
    }

    public void toggleIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> new HashSet<>());
        if (ignored.contains(target)) {
            ignored.remove(target);
        } else {
            ignored.add(target);
        }
        saveIgnoreList(player);
    }

    public boolean addIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> new HashSet<>());
        if (!ignored.add(target)) return false;
        saveIgnoreList(player);
        return true;
    }

    public boolean removeIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        if (ignored == null || !ignored.remove(target)) return false;
        saveIgnoreList(player);
        return true;
    }

    public void clearIgnoreList(UUID player) {
        ignoreMap.put(player, new HashSet<>());
        saveIgnoreList(player);
    }

    public void loadPlayer(UUID uuid) {
        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = provider.getPlayerData(uuid);
                Set<UUID> ignored = new HashSet<>();
                Object ignoredObj = data.get("ignored");
                if (ignoredObj instanceof List<?> list) {
                    for (Object s : list) {
                        try {
                            ignored.add(UUID.fromString(s.toString()));
                        } catch (IllegalArgumentException ignored2) {}
                    }
                }
                ignoreMap.put(uuid, ignored);
                return;
            }
        }

        YamlConfiguration data = dataManager.getPlayerData(uuid);
        List<String> list = data.getStringList("ignored");
        Set<UUID> ignored = new HashSet<>();
        for (String s : list) {
            try {
                ignored.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored2) {}
        }
        ignoreMap.put(uuid, ignored);
    }

    public void unloadPlayer(UUID uuid) {
        saveIgnoreList(uuid);
        ignoreMap.remove(uuid);
    }

    private void saveIgnoreList(UUID uuid) {
        Set<UUID> ignored = ignoreMap.getOrDefault(uuid, Collections.emptySet());
        List<String> list = new ArrayList<>();
        for (UUID id : ignored) {
            list.add(id.toString());
        }

        if (isUsingDatabase()) {
            StorageProvider provider = getStorageProvider();
            if (provider != null) {
                Map<String, Object> data = provider.getPlayerData(uuid);
                data.put("ignored", list);
                SchedulerUtil.runAsync(plugin, () -> provider.savePlayerData(uuid, data));
                return;
            }
        }

        YamlConfiguration data = dataManager.getPlayerData(uuid);
        data.set("ignored", list);
        dataManager.savePlayerData(uuid, data);
    }

    public Set<UUID> getIgnoredPlayers(UUID uuid) {
        return ignoreMap.getOrDefault(uuid, Collections.emptySet());
    }
}
