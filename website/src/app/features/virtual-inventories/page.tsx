import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const inventories = [
  { command: "/anvil", desc: "Open a virtual anvil GUI", permission: "justplugin.anvil" },
  { command: "/craft", desc: "Open a virtual crafting table GUI", permission: "justplugin.craft" },
  { command: "/grindstone", desc: "Open a virtual grindstone GUI", permission: "justplugin.grindstone" },
  { command: "/enderchest", desc: "Open your ender chest anywhere", permission: "justplugin.enderchest" },
  { command: "/stonecutter", desc: "Open a virtual stonecutter GUI", permission: "justplugin.stonecutter" },
  { command: "/loom", desc: "Open a virtual loom GUI", permission: "justplugin.loom" },
  { command: "/enchantingtable", desc: "Open a virtual enchanting table GUI", permission: "justplugin.enchantingtable" },
  { command: "/smithingtable", desc: "Open a virtual smithing table GUI", permission: "justplugin.smithingtable" },
  { command: "/pv [number]", desc: "Open a player vault (54-slot virtual storage)", permission: "justplugin.vault" },
];

export default function VirtualInventoriesPage() {
  return (
    <>
      <PageHeader
        title="Virtual Inventories"
        description="Open functional crafting stations and storage from anywhere with simple commands."
        badge="9 Inventories"
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          Virtual inventories let players open fully functional crafting station GUIs from anywhere in the world,
          without needing to find or place the actual block. All inventories work exactly like their
          block counterparts. Player Vaults provide persistent 54-slot virtual storage separate from the ender chest.
        </p>
      </section>

      {/* Inventory Table */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Available Inventories</h2>
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
              {inventories.map((inv) => (
                <tr key={inv.command} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{inv.command}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{inv.desc}</td>
                  <td className="px-4 py-3 font-mono text-[var(--text-muted)] text-xs">{inv.permission}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Player Vaults */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Player Vaults</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Player Vaults provide 54-slot virtual storage inventories that are completely separate from the
          ender chest. Each player can have multiple vaults (configurable max, default 3). Vaults are
          persistent and data is never lost &mdash; contents are saved on close, quit, and auto-save.
        </p>
        <ul className="list-disc list-inside text-[var(--text-secondary)] space-y-1.5 ml-2 text-sm mb-4">
          <li><code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/pv [number]</code> &mdash; open your vault (defaults to vault 1)</li>
          <li><code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">/pv &lt;player&gt; &lt;number&gt;</code> &mdash; open another player&apos;s vault (staff)</li>
          <li>Permission <code className="text-xs bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded font-mono">justplugin.vaults.&lt;number&gt;</code> to bypass the server max vault limit</li>
          <li>Disabled by default &mdash; opt-in via config</li>
        </ul>
        <CodeBlock
          language="yaml"
          filename="config.yml"
          code={`vaults:
  enabled: false       # Disabled by default, opt-in
  max-vaults: 3        # Maximum vaults per player`}
        />
      </section>
    </>
  );
}
