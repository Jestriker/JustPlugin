<p align="center">
  <img src="https://raw.githubusercontent.com/Jestriker/JustPlugin/main/.github/assets/justplugin-logo.svg" alt="Available for Paper" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Jestriker/JustPlugin/main/.github/assets/builtwith-java25.svg" alt="Built with Java 25" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Release%20Version-1.3-blue" alt="Release Version" />
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License" />
  <img src="https://img.shields.io/badge/Available%20on-Paper-purple" alt="Paper" />
  <img src="https://img.shields.io/badge/Built%20with-Java%2025-orange" alt="Java 25" />
  <img src="https://img.shields.io/badge/Available%20on-Folia-cyan" alt="Folia" />
  <img src="https://img.shields.io/bstats/servers/30446" alt="bStats Servers" />
  <img src="https://img.shields.io/discord/1482382694443913249" alt="Discord" />
  <img src="https://img.shields.io/github/stars/Jestriker/JustPlugin" alt="GitHub Stars" />
</p>

<p align="center">
  <b>The only essentials plugin you'll ever need.</b><br>
  A lightweight, fully configurable all-in-one server management plugin for Paper 1.21.11+
</p>

---

## 🚀 Overview

JustPlugin replaces dozens of separate plugins with a single JAR. Economy, teleportation, moderation, teams, trading, vanish, warnings, mutes, skins, scoreboard, maintenance mode, and **100+ commands** - all built from scratch with performance and simplicity in mind.

Every command can be individually enabled or disabled. Every permission is granular and hierarchical. Every feature just works out of the box.

---

## ✨ Feature Highlights

### 💰 Economy
- Full balance system with Vault integration support
- `/balance`, `/pay`, `/paytoggle`, `/baltop`, `/addcash`
- **PayNotes** - enchanted paper items as redeemable balance vouchers
- **Baltop GUI** - interactive leaderboard with player heads, medals, and hidden player support
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
- **Hover stats tooltip** - hovering over a player's name in chat shows configurable stats (balance, kills, deaths, playtime, K/D)
- Click-to-view stats with permission check

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
- Full docs in `ECOSYSTEM.md`

### 📦 More Features
- **Modular Listener Architecture** - event handling split into 6 categorized sub-listeners (connection, chat, combat, player, server, inventory) for clean separation of concerns
- **Web Config Editor** - browser-based config editing with security
- **Discord Webhook Logging** - color-coded embeds for every staff action
- **Entity Clear System** - ClearLag replacement with warnings and notifications
- **Virtual Inventories** - `/anvil`, `/craft`, `/grindstone`, `/enderchest`, and more
- **PlaceholderAPI Support** - all JustPlugin placeholders available to other plugins
- **bStats Metrics** - anonymous usage statistics
- **Stats GUI** - `/stats` opens an interactive stats inventory
- **Config Migration** - auto-adds new settings on upgrade without wiping existing values

---

## 📥 Installation

1. Drop `JustPlugin.jar` into your server's `plugins/` folder
2. Start the server - all config files are auto-generated with comments
3. Customize `config.yml`, `scoreboard.yml`, `motd.yml`, `maintenance/config.yml`, `icon.yml`, `stats.yml`
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
| `SCOREBOARD.md` | Placeholder variables and scoreboard config guide |
| `FORMATTING.md` | MiniMessage formatting guide |
| `ECOSYSTEM.md` | Developer API documentation |

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

Shared state (god mode, death locations, persistence) lives in `PlayerListener`, which acts as a utility class accessed by all sub-listeners via `plugin.getPlayerListener()`.

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






