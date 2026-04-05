import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

export default function EconomyPage() {
  return (
    <>
      <PageHeader
        title="Economy"
        description="A full-featured economy system with balance management, player-to-player payments, PayNotes, an interactive Baltop GUI, and first-class Vault integration."
      />

      {/* ------------------------------------------------------------------ */}
      {/*  Overview                                                           */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          JustPlugin ships with its own economy provider out of the box. Every player starts with a
          configurable balance and can send money, toggle payment acceptance, and attach PayNotes to
          transactions. Server administrators can top up balances, hide players from the leaderboard,
          and switch to Vault as the backing provider if they already run another economy plugin.
        </p>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Commands                                                           */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-4">Commands</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/balance", usage: "/balance [player]", desc: "Check your own or another player's balance" },
                { cmd: "/pay", usage: "/pay <player> <amount>", desc: "Send money to another player" },
                { cmd: "/paytoggle", usage: "/paytoggle", desc: "Toggle whether you accept payments" },
                { cmd: "/paynote", usage: "/pay <player> <amount> <note>", desc: "Send a payment with an attached note" },
                { cmd: "/addcash", usage: "/addcash <player> <amount>", desc: "Add money to a player's balance" },
                { cmd: "/baltop", usage: "/baltop", desc: "Open the interactive Baltop leaderboard GUI" },
                { cmd: "/baltophide", usage: "/baltophide", desc: "Hide or unhide yourself from the Baltop leaderboard" },
                { cmd: "/transactions", usage: "/transactions [player]", desc: "View transaction history in a paginated GUI" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">{r.usage}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Permissions                                                        */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-4">Permissions</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Permission</th>
                <th className="px-4 py-3">Description</th>
                <th className="px-4 py-3">Default</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { perm: "justplugin.balance", desc: "Check your own balance", def: "Player" },
                { perm: "justplugin.balance.others", desc: "Check another player's balance", def: "Admin" },
                { perm: "justplugin.pay", desc: "Send money to another player", def: "Player" },
                { perm: "justplugin.paytoggle", desc: "Toggle receiving payments", def: "Player" },
                { perm: "justplugin.paynote", desc: "Send a note with a payment", def: "Player" },
                { perm: "justplugin.addcash", desc: "Add cash to your own balance", def: "Admin" },
                { perm: "justplugin.addcash.others", desc: "Add cash to another player's balance", def: "Admin" },
                { perm: "justplugin.baltop", desc: "Open the Baltop leaderboard GUI", def: "Player" },
                { perm: "justplugin.baltophide", desc: "Hide yourself from the Baltop leaderboard", def: "Admin" },
                { perm: "justplugin.transactions", desc: "View your own transaction history", def: "Player" },
                { perm: "justplugin.transactions.others", desc: "View another player's transaction history", def: "Admin" },
              ].map((r) => (
                <tr key={r.perm} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--accent)]">{r.perm}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                  <td className="px-4 py-2.5">
                    <span
                      className={`px-2 py-0.5 rounded text-xs font-medium ${
                        r.def === "Player"
                          ? "bg-[var(--green)]/15 text-[var(--green)]"
                          : "bg-[var(--yellow)]/15 text-[var(--yellow)]"
                      }`}
                    >
                      {r.def}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Configuration                                                      */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <p className="text-[var(--text-secondary)] mb-4 leading-relaxed">
          The economy section in <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">config.yml</code> controls
          the provider, starting balance, currency symbol, maximum balance cap, and pay cooldown.
        </p>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`economy:
  provider: justplugin  # or "vault"
  starting-balance: 100.0
  currency-symbol: "$"
  max-balance: 1000000000.0
  pay-cooldown: 0`}
        />
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  PayNotes                                                           */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">PayNotes</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          PayNotes are physical items that represent a fixed amount of currency. Players can convert
          coins into a redeemable note item and trade it like any other in-game item. When redeemed,
          the value is deposited back into the holder&apos;s balance. This is useful for drop-party
          rewards, player shops, or any scenario where currency needs to be represented as an item.
        </p>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`economy:
  paynotes:
    enabled: true
    material: PAPER
    custom-model-data: 0`}
        />
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Baltop GUI                                                         */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Baltop GUI</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-2">
          Running <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/baltop</code> opens an interactive
          in-game inventory GUI that displays the richest players on the server. Key features include:
        </p>
        <ul className="list-disc list-inside text-[var(--text-secondary)] space-y-1.5 ml-2 text-sm">
          <li>Gold, silver, and bronze medal indicators for the top 3 players</li>
          <li>Paginated navigation for servers with many players</li>
          <li>Players hidden via <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/baltophide</code> are excluded from the leaderboard</li>
          <li>Player head icons with balance and rank displayed in the lore</li>
        </ul>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Transaction History                                                */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Transaction History</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          JustPlugin tracks every economy transaction and provides a paginated GUI to browse them.
          Players can view their own history with{" "}
          <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/transactions</code>,
          and staff can inspect any player&apos;s history with{" "}
          <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/transactions &lt;player&gt;</code>.
          Clicking a transaction in the GUI opens a detail view with full metadata.
        </p>
        <ul className="list-disc list-inside text-[var(--text-secondary)] space-y-1.5 ml-2 text-sm mb-4">
          <li>Paginated GUI showing 28 transactions per page</li>
          <li>Tracks 6 transaction types: PAY, PAYNOTE_CREATE, PAYNOTE_REDEEM, ADDCASH, TRADE, and API</li>
          <li>Click any transaction for a detailed breakdown</li>
          <li>Configurable retention period and max entries per player</li>
        </ul>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`economy:
  transaction-history:
    enabled: true
    retention-days: 30       # Auto-prune after 30 days
    max-entries: 500         # Max entries stored per player
  addcash:
    show-to-player: true     # Notify player when cash is added
  paynote:
    show-creator: true       # Show who created the PayNote
    notify-creator: "name"   # "name", "anonymous", or "none"`}
        />
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Vault Integration                                                  */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Vault Integration</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          If your server already uses an economy plugin that registers with Vault, you can set{" "}
          <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">provider: vault</code> to delegate all
          balance operations to the existing Vault economy. JustPlugin&apos;s commands, GUI, and PayNotes
          will continue to work as normal, but the underlying balance storage will be handled by your
          Vault-compatible provider (e.g., EssentialsX Economy, CMI, etc.).
        </p>
      </section>
    </>
  );
}
