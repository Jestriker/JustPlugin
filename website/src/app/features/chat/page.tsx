import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const messagingCommands = [
  { command: "/msg <player> <message>", desc: "Send a private message to a player", permission: "justplugin.msg" },
  { command: "/r <message>", desc: "Reply to the last private message", permission: "justplugin.reply" },
  { command: "/announce <message>", desc: "Broadcast a server-wide announcement", permission: "justplugin.announce" },
  { command: "/sharecoords", desc: "Share your current coordinates in chat", permission: "justplugin.sharecoords" },
  { command: "/sharedeathcoords", desc: "Share your last death coordinates in chat", permission: "justplugin.sharedeathcoords" },
  { command: "/clearchat [reason]", desc: "Clear the chat for all players", permission: "justplugin.clearchat" },
  { command: "/chat all|team", desc: "Switch between global and team chat mode", permission: "justplugin.chat" },
  { command: "/teammsg <message>", desc: "Send a message only to your team", permission: "justplugin.teammsg" },
];

const ignoreCommands = [
  { command: "/ignore add <player>", desc: "Block all communication from a player" },
  { command: "/ignore remove <player>", desc: "Unblock a player" },
  { command: "/ignore list", desc: "View your ignore list" },
  { command: "/ignore clearlist", desc: "Remove all players from your ignore list" },
];

const blockedInteractions = ["msg", "r", "tpa", "tpahere", "trade", "chat"];

const joinLeaveMode = [
  { mode: "none", desc: "No join/leave messages" },
  { mode: "all", desc: "Shown to all players" },
  { mode: "staff-only", desc: "Only visible to staff members" },
  { mode: "op-only", desc: "Only visible to server operators" },
  { mode: "group-based", desc: "Visibility based on LuckPerms groups" },
];

const joinLeaveConfig = `join-leave-messages:
  mode: all
  join: "<green>+ <white>{player} <gray>joined the server"
  leave: "<red>- <white>{player} <gray>left the server"`;

const permissions = [
  { perm: "justplugin.msg", desc: "Send private messages" },
  { perm: "justplugin.reply", desc: "Reply to private messages" },
  { perm: "justplugin.announce", desc: "Send server announcements" },
  { perm: "justplugin.ignore", desc: "Use the ignore system" },
  { perm: "justplugin.clearchat", desc: "Clear the server chat" },
  { perm: "justplugin.chat", desc: "Switch chat modes" },
  { perm: "justplugin.teammsg", desc: "Send team messages" },
  { perm: "justplugin.sharecoords", desc: "Share coordinates in chat" },
  { perm: "justplugin.sharedeathcoords", desc: "Share death coordinates" },
  { perm: "justplugin.chat.hover", desc: "View hover stat tooltips" },
  { perm: "justplugin.chat.clickstats", desc: "Click to view player stats" },
];

export default function ChatPage() {
  return (
    <>
      <PageHeader
        title="Chat & Messaging"
        description="Private messaging, announcements, ignore system, hover stats, coordinate sharing, and custom join/leave messages."
      />

      {/* Private Messaging */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Private Messaging</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Players can send private messages with <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/msg</code>{" "}
          and reply instantly with <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/r</code>.
          Messages include a clickable <span className="text-[var(--accent)] font-semibold">[Reply]</span> button
          in chat for quick responses.
        </p>
      </section>

      {/* Ignore System */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Ignore System</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The ignore system lets players block all communication from specific players. Ignored players are
          blocked from the following interactions:
        </p>
        <div className="flex flex-wrap gap-2 mb-4">
          {blockedInteractions.map((i) => (
            <span key={i} className="px-2.5 py-1 rounded-md bg-[var(--bg-tertiary)] border border-[var(--border)] text-xs font-mono text-[var(--red)]">
              {i}
            </span>
          ))}
        </div>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Command</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {ignoreCommands.map((c) => (
                <tr key={c.command} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{c.command}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{c.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Hover Stats */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Hover Stats & Click-to-View</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Hovering over a player&apos;s name in chat displays a tooltip with their stats:
        </p>
        <div className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5 max-w-sm">
          <div className="space-y-1.5 text-sm font-mono">
            <div className="text-[var(--text-muted)]">--- Player Stats ---</div>
            <div><span className="text-[var(--text-muted)]">Balance:</span> <span className="text-[var(--green)]">$12,500</span></div>
            <div><span className="text-[var(--text-muted)]">Kills:</span> <span className="text-[var(--red)]">247</span></div>
            <div><span className="text-[var(--text-muted)]">Deaths:</span> <span className="text-[var(--red)]">89</span></div>
            <div><span className="text-[var(--text-muted)]">Playtime:</span> <span className="text-[var(--yellow)]">14d 6h</span></div>
            <div><span className="text-[var(--text-muted)]">K/D:</span> <span className="text-[var(--accent)]">2.77</span></div>
          </div>
        </div>
        <p className="text-sm text-[var(--text-muted)] mt-3">
          Clicking a player&apos;s name opens a detailed stats view. Requires the{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.chat.clickstats</code> permission.
        </p>
      </section>

      {/* Join/Leave Messages */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Custom Join/Leave Messages</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Customize join and leave messages with 5 visibility modes:
        </p>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)] mb-4">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Mode</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {joinLeaveMode.map((m) => (
                <tr key={m.mode} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--yellow)] text-xs">{m.mode}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{m.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <CodeBlock code={joinLeaveConfig} language="yaml" filename="config.yml" />
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
              {messagingCommands.map((c) => (
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
