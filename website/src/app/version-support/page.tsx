import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";
import { PLUGIN_VERSION } from "@/data/constants";

function SupportBadge({ supported }: { supported: boolean }) {
  return supported ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-green-500/15 text-green-400">
      Supported
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-red-500/15 text-red-400">
      Not Supported
    </span>
  );
}

export default function VersionSupportPage() {
  return (
    <div>
      <PageHeader
        title="Version Support"
        description={<>Platform compatibility, version requirements, and build information for <span className="text-[var(--accent)]">JustPlugin</span>.</>}
        badge={`v${PLUGIN_VERSION}`}
      />

      {/* Minecraft Version */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-8">
        <h2 className="text-lg font-semibold mb-4">Requirements at a Glance</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-[var(--bg-tertiary)] rounded-lg p-4">
            <div className="text-xs text-[var(--text-muted)] mb-1">Minecraft Version</div>
            <div className="text-xl font-bold text-[var(--accent-hover)]">1.21.11+</div>
          </div>
          <div className="bg-[var(--bg-tertiary)] rounded-lg p-4">
            <div className="text-xs text-[var(--text-muted)] mb-1">Java Version</div>
            <div className="text-xl font-bold text-[var(--accent-hover)]">Java 21+</div>
          </div>
          <div className="bg-[var(--bg-tertiary)] rounded-lg p-4">
            <div className="text-xs text-[var(--text-muted)] mb-1">Server Software</div>
            <div className="text-xl font-bold text-[var(--accent-hover)]">Paper / Purpur / Folia</div>
          </div>
        </div>
      </div>

      {/* Supported Platforms */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Supported Platforms</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          JustPlugin is built exclusively on the Paper API and its forks. Spigot, Bukkit, and other
          platforms are not supported due to reliance on Paper-exclusive features like the Adventure
          component library, async scheduling, and enhanced event APIs.
        </p>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Platform</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Status</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Notes</th>
              </tr>
            </thead>
            <tbody>
              {[
                { platform: "Paper", supported: true, notes: "Primary target platform. Fully supported." },
                { platform: "Purpur", supported: true, notes: "Paper fork. Fully compatible with all features." },
                { platform: "Folia", supported: true, notes: "Region-threaded Paper fork. Full Folia scheduler support." },
                { platform: "Spigot", supported: false, notes: "Not supported. Uses Paper-exclusive APIs (Adventure, async events)." },
                { platform: "Bukkit", supported: false, notes: "Not supported. Missing required modern APIs." },
                { platform: "Sponge", supported: false, notes: "Not supported. Entirely different API ecosystem." },
                { platform: "Proxy (Velocity/BungeeCord)", supported: false, notes: "Not supported. JustPlugin is a server-side plugin only." },
              ].map((row) => (
                <tr key={row.platform} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4 font-mono text-sm font-semibold">{row.platform}</td>
                  <td className="py-3 px-4"><SupportBadge supported={row.supported} /></td>
                  <td className="py-3 px-4 text-sm text-[var(--text-secondary)]">{row.notes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Optional Dependencies */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Optional Dependencies</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          JustPlugin has no required dependencies &mdash; it is fully self-contained. The following plugins
          provide optional integrations for extended functionality.
        </p>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Plugin</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Purpose</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Required?</th>
              </tr>
            </thead>
            <tbody>
              {[
                {
                  plugin: "Vault",
                  purpose: "Use an external economy provider instead of the built-in balance system. Set economy.provider to \"vault\" in config.yml.",
                  required: "No",
                },
                {
                  plugin: "LuckPerms",
                  purpose: "Enable /rank GUI, chat prefix/suffix display, staff group detection, and maintenance mode group bypass.",
                  required: "No",
                },
                {
                  plugin: "PlaceholderAPI",
                  purpose: "Expose all 50+ JustPlugin placeholders to other plugins and use external placeholders in scoreboard/tab list.",
                  required: "No",
                },
              ].map((row) => (
                <tr key={row.plugin} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4 font-mono text-sm font-semibold text-[var(--accent-hover)]">{row.plugin}</td>
                  <td className="py-3 px-4 text-sm text-[var(--text-secondary)]">{row.purpose}</td>
                  <td className="py-3 px-4 text-sm text-[var(--text-muted)]">{row.required}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Build Information */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Build Information</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Technical details about how JustPlugin is built and packaged.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
          <div className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4">
            <div className="text-xs text-[var(--text-muted)] mb-1">Build System</div>
            <div className="font-semibold">Gradle</div>
          </div>
          <div className="bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4">
            <div className="text-xs text-[var(--text-muted)] mb-1">Packaging</div>
            <div className="font-semibold">ShadowJar (fat JAR)</div>
          </div>
        </div>

        <h3 className="text-lg font-semibold mb-3">Shaded Dependencies</h3>
        <p className="text-[var(--text-secondary)] mb-4">
          The following libraries are shaded (bundled) into the plugin JAR so no external downloads are needed:
        </p>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Library</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Purpose</th>
              </tr>
            </thead>
            <tbody>
              {[
                { lib: "bStats", purpose: "Anonymous usage statistics collection for plugin metrics." },
                { lib: "HikariCP", purpose: "High-performance JDBC connection pooling for MySQL database backend." },
                { lib: "SLF4J", purpose: "Logging facade used internally by HikariCP and other components." },
              ].map((row) => (
                <tr key={row.lib} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4 font-mono text-sm font-semibold">{row.lib}</td>
                  <td className="py-3 px-4 text-sm text-[var(--text-secondary)]">{row.purpose}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="bg-[var(--bg-tertiary)] border border-[var(--border)] rounded-lg p-4 mt-4 text-sm text-[var(--text-secondary)]">
          All shaded dependencies are relocated to prevent classpath conflicts with other plugins. The final
          JAR is self-contained and requires no additional downloads or library installations.
        </div>
      </div>

      {/* Folia Compatibility Details */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Folia Compatibility</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          JustPlugin fully supports Folia, Paper&apos;s region-threaded server fork. All scheduling operations
          use Folia-aware schedulers when running on a Folia server, ensuring thread-safe execution across
          region boundaries.
        </p>

        <CodeBlock
          language="yaml"
          filename="Automatic detection"
          code={`# No configuration needed!
# JustPlugin automatically detects Folia at startup and switches
# to region-threaded schedulers. All features work identically
# on Paper and Folia.`}
        />

        <div className="bg-[var(--bg-tertiary)] border border-[var(--border)] rounded-lg p-4 mt-4 text-sm text-[var(--text-secondary)]">
          Folia detection is automatic at startup. All teleportation, entity manipulation, and scheduled tasks
          use the appropriate region-aware or async scheduler. No manual configuration is required.
        </div>
      </div>
    </div>
  );
}
