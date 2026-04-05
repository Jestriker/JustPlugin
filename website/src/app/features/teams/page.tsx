import PageHeader from "@/components/PageHeader";

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="mb-10">
      <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">{title}</h2>
      {children}
    </section>
  );
}

export default function TeamsPage() {
  return (
    <div>
      <PageHeader
        title="Teams"
        description="Form teams with other players, communicate privately with team chat, and share team homes for convenient group teleportation."
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
                { cmd: "/team create <name>", desc: "Create a new team and become its leader" },
                { cmd: "/team disband", desc: "Disband your team (leader only)" },
                { cmd: "/team invite <player>", desc: "Invite a player to your team (leader only)" },
                { cmd: "/team join <name>", desc: "Accept an invitation and join a team" },
                { cmd: "/team leave", desc: "Leave your current team" },
                { cmd: "/team kick <player>", desc: "Kick a member from your team (leader only)" },
                { cmd: "/team info [name]", desc: "View information about a team" },
                { cmd: "/team list", desc: "List all teams on the server" },
                { cmd: "/teammsg <message>", desc: "Send a message to your team members only" },
                { cmd: "/chat all", desc: "Switch to global chat mode" },
                { cmd: "/chat team", desc: "Switch to team-only chat mode" },
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

      {/* Team Homes */}
      <Section id="team-homes" title="Team Homes">
        <p className="text-[var(--text-secondary)] mb-4">
          Teams can set shared home locations that any member can teleport to. This makes it easy for
          teams to regroup at a base or meet at a shared landmark without needing individual home slots.
        </p>
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4 text-sm text-[var(--text-secondary)]">
          <strong>Tip:</strong> Team homes are separate from personal homes. Leaving or being kicked from
          a team removes your access to that team&apos;s shared homes.
        </div>
      </Section>

      {/* Rules & Restrictions */}
      <Section id="rules" title="Rules &amp; Restrictions">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {[
            { title: "One Team Per Player", desc: "Each player can only belong to one team at a time. Leave your current team before joining another." },
            { title: "Leader Privileges", desc: "Only the team leader can invite new members, kick existing members, and disband the team." },
            { title: "Invitation Required", desc: "Players must be invited before they can join a team. Open joining is not supported." },
            { title: "Private Communication", desc: "Use /teammsg or switch to team chat mode with /chat team to keep conversations within your team." },
          ].map((item) => (
            <div key={item.title} className="bg-[var(--bg-card)] border border-[var(--border)] rounded-lg p-4">
              <h3 className="font-semibold text-sm mb-1">{item.title}</h3>
              <p className="text-sm text-[var(--text-secondary)]">{item.desc}</p>
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
              <tr className="hover:bg-[var(--bg-hover)] transition-colors">
                <td className="px-4 py-3 font-mono text-xs text-[var(--accent-hover)]">justplugin.team</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Access to all team commands</td>
                <td className="px-4 py-3 text-[var(--text-muted)]">Player</td>
              </tr>
            </tbody>
          </table>
        </div>
      </Section>
    </div>
  );
}
