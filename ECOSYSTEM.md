# 🔌 JustPlugin - Ecosystem Developer Guide

> **Version:** 1.3  
> **API Package:** `org.justme.justPlugin.api`  
> **Minecraft:** 1.21.11 (Paper)  
> **Last Updated:** March 29, 2026

---

## Overview

JustPlugin exposes a public API that allows **add-on plugins** to interact with its core systems:

- **💰 Economy** - Read, modify, and transfer player balances
- **🔨 Punishments** - Ban, temp-ban, mute, temp-mute, warn, and check status
- **👻 Vanish** - Check if a player is vanished or super-vanished

This guide is intended for developers (or AI agents) building plugins that integrate with JustPlugin's ecosystem - for example, a **sign shop plugin**, a **chest shop**, an **auction house**, a **voting rewards** plugin, etc.

---

## Table of Contents

- [Setup - Adding JustPlugin as a Dependency](#setup--adding-justplugin-as-a-dependency)
- [Accessing the API](#accessing-the-api)
- [Economy API](#-economy-api)
- [Punishment API](#-punishment-api)
- [Vanish API](#-vanish-api)
- [Full Example: Sign Shop Plugin](#-full-example-sign-shop-plugin)
- [Important Notes & Best Practices](#-important-notes--best-practices)

---

## Setup - Adding JustPlugin as a Dependency

### 1. Place the JustPlugin JAR in your project

Since JustPlugin is not published to a Maven repository, place `JustPlugin-1.0-SNAPSHOT.jar` in a `libs/` folder in your add-on plugin project.

### 2. `build.gradle`

```groovy
plugins {
    id 'java'
}

group = 'me.justme'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = "https://repo.papermc.io/repository/maven-public/"
    }
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    // JustPlugin API - compileOnly so it's not bundled in your JAR
    compileOnly files('libs/JustPlugin-1.0-SNAPSHOT.jar')
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
```

### 3. `plugin.yml`

You **must** declare JustPlugin as a dependency so your plugin loads after it:

```yaml
name: MyAddonPlugin
version: '1.0'
main: me.justme.myaddon.MyAddonPlugin
api-version: '1.21'
depend: [JustPlugin]   # <-- REQUIRED: ensures JustPlugin loads first
```

> Use `depend` (hard dependency) if your plugin **cannot function** without JustPlugin.  
> Use `softdepend` if your plugin can work without it but gains features when JustPlugin is present.

---

## Accessing the API

The API is accessed via the static `JustPluginProvider` class. It returns `null` if JustPlugin is not loaded.

```java
import org.justme.justPlugin.api.JustPluginAPI;
import org.justme.justPlugin.api.JustPluginProvider;
import org.justme.justPlugin.api.EconomyAPI;
import org.justme.justPlugin.api.PunishmentAPI;
import org.justme.justPlugin.api.VanishAPI;

// Get the API instance (do this in onEnable or when needed - not in constructor)
JustPluginAPI api = JustPluginProvider.get();
if (api == null) {
    getLogger().severe("JustPlugin not found! Disabling...");
    getServer().getPluginManager().disablePlugin(this);
    return;
}

// Access sub-APIs
EconomyAPI economy = api.getEconomyAPI();
PunishmentAPI punishments = api.getPunishmentAPI();
VanishAPI vanish = api.getVanishAPI();
```

### ⚠️ When is the API available?

- The API is registered during JustPlugin's `onEnable()`.
- The API is cleared during JustPlugin's `onDisable()`.
- Always call `JustPluginProvider.get()` **after** your plugin loads (i.e., in `onEnable()` or later).
- If using `softdepend`, always null-check the result.

---

## 💰 Economy API

The `EconomyAPI` interface provides full access to player balances.

> **Vault Support:** JustPlugin optionally delegates to Vault's economy API when `economy.provider` is set to `"vault"` in `config.yml`. Your add-on plugin does **not** need to care about this - the `EconomyAPI` interface works identically regardless of the backend. All calls are transparently routed to either JustPlugin's built-in system or Vault.

### Interface

```java
public interface EconomyAPI {
    double getBalance(UUID uuid);
    void setBalance(UUID uuid, double amount);
    void addBalance(UUID uuid, double amount);
    boolean removeBalance(UUID uuid, double amount);   // returns false if insufficient funds
    boolean pay(UUID from, UUID to, double amount);     // returns false if insufficient or pay-toggled-off
    String format(double amount);                       // e.g. "$1,234.56"
    boolean hasBalance(UUID uuid, double amount);       // check if player has >= amount
}
```

### Method Details

| Method | Returns | Description |
|--------|---------|-------------|
| `getBalance(UUID)` | `double` | Get the player's current balance. Works for online and offline players. Returns the starting balance (default: `100.0`) if the player has never joined. |
| `setBalance(UUID, double)` | `void` | Set the player's balance to an exact amount. Saves immediately to disk. |
| `addBalance(UUID, double)` | `void` | Add to the player's balance. |
| `removeBalance(UUID, double)` | `boolean` | Subtract from the player's balance. Returns `false` if the player does not have enough (balance is NOT modified in that case). |
| `pay(UUID, UUID, double)` | `boolean` | Transfer money from one player to another. Returns `false` if: the sender doesn't have enough, the amount is ≤ 0, or the recipient has pay-toggle disabled. |
| `format(double)` | `String` | Format an amount using the server's currency symbol and locale. E.g., `format(1234.5)` → `"$1,234.50"`. The currency symbol is configured in JustPlugin's `config.yml` under `economy.currency-symbol`. |
| `hasBalance(UUID, double)` | `boolean` | Check if a player's balance is ≥ the given amount. Shortcut for `getBalance(uuid) >= amount`. |

### Example: Taking payment from a player

```java
EconomyAPI economy = JustPluginProvider.get().getEconomyAPI();
UUID buyerUuid = player.getUniqueId();
double price = 500.0;

if (!economy.hasBalance(buyerUuid, price)) {
    player.sendMessage("You don't have enough money! You need " + economy.format(price));
    return;
}

// Remove the money
if (economy.removeBalance(buyerUuid, price)) {
    player.sendMessage("Purchased for " + economy.format(price) + "!");
    player.sendMessage("New balance: " + economy.format(economy.getBalance(buyerUuid)));
} else {
    player.sendMessage("Transaction failed - insufficient funds.");
}
```

### Example: Paying a shop owner

```java
EconomyAPI economy = JustPluginProvider.get().getEconomyAPI();
UUID buyerUuid = player.getUniqueId();
UUID ownerUuid = shopOwnerUuid; // stored on the sign/chest
double price = 100.0;

// pay() handles balance check, pay-toggle check, and transfer atomically
if (economy.pay(buyerUuid, ownerUuid, price)) {
    player.sendMessage("Purchased! " + economy.format(price) + " sent to the shop owner.");
} else {
    player.sendMessage("Purchase failed. Not enough balance or the owner has payments disabled.");
}
```

### Example: Giving money (rewards, voting, etc.)

```java
EconomyAPI economy = JustPluginProvider.get().getEconomyAPI();
UUID playerUuid = player.getUniqueId();

economy.addBalance(playerUuid, 1000.0);
player.sendMessage("You received " + economy.format(1000.0) + " as a reward!");
```

---

## 🔨 Punishment API

The `PunishmentAPI` interface allows checking and managing bans, mutes, and warnings.

### Interface

```java
public interface PunishmentAPI {
    // --- Bans ---
    boolean isBanned(UUID uuid);
    void ban(UUID uuid, String playerName, String reason, String bannedBy);
    void tempBan(UUID uuid, String playerName, String reason, String bannedBy, long durationMs);
    boolean unban(UUID uuid);

    // --- Mutes ---
    boolean isMuted(UUID uuid);
    void mute(UUID uuid, String playerName, String reason, String mutedBy);
    void tempMute(UUID uuid, String playerName, String reason, String mutedBy, long durationMs);
    boolean unmute(UUID uuid);
    String getMuteReason(UUID uuid);

    // --- Warnings ---
    int getActiveWarnCount(UUID uuid);
    int getTotalWarnCount(UUID uuid);
    void addWarn(UUID uuid, String playerName, String reason, String warnedBy);
    boolean liftWarn(UUID uuid, int index, String liftedBy, String reason);
}
```

### Bans

| Method | Returns | Description |
|--------|---------|-------------|
| `isBanned(UUID)` | `boolean` | Check if a player is currently banned (permanent or temp). |
| `ban(UUID, name, reason, bannedBy)` | `void` | Permanently ban a player. Bans both UUID and username. If online, they are kicked with a ban screen. |
| `tempBan(UUID, name, reason, bannedBy, durationMs)` | `void` | Temporarily ban a player. Duration is in **milliseconds**. E.g., 1 hour = `3_600_000L`, 1 day = `86_400_000L`. |
| `unban(UUID)` | `boolean` | Unban a player. Returns `false` if they weren't banned. |

### Mutes

| Method | Returns | Description |
|--------|---------|-------------|
| `isMuted(UUID)` | `boolean` | Check if a player is currently muted (permanent or temp). |
| `mute(UUID, name, reason, mutedBy)` | `void` | Permanently mute a player. Muted players cannot use chat or `/msg`. |
| `tempMute(UUID, name, reason, mutedBy, durationMs)` | `void` | Temporarily mute a player. Duration in milliseconds. |
| `unmute(UUID)` | `boolean` | Unmute a player. Returns `false` if not muted. |
| `getMuteReason(UUID)` | `String` | Get the reason for the mute, or `null` if not muted. |

### Warnings

| Method | Returns | Description |
|--------|---------|-------------|
| `getActiveWarnCount(UUID)` | `int` | Number of active (non-lifted) warnings. This determines the next punishment level. |
| `getTotalWarnCount(UUID)` | `int` | Total warnings ever issued (including lifted ones). |
| `addWarn(UUID, name, reason, warnedBy)` | `void` | Issue a warning. Automatically applies the configured punishment (ChatMessage, Kick, TempBan, Ban, etc.) based on the active warning count. |
| `liftWarn(UUID, index, liftedBy, reason)` | `boolean` | Lift (cancel) a specific warning by its index. The warning stays in history but no longer counts toward punishment level. Returns `false` if the index is invalid or already lifted. |

### Example: Check if a player is banned before letting them interact

```java
PunishmentAPI punishments = JustPluginProvider.get().getPunishmentAPI();
UUID targetUuid = targetPlayer.getUniqueId();

if (punishments.isBanned(targetUuid)) {
    player.sendMessage("This player is currently banned.");
    return;
}
```

### Example: Auto-warn a player from your plugin

```java
PunishmentAPI punishments = JustPluginProvider.get().getPunishmentAPI();

// "MyAddonPlugin" is logged as the issuer
punishments.addWarn(
    player.getUniqueId(),
    player.getName(),
    "Attempted to exploit shop duplication",
    "MyAddonPlugin"
);
// JustPlugin automatically applies the configured punishment level
```

### Example: Temp-ban for 1 day

```java
PunishmentAPI punishments = JustPluginProvider.get().getPunishmentAPI();

punishments.tempBan(
    player.getUniqueId(),
    player.getName(),
    "Shop exploitation",
    "MyAddonPlugin",
    86_400_000L  // 1 day in milliseconds
);
```

### Duration Helper

For convenience, here are common durations in milliseconds:

| Duration | Milliseconds |
|----------|-------------|
| 5 minutes | `300_000L` |
| 30 minutes | `1_800_000L` |
| 1 hour | `3_600_000L` |
| 12 hours | `43_200_000L` |
| 1 day | `86_400_000L` |
| 7 days | `604_800_000L` |
| 30 days | `2_592_000_000L` |
| 365 days | `31_536_000_000L` |

---

## 👻 Vanish API

The `VanishAPI` allows checking a player's vanish status. This is useful to avoid showing vanished staff in shop GUIs, scoreboards, or leaderboards.

### Interface

```java
public interface VanishAPI {
    boolean isVanished(UUID uuid);
    boolean isSuperVanished(UUID uuid);
}
```

| Method | Returns | Description |
|--------|---------|-------------|
| `isVanished(UUID)` | `boolean` | `true` if the player is in regular vanish or super vanish. |
| `isSuperVanished(UUID)` | `boolean` | `true` only if in super vanish (spectator-based ghost mode). |

### Example: Skip vanished players in a leaderboard

```java
VanishAPI vanish = JustPluginProvider.get().getVanishAPI();

for (Player p : Bukkit.getOnlinePlayers()) {
    if (vanish.isVanished(p.getUniqueId())) continue;
    // ... add to leaderboard
}
```

---

## 🏪 Full Example: Sign Shop Plugin

Here's a simplified skeleton for a sign-based selling plugin that integrates with JustPlugin's economy:

### `plugin.yml`

```yaml
name: JustShops
version: '1.0'
main: me.justme.justshops.JustShops
api-version: '1.21'
depend: [JustPlugin]
description: Sign-based shops powered by JustPlugin's economy
```

### Main Class

```java
package me.justme.justshops;

import org.bukkit.plugin.java.JavaPlugin;
import org.justme.justPlugin.api.JustPluginAPI;
import org.justme.justPlugin.api.JustPluginProvider;
import org.justme.justPlugin.api.EconomyAPI;

public class JustShops extends JavaPlugin {

    private EconomyAPI economy;

    @Override
    public void onEnable() {
        // Get JustPlugin API
        JustPluginAPI api = JustPluginProvider.get();
        if (api == null) {
            getLogger().severe("JustPlugin is required but not found! Disabling JustShops...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.economy = api.getEconomyAPI();
        getLogger().info("JustShops loaded! Using JustPlugin economy.");

        // Register listeners, commands, etc.
        getServer().getPluginManager().registerEvents(new ShopSignListener(this), this);
    }

    public EconomyAPI getEconomy() {
        return economy;
    }
}
```

### Sign Interaction Listener

```java
package me.justme.justshops;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.justme.justPlugin.api.EconomyAPI;

import java.util.UUID;

public class ShopSignListener implements Listener {

    private final JustShops plugin;

    public ShopSignListener(JustShops plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign sign)) return;

        // Parse sign lines: [Shop], Item, Amount, Price
        // (your parsing logic here)
        String itemName = "Diamond";
        int amount = 1;
        double price = 50.0;
        UUID ownerUuid = UUID.fromString("..."); // stored in sign or nearby

        Player buyer = event.getPlayer();
        EconomyAPI economy = plugin.getEconomy();

        // Check balance
        if (!economy.hasBalance(buyer.getUniqueId(), price)) {
            buyer.sendMessage("§cYou need " + economy.format(price) + " to buy this!");
            return;
        }

        // Process purchase: pay the shop owner
        if (economy.pay(buyer.getUniqueId(), ownerUuid, price)) {
            // Give items to buyer
            // buyer.getInventory().addItem(...)
            buyer.sendMessage("§aPurchased " + amount + "x " + itemName
                + " for " + economy.format(price) + "!");
        } else {
            buyer.sendMessage("§cPurchase failed. Check your balance.");
        }
    }
}
```

---

## 📌 Important Notes & Best Practices

### 1. Always use `compileOnly`
JustPlugin's JAR must be added as `compileOnly` - do **not** shade/bundle it into your plugin. It's already on the server as a separate plugin.

### 2. Always null-check the API
```java
JustPluginAPI api = JustPluginProvider.get();
if (api == null) {
    // JustPlugin is not loaded - handle gracefully
}
```

### 3. Use `depend` in `plugin.yml`
This ensures JustPlugin loads before your plugin, so the API is available in your `onEnable()`.

### 4. Balance works for offline players
`getBalance()`, `setBalance()`, `addBalance()`, `removeBalance()`, and `pay()` all work for offline players. Data is loaded from disk if the player isn't cached in memory.

### 5. Punishments are persistent
All bans, mutes, and warnings are saved to disk (YAML files in JustPlugin's data folder). They persist across server restarts.

### 6. Currency formatting
Always use `economy.format(amount)` when displaying money to players. This ensures the correct currency symbol (configured by the server admin) is used.

### 7. `removeBalance()` is safe
It returns `false` and does **not** modify the balance if the player doesn't have enough. You don't need to call `hasBalance()` first if you're going to remove right after - just check the return value.

### 8. `pay()` respects pay-toggle
If the recipient has `/paytoggle` enabled (disabled receiving payments), `pay()` returns `false`. Your plugin should handle this case.

### 9. Warning punishments are automatic
When you call `addWarn()`, JustPlugin automatically looks up the punishment for the player's current active warning count (configured in `config.yml`) and applies it (chat message, kick, temp-ban, ban, mute, etc.). You don't need to handle punishment logic yourself.

### 10. Don't store JustPlugin references in static fields
The API is cleared when JustPlugin disables. Always use `JustPluginProvider.get()` for fresh access, or store it in an instance field that's set in `onEnable()`.

### 11. Internal listener architecture
JustPlugin uses a modular listener architecture. Event handling is split into 6 categorized sub-listeners (`connection`, `chat`, `combat`, `player`, `server`, `inventory`). The `PlayerListener` class is a shared state holder (god mode, death locations, persistence) - it does **not** handle events directly. If your plugin listens for events that JustPlugin also handles, be aware of event priority: most JustPlugin listeners use `EventPriority.HIGH` or `EventPriority.LOWEST`.

---

## 📁 API Class Reference

| Class / Interface | Location | Purpose |
|-------------------|----------|---------|
| `JustPluginProvider` | `org.justme.justPlugin.api` | Static accessor - call `.get()` to obtain the API |
| `JustPluginAPI` | `org.justme.justPlugin.api` | Main API interface - provides sub-APIs |
| `EconomyAPI` | `org.justme.justPlugin.api` | Balance management (get, set, add, remove, pay, format) |
| `PunishmentAPI` | `org.justme.justPlugin.api` | Bans, mutes, warnings (check, apply, lift) |
| `VanishAPI` | `org.justme.justPlugin.api` | Check vanish / super-vanish status |

---

## 🧪 Testing Your Integration

1. Build your add-on plugin JAR.
2. Place **both** `JustPlugin-1.0-SNAPSHOT.jar` and your add-on JAR in the server's `plugins/` folder.
3. Start the server - your plugin should load **after** JustPlugin.
4. If JustPlugin is missing, your plugin should log an error and disable itself (if using `depend`).

### Quick test commands (in-game):
```
/balance              → Check your balance (verify economy is working)
/addcash 1000         → Give yourself money to test purchases
/bal <player>         → Verify balance changes after transactions
```

