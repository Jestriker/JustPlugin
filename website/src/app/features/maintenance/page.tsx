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

export default function MaintenancePage() {
  return (
    <div>
      <PageHeader
        title="Maintenance Mode"
        description="Lock down your server for maintenance with a custom kick screen, MOTD override, server icon, LuckPerms group bypass, and automatic disable on cooldown expiry."
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
                { cmd: "/maintenance mode on", desc: "Enable maintenance mode and kick non-allowed players" },
                { cmd: "/maintenance mode off", desc: "Disable maintenance mode" },
                { cmd: "/maintenance allowed-users add <player>", desc: "Add a player to the maintenance whitelist" },
                { cmd: "/maintenance allowed-users remove <player>", desc: "Remove a player from the maintenance whitelist" },
                { cmd: "/maintenance allowed-users list", desc: "List all whitelisted players" },
                { cmd: "/maintenance allowed-groups add <group>", desc: "Add a LuckPerms group to the maintenance whitelist" },
                { cmd: "/maintenance allowed-groups remove <group>", desc: "Remove a LuckPerms group from the whitelist" },
                { cmd: "/maintenance allowed-groups list", desc: "List all whitelisted groups" },
                { cmd: "/maintenance cooldown <duration>", desc: "Set an estimated end time (auto-disables when expired)" },
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
            { title: "Custom Kick Screen", desc: "Players see a fully customizable kick message with the reason and estimated end time." },
            { title: "MOTD Override", desc: "The server MOTD is replaced with a maintenance message while active." },
            { title: "Custom Server Icon", desc: "Display a custom 64x64 PNG server icon during maintenance." },
            { title: "LuckPerms Group Bypass", desc: "Allow entire LuckPerms groups (e.g., staff) to join during maintenance." },
            { title: "OP Bypass", desc: "Optionally allow server operators to bypass maintenance (disabled by default)." },
            { title: "Auto-Disable on Expiry", desc: "When a cooldown is set, maintenance mode automatically disables when the timer expires." },
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
          filename="maintenance/config.yml"
          code={`kick-message: "<red>Server is under maintenance!"
motd: "<red>[MAINTENANCE] <gray>Server is being updated"
icon: "maintenance-icon.png"
op-bypass: false
auto-disable-on-expire: true`}
        />
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-sm text-[var(--text-secondary)] mt-4 space-y-2">
          <p><strong>kick-message</strong> &mdash; MiniMessage-formatted text shown to players when they are kicked. Supports the estimated end time placeholder.</p>
          <p><strong>motd</strong> &mdash; Replaces the normal server MOTD in the server list while maintenance is active.</p>
          <p><strong>icon</strong> &mdash; Path to a 64x64 PNG file used as the server icon during maintenance. Place it in the <code className="text-[var(--accent-hover)]">maintenance/</code> folder.</p>
          <p><strong>op-bypass</strong> &mdash; When <code className="text-[var(--accent-hover)]">true</code>, server operators can join during maintenance. Defaults to <code className="text-[var(--accent-hover)]">false</code>.</p>
          <p><strong>auto-disable-on-expire</strong> &mdash; Automatically turn off maintenance mode when the cooldown duration expires.</p>
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
                { perm: "justplugin.maintenance", desc: "Manage maintenance mode settings", def: "Admin" },
                { perm: "justplugin.maintenance.bypass", desc: "Join the server during maintenance", def: "Admin" },
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
