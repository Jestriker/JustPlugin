import PageHeader from "@/components/PageHeader";

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="mb-10">
      <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">{title}</h2>
      {children}
    </section>
  );
}

export default function SkinsPage() {
  return (
    <div>
      <PageHeader
        title="Skin Restorer"
        description="Allow players to set custom skins using Mojang usernames, with automatic skin application on join and support for cracked players."
      />

      {/* Opt-In Notice */}
      <div className="bg-[var(--bg-tertiary)] border border-[var(--border)] rounded-lg p-4 mb-8 text-sm text-[var(--text-secondary)]">
        <strong className="text-[var(--yellow)]">Opt-In Feature</strong> &mdash; The skin restorer system is{" "}
        <strong>disabled by default</strong>. Enable it in your configuration before use.
      </div>

      {/* Commands */}
      <Section id="commands" title="Commands">
        <div className="overflow-x-auto">
          <table className="w-full text-sm border border-[var(--border)] rounded-lg overflow-hidden">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left">
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)]">Command</th>
                <th className="px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/skin set <name>", desc: "Set your skin to a Mojang username" },
                { cmd: "/skin set <name> <player>", desc: "Set another player's skin (requires skin.others)" },
                { cmd: "/skin clear", desc: "Remove your custom skin and revert to default" },
                { cmd: "/skinban <player>", desc: "Ban a player from using the skin system" },
                { cmd: "/skinunban <player>", desc: "Unban a player from the skin system" },
              ].map((row) => (
                <tr key={row.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-[var(--accent-hover)] whitespace-nowrap">{row.cmd}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{row.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Section>

      {/* Features */}
      <Section id="features" title="Key Features">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {[
            { title: "Mojang API Integration", desc: "Fetches skin data directly from Mojang's API using the provided username." },
            { title: "Auto-Apply on Join", desc: "Stored skins are automatically applied when a player joins the server." },
            { title: "Cracked Player Support", desc: "Works for cracked (offline mode) players who would otherwise have default skins." },
            { title: "Skin Banning", desc: "Administrators can ban specific players from using the skin system entirely." },
          ].map((feature) => (
            <div key={feature.title} className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4">
              <h3 className="font-semibold text-sm mb-1">{feature.title}</h3>
              <p className="text-sm text-[var(--text-secondary)]">{feature.desc}</p>
            </div>
          ))}
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
                { perm: "justplugin.skin", desc: "Set your own skin", def: "Player" },
                { perm: "justplugin.skin.others", desc: "Set another player's skin", def: "Admin" },
                { perm: "justplugin.skin.bypassban", desc: "Bypass skin bans", def: "Admin" },
                { perm: "justplugin.skinban", desc: "Ban a player from using skins", def: "Admin" },
                { perm: "justplugin.skinunban", desc: "Unban a player from using skins", def: "Admin" },
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
