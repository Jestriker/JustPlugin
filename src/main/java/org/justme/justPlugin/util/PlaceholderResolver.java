package org.justme.justPlugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.ChatManager;
import org.justme.justPlugin.managers.TeamManager;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Resolves {placeholder} tokens in text strings for a given player.
 * Supports 50+ variables from Bukkit API, plugin managers, and server info.
 */
public final class PlaceholderResolver {

    private PlaceholderResolver() {}

    // Cache TPS for 5 seconds to avoid expensive lookups every tick
    private static double cachedTps = 20.0;
    private static long tpsCacheTime = 0;

    // Cache expensive aggregate stats per player (refreshed every 10s)
    private static final Map<UUID, Map<String, Object>> statsCache = new HashMap<>();
    private static final Map<UUID, Long> statsCacheTime = new HashMap<>();
    private static final long STATS_CACHE_TTL = 10_000; // 10 seconds

    // Session join times (set when player joins, cleared when they leave)
    private static final Map<UUID, Long> sessionJoinTimes = new HashMap<>();

    public static void recordJoin(UUID uuid) {
        sessionJoinTimes.put(uuid, System.currentTimeMillis());
    }

    public static void recordQuit(UUID uuid) {
        sessionJoinTimes.remove(uuid);
        statsCache.remove(uuid);
        statsCacheTime.remove(uuid);
    }

    /**
     * Replace all {placeholder} tokens in the input text for the given player.
     */
    public static String resolve(Player player, JustPlugin plugin, String text) {
        if (text == null || !text.contains("{")) return text;

        StringBuilder sb = new StringBuilder(text.length());
        int len = text.length();
        int i = 0;
        while (i < len) {
            int open = text.indexOf('{', i);
            if (open == -1) {
                sb.append(text, i, len);
                break;
            }
            sb.append(text, i, open);
            int close = text.indexOf('}', open + 1);
            if (close == -1) {
                sb.append(text, open, len);
                break;
            }
            String key = text.substring(open + 1, close).toLowerCase();
            String value = resolveKey(key, player, plugin);
            sb.append(value != null ? value : text.substring(open, close + 1));
            i = close + 1;
        }
        return sb.toString();
    }

    @SuppressWarnings("deprecation")
    private static String resolveKey(String key, Player player, JustPlugin plugin) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        return switch (key) {
            // ===== Player Info =====
            case "player", "name" -> player.getName();
            case "display_name", "displayname" -> CC.legacy(player.displayName());
            case "uuid" -> uuid.toString();
            case "ip" -> player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown";

            // ===== Health & Food =====
            case "health" -> String.format("%.1f", player.getHealth());
            case "max_health", "maxhealth" -> String.format("%.1f", player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
            case "health_bar", "healthbar" -> makeBar(player.getHealth(), player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue(), 10, "❤");
            case "food", "hunger" -> String.valueOf(player.getFoodLevel());
            case "saturation" -> String.format("%.1f", player.getSaturation());
            case "absorption" -> String.format("%.1f", player.getAbsorptionAmount());

            // ===== Level & XP =====
            case "level", "lvl" -> String.valueOf(player.getLevel());
            case "exp", "xp" -> String.format("%.0f%%", player.getExp() * 100);
            case "total_exp", "totalexp" -> String.valueOf(player.getTotalExperience());

            // ===== Location =====
            case "x" -> String.valueOf(loc.getBlockX());
            case "y" -> String.valueOf(loc.getBlockY());
            case "z" -> String.valueOf(loc.getBlockZ());
            case "world", "world_name" -> loc.getWorld() != null ? loc.getWorld().getName() : "Unknown";
            case "biome" -> loc.getBlock().getBiome().getKey().getKey().replace('_', ' ');
            case "dimension" -> {
                if (loc.getWorld() == null) yield "Unknown";
                yield switch (loc.getWorld().getEnvironment()) {
                    case NORMAL -> "Overworld";
                    case NETHER -> "Nether";
                    case THE_END -> "The End";
                    default -> loc.getWorld().getEnvironment().name();
                };
            }
            case "direction", "facing" -> getCardinalDirection(loc.getYaw());
            case "light_level", "lightlevel", "light" -> String.valueOf(loc.getBlock().getLightLevel());
            case "yaw" -> String.format("%.1f", loc.getYaw());
            case "pitch" -> String.format("%.1f", loc.getPitch());

            // ===== Player State =====
            case "gamemode", "gm" -> player.getGameMode().name().toLowerCase();
            case "fly_status", "fly", "flying" -> player.getAllowFlight() ? "Enabled" : "Disabled";
            case "god_status", "god" -> plugin.getPlayerListener().isGodMode(uuid) ? "Enabled" : "Disabled";
            case "vanish_status", "vanish" -> {
                if (plugin.getVanishManager().isSuperVanished(uuid)) yield "Super Vanish";
                if (plugin.getVanishManager().isVanished(uuid)) yield "Vanished";
                yield "Visible";
            }
            case "speed" -> player.isFlying()
                    ? String.format("%.1f", player.getFlySpeed() * 10)
                    : String.format("%.1f", player.getWalkSpeed() * 10);
            case "fly_speed", "flyspeed" -> String.format("%.1f", player.getFlySpeed() * 10);
            case "walk_speed", "walkspeed" -> String.format("%.1f", player.getWalkSpeed() * 10);

            // ===== Held Item & Armor =====
            case "held_item", "helditem", "hand" -> {
                var item = player.getInventory().getItemInMainHand();
                yield item.getType().isAir() ? "Empty" : formatMaterialName(item.getType().name());
            }
            case "offhand", "offhand_item" -> {
                var item = player.getInventory().getItemInOffHand();
                yield item.getType().isAir() ? "Empty" : formatMaterialName(item.getType().name());
            }
            case "armor_durability", "armordurability" -> {
                int total = 0, remaining = 0;
                for (var armor : player.getInventory().getArmorContents()) {
                    if (armor != null && armor.getType().getMaxDurability() > 0) {
                        total += armor.getType().getMaxDurability();
                        remaining += armor.getType().getMaxDurability() - ((org.bukkit.inventory.meta.Damageable) armor.getItemMeta()).getDamage();
                    }
                }
                yield total > 0 ? ((remaining * 100 / total) + "%") : "N/A";
            }

            // ===== Economy =====
            case "balance", "bal", "money" -> plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(uuid));
            case "balance_raw", "bal_raw" -> String.format("%.2f", plugin.getEconomyManager().getBalance(uuid));
            case "balance_rank", "balrank" -> {
                var sorted = plugin.getEconomyManager().getAllBalancesSorted();
                int rank = 1;
                for (var entry : sorted) {
                    if (entry.getKey().equals(uuid)) break;
                    rank++;
                }
                yield "#" + rank;
            }

            // ===== Statistics =====
            case "kills", "player_kills" -> String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS));
            case "deaths" -> String.valueOf(player.getStatistic(Statistic.DEATHS));
            case "kdr", "kd" -> {
                int kills = player.getStatistic(Statistic.PLAYER_KILLS);
                int deaths = player.getStatistic(Statistic.DEATHS);
                yield deaths > 0 ? String.format("%.2f", (double) kills / deaths) : String.valueOf(kills);
            }
            case "mobs_killed", "mobkills" -> String.valueOf(player.getStatistic(Statistic.MOB_KILLS));
            case "blocks_broken", "blocksbroken" -> String.valueOf(getCachedAggregateStat(player, "blocks_broken"));
            case "blocks_placed", "blocksplaced" -> String.valueOf(getCachedAggregateStat(player, "blocks_placed"));
            case "blocks_walked", "blockswalked", "distance" -> {
                long cm = player.getStatistic(Statistic.WALK_ONE_CM)
                        + player.getStatistic(Statistic.SPRINT_ONE_CM);
                yield String.format("%.0f", cm / 100.0);
            }
            case "jumps" -> String.valueOf(player.getStatistic(Statistic.JUMP));
            case "damage_dealt" -> String.valueOf(player.getStatistic(Statistic.DAMAGE_DEALT));
            case "damage_taken" -> String.valueOf(player.getStatistic(Statistic.DAMAGE_TAKEN));
            case "fish_caught", "fishcaught" -> String.valueOf(player.getStatistic(Statistic.FISH_CAUGHT));
            case "animals_bred", "bred" -> String.valueOf(player.getStatistic(Statistic.ANIMALS_BRED));
            case "items_crafted", "crafted" -> String.valueOf(getCachedAggregateStat(player, "items_crafted"));
            case "times_slept", "slept" -> String.valueOf(player.getStatistic(Statistic.SLEEP_IN_BED));

            // ===== Time & Playtime =====
            case "total_playtime", "playtime" -> formatDuration(player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L);
            case "session_playtime", "session" -> {
                Long joinTime = sessionJoinTimes.get(uuid);
                yield joinTime != null ? formatDuration(System.currentTimeMillis() - joinTime) : "0s";
            }

            // ===== Team =====
            case "team", "team_name" -> {
                String teamName = plugin.getTeamManager().getPlayerTeam(uuid);
                yield teamName != null ? teamName : "None";
            }
            case "team_members", "team_size" -> {
                String teamName = plugin.getTeamManager().getPlayerTeam(uuid);
                if (teamName == null) yield "0";
                TeamManager.TeamData data = plugin.getTeamManager().getTeam(teamName);
                yield data != null ? String.valueOf(data.members.size()) : "0";
            }
            case "team_leader" -> {
                String teamName = plugin.getTeamManager().getPlayerTeam(uuid);
                if (teamName == null) yield "None";
                TeamManager.TeamData data = plugin.getTeamManager().getTeam(teamName);
                if (data == null) yield "None";
                var leader = Bukkit.getOfflinePlayer(data.leader);
                yield leader.getName() != null ? leader.getName() : "Unknown";
            }

            // ===== Home & Warp =====
            case "homes_count", "homes" -> String.valueOf(plugin.getHomeManager().getHomes(uuid).size());
            case "warps_count", "warps" -> String.valueOf(plugin.getWarpManager().getWarpNames().size());

            // ===== Chat =====
            case "chat_mode", "chatmode" -> plugin.getChatManager().getChatMode(uuid) == ChatManager.ChatMode.TEAM ? "Team" : "All";

            // ===== Warnings =====
            case "active_warnings", "warns" -> String.valueOf(plugin.getWarnManager().getActiveWarnCount(uuid));
            case "total_warnings", "totalwarns" -> String.valueOf(plugin.getWarnManager().getTotalWarnCount(uuid));

            // ===== Server =====
            case "online", "online_players" -> String.valueOf(Bukkit.getOnlinePlayers().size());
            case "max_players", "maxplayers", "max" -> String.valueOf(Bukkit.getMaxPlayers());
            case "tps" -> String.format("%.1f", getTps());
            case "server_name", "servername" -> Bukkit.getServer().getName();
            case "free_memory", "freemem" -> (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB";
            case "used_memory", "usedmem" -> ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + " MB";
            case "max_memory", "maxmem" -> (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB";
            case "uptime", "server_uptime" -> formatDuration(ManagementFactory.getRuntimeMXBean().getUptime());

            // ===== Weather =====
            case "weather" -> {
                if (loc.getWorld() == null) yield "Unknown";
                if (loc.getWorld().isThundering()) yield "Thunder";
                if (loc.getWorld().hasStorm()) yield "Rain";
                yield "Clear";
            }

            // ===== Time (Real World) =====
            case "real_time", "irl_time", "clock" -> {
                String tz = plugin.getConfig().getString("timezone", "UTC");
                yield LocalTime.now(ZoneId.of(tz)).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
            case "real_date", "irl_date", "date" -> {
                String tz = plugin.getConfig().getString("timezone", "UTC");
                yield LocalDate.now(ZoneId.of(tz)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            // ===== Time (Game) =====
            case "game_time", "world_time" -> {
                if (loc.getWorld() == null) yield "Unknown";
                long ticks = loc.getWorld().getTime();
                long hours = (ticks / 1000 + 6) % 24;
                long minutes = (ticks % 1000) * 60 / 1000;
                yield String.format("%02d:%02d", hours, minutes);
            }

            // ===== Ping =====
            case "ping", "latency" -> String.valueOf(player.getPing());

            // ===== Misc =====
            case "empty", "blank" -> "";
            case "line" -> "━━━━━━━━━━━━━━━━━━━━";

            default -> null;
        };
    }

    // ---- Helpers ----

    private static String getCardinalDirection(float yaw) {
        double rotation = (yaw - 180) % 360;
        if (rotation < 0) rotation += 360;
        if (rotation < 22.5) return "N";
        if (rotation < 67.5) return "NE";
        if (rotation < 112.5) return "E";
        if (rotation < 157.5) return "SE";
        if (rotation < 202.5) return "S";
        if (rotation < 247.5) return "SW";
        if (rotation < 292.5) return "W";
        if (rotation < 337.5) return "NW";
        return "N";
    }

    private static String formatMaterialName(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.toLowerCase().split("_")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static String makeBar(double current, double max, int length, String symbol) {
        int filled = (int) Math.round((current / max) * length);
        return ("<green>" + symbol.repeat(Math.max(0, filled)) +
                "<dark_gray>" + symbol.repeat(Math.max(0, length - filled)));
    }

    @SuppressWarnings("deprecation")
    private static double getTps() {
        long now = System.currentTimeMillis();
        if (now - tpsCacheTime > 5000) {
            try {
                double[] tps = Bukkit.getTPS();
                cachedTps = tps.length > 0 ? Math.min(tps[0], 20.0) : 20.0;
            } catch (Exception e) {
                cachedTps = 20.0;
            }
            tpsCacheTime = now;
        }
        return cachedTps;
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m " + (seconds % 60) + "s";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h " + (minutes % 60) + "m";
        long days = hours / 24;
        return days + "d " + (hours % 24) + "h";
    }

    /**
     * Gets an expensive aggregate stat with caching to avoid iterating all materials every tick.
     */
    private static int getCachedAggregateStat(Player player, String statKey) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastTime = statsCacheTime.get(uuid);
        Map<String, Object> playerCache = statsCache.get(uuid);

        if (lastTime != null && playerCache != null && (now - lastTime) < STATS_CACHE_TTL) {
            Object cached = playerCache.get(statKey);
            if (cached != null) return (int) cached;
        }

        // Recompute
        if (playerCache == null) {
            playerCache = new HashMap<>();
            statsCache.put(uuid, playerCache);
        }

        int value = 0;
        switch (statKey) {
            case "blocks_broken" -> {
                for (org.bukkit.Material mat : org.bukkit.Material.values()) {
                    if (mat.isBlock()) {
                        try { value += player.getStatistic(Statistic.MINE_BLOCK, mat); } catch (Exception ignored) {}
                    }
                }
            }
            case "blocks_placed" -> {
                for (org.bukkit.Material mat : org.bukkit.Material.values()) {
                    if (mat.isBlock()) {
                        try { value += player.getStatistic(Statistic.USE_ITEM, mat); } catch (Exception ignored) {}
                    }
                }
            }
            case "items_crafted" -> {
                for (org.bukkit.Material mat : org.bukkit.Material.values()) {
                    if (mat.isItem()) {
                        try { value += player.getStatistic(Statistic.CRAFT_ITEM, mat); } catch (Exception ignored) {}
                    }
                }
            }
        }

        playerCache.put(statKey, value);
        statsCacheTime.put(uuid, now);
        return value;
    }
}










