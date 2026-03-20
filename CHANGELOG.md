# JustPlugin - Changelog

## v1.2 - Ranks, Revamps & Permission Fixes
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

## v1.1 - Initial Release
**Released:** March 2026

- Full release with 90+ commands
- Economy, teleportation, moderation, teams, trading, vanish, warnings, mutes
- Scoreboard, tab list, web config editor, Discord webhook logging
- Vault integration, entity clear system, safe teleport protection
- Developer API for ecosystem plugins

