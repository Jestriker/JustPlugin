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

export default function JailPage() {
  return (
    <div>
      <PageHeader
        title="Jail System"
        description="Confine rule-breaking players to designated jail locations with full restriction controls, temporary or permanent durations, and multi-jail support."
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
                { cmd: "/jail <player> [duration] [reason]", desc: "Jail a player, optionally with a duration and reason" },
                { cmd: "/unjail <player>", desc: "Release a player from jail" },
                { cmd: "/setjail <name>", desc: "Create a named jail location at your position" },
                { cmd: "/deljail <name>", desc: "Delete a named jail location" },
                { cmd: "/jails", desc: "List all configured jail locations" },
                { cmd: "/jailinfo <player>", desc: "View jail details for a player (reason, time remaining)" },
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

      {/* Multiple Jail Locations */}
      <Section id="jail-locations" title="Multiple Jail Locations">
        <p className="text-[var(--text-secondary)] mb-4">
          Create as many named jail locations as you like with <code className="text-[var(--accent-hover)]">/setjail</code>.
          When a player is jailed without specifying a location, one is selected at random from
          the available jails. This prevents players from memorizing a single jail layout and keeps
          things unpredictable.
        </p>
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-sm text-[var(--text-secondary)]">
          <strong>Tip:</strong> Use descriptive names like <code className="text-[var(--accent-hover)]">dungeon</code>,{" "}
          <code className="text-[var(--accent-hover)]">cell-block-a</code>, or{" "}
          <code className="text-[var(--accent-hover)]">timeout-room</code> so staff can easily manage them.
        </div>
      </Section>

      {/* Duration Format */}
      <Section id="durations" title="Temporary &amp; Permanent Jails">
        <p className="text-[var(--text-secondary)] mb-4">
          Jail durations use a human-readable format combining days, hours, and minutes:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-4">
          {[
            { example: "30m", label: "30 minutes" },
            { example: "2h", label: "2 hours" },
            { example: "1d2h30m", label: "1 day, 2 hours, 30 minutes" },
          ].map((item) => (
            <div key={item.example} className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-center">
              <code className="text-lg text-[var(--accent-hover)] font-mono">{item.example}</code>
              <p className="text-xs text-[var(--text-muted)] mt-1">{item.label}</p>
            </div>
          ))}
        </div>
        <p className="text-[var(--text-secondary)]">
          Omit the duration entirely to jail a player <strong>permanently</strong> until manually released
          with <code className="text-[var(--accent-hover)]">/unjail</code>.
        </p>
      </Section>

      {/* Restrictions */}
      <Section id="restrictions" title="Restrictions">
        <p className="text-[var(--text-secondary)] mb-4">
          While jailed, players are subject to the following restrictions:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {[
            { icon: "Movement", desc: "Players cannot leave the jail area" },
            { icon: "Commands", desc: "All commands are blocked except those in the allowed list" },
            { icon: "Block Breaking", desc: "Players cannot break any blocks" },
            { icon: "Block Placing", desc: "Players cannot place any blocks" },
            { icon: "Entity Interaction", desc: "Players cannot interact with entities" },
            { icon: "Adventure Mode", desc: "Optionally switch players to adventure mode while jailed" },
          ].map((item) => (
            <div key={item.icon} className="flex items-start gap-3 bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4">
              <span className="text-[var(--red)] font-semibold text-sm shrink-0">{item.icon}</span>
              <span className="text-sm text-[var(--text-secondary)]">{item.desc}</span>
            </div>
          ))}
        </div>
      </Section>

      {/* Persistence */}
      <Section id="persistence" title="Persistence &amp; Offline Support">
        <p className="text-[var(--text-secondary)]">
          Jail data <strong>persists across server restarts</strong>. If a jailed player disconnects and
          reconnects, they remain jailed with their remaining time intact. You can also jail
          <strong> offline players</strong> &mdash; the jail will be applied when they next log in.
        </p>
      </Section>

      {/* Configuration */}
      <Section id="configuration" title="Configuration">
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`jail:
  adventure-mode: true
  block-commands: true
  allowed-commands:
    - "/msg"
    - "/r"`}
        />
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-sm text-[var(--text-secondary)] mt-2">
          <p><strong>adventure-mode</strong> &mdash; When enabled, jailed players are switched to adventure mode to prevent block interaction. Their previous gamemode is restored on unjail.</p>
          <p className="mt-2"><strong>block-commands</strong> &mdash; Block all commands while jailed except those in the <code className="text-[var(--accent-hover)]">allowed-commands</code> list.</p>
          <p className="mt-2"><strong>allowed-commands</strong> &mdash; Commands that jailed players can still use (e.g., messaging commands).</p>
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
                { perm: "justplugin.jail", desc: "Jail a player", def: "Admin" },
                { perm: "justplugin.unjail", desc: "Release a player from jail", def: "Admin" },
                { perm: "justplugin.setjail", desc: "Create a jail location", def: "Admin" },
                { perm: "justplugin.deljail", desc: "Delete a jail location", def: "Admin" },
                { perm: "justplugin.jails", desc: "List all jail locations", def: "Admin" },
                { perm: "justplugin.jailinfo", desc: "View jail info for a player", def: "Admin" },
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
