# JustPlugin - Changelog

## Unreleased

### License
+ **License changed from MIT to PolyForm Noncommercial 1.0.0.** JustPlugin remains free for personal, educational, community, and other non-commercial use. Commercial use (reselling, paid hosting, paid bundles, in-game perk sales, or any revenue-generating use) now requires written permission from the author. Credit is required for all uses.
+ Added `LICENSE` (full PolyForm Noncommercial 1.0.0 text) and `COMMERCIAL.md` (commercial-licensing FAQ + contact instructions) to the repo root.

### Added
+ `/permissions <player> [filter] [page]` — staff-only command to inspect an online player's effective permissions. 10 per page, clickable `← Prev` / `Next →` navigation, optional substring filter, and a hover tooltip showing which plugin or attachment granted each permission. Permission: `justplugin.permissions` (default `op`). Aliases: `perms`, `permslist`, `permissionlist`.
+ New `justplugin.log` parent permission (default `op`) — grants all `justplugin.log.*` children at once so operators see every staff action in chat out of the box.
+ 14 new top-level log permission definitions for fine-grained control: `justplugin.log.admin`, `.economy`, `.gamemode`, `.item`, `.jail`, `.moderation`, `.mute`, `.player`, `.security`, `.teleport`, `.unjail`, `.vanilla`, `.vanish`, `.warn`. Each defaults to `op` — set to `false` in LuckPerms/etc. to suppress specific categories.
+ Audit logging now covers previously silent admin commands: `/setjail`, `/deljail`, `/permissions`, `/jpbackup export|import|delete`, `/automessage reload|toggle|send`, `/tagcreate`, `/tagdelete`, `/kitrename`, `/kitdelete`, `/kitpublish`, `/kitdisable`, `/kitenable`, `/kitarchive restore|delete|deleteall`. Each log entry includes the executor, action, and full arguments.

### Fixed
+ Fixed a stray character in the default `scoreboard.yml` that caused `InvalidConfigurationException: could not find expected ':'` on first startup.

### Changed
+ `justplugin.announce.ban/banip/tempban/tempbanip/mute/tempmute/warn/kick` now default to `op` (previously `false`). Operators see punishment announcements by default — matches the existing `justplugin.announce.jail` behavior.
+ `plugin.yml` now declares `website: https://liam.plus/justplugin`, so the wiki URL is visible in `/plugins JustPlugin`.
+ `README.md`, `DESCRIPTION.md`, and `DESCRIPTION_PLAIN.txt` now link to the official wiki at https://liam.plus/justplugin.
+ AFK notifications are now configurable via `afk.announce-mode` in `config.yml` — `everyone` / `staff` / `self` / `none`. **Default changed to `self`** — AFK and return-from-AFK messages are now private to the player by default instead of broadcasting to the server. Set to `everyone` to restore the old behavior. New `justplugin.afk.see` permission (default `op`) controls who receives notifications under `staff` mode. The legacy `afk.broadcast` boolean is still honored when `announce-mode` is unset (true → everyone, false → self).

---

## v1.0 - Player Vaults, Transaction History, Utilities & Events API
**Released:** April 12, 2026

### What's New

#### Player Vaults System
+ `/pv [number]` - 54-slot virtual storage inventories, separate from ender chest
+ `/pv <player> <number>` - staff access to view other players' vaults
+ Configurable max vaults per player (default: 3) with permission-based override (`justplugin.vaults.<number>`)
+ Data saved on every close, quit, and auto-save to prevent data loss
+ Disabled by default - opt-in via `vaults.enabled` in config.yml

#### Transaction History
+ `/transactions [player]` - paginated GUI showing all economy transactions
+ Tracks 6 transaction types: PAY, PAYNOTE_CREATE, PAYNOTE_REDEEM, ADDCASH, TRADE, API
+ Click any transaction to view full details (time, amount, parties involved)
+ Configurable retention period (default: 30 days) and max entries (default: 500)
+ Configurable PayNote creator visibility: show name, anonymous, or no notification
+ Configurable AddCash staff name visibility to players

#### Near Command
+ `/near [radius]` - show nearby players with distance, compass direction, and coordinates
+ Clickable `[TP]` button with safe teleport for each player
+ Staff-only command with configurable default radius (1000) and max radius (5000)
+ Excludes vanished players unless viewer has `justplugin.vanish.see`

#### Repair Command
+ `/repair` - repair held item to maximum durability
+ `/repair <player>` - repair another player's held item (staff)
+ Separate permissions for self and others

#### Enchant Command
+ `/enchant <enchantment> [level]` - apply enchantments to held items
+ Level 0 removes the enchantment
+ Respects vanilla restrictions by default (item type, max level)
+ Configurable bypass via `enchant.bypass-restrictions` or `justplugin.enchant.bypass` permission
+ Supports both namespaced and simple enchantment names

#### Custom Events API
+ 10 custom Bukkit events for add-on plugins to listen to
+ **Cancellable events:** PlayerBalanceChangeEvent, PlayerPunishEvent, PlayerTeleportRequestEvent, PlayerTradeEvent, PlayerJailEvent, KitClaimEvent
+ **Informational events:** PlayerUnjailEvent, PlayerAfkEvent, WarpCreateEvent, WarpDeleteEvent
+ All events use defensive copies for security - no database access exposed
+ Full JavaDoc documentation

#### Documentation
+ Wiki: Added Placeholders reference, Formatting guide, Migration guide, Troubleshooting page, Comparison table
+ Wiki: Global search (Cmd+K), breadcrumbs, prev/next navigation, sticky table of contents
+ Wiki: Version selector, changelog page with color-coded +/~/- badges
+ Wiki: Light/dark mode toggle with animated sun/moon icon

---

## beta.5 - Folia Support, Automated Messages & Platform Expansion
**Released:** April 3, 2026

### What's New

#### Native Folia Support
- **SchedulerUtil compatibility layer** - detects Folia at runtime and routes all scheduler calls appropriately
- Replaced 75+ `Bukkit.getScheduler()` calls across 27 files with Folia-compatible wrappers
- Entity-bound, global, async, and location-based schedulers all supported
- All synchronous teleports converted to `teleportAsync()`
- `folia-supported: true` flag in plugin.yml
- Plugin now runs natively on **Paper, Purpur, and Folia**

#### Automated Messages System
- Configurable automated broadcast messages sent to players at intervals or specific times
- 4 scheduling modes: `interval`, `schedule` (specific times of day), `on-the-hour`, `on-the-half-hour`
- Rotating message support - cycle through multiple messages
- Per-message permission filtering and world filtering
- Custom prefix, sound effects, full MiniMessage formatting
- `/automessage reload|list|toggle|send` management commands
- Dedicated `automessages.yml` config file with 8 examples
- Disabled by default

#### Web Editor Expansion
- Added `database.yml`, `automessages.yml`, `texts/kits.yml`, `texts/nick.yml` to the browser-based config editor
- All 25+ config files now editable from the web interface

#### Platform Support
- Official support for Paper, Purpur, and Folia
- Version bumped to beta.5

---

## beta.4 - Database, Jail, Kits, AFK, Mail & 28+ New Features
**Released:** April 3, 2026

### What's New

#### Database Support
- **Multi-backend storage** - choose between SQLite, MySQL, or YAML via `database.yml`
- Switch backends without data loss - all player data, economy, punishments, and more supported

#### Jail System
- `/jail <player> [duration] [reason]` - jail players with temporary or permanent sentences
- `/unjail`, `/setjail`, `/deljail`, `/jails`, `/jailinfo` - full jail management
- Multiple named jail locations with random selection
- Jailed players are restricted from movement, commands, and interactions
- Persists across restarts. Works for offline players (jailed on next login)

#### Kit System
- `/kit` - GUI-based kit selection and claiming
- `/kitpreview`, `/kitcreate`, `/kitedit`, `/kitrename`, `/kitdelete`
- `/kitpublish`, `/kitdisable`, `/kitenable`, `/kitarchive`, `/kitlist`
- Full lifecycle: Pending -> Published -> Archived
- Per-kit permissions (`justplugin.kits.<name>`), cooldowns, auto-equip armor
- Archive retention with configurable auto-delete (default: 30 days)
- Full documentation in `KITS.md`

#### AFK System
- `/afk` - toggle AFK status manually
- Auto-AFK after configurable idle time
- Optional idle kick with bypass permission (`justplugin.afk.kickbypass`)
- AFK status shown in tab list and chat
- Movement, chat, and interaction auto-clear AFK

#### Mail System
- `/mail send <player> <message>` - offline messaging to any player
- `/mail read`, `/mail clear`, `/mail clearall` - inbox management with pagination
- Notifications on login for unread mail

#### Nickname System
- `/nick <name>` - custom display name with MiniMessage formatting
- `/nick off` / `/nick reset` - remove nickname
- Granular color permissions: `justplugin.nick.color`, `.format`, `.rainbow`
- Configurable min/max length validation

#### Tag System
- `/tag` - GUI-based tag selection and equipping
- `/tagcreate <id> <prefix|suffix> <display>` - admin tag creation
- `/tagdelete`, `/taglist` - tag management
- Tags display as prefixes or suffixes in chat

#### Backup & Export System
- `/jpbackup export` - create full plugin data backups
- `/jpbackup import <file>` - restore with confirmation
- `/jpbackup list`, `/jpbackup delete` - backup management
- All I/O runs asynchronously

#### Offline Player Commands
- `/tpoff <player>` - teleport to offline player's last location
- `/getposoff <player>` - view offline player's last known position
- `/getdeathposoff <player>` - view offline player's last death location
- `/invseeoff <player>` - view offline player's inventory
- `/echestseeoff <player>` - view offline player's ender chest

#### Spawn Protection
- Configurable radius around spawn where building is restricted
- Disabled by default - opt-in via `config.yml`
- Bypass with `justplugin.spawnprotection.bypass`

#### Seed Protection
- Blocks `/seed` command from non-permitted players
- Staff notifications on attempted use
- Bypass with `justplugin.seedprotection.bypass`

#### Custom Join/Leave Messages
- 5 visibility modes: none, all, staff-only, op-only, group-based
- Fully configurable message templates

### Performance & Security Improvements

- **Async I/O** - all file read/write operations run off the main thread
- **Thread safety** - concurrent data access protected throughout the plugin
- **Balance overflow protection** - prevents integer overflow in economy operations
- **Pay rate limiting** - prevents payment spam abuse
- **Input sanitization** - all user inputs are validated and sanitized
- **Teleport safety enhancements** - improved hazard detection
- **IP ban subnets (CIDR)** - ban entire IP ranges (e.g., `192.168.1.0/24`)
- **Webhook retry logic** - automatic retry with backoff for failed Discord webhook deliveries
- **Web editor CSRF protection** - prevents cross-site request forgery attacks
- **Scoreboard flicker fix** - eliminates visual flickering on scoreboard updates
- **Placeholder performance** - optimized caching for PlaceholderAPI
- **Web page extraction** - improved web config editor page handling
- **Tab completion cache** - cached completions for better performance
- **Graceful shutdown** - all data saved cleanly on server stop

### Config Changes
- New `database.yml` configuration file for storage backend selection
- New jail settings in `config.yml` under `jail`
- New kit settings in `config.yml` under `kits`
- New AFK settings in `config.yml` under `afk`
- New mail settings in `config.yml` under `mail`
- New nickname settings in `config.yml` under `nick`
- New tag settings in `config.yml` under `tags`
- New spawn protection settings in `config.yml` under `spawn-protection`
- New seed protection settings in `config.yml` under `seed-protection`
- New join/leave message settings in `config.yml`
- Config auto-migrates - new settings are added automatically on load

---

## beta.3 - Architecture Refactor & Listener Modularization
**Released:** March 29, 2026

### What's New

#### Listener Architecture Refactor
- **Monolithic `PlayerListener` split into 6 categorized sub-listeners** for better code organization, maintainability, and performance:
  - `connection/ConnectionListener` - Login, join, quit events (ban checks, maintenance mode, MOTD, data loading/saving, vanish handling, scoreboard, startup warnings)
  - `chat/ChatListener` - Async chat events (mute checks, chat formatting with LuckPerms prefixes/suffixes, hover stats, team chat mode, ignore filtering)
  - `combat/CombatListener` - Damage events (god mode, hunger drain prevention, bad potion effect blocking, teleport cancellation on damage)
  - `player/PlayerEventListener` - Death, respawn, teleport, movement, and advancement hiding for vanished players
  - `server/ServerListener` - Server list ping (MOTD, maintenance icon, vanished player hiding) and tab completion filtering
  - `inventory/InventoryListener` - PayNote redemption on right-click
- **`PlayerListener` converted to a shared state holder** - no longer implements `Listener` or handles events directly. Retains all public utility methods (god mode, death locations, back/death location persistence, inventory snapshots) used by the categorized sub-listeners
- **Zero behavioral changes** - all events are handled identically to before; this is a pure internal refactor
- **No duplicate event firing** - the old monolith is no longer registered as an event listener

#### Stats GUI
- New `/stats` command opens an interactive stats inventory GUI

#### Skin System
- `/skin set/clear` with Mojang API integration
- `/skinban` / `/skinunban` for banning specific skin names
- Auto-applies stored skins on join

#### Maintenance Mode
- Full maintenance system with kick screen, MOTD override, server icon, LuckPerms group bypass
- Cooldown with estimated end time and auto-disable option

### Internal Changes
- Each sub-listener receives the `JustPlugin` instance and delegates shared state operations to `PlayerListener` via `plugin.getPlayerListener()`
- `PlayerListener` class reduced from ~710 lines to ~146 lines (utility methods only)
- All event handler registration moved to categorized listeners in `JustPlugin.onEnable()`

---

## beta.2 - Ranks, Revamps & Permission Fixes
**Released:** March 21, 2026

### What's New

#### Ranks System (LuckPerms Integration)
- New `/rank` command opens a full management GUI - **disabled by default**, opt-in for servers running LuckPerms
- **Groups tab** - browse, create, delete, rename, change prefix/suffix, manage parent groups (inheritance), and edit permission nodes for any LuckPerms group. Default group is pinned to the top. Pagination, search, and filtering built in
- **Players tab** - browse all known LuckPerms users, add/remove from groups, view group memberships, and manage individual permission nodes. Default group is protected from removal
- **Chat prefix/suffix** - LuckPerms prefixes and suffixes now automatically display in chat. Only the highest-priority prefix is shown (resolved across all groups, inheritance chains, and player-specific overrides)
- **Configurable chat separator** - the `»` between name and message can now be changed to `:`, `>`, `-`, `|`, or anything else in `config.yml` under `chat.separator`
- Type `clear` when editing a prefix, suffix, or display name to remove it. Type `cancel` to keep the old value
- 25+ granular permissions for every rank management action

#### Scoreboard Revamp
- **Animated wave gradient title** - the scoreboard header smoothly shifts colors back and forth (configurable colors, speed, and can be disabled)
- **Discord link variable** - `{discord}` and `{discord_link}` placeholders for scoreboard lines
- **Playtime mode toggle** - new `default-playtime` setting in scoreboard.yml. Choose between `total` (all-time, the new default) or `session` (since last login). Dynamic `{playtime_display}` placeholder follows this setting
- **Fast ping refresh** - ping checked every 5 seconds on a separate interval; scoreboard updates instantly when ping changes
- Updated default design with bold emojis and improved spacing

#### RTP Fix
- `/tpr` (random teleport) now correctly applies the pre-teleport cooldown countdown. Previously it was instant even with cooldown configured - now it properly waits the configured seconds (default 5s) with movement cancellation and safety checks, just like `/home`, `/warp`, and `/spawn`

#### Home & Baltop GUIs
- Home GUI: team home slot added alongside spawn. Lime-colored beds, light blue dyes for active teleport slots, and proper cooldown integration on all GUI actions
- Baltop GUI: renamed to "Top Balances", updated formatting

#### Tab List
- Added `{tps}` and `{ping}` placeholders for tab header/footer
- Default footer now shows Players, TPS, and Ping
- Configurable refresh interval (default 5 seconds, was hardcoded 30s)

#### Permission Fixes
- **Removed from default player group** (`justplugin.player`): `/back`, `/kill`, `/suicide`, `/enderchest`, `/anvil`, `/craft`, `/playerlist` - these now require explicit permission grants
- **`/deathitems`** changed from player-accessible to admin-only (`op` default)
- **`/plugins`** is now staff-only with `justplugin.plugins` permission (`op` default)
- All changes reflected in updated `PERMISSIONS.md` hierarchy

### Config Changes
- New `chat.separator` setting for customizing the name-to-message separator in chat
- New `ranks` section with the rank system config (disabled by default)
- New `default-playtime` setting in `scoreboard.yml`
- New `wave-title` settings in `scoreboard.yml` for the animated title
- New `ping-refresh-interval` in `scoreboard.yml`
- Config auto-migrates - new settings are added automatically on load

### Optional Dependencies
- **LuckPerms** - enables the `/rank` management GUI and chat prefix/suffix display. Fully optional
- **Vault** - use an external economy provider. Fully optional

---

## beta.1 - Initial Release
**Released:** March 2026

- Full release with 90+ commands
- Economy, teleportation, moderation, teams, trading, vanish, warnings, mutes
- Scoreboard, tab list, web config editor, Discord webhook logging
- Vault integration, entity clear system, safe teleport protection
- Developer API for ecosystem plugins

