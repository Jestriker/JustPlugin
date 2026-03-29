package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.CachedServerIcon;
import org.justme.justPlugin.JustPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;

/**
 * Manages the server icon from a configurable URL.
 * <p>
 * Configuration lives in icon.yml:
 * <ul>
 *   <li><b>enabled</b> - Whether to use a custom server icon</li>
 *   <li><b>url</b> - Direct URL to a 64x64 PNG image</li>
 * </ul>
 * <p>
 * The image is fetched asynchronously on startup and cached locally.
 * The icon is applied to the server list ping event.
 */
public class IconManager {

    private final JustPlugin plugin;
    private final File configFile;
    private final File cacheFile;
    private YamlConfiguration config;

    private boolean enabled;
    private String iconUrl;
    private CachedServerIcon cachedIcon;

    public IconManager(JustPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "icon.yml");
        this.cacheFile = new File(plugin.getDataFolder(), "server-icon-cache.png");
        load();
    }

    private void load() {
        if (!configFile.exists()) {
            // Save default from resources
            try (InputStream in = plugin.getResource("icon.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("[Icon] Failed to save default icon.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        enabled = config.getBoolean("enabled", false);
        iconUrl = config.getString("url", "");

        if (enabled && iconUrl != null && !iconUrl.isEmpty()) {
            // Try loading from cache first
            if (cacheFile.exists()) {
                loadFromCache();
            }
            // Fetch from URL asynchronously (will update cache)
            fetchAsync();
        }
    }

    private void loadFromCache() {
        try {
            BufferedImage image = ImageIO.read(cacheFile);
            if (image != null && image.getWidth() == 64 && image.getHeight() == 64) {
                cachedIcon = Bukkit.loadServerIcon(image);
                plugin.getLogger().info("[Icon] Loaded server icon from cache.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Icon] Failed to load cached icon: " + e.getMessage());
        }
    }

    private void fetchAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URI uri = URI.create(iconUrl);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("User-Agent", "JustPlugin");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("[Icon] Failed to fetch icon from URL (HTTP " + responseCode + "): " + iconUrl);
                    return;
                }

                BufferedImage image = ImageIO.read(conn.getInputStream());
                conn.disconnect();

                if (image == null) {
                    plugin.getLogger().warning("[Icon] Could not read image from URL: " + iconUrl);
                    return;
                }

                // Resize to 64x64 if needed
                if (image.getWidth() != 64 || image.getHeight() != 64) {
                    BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g = resized.createGraphics();
                    g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(image, 0, 0, 64, 64, null);
                    g.dispose();
                    image = resized;
                }

                // Save to cache
                ImageIO.write(image, "PNG", cacheFile);

                // Load the icon on the main thread (Bukkit requires main thread for loadServerIcon)
                final BufferedImage finalImage = image;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        cachedIcon = Bukkit.loadServerIcon(finalImage);
                        plugin.getLogger().info("[Icon] Server icon loaded from URL and cached.");
                    } catch (Exception e) {
                        plugin.getLogger().warning("[Icon] Failed to create server icon: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                plugin.getLogger().warning("[Icon] Failed to fetch icon from URL: " + e.getMessage());
            }
        });
    }

    /** Returns the cached server icon, or null if not configured/loaded. */
    public CachedServerIcon getCachedIcon() {
        return enabled ? cachedIcon : null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Reload config and re-fetch icon. */
    public void reload() {
        cachedIcon = null;
        load();
    }
}

