package org.justme.justPlugin.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.ChatManager;
import org.justme.justPlugin.managers.MuteManager;
import org.justme.justPlugin.managers.TeleportManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.PlaceholderResolver;
import org.justme.justPlugin.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {

    private final JustPlugin plugin;
    private final Set<UUID> godMode = new HashSet<>();
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();

    public PlayerListener(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String ip = event.getAddress().getHostAddress();
        net.kyori.adventure.text.Component kickMsg = plugin.getBanManager().checkJoinBan(
                player.getUniqueId(), player.getName(), ip);
        if (kickMsg != null) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMsg);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getEconomyManager().loadPlayer(player.getUniqueId());
        plugin.getIgnoreManager().loadPlayer(player.getUniqueId());
        plugin.getVanishManager().handleJoin(player);

        // Restore persistent player states (fly, speed, god, vanish)
        plugin.getPlayerStateManager().loadState(player);

        // Save player's IP for offline IP-ban lookups
        if (player.getAddress() != null) {
            YamlConfiguration data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            data.set("last-ip", player.getAddress().getAddress().getHostAddress());
            data.set("last-name", player.getName());
            plugin.getDataManager().savePlayerData(player.getUniqueId(), data);
        }

        // Restore persistent back location
        loadBackLocation(player.getUniqueId());

        // Restore persistent death location
        loadDeathLocation(player.getUniqueId());

        // If joining player is vanished, suppress real join message
        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            event.joinMessage(null);
            plugin.getVanishManager().handleVanishedPlayerJoin(player);
        }

        // Join MOTD (player join message)
        String joinMotd = plugin.getMotdManager().getJoinMotd();
        if (joinMotd != null && !joinMotd.isEmpty()) {
            String resolved = joinMotd
                    .replace("{player}", player.getName())
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            player.sendMessage(CC.translate(resolved));
        }

        // Track session playtime for scoreboard placeholders
        PlaceholderResolver.recordJoin(player.getUniqueId());

        // Show scoreboard (delayed slightly so the player is fully loaded)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getScoreboardManager().show(player);
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Save persistent player states before cleanup
        plugin.getPlayerStateManager().saveState(player);

        // Persist back location
        saveBackLocation(uuid);

        // Persist death location
        saveDeathLocation(uuid);

        // Persist inventory snapshot for offline /invsee
        saveInventorySnapshot(player);

        plugin.getEconomyManager().unloadPlayer(uuid);
        plugin.getIgnoreManager().unloadPlayer(uuid);
        plugin.getChatManager().removePlayer(uuid);
        plugin.getScoreboardManager().handleQuit(uuid);
        PlaceholderResolver.recordQuit(uuid);
        godMode.remove(uuid);

        // Cancel TPA requests and notify counterpart
        TeleportManager tpManager = plugin.getTeleportManager();
        TeleportManager.TpaRequest outgoing = tpManager.getOutgoingRequest(uuid);
        if (outgoing != null) {
            Player other = Bukkit.getPlayer(outgoing.target);
            tpManager.cancelOutgoingRequest(uuid);
            if (other != null) {
                other.sendMessage(CC.warning("Teleport request from <yellow>" + player.getName() + "</yellow> was cancelled — they logged off."));
            }
        }
        // Check if someone sent a request TO this player
        TeleportManager.TpaRequest incoming = tpManager.getIncomingRequest(uuid);
        if (incoming != null) {
            Player sender = Bukkit.getPlayer(incoming.sender);
            tpManager.removeRequest(incoming.sender);
            if (sender != null) {
                sender.sendMessage(CC.warning("Your teleport request was cancelled — <yellow>" + player.getName() + "</yellow> logged off."));
            }
        }

        tpManager.cancelPendingTeleport(uuid);

        // If quitting player is vanished, suppress real quit message
        if (plugin.getVanishManager().isVanished(uuid)) {
            event.quitMessage(null);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getTeleportManager().setBackLocation(player.getUniqueId(), player.getLocation());
        deathLocations.put(player.getUniqueId(), player.getLocation().clone());
        // Persist immediately
        saveBackLocation(player.getUniqueId());
        saveDeathLocation(player.getUniqueId());
    }

    // --- Advancement hiding for vanished/super-vanished players ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // If vanished or super-vanished, suppress the advancement broadcast message
        if (plugin.getVanishManager().isVanished(uuid) || plugin.getVanishManager().isSuperVanished(uuid)) {
            event.message(null);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // If no bed/anchor spawn, teleport to server spawn
        if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            String worldName = plugin.getConfig().getString("spawn.world");
            if (worldName != null && plugin.getConfig().contains("spawn.x")) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location spawnLoc = new Location(world,
                            plugin.getConfig().getDouble("spawn.x"),
                            plugin.getConfig().getDouble("spawn.y"),
                            plugin.getConfig().getDouble("spawn.z"),
                            (float) plugin.getConfig().getDouble("spawn.yaw"),
                            (float) plugin.getConfig().getDouble("spawn.pitch"));
                    event.setRespawnLocation(spawnLoc);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND
                || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            plugin.getTeleportManager().setBackLocation(event.getPlayer().getUniqueId(), event.getFrom());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        plugin.getTeleportManager().handleMoveDuringTeleport(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        NamespacedKey noteKey = new NamespacedKey(plugin, "paynote_value");

        // Check which hand holds the pay note (main hand or offhand)
        ItemStack item = null;
        boolean isOffhand = false;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand.getType() == Material.PAPER && mainHand.hasItemMeta()
                && mainHand.getItemMeta().getPersistentDataContainer().has(noteKey, PersistentDataType.DOUBLE)) {
            item = mainHand;
        } else if (offHand.getType() == Material.PAPER && offHand.hasItemMeta()
                && offHand.getItemMeta().getPersistentDataContainer().has(noteKey, PersistentDataType.DOUBLE)) {
            item = offHand;
            isOffhand = true;
        }

        if (item == null) return;

        event.setCancelled(true);

        ItemMeta meta = item.getItemMeta();
        Double rawValue = meta.getPersistentDataContainer().get(noteKey, PersistentDataType.DOUBLE);
        if (rawValue == null) return;
        double value = rawValue;

        // Remove the note and replace with a normal paper
        ItemStack normalPaper = new ItemStack(Material.PAPER, 1);
        if (isOffhand) {
            player.getInventory().setItemInOffHand(normalPaper);
        } else {
            player.getInventory().setItemInMainHand(normalPaper);
        }

        // Give the balance
        plugin.getEconomyManager().addBalance(player.getUniqueId(), value);
        String formatted = plugin.getEconomyManager().format(value);
        player.sendMessage(CC.success("Redeemed balance note for <yellow>" + formatted + "</yellow>!"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godMode.contains(player.getUniqueId())) {
                event.setCancelled(true);
                player.setFireTicks(0);
            }
            // Cancel pending teleport if player takes damage during warmup
            if (!event.isCancelled()) {
                plugin.getTeleportManager().handleDamageDuringTeleport(player);
            }
        }
    }

    // Prevent hunger drain in god mode
    @EventHandler
    public void onFoodChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godMode.contains(player.getUniqueId())) {
                event.setCancelled(true);
                player.setFoodLevel(20);
                player.setSaturation(20f);
            }
        }
    }

    // Block bad potion effects while in god mode
    @EventHandler
    public void onPotionEffect(org.bukkit.event.entity.EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godMode.contains(player.getUniqueId()) && event.getNewEffect() != null) {
                var type = event.getNewEffect().getType();
                if (isBadEffect(type)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isBadEffect(org.bukkit.potion.PotionEffectType type) {
        return type == org.bukkit.potion.PotionEffectType.POISON
                || type == org.bukkit.potion.PotionEffectType.WITHER
                || type == org.bukkit.potion.PotionEffectType.HUNGER
                || type == org.bukkit.potion.PotionEffectType.WEAKNESS
                || type == org.bukkit.potion.PotionEffectType.SLOWNESS
                || type == org.bukkit.potion.PotionEffectType.MINING_FATIGUE
                || type == org.bukkit.potion.PotionEffectType.NAUSEA
                || type == org.bukkit.potion.PotionEffectType.BLINDNESS
                || type == org.bukkit.potion.PotionEffectType.LEVITATION
                || type == org.bukkit.potion.PotionEffectType.BAD_OMEN
                || type == org.bukkit.potion.PotionEffectType.DARKNESS
                || type == org.bukkit.potion.PotionEffectType.INSTANT_DAMAGE;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // --- Rank GUI chat input callback ---
        if (plugin.getRankGuiManager() != null && plugin.getRankGuiManager().hasPendingInput(player.getUniqueId())) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getRankGuiManager().handleChatInput(player, message)
            );
            return;
        }

        // --- Mute check ---
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            MuteManager.MuteEntry muteEntry = plugin.getMuteManager().getMuteEntry(player.getUniqueId());
            if (muteEntry != null) {
                player.sendMessage(CC.error("You are muted!"));
                player.sendMessage(CC.line("Reason: <white>" + muteEntry.reason));
                if (muteEntry.expires != -1L) {
                    long remaining = muteEntry.getRemainingMs();
                    player.sendMessage(CC.line("Remaining: <yellow>" + TimeUtil.formatDuration(remaining)));
                } else {
                    player.sendMessage(CC.line("Duration: <red>Permanent"));
                }
            }
            return;
        }

        // --- LuckPerms prefix in chat ---
        if (plugin.isLuckPermsAvailable()) {
            String separator = plugin.getConfig().getString("chat.separator", "<dark_gray> » <reset>");
            event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) -> {
                String prefix = getLuckPermsPrefix(source);
                String suffix = getLuckPermsSuffix(source);
                Component prefixComponent = (prefix != null && !prefix.isEmpty())
                        ? CC.colorize(prefix) : Component.empty();
                Component suffixComponent = (suffix != null && !suffix.isEmpty())
                        ? CC.colorize(suffix) : Component.empty();
                return prefixComponent
                        .append(sourceDisplayName)
                        .append(suffixComponent)
                        .append(CC.translate(separator))
                        .append(message);
            }));
        }

        ChatManager.ChatMode mode = plugin.getChatManager().getChatMode(player.getUniqueId());

        if (mode == ChatManager.ChatMode.TEAM) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getChatManager().sendTeamMessage(player, message)
            );
        } else {
            // Filter out ignored players from seeing this message
            event.viewers().removeIf(viewer -> {
                if (viewer instanceof Player viewerPlayer) {
                    return plugin.getIgnoreManager().isIgnoring(viewerPlayer.getUniqueId(), player.getUniqueId());
                }
                return false;
            });
        }
    }

    // Server list ping — set server MOTD and hide vanished players from count and hover
    @SuppressWarnings("removal")
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // Set the server list MOTD if configured
        String serverMotd = plugin.getMotdManager().getServerMotd();
        if (serverMotd != null && !serverMotd.isEmpty()) {
            String resolved = serverMotd
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            event.motd(CC.translate(resolved));
        }

        event.setMaxPlayers(event.getMaxPlayers());
        try {
            Iterator<Player> iterator = event.iterator();
            while (iterator.hasNext()) {
                Player p = iterator.next();
                if (plugin.getVanishManager().isVanished(p.getUniqueId())) {
                    iterator.remove();
                }
            }
        } catch (UnsupportedOperationException ignored) {
        }
    }

    // Prevent vanished players from appearing in tab completion for anyone
    @EventHandler
    public void onTabComplete(com.destroystokyo.paper.event.server.AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player viewer)) return;
        if (viewer.hasPermission("justplugin.vanish.see")) return;

        // Remove any completions that match vanished player names
        Set<String> vanishedNames = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getVanishManager().isVanished(p.getUniqueId())) {
                vanishedNames.add(p.getName().toLowerCase());
            }
        }
        if (vanishedNames.isEmpty()) return;

        event.completions().removeIf(completion -> {
            String s = completion.suggestion().toLowerCase();
            return vanishedNames.contains(s);
        });
    }

    // God mode helpers
    public boolean isGodMode(UUID uuid) {
        return godMode.contains(uuid);
    }

    public void toggleGodMode(UUID uuid) {
        if (godMode.contains(uuid)) {
            godMode.remove(uuid);
        } else {
            godMode.add(uuid);
        }
    }

    // --- LuckPerms helpers ---

    /**
     * Get the player's highest-priority prefix from LuckPerms.
     * Uses the user's cached metadata which already resolves priority
     * across all groups (including inheritance) and player-specific nodes.
     * The prefix with the highest weight wins automatically.
     */
    private String getLuckPermsPrefix(Player player) {
        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                // User not loaded yet — try loading synchronously from cache
                user = lp.getUserManager().loadUser(player.getUniqueId()).join();
            }
            if (user != null) {
                return user.getCachedData().getMetaData().getPrefix();
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Get the player's highest-priority suffix from LuckPerms.
     */
    private String getLuckPermsSuffix(Player player) {
        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                user = lp.getUserManager().loadUser(player.getUniqueId()).join();
            }
            if (user != null) {
                return user.getCachedData().getMetaData().getSuffix();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Death location helpers
    public Location getDeathLocation(UUID uuid) {
        return deathLocations.get(uuid);
    }

    public boolean hasDeathLocation(UUID uuid) {
        return deathLocations.containsKey(uuid);
    }

    public void setDeathLocation(UUID uuid, Location loc) {
        deathLocations.put(uuid, loc.clone());
    }

    // --- Persistence helpers for back & death locations ---

    private void saveBackLocation(UUID uuid) {
        Location loc = plugin.getTeleportManager().getBackLocation(uuid);
        if (loc == null) return;
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("lastBack.world", loc.getWorld().getName());
        data.set("lastBack.x", loc.getX());
        data.set("lastBack.y", loc.getY());
        data.set("lastBack.z", loc.getZ());
        data.set("lastBack.yaw", loc.getYaw());
        data.set("lastBack.pitch", loc.getPitch());
        plugin.getDataManager().savePlayerData(uuid, data);
    }

    private void loadBackLocation(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (!data.contains("lastBack.world")) return;
        World world = Bukkit.getWorld(data.getString("lastBack.world", ""));
        if (world == null) return;
        Location loc = new Location(world,
                data.getDouble("lastBack.x"),
                data.getDouble("lastBack.y"),
                data.getDouble("lastBack.z"),
                (float) data.getDouble("lastBack.yaw"),
                (float) data.getDouble("lastBack.pitch"));
        plugin.getTeleportManager().setBackLocation(uuid, loc);
    }

    private void saveDeathLocation(UUID uuid) {
        Location loc = deathLocations.get(uuid);
        if (loc == null) return;
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("lastDeath.world", loc.getWorld().getName());
        data.set("lastDeath.x", loc.getX());
        data.set("lastDeath.y", loc.getY());
        data.set("lastDeath.z", loc.getZ());
        data.set("lastDeath.yaw", loc.getYaw());
        data.set("lastDeath.pitch", loc.getPitch());
        plugin.getDataManager().savePlayerData(uuid, data);
    }

    private void loadDeathLocation(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (!data.contains("lastDeath.world")) return;
        World world = Bukkit.getWorld(data.getString("lastDeath.world", ""));
        if (world == null) return;
        Location loc = new Location(world,
                data.getDouble("lastDeath.x"),
                data.getDouble("lastDeath.y"),
                data.getDouble("lastDeath.z"),
                (float) data.getDouble("lastDeath.yaw"),
                (float) data.getDouble("lastDeath.pitch"));
        deathLocations.put(uuid, loc);
    }

    private void saveInventorySnapshot(Player player) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);

        // Save main inventory (slots 0-35)
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            data.set("inventory.slot_" + i, (i < contents.length && contents[i] != null) ? contents[i] : null);
        }

        // Save armor
        data.set("inventory.helmet", player.getInventory().getHelmet());
        data.set("inventory.chestplate", player.getInventory().getChestplate());
        data.set("inventory.leggings", player.getInventory().getLeggings());
        data.set("inventory.boots", player.getInventory().getBoots());

        // Save offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        data.set("inventory.offhand", offhand.getType() != Material.AIR ? offhand : null);

        plugin.getDataManager().savePlayerData(uuid, data);
    }
}
