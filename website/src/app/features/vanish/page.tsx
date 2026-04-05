import PageHeader from "@/components/PageHeader";

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="mb-10">
      <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">{title}</h2>
      {children}
    </section>
  );
}

export default function VanishPage() {
  return (
    <div>
      <PageHeader
        title="Vanish"
        description="Become invisible to other players with two distinct vanish modes: standard invisibility and super vanish with full spectator-like ghost mode."
      />

      {/* Standard Vanish */}
      <Section id="standard-vanish" title="Standard Vanish">
        <p className="text-[var(--text-secondary)] mb-4">
          Toggle standard vanish with <code className="text-[var(--accent-hover)]">/vanish</code>.
          While vanished, you are:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
          {[
            "Completely invisible to other players",
            "Hidden from the tab list",
            "Hidden from the player count",
            "Hidden from command autocomplete",
            "A fake quit message is sent on vanish",
            "A fake join message is sent on unvanish",
          ].map((item) => (
            <div key={item} className="flex items-start gap-2 bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-3">
              <span className="text-[var(--green)] shrink-0 mt-0.5">&#10003;</span>
              <span className="text-sm text-[var(--text-secondary)]">{item}</span>
            </div>
          ))}
        </div>
      </Section>

      {/* Super Vanish */}
      <Section id="super-vanish" title="Super Vanish">
        <p className="text-[var(--text-secondary)] mb-4">
          Toggle super vanish with <code className="text-[var(--accent-hover)]">/supervanish</code>.
          This puts you in a spectator-like ghost mode with all standard vanish features plus additional restrictions:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
          {[
            "Cannot pick up items",
            "Cannot drop items",
            "Cannot break blocks",
            "Cannot place blocks",
            "Cannot trigger redstone",
            "Cannot trigger pressure plates",
            "Cannot trigger sculk sensors",
            "Cannot open chests",
          ].map((item) => (
            <div key={item} className="flex items-start gap-2 bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-3">
              <span className="text-[var(--red)] shrink-0 mt-0.5">&#10007;</span>
              <span className="text-sm text-[var(--text-secondary)]">{item}</span>
            </div>
          ))}
        </div>
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-sm text-[var(--text-secondary)]">
          <strong>Note:</strong> Super vanish uses spectator mode internally. Your previous gamemode is
          saved and restored automatically when you unvanish.
        </div>
      </Section>

      {/* Persistence */}
      <Section id="persistence" title="Persistence">
        <p className="text-[var(--text-secondary)]">
          Both vanish modes <strong>persist across server restarts</strong>. If a vanished player disconnects
          and reconnects, they will remain vanished. The previous gamemode is always restored correctly on unvanish.
        </p>
      </Section>

      {/* Comparison Table */}
      <Section id="comparison" title="Comparison">
        <div className="overflow-x-auto">
          <table className="w-full text-sm border border-[var(--border)] rounded-lg overflow-hidden">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left">
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)]">Feature</th>
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)] text-center">Standard Vanish</th>
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)] text-center">Super Vanish</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { feature: "Invisible to players", standard: true, super: true },
                { feature: "Hidden from tab list", standard: true, super: true },
                { feature: "Hidden from player count", standard: true, super: true },
                { feature: "Hidden from autocomplete", standard: true, super: true },
                { feature: "Fake join/quit messages", standard: true, super: true },
                { feature: "Persists across restarts", standard: true, super: true },
                { feature: "Cannot pick up / drop items", standard: false, super: true },
                { feature: "Cannot break / place blocks", standard: false, super: true },
                { feature: "Cannot trigger redstone", standard: false, super: true },
                { feature: "Cannot trigger pressure plates", standard: false, super: true },
                { feature: "Cannot trigger sculk sensors", standard: false, super: true },
                { feature: "Cannot open chests", standard: false, super: true },
              ].map((row) => (
                <tr key={row.feature} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{row.feature}</td>
                  <td className="px-4 py-3 text-center">
                    <span style={{ color: row.standard ? "var(--green)" : "var(--text-muted)" }}>
                      {row.standard ? "\u2713" : "\u2014"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span style={{ color: row.super ? "var(--green)" : "var(--text-muted)" }}>
                      {row.super ? "\u2713" : "\u2014"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
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
              {[
                { perm: "justplugin.vanish", desc: "Toggle vanish on yourself", def: "Admin" },
                { perm: "justplugin.vanish.others", desc: "Toggle vanish on other players", def: "Admin" },
                { perm: "justplugin.vanish.see", desc: "See vanished players", def: "Admin" },
                { perm: "justplugin.supervanish", desc: "Toggle super vanish on yourself", def: "Admin" },
                { perm: "justplugin.supervanish.others", desc: "Toggle super vanish on other players", def: "Admin" },
              ].map((row) => (
                <tr key={row.perm} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-[var(--accent-hover)]">{row.perm}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{row.desc}</td>
                  <td className="px-4 py-3 text-[var(--text-muted)]">{row.def}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Section>
    </div>
  );
}
