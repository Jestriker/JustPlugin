import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="mb-10">
      <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">{title}</h2>
      {children}
    </section>
  );
}

export default function TradingPage() {
  return (
    <div>
      <PageHeader
        title="Trading"
        description="A secure, Hypixel SkyBlock-style trading GUI that lets players exchange items and money with visual confirmation, countdown protection, and sound feedback."
      />

      {/* How It Works */}
      <Section id="how-it-works" title="How It Works">
        <div className="space-y-4">
          <div className="flex items-start gap-3">
            <span className="flex items-center justify-center w-7 h-7 rounded-full bg-[var(--accent)] text-white text-xs font-bold shrink-0">1</span>
            <div>
              <h3 className="font-semibold text-sm">Send a Request</h3>
              <p className="text-sm text-[var(--text-secondary)]">
                Use <code className="text-[var(--accent-hover)]">/trade &lt;player&gt;</code> to send a trade request.
                The other player has a configurable timeout to accept (default 60 seconds).
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <span className="flex items-center justify-center w-7 h-7 rounded-full bg-[var(--accent)] text-white text-xs font-bold shrink-0">2</span>
            <div>
              <h3 className="font-semibold text-sm">Place Items &amp; Set Balance</h3>
              <p className="text-sm text-[var(--text-secondary)]">
                Both players get dedicated item areas in the GUI. Click the balance sign to enter an amount
                using shorthand like <code className="text-[var(--accent-hover)]">500k</code> or <code className="text-[var(--accent-hover)]">1m</code>.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <span className="flex items-center justify-center w-7 h-7 rounded-full bg-[var(--accent)] text-white text-xs font-bold shrink-0">3</span>
            <div>
              <h3 className="font-semibold text-sm">Confirm &amp; Countdown</h3>
              <p className="text-sm text-[var(--text-secondary)]">
                Once both players confirm, a <strong>5-second countdown</strong> begins with visual feedback.
                Either player can cancel during this window. Sound effects play throughout for clear feedback.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <span className="flex items-center justify-center w-7 h-7 rounded-full bg-[var(--accent)] text-white text-xs font-bold shrink-0">4</span>
            <div>
              <h3 className="font-semibold text-sm">Trade Complete</h3>
              <p className="text-sm text-[var(--text-secondary)]">
                Items and balance are exchanged atomically. If anything goes wrong, the trade is rolled back.
              </p>
            </div>
          </div>
        </div>
      </Section>

      {/* Features */}
      <Section id="features" title="Key Features">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {[
            { title: "Hypixel SkyBlock-Style GUI", desc: "Familiar split-screen trading interface with dedicated areas for each player." },
            { title: "Balance Transfer", desc: "Transfer money via sign input with shorthand support (500k, 1m, 2.5m)." },
            { title: "5-Second Countdown", desc: "Visual countdown with sound effects gives both players time to review before finalizing." },
            { title: "Cancel Protection", desc: "Either player can cancel at any point during the trade, even during the countdown." },
            { title: "Sound Effects", desc: "Audio feedback for confirmations, countdown ticks, completion, and cancellation." },
            { title: "Ignore System Integration", desc: "Trade requests are blocked if the target player has ignored the sender." },
          ].map((feature) => (
            <div key={feature.title} className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4">
              <h3 className="font-semibold text-sm mb-1">{feature.title}</h3>
              <p className="text-sm text-[var(--text-secondary)]">{feature.desc}</p>
            </div>
          ))}
        </div>
      </Section>

      {/* Configuration */}
      <Section id="configuration" title="Configuration">
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`trade:
  request-timeout: 60  # seconds before a trade request expires`}
        />
      </Section>

      {/* Permissions */}
      <Section id="permissions" title="Permissions">
        <div className="overflow-x-auto">
          <table className="w-full text-sm border border-[var(--border)] rounded-lg overflow-hidden">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left">
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)]">Permission</th>
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)]">Default</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              <tr className="hover:bg-[var(--bg-hover)] transition-colors">
                <td className="px-4 py-3 font-mono text-xs text-[var(--accent-hover)]">justplugin.trade</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Send and accept trade requests</td>
                <td className="px-4 py-3 text-[var(--text-muted)]">Player</td>
              </tr>
            </tbody>
          </table>
        </div>
      </Section>
    </div>
  );
}
