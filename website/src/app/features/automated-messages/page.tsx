import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const schedulingModes = [
  {
    name: "Interval",
    value: "interval",
    desc: "Sends messages at a fixed time interval (e.g., every 10 minutes). Rotates through the message list.",
  },
  {
    name: "Schedule",
    value: "schedule",
    desc: "Sends messages at specific times of day (e.g., 12:00, 18:00).",
  },
  {
    name: "On-the-Hour",
    value: "on-the-hour",
    desc: "Automatically fires at the top of every hour (XX:00).",
  },
  {
    name: "On-the-Half-Hour",
    value: "on-the-half-hour",
    desc: "Automatically fires at the bottom of every hour (XX:30).",
  },
];

const commands = [
  { command: "/automessage reload", desc: "Reload the automated messages configuration" },
  { command: "/automessage list", desc: "List all configured message groups" },
  { command: "/automessage toggle", desc: "Enable or disable automated messages" },
  { command: "/automessage send <id>", desc: "Manually trigger a specific message group" },
];

const configExample = `automessages:
  enabled: false
  messages:
    tips:
      mode: interval
      interval: "10m"
      prefix: "<gold>[Tip] "
      sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
      messages:
        - "<yellow>Tip: Use /wild for random teleport!"
        - "<yellow>Tip: Set homes with /sethome!"
        - "<yellow>Tip: Trade safely with /trade!"
    hourly:
      mode: on-the-hour
      message: "<gold>It's now {hour}:00!"
      permission: "justplugin.automessage.hourly"
      worlds:
        - world
        - world_nether
    halfhour:
      mode: on-the-half-hour
      message: "<aqua>Half-hour mark! Keep playing!"
    scheduled:
      mode: schedule
      times: ["12:00", "18:00"]
      message: "<green>Server checkpoint saved!"`;

export default function AutomatedMessagesPage() {
  return (
    <>
      <PageHeader
        title="Automated Messages"
        description="Scheduled broadcasts with four scheduling modes, rotating messages, per-message permissions, world filtering, and sound effects."
        badge="4 Modes"
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Automated Messages let you broadcast scheduled announcements, tips, and reminders to players.
          Messages support MiniMessage formatting, custom prefixes, sound effects, and can be filtered by
          permission or world. This feature is <strong>disabled by default</strong> and must be enabled in the config.
        </p>
      </section>

      {/* Scheduling Modes */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Scheduling Modes</h2>
        <div className="grid gap-4 sm:grid-cols-2">
          {schedulingModes.map((m) => (
            <div key={m.value} className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5">
              <div className="flex items-center gap-3 mb-2">
                <h3 className="font-semibold">{m.name}</h3>
                <code className="text-xs bg-[var(--bg-tertiary)] text-[var(--accent)] px-2 py-0.5 rounded">{m.value}</code>
              </div>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">{m.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Features */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Features</h2>
        <div className="grid gap-3">
          {[
            { title: "Rotating Messages", desc: "Interval mode cycles through a list of messages in order, so players see varied content." },
            { title: "Per-Message Permissions", desc: "Each message group can require a permission. Only players with the permission will see it." },
            { title: "World Filtering", desc: "Restrict messages to specific worlds. Players in other worlds won't receive the message." },
            { title: "Custom Prefix", desc: "Each message group can have its own prefix prepended to the message text." },
            { title: "Sound Effects", desc: "Play a sound when the message is sent. Uses Minecraft sound identifiers." },
            { title: "MiniMessage Formatting", desc: "Full MiniMessage support including gradients, rainbow, hover text, and click events." },
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
        <div className="mb-3 p-3 rounded-lg border border-[var(--yellow)]/30 bg-[var(--yellow)]/5">
          <p className="text-sm text-[var(--yellow)]">
            Automated messages are <strong>disabled by default</strong>. Set <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">enabled: true</code> to activate.
          </p>
        </div>
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
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.automessage.*</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Full access to all automated message commands</td>
              </tr>
              <tr className="border-b border-[var(--border)]">
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.automessage.reload</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Reload automated message configuration</td>
              </tr>
              <tr className="border-b border-[var(--border)]">
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.automessage.list</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">List all message groups</td>
              </tr>
              <tr className="border-b border-[var(--border)]">
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.automessage.toggle</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Toggle automated messages on/off</td>
              </tr>
              <tr>
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.automessage.send</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Manually send a message group</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
