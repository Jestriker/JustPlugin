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
      { placeholder: "{player}", description: "Player's username", example: "Notch", usedIn: "All" },
      { placeholder: "{displayname}", description: "Player's display name (may include nickname)", example: "~Notch", usedIn: "All" },
      { placeholder: "{uuid}", description: "Player's unique ID", example: "069a79f4-...", usedIn: "All" },
      { placeholder: "{health}", description: "Current health points", example: "18.5", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{max_health}", description: "Maximum health points", example: "20.0", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{food}", description: "Current food/hunger level", example: "17", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{xp_level}", description: "Current experience level", example: "30", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{xp_progress}", description: "Progress to next XP level (0-100%)", example: "64%", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{gamemode}", description: "Current gamemode", example: "SURVIVAL", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Economy",
    color: "var(--green)",
    entries: [
      { placeholder: "{balance}", description: "Raw balance value", example: "15230.50", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{balance_formatted}", description: "Balance with currency symbol and commas", example: "$15,230.50", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{balance_short}", description: "Shortened balance for compact display", example: "$15.2K", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Statistics",
    color: "var(--accent)",
    entries: [
      { placeholder: "{kills}", description: "Total player kills", example: "142", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{deaths}", description: "Total deaths", example: "37", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{kd_ratio}", description: "Kill/death ratio", example: "3.84", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{playtime}", description: "Total playtime formatted", example: "3d 7h 22m", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{playtime_hours}", description: "Total playtime in hours", example: "79.4", usedIn: "Scoreboard, Tab List, Stats GUI" },
      { placeholder: "{blocks_broken}", description: "Total blocks mined", example: "48,291", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{blocks_placed}", description: "Total blocks placed", example: "31,044", usedIn: "Scoreboard, Stats GUI" },
      { placeholder: "{mobs_killed}", description: "Total mobs killed", example: "1,203", usedIn: "Scoreboard, Stats GUI" },
    ],
  },
  {
    name: "Server",
    color: "var(--yellow)",
    entries: [
      { placeholder: "{online}", description: "Current online player count", example: "24", usedIn: "All" },
      { placeholder: "{max_players}", description: "Maximum player slots", example: "100", usedIn: "All" },
      { placeholder: "{tps}", description: "Server TPS (ticks per second)", example: "19.98", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{ping}", description: "Player's ping in milliseconds", example: "42", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{mspt}", description: "Milliseconds per tick", example: "12.3", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{server_name}", description: "Server name from server.properties", example: "My Server", usedIn: "All" },
    ],
  },
  {
    name: "Location",
    color: "var(--accent-hover)",
    entries: [
      { placeholder: "{world}", description: "Current world name", example: "world", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{x}", description: "X coordinate (rounded)", example: "142", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{y}", description: "Y coordinate (rounded)", example: "64", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{z}", description: "Z coordinate (rounded)", example: "-891", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{biome}", description: "Current biome name", example: "Plains", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{direction}", description: "Cardinal direction the player is facing", example: "North", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{light_level}", description: "Light level at player's position", example: "12", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{dimension}", description: "Current dimension", example: "OVERWORLD", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "State",
    color: "var(--red)",
    entries: [
      { placeholder: "{gamemode}", description: "Current gamemode", example: "SURVIVAL", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{fly_status}", description: "Whether flight is enabled", example: "Enabled", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{god_status}", description: "Whether god mode is active", example: "Disabled", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{vanish_status}", description: "Whether the player is vanished", example: "Visible", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{afk_status}", description: "Whether the player is AFK", example: "Active", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Team",
    color: "var(--blue)",
    entries: [
      { placeholder: "{team}", description: "Player's team name", example: "Wolves", usedIn: "Scoreboard, Tab List, Chat" },
      { placeholder: "{team_members}", description: "Number of members in the team", example: "5", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{team_leader}", description: "Team leader's username", example: "Notch", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Armor / Items",
    color: "var(--yellow)",
    entries: [
      { placeholder: "{helmet}", description: "Equipped helmet name", example: "Diamond Helmet", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{chestplate}", description: "Equipped chestplate name", example: "Netherite Chestplate", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{leggings}", description: "Equipped leggings name", example: "Iron Leggings", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{boots}", description: "Equipped boots name", example: "Diamond Boots", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{held_item}", description: "Item currently held in main hand", example: "Diamond Sword", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{armor_durability}", description: "Total armor durability percentage", example: "87%", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Time",
    color: "var(--green)",
    entries: [
      { placeholder: "{time}", description: "Current real-world time", example: "14:32", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{date}", description: "Current real-world date", example: "2026-04-04", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{server_time}", description: "Server uptime", example: "3h 22m", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{game_time}", description: "In-game time of day", example: "Day", usedIn: "Scoreboard, Tab List" },
      { placeholder: "{game_day}", description: "In-game day count", example: "142", usedIn: "Scoreboard, Tab List" },
    ],
  },
  {
    name: "Tab List Special",
    color: "var(--accent)",
    entries: [
      { placeholder: "{online_staff}", description: "Number of staff members online", example: "3", usedIn: "Tab List" },
      { placeholder: "{welcome_name}", description: "Formatted welcome name for join messages", example: "Welcome, Notch!", usedIn: "Tab List, MOTD" },
      { placeholder: "{anim:name}", description: "Animated text placeholder (configured in tab list)", example: "(animated)", usedIn: "Tab List" },
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
        description="Complete list of all 50+ built-in placeholders available in JustPlugin. Use these in scoreboards, tab lists, MOTD, chat, and more."
        badge="50+"
      />

      {/* Quick stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        {[
          { label: "Total Placeholders", value: "50+" },
          { label: "Categories", value: "11" },
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
