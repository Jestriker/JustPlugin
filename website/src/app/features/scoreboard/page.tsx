import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const placeholderCategories = [
  {
    name: "Player Info",
    vars: [
      { placeholder: "{player}", desc: "Player username" },
      { placeholder: "{displayname}", desc: "Display name (with nick)" },
      { placeholder: "{health}", desc: "Current health" },
      { placeholder: "{max_health}", desc: "Max health" },
      { placeholder: "{food}", desc: "Food level" },
      { placeholder: "{xp_level}", desc: "XP level" },
    ],
  },
  {
    name: "Economy",
    vars: [
      { placeholder: "{balance}", desc: "Raw balance" },
      { placeholder: "{balance_formatted}", desc: "Formatted with commas" },
      { placeholder: "{balance_short}", desc: "Compact (25.7K, 1.5M)" },
    ],
  },
  {
    name: "Stats",
    vars: [
      { placeholder: "{kills}", desc: "Total kills" },
      { placeholder: "{deaths}", desc: "Total deaths" },
      { placeholder: "{kd_ratio}", desc: "Kill/death ratio" },
      { placeholder: "{playtime}", desc: "Formatted playtime" },
    ],
  },
  {
    name: "Server",
    vars: [
      { placeholder: "{online}", desc: "Online player count" },
      { placeholder: "{max_players}", desc: "Max player slots" },
      { placeholder: "{tps}", desc: "Server TPS" },
      { placeholder: "{ping}", desc: "Player ping (ms)" },
      { placeholder: "{mspt}", desc: "Milliseconds per tick" },
    ],
  },
  {
    name: "Location",
    vars: [
      { placeholder: "{world}", desc: "Current world name" },
      { placeholder: "{x}", desc: "X coordinate" },
      { placeholder: "{y}", desc: "Y coordinate" },
      { placeholder: "{z}", desc: "Z coordinate" },
      { placeholder: "{biome}", desc: "Current biome" },
      { placeholder: "{direction}", desc: "Compass direction (N/S/E/W)" },
    ],
  },
  {
    name: "State",
    vars: [
      { placeholder: "{gamemode}", desc: "Current gamemode" },
      { placeholder: "{fly_status}", desc: "Fly on/off" },
      { placeholder: "{god_status}", desc: "God mode on/off" },
      { placeholder: "{vanish_status}", desc: "Vanish on/off" },
    ],
  },
  {
    name: "Team",
    vars: [
      { placeholder: "{team}", desc: "Team name" },
      { placeholder: "{team_members}", desc: "Online team member count" },
    ],
  },
];

const animationTypes = [
  { name: "rainbow_gradient", desc: "Smoothly shifts through the full color spectrum" },
  { name: "typing", desc: "Simulates text being typed character-by-character" },
  { name: "sweep_right", desc: "Highlight color sweeps left to right" },
  { name: "sweep_left", desc: "Highlight color sweeps right to left" },
  { name: "wave", desc: "Characters bob up and down in a wave pattern" },
];

const configExample = `scoreboard:
  enabled: true
  update-interval: 20
  title: "<gradient:gold:yellow>JustPlugin</gradient>"
  wave-animation:
    color1: "#FFD700"
    color2: "#FFA500"
    speed: 2
  lines:
    1:
      text: "&7Balance: &a{balance_formatted}"
    2:
      text: "&7Kills: &c{kills} &8| &7Deaths: &c{deaths}"
    3:
      text: "&7Team: &b{team}"
      condition: "has_team"`;

export default function ScoreboardPage() {
  return (
    <>
      <PageHeader
        title="Scoreboard"
        description="A fully animated, placeholder-rich sidebar scoreboard with conditional lines, compact formatting, and fast refresh."
        badge="50+ Placeholders"
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The JustPlugin scoreboard displays a customizable sidebar with up to <strong>15 lines</strong>.
          It supports over 50 placeholder variables, five text animation types, conditional line visibility,
          and compact number formatting. The title features an animated wave gradient, and ping values refresh
          on a separate, faster interval for real-time accuracy.
        </p>
      </section>

      {/* Animated Title */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Animated Wave Title</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The scoreboard title supports an animated wave gradient that smoothly shifts between two configurable
          colors. Adjust <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">color1</code>,{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">color2</code>, and{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">speed</code> to
          customize the animation.
        </p>
      </section>

      {/* Text Animations */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Text Animations</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Five animation types can be applied to any scoreboard line text:
        </p>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Animation</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {animationTypes.map((a) => (
                <tr key={a.name} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{a.name}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{a.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Placeholders */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Placeholder Variables</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Over 50 placeholders are available, organized by category. All placeholders update automatically
          at the configured refresh interval.
        </p>
        <div className="space-y-6">
          {placeholderCategories.map((cat) => (
            <div key={cat.name}>
              <h3 className="text-sm font-semibold mb-2 text-[var(--accent)]">{cat.name}</h3>
              <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-[var(--bg-tertiary)]">
                      <th className="text-left px-4 py-2.5 font-semibold border-b border-[var(--border)]">Placeholder</th>
                      <th className="text-left px-4 py-2.5 font-semibold border-b border-[var(--border)]">Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cat.vars.map((v) => (
                      <tr key={v.placeholder} className="border-b border-[var(--border)] last:border-0">
                        <td className="px-4 py-2.5 font-mono text-[var(--yellow)] text-xs">{v.placeholder}</td>
                        <td className="px-4 py-2.5 text-[var(--text-secondary)]">{v.desc}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Conditional Lines */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Conditional Lines</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Lines can include a <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">condition</code> field.
          When set, the line only appears if the condition evaluates to true. For example, a team line with{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">condition: &quot;has_team&quot;</code>{" "}
          will only display when the player is in a team.
        </p>
      </section>

      {/* Compact Formatting */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Compact Number Formatting</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          The <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">{"{balance_short}"}</code> placeholder
          automatically formats large numbers into compact notation:
        </p>
        <div className="grid grid-cols-3 gap-3 max-w-md">
          {[
            { raw: "25,700", compact: "25.7K" },
            { raw: "1,500,000", compact: "1.5M" },
            { raw: "2,300,000,000", compact: "2.3B" },
          ].map((n) => (
            <div key={n.compact} className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-3 text-center">
              <div className="text-xs text-[var(--text-muted)] mb-1">{n.raw}</div>
              <div className="font-mono font-semibold text-[var(--green)]">{n.compact}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Fast Ping Refresh */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Fast Ping Refresh</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">{"{ping}"}</code> placeholder
          refreshes on a separate, faster interval than the rest of the scoreboard, ensuring players always
          see an up-to-date latency value without increasing overall refresh load.
        </p>
      </section>

      {/* Config */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <CodeBlock code={configExample} language="yaml" filename="config.yml" />
        <div className="mt-3 p-3 rounded-lg border border-[var(--border)] bg-[var(--bg-card)]">
          <p className="text-sm text-[var(--text-muted)]">
            Maximum of <strong className="text-[var(--text-secondary)]">15 lines</strong>. Lines exceeding
            this limit are silently ignored.
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
                <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">justplugin.scoreboard.reload</td>
                <td className="px-4 py-3 text-[var(--text-secondary)]">Reload the scoreboard configuration</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
