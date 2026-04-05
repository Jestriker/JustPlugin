import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

export default function TeleportationPage() {
  return (
    <>
      <PageHeader
        title="Teleportation"
        description="A comprehensive teleportation suite featuring TPA requests, random teleport with dimension selection, safe-landing protection, warmup countdowns, and configurable cooldowns."
      />

      {/* ------------------------------------------------------------------ */}
      {/*  Overview                                                           */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          JustPlugin&apos;s teleportation system covers every common server need: player-to-player TPA
          requests, a random wild teleport with per-dimension support, coordinate-based teleportation,
          spawn management, and a <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/back</code> command
          to return to previous locations. Every teleport action can have its own cooldown and warmup
          delay, and all teleports include built-in safe-landing checks.
        </p>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Commands                                                           */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-4">Commands</h2>
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
                { cmd: "/tpa", usage: "/tpa <player>", desc: "Send a teleport request to a player" },
                { cmd: "/tpahere", usage: "/tpahere <player>", desc: "Request a player to teleport to you" },
                { cmd: "/tpaccept", usage: "/tpaccept", desc: "Accept an incoming teleport request" },
                { cmd: "/tpreject", usage: "/tpreject", desc: "Reject an incoming teleport request" },
                { cmd: "/tpacancel", usage: "/tpacancel", desc: "Cancel your outgoing teleport request" },
                { cmd: "/tppos", usage: "/tppos <x> <y> <z> [world]", desc: "Teleport to exact coordinates" },
                { cmd: "/tpr", usage: "/tpr", desc: "Teleport to a random location (opens dimension GUI)" },
                { cmd: "/back", usage: "/back", desc: "Return to your previous location" },
                { cmd: "/spawn", usage: "/spawn [player]", desc: "Teleport to the server spawn point" },
                { cmd: "/setspawn", usage: "/setspawn", desc: "Set the server spawn point to your current location" },
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

      {/* ------------------------------------------------------------------ */}
      {/*  Safe Teleport Protection                                           */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Safe Teleport Protection</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Before completing any teleport, JustPlugin scans the 3&times;3 area around the destination
          for hazards. If a danger is detected the teleport is blocked and the player receives a
          warning with actionable options.
        </p>

        <div className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5 mb-4">
          <h3 className="text-sm font-semibold mb-3">Detected Hazards</h3>
          <div className="flex flex-wrap gap-2">
            {["Lava", "Fire", "Cactus", "Pressure Plates", "Sculk Sensors"].map((h) => (
              <span
                key={h}
                className="px-2.5 py-1 rounded-md text-xs font-medium bg-[var(--red)]/15 text-[var(--red)] border border-[var(--red)]/20"
              >
                {h}
              </span>
            ))}
          </div>
        </div>

        <div className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5">
          <h3 className="text-sm font-semibold mb-3">Staff Override Buttons</h3>
          <p className="text-[var(--text-secondary)] text-sm mb-3">
            When a hazard is detected, staff members with the appropriate permissions see clickable
            chat buttons that allow them to bypass the safety check:
          </p>
          <div className="flex flex-wrap gap-2">
            {[
              { label: "[TP Anyway]", color: "var(--yellow)" },
              { label: "[Creative Mode]", color: "var(--green)" },
              { label: "[God Mode]", color: "var(--accent)" },
            ].map((b) => (
              <span
                key={b.label}
                className="px-3 py-1.5 rounded-md text-xs font-bold font-mono"
                style={{ backgroundColor: `color-mix(in srgb, ${b.color} 15%, transparent)`, color: b.color }}
              >
                {b.label}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Random Teleport                                                    */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Random Teleport</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/tpr</code> command (aliases:{" "}
          <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/wild</code>,{" "}
          <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/rtp</code>) opens a dimension selection GUI
          allowing the player to choose between the Overworld, Nether, and End. Each dimension requires
          its own permission node. The teleport respects the configured wild range and minimum range
          values and always performs a safe-landing check before placing the player.
        </p>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Warmup Countdown                                                   */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Warmup Countdown</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Each teleport type can have its own warmup delay (in seconds). During the countdown the
          player must remain still; moving cancels the teleport. This is configured under the{" "}
          <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">delays</code> section. Staff with the
          bypass permission skip warmups entirely.
        </p>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Configuration                                                      */}
      {/* ------------------------------------------------------------------ */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Configuration</h2>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`teleportation:
  request-timeout: 60
  wild-range: 50000
  wild-min-range: 500
  cooldowns:
    tpa: 3
    spawn: 5
    wild: 5
    warp: 5
    home: 3
    back: 3
  delays:
    tpa: 180
    wild: 1800`}
        />
        <div className="mt-4 rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-4">
          <h3 className="text-sm font-semibold mb-2">Key Settings</h3>
          <ul className="text-sm text-[var(--text-secondary)] space-y-1.5">
            <li><strong>request-timeout</strong> &mdash; Seconds before an unanswered TPA request expires.</li>
            <li><strong>wild-range</strong> &mdash; Maximum distance (in blocks) from world center for random TP.</li>
            <li><strong>wild-min-range</strong> &mdash; Minimum distance from world center to avoid spawning near spawn.</li>
            <li><strong>cooldowns</strong> &mdash; Per-command cooldown in seconds between successive uses.</li>
            <li><strong>delays</strong> &mdash; Per-command warmup delay in seconds before the teleport executes.</li>
          </ul>
        </div>
      </section>

      {/* ------------------------------------------------------------------ */}
      {/*  Permissions                                                        */}
      {/* ------------------------------------------------------------------ */}
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
                { perm: "justplugin.tpa", desc: "Send a teleport request", def: "Player" },
                { perm: "justplugin.tpahere", desc: "Request a player to teleport to you", def: "Player" },
                { perm: "justplugin.tpaccept", desc: "Accept teleport requests", def: "Player" },
                { perm: "justplugin.tpreject", desc: "Reject teleport requests", def: "Player" },
                { perm: "justplugin.tpacancel", desc: "Cancel outgoing teleport requests", def: "Player" },
                { perm: "justplugin.tppos", desc: "Teleport to specific coordinates", def: "Admin" },
                { perm: "justplugin.wild", desc: "Random teleport (Overworld)", def: "Player" },
                { perm: "justplugin.wild.nether", desc: "Random teleport (Nether)", def: "Player" },
                { perm: "justplugin.wild.end", desc: "Random teleport (End)", def: "Player" },
                { perm: "justplugin.back", desc: "Return to previous location", def: "Player" },
                { perm: "justplugin.spawn", desc: "Teleport to spawn", def: "Player" },
                { perm: "justplugin.setspawn", desc: "Set the server spawn point", def: "Admin" },
                { perm: "justplugin.unsafetp", desc: "Bypass safe-teleport hazard checks", def: "Admin" },
                { perm: "justplugin.teleport.bypass", desc: "Bypass all teleport cooldowns and delays", def: "Admin" },
              ].map((r) => (
                <tr key={r.perm} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--accent)]">{r.perm}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                  <td className="px-4 py-2.5">
                    <span
                      className={`px-2 py-0.5 rounded text-xs font-medium ${
                        r.def === "Player"
                          ? "bg-[var(--green)]/15 text-[var(--green)]"
                          : "bg-[var(--yellow)]/15 text-[var(--yellow)]"
                      }`}
                    >
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
