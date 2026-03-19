# 📊 JustPlugin - Scoreboard System

> **Version:** 1.1  
> **Config File:** `scoreboard.yml`  
> **Author:** JustMe  
> **Last Updated:** March 19, 2026

---

## Overview

JustPlugin includes a fully configurable sidebar scoreboard that displays real-time information to players. It supports **50+ placeholder variables**, per-line emoji customization, MiniMessage formatting, and deep configuration options.

The scoreboard updates every second by default (configurable) and each player sees their own personalized data (balance, health, coordinates, etc.).

---

## Table of Contents

- [Quick Start](#quick-start)
- [Configuration Reference](#configuration-reference)
- [Placeholder Variables](#placeholder-variables)
- [Line Configuration](#line-configuration)
- [Emoji System](#emoji-system)
- [Commands](#commands)
- [Permissions](#permissions)
- [Examples](#examples)

---

## Quick Start

1. The scoreboard is **enabled by default** when JustPlugin is installed
2. The config file `scoreboard.yml` is auto-generated in the plugin folder
3. Players can toggle it with `/scoreboard` (or `/sb`)
4. Admins can reload the config with `/scoreboard reload`

---

## Configuration Reference

All settings are in `plugins/JustPlugin/scoreboard.yml`:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable the entire scoreboard system |
| `update-interval` | int (ticks) | `20` | How often the scoreboard refreshes. 20 ticks = 1 second. Lower = smoother but more CPU |
| `global-emojis` | boolean | `true` | Master toggle for all emojis. When `false`, no emojis appear regardless of per-line settings |
| `title` | string | `<gradient:...>JustPlugin</gradient>` | The sidebar title. Supports MiniMessage and placeholders |
| `lines` | list | (see below) | Ordered list of line entries displayed top to bottom |

**Important:** The Minecraft sidebar supports a **maximum of 15 lines**.

---

## Placeholder Variables

Use `{placeholder_name}` in any line text or in the title. All placeholders are resolved per-player.

### Player Info

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{player}` | `{name}` | Player's username | 👤 |
| `{display_name}` | `{displayname}` | Player's display name (with formatting) | 👤 |
| `{uuid}` | | Player's UUID | |
| `{ip}` | | Player's IP address | |

### Health & Food

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{health}` | | Current health (e.g. `20.0`) | ❤ |
| `{max_health}` | `{maxhealth}` | Maximum health | ❤ |
| `{health_bar}` | `{healthbar}` | Visual health bar using ❤ symbols | ❤ |
| `{food}` | `{hunger}` | Current food level (0-20) | 🍖 |
| `{saturation}` | | Current saturation level | 🍖 |
| `{absorption}` | | Absorption hearts amount | 💛 |

### Level & Experience

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{level}` | `{lvl}` | Current XP level | ⭐ |
| `{exp}` | `{xp}` | XP progress as percentage (e.g. `45%`) | ⭐ |
| `{total_exp}` | `{totalexp}` | Total experience points | ⭐ |

### Location

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{x}` | | Block X coordinate | 📍 |
| `{y}` | | Block Y coordinate | 📍 |
| `{z}` | | Block Z coordinate | 📍 |
| `{world}` | `{world_name}` | Current world name | 🌍 |
| `{biome}` | | Current biome (e.g. `plains`, `dark forest`) | 🌿 |
| `{dimension}` | | Dimension name: Overworld, Nether, The End | 🌍 |
| `{direction}` | `{facing}` | Cardinal direction: N, NE, E, SE, S, SW, W, NW | 🧭 |
| `{light_level}` | `{lightlevel}`, `{light}` | Light level at player's position (0-15) | 💡 |
| `{yaw}` | | Player's yaw rotation | |
| `{pitch}` | | Player's pitch rotation | |

### Player State

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{gamemode}` | `{gm}` | Current gamemode (survival, creative, etc.) | 🎮 |
| `{fly_status}` | `{fly}`, `{flying}` | Flight status: Enabled or Disabled | 🕊 |
| `{god_status}` | `{god}` | God mode: Enabled or Disabled | 🛡 |
| `{vanish_status}` | `{vanish}` | Vanish state: Visible, Vanished, or Super Vanish | 👻 |
| `{speed}` | | Dynamic speed (fly speed if flying, walk speed if walking) | 💨 |
| `{fly_speed}` | `{flyspeed}` | Fly speed (0-10 scale) | 💨 |
| `{walk_speed}` | `{walkspeed}` | Walk speed (0-10 scale) | 💨 |

### Held Items & Armor

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{held_item}` | `{helditem}`, `{hand}` | Item in main hand (or "Empty") | 🗡 |
| `{offhand}` | `{offhand_item}` | Item in off hand (or "Empty") | 🛡 |
| `{armor_durability}` | `{armordurability}` | Total armor durability as percentage | 🛡 |

### Economy

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{balance}` | `{bal}`, `{money}` | Formatted balance (e.g. `$1,234.56`) | 💰 |
| `{balance_raw}` | `{bal_raw}` | Raw balance number (e.g. `1234.56`) | 💰 |
| `{balance_rank}` | `{balrank}` | Player's rank in balance leaderboard (e.g. `#3`) | 🏆 |

> **Note:** Economy placeholders work with both JustPlugin's built-in economy and Vault (when configured).

### Statistics

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{kills}` | `{player_kills}` | Total player kills | ⚔ |
| `{deaths}` | | Total deaths | 💀 |
| `{kdr}` | `{kd}` | Kill/death ratio (e.g. `2.50`) | ⚔ |
| `{mobs_killed}` | `{mobkills}` | Total mobs killed | 🐛 |
| `{blocks_broken}` | `{blocksbroken}` | Total blocks mined | ⛏ |
| `{blocks_placed}` | `{blocksplaced}` | Total blocks placed | 🧱 |
| `{blocks_walked}` | `{blockswalked}`, `{distance}` | Total distance walked (blocks) | 🚶 |
| `{jumps}` | | Total jumps | 🦘 |
| `{damage_dealt}` | | Total damage dealt | ⚔ |
| `{damage_taken}` | | Total damage taken | 💔 |
| `{fish_caught}` | `{fishcaught}` | Total fish caught | 🎣 |
| `{animals_bred}` | `{bred}` | Total animals bred | 🐄 |
| `{items_crafted}` | `{crafted}` | Total items crafted | 🔨 |
| `{times_slept}` | `{slept}` | Times slept in a bed | 🛏 |

> **Performance note:** `{blocks_broken}`, `{blocks_placed}`, and `{items_crafted}` are cached for 10 seconds to avoid expensive per-material iteration.

### Time & Playtime

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{total_playtime}` | `{playtime}` | Total playtime (e.g. `5d 3h`) | ⏱ |
| `{session_playtime}` | `{session}` | Current session playtime | ⏱ |

### Team

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{team}` | `{team_name}` | Player's team name (or "None") | 🏴 |
| `{team_members}` | `{team_size}` | Number of members in the team | 👥 |
| `{team_leader}` | | Team leader's name | 👑 |

### Home & Warp

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{homes_count}` | `{homes}` | Number of homes set | 🏠 |
| `{warps_count}` | `{warps}` | Total server warps available | 🌀 |

### Chat & Warnings

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{chat_mode}` | `{chatmode}` | Current chat mode: All or Team | 💬 |
| `{active_warnings}` | `{warns}` | Number of active warnings | ⚠ |
| `{total_warnings}` | `{totalwarns}` | Total warnings (including lifted) | ⚠ |

### Server Info

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{online}` | `{online_players}` | Current online player count | 👥 |
| `{max_players}` | `{maxplayers}`, `{max}` | Maximum player slots | 👥 |
| `{tps}` | | Server TPS (cached 5s, max 20.0) | 📊 |
| `{server_name}` | `{servername}` | Server name | 🖥 |
| `{free_memory}` | `{freemem}` | Free JVM memory in MB | 🧠 |
| `{used_memory}` | `{usedmem}` | Used JVM memory in MB | 🧠 |
| `{max_memory}` | `{maxmem}` | Max JVM memory in MB | 🧠 |
| `{uptime}` | `{server_uptime}` | Server uptime (e.g. `2d 5h`) | ⏰ |
| `{ping}` | `{latency}` | Player's ping in ms | 📶 |

### Weather & Time

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{weather}` | | Current weather: Clear, Rain, or Thunder | 🌤 |
| `{real_time}` | `{irl_time}`, `{clock}` | Real-world time (HH:mm:ss, uses config timezone) | 🕐 |
| `{real_date}` | `{irl_date}`, `{date}` | Real-world date (yyyy-MM-dd, uses config timezone) | 📅 |
| `{game_time}` | `{world_time}` | In-game time (24h format, e.g. `14:30`) | 🌙 |

### Utility

| Placeholder | Aliases | Description | Default Emoji |
|-------------|---------|-------------|---------------|
| `{empty}` | `{blank}` | Empty string (for spacing) | |
| `{line}` | | Separator line (━━━━━━━━━━━━) | |

---

## Line Configuration

Each line in the `lines` list has three properties:

```yaml
lines:
  - text: "{emoji} <green>{balance}"    # The display text with MiniMessage + placeholders
    emoji: "💰"                          # The emoji to use when {emoji} is in the text
    show-emoji: true                     # Whether to show the emoji for THIS line
```

### How `{emoji}` works in text

When you put `{emoji}` in a line's `text`, it gets replaced with that line's `emoji` value (if both `global-emojis` and `show-emoji` are true). If either is false, `{emoji}` is replaced with an empty string.

This lets you control where the emoji appears:
- `"{emoji} Balance: {balance}"` - Emoji on the left
- `"Balance: {balance} {emoji}"` - Emoji on the right
- `"Balance: {balance}"` - No emoji placeholder = never shows

---

## Emoji System

The emoji system has two levels of control:

1. **Global toggle** (`global-emojis: true/false`) - Master switch for all emojis
2. **Per-line toggle** (`show-emoji: true/false`) - Individual control per line

| `global-emojis` | `show-emoji` | Result |
|-----------------|--------------|--------|
| `true` | `true` | Emoji shown |
| `true` | `false` | No emoji |
| `false` | `true` | No emoji |
| `false` | `false` | No emoji |

Each line's `emoji` can be customized to any text or Unicode character.

---

## Commands

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/scoreboard` | `/sb` | Toggle the sidebar scoreboard on/off for yourself | `justplugin.scoreboard` |
| `/scoreboard reload` | `/sb reload` | Reload `scoreboard.yml` from disk | `justplugin.scoreboard.reload` |

---

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `justplugin.scoreboard` | `true` (all players) | Toggle the scoreboard on/off |
| `justplugin.scoreboard.reload` | `op` | Reload the scoreboard configuration |

---

## Examples

### Minimal Scoreboard (3 lines)

```yaml
enabled: true
update-interval: 20
global-emojis: false
title: "<gold><bold>My Server</bold></gold>"
lines:
  - text: "<white>{player}"
    emoji: ""
    show-emoji: false
  - text: "<green>{balance}"
    emoji: ""
    show-emoji: false
  - text: "<gray>{online}/{max_players} online"
    emoji: ""
    show-emoji: false
```

### PvP Server Scoreboard

```yaml
enabled: true
update-interval: 10
global-emojis: true
title: "<red><bold>  PvP Arena  </bold></red>"
lines:
  - text: "<gray>━━━━━━━━━━━━━━━━━━━━"
    emoji: ""
    show-emoji: false
  - text: "{emoji} <white>{player}"
    emoji: "⚔"
    show-emoji: true
  - text: "{emoji} <red>{health}<dark_gray>/<red>{max_health}"
    emoji: "❤"
    show-emoji: true
  - text: "<dark_gray>"
    emoji: ""
    show-emoji: false
  - text: "{emoji} <green>{kills} <dark_gray>Kills"
    emoji: "🗡"
    show-emoji: true
  - text: "{emoji} <red>{deaths} <dark_gray>Deaths"
    emoji: "💀"
    show-emoji: true
  - text: "{emoji} <yellow>{kdr} <dark_gray>K/D"
    emoji: "📊"
    show-emoji: true
  - text: "<dark_gray>"
    emoji: ""
    show-emoji: false
  - text: "{emoji} <aqua>{x}, {y}, {z}"
    emoji: "📍"
    show-emoji: true
  - text: "{emoji} <white>{ping}ms"
    emoji: "📶"
    show-emoji: true
  - text: "<gray>━━━━━━━━━━━━━━━━━━━━"
    emoji: ""
    show-emoji: false
```

### Economy-Focused Scoreboard

```yaml
enabled: true
update-interval: 40
global-emojis: true
title: "<gradient:#FFD700:#FFA500><bold>  Economy  </bold></gradient>"
lines:
  - text: "<gray>━━━━━━━━━━━━━━━━━━━━"
    emoji: ""
    show-emoji: false
  - text: "{emoji} <white>{player}"
    emoji: "👤"
    show-emoji: true
  - text: "{emoji} <green>{balance}"
    emoji: "💰"
    show-emoji: true
  - text: "{emoji} <yellow>Rank {balance_rank}"
    emoji: "🏆"
    show-emoji: true
  - text: "<dark_gray>"
    emoji: ""
    show-emoji: false
  - text: "{emoji} <white>{team}"
    emoji: "🏴"
    show-emoji: true
  - text: "{emoji} <aqua>{homes_count} homes"
    emoji: "🏠"
    show-emoji: true
  - text: "<dark_gray>"
    emoji: ""
    show-emoji: false
  - text: " <dark_gray>{real_time} | {online} online"
    emoji: ""
    show-emoji: false
  - text: "<gray>━━━━━━━━━━━━━━━━━━━━"
    emoji: ""
    show-emoji: false
```

---

## Tips

- **Performance:** Keep `update-interval` at 20 (1 second) or higher. Going below 10 ticks is unnecessary for most servers.
- **Line limit:** Sidebar supports max 15 lines. Lines beyond 15 are silently ignored.
- **Empty lines:** Use `text: "<dark_gray>"` with `show-emoji: false` as a spacer between sections.
- **Separator lines:** Use `text: "<gray>━━━━━━━━━━━━━━━━━━━━"` for visual separators.
- **Disable per-player:** Players can toggle the scoreboard with `/scoreboard` and the preference is saved across sessions and restarts.
- **MiniMessage:** All text fields support full MiniMessage formatting - gradients, colors, bold, italic, etc. See `FORMATTING.md` for details.

