import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

export default function ModerationPage() {
  return (
    <>
      <PageHeader
        title="Moderation"
        description="A complete moderation toolkit covering bans, IP bans with CIDR support, mutes, progressive warnings, inventory inspection, offline player utilities, and configurable punishment announcements."
      />

      {/* ================================================================== */}
      {/*  Ban System                                                         */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--red)]" />
          <h2 className="text-xl font-semibold">Ban System</h2>
        </div>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Permanent and temporary bans for both player names and IP addresses. IP bans support CIDR
          notation, allowing you to block entire subnets. Banned players see a fully customizable
          disconnect screen with the reason, duration, and appeal information.
        </p>

        <div className="overflow-x-auto rounded-lg border border-[var(--border)] mb-4">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/ban", usage: "/ban <player> [reason]", desc: "Permanently ban a player" },
                { cmd: "/tempban", usage: "/tempban <player> <duration> [reason]", desc: "Temporarily ban a player" },
                { cmd: "/unban", usage: "/unban <player>", desc: "Unban a player" },
                { cmd: "/banip", usage: "/banip <ip|player> [reason]", desc: "Ban an IP address (CIDR supported)" },
                { cmd: "/tempbanip", usage: "/tempbanip <ip|player> <duration> [reason]", desc: "Temporarily ban an IP address" },
                { cmd: "/unbanip", usage: "/unbanip <ip>", desc: "Unban an IP address" },
                { cmd: "/banlist", usage: "/banlist", desc: "View all active player bans" },
                { cmd: "/baniplist", usage: "/baniplist", desc: "View all active IP bans" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">{r.usage}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5">
          <h3 className="text-sm font-semibold mb-2">Custom Ban Screen</h3>
          <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
            When a banned player attempts to join, they are shown a fully customizable disconnect
            screen. The screen displays the ban reason, remaining duration (for temp bans), the
            banning staff member, and optional appeal instructions. All text is configurable via
            MiniMessage formatting in the messages file.
          </p>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Mute System                                                        */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--yellow)]" />
          <h2 className="text-xl font-semibold">Mute System</h2>
        </div>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Silence players permanently or for a set duration. Muted players are prevented from sending
          public chat messages and private messages.
        </p>

        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/mute", usage: "/mute <player> [reason]", desc: "Permanently mute a player" },
                { cmd: "/tempmute", usage: "/tempmute <player> <duration> [reason]", desc: "Temporarily mute a player" },
                { cmd: "/unmute", usage: "/unmute <player>", desc: "Unmute a player" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">{r.usage}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Warning System                                                     */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--yellow)]" />
          <h2 className="text-xl font-semibold">Warning System</h2>
        </div>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Issue, remove, and review warnings with progressive punishment escalation. Each warning
          threshold triggers a configurable action, from a simple chat message all the way up to a
          permanent ban.
        </p>

        <div className="overflow-x-auto rounded-lg border border-[var(--border)] mb-6">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/warn add", usage: "/warn add <player> <reason>", desc: "Issue a warning to a player" },
                { cmd: "/warn remove", usage: "/warn remove <player> <id>", desc: "Remove a specific warning" },
                { cmd: "/warn list", usage: "/warn list <player>", desc: "List all warnings for a player" },
                { cmd: "/warn confirm", usage: "/warn confirm", desc: "Confirm a pending warning action" },
                { cmd: "/warn cancel", usage: "/warn cancel", desc: "Cancel a pending warning action" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">{r.usage}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <h3 className="text-base font-semibold mb-3">Default Escalation Config</h3>
        <p className="text-[var(--text-secondary)] text-sm mb-3">
          The punishment that is applied at each warning threshold is fully configurable. Below is
          the default progression:
        </p>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`warnings:
  punishments:
    1: "ChatMessage"
    2: "Kick"
    3: "TempBan 5m"
    4: "TempBan 1d"
    5: "TempBan 30d"
    6: "TempBan 365d"
    7: "Ban"`}
        />

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
          {[
            { count: "1 warning", action: "Chat message warning", color: "var(--green)" },
            { count: "2 warnings", action: "Kicked from server", color: "var(--yellow)" },
            { count: "3 warnings", action: "Temp banned for 5 minutes", color: "var(--yellow)" },
            { count: "4 warnings", action: "Temp banned for 1 day", color: "var(--accent)" },
            { count: "5-6 warnings", action: "Temp banned for 30d / 365d", color: "var(--red)" },
            { count: "7 warnings", action: "Permanent ban", color: "var(--red)" },
          ].map((s) => (
            <div
              key={s.count}
              className="flex items-center gap-3 rounded-lg border border-[var(--border)] bg-[var(--bg-card)] px-4 py-3"
            >
              <span
                className="w-2 h-2 rounded-full flex-shrink-0"
                style={{ backgroundColor: s.color }}
              />
              <div>
                <span className="text-xs font-semibold">{s.count}</span>
                <p className="text-xs text-[var(--text-muted)]">{s.action}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Kick                                                               */}
      {/* ================================================================== */}
      <section className="mb-12">
        <h2 className="text-xl font-semibold mb-3">Kick</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              <tr className="hover:bg-[var(--bg-hover)] transition-colors">
                <td className="px-4 py-2.5 font-mono text-[var(--accent)]">/kick</td>
                <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">/kick &lt;player&gt; [reason]</td>
                <td className="px-4 py-2.5 text-[var(--text-secondary)]">Kick a player from the server</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Inventory Inspection                                               */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--accent)]" />
          <h2 className="text-xl font-semibold">Inventory Inspection</h2>
        </div>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Staff can view and interact with any online player&apos;s inventory, ender chest, or death
          items in real-time.
        </p>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/invsee", usage: "/invsee <player>", desc: "Open a player's inventory in a 6-row GUI" },
                { cmd: "/echestsee", usage: "/echestsee <player>", desc: "View a player's ender chest" },
                { cmd: "/deathitems", usage: "/deathitems <player>", desc: "View items a player had on their last death" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">{r.usage}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Sudo                                                               */}
      {/* ================================================================== */}
      <section className="mb-12">
        <h2 className="text-xl font-semibold mb-3">Sudo</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              <tr className="hover:bg-[var(--bg-hover)] transition-colors">
                <td className="px-4 py-2.5 font-mono text-[var(--accent)]">/sudo</td>
                <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">/sudo &lt;player&gt; &lt;command|message&gt;</td>
                <td className="px-4 py-2.5 text-[var(--text-secondary)]">Force a player to execute a command or send a chat message</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Offline Player Commands                                            */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--accent-hover)]" />
          <h2 className="text-xl font-semibold">Offline Player Commands</h2>
        </div>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Inspect data for players who are not currently online. Useful for investigating reports and
          recovering items.
        </p>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Usage</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/tpoff", usage: "/tpoff <player>", desc: "Teleport to an offline player's last known location" },
                { cmd: "/invseeoff", usage: "/invseeoff <player>", desc: "View an offline player's inventory" },
                { cmd: "/echestseeoff", usage: "/echestseeoff <player>", desc: "View an offline player's ender chest" },
                { cmd: "/getposoff", usage: "/getposoff <player>", desc: "Get an offline player's last known position" },
                { cmd: "/getdeathposoff", usage: "/getdeathposoff <player>", desc: "Get an offline player's last death position" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)]">{r.usage}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Punishment Announcements                                           */}
      {/* ================================================================== */}
      <section className="mb-12">
        <h2 className="text-xl font-semibold mb-3">Punishment Announcements</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Every punishment type (ban, mute, kick, warn) can optionally broadcast a server-wide
          announcement when applied. Announcements are configurable per-type in the messages file and
          can be individually enabled or disabled. This lets you publicly announce bans while keeping
          mutes silent, for example.
        </p>
      </section>

      {/* ================================================================== */}
      {/*  Utility Commands                                                   */}
      {/* ================================================================== */}
      <section className="mb-12">
        <h2 className="text-xl font-semibold mb-4">Utility Commands</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Command</th>
                <th className="px-4 py-3">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { cmd: "/oplist", desc: "View all server operators" },
                { cmd: "/banlist", desc: "View all active player bans" },
                { cmd: "/baniplist", desc: "View all active IP bans" },
              ].map((r) => (
                <tr key={r.cmd} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-[var(--accent)]">{r.cmd}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* ================================================================== */}
      {/*  Permissions                                                        */}
      {/* ================================================================== */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-4">Permissions</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)] text-left text-xs text-[var(--text-muted)] uppercase tracking-wider">
                <th className="px-4 py-3">Permission</th>
                <th className="px-4 py-3">Description</th>
                <th className="px-4 py-3">Default</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {[
                { perm: "justplugin.ban", desc: "Ban a player", def: "Admin" },
                { perm: "justplugin.tempban", desc: "Temporarily ban a player", def: "Admin" },
                { perm: "justplugin.unban", desc: "Unban a player", def: "Admin" },
                { perm: "justplugin.banip", desc: "Ban an IP address", def: "Admin" },
                { perm: "justplugin.tempbanip", desc: "Temporarily ban an IP address", def: "Admin" },
                { perm: "justplugin.unbanip", desc: "Unban an IP address", def: "Admin" },
                { perm: "justplugin.mute", desc: "Mute a player", def: "Admin" },
                { perm: "justplugin.tempmute", desc: "Temporarily mute a player", def: "Admin" },
                { perm: "justplugin.unmute", desc: "Unmute a player", def: "Admin" },
                { perm: "justplugin.warn", desc: "Manage player warnings", def: "Admin" },
                { perm: "justplugin.kick", desc: "Kick a player", def: "Admin" },
                { perm: "justplugin.invsee", desc: "View a player's inventory", def: "Admin" },
                { perm: "justplugin.echestsee", desc: "View a player's ender chest", def: "Admin" },
                { perm: "justplugin.deathitems", desc: "View a player's death items", def: "Admin" },
                { perm: "justplugin.sudo", desc: "Force a player to run a command", def: "Admin" },
                { perm: "justplugin.tpoff", desc: "Teleport to an offline player", def: "Admin" },
                { perm: "justplugin.invseeoff", desc: "View an offline player's inventory", def: "Admin" },
                { perm: "justplugin.echestseeoff", desc: "View an offline player's ender chest", def: "Admin" },
                { perm: "justplugin.getposoff", desc: "Get an offline player's position", def: "Admin" },
                { perm: "justplugin.getdeathposoff", desc: "Get an offline player's death position", def: "Admin" },
                { perm: "justplugin.oplist", desc: "View the operator list", def: "Admin" },
                { perm: "justplugin.banlist", desc: "View the ban list", def: "Admin" },
                { perm: "justplugin.baniplist", desc: "View the IP ban list", def: "Admin" },
              ].map((r) => (
                <tr key={r.perm} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--accent)]">{r.perm}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                  <td className="px-4 py-2.5">
                    <span className="px-2 py-0.5 rounded text-xs font-medium bg-[var(--yellow)]/15 text-[var(--yellow)]">
                      {r.def}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
