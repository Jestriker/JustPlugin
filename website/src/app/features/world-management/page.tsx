import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const timePresets = [
  { preset: "day", value: "1000", desc: "Early morning" },
  { preset: "noon", value: "6000", desc: "Midday" },
  { preset: "sunset", value: "12000", desc: "Evening" },
  { preset: "night", value: "13000", desc: "Nightfall" },
  { preset: "midnight", value: "18000", desc: "Middle of night" },
  { preset: "sunrise", value: "23000", desc: "Dawn" },
];

const entityConfig = `entity-clear:
  enabled: true
  interval: 900
  warning-time: 30
  clear-items: true
  clear-hostile: false
  clear-friendly: false`;

const commands = [
  { command: "/weather <clear|rain|thunder>", desc: "Set the weather in the current world", permission: "justplugin.weather" },
  { command: "/time set <value>", desc: "Set the world time (ticks or preset name)", permission: "justplugin.time" },
  { command: "/time add <value>", desc: "Add ticks to the current world time", permission: "justplugin.time" },
  { command: "/time query", desc: "Query the current world time", permission: "justplugin.time" },
  { command: "/freezetick", desc: "Freeze the game tick (pause time and weather)", permission: "justplugin.freezetick" },
  { command: "/unfreezetick", desc: "Unfreeze the game tick", permission: "justplugin.freezetick" },
  { command: "/clearentities", desc: "Manually clear entities in the current world", permission: "justplugin.clearentities" },
  { command: "/friendlyfire", desc: "Toggle friendly fire for all players", permission: "justplugin.friendlyfire" },
];

const permissions = [
  { perm: "justplugin.weather", desc: "Change world weather" },
  { perm: "justplugin.time", desc: "Set, add, or query world time" },
  { perm: "justplugin.freezetick", desc: "Freeze and unfreeze game tick" },
  { perm: "justplugin.clearentities", desc: "Manually clear entities" },
  { perm: "justplugin.clearentities.notify", desc: "Receive staff notifications for excessive entities" },
  { perm: "justplugin.friendlyfire", desc: "Toggle friendly fire" },
];

export default function WorldManagementPage() {
  return (
    <>
      <PageHeader
        title="World Management"
        description="Control weather, time, game tick freezing, entity clearing, and friendly fire from a single feature set."
      />

      {/* Weather */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Weather Control</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Set the weather in any world to clear, rain, or thunder with a single command.
        </p>
      </section>

      {/* Time */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Time Control</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Set, add, or query world time. Use tick values directly or convenient presets:
        </p>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Preset</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Ticks</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {timePresets.map((t) => (
                <tr key={t.preset} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--yellow)] text-xs">{t.preset}</td>
                  <td className="px-4 py-3 font-mono text-[var(--text-muted)] text-xs">{t.value}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{t.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Freeze Tick */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Freeze / Unfreeze Tick</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Freeze the game tick to pause time progression and weather changes. Useful for builds, events,
          or screenshots. Use <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/unfreezetick</code> to
          resume normal operation.
        </p>
      </section>

      {/* Entity Clear */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Entity Clear System</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          A built-in ClearLag replacement that automatically removes ground items and optionally mobs at a
          configurable interval. Key features:
        </p>
        <div className="grid gap-3 sm:grid-cols-2 mb-4">
          {[
            { title: "Auto-Clear Interval", desc: "Default every 15 minutes (900 seconds). Fully configurable." },
            { title: "Warning Before Clear", desc: "Players receive a warning message before entities are cleared." },
            { title: "Staff Notifications", desc: "Staff are notified when excessive entities are detected in a world." },
            { title: "Protected Entities", desc: "Named and tamed entities are never cleared, keeping pets and custom mobs safe." },
          ].map((f) => (
            <div key={f.title} className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-4">
              <h3 className="font-semibold text-sm mb-1">{f.title}</h3>
              <p className="text-sm text-[var(--text-secondary)]">{f.desc}</p>
            </div>
          ))}
        </div>
        <CodeBlock code={entityConfig} language="yaml" filename="config.yml" />
      </section>

      {/* Friendly Fire */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Friendly Fire</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Toggle friendly fire server-wide with <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/friendlyfire</code>.
          When disabled, players cannot damage each other.
        </p>
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
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Permission</th>
              </tr>
            </thead>
            <tbody>
              {commands.map((c) => (
                <tr key={c.command} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{c.command}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{c.desc}</td>
                  <td className="px-4 py-3 font-mono text-[var(--text-muted)] text-xs">{c.permission}</td>
                </tr>
              ))}
            </tbody>
          </table>
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
              {permissions.map((p) => (
                <tr key={p.perm} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{p.perm}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{p.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
