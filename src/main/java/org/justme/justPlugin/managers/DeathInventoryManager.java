package org.justme.justPlugin.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.justme.justPlugin.JustPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records player inventories on death when items are dropped (keepInventory=false).
 * Does NOT record when keepInventory keeps items.
 */
public class DeathInventoryManager {

    private final JustPlugin plugin;
    // In-memory cache: UUID -> death inventory snapshot
    private final Map<UUID, ItemStack[]> deathInventories = new ConcurrentHashMap<>();
    // Tracks whether the last death had dropped items
    private final Map<UUID, Boolean> deathHadDrops = new ConcurrentHashMap<>();

    public DeathInventoryManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called on PlayerDeathEvent. Only records if keepInventory is false and items were dropped.
     */
    public void recordDeath(Player player, boolean keepInventory, boolean hadDrops) {
        UUID uuid = player.getUniqueId();

        if (keepInventory || !hadDrops) {
            // Don't record if items weren't dropped
            return;
        }

        // Snapshot the full inventory before items are removed
        ItemStack[] snapshot = new ItemStack[41]; // 36 inv + 4 armor + 1 offhand
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < Math.min(36, contents.length); i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                snapshot[i] = contents[i].clone();
            }
        }
        // Armor: slots 36-39
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && armor[i].getType() != Material.AIR) {
                snapshot[36 + i] = armor[i].clone();
            }
        }
        // Offhand: slot 40
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() != Material.AIR) {
            snapshot[40] = offhand.clone();
        }

        deathInventories.put(uuid, snapshot);
        deathHadDrops.put(uuid, true);

        // Persist to player data
        saveDeathInventory(uuid, snapshot);
    }

    /**
     * Record the inventory BEFORE the death event clears it.
     * Call this at the BEGINNING of the death event.
     */
    public void preRecordInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] snapshot = new ItemStack[41];
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < Math.min(36, contents.length); i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                snapshot[i] = contents[i].clone();
            }
        }
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && armor[i].getType() != Material.AIR) {
                snapshot[36 + i] = armor[i].clone();
            }
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() != Material.AIR) {
            snapshot[40] = offhand.clone();
        }
        deathInventories.put(uuid, snapshot);
    }

    /**
     * Finalize the recording. Call after death event — only keeps it if drops happened.
     */
    public void finalizeRecording(UUID uuid, boolean keepInventory, boolean hadDrops) {
        if (keepInventory || !hadDrops) {
            deathInventories.remove(uuid);
            deathHadDrops.remove(uuid);
            return;
        }
        deathHadDrops.put(uuid, true);
        ItemStack[] snapshot = deathInventories.get(uuid);
        if (snapshot != null) {
            saveDeathInventory(uuid, snapshot);
        }
    }

    public boolean hasDeathInventory(UUID uuid) {
        return deathInventories.containsKey(uuid) && deathHadDrops.getOrDefault(uuid, false);
    }

    public ItemStack[] getDeathInventory(UUID uuid) {
        return deathInventories.get(uuid);
    }

    /**
     * Give the death items back to a player. Items that don't fit are dropped.
     */
    public void restoreDeathItems(Player target) {
        UUID uuid = target.getUniqueId();
        ItemStack[] snapshot = deathInventories.get(uuid);
        if (snapshot == null) return;

        // Give main inventory items
        for (int i = 0; i < 36; i++) {
            if (snapshot[i] != null) {
                Map<Integer, ItemStack> leftover = target.getInventory().addItem(snapshot[i].clone());
                for (ItemStack drop : leftover.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), drop);
                }
            }
        }
        // Give armor
        for (int i = 36; i < 40; i++) {
            if (snapshot[i] != null) {
                int armorSlot = i - 36;
                ItemStack current = target.getInventory().getArmorContents()[armorSlot];
                if (current == null || current.getType() == Material.AIR) {
                    ItemStack[] armorContents = target.getInventory().getArmorContents();
                    armorContents[armorSlot] = snapshot[i].clone();
                    target.getInventory().setArmorContents(armorContents);
                } else {
                    Map<Integer, ItemStack> leftover = target.getInventory().addItem(snapshot[i].clone());
                    for (ItemStack drop : leftover.values()) {
                        target.getWorld().dropItemNaturally(target.getLocation(), drop);
                    }
                }
            }
        }
        // Give offhand
        if (snapshot[40] != null) {
            ItemStack currentOff = target.getInventory().getItemInOffHand();
            if (currentOff.getType() == Material.AIR) {
                target.getInventory().setItemInOffHand(snapshot[40].clone());
            } else {
                Map<Integer, ItemStack> leftover = target.getInventory().addItem(snapshot[40].clone());
                for (ItemStack drop : leftover.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), drop);
                }
            }
        }

        // Clear the record after restoring
        deathInventories.remove(uuid);
        deathHadDrops.remove(uuid);
        clearDeathInventoryData(uuid);
    }

    // --- Persistence ---

    private void saveDeathInventory(UUID uuid, ItemStack[] snapshot) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        for (int i = 0; i < snapshot.length; i++) {
            data.set("deathInventory.slot_" + i, snapshot[i]);
        }
        data.set("deathInventory.recorded", true);
        plugin.getDataManager().savePlayerData(uuid, data);
    }

    private void clearDeathInventoryData(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        data.set("deathInventory", null);
        plugin.getDataManager().savePlayerData(uuid, data);
    }

    public void loadDeathInventory(UUID uuid) {
        YamlConfiguration data = plugin.getDataManager().getPlayerData(uuid);
        if (!data.getBoolean("deathInventory.recorded", false)) return;

        ItemStack[] snapshot = new ItemStack[41];
        for (int i = 0; i < 41; i++) {
            snapshot[i] = data.getItemStack("deathInventory.slot_" + i, null);
        }
        deathInventories.put(uuid, snapshot);
        deathHadDrops.put(uuid, true);
    }
}

