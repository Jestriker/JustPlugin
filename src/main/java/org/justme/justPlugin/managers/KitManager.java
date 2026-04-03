package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.SchedulerUtil;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages kit definitions, cooldowns, and claiming logic.
 * Kits are stored in kits.yml. Each kit has a display name, permission,
 * cooldown, enabled flag, status (published/pending/archived), and items.
 */
public class KitManager {

    private final JustPlugin plugin;
    private final File kitsFile;
    private YamlConfiguration kitsConfig;

    // Kit data cached in memory: kitName (lowercase) -> KitData
    private final Map<String, KitData> kits = new ConcurrentHashMap<>();

    // Per-player cooldowns: playerUUID -> (kitName -> claimTimeMillis)
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private SchedulerUtil.CancellableTask cleanupTask;

    // Armor material name suffixes for auto-equip detection
    private static final Set<String> HELMET_SUFFIXES = Set.of("_HELMET", "_CAP");
    private static final Set<String> CHESTPLATE_SUFFIXES = Set.of("_CHESTPLATE", "_TUNIC");
    private static final Set<String> LEGGINGS_SUFFIXES = Set.of("_LEGGINGS", "_PANTS");
    private static final Set<String> BOOTS_SUFFIXES = Set.of("_BOOTS");

    public KitManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        load();
        loadCooldowns();
        startCleanupTask();
    }

    // ==================== Data Classes ====================

    public static class KitData {
        public String name;           // internal name (lowercase)
        public String displayName;    // MiniMessage display name
        public String permission;     // permission node
        public int cooldownSeconds;   // cooldown in seconds between claims
        public boolean enabled;       // whether the kit is enabled
        public String status;         // "published", "pending", "archived"
        public Map<Integer, ItemStack> items; // slot -> item
        public long archiveDate;      // epoch millis when archived (0 if not archived)

        public KitData(String name) {
            this.name = name.toLowerCase();
            this.displayName = name;
            this.permission = "justplugin.kits." + this.name;
            this.cooldownSeconds = 3600; // default 1 hour
            this.enabled = true;
            this.status = "pending";
            this.items = new LinkedHashMap<>();
            this.archiveDate = 0;
        }
    }

    // ==================== Load / Save ====================

    private void load() {
        kits.clear();
        if (!kitsFile.exists()) {
            kitsConfig = new YamlConfiguration();
            save();
            return;
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);

        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        if (kitsSection == null) return;

        for (String key : kitsSection.getKeys(false)) {
            ConfigurationSection sec = kitsSection.getConfigurationSection(key);
            if (sec == null) continue;

            KitData kit = new KitData(key);
            kit.displayName = sec.getString("display-name", key);
            kit.permission = sec.getString("permission", "justplugin.kits." + key);
            kit.cooldownSeconds = sec.getInt("cooldown", 3600);
            kit.enabled = sec.getBoolean("enabled", true);
            kit.status = sec.getString("status", "pending");
            kit.archiveDate = sec.getLong("archive-date", 0);

            // Load items
            ConfigurationSection itemsSec = sec.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String slotStr : itemsSec.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(slotStr);
                        ItemStack item = itemsSec.getItemStack(slotStr);
                        if (item != null) {
                            kit.items.put(slot, item);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            kits.put(key.toLowerCase(), kit);
        }
        plugin.getLogger().info("[Kits] Loaded " + kits.size() + " kit(s).");
    }

    public void save() {
        kitsConfig = new YamlConfiguration();
        for (Map.Entry<String, KitData> entry : kits.entrySet()) {
            KitData kit = entry.getValue();
            String path = "kits." + kit.name;
            kitsConfig.set(path + ".display-name", kit.displayName);
            kitsConfig.set(path + ".permission", kit.permission);
            kitsConfig.set(path + ".cooldown", kit.cooldownSeconds);
            kitsConfig.set(path + ".enabled", kit.enabled);
            kitsConfig.set(path + ".status", kit.status);
            kitsConfig.set(path + ".archive-date", kit.archiveDate);

            for (Map.Entry<Integer, ItemStack> itemEntry : kit.items.entrySet()) {
                kitsConfig.set(path + ".items." + itemEntry.getKey(), itemEntry.getValue());
            }
        }

        // Save cooldowns
        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Map.Entry<String, Long> cd : entry.getValue().entrySet()) {
                kitsConfig.set("cooldowns." + uuid + "." + cd.getKey(), cd.getValue());
            }
        }

        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Kits] Failed to save kits.yml: " + e.getMessage());
        }
    }

    private void loadCooldowns() {
        cooldowns.clear();
        ConfigurationSection cdSection = kitsConfig.getConfigurationSection("cooldowns");
        if (cdSection == null) return;
        for (String uuidStr : cdSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerCd = cdSection.getConfigurationSection(uuidStr);
                if (playerCd == null) continue;
                Map<String, Long> playerMap = new ConcurrentHashMap<>();
                for (String kitName : playerCd.getKeys(false)) {
                    playerMap.put(kitName, playerCd.getLong(kitName, 0));
                }
                cooldowns.put(uuid, playerMap);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // ==================== Cleanup Task ====================

    private void startCleanupTask() {
        // Run every hour (72000 ticks)
        cleanupTask = SchedulerUtil.runTaskTimer(plugin, this::cleanupArchived, 72000L, 72000L);
    }

    private void cleanupArchived() {
        int retentionDays = plugin.getConfig().getInt("kits.archive-retention-days", 30);
        if (retentionDays <= 0) return; // disabled

        long now = System.currentTimeMillis();
        long retentionMs = retentionDays * 86400000L;
        List<String> toRemove = new ArrayList<>();

        for (KitData kit : kits.values()) {
            if ("archived".equals(kit.status) && kit.archiveDate > 0) {
                if (now - kit.archiveDate > retentionMs) {
                    toRemove.add(kit.name);
                }
            }
        }

        for (String name : toRemove) {
            kits.remove(name);
            plugin.getLogger().info("[Kits] Auto-deleted archived kit '" + name + "' (exceeded " + retentionDays + " day retention).");
        }

        if (!toRemove.isEmpty()) {
            save();
        }
    }

    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        save();
    }

    // ==================== Kit CRUD ====================

    /**
     * Create a new kit with the given name and items.
     * @return true if created, false if a kit with that name already exists.
     */
    public boolean createKit(String name, String displayName, Map<Integer, ItemStack> items) {
        String key = name.toLowerCase();
        if (kits.containsKey(key)) return false;

        KitData kit = new KitData(name);
        kit.displayName = displayName;
        kit.items = new LinkedHashMap<>(items);
        kit.status = "pending";
        kits.put(key, kit);
        save();
        return true;
    }

    /**
     * Permanently delete a kit.
     * @return true if deleted, false if not found.
     */
    public boolean deleteKit(String name) {
        String key = name.toLowerCase();
        if (kits.remove(key) != null) {
            save();
            return true;
        }
        return false;
    }

    /**
     * Publish a kit (set status to "published").
     * @return true if published, false if not found.
     */
    public boolean publishKit(String name) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.status = "published";
        kit.archiveDate = 0;
        save();
        return true;
    }

    /**
     * Archive a kit (set status to "archived").
     * @return true if archived, false if not found.
     */
    public boolean archiveKit(String name) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.status = "archived";
        kit.archiveDate = System.currentTimeMillis();
        save();
        return true;
    }

    /**
     * Restore an archived kit to pending status.
     * @return true if restored, false if not found or not archived.
     */
    public boolean restoreKit(String name) {
        KitData kit = getKit(name);
        if (kit == null || !"archived".equals(kit.status)) return false;
        kit.status = "pending";
        kit.archiveDate = 0;
        save();
        return true;
    }

    /**
     * Rename a kit.
     * @return true if renamed, false if old not found or new already exists.
     */
    public boolean renameKit(String oldName, String newName) {
        String oldKey = oldName.toLowerCase();
        String newKey = newName.toLowerCase();
        if (!kits.containsKey(oldKey) || kits.containsKey(newKey)) return false;

        KitData kit = kits.remove(oldKey);
        kit.name = newKey;
        kit.permission = "justplugin.kits." + newKey;
        kits.put(newKey, kit);
        save();
        return true;
    }

    /**
     * Enable a kit.
     */
    public boolean enableKit(String name) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.enabled = true;
        save();
        return true;
    }

    /**
     * Disable a kit.
     */
    public boolean disableKit(String name) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.enabled = false;
        save();
        return true;
    }

    /**
     * Update a kit's items.
     */
    public boolean updateKit(String name, Map<Integer, ItemStack> items) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.items = new LinkedHashMap<>(items);
        save();
        return true;
    }

    /**
     * Update a kit's display name.
     */
    public boolean setDisplayName(String name, String displayName) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.displayName = displayName;
        save();
        return true;
    }

    /**
     * Update a kit's cooldown.
     */
    public boolean setCooldown(String name, int seconds) {
        KitData kit = getKit(name);
        if (kit == null) return false;
        kit.cooldownSeconds = seconds;
        save();
        return true;
    }

    // ==================== Getters ====================

    public KitData getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public Collection<KitData> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

    /**
     * Get all kit names.
     */
    public Set<String> getKitNames() {
        return Collections.unmodifiableSet(kits.keySet());
    }

    /**
     * Get published and enabled kits a player has permission for.
     */
    public List<KitData> getAvailableKits(Player player) {
        List<KitData> result = new ArrayList<>();
        for (KitData kit : kits.values()) {
            if ("published".equals(kit.status) && kit.enabled && player.hasPermission(kit.permission)) {
                result.add(kit);
            }
        }
        return result;
    }

    // ==================== Cooldown ====================

    /**
     * Check if a player can claim a kit (not on cooldown).
     */
    public boolean canClaim(UUID playerUuid, String kitName) {
        KitData kit = getKit(kitName);
        if (kit == null) return false;
        if (kit.cooldownSeconds <= 0) return true;

        Map<String, Long> playerCd = cooldowns.get(playerUuid);
        if (playerCd == null) return true;

        Long lastClaim = playerCd.get(kitName.toLowerCase());
        if (lastClaim == null) return true;

        return System.currentTimeMillis() - lastClaim >= kit.cooldownSeconds * 1000L;
    }

    /**
     * Get remaining cooldown seconds for a player and kit.
     */
    public int getRemainingCooldown(UUID playerUuid, String kitName) {
        KitData kit = getKit(kitName);
        if (kit == null || kit.cooldownSeconds <= 0) return 0;

        Map<String, Long> playerCd = cooldowns.get(playerUuid);
        if (playerCd == null) return 0;

        Long lastClaim = playerCd.get(kitName.toLowerCase());
        if (lastClaim == null) return 0;

        long elapsed = System.currentTimeMillis() - lastClaim;
        long remaining = (kit.cooldownSeconds * 1000L) - elapsed;
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    /**
     * Set the player's cooldown for a kit to now.
     */
    public void setPlayerCooldown(UUID playerUuid, String kitName) {
        cooldowns.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .put(kitName.toLowerCase(), System.currentTimeMillis());
    }

    /**
     * Get a player's last claim time for a kit, or 0 if never claimed.
     */
    public long getPlayerCooldown(UUID playerUuid, String kitName) {
        Map<String, Long> playerCd = cooldowns.get(playerUuid);
        if (playerCd == null) return 0;
        return playerCd.getOrDefault(kitName.toLowerCase(), 0L);
    }

    // ==================== Claiming ====================

    /**
     * Give a kit's items to a player.
     * Auto-equips armor to the correct slots.
     * @return true if claimed successfully, false if kit not found or not claimable.
     */
    public boolean claimKit(Player player, String kitName) {
        KitData kit = getKit(kitName);
        if (kit == null) return false;
        if (!"published".equals(kit.status) || !kit.enabled) return false;

        PlayerInventory inv = player.getInventory();

        for (Map.Entry<Integer, ItemStack> entry : kit.items.entrySet()) {
            ItemStack item = entry.getValue().clone();
            int slot = entry.getKey();

            // Try to auto-equip armor
            if (isHelmet(item.getType()) && isEmpty(inv.getHelmet())) {
                inv.setHelmet(item);
            } else if (isChestplate(item.getType()) && isEmpty(inv.getChestplate())) {
                inv.setChestplate(item);
            } else if (isLeggings(item.getType()) && isEmpty(inv.getLeggings())) {
                inv.setLeggings(item);
            } else if (isBoots(item.getType()) && isEmpty(inv.getBoots())) {
                inv.setBoots(item);
            } else {
                // Try the original slot first, then add to first available
                if (slot >= 0 && slot < 36 && isEmpty(inv.getItem(slot))) {
                    inv.setItem(slot, item);
                } else {
                    // Add to first empty slot, or drop if full
                    Map<Integer, ItemStack> overflow = inv.addItem(item);
                    for (ItemStack leftover : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                }
            }
        }

        // Set cooldown
        setPlayerCooldown(player.getUniqueId(), kitName);
        save();
        return true;
    }

    // ==================== Armor Detection ====================

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private boolean isHelmet(Material mat) {
        String name = mat.name();
        for (String suffix : HELMET_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        return name.equals("CARVED_PUMPKIN") || name.equals("PLAYER_HEAD")
                || name.equals("CREEPER_HEAD") || name.equals("ZOMBIE_HEAD")
                || name.equals("SKELETON_SKULL") || name.equals("WITHER_SKELETON_SKULL")
                || name.equals("DRAGON_HEAD") || name.equals("PIGLIN_HEAD")
                || name.equals("TURTLE_HELMET");
    }

    private boolean isChestplate(Material mat) {
        String name = mat.name();
        for (String suffix : CHESTPLATE_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        return name.equals("ELYTRA");
    }

    private boolean isLeggings(Material mat) {
        String name = mat.name();
        for (String suffix : LEGGINGS_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        return false;
    }

    private boolean isBoots(Material mat) {
        String name = mat.name();
        for (String suffix : BOOTS_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        return false;
    }

    /**
     * Get a summary of a kit's contents for lore display.
     * Returns a list of item descriptions (e.g., "Diamond Sword x1").
     */
    public List<String> getContentsSummary(KitData kit, int maxLines) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> entry : kit.items.entrySet()) {
            ItemStack item = entry.getValue();
            String name = formatMaterialName(item.getType());
            int amount = item.getAmount();
            lines.add("<gray>" + name + (amount > 1 ? " x" + amount : ""));
            if (lines.size() >= maxLines) {
                int remaining = kit.items.size() - maxLines;
                if (remaining > 0) {
                    lines.add("<dark_gray>...and " + remaining + " more item(s)");
                }
                break;
            }
        }
        if (lines.isEmpty()) {
            lines.add("<dark_gray>Empty kit");
        }
        return lines;
    }

    private String formatMaterialName(Material mat) {
        String name = mat.name().toLowerCase().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return sb.toString().trim();
    }
}
