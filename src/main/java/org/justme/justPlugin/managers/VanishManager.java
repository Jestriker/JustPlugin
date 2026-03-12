package org.justme.justPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;

public class VanishManager {

    private final JustPlugin plugin;
    private final Set<UUID> vanished = new HashSet<>();
    private final Set<UUID> superVanished = new HashSet<>();
    private final Map<UUID, GameMode> previousGameModes = new HashMap<>();
    private final Set<UUID> playerListHidden = new HashSet<>();

    public VanishManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    // --- Regular Vanish ---

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public void toggleVanish(Player player) {
        if (vanished.contains(player.getUniqueId())) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public void vanish(Player player) {
        // If super vanished, un-super-vanish first
        if (superVanished.contains(player.getUniqueId())) {
            unsuperVanish(player);
        }

        vanished.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        // Hide from all non-permitted players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        // Infinite invisibility with no particles
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                PotionEffect.INFINITE_DURATION, 0, true, false, false
        ));

        // Fake quit message
        Bukkit.broadcast(CC.translate("<yellow>" + player.getName() + " left the game"));

        // Remove from player list for non-vanish-see players
        player.playerListName(Component.empty());

        player.sendMessage(CC.success("You are now vanished."));
        player.sendMessage(CC.info("<gray>Note: Regular vanish makes you invisible, but you can still interact with the world (break blocks, pick up items, trigger redstone, etc.). Use <yellow>/supervanish</yellow> for full ghost mode."));
    }

    /**
     * Silently vanish a player without broadcasting quit message.
     * Used when restoring vanish state on join.
     */
    public void vanishSilent(Player player) {
        vanished.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                PotionEffect.INFINITE_DURATION, 0, true, false, false
        ));

        player.playerListName(Component.empty());
        player.sendMessage(CC.success("Your vanish state has been restored."));
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        player.removeMetadata("vanished", plugin);

        // Show to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        // Restore tab name
        player.playerListName(null); // null = default name

        // Fake join message
        Bukkit.broadcast(CC.translate("<yellow>" + player.getName() + " joined the game"));

        player.sendMessage(CC.success("You are no longer vanished."));
    }

    // --- Super Vanish (spectator-based full ghost mode) ---

    public boolean isSuperVanished(UUID uuid) {
        return superVanished.contains(uuid);
    }

    public boolean isSuperVanished(Player player) {
        return superVanished.contains(player.getUniqueId());
    }

    public void superVanish(Player player) {
        // If regular vanished, remove from regular set first
        if (vanished.contains(player.getUniqueId())) {
            vanished.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        superVanished.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player.setMetadata("supervanished", new FixedMetadataValue(plugin, true));

        // Store original game mode
        previousGameModes.put(player.getUniqueId(), player.getGameMode());

        // Hide from all non-permitted players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        // Fake quit
        Bukkit.broadcast(CC.translate("<yellow>" + player.getName() + " left the game"));

        // Switch to spectator mode
        player.setGameMode(GameMode.SPECTATOR);
        player.playerListName(Component.empty());

        player.sendMessage(CC.success("You are now <dark_purple>super vanished</dark_purple>."));
        player.sendMessage(CC.info("<gray>Super Vanish puts you in spectator mode. You are a complete ghost:"));
        player.sendMessage(CC.line("Cannot pick up or drop items"));
        player.sendMessage(CC.line("Cannot break blocks or place blocks"));
        player.sendMessage(CC.line("Cannot earn advancements"));
        player.sendMessage(CC.line("Cannot trigger redstone (pressure plates, tripwire, sculk sensors)"));
        player.sendMessage(CC.line("Cannot open chests (no animation/sound)"));
        player.sendMessage(CC.line("Cannot shoot arrows, throw projectiles, or use items"));
        player.sendMessage(CC.line("Your identity is fully hidden from other players"));
    }

    public void superVanishSilent(Player player) {
        superVanished.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player.setMetadata("supervanished", new FixedMetadataValue(plugin, true));

        // Store original game mode if not already stored
        if (!previousGameModes.containsKey(player.getUniqueId())) {
            // Try to load from player data
            YamlConfiguration data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            String gmName = data.getString("state.supervanish.previousGamemode", "SURVIVAL");
            try {
                previousGameModes.put(player.getUniqueId(), GameMode.valueOf(gmName));
            } catch (IllegalArgumentException e) {
                previousGameModes.put(player.getUniqueId(), GameMode.SURVIVAL);
            }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.playerListName(Component.empty());
        player.sendMessage(CC.success("Your super vanish state has been restored."));
    }

    public void unsuperVanish(Player player) {
        superVanished.remove(player.getUniqueId());
        player.removeMetadata("vanished", plugin);
        player.removeMetadata("supervanished", plugin);

        // Restore game mode
        GameMode prev = previousGameModes.remove(player.getUniqueId());
        if (prev != null && prev != GameMode.SPECTATOR) {
            player.setGameMode(prev);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }

        // Show to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        player.playerListName(null);

        Bukkit.broadcast(CC.translate("<yellow>" + player.getName() + " joined the game"));
        player.sendMessage(CC.success("You are no longer super vanished."));
    }

    // --- Player List Hidden ---

    public boolean isPlayerListHidden(UUID uuid) {
        if (playerListHidden.contains(uuid)) return true;
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        return data.getBoolean("playerlistHidden", false);
    }

    public void togglePlayerListHidden(UUID uuid) {
        if (playerListHidden.contains(uuid)) {
            playerListHidden.remove(uuid);
        } else {
            playerListHidden.add(uuid);
        }
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("playerlistHidden", playerListHidden.contains(uuid));
        plugin.getDataManager().savePlayerData(uuid, data);
    }

    // --- General helpers ---

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanished);
    }

    /**
     * Returns combined set of all vanished players (regular + super).
     */
    public Set<UUID> getAllVanishedPlayers() {
        Set<UUID> all = new HashSet<>(vanished);
        all.addAll(superVanished);
        return Collections.unmodifiableSet(all);
    }

    public List<Player> getVisiblePlayers(Player viewer) {
        List<Player> visible = new java.util.ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if ((isVanished(p.getUniqueId()) || isSuperVanished(p.getUniqueId())) && !viewer.hasPermission("justplugin.vanish.see")) continue;
            visible.add(p);
        }
        return visible;
    }

    public List<Player> getVisiblePlayers(org.bukkit.command.CommandSender sender) {
        List<Player> visible = new java.util.ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if ((isVanished(p.getUniqueId()) || isSuperVanished(p.getUniqueId())) && !sender.hasPermission("justplugin.vanish.see")) continue;
            visible.add(p);
        }
        return visible;
    }

    public int getVanishedCount() {
        Set<UUID> all = new HashSet<>(vanished);
        all.addAll(superVanished);
        return all.size();
    }

    public GameMode getPreviousGameMode(UUID uuid) {
        return previousGameModes.get(uuid);
    }

    public void handleJoin(Player player) {
        // Hide vanished players from non-permitted joining player
        Set<UUID> allVanished = getAllVanishedPlayers();
        for (UUID vanishedUuid : allVanished) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUuid);
            if (vanishedPlayer != null && !player.hasPermission("justplugin.vanish.see")) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    public void handleVanishedPlayerJoin(Player player) {
        if (vanished.contains(player.getUniqueId())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && !online.hasPermission("justplugin.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    PotionEffect.INFINITE_DURATION, 0, true, false, false
            ));
            player.playerListName(Component.empty());
        }
    }

    /**
     * Load playerlistHidden state from disk for an online player.
     */
    public void loadPlayerListHidden(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (data.getBoolean("playerlistHidden", false)) {
            playerListHidden.add(uuid);
        }
    }
}
