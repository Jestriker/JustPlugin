<p align="center">
  <picture>
    <source srcset="https://raw.githubusercontent.com/Jestriker/JustPlugin/main/.github/assets/justplugin-logo.svg" type="image/svg+xml" />
    <img src="" alt="" width="0" height="0" />
  </picture>
  <picture>
    <source srcset="https://raw.githubusercontent.com/Jestriker/JustPlugin/main/.github/assets/builtwith-java25.svg" type="image/svg+xml" />
    <img src="" alt="" width="0" height="0" />
  </picture>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Release%20Version-1.3-blue" alt="Release Version" />
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License" />
  <img src="https://img.shields.io/badge/Available%20on-Paper-blue?logo=paper" alt="Paper" />
  <img src="https://img.shields.io/badge/Available%20on-Purpur-purple" alt="Purpur" />
  <img src="https://img.shields.io/badge/Available%20on-Folia-green" alt="Folia" />
  <img src="https://img.shields.io/badge/Built%20with-Java%2021-orange" alt="Java 21" />
  <img src="https://img.shields.io/bstats/servers/30446" alt="bStats Servers" />
  <img src="https://img.shields.io/discord/1482382694443913249" alt="Discord" />
  <img src="https://img.shields.io/github/stars/Jestriker/JustPlugin" alt="GitHub Stars" />
</p>

<p align="center">
  <b>The only essentials plugin you'll ever need.</b><br>
  A lightweight, fully configurable all-in-one server management plugin for Paper, Purpur, and Folia 1.21.11+
</p>

---

## 🚀 Overview

JustPlugin replaces dozens of separate plugins with a single JAR. Economy, teleportation, moderation, jail, kits, AFK, mail, nicknames, tags, teams, trading, vanish, warnings, mutes, skins, scoreboard, maintenance mode, and **200+ commands** - all built from scratch with performance and simplicity in mind.

Every command can be individually enabled or disabled. Every permission is granular and hierarchical. Every feature just works out of the box.

---

## ✨ Feature Highlights

### 💰 Economy
- Full balance system with Vault integration support
- `/balance`, `/pay`, `/paytoggle`, `/baltop`, `/addcash`
- **PayNotes** - enchanted paper items as redeemable balance vouchers
- **Baltop GUI** - interactive leaderboard with player heads, medals, and hidden player support
- **Transaction History** - `/transactions` GUI with pagination, detail view, 6 transaction types, configurable retention
- Offline player support for all economy operations

### 🌍 Teleportation
- `/tpa`, `/tpahere`, `/tpaccept`, `/tpreject`, `/spawn`, `/back`, `/tppos`
- `/tpr` - random teleport with **dimension selection GUI** (Overworld, Nether, End)
- **Safe teleport protection** - checks 3x3 area for hazardous blocks before every teleport
- Staff bypass with clickable **[TP Anyway]**, **[Creative Mode]**, **[God Mode]** buttons
- Configurable warmup countdown with per-second messages ("Teleporting in 3... 2... 1...")
- Per-command cooldowns and delays

### 🏠 Warps & Homes
- `/warp`, `/setwarp`, `/delwarp`, `/renamewarp`, `/warps`
- `/home`, `/sethome`, `/delhome` with configurable max homes
- **Home GUI** - click to teleport, set, or delete homes from an interactive inventory
- All persist across server restarts

### 🔨 Moderation
- `/ban`, `/tempban`, `/banip`, `/tempbanip`, `/unban`, `/unbanip`
- `/mute`, `/tempmute`, `/unmute`
- `/warn` - progressive warning system with fully configurable punishment escalation
- `/kick`, `/sudo`, `/invsee`, `/echestsee`, `/deathitems`
- `/oplist`, `/banlist` - paginated lists with full details
- **Punishment announcements** - configurable per-type (staff-only by default)

### 🔒 Jail System
- `/jail <player> [duration] [reason]` - jail a player with optional duration and reason
- `/unjail`, `/setjail`, `/deljail`, `/jails`, `/jailinfo`
- Multiple jail locations with named identifiers
- Temporary and permanent jails with configurable defaults
- Jailed players are restricted from movement, commands, and interactions
- Persists across restarts

### 🎒 Kit System
- `/kit` - opens kit selection GUI, `/kit <name>` to claim directly
- `/kitpreview`, `/kitcreate`, `/kitedit`, `/kitrename`, `/kitdelete`
- `/kitpublish`, `/kitdisable`, `/kitenable`, `/kitarchive`, `/kitlist`
- Full lifecycle management: Pending -> Published -> Archived
- Per-kit permissions (`justplugin.kits.<name>`), cooldowns, and auto-equip armor
- GUI preview and selection interface
- Full docs in `KITS.md`

### 👻 Vanish
- `/vanish` - hidden from tab, player count, name autocomplete, and server list
- `/supervanish` - spectator-based ghost mode (no block/item/redstone/sound interactions)
- Both modes persist across restarts

### 👥 Teams
- `/team create/invite/join/leave/kick/disband/list/info`
- `/teammsg` and `/chat all/team` for team-only messaging
- Team homes with shared teleportation

### 🤝 Trading
- `/trade` - Hypixel SkyBlock-style trading GUI
- Balance transfer with sign input (supports `500k`, `1m` shorthand)
- 5-second countdown with visual feedback, sound effects, and cancel protection

### 🎨 Skin Restorer
- `/skin set <name>` - set your skin to any player's skin (Mojang API)
- `/skin set <name> <player>` - set another player's skin (staff)
- `/skin clear` - reset to your default username skin
- `/skinban` / `/skinunban` - ban/unban specific skin names
- Auto-applies stored skins on join (works for cracked players)
- **Disabled by default** - opt-in via config

### 🔧 Maintenance Mode
- `/maintenance mode on/off` - block non-whitelisted joins
- `/maintenance allowed-users/allowed-groups` - whitelist management
- `/maintenance cooldown <duration>` - set estimated end time
- Custom kick screen, MOTD override, server icon, and LuckPerms group bypass
- OP bypass enabled by default
- Configurable auto-disable when cooldown expires

### 📊 Scoreboard
- **50+ placeholder variables** - balance, kills, deaths, playtime, ping, TPS, team, and more
- **Animated wave gradient title** with configurable colors and speed
- **5 text animation types** - rainbow gradient, typing, sweep left/right, wave
- Conditional lines (e.g., team line only shows if in a team)
- Compact number formatting (`25.7K`, `1.5M`, `2.3B`)
- Fast ping refresh on a separate interval
- Full config in `scoreboard.yml`

### 📋 Tab List
- Custom header/footer with all 50+ placeholders and animations (`{anim:name}`)
- `{online_staff}` placeholder - counts online staff from configurable LuckPerms groups + OPs
- `{welcome_name}` - shows "Welcome PlayerName"
- Optional maintenance warning footer line (auto-shows/hides)
- Configurable refresh interval

### 🌐 MOTD Profiles
- Multiple server MOTD profiles with three modes: **static**, **cycle**, **random**
- Configurable cycle delay (`1s`, `30s`, `1m`, `1h`, `500ms`)
- Random mode shows a different MOTD on each server list refresh
- Separate join MOTD for player login messages
- Custom server icon from URL with auto-resize and caching

### 💬 Chat
- `/msg`, `/r` with clickable **[Reply]** buttons
- `/ignore add/remove/list/clearlist`
- `/announce`, `/clearchat`, `/itemname`, `/shareitem`
- `/mail send/read/clear/clearall` - offline mail system
- **Hover stats tooltip** - hovering over a player's name in chat shows configurable stats (balance, kills, deaths, playtime, K/D)
- Click-to-view stats with permission check
- **Custom join/leave messages** - 5 modes: none, all, staff-only, op-only, group-based

### 💤 AFK System
- `/afk` - toggle AFK status manually
- **Auto-AFK** - players are automatically marked AFK after configurable idle time
- **Idle kick** - optionally kick players who remain AFK too long
- AFK status shown in tab list and chat

### 📧 Mail System
- `/mail send <player> <message>` - send mail to online or offline players
- `/mail read` - read your inbox with pagination
- `/mail clear` - clear your own mail
- `/mail clearall` - clear all mail (admin)
- Notifications on login for unread mail

### 🎨 Nickname & Tags
- `/nick <name>` - set a custom display name with MiniMessage formatting
- `/nick off` / `/nick reset` - remove your nickname
- Color permissions: `justplugin.nick.color`, `.format`, `.rainbow`
- `/tag` - opens tag selection GUI
- `/tagcreate <id> <prefix|suffix> <display>` - create custom tags
- `/tagdelete <id>`, `/taglist` - manage server tags
- Tags display as prefixes or suffixes in chat

### 🏷️ Ranks (LuckPerms)
- `/rank` - full management GUI for groups and players
- Create, delete, rename, set prefix/suffix/parent, manage permissions
- 25+ granular rank management permissions
- **Disabled by default** - LuckPerms is completely optional

### 🔌 Developer API
- Public API for add-on plugins
- **EconomyAPI** - balance operations
- **PunishmentAPI** - bans, mutes, warns
- **VanishAPI** - vanish state checks
- **10 Custom Events** - PlayerBalanceChangeEvent, PlayerPunishEvent, PlayerTeleportRequestEvent, PlayerTradeEvent, PlayerJailEvent, PlayerUnjailEvent, PlayerAfkEvent, KitClaimEvent, WarpCreateEvent, WarpDeleteEvent (6 cancellable)
- Full docs in `ECOSYSTEM.md`

### 💾 Database Support
- **SQLite**, **MySQL**, and **YAML** storage backends
- Configured via `database.yml` - switch backends without data loss
- All player data, economy, punishments, and more stored in the configured backend

### 📦 Backup & Export
- `/jpbackup export` - create a full plugin data backup
- `/jpbackup import <file>` - restore from a backup with confirmation
- `/jpbackup list` - list available backups
- `/jpbackup delete <file>` - delete a backup
- All backup I/O runs asynchronously

### 🔒 Spawn Protection
- Configurable radius around spawn where building is restricted
- Disabled by default - opt-in via `config.yml`
- Bypass with `justplugin.spawnprotection.bypass`

### 🌱 Seed Protection
- Blocks the `/seed` command from non-permitted players
- Notify staff when someone attempts to use it
- Bypass with `justplugin.seedprotection.bypass`

### 📡 Offline Player Commands
- `/tpoff <player>` - teleport to an offline player's last location
- `/getposoff <player>` - view an offline player's last known position
- `/getdeathposoff <player>` - view an offline player's last death location
- `/invseeoff <player>` - view an offline player's inventory
- `/echestseeoff <player>` - view an offline player's ender chest

### 📢 Automated Messages
- Configurable automated broadcast messages at intervals or specific times
- 4 scheduling modes: `interval` (e.g. every 10m), `schedule` (specific times), `on-the-hour`, `on-the-half-hour`
- Rotating message support (cycle through a list of tips/reminders)
- Per-message permission and world filtering
- Custom prefix, sound effects, MiniMessage formatting
- `/automessage reload|list|toggle|send` for management
- Fully configured in dedicated `automessages.yml`

### 🗄️ Player Vaults
- `/pv [number]` - 54-slot virtual storage per vault, separate from ender chest
- `/pv <player> <number>` - staff can view other players' vaults
- Configurable max vaults per player (default 3), per-player override via `justplugin.vaults.<number>`
- **Disabled by default** - opt-in via `vaults.enabled` in `config.yml`
- Data never lost - saves on close, quit, and auto-save

### 🔧 Utility Commands
- `/near [radius]` - show nearby players with distance, compass direction, coordinates, and clickable [TP] button
- `/repair [player]` - repair held item to max durability
- `/enchant <enchantment> [level]` - apply enchantments, level 0 removes, configurable restriction bypass

### 📦 More Features
- **Modular Listener Architecture** - event handling split into 6 categorized sub-listeners (connection, chat, combat, player, server, inventory) for clean separation of concerns
- **Web Config Editor** - browser-based config editing with security (CSRF protection)
- **Discord Webhook Logging** - color-coded embeds for every staff action (with retry logic)
- **Entity Clear System** - ClearLag replacement with warnings and notifications
- **Virtual Inventories** - `/anvil`, `/craft`, `/grindstone`, `/enderchest`, `/pv` (player vaults), and more
- **PlaceholderAPI Support** - all JustPlugin placeholders available to other plugins (optimized caching)
- **bStats Metrics** - anonymous usage statistics
- **Stats GUI** - `/stats` opens an interactive stats inventory
- **Config Migration** - auto-adds new settings on upgrade without wiping existing values
- **Async I/O** - all file operations run off the main thread for better performance
- **Thread Safety** - concurrent data access protected throughout the plugin
- **Graceful Shutdown** - all data saved cleanly on server stop
- **IP Ban Subnets** - CIDR notation support for IP bans (e.g., `192.168.1.0/24`)
- **Input Sanitization** - all user inputs are validated and sanitized
- **Tab Completion Cache** - cached tab completions for better performance

---

## 📥 Installation

1. Drop `JustPlugin.jar` into your server's `plugins/` folder
2. Start the server - all config files are auto-generated with comments
3. Customize `config.yml`, `database.yml`, `scoreboard.yml`, `motd.yml`, `maintenance/config.yml`, `icon.yml`, `stats.yml`
4. Set up permissions with [LuckPerms](https://luckperms.net/) or any permissions plugin
5. Optionally configure Discord webhook logging with `/setlogswebhook <url>`

---

## 📋 Requirements

- **Paper** 1.21.11 or newer (or any Paper fork like Purpur)
- **Java** 21 or newer
- No required external dependencies - everything is self-contained

## 🔗 Optional Dependencies

| Plugin | Purpose |
|--------|---------|
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Use an external economy provider instead of the built-in balance system |
| [LuckPerms](https://luckperms.net/) | Enable `/rank` GUI, chat prefix/suffix, staff group detection, maintenance group bypass |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Expose all JustPlugin placeholders to other plugins |

## 🖥️ Supported Platforms

| Platform | Supported |
|----------|-----------|
| Paper | ✅ Yes |
| Purpur | ✅ Yes |
| Folia | ✅ Yes |
| Spigot | ❌ No (Paper-exclusive APIs) |
| Bukkit | ❌ No |
| Sponge | ❌ No |
| Proxy (BungeeCord/Velocity) | ❌ Not applicable |

---

## 🔑 Permissions

- `justplugin.*` - grants everything (OP-only by default)
- `justplugin.player` - basic player permissions (default: everyone)
- Every `.others` permission automatically grants the matching self permission
- Cooldown bypass and safe teleport bypass are **never** included in wildcard - must be explicitly granted
- Full permission tree in `PERMISSIONS.md`

---

## 📖 Documentation

| File | Contents |
|------|----------|
| `PERMISSIONS.md` | Complete permission list with hierarchy |
| `COMMANDS.md` | All commands with usage and permissions |
| `KITS.md` | Kit system guide with lifecycle and configuration |
| `SCOREBOARD.md` | Placeholder variables and scoreboard config guide |
| `FORMATTING.md` | MiniMessage formatting guide |
| `ECOSYSTEM.md` | Developer API documentation |
| `CHANGELOG.md` | Version history and release notes |

---

## 🏗️ Architecture

JustPlugin uses a **modular listener architecture**. Event handling is split into categorized sub-listeners for clean separation of concerns:

| Listener | Package | Handles |
|----------|---------|---------|
| `ConnectionListener` | `listeners.connection` | Login, join, quit (bans, maintenance, MOTD, data, vanish, scoreboard) |
| `ChatListener` | `listeners.chat` | Async chat (mute, formatting, hover stats, team chat, ignore) |
| `CombatListener` | `listeners.combat` | Damage (god mode, hunger, potion effects, teleport cancel) |
| `PlayerEventListener` | `listeners.player` | Death, respawn, teleport, movement, advancements |
| `ServerListener` | `listeners.server` | Server list ping (MOTD, icons, vanish hiding), tab completion |
| `InventoryListener` | `listeners.inventory` | PayNote redemption |
| `JailListener` | `listeners.jail` | Jail movement/command/interaction restrictions |
| `SpawnProtectionListener` | `listeners.spawn` | Spawn area build protection |
| `SeedProtectionListener` | `listeners.seed` | /seed command blocking |
| `AfkListener` | `listeners.player` | AFK detection (movement, chat, interaction) |

Shared state (god mode, death locations, persistence) lives in `PlayerListener`, which acts as a utility class accessed by all sub-listeners via `plugin.getPlayerListener()`.

### Manager Architecture

JustPlugin uses 40+ specialized manager classes for clean separation of concerns:

| Manager | Purpose |
|---------|---------|
| `DatabaseManager` | SQLite/MySQL/YAML storage backend |
| `JailManager` | Jail locations, jailed players, durations |
| `KitManager` | Kit definitions, cooldowns, lifecycle |
| `AfkManager` | AFK state, auto-AFK, idle kick |
| `MailManager` | Offline mail storage and delivery |
| `NickManager` | Nickname storage and formatting |
| `TagManager` | Tag definitions and player tag assignments |
| `BackupManager` | Async backup/restore operations |
| `SpawnProtectionManager` | Spawn area protection radius |
| ...and 30+ more | Economy, warps, homes, punishments, vanish, teams, etc. |

---

## 📊 Stats

[![bStats](https://bstats.org/signatures/bukkit/JustPlugin.svg)](https://bstats.org/plugin/bukkit/JustPlugin/30446)

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/Jestriker">Jestriker</a>
</p>






