# JustPlugin - All-in-One Server Essentials

**The only essentials plugin you'll ever need for Paper 1.21.11+**

JustPlugin is a lightweight, fully configurable server management plugin that replaces dozens of separate plugins with a single JAR. Economy, teleportation, moderation, teams, trading, vanish, and over 80 commands - all built from scratch with performance and simplicity in mind.

Every command can be individually enabled or disabled. Every permission is granular. Every feature just works.

---

## Features

### Economy
- Full balance system with configurable starting amount and currency symbol
- `/balance`, `/pay`, `/paytoggle`, `/baltop`, `/addcash`
- **PayNotes** - enchanted paper items that act as redeemable balance vouchers. Hold a single paper, run the command, and it becomes a right-clickable note worth the amount you set
- Offline player support - pay, check, and modify balances even when players are offline
- Baltop leaderboard with options to hide yourself or others from the rankings

### Teleportation
- `/tpa`, `/tpahere`, `/tpaccept`, `/tpreject`, `/tpacancel`
- `/spawn`, `/setspawn`, `/back`, `/tppos`, `/tpr` (random wild teleport)
- Configurable request timeout (default 60s), teleport warmup delay (default 3s), and per-feature cooldowns
- **Safe teleport protection** - every teleport checks a 3x3 area around the destination for hazardous blocks (lava, fire, magma, cactus, pressure plates, tripwire, sculk sensors, pointed dripstone, and more). If unsafe, teleportation is blocked
- Staff with bypass permissions get clickable **[TP Anyway]**, **[Creative Mode]**, and **[God Mode]** buttons - only shown if they have the matching permissions
- Movement or taking damage during the warmup cancels the teleport
- Cooldowns apply to everyone, including OPs - only explicit bypass permissions skip them

### Warps
- `/warp`, `/setwarp`, `/delwarp`, `/renamewarp`, `/warps`
- Clickable warp names in the warp list
- Safety checks and warmup delays on every warp

### Homes
- `/home`, `/sethome`, `/delhome` with a configurable max homes limit per player
- Safety checks, warmup delays, and cooldowns
- Homes persist across server restarts and relogs

### Moderation
- `/ban`, `/tempban`, `/unban` - bans by both UUID and username simultaneously so players can't evade by changing names
- `/banip`, `/tempbanip`, `/unbanip` - IP bans with automatic name/UUID/IP lookup. Works with offline players using their last recorded IP. Also bans the UUID and username so changing IP alone won't help
- `/mute`, `/tempmute`, `/unmute` - muted players can't use chat or `/msg`
- `/warn add/remove/list` - progressive warning system with fully configurable punishments per level. Default escalation: chat warning, kick, 5-minute temp ban, 1-day temp ban, 30-day temp ban, 1-year temp ban, permanent ban. Every level's action is customizable (ChatMessage, Kick, TempBan, Ban, ChatMute, ChatTempMute, NoPunishment)
- `/kick`, `/sudo`, `/invsee`, `/echestsee`
- Custom ban screens with appeal information (links to your Discord or any URL)
- Warning removal requires confirmation with clickable buttons and keeps full history

### Vanish
- `/vanish` - invisible, hidden from tab list, player count, and name autocomplete. Fake quit message on enable, fake join on disable
- `/supervanish` - spectator-based full ghost mode. Players in super vanish cannot: pick up or drop items, break or place blocks, earn advancements, trigger redstone (pressure plates, tripwire, sculk sensors), open chests visibly, shoot projectiles, throw items, or interact with the world in any detectable way
- Both vanish modes persist across server restarts and relogs
- Separate permissions for vanishing yourself vs. vanishing others

### Teams
- `/team create`, `/team invite`, `/team join`, `/team leave`, `/team kick`, `/team disband`, `/team list`, `/team info`
- `/teammsg` - send a message to your team without changing your chat mode
- `/chat all/team` - switch your persistent chat mode

### Trading
- `/trade` - opens a Hypixel SkyBlock-style trading GUI
- Double chest window with a 4x5 item area per player, glass pane divider, and accept/decline wool buttons
- Balance transfer support - click the grey dye to open a sign, type an amount (supports k/m shorthand), and it displays as a gold nugget/ingot/block based on the value
- 5-second countdown when both players accept. Removing acceptance or closing the window cancels the trade
- Trade requests expire after 60 seconds (configurable). Ignored players can't send trade requests

### Player Commands
- `/fly`, `/god`, `/speed`, `/flyspeed`, `/walkspeed`
- `/gm`, `/gmc`, `/gms`, `/gma`, `/gmsp`, `/gmcheck`
- `/heal`, `/feed`, `/exp` (supports both XP orbs and levels, for self and others)
- `/hat`, `/skull`, `/kill` (supports mass-kill: mobs, hostile, friendly, items, entities, players, everything)
- `/getpos`, `/getdeathpos` - staff get clickable teleport buttons with safety indicators
- `/sharecoords`, `/sharedeathcoords` - broadcast coordinates to global or team chat
- `/coords`, `/deathcoords` - view your own position or death location privately
- Fly, god, speed, and vanish states all persist across server restarts and relogs
- God mode fully heals the player, fills their food bar, and pauses negative effects until disabled
- Separate permissions for self vs. others on fly, god, speed, heal, feed, exp, gamemode, and kill

### Chat
- `/msg`, `/r` with a clickable **[Reply]** button on every received private message (configurable)
- `/ignore add/remove/list/clearlist` - blocks messages, TPA requests, and trade requests from ignored players. The ignored player is notified they've been ignored
- `/announce` - server-wide announcements with `&` color code support (`&1`, `&2`, `&a`, `&l`, etc.)
- `/itemname` - rename your held item with color code support

### Info and Utilities
- `/help` - paginated command list with clickable command suggestions
- `/playerlist` - paginated player list sorted with staff first, vanish and hidden indicators for staff, and page navigation buttons
- `/playerlisthide` - hide yourself or others from the player list. Staff with the right permission can still see hidden entries with a **[Hidden]** hover tag
- `/playerinfo` - shows health, food, IP, gamemode, world, coordinates, and more
- `/clock`, `/date` - real-world and in-game time in your configured timezone
- `/motd`, `/resetmotd` - view or reset the message of the day
- `/discord` - display or set the server's Discord link
- `/list`, `/plugins` - custom formatted to match the plugin's style

### World
- `/weather clear/rain/thunder`
- `/time set/add`
- `/freezegame` / `/unfreezegame` - shortcuts for tick freeze and unfreeze with custom output

### Virtual Inventories
Open crafting stations anywhere without placing blocks:
- `/anvil`, `/grindstone`, `/craft`, `/stonecutter`, `/loom`, `/smithingtable`, `/enchantingtable`, `/enderchest`

### Discord Webhook Logging
- Every staff action, moderation event, economy change, and administrative command can be logged to a Discord channel via webhook
- Rich embed format with timestamps, executor info, target info, and permission used
- Set up in-game with `/setlogswebhook` - includes a test message and confirmation flow
- Per-category permissions control which log types staff members see in-game chat
- All logs are always printed to console regardless of settings

---

## Configuration

Everything is controlled through a single `config.yml`:

- **Enable or disable any command** individually, regardless of permissions
- **Override permission nodes** per command
- **Teleport settings** - warmup delay, request timeout, cooldowns per feature, safe teleport toggles per feature
- **Economy** - starting balance, currency symbol
- **Warning punishments** - define the action for each warning level with full customization
- **Clickable commands** - toggle clickable chat buttons for TPA, teams, trades, warps, homes, help, ignore, and private message replies
- **Tab list** - custom header and footer with `{player}`, `{online}`, and `{max}` placeholders
- **Default reasons** - configurable default reasons for bans, kicks, mutes, and temp bans
- **Timezone** - for `/clock` and `/date` commands
- **Discord link** - shown by `/discord`, editable in-game
- **Webhook** - URL, enable/disable

---

## Permissions

JustPlugin uses a clean, hierarchical permission system:

- `justplugin.*` - grants everything (OP-only by default)
- `justplugin.player` - basic player permissions (granted to everyone by default)
- Every `.others` permission automatically grants the matching self permission
- Cooldown bypass permissions are **not** included in `justplugin.*` - even OPs have cooldowns unless explicitly granted
- Safe teleport bypass permissions show a confirmation prompt instead of auto-teleporting

Full permission documentation is included in the plugin's `PERMISSIONS.md`.

---

## Developer API

JustPlugin exposes a public API for add-on plugins to integrate with:

```java
JustPluginAPI api = JustPluginProvider.get();

// Economy
double balance = api.getEconomyAPI().getBalance(playerUuid);
api.getEconomyAPI().removeBalance(playerUuid, 500.0);
String formatted = api.getEconomyAPI().format(1234.50); // "$1,234.50"

// Punishments
api.getPunishmentAPI().isBanned(playerUuid);
api.getPunishmentAPI().addWarn(uuid, name, reason, "MyPlugin");

// Vanish
api.getVanishAPI().isVanished(playerUuid);
```

Build chest shops, auction houses, voting rewards, or any plugin that needs to read or modify balances, check bans, issue warnings, or detect vanished players. Add `JustPlugin` to your `plugin.yml` `depend` list and you're ready to go.

---

## Installation

1. Drop `JustPlugin.jar` into your server's `plugins/` folder
2. Start the server - all config files are generated automatically
3. Customize `config.yml` to fit your server
4. Set up permissions with [LuckPerms](https://luckperms.net/) or any permissions plugin

---

## Requirements

- **Paper** 1.21.11 or newer
- **Purpur** 1.21.11 or newer (Paper fork, fully compatible)
- **Java** 21 or newer
- Optional: [LuckPerms](https://luckperms.net/) for granular permission management

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| Paper | Yes |
| Purpur | Yes |
| Spigot | No - uses Paper-exclusive APIs |
| Bukkit | No - uses Paper-exclusive APIs |
| Folia | Not yet supported |
| Sponge | No |
| BungeeCord / Velocity / Waterfall | Not applicable - this is a server-side plugin, not a proxy plugin |
| Geyser Extension | No |


