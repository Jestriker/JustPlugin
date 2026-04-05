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

export default function KitsPage() {
  return (
    <div>
      <PageHeader
        title="Kit System"
        description="Create, manage, and distribute item kits with a full lifecycle, GUI previews, per-kit permissions, cooldowns, and auto-equip armor support."
      />

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
                { cmd: "/kit [name]", desc: "Claim a kit (opens GUI selector if no name given)" },
                { cmd: "/kitpreview <name>", desc: "Preview the contents of a kit in a GUI" },
                { cmd: "/kitcreate <name>", desc: "Create a new kit from your current inventory" },
                { cmd: "/kitedit <name>", desc: "Edit an existing kit's contents" },
                { cmd: "/kitrename <old> <new>", desc: "Rename an existing kit" },
                { cmd: "/kitdelete <name>", desc: "Permanently delete a kit" },
                { cmd: "/kitpublish <name>", desc: "Publish a pending kit so players can claim it" },
                { cmd: "/kitdisable <name>", desc: "Disable a published kit (players cannot claim it)" },
                { cmd: "/kitenable <name>", desc: "Re-enable a disabled kit" },
                { cmd: "/kitarchive <name>", desc: "Archive a kit (retained for a configurable period)" },
                { cmd: "/kitlist", desc: "List all kits and their statuses" },
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

      {/* Lifecycle */}
      <Section id="lifecycle" title="Kit Lifecycle">
        <p className="text-[var(--text-secondary)] mb-6">
          Every kit follows a defined lifecycle. Only <strong>Published</strong> kits can be claimed by players.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-3 mb-6">
          {[
            { label: "Pending", color: "var(--yellow)" },
            { label: "Published", color: "var(--green)" },
            { label: "Disabled", color: "var(--text-muted)" },
            { label: "Archived", color: "var(--red)" },
          ].map((stage, i, arr) => (
            <div key={stage.label} className="flex items-center gap-3">
              <div
                className="px-4 py-2 rounded-lg border text-sm font-semibold text-center"
                style={{ borderColor: stage.color, color: stage.color, backgroundColor: "var(--bg-card)" }}
              >
                {stage.label}
              </div>
              {i < arr.length - 1 && (
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" className="text-[var(--text-muted)] shrink-0">
                  <path d="M7 4L13 10L7 16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              )}
            </div>
          ))}
        </div>
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-sm text-[var(--text-secondary)] space-y-2">
          <p><strong style={{ color: "var(--yellow)" }}>Pending</strong> &mdash; Kit is created but not yet available to players. Use <code className="text-[var(--accent-hover)]">/kitpublish</code> to make it claimable.</p>
          <p><strong style={{ color: "var(--green)" }}>Published</strong> &mdash; Kit is live and claimable by players with the correct permission.</p>
          <p><strong className="text-[var(--text-muted)]">Disabled</strong> &mdash; Kit is temporarily unavailable. Re-enable with <code className="text-[var(--accent-hover)]">/kitenable</code>.</p>
          <p><strong style={{ color: "var(--red)" }}>Archived</strong> &mdash; Kit is retired and will be automatically deleted after the retention period.</p>
        </div>
      </Section>

      {/* Features */}
      <Section id="features" title="Key Features">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {[
            { title: "GUI Selection & Preview", desc: "Players can browse and preview kit contents in an interactive GUI before claiming." },
            { title: "Per-Kit Permissions", desc: "Each kit has its own permission node: justplugin.kits.<name>. Assign access granularly." },
            { title: "Per-Kit Cooldowns", desc: "Set individual cooldowns per kit to control how often players can claim them." },
            { title: "Auto-Equip Armor", desc: "Armor pieces in a kit are automatically equipped to the correct slots when claimed." },
            { title: "Archive Retention", desc: "Archived kits are retained for a configurable number of days (default 30) before permanent deletion." },
            { title: "Inventory Snapshot", desc: "Kits are created from your current inventory, making setup fast and intuitive." },
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
          code={`kits:
  archive-retention: 30  # days before archived kits are deleted`}
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
              {[
                { perm: "justplugin.kit", desc: "Claim a kit", def: "Player" },
                { perm: "justplugin.kits.<name>", desc: "Permission to claim a specific kit", def: "Player" },
                { perm: "justplugin.kitpreview", desc: "Preview a kit's contents", def: "Player" },
                { perm: "justplugin.kitcreate", desc: "Create a new kit", def: "Admin" },
                { perm: "justplugin.kitedit", desc: "Edit an existing kit", def: "Admin" },
                { perm: "justplugin.kitrename", desc: "Rename a kit", def: "Admin" },
                { perm: "justplugin.kitdelete", desc: "Delete a kit permanently", def: "Admin" },
                { perm: "justplugin.kitpublish", desc: "Publish a pending kit", def: "Admin" },
                { perm: "justplugin.kitdisable", desc: "Disable a published kit", def: "Admin" },
                { perm: "justplugin.kitenable", desc: "Re-enable a disabled kit", def: "Admin" },
                { perm: "justplugin.kitarchive", desc: "Archive a kit", def: "Admin" },
                { perm: "justplugin.kitlist", desc: "List all kits", def: "Admin" },
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
