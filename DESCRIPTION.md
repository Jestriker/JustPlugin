![Release Version](https://img.shields.io/badge/Release%20Version-1.4-blue)
![License](https://img.shields.io/badge/License-MIT-blue)
![Paper](https://img.shields.io/badge/Available%20on-Paper-purple)
![Java 25](https://img.shields.io/badge/Built%20with-Java%2025-orange)
![Folia](https://img.shields.io/badge/Available%20on-Folia-cyan)
![bStats Servers](https://img.shields.io/bstats/servers/30446)
![Discord](https://img.shields.io/discord/1482382694443913249)
![GitHub Stars](https://img.shields.io/github/stars/Jestriker/JustPlugin)

# JustPlugin - All-in-One Server Essentials

A lightweight, fully configurable server management plugin for Paper 1.21.11+ that replaces dozens of separate plugins with a single JAR. 200+ commands, 40+ managers, and every feature built from scratch with performance and simplicity in mind. Every command can be individually enabled or disabled. Every permission is granular and hierarchical.

## Core Features

**Economy** - Full balance system with Vault support, PayNotes, Baltop GUI, offline player support.

**Teleportation** - TPA, spawn, back, random TP with dimension selection GUI, safe teleport protection (3x3 hazard checks), warmup countdowns, and per-command cooldowns.

**Warps & Homes** - Set, delete, rename warps. Interactive home GUI. All persist across restarts.

**Moderation** - Ban, tempban, IP ban (CIDR subnet support), mute, tempmute, warn (progressive system with configurable escalation), kick, sudo, invsee, echestsee, deathitems, oplist, banlist.

**Jail System** - Jail players with configurable locations, durations, and reasons. Full restriction of movement, commands, and interactions.

**Kit System** - Create, publish, and distribute item kits with cooldowns, per-kit permissions, auto-equip armor, and full lifecycle management (pending/published/archived).

**AFK System** - Manual /afk toggle, auto-AFK after idle timeout, optional idle kick.

**Mail System** - Offline mail with send, read, clear. Notifications on login.

**Nicknames & Tags** - Custom display names with MiniMessage formatting. Tag system with prefix/suffix support and GUI selection.

**Vanish** - Standard vanish + Super Vanish (spectator ghost mode). Both persist across restarts.

**Teams** - Create, invite, join, leave, kick, disband. Team homes, team chat.

**Trading** - Hypixel SkyBlock-style GUI with item areas, balance transfer, and 5-second countdown.

**Skin Restorer** - Set/clear skins from Mojang API. Ban specific skin names. Auto-apply on join. Works for cracked players.

**Maintenance Mode** - Block non-whitelisted joins, custom kick screen, MOTD override, custom icon, LuckPerms group bypass, cooldown with auto-disable option.

**Scoreboard** - 50+ placeholders, animated wave gradient title, 5 text animation types, conditional lines, compact number formatting. Configurable in `scoreboard.yml`.

**Tab List** - Full placeholder support, animations, staff count, maintenance warning footer.

**MOTD Profiles** - Static, cycling, or random MOTD modes. Custom server icon from URL.

**Chat** - Private messaging with reply, ignore system, announcements, clear chat, hover stats tooltip with click-to-view. Custom join/leave messages with 5 visibility modes.

**Ranks (LuckPerms)** - Full management GUI for groups and players. Disabled by default.

**Database** - SQLite, MySQL, and YAML storage backends. Configurable in `database.yml`.

**Backup & Export** - Full plugin data backup/restore with async I/O.

**Spawn & Seed Protection** - Configurable spawn build radius and /seed command blocking.

**Offline Player Commands** - Teleport to, view position, view inventory, and view ender chest of offline players.

**Developer API** - Economy, Punishment, and Vanish APIs for add-on plugins.

**More** - Web config editor (with CSRF protection), Discord webhook logging (with retry), entity clear system, virtual inventories (anvil, craft, etc.), PlaceholderAPI support (optimized caching), bStats metrics, stats GUI, config auto-migration, modular listener architecture, async I/O, thread safety, graceful shutdown, input sanitization, tab completion cache.
