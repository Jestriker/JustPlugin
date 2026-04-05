import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

export default function WarpsAndHomesPage() {
  return (
    <>
      <PageHeader
        title="Warps & Homes"
        description="Global server warps for shared destinations and per-player homes with an interactive Home GUI for quick navigation."
      />

      {/* ================================================================== */}
      {/*  WARPS                                                              */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--accent)]" />
          <h2 className="text-xl font-semibold">Warps</h2>
        </div>

        <p className="text-[var(--text-secondary)] leading-relaxed mb-6">
          Warps are global, named teleport locations that any player with the right permission can
          travel to. They persist across server restarts and are ideal for marking hubs, arenas,
          shops, and other shared points of interest.
        </p>

        {/* Warp Commands */}
        <h3 className="text-base font-semibold mb-3">Commands</h3>
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
                { cmd: "/warp", usage: "/warp <name>", desc: "Teleport to a warp location" },
                { cmd: "/warps", usage: "/warps", desc: "List all available warps" },
                { cmd: "/setwarp", usage: "/setwarp <name>", desc: "Create a new warp at your current location" },
                { cmd: "/delwarp", usage: "/delwarp <name>", desc: "Delete an existing warp" },
                { cmd: "/renamewarp", usage: "/renamewarp <old> <new>", desc: "Rename an existing warp" },
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

        {/* Warp Permissions */}
        <h3 className="text-base font-semibold mb-3">Permissions</h3>
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
                { perm: "justplugin.warp", desc: "Teleport to a warp location", def: "Player" },
                { perm: "justplugin.warps", desc: "List all warps", def: "Player" },
                { perm: "justplugin.setwarp", desc: "Create a new warp", def: "Admin" },
                { perm: "justplugin.delwarp", desc: "Delete a warp", def: "Admin" },
                { perm: "justplugin.renamewarp", desc: "Rename a warp", def: "Admin" },
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

      {/* ================================================================== */}
      {/*  HOMES                                                              */}
      {/* ================================================================== */}
      <section className="mb-12">
        <div className="flex items-center gap-2 mb-4">
          <span className="w-1 h-6 rounded-full bg-[var(--green)]" />
          <h2 className="text-xl font-semibold">Homes</h2>
        </div>

        <p className="text-[var(--text-secondary)] leading-relaxed mb-6">
          Each player can set a configurable number of personal home locations. Homes are private and
          persist across sessions. The built-in Home GUI makes managing and teleporting to homes
          intuitive and fast.
        </p>

        {/* Home GUI */}
        <div className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5 mb-6">
          <h3 className="text-sm font-semibold mb-3">Home GUI</h3>
          <p className="text-sm text-[var(--text-secondary)] mb-3">
            Running <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/home</code> without
            arguments opens a 4-row inventory GUI. It includes:
          </p>
          <ul className="text-sm text-[var(--text-secondary)] space-y-1.5 list-disc list-inside ml-2">
            <li>A spawn banner item for quick teleport to the server spawn</li>
            <li>Bed items representing each set home with coordinates in the lore</li>
            <li>Action dye items for setting, deleting, and renaming homes directly from the GUI</li>
            <li>Intuitive layout that scales with the number of homes the player has</li>
          </ul>
        </div>

        {/* Home Commands */}
        <h3 className="text-base font-semibold mb-3">Commands</h3>
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
                { cmd: "/home", usage: "/home [name]", desc: "Teleport to a home (opens GUI if no name given)" },
                { cmd: "/sethome", usage: "/sethome [name]", desc: "Set a home at your current location" },
                { cmd: "/delhome", usage: "/delhome <name>", desc: "Delete one of your homes" },
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

        {/* Home Configuration */}
        <h3 className="text-base font-semibold mb-3">Configuration</h3>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`homes:
  max-homes: 3`}
        />
        <p className="text-sm text-[var(--text-muted)] mt-2">
          The maximum number of homes each player can set. Grant more via permission-based overrides
          if needed.
        </p>

        {/* Home Permissions */}
        <h3 className="text-base font-semibold mt-6 mb-3">Permissions</h3>
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
                { perm: "justplugin.home", desc: "Teleport to your home", def: "Player" },
                { perm: "justplugin.sethome", desc: "Set a home location", def: "Player" },
                { perm: "justplugin.delhome", desc: "Delete a home location", def: "Player" },
              ].map((r) => (
                <tr key={r.perm} className="hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="px-4 py-2.5 font-mono text-xs text-[var(--accent)]">{r.perm}</td>
                  <td className="px-4 py-2.5 text-[var(--text-secondary)]">{r.desc}</td>
                  <td className="px-4 py-2.5">
                    <span className="px-2 py-0.5 rounded text-xs font-medium bg-[var(--green)]/15 text-[var(--green)]">
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
