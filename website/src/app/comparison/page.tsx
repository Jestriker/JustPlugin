import PageHeader from "@/components/PageHeader";

type Support = "full" | "partial" | "none";

interface ComparisonRow {
  feature: string;
  justplugin: Support;
  essentialsx: Support;
  cmi: Support;
  jpNote?: string;
  essNote?: string;
  cmiNote?: string;
}

const comparisonData: ComparisonRow[] = [
  { feature: "Economy", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Teleportation (TPA)", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Homes", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Warps", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Bans", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Mutes", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Warnings", justplugin: "full", essentialsx: "partial", cmi: "full", jpNote: "Progressive escalation system", essNote: "Basic warnings only" },
  { feature: "Jail System", justplugin: "full", essentialsx: "partial", cmi: "full", essNote: "Requires EssentialsX addon" },
  { feature: "Kit System", justplugin: "full", essentialsx: "full", cmi: "full", jpNote: "With GUI preview" },
  { feature: "Vanish", justplugin: "full", essentialsx: "full", cmi: "full", jpNote: "Includes Super Vanish (ghost mode)" },
  { feature: "Teams", justplugin: "full", essentialsx: "none", cmi: "partial", essNote: "Not available", cmiNote: "Via third-party integration" },
  { feature: "Player Trading GUI", justplugin: "full", essentialsx: "none", cmi: "none", essNote: "Not available", cmiNote: "Not available" },
  { feature: "Skin Restorer", justplugin: "full", essentialsx: "none", cmi: "none", essNote: "Requires separate plugin", cmiNote: "Not built-in" },
  { feature: "Maintenance Mode", justplugin: "full", essentialsx: "none", cmi: "full", essNote: "Requires separate plugin" },
  { feature: "Scoreboard", justplugin: "full", essentialsx: "none", cmi: "full", jpNote: "50+ built-in placeholders", essNote: "Requires separate plugin" },
  { feature: "Tab List", justplugin: "full", essentialsx: "none", cmi: "full", jpNote: "Animated headers/footers", essNote: "Requires separate plugin" },
  { feature: "MOTD Profiles", justplugin: "full", essentialsx: "partial", cmi: "full", essNote: "Basic MOTD only" },
  { feature: "Chat Formatting", justplugin: "full", essentialsx: "full", cmi: "full", jpNote: "MiniMessage native", essNote: "Requires EssentialsXChat" },
  { feature: "Mail System", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Nicknames", justplugin: "full", essentialsx: "full", cmi: "full", jpNote: "MiniMessage support" },
  { feature: "Tags", justplugin: "full", essentialsx: "none", cmi: "partial", essNote: "Not available" },
  { feature: "AFK System", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Auto Messages", justplugin: "full", essentialsx: "none", cmi: "full", jpNote: "4 scheduling modes", essNote: "Requires separate plugin" },
  { feature: "Virtual Inventories", justplugin: "full", essentialsx: "partial", cmi: "full", essNote: "Basic /invsee only" },
  { feature: "World Management", justplugin: "partial", essentialsx: "none", cmi: "full", jpNote: "Basic commands", essNote: "Not available", cmiNote: "Full world management" },
  { feature: "Backup & Export", justplugin: "full", essentialsx: "none", cmi: "partial", essNote: "Not available" },
  { feature: "Web Config Editor", justplugin: "full", essentialsx: "none", cmi: "none", essNote: "Not available", cmiNote: "Not available" },
  { feature: "Folia Support", justplugin: "full", essentialsx: "none", cmi: "none", jpNote: "Native support", essNote: "Not supported", cmiNote: "Not supported" },
  { feature: "Database Support", justplugin: "full", essentialsx: "partial", cmi: "full", jpNote: "YAML, SQLite, MySQL", essNote: "YAML only (flat file)" },
  { feature: "Developer API", justplugin: "full", essentialsx: "full", cmi: "full" },
  { feature: "Auction House", justplugin: "none", essentialsx: "none", cmi: "full", jpNote: "Not yet available", essNote: "Not available", cmiNote: "Built-in" },
  { feature: "Player Vaults", justplugin: "none", essentialsx: "none", cmi: "full", jpNote: "Not yet available", essNote: "Requires separate plugin", cmiNote: "Built-in" },
  { feature: "Holograms", justplugin: "none", essentialsx: "none", cmi: "full", jpNote: "Not yet available", essNote: "Not available", cmiNote: "Built-in" },
  { feature: "Custom Commands", justplugin: "none", essentialsx: "none", cmi: "full", jpNote: "Not yet available", essNote: "Not available", cmiNote: "Built-in" },
];

function SupportIndicator({ support, note }: { support: Support; note?: string }) {
  const config = {
    full: { label: "Yes", color: "var(--green)", bg: "rgba(34, 197, 94, 0.12)" },
    partial: { label: "Partial", color: "var(--yellow)", bg: "rgba(234, 179, 8, 0.12)" },
    none: { label: "No", color: "var(--red)", bg: "rgba(239, 68, 68, 0.12)" },
  };

  const c = config[support];

  return (
    <div className="flex flex-col gap-1">
      <span
        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium w-fit"
        style={{ backgroundColor: c.bg, color: c.color }}
      >
        {support === "full" && (
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M2.5 6L5 8.5L9.5 3.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        )}
        {support === "partial" && (
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M3 6H9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
        )}
        {support === "none" && (
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M3.5 3.5L8.5 8.5M8.5 3.5L3.5 8.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
        )}
        {c.label}
      </span>
      {note && <span className="text-xs text-[var(--text-muted)]">{note}</span>}
    </div>
  );
}

export default function ComparisonPage() {
  const jpFull = comparisonData.filter((r) => r.justplugin === "full").length;
  const essFull = comparisonData.filter((r) => r.essentialsx === "full").length;
  const cmiFull = comparisonData.filter((r) => r.cmi === "full").length;

  return (
    <div>
      <PageHeader
        title="Feature Comparison"
        description="Side-by-side comparison of JustPlugin, EssentialsX, and CMI. We aim to be fair and transparent about what each plugin offers."
      />

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-5 text-center">
          <div className="text-2xl font-bold text-[var(--accent-hover)]">{jpFull}</div>
          <div className="text-sm font-semibold mt-1">JustPlugin</div>
          <div className="text-xs text-[var(--text-muted)] mt-0.5">Full features</div>
        </div>
        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-5 text-center">
          <div className="text-2xl font-bold text-[var(--text-secondary)]">{essFull}</div>
          <div className="text-sm font-semibold mt-1">EssentialsX</div>
          <div className="text-xs text-[var(--text-muted)] mt-0.5">Full features</div>
        </div>
        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-5 text-center">
          <div className="text-2xl font-bold text-[var(--text-secondary)]">{cmiFull}</div>
          <div className="text-sm font-semibold mt-1">CMI</div>
          <div className="text-xs text-[var(--text-muted)] mt-0.5">Full features</div>
        </div>
      </div>

      {/* Fairness note */}
      <div className="bg-[var(--bg-tertiary)] border border-[var(--border)] rounded-lg p-4 mb-8 text-sm text-[var(--text-secondary)]">
        <span className="font-semibold">Note on fairness:</span> This comparison is based on built-in
        features only. EssentialsX has a rich ecosystem of addon plugins (EssentialsXChat, EssentialsXSpawn, etc.)
        and third-party extensions. CMI is a premium plugin with years of development and features like
        auction houses, holograms, and custom commands that JustPlugin does not yet offer. We credit both
        projects for their contributions to the Minecraft server community.
      </div>

      {/* Comparison table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr>
              <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b-2 border-[var(--border)] min-w-[180px]">Feature</th>
              <th className="text-left py-3 px-4 text-sm font-semibold border-b-2 border-[var(--border)] min-w-[140px]">
                <span className="text-[var(--accent-hover)]">JustPlugin</span>
              </th>
              <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b-2 border-[var(--border)] min-w-[140px]">EssentialsX</th>
              <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b-2 border-[var(--border)] min-w-[140px]">CMI</th>
            </tr>
          </thead>
          <tbody>
            {comparisonData.map((row) => (
              <tr key={row.feature} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                <td className="py-3 px-4 text-sm font-semibold">{row.feature}</td>
                <td className="py-3 px-4">
                  <SupportIndicator support={row.justplugin} note={row.jpNote} />
                </td>
                <td className="py-3 px-4">
                  <SupportIndicator support={row.essentialsx} note={row.essNote} />
                </td>
                <td className="py-3 px-4">
                  <SupportIndicator support={row.cmi} note={row.cmiNote} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Legend */}
      <div className="flex flex-wrap gap-6 mt-6 mb-10 p-4 bg-[var(--bg-card)] rounded-lg border border-[var(--border)]">
        <div className="flex items-center gap-2">
          <SupportIndicator support="full" />
          <span className="text-sm text-[var(--text-secondary)]">= Fully supported built-in</span>
        </div>
        <div className="flex items-center gap-2">
          <SupportIndicator support="partial" />
          <span className="text-sm text-[var(--text-secondary)]">= Partial or requires addon</span>
        </div>
        <div className="flex items-center gap-2">
          <SupportIndicator support="none" />
          <span className="text-sm text-[var(--text-secondary)]">= Not available</span>
        </div>
      </div>

      {/* Key takeaways */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Key Takeaways</h2>

        <div className="space-y-4">
          <div className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-5">
            <h3 className="font-semibold text-[var(--accent-hover)] mb-2">JustPlugin</h3>
            <p className="text-sm text-[var(--text-secondary)]">
              Best for servers wanting a modern, single-JAR solution with native Folia support, built-in
              web editor, and MiniMessage formatting. Ideal for new servers or those seeking a lighter
              alternative to CMI with most of the same features.
            </p>
          </div>

          <div className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-5">
            <h3 className="font-semibold mb-2">EssentialsX</h3>
            <p className="text-sm text-[var(--text-secondary)]">
              The industry standard with the largest community and third-party support. Best for servers
              that prefer a battle-tested, open-source solution with extensive documentation and addon ecosystem.
              Lacks some modern features like scoreboards, tab lists, and Folia support.
            </p>
          </div>

          <div className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-5">
            <h3 className="font-semibold mb-2">CMI</h3>
            <p className="text-sm text-[var(--text-secondary)]">
              The most feature-rich option with auction houses, holograms, custom commands, and player vaults
              built in. Best for established servers that need every possible feature. Premium (paid) plugin
              with years of development. Does not support Folia.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
