import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const configExample = `tab-list:
  enabled: true
  update-interval: 100
  header:
    - ""
    - "<gradient:gold:yellow>  My Server  </gradient>"
    - "<gray>Online: <white>{online}<gray>/<white>{max_players}"
    - "<gray>Staff Online: <green>{online_staff}"
    - ""
  footer:
    - ""
    - "<gray>Welcome, <aqua>{welcome_name}<gray>!"
    - "<gray>TPS: <green>{tps} <dark_gray>| <gray>Ping: <green>{ping}ms"
    - "<gray>{anim:tips}"
    - ""`;

const maintenanceExample = `tab-list:
  maintenance-footer:
    enabled: true
    line: "<red><bold>MAINTENANCE SCHEDULED - Server restart at 3:00 AM UTC"`;

export default function TabListPage() {
  return (
    <>
      <PageHeader
        title="Tab List"
        description="Customize the player list header and footer with placeholders, animations, and real-time server info."
        badge="Customizable"
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The Tab List feature replaces the default player list header and footer with fully customizable
          templates. All 50+ scoreboard placeholders are available, plus animation support and special
          tab-only placeholders. The list refreshes at a configurable interval (default: 5 seconds / 100 ticks).
        </p>
      </section>

      {/* Placeholders */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Placeholders</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          All 50+ placeholders from the scoreboard are supported. Additionally, the tab list includes these
          exclusive placeholders:
        </p>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Placeholder</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-[var(--border)]">
                <td className="px-4 py-3 font-mono text-[var(--yellow)] text-xs">{"{online_staff}"}</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">
                  Count of online staff members. Detected from LuckPerms staff groups and server operators.
                </td>
              </tr>
              <tr className="border-b border-[var(--border)]">
                <td className="px-4 py-3 font-mono text-[var(--yellow)] text-xs">{"{welcome_name}"}</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">
                  The player&apos;s display name, used for personalized welcome messages.
                </td>
              </tr>
              <tr>
                <td className="px-4 py-3 font-mono text-[var(--yellow)] text-xs">{"{anim:name}"}</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">
                  Insert a named animation. Supports all 5 animation types (rainbow_gradient, typing, sweep_right, sweep_left, wave).
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* Staff Detection */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Staff Detection</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">{"{online_staff}"}</code> placeholder
          automatically counts online staff by checking LuckPerms group membership and server operator status.
          Any player who is in a configured staff group or is an OP will be counted.
        </p>
      </section>

      {/* Maintenance Warning */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Maintenance Warning</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          An optional maintenance warning line can be appended to the footer. When enabled, it displays a
          prominent message to all players in the tab list.
        </p>
        <CodeBlock code={maintenanceExample} language="yaml" filename="config.yml (maintenance)" />
      </section>

      {/* Config */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <CodeBlock code={configExample} language="yaml" filename="config.yml" />
        <div className="mt-3 p-3 rounded-lg border border-[var(--border)] bg-[var(--bg-card)]">
          <p className="text-sm text-[var(--text-muted)]">
            Default refresh interval is <strong className="text-[var(--text-secondary)]">100 ticks (5 seconds)</strong>.
            Lower values increase update frequency but may impact performance on large servers.
          </p>
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
              <tr>
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.tab</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">View the custom tab list header and footer</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
