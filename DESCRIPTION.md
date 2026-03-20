# JustPlugin - All-in-One Server Essentials

**The only essentials plugin you'll ever need for Paper 1.21.11+**

JustPlugin is a lightweight, fully configurable server management plugin that replaces dozens of separate plugins with a single JAR. Economy, teleportation, moderation, teams, trading, vanish, warnings, mutes, and over 90 commands - all built from scratch with performance and simplicity in mind.

Every command can be turned on or off individually. Every permission is granular and hierarchical. Every feature just works.

---

## Features

### Economy
- Full balance system with configurable starting amount and currency symbol
- **Vault support** - optionally use Vault's economy API instead of the built-in system. Set `economy.provider: "vault"` in config and all JustPlugin economy commands will read/write through Vault. Requires a Vault-compatible economy plugin (e.g. EssentialsX Economy, CMI). Falls back to JustPlugin's built-in system if Vault is not found
- `/balance`, `/pay`, `/paytoggle`, `/baltop`, `/addcash`
- **PayNotes** - enchanted paper items that act as redeemable balance vouchers. Hold a single paper, run the command, and it becomes a glowing right-clickable note worth the amount you set. Right-click to redeem the note and add the balance to your account
- Offline player support - pay, check, and modify balances even when players are offline
- **Baltop GUI** - `/baltop` opens a 4-row inventory with player head skulls for the top 10 richest players. Hover over a head to see name, balance, and rank. Medal icons for #1 gold, #2 silver, #3 bronze. Your own rank is shown in the bottom row. Hidden players appear as barriers with obfuscated names for regular users, while staff with the `baltop.viewhidden` permission see their real info. Console gets a text-based fallback
- Separate permissions for self vs. others on balance checking, cash adding, and leaderboard hiding

### Teleportation
- `/tpa`, `/tpahere`, `/tpaccept`, `/tpreject`, `/tpacancel`
- `/spawn`, `/setspawn`, `/back`, `/tppos`, `/tpr` (random wild teleport)
- Configurable request timeout (default 60s), teleport warmup delay (default 3s), and per-feature cooldowns
- **Safe teleport protection** - every teleport (TPA, TPAHere, warp, spawn, home, back) checks a 3x3 area around the destination for hazardous blocks (lava, fire, magma, cactus, pressure plates, tripwire, sculk sensors, pointed dripstone, strings connected to tripwire hooks, and more). Checks the standing block, the two blocks the player occupies, and the surrounding blocks. If unsafe, teleportation is blocked entirely
- Staff with bypass permissions get clickable **[TP Anyway]**, **[Creative Mode]**, and **[God Mode]** buttons - only shown to those with the matching permissions. Non-staff are simply told the destination is unsafe
- Movement or taking damage during the warmup cancels the teleport
- Cooldowns apply to everyone, including OPs - only explicit bypass permissions skip them
- Random teleport (`/tpr`) opens a **dimension selection GUI** - choose between Overworld (grass block), Nether (netherrack), and The End (end stone). Each dimension requires its own permission, and disabled dimensions are shown as unavailable. Finds a safe, solid block within the configurable wild range - never water, lava, magma, or air. Includes cooldowns and multiple retry attempts for safe locations

### Warps
- `/warp`, `/setwarp`, `/delwarp`, `/renamewarp`, `/warps`
- Clickable warp names in the warp list
- Safety checks, warmup delays, and cooldowns on every warp
- Warps persist across server restarts

### Homes
- `/home`, `/sethome`, `/delhome` with a configurable max homes limit per player
- **Interactive GUI** - running `/home` without arguments opens a 4-row inventory with your spawn point, all your home slots (green beds for set homes, gray for available, red for locked), and action dyes below each slot (yellow to set, red to delete with confirmation, black for unavailable). Click a green bed to teleport, click the spawn banner to go to spawn - all with cooldowns and safety checks built in
- Running `/home <name>` still directly teleports to that home
- Safety checks, warmup delays, and cooldowns
- Homes persist across server restarts and player relogs

### Moderation
- `/ban`, `/tempban`, `/unban` - bans by both UUID and username simultaneously, so players can't evade by changing names
- `/banip`, `/tempbanip`, `/unbanip` - IP bans with automatic name/UUID/IP lookup. Works with offline players using their last recorded IP. Also, it bans UUIDs and usernames, so changing the IP alone won't help. Localhost IPs are handled intelligently
- `/mute`, `/tempmute`, `/unmute` - muted players can't use chat or `/msg`/`/r`. Default reasons configurable
- `/warn add/remove/list` - progressive warning system with fully configurable punishments per level. Default escalation: chat warning, kick, 5-minute temp ban, 1-day temp ban, 30-day temp ban, 1-year temp ban, permanent ban. Every level's action is customizable (ChatMessage, Kick, TempBan, Ban, ChatMute, ChatTempMute, NoPunishment). Warning removal requires clickable confirmation buttons and keeps a full history for documentation
- `/kick`, `/sudo`, `/invsee`, `/echestsee`
- `/invsee` - view and manipulate a player's full inventory including armor slots and offhand. Works on offline players too. Armor slots show orange glass indicators when empty and allow placing/removing armor. Updates in real-time
- `/echestsee` - view and manipulate a player's ender chest. Updates in real-time
- `/deathitems` - view and restore items from a player's last death (only if items were dropped, not if keepInventory kept them). Separate permissions for self vs. others
- `/oplist` - list all server operators with online/offline status
- `/banlist`, `/baniplist` - paginated ban and IP ban lists with full details (reason, banned-by, date, duration, IP, UUID)
- Custom ban screens with appeal information (links to your Discord or any URL)
- **Punishment announcements** - by default, punishments are only visible to staff with the matching permission (e.g., `justplugin.announce.ban`). Public announcements can be enabled per-punishment type in the config

### Vanish
- `/vanish` - invisible, hidden from tab list, player count, name autocomplete, and server player list (even external pings). Fake quit message on enable, fake join on disable. Vanished players can't be targeted by `/tpa`, `/trade`, `/msg`, or tab-completed by other players
- `/supervanish` - spectator-based full ghost mode. Players in super vanish cannot: pick up or drop items, break or place blocks, earn or display advancements, trigger redstone (pressure plates, tripwire, sculk sensors), open chests visibly or audibly, shoot projectiles, throw items, use fishing rods, throw enderpearls or snowballs, or interact with the world in any detectable way. Essentially invisible ghosts
- Both vanish modes persist across server restarts and relogs
- Separate permissions for vanishing yourself vs. vanishing others

### Teams
- `/team create`, `/team invite`, `/team join`, `/team leave`, `/team kick`, `/team disband`, `/team list`, `/team info`
- `/teammsg` - send a message to your team without changing your chat mode
- `/chat all/team` - switch your persistent chat mode

### Trading
- `/trade` - opens a Hypixel SkyBlock-style trading GUI
- Double chest window with a 4x5 item area per player, glass pane divider, and accept/decline wool buttons
- Balance transfer support - click the grey dye to type an amount on a sign (supports k/m shorthand like `500k`, `1m`). Displays as a gold nugget, ingot, or block based on the value
- Sound effects for adding/removing items, changing cash amounts, accepting, and the countdown
- 5-second countdown when both players accept, shown as glass pane color changes. Removing acceptance or closing the window cancels the trade
- Trade requests expire after 60 seconds (configurable). Ignored players can't send trade requests. Requests cancel if either player logs off

### Player Commands
- `/fly`, `/god`, `/speed`, `/flyspeed`, `/walkspeed`
- `/gm`, `/gmc`, `/gms`, `/gma`, `/gmsp`, `/gmcheck`
- `/heal`, `/feed`, `/exp` (supports both XP orbs and levels, for self and others)
- `/hat`, `/skull`, `/suicide`
- `/kill` - kill yourself, another player, or mass-kill entities with options: `mobs`, `hostile`, `friendly`, `items`, `entities`, `players`, `allplayers`, `everything`
- `/getpos`, `/getdeathpos` - staff get clickable **[TP to Player]** and **[TP to Coords]** buttons with safety indicators showing if the destination is safe or unsafe
- `/sharecoords`, `/sharedeathcoords` - broadcast coordinates to global or team chat
- `/coords`, `/deathcoords` - view your own position or death location privately (visible only to you)
- Fly, god, speed, and vanish states all persist across server restarts and relogs
- God mode fully heals the player, fills their food bar, and pauses negative effects until disabled. On disable, suggests `/effect clear` if effects are present
- `/speed` is dynamic - sets fly speed if flying, walk speed if walking. `/flyspeed` and `/walkspeed` always target their respective mode
- Separate permissions for self vs. others on fly, god, speed, heal, feed, exp, gamemode, and kill

### Chat
- `/msg`, `/r` with a clickable **[Reply]** button on every received private message (configurable)
- `/ignore add/remove/list/clearlist` - blocks messages, TPA requests, trade requests, and all private contact from ignored players. The ignored player is notified they've been ignored
- `/announce` - server-wide announcements with MiniMessage formatting and `&` color code support (`&1`, `&2`, `&a`, `&l`, etc.)
- `/clearchat` - clear the chat for all online players with optional reason. Logs to staff and webhook
- `/itemname` - rename your held item with MiniMessage and `&` color code support
- `/teammsg` - send a one-off message to your team without changing your current chat mode

### Items
- `/shareitem` - show your currently held item in chat for all players to see (with hover details)
- `/setspawner` - change a mob spawner's entity type by looking at it

### Info and Utilities
- `/help` and `/?` - paginated command list with clickable command suggestions (overrides vanilla `/help`)
- `/playerlist` - paginated player list sorted with staff first, vanish and hidden indicators for staff, and clickable page navigation. Staff with the right permission see hidden entries marked with a **[Hidden]** hover tag
- `/playerlisthide` - hide yourself or others from the player list. Staff-only with separate permissions for self vs. others
- `/playerinfo` - shows health (20.0/20.0), food (20.0/20.0), IP, gamemode, world, coordinates, balance, and more. Works for offline players too (shows UUID, last seen, first played, ban status)
- `/clock`, `/date` - real-world and in-game time in your configured timezone
- `/motd`, `/resetmotd` - view, set, or reset the MOTDs. Two separate MOTDs: **server-motd** (shown in the Minecraft server list / multiplayer screen) and **join-motd** (shown to players in chat when they join). Both support MiniMessage formatting and placeholders (`{player}`, `{online}`, `{max}`). Set with `/motd server <text>` or `/motd join <text>`. Reset with `/resetmotd server`, `/resetmotd join`, or `/resetmotd` (both)
- `/discord` - display or set the server's Discord link (separate permission for setting)
- `/plist` - custom formatted to match the plugin's style
- `/plugins` - staff-only plugin list with enable/disable status (overrides vanilla `/plugins`)
- `/jpinfo` - plugin version and author info

### World
- `/weather clear/rain/thunder`
- `/time set/add/query`
- `/freezegame` / `/unfreezegame` (`/tf`, `/unft`) - shortcuts for tick freeze and unfreeze with custom plugin-style output
- `/clearentities` (`/clearlag`) - manually trigger entity clearing
- `/friendlyfire` (`/ff`, `/pvp`) - toggle PvP on or off server-wide with logging

### Virtual Inventories
Open crafting stations anywhere without placing blocks:
- `/anvil`, `/grindstone`, `/craft`, `/stonecutter`, `/loom`, `/smithingtable`, `/enchantingtable`, `/enderchest`

### Tab List
- Custom tab list header and footer with MiniMessage formatting and `{player}`, `{online}`, `{max}`, `{tps}`, `{ping}` placeholders
- Default footer shows **Players**, **TPS**, and **Ping** in a clean format
- Configurable refresh interval (default 5 seconds) to keep TPS and Ping up-to-date
- Configurable in `config.yml` under `tab.header`, `tab.footer`, and `tab.refresh-interval`
- Auto-applied on join and refreshed at the configured interval
- `/tab` - manually refresh the tab header/footer

### Entity Clear System (ClearLag replacement)
- Automatic clearing of ground items and optionally mobs at a configurable interval (default: 5 minutes)
- Warning messages sent to all players before each clear (configurable timing and message with placeholders)
- Staff notifications for excessive entities per chunk (mobs, armor stands, item frames)
- Manual trigger via `/clearentities`
- Configurable what to clear: items, hostile mobs, friendly mobs
- Named entities, tamed animals, and persistent entities are never cleared
- Post-clear message with `%items%`, `%mobs%`, `%total%` placeholders
- All clears logged to staff and Discord webhook

### Discord Webhook Logging
- Every staff action, moderation event, economy change, administrative command, and automated task logged to a Discord channel via webhook
- Rich embed format with color-coded categories (red for moderation, green for economy, blue for teleport, purple for vanish, orange for gamemode, yellow for player state, pink for admin, teal for items, dark orange for warnings, dark red for mutes)
- Set up in-game with `/setlogswebhook` - includes a test message, confirmation flow, and retry option (10s cooldown between retries)
- **Vanilla command logging** - commands like `/op`, `/give`, `/gamemode`, `/kick`, `/summon`, `/gamerule`, `/whitelist`, `/stop`, `/restart`, and many more are logged to staff chat and webhook. Full list is configurable in config
- Per-category permissions control which log types staff members see in-game chat
- All logs are always printed to console regardless of settings

### Web Config Editor
- Self-hosted config editor accessible via a web browser when enabled
- Beautiful, responsive editing interface served on a configurable port (default: 8585)
- Changes are staged and require running `/applyedits <code>` in-game by a player with the highest-level admin permission
- Session codes expire after 10 minutes for security
- No external dependencies - runs entirely within the plugin

### Scoreboard System
- Fully configurable sidebar scoreboard with **50+ placeholder variables**
- Dedicated `scoreboard.yml` config file with deep customization options
- **Animated wave gradient title** - the scoreboard title gradient smoothly shifts its colors back and forth, creating a dynamic wave effect. Configurable colors, speed (default 1 frame per second), and can be disabled for a static title
- **Default design** inspired by popular SMP servers: Money (green), Kills (red), Deaths (orange), Playtime (yellow), Team (blue, only shown if player is in a team), and a footer with ping and time
- **Compact number formatting** - `{balance_short}`, `{kills_short}`, `{deaths_short}` format numbers with K, M, B suffixes (e.g. 25.70K, 1.50M, 2.30B)
- **Discord link variable** - use `{discord}` or `{discord_link}` on any line to display the server's Discord invite link. Shows "Undefined Discord Link" if not configured
- **Conditional lines** - lines can have a `condition` field (e.g. `has_team`) so they only display when the condition is met. Team line automatically hides if you're not in a team
- **Configurable time format** - `time-format` setting in scoreboard.yml supports any Java DateTimeFormatter pattern: `HH:mm`, `HH:mm:ss`, `hh:mm a`, `dd/MM/YYYY`, `MM/dd/YYYY`, and more
- **Fast ping refresh** - ping values are checked every 5 seconds (configurable) on a separate interval from the main scoreboard update. If a player's ping changes, their scoreboard is immediately refreshed
- Session-based playtime that resets when you reconnect (separate from total playtime)
- **Playtime mode toggle** - `{playtime_display}` placeholder resolves to either total or session playtime based on `default-playtime` setting in scoreboard.yml. Default is total playtime. Use `{total_playtime}` or `{session_playtime}` directly to bypass the setting
- Per-player data: balance, health, food, coordinates, biome, ping, kills, deaths, K/D, playtime, team, and more
- Server data: TPS, online players, memory usage, uptime, weather, real-world time/date
- Configurable emoji system with global and per-line toggles, custom emoji per line
- Configurable title, line order, footer, update interval (default: 1 second)
- Always visible to all players when enabled in config (no per-player toggle)
- Staff-only reload with `/reloadscoreboard` or `/reloadsb` to apply config changes live
- Performance-optimized with caching for expensive stat lookups
- Full reference guide in `SCOREBOARD.md`

### Developer API (Ecosystem)
- Public API for add-on plugins to integrate with JustPlugin's economy, punishment, and vanish systems
- **EconomyAPI** - `getBalance`, `setBalance`, `addBalance`, `removeBalance`, `pay`, `format`, `hasBalance`
- **PunishmentAPI** - `isBanned`, `ban`, `tempBan`, `unban`, `isMuted`, `mute`, `tempMute`, `unmute`, `getMuteReason`, `getActiveWarnCount`, `getTotalWarnCount`, `addWarn`, `liftWarn`
- **VanishAPI** - `isVanished`, `isSuperVanished`
- Build chest shops, auction houses, voting rewards, or any plugin that needs to interact with balances, bans, mutes, warns, or vanish. Full developer guide included in `ECOSYSTEM.md`

### Ranks System (LuckPerms Integration)
- **Disabled by default** - opt-in feature, LuckPerms is completely optional
- `/rank` opens a full management GUI with two tabs: **Groups** and **Players**
- **Groups tab** - browse, search, and manage all LuckPerms groups. Default group is pinned to the top. Actions include: Create, Delete, Rename (display name), Change Prefix, Change Suffix, Set/Remove Parent (inheritance), and full Permission Node management (add, toggle, remove, search/filter)
- **Players tab** - browse and search all known LuckPerms users. Actions include: Add to Group, Remove from Group (default group is protected), View Groups, and full Permission Node management
- **Chat prefix/suffix integration** - LuckPerms prefixes and suffixes automatically display in chat. Only the highest-priority prefix/suffix is shown (resolved from all groups, inheritance, and player-specific nodes). Configurable chat separator (`»`, `:`, `>`, `-`, etc.) in `config.yml`
- Type `clear` when editing a prefix, suffix, or display name to remove it. Type `cancel` to keep the old value
- Every action has its own granular permission (25+ rank management permissions, all `op`-default)
- If LuckPerms is not installed, players are informed when they try to use `/rank`
- Startup console warning if the system is enabled but LuckPerms is missing

### Startup and Console
- Distinctive LuckPerms-style ASCII art banner in the console on startup showing version, server info, command count, and status of every system (economy provider, teams, warps, punishments, webhook, web editor, scoreboard)
- Automatic dependency warnings at startup if related features are enabled but their dependencies are disabled (e.g. `/pay` enabled but `/balance` disabled, or Vault configured but not found)
- Load time displayed on startup

---

## Configuration

Everything is controlled through multiple config files. `config.yml` auto-migrates when the plugin updates - new settings are added automatically while preserving all existing values:

- **`config.yml`** - Main configuration for commands, economy, teleportation, trade, homes, warnings, web editor, webhook, tab list, vanilla command logging, clickable commands, punishment announcements, and per-command enable/disable and permission overrides
- **`motd.yml`** - Two separate MOTDs: `server-motd` (server list) and `join-motd` (player join message). Auto-migrated from old config.yml or old single-motd format on first load
- **`scoreboard.yml`** - Sidebar scoreboard configuration with title, lines, emoji settings, and update interval

Key config features:

- **Enable or disable any command** individually, regardless of permissions
- **Override permission nodes** per command
- **Teleport settings** - warmup delay, request timeout, per-feature cooldowns, safe teleport toggles per feature (TPA, TPAHere, warp, spawn, home, back)
- **Economy** - starting balance, currency symbol, economy provider (`justplugin` or `vault`)
- **Warning punishments** - define the action for each warning level with full customization
- **Clickable commands** - toggle clickable chat buttons for TPA, teams, trades, warps, homes, help, ignore, and private message replies
- **Tab list** - custom header and footer with `{player}`, `{online}`, `{max}`, `{tps}`, `{ping}` placeholders and configurable refresh interval
- **Default reasons** - configurable default reasons for bans, kicks, mutes, and temp bans
- **Chat format** - configurable separator between player name and message (default `»`, supports MiniMessage)
- **Punishment announcements** - per-punishment type toggle for broadcasting to all players (disabled by default; staff always see them)
- **Entity clear** - interval, warning timing, what to clear, public announcements, threshold alerts
- **Vanilla command logging** - fully customizable list of vanilla commands to log
- **Timezone** - for `/clock` and `/date` commands
- **Discord link** - shown by `/discord`, editable in-game
- **Webhook** - URL, enable/disable
- **Web editor** - enable/disable, port, bind address
- **Scoreboard** - enable/disable, update interval, global emoji toggle, title, lines with per-line emoji and placeholder config
- **MOTD** - separate server list MOTD and player join MOTD, both with MiniMessage and placeholders

---

## Permissions

JustPlugin uses a clean, hierarchical permission system:

- `justplugin.*` - grants everything (OP-only by default)
- `justplugin.player` - basic player permissions (granted to everyone by default). Commands like `/back`, `/kill`, `/suicide`, `/enderchest`, `/anvil`, `/craft`, and `/playerlist` are **not** included - they require explicit permission grants
- Every `.others` permission automatically grants the matching self permission
- Cooldown bypass permissions (`*.nocooldown`) are **not** included in `justplugin.*` - even OPs have cooldowns unless explicitly granted
- Safe teleport bypass permissions (`*.unsafetp`) show a confirmation prompt instead of silently teleporting
- Punishment announcements have per-type permissions (`justplugin.announce.ban`, `.kick`, `.mute`, `.warn`, etc.)
- Logging permissions are per-category (`justplugin.log.moderation`, `.economy`, `.teleport`, `.vanish`, `.gamemode`, `.player`, `.admin`, `.item`, `.vanilla`, `.warn`, `.mute`)

Full permission documentation with hierarchy tree is included in `PERMISSIONS.md`.

---

## Installation

1. Drop `JustPlugin.jar` into your server's `plugins/` folder
2. Start the server - config files are generated automatically (`config.yml`, `motd.yml`, `scoreboard.yml`) with full comments
3. Customize `config.yml` for commands, economy, teleportation, and general settings
4. Customize `scoreboard.yml` for the sidebar scoreboard (title, lines, emojis, placeholders)
5. Customize `motd.yml` for the server list MOTD and player join message
6. Set up permissions with [LuckPerms](https://luckperms.net/) or any permissions plugin
7. Optionally configure Discord webhook logging with `/setlogswebhook <url>`

---

## Requirements

- **Paper** 1.21.11 or newer
- **Java** 21 or newer
- No required external dependencies - everything is self-contained

## Optional Dependencies

| Plugin | Purpose |
|--------|---------|
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Use an external economy provider (e.g. EssentialsX Economy, CMI) instead of JustPlugin's built-in balance system. Set `economy.provider: "vault"` in config. |
| [LuckPerms](https://luckperms.net/) | Enable the `/rank` management GUI to create, edit, and manage groups and player permissions through an in-game interface. Also enables automatic chat prefix/suffix display. |

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| Paper | Yes |
| Purpur | Yes (fully compatible Paper fork) |
| Spigot | No - uses Paper-exclusive APIs |
| Bukkit | No - uses Paper-exclusive APIs |
| Folia | Not yet supported |
| Sponge | No |
| BungeeCord / Velocity / Waterfall | Not applicable - this is a server-side plugin, not a proxy plugin |
| Geyser Extension | No |

