import PageHeader from "@/components/PageHeader";

interface PlaceholderEntry {
  placeholder: string;
  description: string;
  example: string;
  usedIn: string;
}

interface PlaceholderCategory {
  name: string;
  color: string;
  entries: PlaceholderEntry[];
}

const categories: PlaceholderCategory[] = [
  {
    name: "Player Info",
    color: "var(--blue)",
    entries: [
      { placeholder: "{player} / {name}", description: "Player's username", example: "Notch", usedIn: "All" },
      { placeholder: "{display_name} / {displayname}", description: "Player's display name (may include nickname/formatting)", example: "~Notch", usedIn: "All" },
      { placeholder: "{uuid}", description: "Player's unique ID", example: "069a79f4-...", usedIn: "All" },
      { placeholder: "{ip}", description: "Player's IP address", example: "127.0.0.1", usedIn: "All" },
    ],
  },
  {
    name: "Health & Food",
    color: "var(--red)",
    entries: [
      { placeholder: "{health}", description: "Current health points", example: "18.5", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{max_health} / {maxhealth}", description: "Maximum health points", example: "20.0", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{health_bar} / {healthbar}", description: "Visual health bar using heart symbols", example: "❤❤❤❤❤❤❤❤❤❤", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{food} / {hunger}", description: "Current food/hunger level (0-20)", example: "17", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{saturation}", description: "Current saturation level", example: "5.0", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{absorption}", description: "Absorption hearts amount", example: "4.0", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Level & Experience",
    color: "var(--green)",
    entries: [
      { placeholder: "{level} / {lvl}", description: "Current XP level", example: "30", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{exp} / {xp}", description: "XP progress to next level as percentage", example: "64%", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{total_exp} / {totalexp}", description: "Total accumulated experience points", example: "1395", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Location",
    color: "var(--accent-hover)",
    entries: [
      { placeholder: "{x}", description: "Block X coordinate", example: "142", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{y}", description: "Block Y coordinate", example: "64", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{z}", description: "Block Z coordinate", example: "-891", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{world} / {world_name}", description: "Current world name", example: "world", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{biome}", description: "Current biome name (formatted)", example: "dark forest", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{dimension}", description: "Dimension name: Overworld, Nether, or The End", example: "Overworld", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{direction} / {facing}", description: "Cardinal direction the player is facing", example: "NE", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{light_level} / {lightlevel} / {light}", description: "Light level at player's position (0-15)", example: "12", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{yaw}", description: "Player's yaw rotation", example: "142.5", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{pitch}", description: "Player's pitch rotation", example: "-12.3", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Player State",
    color: "var(--red)",
    entries: [
      { placeholder: "{gamemode} / {gm}", description: "Current gamemode (lowercase)", example: "survival", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{fly_status} / {fly} / {flying}", description: "Whether flight is enabled", example: "Enabled", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{god_status} / {god}", description: "Whether god mode is active", example: "Disabled", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{vanish_status} / {vanish}", description: "Vanish state: Visible, Vanished, or Super Vanish", example: "Visible", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{speed}", description: "Dynamic speed (fly speed if flying, walk speed if walking, 0-10 scale)", example: "2.0", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{fly_speed} / {flyspeed}", description: "Fly speed (0-10 scale)", example: "1.0", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{walk_speed} / {walkspeed}", description: "Walk speed (0-10 scale)", example: "2.0", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{afk} / {afk_status}", description: "Shows \"AFK\" if player is AFK, empty otherwise", example: "AFK", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Held Items & Armor",
    color: "var(--yellow)",
    entries: [
      { placeholder: "{held_item} / {helditem} / {hand}", description: "Item in main hand (or \"Empty\")", example: "Diamond Sword", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{offhand} / {offhand_item}", description: "Item in off hand (or \"Empty\")", example: "Shield", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{armor_durability} / {armordurability}", description: "Total armor durability as percentage", example: "87%", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Economy",
    color: "var(--green)",
    entries: [
      { placeholder: "{balance} / {bal} / {money}", description: "Formatted balance with currency symbol", example: "$15,230.50", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{balance_raw} / {bal_raw}", description: "Raw balance number without formatting", example: "15230.50", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{balance_short} / {bal_short} / {money_short}", description: "Compact balance with K/M/B suffix", example: "15.23K", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{balance_rank} / {balrank}", description: "Player's rank in balance leaderboard", example: "#3", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Statistics",
    color: "var(--accent)",
    entries: [
      { placeholder: "{kills} / {player_kills}", description: "Total player kills", example: "142", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{kills_short}", description: "Compact player kills with K/M/B suffix", example: "1.50K", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{deaths}", description: "Total deaths", example: "37", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{deaths_short}", description: "Compact deaths with K/M/B suffix", example: "1.50K", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{kdr} / {kd}", description: "Kill/death ratio", example: "3.84", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{mobs_killed} / {mobkills}", description: "Total mobs killed", example: "1203", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{mobs_killed_short} / {mobkills_short}", description: "Compact mob kills with K/M/B suffix", example: "1.20K", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{blocks_broken} / {blocksbroken}", description: "Total blocks mined (cached 10s)", example: "48291", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{blocks_broken_short} / {blocksbroken_short}", description: "Compact blocks mined with K/M/B suffix", example: "48.29K", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{blocks_placed} / {blocksplaced}", description: "Total blocks placed (cached 10s)", example: "31044", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{blocks_placed_short} / {blocksplaced_short}", description: "Compact blocks placed with K/M/B suffix", example: "31.04K", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{blocks_walked} / {blockswalked} / {distance}", description: "Total distance walked + sprinted (blocks)", example: "58420", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{jumps}", description: "Total jumps", example: "9841", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{damage_dealt}", description: "Total damage dealt", example: "24510", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{damage_taken}", description: "Total damage taken", example: "18320", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{fish_caught} / {fishcaught}", description: "Total fish caught", example: "64", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{animals_bred} / {bred}", description: "Total animals bred", example: "230", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{items_crafted} / {crafted}", description: "Total items crafted (cached 10s)", example: "5120", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{times_slept} / {slept}", description: "Times slept in a bed", example: "42", usedIn: "Scoreboard, Stats GUI" },
    ],
  },
  {
    name: "Time & Playtime",
    color: "var(--green)",
    entries: [
      { placeholder: "{total_playtime} / {playtime}", description: "Total playtime (format from scoreboard config)", example: "5d 3h", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{session_playtime} / {session}", description: "Current session playtime (format from scoreboard config)", example: "1h 22m", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{playtime_display}", description: "Dynamic playtime (total or session based on scoreboard config)", example: "5d 3h", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{total_playtime_detailed} / {playtime_detailed}", description: "Total playtime in detailed format (includes seconds)", example: "5d 3h 22m 10s", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{total_playtime_compact} / {playtime_compact}", description: "Total playtime in compact format (no seconds)", example: "5d 3h", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{session_playtime_detailed} / {session_detailed}", description: "Session playtime in detailed format (includes seconds)", example: "1h 22m 5s", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{session_playtime_compact} / {session_compact}", description: "Session playtime in compact format (no seconds)", example: "1h 22m", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{real_time} / {irl_time} / {clock} / {time}", description: "Current real-world time (uses config timezone)", example: "14:32:05", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{real_date} / {irl_date} / {date}", description: "Current real-world date (uses config timezone)", example: "2026-04-10", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{game_time} / {world_time}", description: "In-game time of day (24h format)", example: "14:30", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Team",
    color: "var(--blue)",
    entries: [
      { placeholder: "{team} / {team_name}", description: "Player's team name (or \"None\")", example: "Wolves", usedIn: "Scoreboard, Tab List, Chat" },
      { placeholder: "{has_team}", description: "Whether the player is in a team (\"true\" or \"false\")", example: "true", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{team_members} / {team_size}", description: "Number of members in the player's team", example: "5", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{team_leader}", description: "Team leader(s) name(s)", example: "Notch", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Home & Warp",
    color: "var(--accent-hover)",
    entries: [
      { placeholder: "{homes_count} / {homes}", description: "Number of homes the player has set", example: "3", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{warps_count} / {warps}", description: "Total server warps available", example: "12", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Chat & Warnings",
    color: "var(--yellow)",
    entries: [
      { placeholder: "{chat_mode} / {chatmode}", description: "Current chat mode: All or Team", example: "All", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{active_warnings} / {warns}", description: "Number of active warnings", example: "1", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{total_warnings} / {totalwarns}", description: "Total warnings (including lifted)", example: "3", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Server",
    color: "var(--yellow)",
    entries: [
      { placeholder: "{online} / {online_players}", description: "Current online player count", example: "24", usedIn: "All" },
      { placeholder: "{max_players} / {maxplayers} / {max}", description: "Maximum player slots", example: "100", usedIn: "All" },
      { placeholder: "{tps}", description: "Server TPS (cached 5s, capped at 20.0)", example: "19.9", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{server_name} / {servername}", description: "Server name", example: "My Server", usedIn: "All" },
      { placeholder: "{free_memory} / {freemem}", description: "Free JVM memory", example: "512 MB", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{used_memory} / {usedmem} / {used_ram}", description: "Used JVM memory", example: "1024 MB", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{max_memory} / {maxmem}", description: "Maximum JVM memory", example: "2048 MB", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{uptime} / {server_uptime}", description: "Server uptime", example: "2d 5h", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{ping} / {latency}", description: "Player's ping in milliseconds", example: "42", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{online_staff} / {staff_count} / {staff_online}", description: "Number of staff members currently online", example: "3", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Weather",
    color: "var(--accent)",
    entries: [
      { placeholder: "{weather}", description: "Current weather: Clear, Rain, or Thunder", example: "Clear", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Discord",
    color: "var(--blue)",
    entries: [
      { placeholder: "{discord} / {discord_link}", description: "Discord invite link from config (shows \"Undefined Discord Link\" if not set)", example: "https://discord.gg/abc", usedIn: "All" },
    ],
  },
  {
    name: "LuckPerms Integration",
    color: "var(--accent-hover)",
    entries: [
      { placeholder: "{prefix} / {luckperms_prefix}", description: "Player's LuckPerms prefix (requires LuckPerms)", example: "[Admin] ", usedIn: "Scoreboard, Tab List, Chat" },
      { placeholder: "{suffix} / {luckperms_suffix}", description: "Player's LuckPerms suffix (requires LuckPerms)", example: " [VIP]", usedIn: "Scoreboard, Tab List, Chat" },
      { placeholder: "{group} / {luckperms_group} / {primary_group}", description: "Player's primary LuckPerms group (requires LuckPerms)", example: "admin", usedIn: "Scoreboard, Tab List, Chat" },
    ],
  },
  {
    name: "Utility",
    color: "var(--accent)",
    entries: [
      { placeholder: "{empty} / {blank}", description: "Empty string (for spacing)", example: "(empty)", usedIn: "All" },
      { placeholder: "{line}", description: "Separator line character", example: "━━━━━━━━━━━━━━━━━━━━", usedIn: "All" },
      { placeholder: "{welcome_name} / {welcome}", description: "Welcome message with player name", example: "Welcome Notch", usedIn: "Tab List, MOTD" },
    ],
  },
  {
    name: "PlaceholderAPI",
    color: "var(--red)",
    entries: [
      { placeholder: "{papi:placeholder_name}", description: "Any PlaceholderAPI placeholder (requires PAPI)", example: "Varies", usedIn: "Scoreboard, Tab List" },
    ],
  },
];

function CategoryBadge({ name, color }: { name: string; color: string }) {
  return (
    <span
      className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
      style={{ backgroundColor: `color-mix(in srgb, ${color} 15%, transparent)`, color }}
    >
      {name}
    </span>
  );
}

export default function PlaceholdersPage() {
  return (
    <div>
      <PageHeader
        title="Placeholder Reference"
        description={<>Complete list of all 75+ built-in placeholders available in <span className="text-[var(--accent)]">JustPlugin</span>. Use these in scoreboards, tab lists, MOTD, chat, and more.</>}
        badge="75+"
      />

      {/* Quick stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        {[
          { label: "Total Placeholders", value: "75+" },
          { label: "Categories", value: "18" },
          { label: "PAPI Support", value: "Yes" },
          { label: "Custom Animations", value: "Yes" },
        ].map((stat) => (
          <div key={stat.label} className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4 text-center">
            <div className="text-xl font-bold text-[var(--accent-hover)]">{stat.value}</div>
            <div className="text-xs text-[var(--text-muted)] mt-1">{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Where placeholders work */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-8">
        <h2 className="text-lg font-semibold mb-4">Where Placeholders Work</h2>
        <p className="text-sm text-[var(--text-secondary)] mb-4">
          Placeholders can be used in most text-based configuration options throughout JustPlugin.
          The &quot;Used In&quot; column in the tables below shows where each placeholder is supported.
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {[
            { name: "Scoreboard", desc: "All lines and title in scoreboard.yml" },
            { name: "Tab List", desc: "Header, footer, and player format" },
            { name: "MOTD / Join Messages", desc: "Server list and join/quit messages" },
            { name: "Chat Hover Tooltip", desc: "Hover text shown over player names" },
            { name: "Stats GUI", desc: "Player statistics inventory GUI" },
            { name: "Automated Messages", desc: "Scheduled broadcast messages" },
          ].map((item) => (
            <div key={item.name} className="bg-[var(--bg-tertiary)] rounded-lg p-3">
              <div className="font-semibold text-sm">{item.name}</div>
              <div className="text-xs text-[var(--text-muted)] mt-1">{item.desc}</div>
            </div>
          ))}
        </div>
      </div>

      {/* PlaceholderAPI note */}
      <div className="bg-[var(--bg-tertiary)] border border-[var(--border)] rounded-lg p-4 mb-8 text-sm text-[var(--text-secondary)]">
        <span className="font-semibold text-[var(--accent-hover)]">PlaceholderAPI Integration:</span>{" "}
        When PlaceholderAPI is installed, all JustPlugin placeholders are automatically registered as PAPI
        placeholders. This means other plugins can use JustPlugin&apos;s data via{" "}
        <code className="bg-[var(--bg-card)] px-1.5 py-0.5 rounded text-xs">%justplugin_balance%</code>,{" "}
        <code className="bg-[var(--bg-card)] px-1.5 py-0.5 rounded text-xs">%justplugin_kills%</code>, etc.
        Conversely, you can use any PAPI placeholder inside JustPlugin configs with the{" "}
        <code className="bg-[var(--bg-card)] px-1.5 py-0.5 rounded text-xs">{"{papi:placeholder_name}"}</code> syntax.
      </div>

      {/* Placeholder tables by category */}
      {categories.map((category) => (
        <div key={category.name} className="mb-10">
          <div className="flex items-center gap-3 mb-4 pb-2 border-b border-[var(--border)]">
            <h2 className="text-2xl font-bold">{category.name}</h2>
            <CategoryBadge name={`${category.entries.length} placeholders`} color={category.color} />
          </div>

          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Placeholder</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Description</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Example Output</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Used In</th>
                </tr>
              </thead>
              <tbody>
                {category.entries.map((entry) => (
                  <tr key={entry.placeholder} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                    <td className="py-3 px-4">
                      <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono font-semibold text-[var(--accent-hover)]">
                        {entry.placeholder}
                      </code>
                    </td>
                    <td className="py-3 px-4 text-sm text-[var(--text-secondary)]">{entry.description}</td>
                    <td className="py-3 px-4 text-sm font-mono text-[var(--text-muted)]">{entry.example}</td>
                    <td className="py-3 px-4 text-sm text-[var(--text-muted)]">{entry.usedIn}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </div>
  );
}
