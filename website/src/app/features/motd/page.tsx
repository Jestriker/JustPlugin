import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const modes = [
  {
    name: "Static",
    value: "static",
    desc: "Displays a single MOTD profile at all times. The simplest mode for servers that don't need rotation.",
  },
  {
    name: "Cycle",
    value: "cycle",
    desc: "Rotates through MOTD profiles on a configurable timer. Supports intervals of 1s, 30s, 1m, and more.",
  },
  {
    name: "Random",
    value: "random",
    desc: "Shows a different MOTD profile each time a player refreshes the server list. Great for variety.",
  },
];

const configExample = `server-motd:
  mode: cycle
  delay: "30s"
  profiles:
    default:
      line1: "<gradient:gold:yellow>Welcome to My Server!"
      line2: "<gray>Paper 1.21.11 | {online}/{max} players"
    event:
      line1: "<rainbow>Double XP Weekend!"
      line2: "<yellow>Join now for bonus rewards!"

join-motd:
  message: "<gold>Welcome back, {player}! <gray>({online} players online)"`;

const iconExample = `server-motd:
  icon:
    enabled: true
    url: "https://example.com/server-icon.png"`;

const commands = [
  { command: "/motd", desc: "View the current MOTD configuration" },
  { command: "/motd server <text>", desc: "Set the server list MOTD" },
  { command: "/motd join <text>", desc: "Set the join MOTD message" },
  { command: "/resetmotd", desc: "Reset all MOTD settings to default" },
];

export default function MotdPage() {
  return (
    <>
      <PageHeader
        title="MOTD Profiles"
        description="Configure server list messages with static, cycling, or random profiles, custom icons, and join messages."
        badge="3 Modes"
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          MOTD Profiles let you control what players see in the server list and when they join. Define multiple
          profiles and choose how they rotate. Each profile supports MiniMessage formatting and placeholders.
          A separate join MOTD can display a personalized welcome when players log in.
        </p>
      </section>

      {/* Modes */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Display Modes</h2>
        <div className="grid gap-4">
          {modes.map((m) => (
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

      {/* Join MOTD */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Join MOTD</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The join MOTD is a separate message shown to players when they log into the server. It supports
          all placeholders including{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">{"{player}"}</code> and{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">{"{online}"}</code>.
        </p>
      </section>

      {/* Custom Server Icon */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Custom Server Icon</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Set a custom server icon from any URL. The image is automatically resized to 64x64 pixels and cached
          locally for fast loading.
        </p>
        <CodeBlock code={iconExample} language="yaml" filename="config.yml (icon)" />
      </section>

      {/* Config */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <CodeBlock code={configExample} language="yaml" filename="config.yml" />
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
    </>
  );
}
