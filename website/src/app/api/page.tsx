import type { Metadata } from "next";
import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

export const metadata: Metadata = {
  title: "Developer API Reference",
  description:
    "JustPlugin developer API documentation. Integrate with EconomyAPI, PunishmentAPI, VanishAPI, and more in your Minecraft plugins.",
};

const economyMethods = [
  { signature: "double getBalance(UUID player)", description: "Returns the player's current balance." },
  { signature: "void setBalance(UUID player, double amount)", description: "Sets the player's balance to an exact amount." },
  { signature: "void addBalance(UUID player, double amount)", description: "Adds the specified amount to the player's balance." },
  { signature: "boolean removeBalance(UUID player, double amount)", description: "Removes the specified amount. Returns false if the player has insufficient funds." },
  { signature: "boolean pay(UUID from, UUID to, double amount)", description: "Transfers funds between two players. Returns false if the sender has insufficient funds." },
  { signature: "String format(double amount)", description: "Formats a balance value using the server's configured currency symbol and format." },
  { signature: "boolean hasBalance(UUID player, double amount)", description: "Checks whether the player has at least the specified amount." },
];

const punishmentMethods = [
  { signature: "boolean isBanned(UUID player)", description: "Checks if a player is currently banned." },
  { signature: "void ban(UUID player, String name, String reason, String bannedBy)", description: "Permanently bans a player." },
  { signature: "void tempBan(UUID player, String name, String reason, String bannedBy, long durationMs)", description: "Temporarily bans a player for the specified duration in milliseconds." },
  { signature: "boolean unban(UUID player)", description: "Unbans a player. Returns false if the player was not banned." },
  { signature: "boolean isMuted(UUID player)", description: "Checks if a player is currently muted." },
  { signature: "void mute(UUID player, String name, String reason, String mutedBy)", description: "Permanently mutes a player." },
  { signature: "void tempMute(UUID player, String name, String reason, String mutedBy, long durationMs)", description: "Temporarily mutes a player for the specified duration in milliseconds." },
  { signature: "boolean unmute(UUID player)", description: "Unmutes a player. Returns false if the player was not muted." },
  { signature: "String getMuteReason(UUID player)", description: "Returns the reason for a player's mute, or null if not muted." },
  { signature: "int getActiveWarnCount(UUID player)", description: "Returns the number of active (non-lifted) warnings for a player." },
  { signature: "int getTotalWarnCount(UUID player)", description: "Returns the total number of warnings ever issued to a player." },
  { signature: "void addWarn(UUID player, String name, String reason, String warnedBy)", description: "Issues a warning to a player." },
  { signature: "boolean liftWarn(UUID player, int index, String liftedBy, String reason)", description: "Lifts a specific warning by index. Returns false if the index is invalid." },
];

const vanishMethods = [
  { signature: "boolean isVanished(UUID player)", description: "Checks if a player is in standard vanish mode (invisible to non-staff)." },
  { signature: "boolean isSuperVanished(UUID player)", description: "Checks if a player is in super vanish mode (spectator-like ghost mode, fully invisible)." },
];

function MethodTable({ methods }: { methods: { signature: string; description: string }[] }) {
  return (
    <div className="overflow-x-auto my-4">
      <table className="w-full">
        <thead>
          <tr>
            <th className="text-left">Method</th>
            <th className="text-left">Description</th>
          </tr>
        </thead>
        <tbody>
          {methods.map((m) => (
            <tr key={m.signature}>
              <td className="font-mono text-xs whitespace-nowrap">{m.signature}</td>
              <td className="text-sm">{m.description}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default function APIReferencePage() {
  return (
    <div>
      <PageHeader
        title="API Reference"
        description={<>Integrate your plugins with <span className="text-[var(--accent)]">JustPlugin</span> using the public developer API. Access economy, punishment, and vanish systems programmatically.</>}
        badge="Developer"
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          JustPlugin exposes a public API that allows add-on plugins to interact with its core systems.
          The API is accessed through a central provider and gives you type-safe access to three modules:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {[
            { name: "EconomyAPI", desc: "Query and modify player balances, process payments, format currency." },
            { name: "PunishmentAPI", desc: "Ban, mute, warn players and query punishment status programmatically." },
            { name: "VanishAPI", desc: "Check whether players are vanished or super-vanished." },
          ].map((api) => (
            <div key={api.name} className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4">
              <div className="font-semibold text-sm font-mono text-[var(--accent-hover)] mb-1">{api.name}</div>
              <p className="text-xs text-[var(--text-muted)] leading-relaxed">{api.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Setup */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Setup</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          To use the JustPlugin API, add the plugin JAR as a compile-only dependency in your project
          and declare JustPlugin as a dependency in your <code>plugin.yml</code>.
        </p>

        <h3 className="text-lg font-semibold mb-2">Gradle (build.gradle)</h3>
        <CodeBlock
          language="groovy"
          filename="build.gradle"
          code={`dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly files('libs/JustPlugin-1.0-SNAPSHOT.jar')
}`}
        />

        <h3 className="text-lg font-semibold mt-6 mb-2">Plugin Dependency (plugin.yml)</h3>
        <CodeBlock
          language="yaml"
          filename="plugin.yml"
          code={`name: MyAddonPlugin
version: 1.0
main: com.example.myaddon.MyAddonPlugin
api-version: '1.21'
depend: [JustPlugin]`}
        />
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 mt-4">
          <p className="text-sm text-[var(--text-secondary)]">
            <strong className="text-[var(--text-primary)]">Note:</strong> Using <code>depend</code> ensures
            JustPlugin loads before your plugin. If your plugin can function without JustPlugin,
            use <code>softdepend</code> instead and check for the API at runtime.
          </p>
        </div>
      </section>

      {/* Accessing the API */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Accessing the API</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          All API access goes through <code>JustPluginProvider</code>. Call <code>JustPluginProvider.get()</code> to
          obtain the root API instance, then retrieve the sub-API you need.
        </p>
        <CodeBlock
          language="java"
          filename="MyAddonPlugin.java"
          code={`import org.justme.justPlugin.api.JustPluginAPI;
import org.justme.justPlugin.api.JustPluginProvider;
import org.justme.justPlugin.api.economy.EconomyAPI;
import org.justme.justPlugin.api.punishment.PunishmentAPI;
import org.justme.justPlugin.api.vanish.VanishAPI;

public class MyAddonPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        JustPluginAPI api = JustPluginProvider.get();

        EconomyAPI economy = api.getEconomyAPI();
        PunishmentAPI punishments = api.getPunishmentAPI();
        VanishAPI vanish = api.getVanishAPI();

        getLogger().info("JustPlugin API loaded successfully!");
    }
}`}
        />
      </section>

      {/* Economy API */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Economy API</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The Economy API provides full access to JustPlugin&apos;s balance system. All operations
          are thread-safe and respect the server&apos;s configured economy settings.
        </p>

        <h3 className="text-lg font-semibold mb-2">Methods</h3>
        <MethodTable methods={economyMethods} />

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Taking Payment</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Charge a player for a service and handle insufficient funds gracefully.
        </p>
        <CodeBlock
          language="java"
          code={`EconomyAPI economy = JustPluginProvider.get().getEconomyAPI();

double price = 500.0;
UUID buyer = player.getUniqueId();

if (!economy.hasBalance(buyer, price)) {
    player.sendMessage("You need " + economy.format(price) + " but only have "
        + economy.format(economy.getBalance(buyer)) + ".");
    return;
}

economy.removeBalance(buyer, price);
player.sendMessage("Purchased! " + economy.format(price) + " has been deducted.");`}
        />

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Paying a Shop Owner</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Transfer funds from a buyer to a shop owner atomically.
        </p>
        <CodeBlock
          language="java"
          code={`EconomyAPI economy = JustPluginProvider.get().getEconomyAPI();

UUID buyer = player.getUniqueId();
UUID shopOwner = getShopOwner(signLocation);
double price = getSignPrice(signLocation);

boolean success = economy.pay(buyer, shopOwner, price);

if (success) {
    player.sendMessage("Payment of " + economy.format(price) + " sent!");
} else {
    player.sendMessage("You don't have enough funds.");
}`}
        />

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Giving Rewards</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Reward a player for completing a task, such as a quest or vote.
        </p>
        <CodeBlock
          language="java"
          code={`EconomyAPI economy = JustPluginProvider.get().getEconomyAPI();

UUID playerId = player.getUniqueId();
double reward = 1000.0;

economy.addBalance(playerId, reward);
player.sendMessage("You earned " + economy.format(reward) + " for voting! "
    + "New balance: " + economy.format(economy.getBalance(playerId)));`}
        />
      </section>

      {/* Punishment API */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Punishment API</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The Punishment API lets you manage bans, mutes, and warnings programmatically.
          All punishments issued through the API are logged and appear in the in-game punishment history.
        </p>

        <h3 className="text-lg font-semibold mb-2">Methods</h3>
        <MethodTable methods={punishmentMethods} />

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Checking Punishment Status</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Gate access to features based on a player&apos;s punishment record.
        </p>
        <CodeBlock
          language="java"
          code={`PunishmentAPI punishments = JustPluginProvider.get().getPunishmentAPI();

UUID playerId = player.getUniqueId();

if (punishments.isMuted(playerId)) {
    String reason = punishments.getMuteReason(playerId);
    player.sendMessage("You are muted and cannot use chat features.");
    player.sendMessage("Reason: " + reason);
    return;
}

int activeWarns = punishments.getActiveWarnCount(playerId);
if (activeWarns >= 3) {
    // Auto-temp-ban for excessive warnings
    punishments.tempBan(
        playerId,
        player.getName(),
        "Automatic ban: " + activeWarns + " active warnings",
        "System",
        24 * 60 * 60 * 1000L // 24 hours in ms
    );
    player.kick(Component.text("You have been temporarily banned for excessive warnings."));
}`}
        />
      </section>

      {/* Vanish API */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Vanish API</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The Vanish API allows you to check a player&apos;s vanish state. This is useful for plugins
          that display player lists, leaderboards, or interact with visibility logic.
        </p>

        <h3 className="text-lg font-semibold mb-2">Methods</h3>
        <MethodTable methods={vanishMethods} />

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Hiding Vanished Players</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Filter vanished players from a custom player list or leaderboard.
        </p>
        <CodeBlock
          language="java"
          code={`VanishAPI vanish = JustPluginProvider.get().getVanishAPI();

List<Player> visiblePlayers = Bukkit.getOnlinePlayers().stream()
    .filter(p -> !vanish.isVanished(p.getUniqueId()))
    .toList();

// Use visiblePlayers for your scoreboard, tab list, or GUI
player.sendMessage("Online players: " + visiblePlayers.size());

for (Player visible : visiblePlayers) {
    player.sendMessage(" - " + visible.getName());
}`}
        />
      </section>

      {/* Custom Events */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Custom Events</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          JustPlugin fires 10 custom Bukkit events that add-on plugins can listen for. Cancellable events
          can be cancelled to prevent the action. All events use defensive copies &mdash; no direct database
          access is exposed.
        </p>

        <div className="overflow-x-auto my-4">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left">Event</th>
                <th className="text-left">Cancellable</th>
                <th className="text-left">Description</th>
              </tr>
            </thead>
            <tbody>
              {[
                { event: "PlayerBalanceChangeEvent", cancel: "Yes", desc: "Fired when a player's balance changes" },
                { event: "PlayerPunishEvent", cancel: "Yes", desc: "Fired when a player is punished (ban, mute, warn, etc.)" },
                { event: "PlayerTeleportRequestEvent", cancel: "Yes", desc: "Fired when a TPA/TPAHere request is sent" },
                { event: "PlayerTradeEvent", cancel: "Yes", desc: "Fired when a trade completes between players" },
                { event: "PlayerJailEvent", cancel: "Yes", desc: "Fired when a player is jailed" },
                { event: "PlayerUnjailEvent", cancel: "No", desc: "Fired when a player is released from jail" },
                { event: "PlayerAfkEvent", cancel: "No", desc: "Fired when a player enters or leaves AFK" },
                { event: "KitClaimEvent", cancel: "Yes", desc: "Fired when a player claims a kit" },
                { event: "WarpCreateEvent", cancel: "No", desc: "Fired when a warp is created" },
                { event: "WarpDeleteEvent", cancel: "No", desc: "Fired when a warp is deleted" },
              ].map((e) => (
                <tr key={e.event}>
                  <td className="font-mono text-xs whitespace-nowrap">{e.event}</td>
                  <td className="text-sm">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                      e.cancel === "Yes"
                        ? "bg-[var(--green)]/15 text-[var(--green)]"
                        : "bg-[var(--bg-tertiary)] text-[var(--text-muted)]"
                    }`}>
                      {e.cancel}
                    </span>
                  </td>
                  <td className="text-sm">{e.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Listening for Balance Changes</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Cancel a balance change if it would make a player exceed a custom cap.
        </p>
        <CodeBlock
          language="java"
          code={`import org.justme.justPlugin.api.events.PlayerBalanceChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BalanceCapListener implements Listener {

    private static final double MAX_ALLOWED = 500_000.0;

    @EventHandler
    public void onBalanceChange(PlayerBalanceChangeEvent event) {
        if (event.getNewBalance() > MAX_ALLOWED) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Balance cap reached!");
        }
    }
}`}
        />

        <h3 className="text-lg font-semibold mt-6 mb-2">Example: Logging Punishments</h3>
        <p className="text-sm text-[var(--text-secondary)] mb-2">
          Log all punishments to an external system.
        </p>
        <CodeBlock
          language="java"
          code={`import org.justme.justPlugin.api.events.PlayerPunishEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PunishLogListener implements Listener {

    @EventHandler
    public void onPunish(PlayerPunishEvent event) {
        String log = String.format("[%s] %s punished %s: %s",
            event.getType(),
            event.getPunisher(),
            event.getTarget().getName(),
            event.getReason());
        // Send to your external logging service
        getLogger().info(log);
    }
}`}
        />
      </section>

      {/* Full Example */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Full Example: Sign Shop Plugin</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          A complete example showing how to build a simple sign-based shop that uses the Economy API
          to charge players when they right-click a shop sign.
        </p>
        <CodeBlock
          language="java"
          filename="SignShopListener.java"
          code={`import org.justme.justPlugin.api.JustPluginProvider;
import org.justme.justPlugin.api.economy.EconomyAPI;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.UUID;

public class SignShopListener implements Listener {

    private final EconomyAPI economy;

    public SignShopListener() {
        this.economy = JustPluginProvider.get().getEconomyAPI();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        // Check if the sign is a shop sign (first line is "[Shop]")
        String firstLine = sign.getSide(Side.FRONT).line(0).toString();
        if (!firstLine.contains("[Shop]")) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Parse item and price from the sign
        String itemName = sign.getSide(Side.FRONT).line(1).toString();
        double price;
        try {
            price = Double.parseDouble(sign.getSide(Side.FRONT).line(2).toString());
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid shop sign configuration.");
            return;
        }

        Material material = Material.matchMaterial(itemName);
        if (material == null) {
            player.sendMessage("Unknown item: " + itemName);
            return;
        }

        // Check and process payment
        if (!economy.hasBalance(playerId, price)) {
            player.sendMessage("You need " + economy.format(price)
                + " to buy this item. Balance: " + economy.format(economy.getBalance(playerId)));
            return;
        }

        boolean removed = economy.removeBalance(playerId, price);
        if (!removed) {
            player.sendMessage("Transaction failed.");
            return;
        }

        // Give the item
        ItemStack item = new ItemStack(material, 1);
        player.getInventory().addItem(item);
        player.sendMessage("Purchased " + material.name() + " for "
            + economy.format(price) + "! Remaining balance: "
            + economy.format(economy.getBalance(playerId)));
    }
}`}
        />
      </section>

      {/* Best Practices */}
      <section className="mb-10">
        <h2 className="text-2xl font-bold mb-3">Best Practices</h2>
        <div className="space-y-3">
          {[
            {
              title: "Cache the API reference",
              desc: "Call JustPluginProvider.get() once during onEnable() and store the result. Avoid calling it repeatedly in hot paths like event listeners.",
            },
            {
              title: "Always check hasBalance before removeBalance",
              desc: "While removeBalance returns false on insufficient funds, checking first lets you provide a better error message to the player with the exact shortfall.",
            },
            {
              title: "Use depend, not softdepend, if your plugin requires JustPlugin",
              desc: "This guarantees JustPlugin is loaded and the API is available when your plugin initializes. If you use softdepend, you must null-check the API at runtime.",
            },
            {
              title: "Handle null returns from punishment queries",
              desc: "Methods like getMuteReason() return null when the player has no active punishment. Always null-check before using the result.",
            },
            {
              title: "Use the format() method for display",
              desc: "Never hardcode currency symbols. Use economy.format() so your output matches the server's configured currency format and symbol.",
            },
            {
              title: "Don't call the API on the main thread for bulk operations",
              desc: "For operations that iterate over many players (e.g., resetting all balances), consider running the work asynchronously using the Bukkit scheduler to avoid freezing the server.",
            },
            {
              title: "Provide meaningful punishment reasons",
              desc: "When issuing bans, mutes, or warnings through the API, always include a descriptive reason. These are shown to players and stored in the punishment history.",
            },
          ].map((tip) => (
            <div
              key={tip.title}
              className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4"
            >
              <h3 className="font-semibold text-sm mb-1">{tip.title}</h3>
              <p className="text-sm text-[var(--text-muted)] leading-relaxed">{tip.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
