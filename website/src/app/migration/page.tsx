import type { Metadata } from "next";
import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

export const metadata: Metadata = {
  title: "Migration Guide",
  description:
    "Step-by-step guide to migrate from EssentialsX, CMI, or other plugins to JustPlugin. Transfer economy data, homes, warps, and more.",
};

export default function MigrationPage() {
  return (
    <div>
      <PageHeader
        title="Migration Guide"
        description={<>Guide for migrating from EssentialsX, CMI, or SunLight to <span className="text-[var(--accent)]">JustPlugin</span>.</>}
      />

      {/* Credits */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-8">
        <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
          JustPlugin is a modern, from-scratch server management plugin that builds on the ideas pioneered
          by EssentialsX, CMI, and SunLight. We respect and credit these projects for paving the way &mdash;
          JustPlugin aims to bring a more streamlined, performant, and configurable approach to server management.
        </p>
      </div>

      {/* Why Migrate */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Why Migrate?</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          JustPlugin offers several advantages over traditional server management plugins.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {[
            { title: "Single JAR", desc: "One plugin replaces multiple. No EssentialsX + EssentialsXSpawn + EssentialsXChat combo needed." },
            { title: "Native Folia Support", desc: "Built-in region-threaded scheduler support. No patches or forks required." },
            { title: "Modern Paper API", desc: "Built exclusively on Paper's Adventure library. MiniMessage formatting, async events, modern Java 21." },
            { title: "Lighter Footprint", desc: "Lean codebase with minimal dependencies. Shaded HikariCP and bStats only." },
            { title: "50+ Built-in Placeholders", desc: "No PlaceholderAPI required for internal use. Player stats, economy, location, armor, and more." },
            { title: "GUI Management", desc: "In-game GUIs for kits, homes, stats, ranks, and trading. Less time in config files." },
            { title: "Progressive Warning System", desc: "Configurable escalation from chat warning to permanent ban. Per-player warning history." },
            { title: "Web Config Editor", desc: "Browser-based editor for all config files. Edit YAML without touching the server filesystem." },
          ].map((item) => (
            <div key={item.title} className="bg-[var(--bg-tertiary)] rounded-lg p-4">
              <div className="font-semibold text-sm text-[var(--accent-hover)] mb-1">{item.title}</div>
              <div className="text-xs text-[var(--text-secondary)]">{item.desc}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Command Mapping */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Command Mapping</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Most EssentialsX commands have direct equivalents in JustPlugin. Commands not listed here may
          be covered by a different feature or are not yet implemented.
        </p>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">EssentialsX</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--accent)] border-b border-[var(--border)]">JustPlugin</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Notes</th>
              </tr>
            </thead>
            <tbody>
              {[
                { ess: "/bal", jp: "/balance", notes: "Same functionality" },
                { ess: "/balancetop", jp: "/baltop", notes: "Same functionality" },
                { ess: "/pay", jp: "/pay", notes: "Same syntax" },
                { ess: "/tpa", jp: "/tpa", notes: "Same syntax" },
                { ess: "/tpahere", jp: "/tpahere", notes: "Same syntax" },
                { ess: "/tpaccept", jp: "/tpaccept", notes: "Same syntax" },
                { ess: "/tpdeny", jp: "/tpdeny", notes: "Same syntax" },
                { ess: "/sethome", jp: "/sethome", notes: "Same syntax, configurable max homes" },
                { ess: "/home", jp: "/home", notes: "GUI available via /homes" },
                { ess: "/delhome", jp: "/delhome", notes: "Same syntax" },
                { ess: "/warp", jp: "/warp", notes: "Same syntax" },
                { ess: "/setwarp", jp: "/setwarp", notes: "Same syntax" },
                { ess: "/delwarp", jp: "/delwarp", notes: "Same syntax" },
                { ess: "/ban", jp: "/ban", notes: "Same syntax" },
                { ess: "/tempban", jp: "/tempban", notes: "Same syntax" },
                { ess: "/unban", jp: "/unban", notes: "Same syntax" },
                { ess: "/mute", jp: "/mute", notes: "Same syntax" },
                { ess: "/unmute", jp: "/unmute", notes: "Same syntax" },
                { ess: "/kick", jp: "/kick", notes: "Same syntax" },
                { ess: "/god", jp: "/god", notes: "Same syntax" },
                { ess: "/fly", jp: "/fly", notes: "Same syntax" },
                { ess: "/speed", jp: "/speed", notes: "Same syntax" },
                { ess: "/heal", jp: "/heal", notes: "Same syntax" },
                { ess: "/feed", jp: "/feed", notes: "Same syntax" },
                { ess: "/hat", jp: "/hat", notes: "Same syntax" },
                { ess: "/nick", jp: "/nick", notes: "MiniMessage formatting supported" },
                { ess: "/mail", jp: "/mail", notes: "Same subcommands: send, read, clear" },
                { ess: "/afk", jp: "/afk", notes: "Same syntax, auto-AFK configurable" },
                { ess: "/back", jp: "/back", notes: "Returns to last location" },
                { ess: "/tpr (wild)", jp: "/tpr", notes: "Random teleport with safety checks" },
                { ess: "/invsee", jp: "/invsee", notes: "Same syntax" },
                { ess: "/enderchest", jp: "/enderchest", notes: "Same syntax" },
              ].map((row) => (
                <tr key={row.ess} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono">{row.ess}</code>
                  </td>
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono text-[var(--accent-hover)]">{row.jp}</code>
                  </td>
                  <td className="py-3 px-4 text-sm text-[var(--text-muted)]">{row.notes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Permission Mapping */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Permission Mapping</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          JustPlugin uses the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.*</code> namespace
          instead of <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">essentials.*</code>.
        </p>

        <div className="overflow-x-auto mb-4">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">EssentialsX</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--accent)] border-b border-[var(--border)]">JustPlugin</th>
              </tr>
            </thead>
            <tbody>
              {[
                { ess: "essentials.*", jp: "justplugin.*" },
                { ess: "essentials.fly", jp: "justplugin.fly" },
                { ess: "essentials.god", jp: "justplugin.god" },
                { ess: "essentials.heal", jp: "justplugin.heal" },
                { ess: "essentials.tpa", jp: "justplugin.tpa" },
                { ess: "essentials.home", jp: "justplugin.home" },
                { ess: "essentials.warp", jp: "justplugin.warp" },
                { ess: "essentials.ban", jp: "justplugin.ban" },
                { ess: "essentials.mute", jp: "justplugin.mute" },
                { ess: "essentials.nick", jp: "justplugin.nick" },
                { ess: "essentials.kit", jp: "justplugin.kit" },
                { ess: "essentials.balance", jp: "justplugin.balance" },
                { ess: "essentials.pay", jp: "justplugin.pay" },
                { ess: "essentials.afk", jp: "justplugin.afk" },
                { ess: "essentials.mail", jp: "justplugin.mail" },
                { ess: "essentials.vanish", jp: "justplugin.vanish" },
              ].map((row) => (
                <tr key={row.ess} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono">{row.ess}</code>
                  </td>
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono text-[var(--accent-hover)]">{row.jp}</code>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <CodeBlock
          language="yaml"
          filename="LuckPerms example"
          code={`# Grant all JustPlugin permissions to admin group
/lp group admin permission set justplugin.* true

# Grant basic player permissions to default group
/lp group default permission set justplugin.player true`}
        />
      </div>

      {/* What's Different */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">What&apos;s Different</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Key behavioral differences between JustPlugin and EssentialsX.
        </p>

        <div className="space-y-3">
          {[
            {
              title: "OPs don't get permissions by default",
              desc: "Unlike EssentialsX, being OP does not grant JustPlugin permissions. You must explicitly assign permissions via a permissions plugin like LuckPerms.",
            },
            {
              title: "Certain permissions are excluded from wildcard",
              desc: "justplugin.kit.cooldownbypass and justplugin.teleport.unsafe are never included in justplugin.*. They must be granted individually for safety.",
            },
            {
              title: "MiniMessage instead of legacy color codes",
              desc: "Configuration files use MiniMessage tags (<red>, <gradient:...>, etc.) instead of & color codes. Legacy codes still work in player-facing chat input.",
            },
            {
              title: "Single config structure",
              desc: "JustPlugin uses config.yml, database.yml, scoreboard.yml, automessages.yml, and per-feature configs. No separate plugin JARs for chat, spawn, etc.",
            },
            {
              title: "Built-in economy",
              desc: "JustPlugin has its own economy system. Vault integration is optional, not required.",
            },
          ].map((item) => (
            <div key={item.title} className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4">
              <div className="font-semibold text-sm mb-1">{item.title}</div>
              <div className="text-sm text-[var(--text-secondary)]">{item.desc}</div>
            </div>
          ))}
        </div>
      </div>

      {/* What JustPlugin Adds */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">What <span data-glow-text="" className="text-[var(--accent)]">JustPlugin</span> Adds</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Features available in JustPlugin that are not part of EssentialsX.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {[
            "Jail system with timed sentences",
            "Kit lifecycle GUI with preview",
            "Super Vanish (spectator ghost mode)",
            "Trading GUI between players",
            "Scoreboard with 50+ placeholders",
            "Teams with team chat",
            "Skin restorer (change skins in-game)",
            "Maintenance mode with bypass",
            "Web config editor (browser-based)",
            "Automated messages (interval, schedule, hourly)",
            "Safe teleport protection (3x3 hazard check)",
            "MOTD profiles with per-profile config",
            "Progressive warning escalation",
            "Stats GUI with player statistics",
            "Tab list with animated headers",
            "Built-in backup & export system",
            "World management commands",
            "Discord webhook logging",
          ].map((feature) => (
            <div key={feature} className="flex items-center gap-2 bg-[var(--bg-tertiary)] rounded-lg p-3">
              <span className="text-[var(--green)] flex-shrink-0">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M3 8L6.5 11.5L13 4.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </span>
              <span className="text-sm">{feature}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Data Migration */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Data Migration</h2>

        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6">
          <div className="flex items-start gap-3">
            <span className="text-[var(--yellow)] mt-1 flex-shrink-0">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
              </svg>
            </span>
            <div>
              <div className="font-semibold mb-2">Automatic data import is not yet available</div>
              <p className="text-sm text-[var(--text-secondary)] mb-3">
                JustPlugin does not currently support automatic import of player data, balances, homes, or warps
                from EssentialsX, CMI, or SunLight. Manual setup is required when switching.
              </p>
              <p className="text-sm text-[var(--text-secondary)] mb-3">
                We recommend starting fresh for the cleanest experience. This ensures all data is stored
                in JustPlugin&apos;s native format and avoids potential compatibility issues.
              </p>
              <p className="text-sm text-[var(--text-secondary)]">
                If you need to preserve specific data (like economy balances), you can manually transfer
                values using the JustPlugin API or in-game commands after setup.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Migration Checklist */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Migration Checklist</h2>

        <div className="space-y-2">
          {[
            "Remove EssentialsX / CMI / SunLight JARs from the plugins folder",
            "Install JustPlugin JAR",
            "Start the server to generate default configs",
            "Configure config.yml (economy, teleportation, command settings)",
            "Configure database.yml (YAML, SQLite, or MySQL)",
            "Set up permissions via LuckPerms (justplugin.player for default, justplugin.* for admin)",
            "Re-create warps using /setwarp",
            "Test core commands: /balance, /tpa, /home, /warp, /fly",
            "Configure scoreboard.yml and tab list if desired",
            "Set up automated messages in automessages.yml",
            "Enable the web editor if desired (config.yml > web-editor)",
          ].map((step, i) => (
            <div key={i} className="flex items-start gap-3 bg-[var(--bg-tertiary)] rounded-lg p-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--accent)] text-white text-xs font-bold flex items-center justify-center mt-0.5">
                {i + 1}
              </span>
              <span className="text-sm text-[var(--text-secondary)]">{step}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
