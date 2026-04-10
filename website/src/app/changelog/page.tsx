"use client";

import PageHeader from "@/components/PageHeader";

interface ChangelogEntry {
  version: string;
  title: string;
  date: string;
  sections: {
    title: string;
    changes: { type: "added" | "changed" | "removed"; text: string }[];
  }[];
}

const changelog: ChangelogEntry[] = [
  {
    version: "1.3",
    title: "Player Vaults, Transaction History, Utilities & Events API",
    date: "April 10, 2026",
    sections: [
      {
        title: "Player Vaults System",
        changes: [
          { type: "added", text: "/pv [number] - 54-slot virtual storage inventories, separate from ender chest" },
          { type: "added", text: "/pv <player> <number> - staff access to view other players' vaults" },
          { type: "added", text: "Configurable max vaults per player (default: 3) with permission-based override (justplugin.vaults.<number>)" },
          { type: "added", text: "Data saved on every close, quit, and auto-save to prevent data loss" },
          { type: "added", text: "Disabled by default - opt-in via vaults.enabled in config.yml" },
        ],
      },
      {
        title: "Transaction History",
        changes: [
          { type: "added", text: "/transactions [player] - paginated GUI showing all economy transactions" },
          { type: "added", text: "Tracks 6 transaction types: PAY, PAYNOTE_CREATE, PAYNOTE_REDEEM, ADDCASH, TRADE, API" },
          { type: "added", text: "Click any transaction to view full details (time, amount, parties involved)" },
          { type: "added", text: "Configurable retention period (default: 30 days) and max entries (default: 500)" },
          { type: "added", text: "Configurable PayNote creator visibility and AddCash staff name visibility" },
        ],
      },
      {
        title: "Near Command",
        changes: [
          { type: "added", text: "/near [radius] - show nearby players with distance, compass direction, and coordinates" },
          { type: "added", text: "Clickable [TP] button with safe teleport for each player" },
          { type: "added", text: "Staff-only command with configurable default radius (1000) and max radius (5000)" },
        ],
      },
      {
        title: "Repair & Enchant Commands",
        changes: [
          { type: "added", text: "/repair [player] - repair held item to maximum durability (self or others)" },
          { type: "added", text: "/enchant <enchantment> [level] - apply enchantments to held items" },
          { type: "added", text: "Level 0 removes the enchantment, configurable vanilla restriction bypass" },
        ],
      },
      {
        title: "Custom Events API",
        changes: [
          { type: "added", text: "10 custom Bukkit events for add-on plugins to listen to" },
          { type: "added", text: "6 cancellable events: PlayerBalanceChangeEvent, PlayerPunishEvent, PlayerTeleportRequestEvent, PlayerTradeEvent, PlayerJailEvent, KitClaimEvent" },
          { type: "added", text: "4 informational events: PlayerUnjailEvent, PlayerAfkEvent, WarpCreateEvent, WarpDeleteEvent" },
          { type: "added", text: "All events use defensive copies for security - no database access exposed" },
        ],
      },
      {
        title: "Documentation & Wiki",
        changes: [
          { type: "added", text: "Global search (Cmd+K / Ctrl+K) with 55+ indexed entries" },
          { type: "added", text: "Breadcrumbs, prev/next navigation, sticky table of contents" },
          { type: "added", text: "Version selector with changelog page and color-coded +/~/- badges" },
          { type: "added", text: "Light/dark mode toggle with animated sun/moon icon" },
          { type: "added", text: "Placeholders reference, Formatting guide, Migration guide, Troubleshooting, Comparison pages" },
          { type: "changed", text: "Theme updated from purple to orange accent color" },
          { type: "changed", text: "Emojis replaced with SVG icons throughout the wiki" },
          { type: "added", text: "Syntax highlighting for Java, YAML, and Groovy code blocks" },
        ],
      },
    ],
  },
  {
    version: "1.3-beta.3",
    title: "Folia Support, Automated Messages & Platform Expansion",
    date: "April 3, 2026",
    sections: [
      {
        title: "Native Folia Support",
        changes: [
          { type: "added", text: "SchedulerUtil compatibility layer - detects Folia at runtime and routes all scheduler calls appropriately" },
          { type: "changed", text: "Replaced 75+ Bukkit.getScheduler() calls across 27 files with Folia-compatible wrappers" },
          { type: "added", text: "Entity-bound, global, async, and location-based schedulers all supported" },
          { type: "changed", text: "All synchronous teleports converted to teleportAsync()" },
          { type: "added", text: "folia-supported: true flag in plugin.yml" },
          { type: "added", text: "Plugin now runs natively on Paper, Purpur, and Folia" },
        ],
      },
      {
        title: "Automated Messages System",
        changes: [
          { type: "added", text: "Configurable automated broadcast messages sent to players at intervals or specific times" },
          { type: "added", text: "4 scheduling modes: interval, schedule (specific times of day), on-the-hour, on-the-half-hour" },
          { type: "added", text: "Rotating message support - cycle through multiple messages" },
          { type: "added", text: "Per-message permission filtering and world filtering" },
          { type: "added", text: "Custom prefix, sound effects, full MiniMessage formatting" },
          { type: "added", text: "/automessage reload|list|toggle|send management commands" },
          { type: "added", text: "Dedicated automessages.yml config file with 8 examples" },
        ],
      },
      {
        title: "Web Editor Expansion",
        changes: [
          { type: "added", text: "Added database.yml, automessages.yml, texts/kits.yml, texts/nick.yml to the browser-based config editor" },
          { type: "changed", text: "All 25+ config files now editable from the web interface" },
        ],
      },
      {
        title: "Platform Support",
        changes: [
          { type: "added", text: "Official support for Paper, Purpur, and Folia" },
          { type: "changed", text: "Version bumped to 1.5" },
        ],
      },
    ],
  },
  {
    version: "1.3-beta.2",
    title: "Database, Jail, Kits, AFK, Mail & 28+ New Features",
    date: "April 3, 2026",
    sections: [
      {
        title: "Database Support",
        changes: [
          { type: "added", text: "Multi-backend storage - choose between SQLite, MySQL, or YAML via database.yml" },
          { type: "added", text: "Switch backends without data loss - all player data, economy, punishments, and more supported" },
        ],
      },
      {
        title: "Jail System",
        changes: [
          { type: "added", text: "/jail <player> [duration] [reason] - jail players with temporary or permanent sentences" },
          { type: "added", text: "/unjail, /setjail, /deljail, /jails, /jailinfo - full jail management" },
          { type: "added", text: "Multiple named jail locations with random selection" },
          { type: "added", text: "Jailed players are restricted from movement, commands, and interactions" },
          { type: "added", text: "Persists across restarts. Works for offline players (jailed on next login)" },
        ],
      },
      {
        title: "Kit System",
        changes: [
          { type: "added", text: "/kit - GUI-based kit selection and claiming" },
          { type: "added", text: "/kitpreview, /kitcreate, /kitedit, /kitrename, /kitdelete" },
          { type: "added", text: "/kitpublish, /kitdisable, /kitenable, /kitarchive, /kitlist" },
          { type: "added", text: "Full lifecycle: Pending -> Published -> Archived" },
          { type: "added", text: "Per-kit permissions, cooldowns, auto-equip armor" },
          { type: "added", text: "Archive retention with configurable auto-delete (default: 30 days)" },
        ],
      },
      {
        title: "AFK System",
        changes: [
          { type: "added", text: "/afk - toggle AFK status manually" },
          { type: "added", text: "Auto-AFK after configurable idle time" },
          { type: "added", text: "Optional idle kick with bypass permission" },
          { type: "added", text: "AFK status shown in tab list and chat" },
          { type: "added", text: "Movement, chat, and interaction auto-clear AFK" },
        ],
      },
      {
        title: "Mail System",
        changes: [
          { type: "added", text: "/mail send <player> <message> - offline messaging to any player" },
          { type: "added", text: "/mail read, /mail clear, /mail clearall - inbox management with pagination" },
          { type: "added", text: "Notifications on login for unread mail" },
        ],
      },
      {
        title: "Nickname System",
        changes: [
          { type: "added", text: "/nick <name> - custom display name with MiniMessage formatting" },
          { type: "added", text: "/nick off / /nick reset - remove nickname" },
          { type: "added", text: "Granular color permissions: justplugin.nick.color, .format, .rainbow" },
          { type: "added", text: "Configurable min/max length validation" },
        ],
      },
      {
        title: "Tag System",
        changes: [
          { type: "added", text: "/tag - GUI-based tag selection and equipping" },
          { type: "added", text: "/tagcreate <id> <prefix|suffix> <display> - admin tag creation" },
          { type: "added", text: "/tagdelete, /taglist - tag management" },
          { type: "added", text: "Tags display as prefixes or suffixes in chat" },
        ],
      },
      {
        title: "Backup & Export System",
        changes: [
          { type: "added", text: "/jpbackup export - create full plugin data backups" },
          { type: "added", text: "/jpbackup import <file> - restore with confirmation" },
          { type: "added", text: "/jpbackup list, /jpbackup delete - backup management" },
          { type: "added", text: "All I/O runs asynchronously" },
        ],
      },
      {
        title: "Offline Player Commands",
        changes: [
          { type: "added", text: "/tpoff <player> - teleport to offline player's last location" },
          { type: "added", text: "/getposoff <player> - view offline player's last known position" },
          { type: "added", text: "/getdeathposoff <player> - view offline player's last death location" },
          { type: "added", text: "/invseeoff <player> - view offline player's inventory" },
          { type: "added", text: "/echestseeoff <player> - view offline player's ender chest" },
        ],
      },
      {
        title: "Spawn & Seed Protection",
        changes: [
          { type: "added", text: "Configurable radius around spawn where building is restricted" },
          { type: "added", text: "Blocks /seed command from non-permitted players with staff notifications" },
        ],
      },
      {
        title: "Custom Join/Leave Messages",
        changes: [
          { type: "added", text: "5 visibility modes: none, all, staff-only, op-only, group-based" },
          { type: "added", text: "Fully configurable message templates" },
        ],
      },
      {
        title: "Performance & Security",
        changes: [
          { type: "changed", text: "Async I/O - all file read/write operations run off the main thread" },
          { type: "changed", text: "Thread safety - concurrent data access protected throughout the plugin" },
          { type: "added", text: "Balance overflow protection - prevents integer overflow in economy operations" },
          { type: "added", text: "Pay rate limiting - prevents payment spam abuse" },
          { type: "added", text: "Input sanitization - all user inputs are validated and sanitized" },
          { type: "changed", text: "Teleport safety enhancements - improved hazard detection" },
          { type: "added", text: "IP ban subnets (CIDR) - ban entire IP ranges" },
          { type: "added", text: "Webhook retry logic - automatic retry with backoff for failed Discord webhook deliveries" },
          { type: "added", text: "Web editor CSRF protection" },
          { type: "changed", text: "Scoreboard flicker fix - eliminates visual flickering on updates" },
          { type: "changed", text: "Placeholder performance - optimized caching for PlaceholderAPI" },
          { type: "changed", text: "Tab completion cache - cached completions for better performance" },
          { type: "changed", text: "Graceful shutdown - all data saved cleanly on server stop" },
        ],
      },
    ],
  },
  {
    version: "1.3-beta.1",
    title: "Architecture Refactor & Listener Modularization",
    date: "March 29, 2026",
    sections: [
      {
        title: "Listener Architecture Refactor",
        changes: [
          { type: "changed", text: "Monolithic PlayerListener split into 6 categorized sub-listeners for better code organization, maintainability, and performance" },
          { type: "changed", text: "ConnectionListener, ChatListener, CombatListener, PlayerEventListener, ServerListener, InventoryListener" },
          { type: "changed", text: "PlayerListener converted to a shared state holder - no longer implements Listener or handles events directly" },
          { type: "changed", text: "PlayerListener class reduced from ~710 lines to ~146 lines (utility methods only)" },
        ],
      },
      {
        title: "Stats GUI",
        changes: [
          { type: "added", text: "New /stats command opens an interactive stats inventory GUI" },
        ],
      },
      {
        title: "Skin System",
        changes: [
          { type: "added", text: "/skin set/clear with Mojang API integration" },
          { type: "added", text: "/skinban / /skinunban for banning specific skin names" },
          { type: "added", text: "Auto-applies stored skins on join" },
        ],
      },
      {
        title: "Maintenance Mode",
        changes: [
          { type: "added", text: "Full maintenance system with kick screen, MOTD override, server icon, LuckPerms group bypass" },
          { type: "added", text: "Cooldown with estimated end time and auto-disable option" },
        ],
      },
    ],
  },
  {
    version: "1.2",
    title: "Ranks, Revamps & Permission Fixes",
    date: "March 21, 2026",
    sections: [
      {
        title: "Ranks System (LuckPerms Integration)",
        changes: [
          { type: "added", text: "New /rank command opens a full management GUI - disabled by default, opt-in for servers running LuckPerms" },
          { type: "added", text: "Groups tab - browse, create, delete, rename, change prefix/suffix, manage parent groups and permission nodes" },
          { type: "added", text: "Players tab - browse all known LuckPerms users, add/remove from groups, view memberships, manage permissions" },
          { type: "added", text: "Chat prefix/suffix - LuckPerms prefixes and suffixes now automatically display in chat" },
          { type: "added", text: "Configurable chat separator between name and message" },
          { type: "added", text: "25+ granular permissions for every rank management action" },
        ],
      },
      {
        title: "Scoreboard Revamp",
        changes: [
          { type: "added", text: "Animated wave gradient title - smoothly shifts colors back and forth" },
          { type: "added", text: "Discord link variable - {discord} and {discord_link} placeholders" },
          { type: "added", text: "Playtime mode toggle - choose between total or session playtime display" },
          { type: "changed", text: "Fast ping refresh - ping checked every 5 seconds on a separate interval" },
          { type: "changed", text: "Updated default design with bold emojis and improved spacing" },
        ],
      },
      {
        title: "RTP Fix",
        changes: [
          { type: "changed", text: "/tpr now correctly applies the pre-teleport cooldown countdown instead of being instant" },
        ],
      },
      {
        title: "Home & Baltop GUIs",
        changes: [
          { type: "changed", text: "Home GUI: team home slot added alongside spawn, improved colors and cooldown integration" },
          { type: "changed", text: "Baltop GUI: renamed to Top Balances, updated formatting" },
        ],
      },
      {
        title: "Tab List",
        changes: [
          { type: "added", text: "{tps} and {ping} placeholders for tab header/footer" },
          { type: "changed", text: "Default footer now shows Players, TPS, and Ping" },
          { type: "changed", text: "Configurable refresh interval (default 5 seconds, was hardcoded 30s)" },
        ],
      },
      {
        title: "Permission Fixes",
        changes: [
          { type: "removed", text: "Removed /back, /kill, /suicide, /enderchest, /anvil, /craft, /playerlist from default player group" },
          { type: "changed", text: "/deathitems changed from player-accessible to admin-only (op default)" },
          { type: "changed", text: "/plugins is now staff-only with justplugin.plugins permission" },
        ],
      },
    ],
  },
  {
    version: "1.1",
    title: "Initial Release",
    date: "March 2026",
    sections: [
      {
        title: "Core Features",
        changes: [
          { type: "added", text: "Full release with 90+ commands" },
          { type: "added", text: "Economy, teleportation, moderation, teams, trading, vanish, warnings, mutes" },
          { type: "added", text: "Scoreboard, tab list, web config editor, Discord webhook logging" },
          { type: "added", text: "Vault integration, entity clear system, safe teleport protection" },
          { type: "added", text: "Developer API for ecosystem plugins" },
        ],
      },
    ],
  },
];

const badgeConfig = {
  added: { label: "+", color: "var(--green)", bg: "var(--green)" },
  changed: { label: "~", color: "var(--yellow)", bg: "var(--yellow)" },
  removed: { label: "-", color: "var(--red)", bg: "var(--red)" },
} as const;

function ChangeBadge({ type }: { type: "added" | "changed" | "removed" }) {
  const config = badgeConfig[type];
  return (
    <span
      className="inline-flex items-center justify-center w-5 h-5 rounded text-xs font-bold flex-shrink-0"
      style={{ backgroundColor: `color-mix(in srgb, ${config.bg} 15%, transparent)`, color: config.color }}
    >
      {config.label}
    </span>
  );
}

export default function ChangelogPage() {
  return (
    <>
      <PageHeader
        title="Changelog"
        description="A complete history of JustPlugin releases, features, and improvements."
      />

      <div className="relative">
        {/* Timeline line */}
        <div className="absolute left-[15px] top-0 bottom-0 w-px bg-[var(--border)] hidden md:block" />

        <div className="space-y-10">
          {changelog.map((entry, i) => (
            <div key={entry.version} className="relative md:pl-10">
              {/* Timeline dot */}
              <div
                className="absolute left-[9px] top-2 w-[13px] h-[13px] rounded-full border-2 hidden md:block"
                style={{
                  borderColor: i === 0 ? "var(--accent)" : "var(--border)",
                  backgroundColor: i === 0 ? "var(--accent)" : "var(--bg-secondary)",
                }}
              />

              {/* Version card */}
              <div className="border border-[var(--border)] rounded-xl overflow-hidden bg-[var(--bg-card)]">
                {/* Header */}
                <div className="px-6 py-4 border-b border-[var(--border)] bg-[var(--bg-tertiary)]">
                  <div className="flex items-center gap-3 flex-wrap">
                    <span
                      className="px-2.5 py-0.5 rounded-full text-xs font-bold text-white"
                      style={{ backgroundColor: i === 0 ? "var(--accent)" : "var(--text-muted)" }}
                    >
                      v{entry.version}
                    </span>
                    <h2 className="text-lg font-semibold">{entry.title}</h2>
                  </div>
                  <p className="text-sm text-[var(--text-muted)] mt-1">{entry.date}</p>
                </div>

                {/* Sections */}
                <div className="divide-y divide-[var(--border)]">
                  {entry.sections.map((section) => (
                    <div key={section.title} className="px-6 py-4">
                      <h3 className="text-sm font-semibold text-[var(--text-secondary)] mb-3">
                        {section.title}
                      </h3>
                      <ul className="space-y-2">
                        {section.changes.map((change, ci) => (
                          <li key={ci} className="flex items-start gap-2.5 text-sm leading-relaxed">
                            <ChangeBadge type={change.type} />
                            <span className="text-[var(--text-primary)]">{change.text}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
