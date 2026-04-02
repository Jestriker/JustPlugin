# Kit System

JustPlugin's kit system allows server administrators to create, manage, and distribute
pre-defined sets of items to players. Kits support cooldowns, permissions, and a full
lifecycle (pending -> published -> archived).

## Player Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/kit` | `justplugin.kit` | Opens the kit selection GUI |
| `/kit <name>` | `justplugin.kit` + `justplugin.kits.<name>` | Claim a specific kit |
| `/kitpreview <name>` | `justplugin.kit.preview` + `justplugin.kits.<name>` | Preview a kit's contents |

## Admin Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/kitcreate` | `justplugin.kit.create` | Create a new kit via GUI |
| `/kitedit <name>` | `justplugin.kit.edit` | Edit an existing kit's items |
| `/kitrename <old> <new>` | `justplugin.kit.rename` | Rename a kit |
| `/kitdelete <name>` | `justplugin.kit.delete` | Archive a kit (soft delete) |
| `/kitdelete <name> permanent` | `justplugin.kit.delete.permanent` | Permanently delete a kit |
| `/kitpublish <name>` | `justplugin.kit.publish` | Publish a kit for players |
| `/kitdisable <name>` | `justplugin.kit.disable` | Disable a kit temporarily |
| `/kitenable <name>` | `justplugin.kit.disable` | Re-enable a disabled kit |
| `/kitarchive` | `justplugin.kit.archive` | List archived kits |
| `/kitarchive restore <name>` | `justplugin.kit.archive.restore` | Restore an archived kit |
| `/kitarchive delete <name>` | `justplugin.kit.archive.delete` | Permanently delete an archived kit |
| `/kitarchive deleteall` | `justplugin.kit.archive.delete` | Delete all archived kits |
| `/kitlist` | `justplugin.kit.list` | List all kits with statuses |

## Kit Lifecycle

1. **Pending** - Kit is created but not yet available to players.
2. **Published** - Kit is active and can be claimed by players with permission.
3. **Archived** - Kit is soft-deleted. Can be restored or permanently deleted.
4. **Disabled** - Published kit that is temporarily unavailable (separate from status).

## Kit Features

- **Auto-equip Armor**: Helmets, chestplates, leggings, and boots are automatically
  equipped to the correct armor slot when claiming a kit.
- **Cooldowns**: Each kit has a configurable cooldown between claims.
  Players with `justplugin.kit.cooldownbypass` bypass all kit cooldowns.
- **Per-Kit Permissions**: Each kit requires `justplugin.kits.<name>` to claim.
- **Archive Retention**: Archived kits are auto-deleted after a configurable number
  of days (default 30, set in config.yml under `kits.archive-retention-days`).
- **GUI Preview**: Players can right-click kits in the selection GUI to preview contents.

## Configuration

In `config.yml`:
```yaml
kits:
  archive-retention-days: 30  # Days before archived kits are auto-deleted (0 = never)
```

All kit commands can be individually enabled/disabled in the `command-settings` section.

## Data Storage

Kit definitions, items, and cooldowns are stored in `kits.yml` in the plugin data folder.
Items are serialized using Bukkit's built-in ItemStack serialization.
