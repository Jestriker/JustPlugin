package org.justme.justPlugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.justme.justPlugin.JustPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages per-command settings: enable/disable toggle and configurable permissions.
 * All settings are loaded from config.yml under the "command-settings" section.
 */
public class CommandSettings {

    private final JustPlugin plugin;
    private final Map<String, Boolean> enabledMap = new HashMap<>();
    private final Map<String, String> permissionMap = new HashMap<>();

    // Default permissions for every command (acts as constants)
    private static final Map<String, String> DEFAULT_PERMISSIONS = new HashMap<>();

    static {
        // Teleportation
        DEFAULT_PERMISSIONS.put("tpa", "justplugin.tpa");
        DEFAULT_PERMISSIONS.put("tpaccept", "");
        DEFAULT_PERMISSIONS.put("tpacancel", "");
        DEFAULT_PERMISSIONS.put("tpreject", "");
        DEFAULT_PERMISSIONS.put("tppos", "justplugin.tppos");
        DEFAULT_PERMISSIONS.put("tpr", "justplugin.wild");
        DEFAULT_PERMISSIONS.put("tpahere", "justplugin.tpahere");
        DEFAULT_PERMISSIONS.put("back", "justplugin.back");
        DEFAULT_PERMISSIONS.put("spawn", "justplugin.spawn");
        DEFAULT_PERMISSIONS.put("setspawn", "justplugin.setspawn");

        // Warps
        DEFAULT_PERMISSIONS.put("warp", "justplugin.warp");
        DEFAULT_PERMISSIONS.put("warps", "justplugin.warp");
        DEFAULT_PERMISSIONS.put("setwarp", "justplugin.setwarp");
        DEFAULT_PERMISSIONS.put("delwarp", "justplugin.delwarp");
        DEFAULT_PERMISSIONS.put("renamewarp", "justplugin.renamewarp");

        // Homes
        DEFAULT_PERMISSIONS.put("home", "justplugin.home");
        DEFAULT_PERMISSIONS.put("sethome", "justplugin.sethome");
        DEFAULT_PERMISSIONS.put("delhome", "justplugin.delhome");

        // Economy
        DEFAULT_PERMISSIONS.put("balance", "justplugin.balance");
        DEFAULT_PERMISSIONS.put("pay", "justplugin.pay");
        DEFAULT_PERMISSIONS.put("paytoggle", "justplugin.paytoggle");
        DEFAULT_PERMISSIONS.put("paynote", "justplugin.paynote");
        DEFAULT_PERMISSIONS.put("addcash", "justplugin.addcash");

        // Moderation
        DEFAULT_PERMISSIONS.put("ban", "justplugin.ban");
        DEFAULT_PERMISSIONS.put("banip", "justplugin.banip");
        DEFAULT_PERMISSIONS.put("tempban", "justplugin.tempban");
        DEFAULT_PERMISSIONS.put("tempbanip", "justplugin.tempbanip");
        DEFAULT_PERMISSIONS.put("unban", "justplugin.unban");
        DEFAULT_PERMISSIONS.put("unbanip", "justplugin.unbanip");
        DEFAULT_PERMISSIONS.put("vanish", "justplugin.vanish");
        DEFAULT_PERMISSIONS.put("sudo", "justplugin.sudo");
        DEFAULT_PERMISSIONS.put("invsee", "justplugin.invsee");
        DEFAULT_PERMISSIONS.put("echestsee", "justplugin.echestsee");

        // Player
        DEFAULT_PERMISSIONS.put("fly", "justplugin.fly");
        DEFAULT_PERMISSIONS.put("gm", "justplugin.gamemode");
        DEFAULT_PERMISSIONS.put("gmc", "justplugin.gamemode");
        DEFAULT_PERMISSIONS.put("gms", "justplugin.gamemode");
        DEFAULT_PERMISSIONS.put("gma", "justplugin.gamemode");
        DEFAULT_PERMISSIONS.put("gmsp", "justplugin.gamemode");
        DEFAULT_PERMISSIONS.put("god", "justplugin.god");
        DEFAULT_PERMISSIONS.put("speed", "justplugin.speed");
        DEFAULT_PERMISSIONS.put("flyspeed", "justplugin.speed");
        DEFAULT_PERMISSIONS.put("walkspeed", "justplugin.speed");
        DEFAULT_PERMISSIONS.put("hat", "justplugin.hat");
        DEFAULT_PERMISSIONS.put("exp", "justplugin.exp");
        DEFAULT_PERMISSIONS.put("skull", "justplugin.skull");
        DEFAULT_PERMISSIONS.put("suicide", "justplugin.suicide");
        DEFAULT_PERMISSIONS.put("heal", "justplugin.heal");
        DEFAULT_PERMISSIONS.put("feed", "justplugin.feed");
        DEFAULT_PERMISSIONS.put("kill", "justplugin.kill");
        DEFAULT_PERMISSIONS.put("getpos", "");
        DEFAULT_PERMISSIONS.put("getdeathpos", "");

        // Chat
        DEFAULT_PERMISSIONS.put("msg", "justplugin.msg");
        DEFAULT_PERMISSIONS.put("r", "justplugin.msg");
        DEFAULT_PERMISSIONS.put("ignore", "justplugin.ignore");
        DEFAULT_PERMISSIONS.put("announce", "justplugin.announce");
        DEFAULT_PERMISSIONS.put("sharecoords", "justplugin.sharecoords");
        DEFAULT_PERMISSIONS.put("sharedeathcoords", "justplugin.sharedeathcoords");
        DEFAULT_PERMISSIONS.put("chat", "justplugin.chat");
        DEFAULT_PERMISSIONS.put("teammsg", "justplugin.chat");

        // Virtual Inventories
        DEFAULT_PERMISSIONS.put("anvil", "justplugin.anvil");
        DEFAULT_PERMISSIONS.put("grindstone", "justplugin.grindstone");
        DEFAULT_PERMISSIONS.put("enderchest", "justplugin.enderchest");
        DEFAULT_PERMISSIONS.put("craft", "justplugin.craft");
        DEFAULT_PERMISSIONS.put("stonecutter", "justplugin.stonecutter");
        DEFAULT_PERMISSIONS.put("loom", "justplugin.loom");
        DEFAULT_PERMISSIONS.put("smithingtable", "justplugin.smithingtable");
        DEFAULT_PERMISSIONS.put("enchantingtable", "justplugin.enchantingtable");

        // Info
        DEFAULT_PERMISSIONS.put("jpinfo", "justplugin.info");
        DEFAULT_PERMISSIONS.put("jphelp", "justplugin.help");
        DEFAULT_PERMISSIONS.put("playerinfo", "justplugin.playerinfo");
        DEFAULT_PERMISSIONS.put("plist", "justplugin.list");
        DEFAULT_PERMISSIONS.put("motd", "justplugin.motd");
        DEFAULT_PERMISSIONS.put("resetmotd", "justplugin.motd.set");
        DEFAULT_PERMISSIONS.put("clock", "justplugin.clock");
        DEFAULT_PERMISSIONS.put("date", "justplugin.date");

        // Items
        DEFAULT_PERMISSIONS.put("itemname", "justplugin.itemname");
        DEFAULT_PERMISSIONS.put("shareitem", "justplugin.shareitem");
        DEFAULT_PERMISSIONS.put("setspawner", "justplugin.setspawner");

        // World
        DEFAULT_PERMISSIONS.put("weather", "justplugin.weather");
        DEFAULT_PERMISSIONS.put("time", "justplugin.time");

        // Team
        DEFAULT_PERMISSIONS.put("team", "justplugin.team");

        // Misc
        DEFAULT_PERMISSIONS.put("trade", "justplugin.trade");
        DEFAULT_PERMISSIONS.put("discord", "");
        DEFAULT_PERMISSIONS.put("tab", "justplugin.tab");

        // Overrides
        DEFAULT_PERMISSIONS.put("help", "");
        DEFAULT_PERMISSIONS.put("plugins", "");
    }

    public CommandSettings(JustPlugin plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    private void loadSettings() {
        enabledMap.clear();
        permissionMap.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("command-settings");
        if (section == null) {
            // Write defaults on first load
            writeDefaults();
            section = plugin.getConfig().getConfigurationSection("command-settings");
        }

        if (section != null) {
            for (String cmd : section.getKeys(false)) {
                ConfigurationSection cs = section.getConfigurationSection(cmd);
                if (cs != null) {
                    enabledMap.put(cmd, cs.getBoolean("enabled", true));
                    String defaultPerm = DEFAULT_PERMISSIONS.getOrDefault(cmd, "justplugin." + cmd);
                    permissionMap.put(cmd, cs.getString("permission", defaultPerm));
                }
            }
        }

        // Ensure all commands have entries (for any not in config)
        for (Map.Entry<String, String> entry : DEFAULT_PERMISSIONS.entrySet()) {
            enabledMap.putIfAbsent(entry.getKey(), true);
            permissionMap.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private void writeDefaults() {
        for (Map.Entry<String, String> entry : DEFAULT_PERMISSIONS.entrySet()) {
            String path = "command-settings." + entry.getKey();
            if (!plugin.getConfig().contains(path)) {
                plugin.getConfig().set(path + ".enabled", true);
                plugin.getConfig().set(path + ".permission", entry.getValue());
            }
        }
        plugin.saveConfig();
    }

    /**
     * Check if a command is enabled in config.
     */
    public boolean isEnabled(String command) {
        return enabledMap.getOrDefault(command, true);
    }

    /**
     * Get the configured permission node for a command.
     */
    public String getPermission(String command) {
        return permissionMap.getOrDefault(command, DEFAULT_PERMISSIONS.getOrDefault(command, "justplugin." + command));
    }

    /**
     * Get the default permission for a command.
     */
    public static String getDefaultPermission(String command) {
        return DEFAULT_PERMISSIONS.getOrDefault(command, "justplugin." + command);
    }

    public void reload() {
        plugin.reloadConfig();
        loadSettings();
    }
}

