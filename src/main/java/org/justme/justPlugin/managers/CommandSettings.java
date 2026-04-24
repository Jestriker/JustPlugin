package org.justme.justPlugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.justme.justPlugin.JustPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages per-command enable/disable toggles.
 * <p>
 * All settings are loaded from config.yml under the "command-settings" section.
 * Only the "enabled" flag is configurable - permissions are fixed and defined
 * in plugin.yml to ensure consistency with documentation and the permissions wiki.
 * <p>
 * Permissions are NOT editable in config. They are hardcoded constants that
 * match plugin.yml exactly. This prevents desync between config, plugin.yml,
 * and the online permissions reference.
 */
public class CommandSettings {

    private final JustPlugin plugin;
    private final Map<String, Boolean> enabledMap = new HashMap<>();

    // Fixed permissions for every command (immutable constants matching plugin.yml)
    private static final Map<String, String> PERMISSIONS = new HashMap<>();

    // Commands that should be disabled by default
    private static final Set<String> DISABLED_BY_DEFAULT = Set.of("rank", "skin", "skinban", "skinunban");

    static {
        // Teleportation
        PERMISSIONS.put("tpa", "justplugin.tpa");
        PERMISSIONS.put("tpaccept", "justplugin.tpaccept");
        PERMISSIONS.put("tpacancel", "justplugin.tpacancel");
        PERMISSIONS.put("tpreject", "justplugin.tpreject");
        PERMISSIONS.put("tppos", "justplugin.tppos");
        PERMISSIONS.put("tpr", "justplugin.wild");
        PERMISSIONS.put("tpahere", "justplugin.tpahere");
        PERMISSIONS.put("back", "justplugin.back");
        PERMISSIONS.put("spawn", "justplugin.spawn");
        PERMISSIONS.put("setspawn", "justplugin.setspawn");
        PERMISSIONS.put("tpsafecheck", "justplugin.tppos");
        PERMISSIONS.put("tpunsafeconfirm", "justplugin.tppos");

        // Warps
        PERMISSIONS.put("warp", "justplugin.warp");
        PERMISSIONS.put("warps", "justplugin.warp");
        PERMISSIONS.put("setwarp", "justplugin.setwarp");
        PERMISSIONS.put("delwarp", "justplugin.delwarp");
        PERMISSIONS.put("renamewarp", "justplugin.renamewarp");

        // Homes
        PERMISSIONS.put("home", "justplugin.home");
        PERMISSIONS.put("sethome", "justplugin.sethome");
        PERMISSIONS.put("delhome", "justplugin.delhome");

        // Economy
        PERMISSIONS.put("balance", "justplugin.balance");
        PERMISSIONS.put("pay", "justplugin.pay");
        PERMISSIONS.put("paytoggle", "justplugin.paytoggle");
        PERMISSIONS.put("paynote", "justplugin.paynote");
        PERMISSIONS.put("addcash", "justplugin.addcash");
        PERMISSIONS.put("baltop", "justplugin.balance");
        PERMISSIONS.put("baltophide", "justplugin.baltophide");
        PERMISSIONS.put("transactions", "justplugin.transactions");

        // Moderation
        PERMISSIONS.put("ban", "justplugin.ban");
        PERMISSIONS.put("banip", "justplugin.banip");
        PERMISSIONS.put("tempban", "justplugin.tempban");
        PERMISSIONS.put("tempbanip", "justplugin.tempbanip");
        PERMISSIONS.put("unban", "justplugin.unban");
        PERMISSIONS.put("unbanip", "justplugin.unbanip");
        PERMISSIONS.put("vanish", "justplugin.vanish");
        PERMISSIONS.put("supervanish", "justplugin.supervanish");
        PERMISSIONS.put("sudo", "justplugin.sudo");
        PERMISSIONS.put("invsee", "justplugin.invsee");
        PERMISSIONS.put("echestsee", "justplugin.echestsee");
        PERMISSIONS.put("mute", "justplugin.mute");
        PERMISSIONS.put("tempmute", "justplugin.tempmute");
        PERMISSIONS.put("unmute", "justplugin.unmute");
        PERMISSIONS.put("warn", "justplugin.warn");
        PERMISSIONS.put("kick", "justplugin.kick");
        PERMISSIONS.put("setlogswebhook", "justplugin.setlogswebhook");
        PERMISSIONS.put("deathitems", "justplugin.deathitems");
        PERMISSIONS.put("oplist", "justplugin.oplist");
        PERMISSIONS.put("banlist", "justplugin.banlist");
        PERMISSIONS.put("baniplist", "justplugin.banlist");
        PERMISSIONS.put("permissions", "justplugin.permissions");

        // Player
        PERMISSIONS.put("fly", "justplugin.fly");
        PERMISSIONS.put("gm", "justplugin.gamemode");
        PERMISSIONS.put("gmc", "justplugin.gamemode");
        PERMISSIONS.put("gms", "justplugin.gamemode");
        PERMISSIONS.put("gma", "justplugin.gamemode");
        PERMISSIONS.put("gmsp", "justplugin.gamemode");
        PERMISSIONS.put("gmcheck", "justplugin.gmcheck");
        PERMISSIONS.put("god", "justplugin.god");
        PERMISSIONS.put("speed", "justplugin.speed");
        PERMISSIONS.put("flyspeed", "justplugin.speed");
        PERMISSIONS.put("walkspeed", "justplugin.speed");
        PERMISSIONS.put("hat", "justplugin.hat");
        PERMISSIONS.put("exp", "justplugin.exp");
        PERMISSIONS.put("skull", "justplugin.skull");
        PERMISSIONS.put("suicide", "justplugin.suicide");
        PERMISSIONS.put("heal", "justplugin.heal");
        PERMISSIONS.put("feed", "justplugin.feed");
        PERMISSIONS.put("kill", "justplugin.kill");
        PERMISSIONS.put("getpos", "justplugin.getpos");
        PERMISSIONS.put("getdeathpos", "justplugin.getdeathpos");

        // Chat
        PERMISSIONS.put("msg", "justplugin.msg");
        PERMISSIONS.put("r", "justplugin.msg");
        PERMISSIONS.put("ignore", "justplugin.ignore");
        PERMISSIONS.put("announce", "justplugin.announce");
        PERMISSIONS.put("sharecoords", "justplugin.sharecoords");
        PERMISSIONS.put("sharedeathcoords", "justplugin.sharedeathcoords");
        PERMISSIONS.put("chat", "justplugin.chat");
        PERMISSIONS.put("teammsg", "justplugin.chat");
        PERMISSIONS.put("clearchat", "justplugin.clearchat");

        // Virtual Inventories
        PERMISSIONS.put("anvil", "justplugin.anvil");
        PERMISSIONS.put("grindstone", "justplugin.grindstone");
        PERMISSIONS.put("enderchest", "justplugin.enderchest");
        PERMISSIONS.put("craft", "justplugin.craft");
        PERMISSIONS.put("stonecutter", "justplugin.stonecutter");
        PERMISSIONS.put("loom", "justplugin.loom");
        PERMISSIONS.put("smithingtable", "justplugin.smithingtable");
        PERMISSIONS.put("enchantingtable", "justplugin.enchantingtable");

        // Info
        PERMISSIONS.put("jpinfo", "justplugin.info");
        PERMISSIONS.put("jphelp", "justplugin.help");
        PERMISSIONS.put("playerinfo", "justplugin.playerinfo");
        PERMISSIONS.put("plist", "justplugin.list");
        PERMISSIONS.put("playerlist", "justplugin.playerlist");
        PERMISSIONS.put("playerlisthide", "justplugin.playerlist.hide");
        PERMISSIONS.put("motd", "justplugin.motd");
        PERMISSIONS.put("resetmotd", "justplugin.motd.set");
        PERMISSIONS.put("clock", "justplugin.clock");
        PERMISSIONS.put("date", "justplugin.date");

        // Items
        PERMISSIONS.put("itemname", "justplugin.itemname");
        PERMISSIONS.put("shareitem", "justplugin.shareitem");
        PERMISSIONS.put("setspawner", "justplugin.setspawner");

        // World
        PERMISSIONS.put("weather", "justplugin.weather");
        PERMISSIONS.put("time", "justplugin.time");
        PERMISSIONS.put("freezegame", "justplugin.freezegame");
        PERMISSIONS.put("unfreezegame", "justplugin.unfreezegame");
        PERMISSIONS.put("clearentities", "justplugin.clearentities");
        PERMISSIONS.put("friendlyfire", "justplugin.friendlyfire");

        // Team
        PERMISSIONS.put("team", "justplugin.team");

        // Misc
        PERMISSIONS.put("trade", "justplugin.trade");
        PERMISSIONS.put("discord", "justplugin.discord");
        PERMISSIONS.put("applyedits", "justplugin.applyedits");
        PERMISSIONS.put("tab", "justplugin.tab");
        PERMISSIONS.put("plugins", "justplugin.plugins");
        PERMISSIONS.put("reloadscoreboard", "justplugin.scoreboard.reload");
        PERMISSIONS.put("stats", "justplugin.stats");
        PERMISSIONS.put("maintenance", "justplugin.maintenance");
        PERMISSIONS.put("skin", "justplugin.skin");
        PERMISSIONS.put("skinban", "justplugin.skinban");
        PERMISSIONS.put("skinunban", "justplugin.skinunban");

        // Kits
        PERMISSIONS.put("kit", "justplugin.kit");
        PERMISSIONS.put("kitpreview", "justplugin.kit.preview");
        PERMISSIONS.put("kitcreate", "justplugin.kit.create");
        PERMISSIONS.put("kitedit", "justplugin.kit.edit");
        PERMISSIONS.put("kitrename", "justplugin.kit.rename");
        PERMISSIONS.put("kitdelete", "justplugin.kit.delete");
        PERMISSIONS.put("kitpublish", "justplugin.kit.publish");
        PERMISSIONS.put("kitdisable", "justplugin.kit.disable");
        PERMISSIONS.put("kitenable", "justplugin.kit.disable");
        PERMISSIONS.put("kitarchive", "justplugin.kit.archive");
        PERMISSIONS.put("kitlist", "justplugin.kit.list");

        // Overrides
        PERMISSIONS.put("help", "justplugin.help");
        PERMISSIONS.put("rank", "justplugin.rank");
    }

    public CommandSettings(JustPlugin plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    private void loadSettings() {
        enabledMap.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("command-settings");
        if (section == null) {
            writeDefaults();
            section = plugin.getConfig().getConfigurationSection("command-settings");
        }

        if (section != null) {
            for (String cmd : section.getKeys(false)) {
                // Support both "command-settings.cmd.enabled: true" and "command-settings.cmd: true"
                if (section.isConfigurationSection(cmd)) {
                    enabledMap.put(cmd, section.getConfigurationSection(cmd).getBoolean("enabled", true));
                } else if (section.isBoolean(cmd)) {
                    enabledMap.put(cmd, section.getBoolean(cmd, true));
                }
            }
        }

        // Migrate old config format: strip out any "permission" keys (they are no longer used)
        migrateRemovePermissionKeys(section);

        // Ensure all known commands have entries
        for (String cmd : PERMISSIONS.keySet()) {
            boolean defaultEnabled = !DISABLED_BY_DEFAULT.contains(cmd);
            enabledMap.putIfAbsent(cmd, defaultEnabled);
        }
    }

    /**
     * Removes any leftover "permission" keys from command-settings entries.
     * Permissions are no longer configurable - they are fixed in plugin.yml.
     */
    private void migrateRemovePermissionKeys(ConfigurationSection section) {
        if (section == null) return;
        boolean changed = false;
        for (String cmd : section.getKeys(false)) {
            if (section.isConfigurationSection(cmd)) {
                ConfigurationSection cs = section.getConfigurationSection(cmd);
                if (cs != null && cs.contains("permission")) {
                    cs.set("permission", null);
                    changed = true;
                }
                if (cs != null && cs.contains("reload-permission")) {
                    cs.set("reload-permission", null);
                    changed = true;
                }
            }
        }
        if (changed) {
            plugin.saveConfig();
            plugin.getLogger().info("[Config Migration] Removed 'permission' keys from command-settings (permissions are now fixed in plugin.yml).");
        }
    }

    private void writeDefaults() {
        for (String cmd : PERMISSIONS.keySet()) {
            String path = "command-settings." + cmd;
            if (!plugin.getConfig().contains(path)) {
                boolean defaultEnabled = !DISABLED_BY_DEFAULT.contains(cmd);
                plugin.getConfig().set(path + ".enabled", defaultEnabled);
            }
        }
        plugin.saveConfig();
    }

    /** Check if a command/feature is enabled in config. */
    public boolean isEnabled(String command) {
        return enabledMap.getOrDefault(command, true);
    }

    /**
     * Programmatically disable a command at runtime and persist the change to config.yml.
     * Used by the startup dependency checker to auto-disable features whose dependencies are missing.
     */
    public void disableCommand(String command) {
        enabledMap.put(command, false);
        plugin.getConfig().set("command-settings." + command + ".enabled", false);
        plugin.saveConfig();
    }

    /**
     * Get the fixed permission node for a command.
     * Permissions are NOT configurable - they are constants matching plugin.yml.
     */
    public String getPermission(String command) {
        return PERMISSIONS.getOrDefault(command, "justplugin." + command);
    }

    /** Get the fixed permission for a command (static version). */
    public static String getDefaultPermission(String command) {
        return PERMISSIONS.getOrDefault(command, "justplugin." + command);
    }

    public void reload() {
        plugin.reloadConfig();
        loadSettings();
    }
}
