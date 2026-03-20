# 🔐 JustPlugin — Permissions Reference

> **Version:** 1.1  
> **Author:** JustMe  
> **Last Updated:** March 15, 2026

---

## Table of Contents

- [Permission Hierarchy](#permission-hierarchy)
- [Wildcard Permission](#-wildcard-permission-justplugin)
- [Default Player Permission](#-default-player-permission-justpluginplayer)
- [All Permissions (Detailed)](#all-permissions-detailed)
  - [Teleportation](#-teleportation-permissions)
  - [Warp](#-warp-permissions)
  - [Home](#-home-permissions)
  - [Economy](#-economy-permissions)
  - [Moderation](#-moderation-permissions)
  - [Player](#-player-permissions)
  - [Chat](#-chat-permissions)
  - [Virtual Inventories](#-virtual-inventory-permissions)
  - [Info](#-info-permissions)
  - [Items](#-items-permissions)
  - [World](#-world-permissions)
  - [Teams](#-teams-permissions)
  - [Misc](#-misc-permissions)
  - [Special](#-special-permissions)

---

## Permission Hierarchy

```
justplugin.*                          ← OP-only, grants EVERYTHING
├── justplugin.player                 ← Default TRUE for all players (basic permissions)
│   ├── justplugin.tpa
│   ├── justplugin.tpahere
│   ├── justplugin.wild
│   ├── justplugin.spawn
│   ├── justplugin.warp
│   ├── justplugin.home
│   ├── justplugin.sethome
│   ├── justplugin.delhome
│   ├── justplugin.balance
│   ├── justplugin.pay
│   ├── justplugin.paytoggle
│   ├── justplugin.paynote
│   ├── justplugin.msg
│   ├── justplugin.ignore
│   ├── justplugin.sharecoords
│   ├── justplugin.sharedeathcoords
│   ├── justplugin.chat
│   ├── justplugin.getdeathpos           ← Self only (configurable if required)
│   ├── justplugin.shareitem
│   ├── justplugin.team
│   ├── justplugin.trade
│   ├── justplugin.tab
│   └── justplugin.rank                  ← Open ranks GUI (requires LuckPerms)
│
│   [No permission needed — public commands]
│   ├── /getpos (self)                   ← Always public, no permission
│   ├── /jpinfo, /jphelp, /plist, /motd (view), /clock, /date
│   └── /help, /discord
│
├── [Requires explicit permission — not in justplugin.player by default]
│   ├── justplugin.back               ← Return to last location
│   ├── justplugin.kill               ← Self-kill
│   ├── justplugin.suicide            ← Suicide command
│   ├── justplugin.enderchest         ← Virtual ender chest
│   ├── justplugin.anvil              ← Virtual anvil
│   ├── justplugin.craft              ← Virtual crafting table
│   └── justplugin.playerlist         ← View advanced player list
│
├── [Staff/Admin — must be explicitly granted]
│   ├── justplugin.fly                ← Toggle own flight
│   ├── justplugin.fly.others         ← Toggle flight for other players
│   ├── justplugin.gamemode           ← Change own gamemode
│   ├── justplugin.gamemode.others    ← Change gamemode for other players
│   ├── justplugin.god                ← Toggle own god mode
│   ├── justplugin.god.others         ← Toggle god mode for other players
│   ├── justplugin.speed              ← Set own speed
│   ├── justplugin.speed.others       ← Set speed for other players
│   ├── justplugin.heal               ← Heal yourself
│   ├── justplugin.heal.others        ← Heal other players
│   ├── justplugin.feed               ← Feed yourself
│   ├── justplugin.feed.others        ← Feed other players
│   ├── justplugin.kill.others        ← Kill other players
│   ├── justplugin.exp                ← Manage own XP
│   ├── justplugin.exp.others         ← Manage other players' XP
│   ├── justplugin.getpos.others      ← View other players' positions
│   ├── justplugin.getdeathpos.others ← View other players' death positions
│   ├── justplugin.vanish             ← Vanish yourself
│   ├── justplugin.vanish.others      ← Vanish other players
│   ├── justplugin.vanish.see         ← See vanished players in /plist
│   ├── justplugin.supervanish        ← Super vanish yourself (spectator ghost mode)
│   ├── justplugin.supervanish.others ← Super vanish other players
│   ├── justplugin.balance.others     ← Check other players' balance
│   ├── justplugin.addcash            ← Add cash to self
│   ├── justplugin.addcash.others     ← Add cash to other players
│   ├── justplugin.baltophide         ← Hide yourself from baltop
│   ├── justplugin.baltophide.others  ← Hide other players from baltop
│   ├── justplugin.baltophide.notify  ← Receive baltop hide notifications
│   ├── justplugin.baltop.viewhidden ← See hidden players in Baltop GUI
│   ├── justplugin.wild.nether       ← Random teleport in Nether (grants .wild)
│   ├── justplugin.wild.end          ← Random teleport in The End (grants .wild)
│   ├── justplugin.playerlist         ← Advanced player list
│   ├── justplugin.playerlist.hide    ← Hide yourself from player list
│   ├── justplugin.playerlist.hide.others  ← Hide other players from player list
│   ├── justplugin.playerlist.hide.notify  ← Receive player list hide notifications
│   ├── justplugin.staff              ← Marks as staff in /playerlist
│   ├── justplugin.log.moderation     ← See moderation logs
│   ├── justplugin.log.economy        ← See economy logs
│   ├── justplugin.log.vanish         ← See vanish logs
│   ├── justplugin.log.gamemode       ← See gamemode logs
│   ├── justplugin.log.player         ← See player action logs
│   ├── justplugin.log.admin          ← See admin action logs
│   ├── justplugin.log.item           ← See item action logs
│   ├── justplugin.gmcheck            ← Check gamemode info
│   ├── justplugin.scoreboard.reload  ← Reload scoreboard config
│   ├── justplugin.hat                ← Wear item as hat
│   ├── justplugin.skull              ← Get player heads
│   ├── justplugin.setspawn           ← Set world spawn
│   ├── justplugin.setwarp            ← Create warps
│   ├── justplugin.delwarp            ← Delete warps
│   ├── justplugin.renamewarp         ← Rename warps
│   ├── justplugin.tppos              ← Teleport to coordinates
│   ├── justplugin.ban                ← Ban players
│   ├── justplugin.banip              ← Ban IPs
│   ├── justplugin.tempban            ← Temp ban players
│   ├── justplugin.tempbanip          ← Temp ban IPs
│   ├── justplugin.unban              ← Unban players
│   ├── justplugin.unbanip            ← Unban IPs
│   ├── justplugin.sudo               ← Force player commands
│   ├── justplugin.invsee             ← View player inventories
│   ├── justplugin.echestsee          ← View player ender chests
│   ├── justplugin.mute               ← Permanently mute players
│   ├── justplugin.tempmute           ← Temporarily mute players
│   ├── justplugin.unmute             ← Unmute players
│   ├── justplugin.warn               ← Manage player warnings
│   ├── justplugin.warn.notify        ← Receive warning notifications
│   ├── justplugin.kick               ← Kick players
│   ├── justplugin.setlogswebhook     ← Configure Discord webhook logging
│   ├── justplugin.applyedits         ← Apply web editor config changes (HIGHEST level)
│   ├── justplugin.log.warn           ← See warning logs
│   ├── justplugin.log.mute           ← See mute logs
│   ├── justplugin.announce           ← Broadcast announcements
│   ├── justplugin.playerinfo         ← View player info
│   ├── justplugin.playerinfo.ip      ← See player IP in /playerinfo
│   ├── justplugin.motd.set           ← Set server MOTD
│   ├── justplugin.discord.set        ← Set Discord link
│   ├── justplugin.itemname           ← Rename items
│   ├── justplugin.setspawner         ← Change spawner types
│   ├── justplugin.weather            ← Change weather
│   ├── justplugin.time               ← Change time
│   ├── justplugin.teleport.bypass    ← Bypass teleport delay
│   ├── justplugin.anvil              ← Virtual anvil
│   ├── justplugin.grindstone         ← Virtual grindstone
│   ├── justplugin.stonecutter        ← Virtual stonecutter
│   ├── justplugin.loom               ← Virtual loom
│   ├── justplugin.smithingtable      ← Virtual smithing table
│   ├── justplugin.enchantingtable    ← Virtual enchanting table
│   ├── justplugin.freezegame         ← Freeze game tick
│   ├── justplugin.unfreezegame       ← Unfreeze game tick
│   ├── justplugin.clearentities      ← Manual entity clear
│   ├── justplugin.clearchat          ← Clear server chat
│   ├── justplugin.friendlyfire       ← Toggle PvP
│   ├── justplugin.deathitems         ← View/restore own death items (admin only)
│   ├── justplugin.deathitems.others  ← View/restore other players' death items
│   ├── justplugin.plugins            ← View installed plugins list (staff only)
│   ├── justplugin.oplist             ← View server operators
│   ├── justplugin.banlist            ← View ban/IP ban lists
│   ├── justplugin.announce.ban       ← See ban announcements (when not public)
│   ├── justplugin.announce.banip     ← See IP ban announcements
│   ├── justplugin.announce.tempban   ← See temp ban announcements
│   ├── justplugin.announce.tempbanip ← See temp IP ban announcements
│   ├── justplugin.announce.mute      ← See mute announcements
│   ├── justplugin.announce.tempmute  ← See temp mute announcements
│   ├── justplugin.announce.warn      ← See warning announcements
│   └── justplugin.announce.kick      ← See kick announcements
```

---

## ⭐ Wildcard Permission: `justplugin.*`

| Property | Value |
|----------|-------|
| **Default** | `op` |
| **Description** | Grants **every** JustPlugin permission. Intended for server operators only. |

Includes all permissions listed in this document.

---

## 👤 Default Player Permission: `justplugin.player`

| Property | Value |
|----------|-------|
| **Default** | `true` (all players) |
| **Description** | Basic survival player permissions. Granted to all players by default. |

### Included Permissions:

| Permission | Description |
|------------|-------------|
| `justplugin.tpa` | Send teleport requests |
| `justplugin.tpahere` | Send "teleport here" requests |
| `justplugin.wild` | Random teleport |
| `justplugin.back` | Return to previous location |
| `justplugin.spawn` | Teleport to spawn |
| `justplugin.warp` | Use and list warps |
| `justplugin.home` | Teleport to homes |
| `justplugin.sethome` | Set homes |
| `justplugin.delhome` | Delete homes |
| `justplugin.balance` | Check own balance |
| `justplugin.pay` | Pay other players |
| `justplugin.paytoggle` | Toggle receiving payments |
| `justplugin.paynote` | Convert items to coins |
| `justplugin.msg` | Send private messages |
| `justplugin.ignore` | Ignore players |
| `justplugin.sharecoords` | Share coordinates |
| `justplugin.sharedeathcoords` | Share death coordinates |
| `justplugin.chat` | Switch chat modes |
| `justplugin.enderchest` | Open own ender chest |
| `justplugin.craft` | Virtual crafting table |
| `justplugin.getdeathpos` | View own death position (also configurable in config.yml) |
| `justplugin.shareitem` | Share items in chat |
| `justplugin.team` | Team management |
| `justplugin.trade` | Trade with players |
| `justplugin.suicide` | Kill yourself (/suicide) |
| `justplugin.kill` | Kill yourself (/kill) |
| `justplugin.tab` | Refresh tab list |
| `justplugin.playerlist` | View advanced player list |
| `justplugin.deathitems` | View and restore own death items |

### Not in `justplugin.player` — Always Public (no permission needed):

| Command | Why |
|---------|-----|
| `/getpos` (self) | Your own position is not a secret — always public |
| `/jpinfo` | Plugin information |
| `/jphelp` | Help pages |
| `/plist` | Player list |
| `/motd` (view) | Message of the day |
| `/clock` | Real-world time |
| `/date` | Real-world date |
| `/discord` | Discord link |
| `/help` | Help override |
| `/plugins` | Plugin list |

---

## All Permissions (Detailed)

### 🌀 Teleportation Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.tpa` | Send teleport requests to other players | `true` (player) | `/tpa` |
| `justplugin.tpahere` | Request a player to teleport to you | `true` (player) | `/tpahere` |
| `justplugin.tppos` | Teleport to exact coordinates | `op` | `/tppos` |
| `justplugin.wild` | Random teleport to a safe location (Overworld) | `true` (player) | `/tpr`, `/wild`, `/rtp` |
| `justplugin.wild.nether` | Random teleport in the Nether (grants `.wild`) | `op` | `/tpr` (Nether option) |
| `justplugin.wild.end` | Random teleport in The End (grants `.wild`) | `op` | `/tpr` (End option) |
| `justplugin.back` | Return to your last location before a teleport | `true` (player) | `/back` |
| `justplugin.spawn` | Teleport to the world spawn point | `true` (player) | `/spawn` |
| `justplugin.setspawn` | Set the world spawn point | `op` | `/setspawn` |
| `justplugin.teleport.bypass` | Legacy bypass for teleport warmup (use per-command `.cooldownbypass` instead) | `op` | All teleport commands |

> **Note:** `/tpaccept`, `/tpacancel`, and `/tpreject` have no permission requirement — any player can accept/cancel/reject requests.

---

### 🚩 Warp Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.warp` | Teleport to warps and list them | `true` (player) | `/warp`, `/warps` |
| `justplugin.setwarp` | Create new warps | `op` | `/setwarp` |
| `justplugin.delwarp` | Delete existing warps | `op` | `/delwarp` |
| `justplugin.renamewarp` | Rename existing warps | `op` | `/renamewarp` |

---

### 🏠 Home Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.home` | Teleport to your homes | `true` (player) | `/home` |
| `justplugin.sethome` | Set homes (up to configured max) | `true` (player) | `/sethome` |
| `justplugin.delhome` | Delete your homes | `true` (player) | `/delhome` |

---

### 💰 Economy Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.balance` | Check your own balance | `true` (player) | `/balance` |
| `justplugin.balance.others` | Check another player's balance | `op` | `/balance <player>` |
| `justplugin.pay` | Pay money to other players | `true` (player) | `/pay` |
| `justplugin.paytoggle` | Toggle whether you receive payments | `true` (player) | `/paytoggle` |
| `justplugin.paynote` | Convert items in hand to coins | `true` (player) | `/paynote` |
| `justplugin.addcash` | Add cash to yourself | `op` | `/addcash <amount>` |
| `justplugin.addcash.others` | Add cash to other players | `op` | `/addcash <player> <amount>` |
| `justplugin.baltophide` | Hide yourself from the balance leaderboard | `op` | `/baltophide` |
| `justplugin.baltophide.others` | Hide other players from the balance leaderboard | `op` | `/baltophide <player>` |
| `justplugin.baltophide.notify` | Receive notifications when players are hidden/unhidden from baltop | `op` | `/baltophide` |
| `justplugin.baltop.viewhidden` | See hidden players in the Balance Leaderboard GUI | `op` | `/baltop` |

---

### 🔨 Moderation Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.ban` | Permanently ban players | `op` | `/ban` |
| `justplugin.banip` | Ban IP addresses | `op` | `/banip` |
| `justplugin.tempban` | Temporarily ban players | `op` | `/tempban` |
| `justplugin.tempbanip` | Temporarily ban IP addresses | `op` | `/tempbanip` |
| `justplugin.unban` | Unban players | `op` | `/unban` |
| `justplugin.unbanip` | Unban IP addresses | `op` | `/unbanip` |
| `justplugin.vanish` | Toggle vanish for yourself | `op` | `/vanish` |
| `justplugin.vanish.others` | Toggle vanish for other players | `op` | `/vanish <player>` |
| `justplugin.vanish.see` | See vanished players in player list | `op` | `/plist`, `/playerlist` |
| `justplugin.supervanish` | Toggle super vanish (spectator-based ghost mode) | `op` | `/supervanish` |
| `justplugin.supervanish.others` | Toggle super vanish for other players | `op` | `/supervanish <player>` |
| `justplugin.sudo` | Force players to run commands or send messages | `op` | `/sudo` |
| `justplugin.invsee` | View another player's inventory | `op` | `/invsee` |
| `justplugin.echestsee` | View another player's ender chest | `op` | `/echestsee` |
| `justplugin.mute` | Permanently mute a player (blocks chat & /msg) | `op` | `/mute` |
| `justplugin.tempmute` | Temporarily mute a player | `op` | `/tempmute` |
| `justplugin.unmute` | Unmute a player | `op` | `/unmute` |
| `justplugin.warn` | Manage player warnings (add, remove, list) | `op` | `/warn` |
| `justplugin.warn.notify` | Receive notifications about warnings | `op` | `/warn` |
| `justplugin.kick` | Kick a player from the server | `op` | `/kick` |
| `justplugin.setlogswebhook` | Configure Discord webhook logging | `op` | `/setlogswebhook` |
| `justplugin.applyedits` | Apply config changes from the web editor (**highest-level** admin permission) | `op` | `/applyedits` |

---

### 🎮 Player Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.fly` | Toggle flight for yourself | `op` | `/fly` |
| `justplugin.fly.others` | Toggle flight for other players | `op` | `/fly <player>` |
| `justplugin.gamemode` | Change your own gamemode | `op` | `/gm`, `/gmc`, `/gms`, `/gma`, `/gmsp` |
| `justplugin.gamemode.others` | Change gamemode for other players | `op` | `/gm <mode> <player>`, `/gmc <player>`, etc. |
| `justplugin.gmcheck` | Check a player's gamemode information | `op` | `/gmcheck` |
| `justplugin.god` | Toggle god mode for yourself | `op` | `/god` |
| `justplugin.god.others` | Toggle god mode for other players | `op` | `/god <player>` |
| `justplugin.speed` | Set your own walk/fly speed | `op` | `/speed`, `/flyspeed`, `/walkspeed` |
| `justplugin.speed.others` | Set speed for other players | `op` | `/speed <value> <player>` |
| `justplugin.heal` | Heal yourself (restore health, extinguish fire) | `op` | `/heal` |
| `justplugin.heal.others` | Heal other players | `op` | `/heal <player>` |
| `justplugin.feed` | Feed yourself (restore hunger & saturation) | `op` | `/feed` |
| `justplugin.feed.others` | Feed other players | `op` | `/feed <player>` |
| `justplugin.kill` | Kill yourself | `true` (player) | `/kill`, `/suicide` |
| `justplugin.kill.others` | Kill other players | `op` | `/kill <player>` |
| `justplugin.hat` | Wear an item as a hat | `op` | `/hat` |
| `justplugin.exp` | Manage your own experience (set/give, levels/orbs) | `op` | `/exp`, `/xp` |
| `justplugin.exp.others` | Manage other players' experience | `op` | `/exp ... <player>` |
| `justplugin.skull` | Get a player's skull/head item | `op` | `/skull` |
| `justplugin.suicide` | Kill yourself (legacy, same as justplugin.kill) | `true` (player) | `/suicide` |
| `justplugin.getpos.others` | View another player's current position | `op` | `/getpos <player>` |
| `justplugin.getdeathpos` | View your own death location (configurable) | `true` (player) | `/getdeathpos` |
| `justplugin.getdeathpos.others` | View another player's death location | `op` | `/getdeathpos <player>` |

> **Note:** `/getpos` (self) requires **no permission** — it is always public. Self-use permission for `/getdeathpos` is configurable via `commands.getdeathpos.require-permission-self` in `config.yml`.

---

### 💬 Chat Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.msg` | Send private messages and replies | `true` (player) | `/msg`, `/r` |
| `justplugin.ignore` | Toggle ignoring other players | `true` (player) | `/ignore` |
| `justplugin.announce` | Broadcast server-wide announcements | `op` | `/announce` |
| `justplugin.sharecoords` | Share your coordinates in chat | `true` (player) | `/sharecoords` |
| `justplugin.sharedeathcoords` | Share your death coordinates in chat | `true` (player) | `/sharedeathcoords` |
| `justplugin.chat` | Switch between global and team chat modes | `true` (player) | `/chat` |

---

### 📦 Virtual Inventory Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.anvil` | Open a virtual anvil | `op` | `/anvil` |
| `justplugin.grindstone` | Open a virtual grindstone | `op` | `/grindstone` |
| `justplugin.enderchest` | Open your own ender chest | `true` (player) | `/enderchest` |
| `justplugin.craft` | Open a virtual crafting table | `true` (player) | `/craft` |
| `justplugin.stonecutter` | Open a virtual stonecutter | `op` | `/stonecutter` |
| `justplugin.loom` | Open a virtual loom | `op` | `/loom` |
| `justplugin.smithingtable` | Open a virtual smithing table | `op` | `/smithingtable` |
| `justplugin.enchantingtable` | Open a virtual enchanting table | `op` | `/enchantingtable` |

---

### ℹ️ Info Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.playerinfo` | View detailed information about a player | `op` | `/playerinfo`, `/whois` |
| `justplugin.playerinfo.ip` | See a player's IP address in `/playerinfo` | `op` | `/playerinfo` |
| `justplugin.playerlist` | View the advanced player list with staff tags and vanish indicators | `op` | `/playerlist` |
| `justplugin.playerlist.hide` | Hide yourself from the player list | `op` | `/playerlisthide` |
| `justplugin.playerlist.hide.others` | Hide other players from the player list | `op` | `/playerlisthide <player>` |
| `justplugin.playerlist.hide.notify` | Receive notifications when players are hidden/unhidden | `op` | `/playerlisthide` |
| `justplugin.staff` | Marks a player as staff (shown with `[Staff]` tag in `/playerlist`) | `op` | `/playerlist` |
| `justplugin.motd.set` | Set the server MOTD | `op` | `/motd <message>` |

> **Note:** `/jpinfo`, `/jphelp`, `/plist`, `/motd` (view), `/clock`, `/date` have no permission requirement — all players can use them.

---

### 🎯 Items Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.itemname` | Rename items with MiniMessage formatting | `op` | `/itemname` |
| `justplugin.shareitem` | Show your held item in chat with hover preview | `true` (player) | `/shareitem` |
| `justplugin.setspawner` | Change the entity type of a spawner block | `op` | `/setspawner` |

---

### 🌍 World Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.weather` | Change the world weather | `op` | `/weather` |
| `justplugin.time` | Set or query the world time | `op` | `/time` |

---

### 👥 Teams Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.team` | Full team management (create, disband, invite, join, leave, kick, info, list) | `true` (player) | `/team` |

---

### 🔧 Misc Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.trade` | Trade items with other players | `true` (player) | `/trade` |
| `justplugin.discord.set` | Change the Discord link | `op` | `/discord set <link>` |
| `justplugin.applyedits` | Apply config changes from the web editor (**highest-level**) | `op` | `/applyedits` |
| `justplugin.tab` | Manually refresh the tab list | `true` (player) | `/tab` |
| `justplugin.scoreboard.reload` | Reload the scoreboard config and refresh for all players (staff only) | `op` | `/reloadscoreboard` |

---

### 🛡️ Special / ".others" Permissions

These permissions control whether a command can target **other players**. The base permission allows self-use only.

| Permission | Description | Default | Used By |
|------------|-------------|---------|---------|
| `justplugin.fly.others` | Toggle flight for other players | `op` | `/fly <player>` |
| `justplugin.gamemode.others` | Change gamemode for other players | `op` | `/gm <mode> <player>` |
| `justplugin.god.others` | Toggle god mode for other players | `op` | `/god <player>` |
| `justplugin.speed.others` | Set speed for other players | `op` | `/speed <value> <player>` |
| `justplugin.heal.others` | Heal other players | `op` | `/heal <player>` |
| `justplugin.feed.others` | Feed other players | `op` | `/feed <player>` |
| `justplugin.kill.others` | Kill other players | `op` | `/kill <player>` |
| `justplugin.exp.others` | Manage other players' experience | `op` | `/exp ... <player>` |
| `justplugin.vanish.others` | Toggle vanish for other players | `op` | `/vanish <player>` |
| `justplugin.supervanish.others` | Toggle super vanish for other players | `op` | `/supervanish <player>` |
| `justplugin.balance.others` | View another player's balance | `op` | `/balance <player>` |
| `justplugin.addcash.others` | Add cash to other players | `op` | `/addcash <player> <amount>` |
| `justplugin.baltophide.others` | Hide other players from baltop | `op` | `/baltophide <player>` |
| `justplugin.playerlist.hide.others` | Hide other players from the player list | `op` | `/playerlisthide <player>` |
| `justplugin.getpos.others` | View another player's position | `op` | `/getpos <player>` |
| `justplugin.getdeathpos.others` | View another player's death location | `op` | `/getdeathpos <player>` |

### Other Special Permissions

| Permission | Description | Default | Used By |
|------------|-------------|---------|---------|
| `justplugin.teleport.bypass` | Skip the teleport warmup delay entirely | `op` | All teleport commands |
| `justplugin.vanish.see` | See vanished players in the player list (`/plist`) | `op` | `/plist` |
| `justplugin.playerinfo.ip` | View a player's IP address in `/playerinfo` | `op` | `/playerinfo` |
| `justplugin.motd.set` | Set the server MOTD via `/motd <message>` | `op` | `/motd` |
| `justplugin.discord.set` | Set the Discord link via `/discord set <link>` | `op` | `/discord` |

### 🛡️ Safe Teleport Bypass Permissions

These let a player teleport to unsafe locations. Instead of auto-teleporting, a clickable **[TP Anyway]** confirmation button is shown alongside options to enable Creative Mode or God Mode.

| Permission | Description | Default | Used By |
|------------|-------------|---------|---------|
| `justplugin.tpa.unsafetp` | Bypass TPA safe teleport protection (with confirmation) | `op` | `/tpa` |
| `justplugin.tpahere.unsafetp` | Bypass TPAHere safe teleport protection (with confirmation) | `op` | `/tpahere` |
| `justplugin.warp.unsafetp` | Bypass warp safe teleport protection (with confirmation) | `op` | `/warp` |
| `justplugin.spawn.unsafetp` | Bypass spawn safe teleport protection (with confirmation) | `op` | `/spawn` |
| `justplugin.home.unsafetp` | Bypass home safe teleport protection (with confirmation) | `op` | `/home` |
| `justplugin.back.unsafetp` | Bypass back safe teleport protection (with confirmation) | `op` | `/back` |

### 📋 Log Permissions

These control which log categories a player sees in-game. All log actions are always printed to the server console.

| Permission | Description | Default | Category |
|------------|-------------|---------|----------|
| `justplugin.log.moderation` | See moderation logs (bans, unbans) | `op` | `MODERATION` |
| `justplugin.log.economy` | See economy logs (addcash, baltophide) | `op` | `ECONOMY` |
| `justplugin.log.teleport` | See teleport logs | `op` | `TELEPORT` |
| `justplugin.log.vanish` | See vanish logs (vanish, supervanish) | `op` | `VANISH` |
| `justplugin.log.gamemode` | See gamemode change logs | `op` | `GAMEMODE` |
| `justplugin.log.player` | See player action logs (fly, god, heal, feed) | `op` | `PLAYER` |
| `justplugin.log.admin` | See admin action logs (sudo, warps, exp, spawners) | `op` | `ADMIN` |
| `justplugin.log.item` | See item-related logs | `op` | `ITEM` |
| `justplugin.log.warn` | See warning logs (warn add, lift) | `op` | `WARN` |
| `justplugin.log.mute` | See mute/unmute logs | `op` | `MUTE` |

### ⏱️ Cooldown Bypass Permissions

Cooldown = the countdown timer BEFORE the teleport executes (e.g. 3 seconds standing still). **NOT included in `justplugin.*`** — even OPs wait for cooldowns unless this permission is explicitly granted.

| Permission | Description | Default | Used By |
|------------|-------------|---------|---------|
| `justplugin.tpa.cooldownbypass` | Skip TPA pre-teleport countdown | `false` | `/tpa` |
| `justplugin.tpahere.cooldownbypass` | Skip TPAHere pre-teleport countdown | `false` | `/tpahere` |
| `justplugin.warp.cooldownbypass` | Skip warp pre-teleport countdown | `false` | `/warp` |
| `justplugin.spawn.cooldownbypass` | Skip spawn pre-teleport countdown | `false` | `/spawn` |
| `justplugin.home.cooldownbypass` | Skip home pre-teleport countdown | `false` | `/home` |
| `justplugin.back.cooldownbypass` | Skip back pre-teleport countdown | `false` | `/back` |
| `justplugin.wild.cooldownbypass` | Skip wild/RTP pre-teleport countdown | `false` | `/tpr` |

### ⏳ Delay Bypass Permissions

Delay = minimum time between successive uses of the same command (e.g. 3 minutes). OPs auto-skip delays. Non-OPs need these permissions to bypass.

| Permission | Description | Default | Used By |
|------------|-------------|---------|---------|
| `justplugin.tpa.delaybypass` | Bypass TPA delay between uses | `false` | `/tpa` |
| `justplugin.tpahere.delaybypass` | Bypass TPAHere delay between uses | `false` | `/tpahere` |
| `justplugin.warp.delaybypass` | Bypass warp delay between uses | `false` | `/warp` |
| `justplugin.spawn.delaybypass` | Bypass spawn delay between uses | `false` | `/spawn` |
| `justplugin.home.delaybypass` | Bypass home delay between uses | `false` | `/home` |
| `justplugin.back.delaybypass` | Bypass back delay between uses | `false` | `/back` |
| `justplugin.wild.delaybypass` | Bypass wild/RTP delay between uses | `false` | `/tpr` |

### 👁️ Visibility Permissions

| Permission | Description | Default | Used By |
|------------|-------------|---------|---------|
| `justplugin.playerlist.seeHiddenPlayers` | See players hidden from playerlist (shows `[Hidden]` tag with hover info) | `op` | `/playerlist` |

---

## 📝 LuckPerms Examples

### Setting up a basic player group (defaults are already granted):
```
/lp group default permission set justplugin.player true
```

### Creating a staff group with moderation tools:
```
/lp creategroup staff
/lp group staff parent add default
/lp group staff permission set justplugin.fly true
/lp group staff permission set justplugin.gamemode true
/lp group staff permission set justplugin.god true
/lp group staff permission set justplugin.speed true
/lp group staff permission set justplugin.heal true
/lp group staff permission set justplugin.feed true
/lp group staff permission set justplugin.vanish true
/lp group staff permission set justplugin.vanish.see true
/lp group staff permission set justplugin.hat true
/lp group staff permission set justplugin.exp true
/lp group staff permission set justplugin.skull true
/lp group staff permission set justplugin.gmcheck true
/lp group staff permission set justplugin.anvil true
/lp group staff permission set justplugin.grindstone true
/lp group staff permission set justplugin.craft true
/lp group staff permission set justplugin.itemname true
/lp group staff permission set justplugin.playerinfo true
/lp group staff permission set justplugin.getpos.others true
/lp group staff permission set justplugin.getdeathpos.others true
```

### Creating an admin group with full control:
```
/lp creategroup admin
/lp group admin parent add staff
/lp group admin permission set justplugin.* true
```

### Granting ".others" permissions to staff:
```
/lp group staff permission set justplugin.fly.others true
/lp group staff permission set justplugin.gamemode.others true
/lp group staff permission set justplugin.god.others true
/lp group staff permission set justplugin.speed.others true
/lp group staff permission set justplugin.heal.others true
/lp group staff permission set justplugin.feed.others true
/lp group staff permission set justplugin.kill.others true
/lp group staff permission set justplugin.exp.others true
/lp group staff permission set justplugin.vanish.others true
/lp group staff permission set justplugin.balance.others true
/lp group staff permission set justplugin.addcash true
/lp group staff permission set justplugin.addcash.others true
/lp group staff permission set justplugin.getpos.others true
/lp group staff permission set justplugin.getdeathpos.others true
```

### Granting moderation permissions to moderators:
```
/lp creategroup moderator
/lp group moderator parent add staff
/lp group moderator permission set justplugin.mute true
/lp group moderator permission set justplugin.tempmute true
/lp group moderator permission set justplugin.unmute true
/lp group moderator permission set justplugin.warn true
/lp group moderator permission set justplugin.kick true
/lp group moderator permission set justplugin.ban true
/lp group moderator permission set justplugin.tempban true
/lp group moderator permission set justplugin.unban true
/lp group moderator permission set justplugin.log.moderation true
/lp group moderator permission set justplugin.log.warn true
/lp group moderator permission set justplugin.log.mute true
/lp group moderator permission set justplugin.announce.ban true
/lp group moderator permission set justplugin.announce.kick true
/lp group moderator permission set justplugin.announce.mute true
/lp group moderator permission set justplugin.announce.warn true
/lp group moderator permission set justplugin.banlist true
/lp group moderator permission set justplugin.oplist true
/lp group moderator permission set justplugin.deathitems.others true
```

---

## New v1.0.1 Permissions

### 🪓 Death Items Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.deathitems` | View and restore your own death items | `op` | `/deathitems` |
| `justplugin.deathitems.others` | View and restore other players' death items | `op` | `/deathitems <player>` |

> `.others` automatically grants `.deathitems` (self). Both are admin-only by default.

### 🔌 Plugins Permission

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.plugins` | View the list of installed server plugins | `op` | `/plugins`, `/pl` |

### 🏅 Rank Permissions (LuckPerms Management)

| Permission | Description | Default | Context |
|------------|-------------|---------|---------|
| `justplugin.rank` | Open the /rank main menu | `true` (player) | `/rank` |
| `justplugin.rank.groups` | View the group list | `op` | Groups tab |
| `justplugin.rank.groups.create` | Create new LuckPerms groups | `op` | Group list |
| `justplugin.rank.groups.delete` | Delete LuckPerms groups | `op` | Group actions |
| `justplugin.rank.groups.rename` | Rename groups (display name) | `op` | Group actions |
| `justplugin.rank.groups.prefix` | Change a group's prefix | `op` | Group actions |
| `justplugin.rank.groups.suffix` | Change a group's suffix | `op` | Group actions |
| `justplugin.rank.groups.parent` | Add/change parent groups (inheritance) | `op` | Group actions |
| `justplugin.rank.groups.permissions` | View permission nodes on a group | `op` | Group actions |
| `justplugin.rank.groups.permissions.add` | Add permission nodes to a group | `op` | Permission list |
| `justplugin.rank.groups.permissions.remove` | Remove permission nodes from a group | `op` | Permission list |
| `justplugin.rank.groups.permissions.toggle` | Enable/disable permission nodes on a group | `op` | Permission list |
| `justplugin.rank.groups.permissions.expiry` | Set expiry on permission nodes for a group | `op` | Permission list |
| `justplugin.rank.players` | View the player list | `op` | Players tab |
| `justplugin.rank.players.addgroup` | Add a player to a LuckPerms group | `op` | Player actions |
| `justplugin.rank.players.removegroup` | Remove a player from a group | `op` | Player actions |
| `justplugin.rank.players.listgroups` | View a player's groups | `op` | Player actions |
| `justplugin.rank.players.permissions` | View permission nodes on a player | `op` | Player actions |
| `justplugin.rank.players.permissions.add` | Add permission nodes to a player | `op` | Permission list |
| `justplugin.rank.players.permissions.remove` | Remove permission nodes from a player | `op` | Permission list |
| `justplugin.rank.players.permissions.toggle` | Enable/disable permission nodes on a player | `op` | Permission list |
| `justplugin.rank.players.permissions.expiry` | Set expiry on permission nodes for a player | `op` | Permission list |

> The ranks system is **disabled by default** in config. When enabled, LuckPerms must be installed. If LuckPerms is missing, players are notified.
> Every management action requires its own permission — opening the menu (`justplugin.rank`) is allowed for all players, but all actions are gated behind `op`-level permissions by default.

### 🛡️ Moderation List Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.oplist` | View the server operator list | `op` | `/oplist`, `/ops` |
| `justplugin.banlist` | View ban and IP ban lists | `op` | `/banlist`, `/baniplist` |

### 🧹 Entity Clear & World Permissions

| Permission | Description | Default | Commands |
|------------|-------------|---------|----------|
| `justplugin.clearentities` | Manually trigger entity clearing | `op` | `/clearentities` |
| `justplugin.clearchat` | Clear the server chat | `op` | `/clearchat` |
| `justplugin.friendlyfire` | Toggle PvP on/off server-wide | `op` | `/friendlyfire`, `/ff` |

### 📢 Punishment Announcement Permissions

When public punishment announcements are **disabled** (default), only staff with these permissions see the announcement. These are separate from log permissions.

| Permission | Description | Default |
|------------|-------------|---------|
| `justplugin.announce.ban` | See ban announcements | `op` |
| `justplugin.announce.banip` | See IP ban announcements | `op` |
| `justplugin.announce.tempban` | See temp ban announcements | `op` |
| `justplugin.announce.tempbanip` | See temp IP ban announcements | `op` |
| `justplugin.announce.mute` | See mute announcements | `op` |
| `justplugin.announce.tempmute` | See temp mute announcements | `op` |
| `justplugin.announce.warn` | See warning announcements | `op` |
| `justplugin.announce.kick` | See kick announcements | `op` |

