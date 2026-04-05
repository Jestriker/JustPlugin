import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";
import { PLUGIN_VERSION } from "@/data/constants";

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="mb-10">
      <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">{title}</h2>
      {children}
    </section>
  );
}

function SubSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="mb-6">
      <h3 className="text-lg font-semibold mb-3">{title}</h3>
      {children}
    </div>
  );
}

function ConfigNote({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-[var(--bg-tertiary)] border border-[var(--border)] rounded-lg p-4 my-4 text-sm text-[var(--text-secondary)]">
      {children}
    </div>
  );
}

export default function ConfigurationPage() {
  return (
    <div>
      <PageHeader
        title="Plugin Configuration"
        description="Complete reference for every configuration file in JustPlugin. All files are auto-generated with inline comments on first run."
        badge={`v${PLUGIN_VERSION}`}
      />

      {/* Table of Contents */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-10">
        <h2 className="text-lg font-semibold mb-3">Configuration Files</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm">
          {[
            { href: "#config-yml", label: "config.yml", desc: "Main plugin configuration" },
            { href: "#database-yml", label: "database.yml", desc: "Database provider & connection" },
            { href: "#scoreboard-yml", label: "scoreboard.yml", desc: "Scoreboard display & animations" },
            { href: "#motd-yml", label: "motd.yml", desc: "Server MOTD & join messages" },
            { href: "#automessages-yml", label: "automessages.yml", desc: "Scheduled broadcast messages" },
            { href: "#maintenance-yml", label: "maintenance/config.yml", desc: "Maintenance mode settings" },
            { href: "#stats-yml", label: "stats.yml", desc: "Stats GUI configuration" },
            { href: "#texts", label: "texts/ folder", desc: "27 localization files" },
          ].map((item) => (
            <a
              key={item.href}
              href={item.href}
              className="flex items-center gap-3 p-2 rounded-lg hover:bg-[var(--bg-hover)] transition-colors"
            >
              <code className="text-xs text-[var(--accent-hover)] font-mono">{item.label}</code>
              <span className="text-[var(--text-muted)] text-xs">{item.desc}</span>
            </a>
          ))}
        </div>
      </div>

      {/* config.yml */}
      <Section id="config-yml" title="config.yml &mdash; Main Configuration">
        <p className="text-[var(--text-secondary)] mb-4">
          The primary configuration file controlling all major plugin features. Located at{" "}
          <code>plugins/JustPlugin/config.yml</code>.
        </p>

        <SubSection title="General Settings">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# General settings
discord-link: "https://discord.gg/QCArmUbaJ8"
timezone: "America/New_York"`}
          />
        </SubSection>

        <SubSection title="Data Settings">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Auto-save interval in minutes
data:
  auto-save-interval: 5`}
          />
          <ConfigNote>
            Player data, warps, homes, and economy balances are automatically saved at this interval.
            Set to <code>0</code> to disable auto-saving (not recommended).
          </ConfigNote>
        </SubSection>

        <SubSection title="Staff Groups (LuckPerms)">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Staff groups for staff-related features (requires LuckPerms)
staff-groups:
  - admin
  - moderator
  - helper`}
          />
        </SubSection>

        <SubSection title="Chat Settings">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Chat configuration
chat:
  separator: " >> "
  hover-tooltip: true  # Show player stats on hover`}
          />
        </SubSection>

        <SubSection title="Nicknames">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`nicknames:
  max-length: 16
  allow-duplicates: false
  blocked-words:
    - "admin"
    - "moderator"
    - "owner"
    - "server"`}
          />
        </SubSection>

        <SubSection title="Economy">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`economy:
  provider: "justplugin"   # Options: justplugin, vault
  starting-balance: 100.0
  currency-symbol: "$"
  max-balance: 1000000000.0
  pay-cooldown: 5           # Seconds between /pay uses`}
          />
          <ConfigNote>
            Set <code>provider</code> to <code>vault</code> to use an external economy plugin through Vault.
            When using the built-in provider, all economy data is stored via the configured database backend.
          </ConfigNote>
        </SubSection>

        <SubSection title="Teleportation">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`teleportation:
  request-timeout: 60       # Seconds before a TPA request expires
  wild:
    range: 50000            # Max distance for /wild
    min-range: 500          # Min distance from spawn
  safe-teleport: true       # Check 3x3 area for safety before TP
  cooldowns:
    tpa: 30
    home: 10
    warp: 10
    spawn: 10
    wild: 60
    back: 5
  delays:
    tpa: 3
    home: 3
    warp: 0
    spawn: 3
    wild: 5
    back: 0`}
          />
          <ConfigNote>
            Safe teleport checks a 3x3 area around the destination for lava, fire, void, and suffocation hazards.
            Players with the <code>justplugin.teleport.unsafe</code> permission bypass safety checks.
          </ConfigNote>
        </SubSection>

        <SubSection title="Clickable Commands">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Enable clickable command suggestions in chat
clickable-commands: true`}
          />
        </SubSection>

        <SubSection title="Trading">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`trade:
  request-timeout: 60  # Seconds before a trade request expires`}
          />
        </SubSection>

        <SubSection title="Homes">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`homes:
  max-homes: 3  # Default max homes per player`}
          />
          <ConfigNote>
            Override per-player with the <code>justplugin.homes.max.&lt;number&gt;</code> permission node.
          </ConfigNote>
        </SubSection>

        <SubSection title="Kits">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`kits:
  archive-retention: 30  # Days to keep deleted kit archives`}
          />
        </SubSection>

        <SubSection title="Warning Punishment Escalation">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`warnings:
  escalation:
    1:
      action: "ChatMessage"
    2:
      action: "Kick"
    3:
      action: "TempBan"
      duration: "5m"
    4:
      action: "TempBan"
      duration: "1h"
    5:
      action: "TempBan"
      duration: "1d"
    6:
      action: "Ban"`}
          />
          <ConfigNote>
            Each warning level triggers the configured action automatically. Actions: <code>ChatMessage</code>,{" "}
            <code>Kick</code>, <code>TempBan</code> (requires duration), <code>Ban</code>.
          </ConfigNote>
        </SubSection>

        <SubSection title="Jail Settings">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`jail:
  adventure-mode: true      # Set jailed players to adventure mode
  block-commands: true       # Block commands while jailed
  allowed-commands:          # Commands allowed while jailed
    - "/msg"
    - "/r"
    - "/helpop"`}
          />
        </SubSection>

        <SubSection title="Default Reasons">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`default-reasons:
  ban: "You have been banned from this server."
  kick: "You have been kicked from this server."
  mute: "You have been muted."
  warn: "You have been warned."
  jail: "You have been jailed."`}
          />
        </SubSection>

        <SubSection title="Web Editor">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`web-editor:
  enabled: false
  host: "localhost"
  port: 8585`}
          />
          <ConfigNote>
            The web editor provides a browser-based GUI for editing all config files. Access it at{" "}
            <code>http://localhost:8585</code> when enabled. Only enable on trusted networks.
          </ConfigNote>
        </SubSection>

        <SubSection title="Discord Webhook Logging">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Discord webhook logging
# Set webhook URL in-game with /setlogswebhook <url>
discord-logging:
  enabled: true
  log-commands: true
  log-punishments: true
  log-economy: true
  log-teleportation: true`}
          />
        </SubSection>

        <SubSection title="Vanilla Command Logging">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Log vanilla Minecraft commands
vanilla-command-logging: true`}
          />
        </SubSection>

        <SubSection title="Tab List">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`tab-list:
  enabled: true
  header: "<gradient:#ff6b6b:#ffa500>Your Server</gradient>"
  footer: "<gray>Players: %online%/%max%</gray>"`}
          />
        </SubSection>

        <SubSection title="Entity Clear">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`entity-clear:
  enabled: true
  interval: 300            # Seconds between clears
  warn-before:
    - 60                   # Warn 60s before clear
    - 30                   # Warn 30s before clear
    - 10                   # Warn 10s before clear
  thresholds:
    items: 200             # Only clear if items exceed this
    mobs: 150              # Only clear if mobs exceed this`}
          />
        </SubSection>

        <SubSection title="AFK System">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`afk:
  idle-time: 300           # Seconds before marked AFK (5 min)
  kick-after: 0            # Seconds to kick after AFK (0 = disabled)
  broadcast: true          # Announce AFK status in chat`}
          />
        </SubSection>

        <SubSection title="Player Vaults">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`vaults:
  enabled: false         # Disabled by default, opt-in
  max-vaults: 3          # Maximum vaults per player`}
          />
          <ConfigNote>
            Player vaults are 54-slot virtual storage inventories, separate from the ender chest.
            Grant <code>justplugin.vaults.&lt;number&gt;</code> to let specific players bypass the server max.
          </ConfigNote>
        </SubSection>

        <SubSection title="Transaction History">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`economy:
  transaction-history:
    enabled: true
    retention-days: 30     # Auto-prune entries older than 30 days
    max-entries: 500       # Max entries stored per player
  addcash:
    show-to-player: true   # Notify players when cash is added
  paynote:
    show-creator: true     # Show creator name on PayNotes
    notify-creator: "name" # "name", "anonymous", or "none"`}
          />
          <ConfigNote>
            Transaction types tracked: PAY, PAYNOTE_CREATE, PAYNOTE_REDEEM, ADDCASH, TRADE, and API.
            View with <code>/transactions [player]</code>.
          </ConfigNote>
        </SubSection>

        <SubSection title="Enchant">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`enchant:
  bypass-restrictions: false  # Allow any enchant on any item at any level`}
          />
          <ConfigNote>
            When enabled, all players can apply any enchantment to any item regardless of vanilla restrictions.
            Players with <code>justplugin.enchant.bypass</code> can always bypass restrictions regardless of this setting.
          </ConfigNote>
        </SubSection>

        <SubSection title="Near Command">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`commands:
  near:
    default-radius: 1000   # Default search radius in blocks
    max-radius: 5000       # Maximum allowed radius`}
          />
          <ConfigNote>
            The <code>/near</code> command shows nearby players with distance, compass direction, coordinates,
            and a clickable [TP] button for safe teleport. Vanished players are excluded unless the viewer
            has <code>vanish.see</code> permission.
          </ConfigNote>
        </SubSection>

        <SubSection title="Command Settings">
          <CodeBlock
            language="yaml"
            filename="config.yml"
            code={`# Enable or disable individual commands
command-settings:
  tpa: true
  home: true
  warp: true
  wild: true
  spawn: true
  back: true
  pay: true
  trade: true
  mail: true
  nick: true
  kit: true
  afk: true
  # ... all commands can be toggled individually`}
          />
          <ConfigNote>
            Disabled commands are completely unregistered from the server. A restart or <code>/jpreload</code> is
            required after changing command-settings.
          </ConfigNote>
        </SubSection>
      </Section>

      {/* database.yml */}
      <Section id="database-yml" title="database.yml &mdash; Database Configuration">
        <p className="text-[var(--text-secondary)] mb-4">
          Configure how JustPlugin stores persistent data. Three backends are supported.
        </p>

        <SubSection title="YAML (Default)">
          <CodeBlock
            language="yaml"
            filename="database.yml"
            code={`type: "yaml"
# Data stored in plugins/JustPlugin/data/ as .yml files
# No additional configuration needed`}
          />
        </SubSection>

        <SubSection title="SQLite">
          <CodeBlock
            language="yaml"
            filename="database.yml"
            code={`type: "sqlite"
sqlite:
  file: "justplugin.db"   # File name in plugin folder`}
          />
        </SubSection>

        <SubSection title="MySQL">
          <CodeBlock
            language="yaml"
            filename="database.yml"
            code={`type: "mysql"
mysql:
  host: "localhost"
  port: 3306
  database: "justplugin"
  username: "root"
  password: "password"
  pool:
    maximum-pool-size: 10
    minimum-idle: 2
    connection-timeout: 30000
    max-lifetime: 1800000`}
          />
          <ConfigNote>
            MySQL connections use HikariCP for connection pooling. Ensure your MySQL server is accessible
            from the Minecraft server and the database exists before enabling.
          </ConfigNote>
        </SubSection>
      </Section>

      {/* scoreboard.yml */}
      <Section id="scoreboard-yml" title="scoreboard.yml &mdash; Scoreboard Configuration">
        <p className="text-[var(--text-secondary)] mb-4">
          Configure the animated sidebar scoreboard with 50+ placeholders.
        </p>

        <CodeBlock
          language="yaml"
          filename="scoreboard.yml"
          code={`enabled: true
update-interval: 20        # Ticks between updates (20 = 1 second)

title:
  text: "<gradient:#ff6b6b:#ffa500>Your Server</gradient>"
  wave-animation: true     # Animate gradient across the title

animations:
  loading:
    frames:
      - "<gray>[    ]</gray>"
      - "<gray>[=   ]</gray>"
      - "<gray>[==  ]</gray>"
      - "<gray>[=== ]</gray>"
      - "<gray>[====]</gray>"
    interval: 5            # Ticks between frames

lines:
  - ""
  - "<white>Player:</white> <aqua>%player_name%</aqua>"
  - "<white>Rank:</white> <aqua>%player_rank%</aqua>"
  - "<white>Balance:</white> <green>%economy_balance_formatted%</green>"
  - ""
  - "<white>Online:</white> <yellow>%online%</yellow><gray>/</gray><yellow>%max%</yellow>"
  - "<white>TPS:</white> <green>%server_tps%</green>"
  - "<white>Ping:</white> <green>%player_ping%</green><gray>ms</gray>"
  - ""
  - "<gray>play.yourserver.com</gray>"`}
        />
        <ConfigNote>
          Supports MiniMessage formatting. Use 50+ built-in placeholders or PlaceholderAPI placeholders.
          Wave animation creates a scrolling color effect across the title text.
        </ConfigNote>
      </Section>

      {/* motd.yml */}
      <Section id="motd-yml" title="motd.yml &mdash; Server MOTD Configuration">
        <p className="text-[var(--text-secondary)] mb-4">
          Configure the server list MOTD with multiple display modes and join messages.
        </p>

        <CodeBlock
          language="yaml"
          filename="motd.yml"
          code={`# Mode options: static, cycle, random
mode: "static"

# Cycle interval in seconds (only for cycle mode)
cycle-interval: 30

profiles:
  default:
    line1: "<gradient:#ff6b6b:#ffa500>Your Server Network</gradient>"
    line2: "<gray>Welcome! Currently </gray><green>%online%</green><gray> players online.</gray>"
  holiday:
    line1: "<gradient:#ff0000:#00ff00>Holiday Event Active!</gradient>"
    line2: "<yellow>Join now for exclusive rewards!</yellow>"

# Message shown to players on join
join-motd:
  enabled: true
  messages:
    - ""
    - "<gradient:#ff6b6b:#ffa500>Welcome to Your Server!</gradient>"
    - "<gray>Type </gray><aqua>/help</aqua><gray> to get started.</gray>"
    - "<gray>Players online: </gray><green>%online%</green>"
    - ""`}
        />
      </Section>

      {/* automessages.yml */}
      <Section id="automessages-yml" title="automessages.yml &mdash; Automated Messages">
        <p className="text-[var(--text-secondary)] mb-4">
          Schedule automated broadcast messages with four flexible timing modes.
        </p>

        <SubSection title="Interval Mode">
          <CodeBlock
            language="yaml"
            filename="automessages.yml"
            code={`messages:
  tips:
    mode: "interval"
    interval: 300          # Every 300 seconds (5 minutes)
    messages:
      - "<gold>[Tip]</gold> <gray>Use /sethome to save your location!</gray>"
      - "<gold>[Tip]</gold> <gray>Trade safely with /trade <player>!</gray>"
      - "<gold>[Tip]</gold> <gray>Check your stats with /stats!</gray>"`}
          />
        </SubSection>

        <SubSection title="Schedule Mode">
          <CodeBlock
            language="yaml"
            filename="automessages.yml"
            code={`messages:
  daily-restart:
    mode: "schedule"
    times:
      - "06:00"
      - "18:00"
    messages:
      - "<red>[Server]</red> <gray>Server restart in 30 minutes!</gray>"`}
          />
        </SubSection>

        <SubSection title="On-the-Hour Mode">
          <CodeBlock
            language="yaml"
            filename="automessages.yml"
            code={`messages:
  hourly:
    mode: "on-the-hour"
    messages:
      - "<aqua>[Info]</aqua> <gray>Visit our store at store.yourserver.com!</gray>"`}
          />
        </SubSection>

        <SubSection title="On-the-Half-Hour Mode">
          <CodeBlock
            language="yaml"
            filename="automessages.yml"
            code={`messages:
  half-hourly:
    mode: "on-the-half-hour"
    messages:
      - "<aqua>[Info]</aqua> <gray>Join our Discord: /discord</gray>"`}
          />
        </SubSection>
      </Section>

      {/* maintenance/config.yml */}
      <Section id="maintenance-yml" title="maintenance/config.yml &mdash; Maintenance Mode">
        <p className="text-[var(--text-secondary)] mb-4">
          Configure maintenance mode behavior including kick messages, custom MOTD, and bypass groups.
        </p>

        <CodeBlock
          language="yaml"
          filename="maintenance/config.yml"
          code={`kick-message: "<red>Server is currently under maintenance. Please try again later.</red>"

motd:
  line1: "<red>Server Maintenance</red>"
  line2: "<gray>We'll be back shortly!</gray>"

# Custom server icon during maintenance (place in maintenance/ folder)
icon: "maintenance-icon.png"

# LuckPerms groups that can bypass maintenance
allowed-groups:
  - "admin"
  - "moderator"

# Specific players that can bypass (by UUID)
allowed-players:
  - "00000000-0000-0000-0000-000000000000"`}
        />
      </Section>

      {/* stats.yml */}
      <Section id="stats-yml" title="stats.yml &mdash; Stats GUI Configuration">
        <p className="text-[var(--text-secondary)] mb-4">
          Configure the player statistics GUI displayed with <code>/stats</code>.
        </p>

        <CodeBlock
          language="yaml"
          filename="stats.yml"
          code={`gui:
  title: "<dark_gray>Player Statistics</dark_gray>"
  size: 27  # Inventory size (9, 18, 27, 36, 45, 54)
  items:
    playtime:
      slot: 11
      material: CLOCK
      name: "<gold>Playtime</gold>"
      lore:
        - "<gray>Total: %playtime%</gray>"
    balance:
      slot: 13
      material: GOLD_INGOT
      name: "<green>Balance</green>"
      lore:
        - "<gray>Balance: %economy_balance_formatted%</gray>"
    kills:
      slot: 15
      material: DIAMOND_SWORD
      name: "<red>Combat Stats</red>"
      lore:
        - "<gray>Kills: %kills%</gray>"
        - "<gray>Deaths: %deaths%</gray>"
        - "<gray>KDR: %kdr%</gray>"`}
        />
      </Section>

      {/* texts/ folder */}
      <Section id="texts" title="texts/ &mdash; Localization Files">
        <p className="text-[var(--text-secondary)] mb-4">
          JustPlugin ships with 27 YAML localization files in the <code>plugins/JustPlugin/texts/</code> folder.
          Each file handles messages for a specific feature module.
        </p>

        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2 text-sm font-mono">
            {[
              "economy.yml",
              "teleportation.yml",
              "homes.yml",
              "warps.yml",
              "moderation.yml",
              "jail.yml",
              "kits.yml",
              "chat.yml",
              "mail.yml",
              "nicknames.yml",
              "tags.yml",
              "vanish.yml",
              "teams.yml",
              "trade.yml",
              "scoreboard.yml",
              "maintenance.yml",
              "afk.yml",
              "general.yml",
              "errors.yml",
              "gui.yml",
              "motd.yml",
              "automessages.yml",
              "backup.yml",
              "stats.yml",
              "tablist.yml",
              "entityclear.yml",
              "warnings.yml",
            ].map((file) => (
              <div key={file} className="text-[var(--text-muted)] py-1 px-2 rounded bg-[var(--bg-tertiary)]">
                {file}
              </div>
            ))}
          </div>
        </div>

        <ConfigNote>
          All text files support MiniMessage formatting. To customize any message, find the corresponding key
          in the relevant file and modify the value. Use <code>/jpreload</code> to apply changes without restarting.
        </ConfigNote>

        <CodeBlock
          language="yaml"
          filename="texts/economy.yml (example)"
          code={`balance-check: "<green>Your balance: <gold>%balance%</gold></green>"
balance-other: "<green>%player%'s balance: <gold>%balance%</gold></green>"
pay-sent: "<green>You sent <gold>%amount%</gold> to <gold>%player%</gold>.</green>"
pay-received: "<green>You received <gold>%amount%</gold> from <gold>%player%</gold>.</green>"
insufficient-funds: "<red>You don't have enough money!</red>"`}
        />
      </Section>
    </div>
  );
}
