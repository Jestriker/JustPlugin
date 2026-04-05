import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const commands = [
  { command: "/mail send <player> <message>", desc: "Send a mail message to any player (online or offline)" },
  { command: "/mail read [page]", desc: "Read your received mail with pagination" },
  { command: "/mail clear", desc: "Clear all your read mail" },
  { command: "/mail clearall", desc: "Clear all mail including unread messages" },
];

const configExample = `mail:
  enabled: true
  per-page: 10
  notify-on-login: true`;

export default function MailPage() {
  return (
    <>
      <PageHeader
        title="Mail System"
        description="Send messages to online or offline players with pagination, login notifications, and simple management commands."
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The mail system allows players to send messages to anyone, even if they are offline. Messages are
          stored persistently and delivered the next time the recipient logs in. Mail is paginated at 10 messages
          per page for easy browsing.
        </p>
      </section>

      {/* Features */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Features</h2>
        <div className="grid gap-3">
          {[
            { title: "Offline Messaging", desc: "Messages are stored and delivered when the recipient next joins the server." },
            { title: "Pagination", desc: "Mail is displayed 10 messages per page. Use /mail read [page] to navigate." },
            { title: "Login Notifications", desc: "Players are notified of unread mail when they log in." },
          ].map((f) => (
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
              {commands.map((c) => (
                <tr key={c.command} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{c.command}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{c.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Config */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <CodeBlock code={configExample} language="yaml" filename="config.yml" />
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
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.mail</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Access the mail system (read, clear, clearall)</td>
              </tr>
              <tr>
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.mail.send</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Send mail to other players</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
