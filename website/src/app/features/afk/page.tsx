import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const configExample = `afk:
  idle-time: 300
  kick-after: 0
  broadcast: true
  tab-prefix: "[AFK] "`;

const features = [
  { title: "Toggle Command", desc: "Players can manually toggle AFK status with /afk." },
  { title: "Auto-AFK", desc: "Players are automatically marked AFK after a configurable idle time (default: 5 minutes)." },
  { title: "Idle Kick", desc: "Optionally kick AFK players after a set duration. Set to 0 to disable." },
  { title: "Tab List Prefix", desc: "AFK players get a configurable prefix in the tab list (default: [AFK])." },
  { title: "Broadcast", desc: "AFK status changes are broadcast to all players when enabled." },
  { title: "Auto-Clear", desc: "AFK status is automatically cleared on movement, chat messages, or block interaction." },
];

export default function AfkPage() {
  return (
    <>
      <PageHeader
        title="AFK System"
        description="Automatic idle detection with configurable timeouts, tab list integration, broadcasts, and optional idle kick."
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The AFK system detects idle players and marks them as away. It integrates with the tab list,
          broadcasts status changes, and can optionally kick players who remain idle for too long.
        </p>
      </section>

      {/* Features */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Features</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          {features.map((f) => (
            <div key={f.title} className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-4">
              <h3 className="font-semibold text-sm mb-1">{f.title}</h3>
              <p className="text-sm text-[var(--text-secondary)]">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Commands */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Commands</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Command</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">/afk</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Toggle your AFK status</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* Config */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <CodeBlock code={configExample} language="yaml" filename="config.yml" />
        <div className="mt-3 space-y-2">
          <div className="p-3 rounded-lg border border-[var(--border)] bg-[var(--bg-card)]">
            <p className="text-sm text-[var(--text-muted)]">
              <strong className="text-[var(--text-secondary)]">idle-time:</strong> Seconds before a player is marked AFK (default: 300 = 5 minutes)
            </p>
          </div>
          <div className="p-3 rounded-lg border border-[var(--border)] bg-[var(--bg-card)]">
            <p className="text-sm text-[var(--text-muted)]">
              <strong className="text-[var(--text-secondary)]">kick-after:</strong> Seconds after AFK before kicking. Set to 0 to disable idle kick.
            </p>
          </div>
        </div>
      </section>

      {/* Permissions */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Permissions</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Permission</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-[var(--border)]">
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.afk</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Toggle AFK status</td>
              </tr>
              <tr>
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.afk.kickbypass</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Bypass the idle kick timer</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
