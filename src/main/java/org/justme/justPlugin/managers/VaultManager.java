package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VaultManager implements Listener {

    private final JustPlugin plugin;
    private final DataManager dataManager;
    private final int maxVaults;

    // Track open vaults: player UUID -> vault number
    private final ConcurrentHashMap<UUID, Integer> openVaults = new ConcurrentHashMap<>();
    // Track who owns the vault being viewed (for staff viewing): viewer UUID -> target UUID
    private final ConcurrentHashMap<UUID, UUID> vaultOwners = new ConcurrentHashMap<>();

    public VaultManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.maxVaults = plugin.getConfig().getInt("vaults.max-vaults", 3);
    }

    /**
     * Opens a player's own vault.
     */
    public void openVault(Player player, int vaultNumber) {
        openVault(player, player, vaultNumber);
    }

    /**
     * Opens a vault for viewing. If viewer != owner, it's a staff view.
     */
    public void openVault(Player viewer, Player owner, int vaultNumber) {
        UUID ownerUuid = owner.getUniqueId();
        boolean isStaff = !viewer.getUniqueId().equals(ownerUuid);

        String title = isStaff
                ? owner.getName() + "'s Vault #" + vaultNumber
                : "Vault #" + vaultNumber;

        Inventory vault = Bukkit.createInventory(null, 54, CC.translate(title));

        // Load contents from player data
        YamlConfiguration data = dataManager.getPlayerData(ownerUuid);
        String path = "vaults." + vaultNumber + ".contents";

        if (data.contains(path)) {
            @SuppressWarnings("unchecked")
            List<ItemStack> items = (List<ItemStack>) data.getList(path);
            if (items != null) {
                for (int i = 0; i < items.size() && i < 54; i++) {
                    vault.setItem(i, items.get(i));
                }
            }
        }

        // Track the open vault
        openVaults.put(viewer.getUniqueId(), vaultNumber);
        vaultOwners.put(viewer.getUniqueId(), ownerUuid);

        viewer.openInventory(vault);
    }

    /**
     * Returns the maximum number of vaults a player can use.
     * Checks for permission-based overrides (justplugin.vaults.<number>).
     */
    public int getMaxVaults(Player player) {
        // Check for permission-based overrides, scanning from high to low
        for (int i = 54; i >= 1; i--) {
            if (player.hasPermission("justplugin.vaults." + i)) {
                return i;
            }
        }
        return maxVaults;
    }

    /**
     * Returns whether the given vault number exists (has been used) for a player.
     */
    public boolean hasVault(Player player, int number) {
        YamlConfiguration data = dataManager.getPlayerData(player.getUniqueId());
        return data.contains("vaults." + number + ".contents");
    }

    /**
     * Saves vault contents to player data.
     */
    public void saveVault(UUID ownerUuid, int number, Inventory inventory) {
        YamlConfiguration data = dataManager.getPlayerData(ownerUuid);
        String path = "vaults." + number + ".contents";

        List<ItemStack> contents = new ArrayList<>(Arrays.asList(inventory.getContents()));
        data.set(path, contents);

        dataManager.savePlayerDataAsync(ownerUuid, data);
    }

    /**
     * Saves all currently open vaults. Called during auto-save and plugin disable.
     */
    public void saveAllVaults() {
        for (Map.Entry<UUID, Integer> entry : openVaults.entrySet()) {
            UUID viewerUuid = entry.getKey();
            int vaultNumber = entry.getValue();
            UUID ownerUuid = vaultOwners.getOrDefault(viewerUuid, viewerUuid);

            Player viewer = Bukkit.getPlayer(viewerUuid);
            if (viewer != null && viewer.isOnline()) {
                saveVault(ownerUuid, vaultNumber, viewer.getOpenInventory().getTopInventory());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        UUID viewerUuid = player.getUniqueId();
        Integer vaultNumber = openVaults.remove(viewerUuid);
        if (vaultNumber == null) return;

        UUID ownerUuid = vaultOwners.remove(viewerUuid);
        if (ownerUuid == null) ownerUuid = viewerUuid;

        saveVault(ownerUuid, vaultNumber, event.getInventory());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID viewerUuid = event.getPlayer().getUniqueId();
        Integer vaultNumber = openVaults.remove(viewerUuid);
        if (vaultNumber == null) return;

        UUID ownerUuid = vaultOwners.remove(viewerUuid);
        if (ownerUuid == null) ownerUuid = viewerUuid;

        saveVault(ownerUuid, vaultNumber, event.getPlayer().getOpenInventory().getTopInventory());
    }
}
