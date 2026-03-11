package org.justme.justPlugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.justme.justPlugin.JustPlugin;

import java.util.*;

public class IgnoreManager {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final Map<UUID, Set<UUID>> ignoreMap = new HashMap<>();

    public IgnoreManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
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

    public void loadPlayer(UUID uuid) {
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
        YamlConfiguration data = dataManager.getPlayerData(uuid);
        Set<UUID> ignored = ignoreMap.getOrDefault(uuid, Collections.emptySet());
        List<String> list = new ArrayList<>();
        for (UUID id : ignored) {
            list.add(id.toString());
        }
        data.set("ignored", list);
        dataManager.savePlayerData(uuid, data);
    }

    public Set<UUID> getIgnoredPlayers(UUID uuid) {
        return ignoreMap.getOrDefault(uuid, Collections.emptySet());
    }
}

