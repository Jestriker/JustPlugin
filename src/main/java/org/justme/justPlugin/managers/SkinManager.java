package org.justme.justPlugin.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages custom player skins using Mojang's API.
 * <p>
 * Features:
 * <ul>
 *   <li>Set a player's skin to any Minecraft username's skin</li>
 *   <li>Clear/reset skin back to the player's own username</li>
 *   <li>Ban specific skin names from being used</li>
 *   <li>Persist skin overrides and bans across restarts</li>
 *   <li>Auto-apply stored skins on join (for cracked/offline players too)</li>
 * </ul>
 */
public class SkinManager {

    private final JustPlugin plugin;
    private final File dataFile;
    private YamlConfiguration data;

    // skin name -> cached texture data (TTL 10 min)
    private final Map<String, CachedSkin> skinCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 600_000; // 10 minutes

    // UUID -> skin name override (persisted)
    private final Map<UUID, String> playerSkins = new HashMap<>();

    // Banned skin names (persisted)
    private final Set<String> bannedSkins = new HashSet<>();

    public SkinManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "skins.yml");
        load();
    }

    private void load() {
        if (!dataFile.exists()) {
            data = new YamlConfiguration();
            save();
        } else {
            data = YamlConfiguration.loadConfiguration(dataFile);
        }

        // Load player skin overrides
        playerSkins.clear();
        if (data.contains("player-skins")) {
            for (String key : data.getConfigurationSection("player-skins").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String skinName = data.getString("player-skins." + key);
                    if (skinName != null && !skinName.isEmpty()) {
                        playerSkins.put(uuid, skinName);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Load banned skins
        bannedSkins.clear();
        List<String> banned = data.getStringList("banned-skins");
        for (String name : banned) {
            bannedSkins.add(name.toLowerCase());
        }
    }

    private void save() {
        // Save player skins
        data.set("player-skins", null);
        for (var entry : playerSkins.entrySet()) {
            data.set("player-skins." + entry.getKey().toString(), entry.getValue());
        }

        // Save banned skins
        data.set("banned-skins", new ArrayList<>(bannedSkins));

        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Skin] Failed to save skins.yml: " + e.getMessage());
        }
    }

    // ==================== Skin Setting ====================

    /**
     * Set a player's skin to the skin of the given username.
     * Runs async for Mojang API calls, applies on main thread.
     */
    public void setSkin(Player target, String skinName, Player initiator) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CachedSkin cached = getCachedSkin(skinName);
            if (cached == null) {
                // Fetch from Mojang API
                cached = fetchSkinFromMojang(skinName);
            }

            if (cached == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        initiator.sendMessage(CC.error("Could not find skin for <yellow>" + skinName + "</yellow>. Player may not exist.")));
                return;
            }

            final CachedSkin skin = cached;
            Bukkit.getScheduler().runTask(plugin, () -> {
                applySkin(target, skin);
                playerSkins.put(target.getUniqueId(), skinName);
                save();

                if (initiator.equals(target)) {
                    target.sendMessage(CC.success("Your skin has been set to <yellow>" + skinName + "</yellow>'s skin."));
                } else {
                    initiator.sendMessage(CC.success("Set <yellow>" + target.getName() + "</yellow>'s skin to <yellow>" + skinName + "</yellow>'s skin."));
                    target.sendMessage(CC.info("Your skin was changed to <yellow>" + skinName + "</yellow>'s skin by a staff member."));
                }
            });
        });
    }

    /**
     * Clear/reset a player's skin back to their own username.
     */
    public void clearSkin(Player target, Player initiator) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CachedSkin skin = fetchSkinFromMojang(target.getName());

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (skin != null) {
                    applySkin(target, skin);
                }
                playerSkins.remove(target.getUniqueId());
                save();

                if (initiator.equals(target)) {
                    target.sendMessage(CC.success("Your skin has been reset to your default."));
                } else {
                    initiator.sendMessage(CC.success("Reset <yellow>" + target.getName() + "</yellow>'s skin to default."));
                    target.sendMessage(CC.info("Your skin was reset by a staff member."));
                }
            });
        });
    }

    /**
     * Apply a stored skin on join (for players who have a skin override).
     */
    public void applyOnJoin(Player player) {
        String skinName = playerSkins.get(player.getUniqueId());
        if (skinName == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CachedSkin cached = getCachedSkin(skinName);
            if (cached == null) cached = fetchSkinFromMojang(skinName);
            if (cached == null) return;

            final CachedSkin skin = cached;
            Bukkit.getScheduler().runTask(plugin, () -> applySkin(player, skin));
        });
    }

    private void applySkin(Player player, CachedSkin skin) {
        try {
            PlayerProfile profile = player.getPlayerProfile();
            profile.setProperty(new ProfileProperty("textures", skin.value, skin.signature));
            player.setPlayerProfile(profile);

            // Refresh for other players (hide/show trick)
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player) && other.canSee(player)) {
                    other.hidePlayer(plugin, player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> other.showPlayer(plugin, player), 2L);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Skin] Failed to apply skin to " + player.getName() + ": " + e.getMessage());
        }
    }

    // ==================== Mojang API ====================

    private CachedSkin getCachedSkin(String name) {
        CachedSkin cached = skinCache.get(name.toLowerCase());
        if (cached != null && System.currentTimeMillis() - cached.fetchTime < CACHE_TTL) {
            return cached;
        }
        return null;
    }

    private CachedSkin fetchSkinFromMojang(String username) {
        try {
            // Step 1: Get UUID from username
            URI uuidUri = URI.create("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection conn = (HttpURLConnection) uuidUri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "JustPlugin");

            if (conn.getResponseCode() != 200) {
                conn.disconnect();
                return null;
            }

            JsonObject uuidResponse = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            conn.disconnect();
            String uuid = uuidResponse.get("id").getAsString();

            // Step 2: Get skin data from session server
            URI profileUri = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            HttpURLConnection conn2 = (HttpURLConnection) profileUri.toURL().openConnection();
            conn2.setRequestMethod("GET");
            conn2.setConnectTimeout(5000);
            conn2.setReadTimeout(5000);
            conn2.setRequestProperty("User-Agent", "JustPlugin");

            if (conn2.getResponseCode() != 200) {
                conn2.disconnect();
                return null;
            }

            JsonObject profileResponse = JsonParser.parseReader(new InputStreamReader(conn2.getInputStream())).getAsJsonObject();
            conn2.disconnect();

            var properties = profileResponse.getAsJsonArray("properties");
            for (var prop : properties) {
                JsonObject propObj = prop.getAsJsonObject();
                if ("textures".equals(propObj.get("name").getAsString())) {
                    String value = propObj.get("value").getAsString();
                    String signature = propObj.has("signature") ? propObj.get("signature").getAsString() : "";
                    CachedSkin skin = new CachedSkin(value, signature, System.currentTimeMillis());
                    skinCache.put(username.toLowerCase(), skin);
                    return skin;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Skin] Mojang API error for " + username + ": " + e.getMessage());
        }
        return null;
    }

    // ==================== Skin Bans ====================

    public boolean isSkinBanned(String name) {
        return bannedSkins.contains(name.toLowerCase());
    }

    public boolean banSkin(String name) {
        if (bannedSkins.add(name.toLowerCase())) {
            save();
            return true;
        }
        return false;
    }

    public boolean unbanSkin(String name) {
        if (bannedSkins.remove(name.toLowerCase())) {
            save();
            return true;
        }
        return false;
    }

    public Set<String> getBannedSkins() {
        return Collections.unmodifiableSet(bannedSkins);
    }

    /** Check if a player has a stored skin override. */
    public boolean hasSkinOverride(UUID uuid) {
        return playerSkins.containsKey(uuid);
    }

    public String getSkinOverride(UUID uuid) {
        return playerSkins.get(uuid);
    }

    // ==================== Data ====================

    private record CachedSkin(String value, String signature, long fetchTime) {}
}

