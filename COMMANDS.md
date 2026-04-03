# 📋 JustPlugin - Command Reference

> **Version:** 1.5  
> **Author:** JustMe  
> **Last Updated:** April 3, 2026

---

## Table of Contents

- [Teleportation](#-teleportation)
- [Offline Player Commands](#-offline-player-commands)
- [Warps](#-warps)
- [Homes](#-homes)
- [Economy](#-economy)
- [Moderation](#-moderation)
- [Jail](#-jail)
- [Player](#-player)
- [Chat](#-chat)
- [Kits](#-kits)
- [Personalization](#-personalization)
- [Virtual Inventories](#-virtual-inventories)
- [Info](#-info)
- [Items](#-items)
- [World](#-world)
- [Teams](#-teams)
- [Misc](#-misc)
- [Overrides](#-overrides)

---

## 🌀 Teleportation

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/tpa` | `/tpa <player>` | Request to teleport to a player | `justplugin.tpa` | - |
| `/tpaccept` | `/tpaccept` | Accept an incoming teleport request | - | `tpyes` |
| `/tpacancel` | `/tpacancel` | Cancel your outgoing teleport request or pending teleport | - | - |
| `/tpreject` | `/tpreject` | Reject an incoming teleport request | - | `tpdeny`, `tpno` |
| `/tpahere` | `/tpahere <player>` | Request a player to teleport to you | `justplugin.tpahere` | - |
| `/tppos` | `/tppos <x> <y> <z> [world]` | Teleport to exact coordinates | `justplugin.tppos` | `tpposition` |
| `/tpr` | `/tpr` | Opens the Random Teleport GUI with dimension selection (Overworld, Nether, End) | `justplugin.wild` | `wild`, `rtp` |
| `/back` | `/back` | Return to your last location before teleporting | `justplugin.back` | `return` |
| `/spawn` | `/spawn` | Teleport to the world spawn | `justplugin.spawn` | - |
| `/setspawn` | `/setspawn` | Set the world spawn to your current location (requires safe block below) | `justplugin.setspawn` | - |

### Details

- **TPA requests** expire after a configurable timeout (default: 60s). Players are notified on send, accept, reject, and cancel.
- **TPA Here** sends a request for the *target* to teleport to *you*.
- **Cooldown** (pre-teleport countdown) is configurable per command (e.g. `/tpa` = 3s, `/warp` = 5s). Movement or damage cancels it. OPs also wait for cooldowns unless they have the explicit `*.cooldownbypass` permission.
- **Delay** (time between uses) is configurable per command (e.g. `/tpa` = 3 minutes, `/wild` = 30 minutes). OPs auto-skip delays. Non-OPs need `*.delaybypass` permissions.
- **Safe teleport protection** applies to all teleportation methods (TPA, TPAHere, Warp, Spawn, Home, Back, Wild). When the destination is unsafe, teleportation is cancelled. Players with `*.unsafetp` permission get a clickable **[TP Anyway]** confirmation button along with **[Creative Mode]** and **[God Mode]** options.
- **Random teleport** (`/tpr`) opens a GUI with three dimension options: Overworld (grass block), Nether (netherrack), and The End (end stone). Nether requires `justplugin.wild.nether`, End requires `justplugin.wild.end`. Finds a safe location within the configured wild range (default: 5000 blocks, min 500 from 0,0). Loads chunks asynchronously. Multiple retry attempts for safe locations.
- **Back** stores your location before each teleport and death. Uses safety checks, cooldown countdown, and delay like all other teleport features.
- **SetSpawn** validates that the block below is solid and safe (no lava, magma, cactus, fire, etc.).

---

## 📡 Offline Player Commands

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/tpoff` | `/tpoff <player>` | Teleport to an offline player's last known location | `justplugin.tpoff` | - |
| `/getposoff` | `/getposoff <player>` | View an offline player's last known position | `justplugin.getposoff` | - |
| `/getdeathposoff` | `/getdeathposoff <player>` | View an offline player's last death location | `justplugin.getdeathposoff` | - |
| `/invseeoff` | `/invseeoff <player>` | View an offline player's inventory | `justplugin.invseeoff` | - |
| `/echestseeoff` | `/echestseeoff <player>` | View an offline player's ender chest | `justplugin.echestseeoff` | - |

### Details

- All offline commands work by reading the player's saved data files.
- **TpOff** teleports you to the exact location where the offline player last logged out.
- **GetPosOff** displays the offline player's last known coordinates, world, yaw, and pitch.
- **GetDeathPosOff** displays the offline player's last death location.
- **InvseeOff** opens a read-only view of the offline player's inventory (armor, offhand, all slots).
- **EchestSeeOff** opens a read-only view of the offline player's ender chest.
- The target player must have joined the server at least once.

---

## 🚩 Warps

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/warp` | `/warp [name]` | Teleport to a warp, or list all warps if no name given | `justplugin.warp` | - |
| `/warps` | `/warps` | List all available warps | `justplugin.warp` | `warplist` |
| `/setwarp` | `/setwarp <name>` | Create a warp at your current location | `justplugin.setwarp` | `createwarp` |
| `/delwarp` | `/delwarp <name>` | Delete a warp | `justplugin.delwarp` | `removewarp`, `rmwarp` |
| `/renamewarp` | `/renamewarp <old> <new>` | Rename an existing warp | `justplugin.renamewarp` | - |

### Details

- Warps are global and persistent across restarts.
- Tab completion suggests existing warp names.

---

## 🏠 Homes

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/home` | `/home [name]` | Opens the Homes GUI (no args) or teleports to a specific home | `justplugin.home` | `h` |
| `/sethome` | `/sethome [name]` | Set a home at your location (defaults to "home" if no name) | `justplugin.sethome` | `sh`, `createhome` |
| `/delhome` | `/delhome <name>` | Delete a home | `justplugin.delhome` | `removehome`, `rmhome` |

### Details

- **Home GUI** - running `/home` without arguments opens a 4-row inventory:
  - **Row 1**: Spawn banner + up to 5 home bed slots
  - **Row 2**: Action dyes - yellow (set home), red (delete with confirmation), black (unavailable/locked)
  - Green beds = defined homes (click to teleport), gray beds = empty available slots, red beds = over max limit
  - All actions respect permissions, cooldowns, and safe teleport protection
- Running `/home <name>` still directly teleports to that home (bypasses GUI)
- Maximum homes per player is configurable (default: 5).
- Homes are persistent and stored per-player.
- Tab completion lists your existing home names.

---

## 💰 Economy

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/balance` | `/balance [player]` | Check your balance, or another player's if permitted | `justplugin.balance` | `bal`, `money` |
| `/pay` | `/pay <player> <amount>` | Send money to another player | `justplugin.pay` | - |
| `/paytoggle` | `/paytoggle` | Toggle receiving payments on/off | `justplugin.paytoggle` | - |
| `/paynote` | `/paynote [amount \| list]` | Convert held items into coins based on configured values | `justplugin.paynote` | - |
| `/addcash` | `/addcash [player] <amount>` | Add cash to yourself or another player | `justplugin.addcash` | `givemoney`, `addmoney`, `addbal` |
| `/baltop` | `/baltop` | Opens the Balance Leaderboard GUI with top 10 player heads | `justplugin.balance` | `balancetop`, `moneytop`, `topbal` |
| `/baltophide` | `/baltophide [player]` | Hide yourself or another player from the balance leaderboard | `justplugin.baltophide` | `hidebaltop`, `balancetophide` |

### Details

- **Starting balance** is configurable (default: $100).
- **Currency symbol** is configurable (default: `$`).
- **Balance others:** Requires `justplugin.balance.others` to check another player's balance. Works with offline players.
- **Pay** checks for sufficient funds (shows your balance on error), works with offline players who have ever joined.
- **AddCash self** requires `justplugin.addcash`. **AddCash others** requires `justplugin.addcash.others`.
- **PayNote** converts items in your main hand. Use `/paynote list` to see all convertible items and their values. Configurable item-to-coin mappings in `config.yml`.
- **Baltop** shows the top 10 richest players with medal rankings (🥇🥈🥉). OPs can see hidden players marked `(hidden)`. Your own rank is shown at the bottom.
- **BaltopHide** toggles your visibility on the leaderboard. `justplugin.baltophide.others` to hide other players. Players with `justplugin.baltophide.notify` receive notifications when players are hidden/unhidden. All baltop hide actions are logged.

---

## 🔨 Moderation

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/ban` | `/ban <player \| uuid> [reason]` | Permanently ban a player | `justplugin.ban` | - |
| `/banip` | `/banip <ip \| player> [reason]` | Ban an IP address (resolves player to IP if name given) | `justplugin.banip` | `ban-ip` |
| `/tempban` | `/tempban <player> <duration> [reason]` | Temporarily ban a player | `justplugin.tempban` | `tban` |
| `/tempbanip` | `/tempbanip <ip \| player> <duration> [reason]` | Temporarily ban an IP | `justplugin.tempbanip` | `tbanip` |
| `/unban` | `/unban <player \| uuid>` | Unban a player | `justplugin.unban` | `pardon` |
| `/unbanip` | `/unbanip <ip>` | Unban an IP address | `justplugin.unbanip` | `pardon-ip`, `unban-ip` |
| `/vanish` | `/vanish [player]` | Toggle vanish mode (invisible to other players) | `justplugin.vanish` | `v` |
| `/supervanish` | `/supervanish [player]` | Toggle super vanish (spectator-based full ghost mode) | `justplugin.supervanish` | `sv` |
| `/sudo` | `/sudo <player> <command \| message>` | Force a player to execute a command or send a chat message | `justplugin.sudo` | - |
| `/invsee` | `/invsee <player>` | View a player's full inventory in a GUI (armor, offhand, all slots) | `justplugin.invsee` | `openinv` |
| `/echestsee` | `/echestsee <player>` | View a player's ender chest | `justplugin.echestsee` | `openec` |
| `/mute` | `/mute <player> [reason]` | Permanently mute a player (blocks chat & /msg) | `justplugin.mute` | - |
| `/tempmute` | `/tempmute <player> <duration> [reason]` | Temporarily mute a player | `justplugin.tempmute` | `tmute` |
| `/unmute` | `/unmute <player>` | Unmute a player | `justplugin.unmute` | - |
| `/warn` | `/warn <add\|remove\|list\|confirm\|cancel> <player> [reason\|index]` | Manage player warnings with auto-punishment | `justplugin.warn` | `warning`, `warnings` |
| `/kick` | `/kick <player> [reason]` | Kick a player from the server | `justplugin.kick` | - |
| `/setlogswebhook` | `/setlogswebhook <url\|disable\|confirm\|cancel\|tryagain>` | Configure Discord webhook for log output | `justplugin.setlogswebhook` | `logwebhook`, `webhooklog` |
| `/deathitems` | `/deathitems [player]` | View or restore items from a player's last death | `justplugin.deathitems` | `di`, `deathinv` |
| `/oplist` | `/oplist` | List all server operators | `justplugin.oplist` | `ops`, `operators` |
| `/banlist` | `/banlist [page]` | View the ban list with pagination | `justplugin.banlist` | `bans` |
| `/baniplist` | `/baniplist [page]` | View the IP ban list with pagination | `justplugin.banlist` | `ipbans`, `ipbanlist` |

### Details

- **Ban** supports both player names and UUIDs. Broadcasts the ban to all online players. Shows "already banned" if player is already banned.
- **BanIP** resolves player names to IPs (online or offline using last recorded IP). Shows "already IP banned" if IP is already banned. Displays styled disconnect screen saying "IP Banned".
- **Duration format** for temp bans: `1d2h30m` (days, hours, minutes, seconds). Tab suggests `1h`, `1d`, `7d`, `30d`.
- **Unban/UnbanIP** will tell you if the player/IP is not currently banned.
- **Custom ban screen** - banned players see a styled disconnect screen with reason, duration (if temp), banned-by, and appeal info.
- **Vanish** hides you from all players. Use `justplugin.vanish.others` to vanish other players. Players with `justplugin.vanish.see` can see vanished players in `/plist`. Grants invisibility potion effect, removes from tab list, and fakes a quit message.
- **Super Vanish** puts you in spectator mode - a complete ghost: can't pick up/drop items, can't break/place blocks, can't trigger redstone/pressure plates/sculk sensors, can't open chests, and is invisible to other players. Use `justplugin.supervanish.others` to super-vanish other players. Previous game mode is restored on unvanish.
- **Sudo** - if the message starts with `/`, it's executed as a command; otherwise it's sent as chat.
- **Invsee** opens a 6-row GUI showing main inventory (slots 0-35), armor, and offhand. Refreshes every second. Armor slots show orange glass panes when empty - click with the correct armor type to equip it on the target.
- **EchestSee** opens the target's real ender chest (live sync). Auto-closes if target logs off.
- **Mute** permanently blocks a player from using chat and `/msg`/`/r`. Muted players see the mute reason. Default reason is configurable in `config.yml`.
- **TempMute** temporarily blocks chat and `/msg`/`/r`. Duration format: `5m`, `1h`, `1d`, etc. Auto-expires. Default reason is configurable.
- **Unmute** lifts mutes (both permanent and temporary). Works by name or UUID.
- **Warn** manages a warning system with configurable auto-punishments per warning level:
  - `/warn add <player> [reason]` - Issues a warning and auto-executes the configured punishment.
  - `/warn remove <player> <index> [reason]` - Lifts a warning (keeps in history, doesn't count toward future punishments). Requires confirmation.
  - `/warn list <player>` - Shows all warnings (active & lifted) with details.
  - Default punishment escalation: 1st=ChatMessage, 2nd=Kick, 3rd=TempBan 5m, 4th=TempBan 1d, 5th=TempBan 30d, 6th=TempBan 1y, 7th=Permanent Ban. All configurable in `config.yml`.
  - Supported punishment types: `ChatMessage`, `Kick`, `TempBan <duration>`, `Ban`, `ChatMute`, `ChatTempMute <duration>`, `NoPunishment`.
- **Kick** disconnects a player with a styled kick screen. Default reason is configurable.
- **SetLogsWebhook** configures a Discord webhook URL for receiving all plugin logs as Discord embeds. Tests the URL before confirming. Rate-limited retries (10s interval). Color-coded embeds by category (red=moderation, green=economy, etc.).
- **DeathItems** opens a GUI showing the items the player had when they last died (only if items were dropped, not if keepInventory kept them). `justplugin.deathitems` for self, `justplugin.deathitems.others` for other players. Includes a clickable **[Restore Items]** button to give items back to the player.
- **OpList** lists all server operators with online/offline status indicators.
- **BanList** shows all name/UUID bans with reason, banned-by, date, and duration. Paginated (8 per page) with clickable navigation.
- **BanIPList** shows all IP bans with associated player names, UUIDs, reason, banned-by, date, and duration. Paginated with clickable navigation.
- **Punishment Announcements:** By default, punishments (bans, kicks, mutes, warns) are NOT broadcast to all players. Only staff with `justplugin.announce.<type>` permissions see the announcement. This is configurable per punishment type in `config.yml` under `punishment-announcements`.

---

## 🔒 Jail

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/jail` | `/jail <player> [duration] [reason]` | Jail a player (permanent or temporary) | `justplugin.jail` | - |
| `/unjail` | `/unjail <player>` | Release a player from jail | `justplugin.unjail` | - |
| `/setjail` | `/setjail <name>` | Set a jail location at your current position | `justplugin.setjail` | - |
| `/deljail` | `/deljail <name>` | Delete a jail location | `justplugin.deljail` | - |
| `/jails` | `/jails` | List all jail locations | `justplugin.jails` | `jaillist` |
| `/jailinfo` | `/jailinfo <player>` | View jail details for a player (reason, duration, jailed by) | `justplugin.jailinfo` | - |

### Details

- **Jail** teleports the target to a random (or specified) jail location and restricts their movement, commands, and interactions.
- **Duration format** uses the same format as temp bans: `1d2h30m` (days, hours, minutes, seconds). No duration means permanent.
- **Unjail** releases the player and teleports them back to their pre-jail location.
- **SetJail** creates a named jail location at your current position. You must set at least one jail before using `/jail`.
- **DelJail** permanently removes a jail location.
- **Jails** lists all configured jail locations with their coordinates and world.
- **JailInfo** shows whether a player is jailed, the jail name, reason, who jailed them, and remaining duration (if temporary).
- Jailed players are restricted from: moving outside the jail area, using most commands, breaking/placing blocks, and interacting with entities.
- Jail data persists across server restarts.
- Works for both online and offline players (offline players are jailed on next login).

---

## 🎮 Player

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/fly` | `/fly [player]` | Toggle flight for yourself or another player | `justplugin.fly` | - |
| `/gm` | `/gm <mode> [player]` | Change gamemode (accepts `0/1/2/3`, `s/c/a/sp`, or full names) | `justplugin.gamemode` | `gamemode` |
| `/gmc` | `/gmc [player]` | Switch to Creative mode | `justplugin.gamemode` | - |
| `/gms` | `/gms [player]` | Switch to Survival mode | `justplugin.gamemode` | - |
| `/gma` | `/gma [player]` | Switch to Adventure mode | `justplugin.gamemode` | - |
| `/gmsp` | `/gmsp [player]` | Switch to Spectator mode | `justplugin.gamemode` | - |
| `/gmcheck` | `/gmcheck [player]` | Check a player's current game mode, fly status, and allow-flight | `justplugin.gmcheck` | `checkgm`, `gamemodecheck` |
| `/god` | `/god [player]` | Toggle god mode (invincible, full heal, clears bad effects) | `justplugin.god` | `godmode`, `tgm` |
| `/speed` | `/speed <0-10> [player]` | Set speed dynamically (fly speed if flying, walk speed if walking) | `justplugin.speed` | - |
| `/flyspeed` | `/flyspeed <0-10> [player]` | Set fly speed explicitly | `justplugin.speed` | `fspeed` |
| `/walkspeed` | `/walkspeed <0-10> [player]` | Set walk speed explicitly | `justplugin.speed` | `wspeed` |
| `/hat` | `/hat` | Wear the item in your main hand as a helmet (swaps with current helmet) | `justplugin.hat` | - |
| `/exp` | `/exp <set \| give> <levels \| orbs> <amount> [player]` | Manage experience points (set/give levels or orbs) | `justplugin.exp` | `xp` |
| `/skull` | `/skull [player]` | Get a player's head item (defaults to your own) | `justplugin.skull` | `head`, `playerhead` |
| `/suicide` | `/suicide` | Kill yourself | `justplugin.suicide` | - |
| `/kill` | `/kill [player]` | Kill yourself or another player (overrides vanilla) | `justplugin.kill` | - |
| `/heal` | `/heal [player]` | Restore full health and extinguish fire | `justplugin.heal` | - |
| `/feed` | `/feed [player]` | Restore full hunger and saturation | `justplugin.feed` | - |
| `/getpos` | `/getpos [player]` | Display current coordinates, world, yaw, and pitch | - (public for self) | `whereami`, `position`, `getcoords`, `coords` |
| `/getdeathpos` | `/getdeathpos [player]` | Display last death location | Configurable for self | `getdeathcoords`, `deathpos`, `deathcoords` |
| `/afk` | `/afk` | Toggle AFK (Away From Keyboard) status | `justplugin.afk` | `away` |

### Details

- **AFK:** Toggles your AFK status. When AFK, a tag is shown in tab list and chat. Auto-AFK triggers after a configurable idle time (default: 5 minutes). Players who remain AFK can optionally be kicked after a configurable duration. Players with `justplugin.afk.kickbypass` are never kicked for being AFK. Movement, chat, and interaction automatically clear AFK status.
- **Fly:** `justplugin.fly` for self, `justplugin.fly.others` for other players.
- **Gamemode:** `justplugin.gamemode` for self, `justplugin.gamemode.others` for other players. Works from console when specifying a player.
- **God mode:** Fully heals, restores food/saturation, extinguishes fire, and removes all negative potion effects. `justplugin.god` for self, `justplugin.god.others` for others.
- **Speed:** `justplugin.speed` for self, `justplugin.speed.others` for other players. Range is 0-10, normalized to Minecraft's 0-1 scale.
- **Exp:** `justplugin.exp` for self, `justplugin.exp.others` for other players.
- **Kill:** Overrides vanilla `/kill`. `justplugin.kill` for self, `justplugin.kill.others` for others.
- **Heal:** `justplugin.heal` for self, `justplugin.heal.others` for others. Restores full HP and extinguishes fire.
- **Feed:** `justplugin.feed` for self, `justplugin.feed.others` for others. Restores food to 20 and saturation to 20.
- **GetPos:** No permission needed for self (public). Requires `justplugin.getpos.others` to view another player's position. Shows clickable `[Click to Teleport]` text when viewing others (requires `justplugin.tppos`).
- **GetDeathPos:** Self-use permission is configurable in `config.yml` (`commands.getdeathpos.require-permission-self`). Requires `justplugin.getdeathpos.others` for other players. Shows clickable `[Click to Teleport]` text (requires `justplugin.tppos`).

---

## 💬 Chat

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/msg` | `/msg <player> <message>` | Send a private message to a player | `justplugin.msg` | `tell`, `whisper`, `w`, `m`, `pm`, `dm` |
| `/r` | `/r <message>` | Reply to the last player who messaged you | - | `reply` |
| `/ignore` | `/ignore <add\|remove\|list\|clearlist> [player]` | Manage your ignore list | `justplugin.ignore` | - |
| `/announce` | `/announce <message>` | Broadcast a server-wide announcement | `justplugin.announce` | `broadcast`, `bcast` |
| `/sharecoords` | `/sharecoords [all \| team]` | Share your current coordinates in chat (global or team) | `justplugin.sharecoords` | `sendcoords` |
| `/sharedeathcoords` | `/sharedeathcoords [all \| team]` | Share your last death coordinates in chat (global or team) | `justplugin.sharedeathcoords` | `senddeathcoords` |
| `/chat` | `/chat <all \| team>` | Switch your chat mode between global and team chat | `justplugin.chat` | - |
| `/teammsg` | `/teammsg <message>` | Send a one-off message to your team (doesn't change chat mode) | `justplugin.chat` | `tmsg`, `tm` |
| `/clearchat` | `/clearchat [reason]` | Clear the chat for all online players | `justplugin.clearchat` | `cc`, `chatclear` |
| `/mail` | `/mail <send\|read\|clear\|clearall> [args]` | Send, read, or manage offline mail | `justplugin.mail` | - |

### Details

- **Mail** is an offline messaging system. Subcommands:
  - `/mail send <player> <message>` - send mail to any player (online or offline). Requires `justplugin.mail.send`.
  - `/mail read [page]` - read your inbox with pagination (10 per page).
  - `/mail clear` - clear all your received mail.
  - `/mail clearall` - clear all mail for all players (admin).
  - Players are notified of unread mail on login.
- **Private messages** respect the ignore system. Ignored players cannot send you `/msg` or `/r`.
- **Reply** remembers the last player you messaged. Respects ignore.
- **Announce** formats as `[Announcement]` in red bold, with yellow message text.
- **Share coords/death coords** can be sent to global chat or team-only. Death coords use the same format as regular coords.
- **Chat modes:** `ALL` (global) or `TEAM` (team members only). Must be in a team to use team chat.
- **TeamMsg** sends a message to your team directly without changing your current chat mode.
- **Ignore** blocks: `/msg`, `/r`, `/tpa`, `/tpahere`, `/trade` requests, and global chat messages from the ignored player. The ignored player is notified when they are added/removed. Subcommands: `add <player>` (add to ignore list), `remove <player>` (remove from ignore list, works for offline players too), `list` (view all ignored players with online/offline status), `clearlist` (wipe the entire ignore list).
- **ClearChat** sends 100 blank lines to every online player, then optionally shows a configurable post-clear message. Logs to staff and webhook with the executor and reason (if provided).

---

## 🎒 Kits

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/kit` | `/kit [name]` | Open kit selection GUI (no args) or claim a specific kit | `justplugin.kit` | - |
| `/kitpreview` | `/kitpreview <name>` | Preview a kit's contents in a GUI | `justplugin.kit.preview` | - |
| `/kitcreate` | `/kitcreate` | Create a new kit via GUI (from your current inventory) | `justplugin.kit.create` | - |
| `/kitedit` | `/kitedit <name>` | Edit an existing kit's items | `justplugin.kit.edit` | - |
| `/kitrename` | `/kitrename <old> <new>` | Rename a kit | `justplugin.kit.rename` | - |
| `/kitdelete` | `/kitdelete <name> [permanent]` | Archive a kit (or permanently delete with `permanent` flag) | `justplugin.kit.delete` | - |
| `/kitpublish` | `/kitpublish <name>` | Publish a pending kit for players | `justplugin.kit.publish` | - |
| `/kitdisable` | `/kitdisable <name>` | Temporarily disable a published kit | `justplugin.kit.disable` | - |
| `/kitenable` | `/kitenable <name>` | Re-enable a disabled kit | `justplugin.kit.disable` | - |
| `/kitarchive` | `/kitarchive [restore\|delete\|deleteall] [name]` | Manage archived kits | `justplugin.kit.archive` | - |
| `/kitlist` | `/kitlist` | List all kits with their current status | `justplugin.kit.list` | - |

### Details

- **Kit** opens a GUI showing all published kits the player has permission for. Each kit requires `justplugin.kits.<name>` to claim.
- **Kit Lifecycle:** Kits go through stages: Pending (just created) -> Published (available to players) -> Archived (soft-deleted). Kits can also be Disabled (temporarily unavailable).
- **Cooldowns** are per-kit and configurable. Players with `justplugin.kit.cooldownbypass` bypass all kit cooldowns.
- **Auto-equip armor** - helmets, chestplates, leggings, and boots are automatically equipped to the correct armor slot when claiming.
- **Permanent delete** requires `justplugin.kit.delete.permanent` permission.
- **Archive** has subcommands: `restore <name>` (requires `justplugin.kit.archive.restore`), `delete <name>` (requires `justplugin.kit.archive.delete`), `deleteall`.
- **Archive retention** - archived kits are auto-deleted after a configurable number of days (default: 30).
- Full documentation in `KITS.md`.

---

## 🎨 Personalization

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/nick` | `/nick <name \| off \| reset>` | Set a custom display name or remove it | `justplugin.nick` | `nickname` |
| `/tag` | `/tag [name \| off]` | Open tag GUI (no args), equip a tag, or remove it | `justplugin.tag` | - |
| `/tagcreate` | `/tagcreate <id> <prefix\|suffix> <display...>` | Create a new tag | `justplugin.tag.create` | - |
| `/tagdelete` | `/tagdelete <id>` | Delete a tag | `justplugin.tag.delete` | - |
| `/taglist` | `/taglist` | List all available tags | `justplugin.tag.list` | - |

### Details

- **Nick** sets your display name shown in chat, tab list, and other player-facing locations. Supports MiniMessage formatting.
  - `/nick off` or `/nick reset` removes your nickname and reverts to your username.
  - Color formatting requires `justplugin.nick.color`. Advanced formatting (bold, italic, etc.) requires `justplugin.nick.format`. Rainbow gradient requires `justplugin.nick.rainbow`.
  - Tags the player doesn't have permission for are automatically stripped from the input.
  - Nickname length is validated (configurable min/max in `config.yml`).
- **Tag** opens a GUI showing all available tags. Click a tag to equip it. Use `/tag off` to remove.
  - Tags are displayed as prefixes or suffixes in chat (configurable per tag).
  - Each tag requires no additional per-tag permission - any player with `justplugin.tag` can equip available tags.
- **TagCreate** creates a new tag with an ID, type (prefix or suffix), and display text (supports MiniMessage formatting).
- **TagDelete** permanently removes a tag. Players who had it equipped lose it.
- **TagList** shows all tags with their IDs, types, and display text.

---

## 📦 Virtual Inventories

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/anvil` | `/anvil` | Open a virtual anvil | `justplugin.anvil` | - |
| `/grindstone` | `/grindstone` | Open a virtual grindstone | `justplugin.grindstone` | - |
| `/enderchest` | `/enderchest` | Open your ender chest | `justplugin.enderchest` | `echest`, `ec` |
| `/craft` | `/craft` | Open a virtual crafting table | `justplugin.craft` | `workbench`, `wb` |
| `/stonecutter` | `/stonecutter` | Open a virtual stonecutter | `justplugin.stonecutter` | - |
| `/loom` | `/loom` | Open a virtual loom | `justplugin.loom` | - |
| `/smithingtable` | `/smithingtable` | Open a virtual smithing table | `justplugin.smithingtable` | `smithtable` |
| `/enchantingtable` | `/enchantingtable` | Open a virtual enchanting table | `justplugin.enchantingtable` | `enchtable` |

### Details

- All virtual inventory commands are player-only.
- These open functional GUIs - you can actually use them to craft, repair, enchant, etc.

---

## ℹ️ Info

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/jpinfo` | `/jpinfo` | Show plugin information (version, author) | - | `about` |
| `/jphelp` | `/jphelp [page]` | Show paginated help (4 pages of commands) | - | - |
| `/playerinfo` | `/playerinfo <player>` | View detailed player info (health, food, gamemode, location, balance, etc.) | `justplugin.playerinfo` | `whois`, `seen` |
| `/plist` | `/plist` | List online players (hides vanished players unless you have `justplugin.vanish.see`) | - | `who`, `online`, `players` |
| `/playerlist` | `/playerlist [page]` | Advanced paginated player list with staff tags, world info, and vanish indicators | `justplugin.playerlist` | `pls` |
| `/playerlisthide` | `/playerlisthide [player]` | Hide yourself or another player from the player list | `justplugin.playerlist.hide` | `plhide`, `hideplayer` |
| `/motd` | `/motd [server \| join] [message]` | View both MOTDs, or set the server list MOTD or join MOTD | - | - |
| `/resetmotd` | `/resetmotd [server \| join]` | Reset one or both MOTDs to defaults | `justplugin.motd.set` | - |
| `/clock` | `/clock` | Show current real-world time and in-game time | - | `realtime`, `rltime` |
| `/date` | `/date` | Show current real-world date, time, game day, and game time | - | `realdate`, `rldate` |

### Details

- **PlayerInfo** shows UUID, display name, health (e.g. `20.0/20.0`), food (e.g. `20.0/20.0`), gamemode, world, location, flying status, OP status, and balance. Players with `justplugin.playerinfo.ip` also see the target's IP address. Works for offline players too (shows UUID, last seen, first played, ban status).
- **PlayerList** is a staff-oriented advanced player list. Shows staff tags `[Staff]`, world names, vanish indicators (`[V]` for regular vanish, `[SV]` for super vanish visible to those with `justplugin.vanish.see`). Players hidden via `/playerlisthide` are invisible to those without `justplugin.playerlist.hide`. Paginated with clickable navigation.
- **PlayerListHide** lets you hide yourself or another player from `/playerlist`. `justplugin.playerlist.hide.others` required to hide other players. Players with `justplugin.playerlist.hide.notify` are notified when someone is hidden/unhidden.
- **MOTD** has two separate values configured in `motd.yml`: **server-motd** (shown in the Minecraft server list / multiplayer screen) and **join-motd** (shown to players in chat when they join). View both with `/motd`. Set either with `/motd server <text>` or `/motd join <text>` (requires `justplugin.motd.set`). Both support MiniMessage formatting and placeholders: `{player}` (join only), `{online}`, `{max}`. Set server-motd to empty (`""`) in `motd.yml` to use the vanilla server.properties MOTD.
- **ResetMotd** resets one or both MOTDs to plugin defaults. `/resetmotd server` resets the server list MOTD, `/resetmotd join` resets the join MOTD, `/resetmotd` resets both. Requires `justplugin.motd.set`.
- **Clock/Date** timezone is configurable in `config.yml` (default: UTC).

---

## 🎯 Items

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/itemname` | `/itemname <name>` | Rename the item in your main hand (supports MiniMessage colors) | `justplugin.itemname` | `rename`, `iname` |
| `/shareitem` | `/shareitem` | Show your held item in chat with hover preview for all players | `justplugin.shareitem` | `show`, `showitem` |
| `/setspawner` | `/setspawner <type>` | Change the entity type of the spawner you're looking at | `justplugin.setspawner` | `changespawner` |

### Details

- **ItemName** applies MiniMessage formatting to the item's display name.
- **ShareItem** broadcasts a hoverable item display showing name, amount, and full item tooltip.
- **SetSpawner** requires you to be looking at a spawner block (within 5 blocks). Tab completes all living entity types.

---

## 🌍 World

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/weather` | `/weather <sun \| rain \| thunder>` | Set the weather in your current world | `justplugin.weather` | - |
| `/time` | `/time <set \| add \| query> <value>` | Set, add, or query the world time | `justplugin.time` | - |
| `/freezegame` | `/freezegame` | Freeze the game (pause tick processing) | `justplugin.freezegame` | `tf` |
| `/unfreezegame` | `/unfreezegame` | Unfreeze the game (resume tick processing) | `justplugin.unfreezegame` | `unft` |
| `/clearentities` | `/clearentities` | Manually clear ground items and mobs | `justplugin.clearentities` | `ce`, `entityclear`, `clearlag` |
| `/friendlyfire` | `/friendlyfire <enable \| disable>` | Toggle PvP on/off server-wide | `justplugin.friendlyfire` | `ff`, `pvp`, `pvptoggle` |

### Details

- **Weather** values: `sun`/`clear`, `rain`/`storm`, `thunder`/`thunderstorm`.
- **Time set** values: `day` (1000), `noon`/`midday` (6000), `sunset`/`dusk` (12000), `night` (13000), `midnight` (18000), `sunrise`/`dawn` (23000), or any tick number.
- **Time query** shows the current game time with human-readable format and tick count.
- **FreezeGame/UnfreezeGame** are shortcuts for `/tick freeze` and `/tick unfreeze` with custom plugin-style output.
- **ClearEntities** manually triggers an entity clear (same as the automatic system). Shows counts of items and mobs removed. Can also be run automatically at a configurable interval (default: 5 minutes) - see `entity-clear` in `config.yml`.
- **FriendlyFire** sets PvP on or off for all worlds. Shows current status if already set. Logs old and new status to staff/webhook. Configurable public announcement in `config.yml` (disabled by default).

---

## 👥 Teams

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/team create` | `/team create <name>` | Create a new team (you become the leader) | `justplugin.team` | - |
| `/team disband` | `/team disband` | Disband your team (leader only) | `justplugin.team` | - |
| `/team invite` | `/team invite <player>` | Invite a player to your team (leader only) | `justplugin.team` | - |
| `/team join` | `/team join <name>` | Join a team you've been invited to | `justplugin.team` | - |
| `/team leave` | `/team leave` | Leave your current team (leaders must disband instead) | `justplugin.team` | - |
| `/team kick` | `/team kick <player>` | Kick a player from your team (leader only) | `justplugin.team` | - |
| `/team info` | `/team info [name]` | View team info (defaults to your team) | `justplugin.team` | - |
| `/team list` | `/team list` | List all existing teams | `justplugin.team` | - |

### Details

- Each player can only be in one team at a time.
- Only the team **leader** can invite, kick, and disband.
- Invites must be accepted via `/team join <name>`.
- Team members see online/offline status in `/team info`.
- Team chat is available via `/chat team`.

---

## 🔧 Misc

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/trade` | `/trade <player \| accept \| deny>` | Initiate, accept, or deny a trade with another player | `justplugin.trade` | - |
| `/discord` | `/discord [set <link>]` | Show the server's Discord link, or set it with permission | - | `dc` |
| `/applyedits` | `/applyedits <code>` | Apply pending config changes from the web editor | `justplugin.applyedits` | `configapply`, `webapply` |
| `/tab` | `/tab` | Manually refresh the tab list header/footer | `justplugin.tab` | - |
| `/reloadscoreboard` | `/reloadscoreboard` | Reload the scoreboard config and refresh for all players (staff only) | `justplugin.scoreboard.reload` | `reloadsb` |
| `/rank` | `/rank` | Open the Ranks GUI showing server ranks from LuckPerms (requires LuckPerms) | `justplugin.rank` | `ranks` |
| `/stats` | `/stats [player]` | View player statistics in an interactive GUI | `justplugin.stats` | `statistics` |
| `/maintenance` | `/maintenance <mode \| allowed-users \| allowed-groups \| cooldown> [args...]` | Manage server maintenance mode | `justplugin.maintenance` | `maint` |
| `/skin` | `/skin <set \| clear> [name] [player]` | Set, clear, or reset your skin | `justplugin.skin` | `setskin` |
| `/skinban` | `/skinban [name \| list]` | Ban a skin name from being used, or list all banned skins | `justplugin.skinban` | - |
| `/skinunban` | `/skinunban <name>` | Unban a skin name | `justplugin.skinunban` | - |
| `/jpbackup` | `/jpbackup <export\|import\|list\|delete> [file]` | Manage plugin data backups | `justplugin.backup` | `backup` |

### Details

- **Backup** manages full plugin data backups:
  - `/jpbackup export` - creates a timestamped backup of all plugin data. Requires `justplugin.backup.export`.
  - `/jpbackup import <file>` - restores from a backup file with confirmation prompt. Requires `justplugin.backup.import`.
  - `/jpbackup list` - lists all available backup files with sizes and dates. Requires `justplugin.backup.list`.
  - `/jpbackup delete <file>` - deletes a backup file. Requires `justplugin.backup.delete`.
  - All backup operations run asynchronously to avoid server lag.
- **Trade** opens a split GUI where both players place items and must both click "Accept" to finalize. Closing the GUI returns items. Trade requests have a configurable timeout (default: 60s). Requests are cancelled if either player logs off (the other is notified). Blocked by the ignore system.
- **Discord set** requires `justplugin.discord.set`. The link is clickable in chat.
- **Apply Edits** requires a session code generated by the self-hosted web config editor. Enable the web editor in `config.yml` under `web-editor.enabled`, then open `http://localhost:8585` in your browser. Edit settings, click "Save & Generate Code", and run `/applyedits <code>` in-game. Session codes expire after 10 minutes. This is the **highest-level** admin permission in the plugin.
- **Tab** header/footer is configurable in `config.yml` with MiniMessage and `{player}`, `{online}`, `{max}`, `{tps}`, `{ping}` placeholders. Auto-applied on join and refreshed every 5 seconds (configurable).
- **Scoreboard** shows a sidebar with configurable lines and 50+ placeholders. Configuration lives in `scoreboard.yml`. Use `/reloadscoreboard` (staff only, requires `justplugin.scoreboard.reload`) to apply config changes to all online players. The scoreboard is always visible to all players when enabled in config. See `SCOREBOARD.md` for full placeholder reference.
- **Rank** opens a GUI displaying server ranks. Each rank corresponds to a LuckPerms group and is configured in `config.yml` under `ranks.list`. Players see their current rank highlighted with an enchanted glow and a green checkmark. The rank system is **disabled by default** and requires LuckPerms to be installed. If LuckPerms is not found, players are notified. Configure ranks by setting group names, display names, icons, slots, and descriptions in config.
- **Stats** opens an interactive inventory GUI showing player statistics (kills, deaths, K/D ratio, playtime, blocks broken, mobs killed, etc.). `justplugin.stats` for self, `justplugin.stats.others` for viewing other players' stats.
- **Maintenance** manages the server maintenance mode:
  - `/maintenance mode on/off` - activate or deactivate maintenance mode
  - `/maintenance allowed-users list/add/remove` - manage the maintenance whitelist
  - `/maintenance allowed-groups list/add/remove` - manage LuckPerms group bypass
  - `/maintenance cooldown <duration | clear>` - set estimated maintenance end time (shown in MOTD and kick screen)
  - OP bypass enabled by default. Configurable auto-disable when cooldown expires.
  - Custom kick screen, MOTD override, and server icon during maintenance. See `maintenance/config.yml`.
- **Skin** manages player skins:
  - `/skin set <name>` - set your skin to any player's skin (fetched via Mojang API)
  - `/skin set <name> <player>` - set another player's skin (requires `justplugin.skin.others`)
  - `/skin clear` / `/skin reset` - reset your skin to your username's default
  - `/skin clear <player>` - reset another player's skin (requires `justplugin.skin.others`)
  - Auto-applies stored skins on join. Works for cracked players. **Disabled by default** - opt-in via config.
- **SkinBan** bans a skin name so no player can use it. Use `/skinban list` to view all banned skins.
- **Auto Messages** manages automated broadcast messages:
  - `/automessage reload` - reload automessages.yml. Requires `justplugin.automessage.reload`.
  - `/automessage list` - list all configured messages with status. Requires `justplugin.automessage.list`.
  - `/automessage toggle <id>` - enable/disable a specific message. Requires `justplugin.automessage.toggle`.
  - `/automessage send <id>` - force-send a message immediately. Requires `justplugin.automessage.send`.
  - 4 scheduling modes: interval (e.g. every 10m), schedule (specific times), on-the-hour, on-the-half-hour.
  - Per-message permission filtering, world filtering, rotating messages, custom prefix and sound.
  - Configure in `automessages.yml` (disabled by default).
- **SkinUnban** unbans a previously banned skin name.

---

## 🔄 Overrides

| Command | Usage | Description | Permission | Aliases |
|---------|-------|-------------|------------|---------|
| `/help` | `/help [page]` | Show plugin help (overrides vanilla help) | - | `?` |
| `/plugins` | `/plugins` | Show installed plugins with enable/disable status (staff only) | `justplugin.plugins` | `pl` |
| `/kill` | `/kill [player]` | Kill yourself or another player (overrides vanilla kill) | `justplugin.kill` | - |

### Details

- **Help** displays 4 pages of command summaries.
- **Plugins** shows all server plugins colored green (enabled) or red (disabled). Requires `justplugin.plugins` (staff/OP only by default).
- **Kill** replaces vanilla `/kill` with plugin-formatted output. Self-kill has `justplugin.kill`, targeting others requires `justplugin.kill.others`.

---

## 🔧 Configuration

All commands can be individually **enabled/disabled** and have their **permission nodes overridden** in `config.yml` under the `command-settings` section.

```yaml
command-settings:
  fly:
    enabled: true
    permission: "justplugin.fly"
```

Set `enabled: false` to completely disable any command regardless of permissions.

### Special Configuration

```yaml
commands:
  getdeathpos:
    # If true, justplugin.getdeathpos is required for self-use.
    # If false (default), any player can view their own death location.
    require-permission-self: false
```

---

## 🔌 Plugin API / Ecosystem

JustPlugin exposes an API for other plugins to interact with its economy, punishment, and vanish systems.

### Accessing the API

```java
JustPluginAPI api = JustPluginProvider.get();
if (api != null) {
    // Economy
    double balance = api.getEconomyAPI().getBalance(playerUuid);
    api.getEconomyAPI().addBalance(playerUuid, 100.0);
    
    // Punishments
    boolean banned = api.getPunishmentAPI().isBanned(playerUuid);
    boolean muted = api.getPunishmentAPI().isMuted(playerUuid);
    int warns = api.getPunishmentAPI().getActiveWarnCount(playerUuid);
    
    // Vanish
    boolean vanished = api.getVanishAPI().isVanished(playerUuid);
}
```

### Available APIs

| API | Methods |
|-----|---------|
| `EconomyAPI` | `getBalance`, `setBalance`, `addBalance`, `removeBalance`, `pay`, `format`, `hasBalance` |
| `PunishmentAPI` | `isBanned`, `ban`, `tempBan`, `unban`, `isMuted`, `mute`, `tempMute`, `unmute`, `getMuteReason`, `getActiveWarnCount`, `getTotalWarnCount`, `addWarn`, `liftWarn` |
| `VanishAPI` | `isVanished`, `isSuperVanished` |

---

## 📡 Discord Webhook Logging

When configured via `/setlogswebhook <url>`, all plugin logs are sent to Discord as rich embeds:

| Category | Color | Includes |
|----------|-------|----------|
| `moderation` | 🔴 Red | Bans, unbans, kicks, mutes, unmutes |
| `economy` | 🟢 Green | Payments, cash additions, balance changes |
| `teleport` | 🔵 Blue | Teleportation events |
| `vanish` | 🟣 Purple | Vanish/super vanish toggles |
| `gamemode` | 🟠 Orange | Gamemode changes |
| `player` | 🟡 Yellow | Player state changes (fly, god, speed) |
| `admin` | 🩷 Pink | Administrative actions |
| `item` | 🩵 Teal | Item operations |
| `warn` | 🟧 Dark Orange | Warning additions/lifts |
| `mute` | 🟥 Dark Red | Mute/unmute actions |

### Vanilla Command Logging

Vanilla Minecraft commands are also logged to staff chat and the Discord webhook. The default list includes: `/op`, `/deop`, `/give`, `/gamemode`, `/kick`, `/whitelist`, `/stop`, `/restart`, `/tp`, `/teleport`, `/summon`, `/setworldspawn`, `/gamerule`, `/execute`, `/ban`, `/ban-ip`, `/pardon`, `/pardon-ip`, and more. The full list is configurable in `config.yml` under `vanilla-command-log`.

---

## 🧹 Entity Clear System

JustPlugin includes a built-in ClearLag-like system that automatically clears ground items (and optionally mobs) at a configurable interval.

- **Automatic clearing** every 5 minutes (configurable via `entity-clear.interval`)
- **Warning messages** sent to all players before each clear (configurable timing and message)
- **Staff notifications** for excessive entities per chunk (mobs, armor stands, item frames)
- **Manual trigger** via `/clearentities`
- **Configurable** what to clear: items, hostile mobs, friendly mobs
- Named entities, tamed animals, and persistent entities are never cleared
- Enable/disable the entire system in `config.yml`

