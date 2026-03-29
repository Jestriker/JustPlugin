package org.justme.justPlugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.PAPIHook;
import org.justme.justPlugin.util.PlaceholderResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Manages the /stats GUI window. Configuration is loaded from stats.yml.
 */
public class StatsGui implements Listener {

    private final JustPlugin plugin;
    private final File statsFile;
    private YamlConfiguration config;

    public StatsGui(JustPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!statsFile.exists()) {
            plugin.saveResource("stats.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(statsFile);

        // Migrate: add missing keys from default
        InputStream defaultStream = plugin.getResource("stats.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (!defaults.isConfigurationSection(key) && !config.contains(key, true)) {
                    config.set(key, defaults.get(key));
                    changed = true;
                }
            }
            if (changed) {
                try { config.save(statsFile); } catch (IOException ignored) {}
            }
        }
    }

    public void reload() {
        loadConfig();
    }

    /**
     * Open the stats GUI for the given viewer, showing the target player's stats.
     */
    public void open(Player viewer, Player target) {
        int rows = config.getInt("rows", 3);
        int size = rows * 9;

        String rawTitle = config.getString("title", "<dark_gray>{player}'s Stats");
        rawTitle = rawTitle.replace("{player}", target.getName());
        // Resolve placeholders for target
        rawTitle = PlaceholderResolver.resolve(target, plugin, rawTitle);
        rawTitle = PAPIHook.setPlaceholders(target, rawTitle);

        Component title = CC.translate(rawTitle);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Fill empty slots if configured
        boolean fillEmpty = config.getBoolean("fill-empty", true);
        if (fillEmpty) {
            String fillMatName = config.getString("fill-material", "GRAY_STAINED_GLASS_PANE");
            Material fillMat = Material.matchMaterial(fillMatName);
            if (fillMat == null) fillMat = Material.GRAY_STAINED_GLASS_PANE;
            ItemStack filler = new ItemStack(fillMat);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.displayName(Component.empty());
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < size; i++) {
                inv.setItem(i, filler.clone());
            }
        }

        // Place configured items
        List<Map<?, ?>> items = config.getMapList("items");
        for (Map<?, ?> itemMap : items) {
            int x = getInt(itemMap, "slot-x", 1);
            int y = getInt(itemMap, "slot-y", 1);
            int slot = (y - 1) * 9 + (x - 1);
            if (slot < 0 || slot >= size) continue;

            String matName = getString(itemMap, "material", "STONE");
            Material mat = Material.matchMaterial(matName);
            if (mat == null) mat = Material.STONE;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            // Name
            String name = getString(itemMap, "name", "");
            name = resolveAll(name, target);
            meta.displayName(CC.translate(name));

            // Lore
            Object loreObj = itemMap.get("lore");
            if (loreObj instanceof List<?> loreList) {
                List<Component> loreComponents = new ArrayList<>();
                for (Object line : loreList) {
                    String lineStr = resolveAll(String.valueOf(line), target);
                    loreComponents.add(CC.translate(lineStr));
                }
                meta.lore(loreComponents);
            }

            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            inv.setItem(slot, item);
        }

        viewer.openInventory(inv);
    }

    private String resolveAll(String text, Player target) {
        text = PlaceholderResolver.resolve(target, plugin, text);
        text = PAPIHook.setPlaceholders(target, text);
        return text;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Component title = event.getView().title();
        // Check if this is a stats inventory by checking the raw title component
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (plain.contains("'s Stats")) {
            event.setCancelled(true);
        }
    }

    private static int getInt(Map<?, ?> map, String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return def;
    }

    private static String getString(Map<?, ?> map, String key, String def) {
        Object val = map.get(key);
        if (val != null) return String.valueOf(val);
        return def;
    }
}



