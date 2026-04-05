"use client";

import { useState } from "react";
import { PLUGIN_VERSION } from "@/data/constants";
import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

interface TroubleshootItem {
  problem: string;
  solution: React.ReactNode;
  tags: string[];
}

const troubleshootItems: TroubleshootItem[] = [
  {
    problem: "Permissions not working",
    tags: ["permissions", "luckperms"],
    solution: (
      <>
        <p>
          <strong>OPs do not have JustPlugin permissions by default.</strong> Unlike many plugins,
          JustPlugin requires explicit permission grants even for operators.
        </p>
        <p className="mt-2">Grant permissions using LuckPerms or any permissions plugin:</p>
        <CodeBlock
          language="yaml"
          code={`# Grant basic player permissions to the default group
/lp group default permission set justplugin.player true

# Grant all permissions to the admin group
/lp group admin permission set justplugin.* true`}
        />
        <p className="mt-2 text-[var(--text-muted)] text-xs">
          Note: <code className="bg-[var(--bg-tertiary)] px-1 py-0.5 rounded">justplugin.kit.cooldownbypass</code> and{" "}
          <code className="bg-[var(--bg-tertiary)] px-1 py-0.5 rounded">justplugin.teleport.unsafe</code> are excluded
          from the wildcard and must be granted individually.
        </p>
      </>
    ),
  },
  {
    problem: "Economy not loading",
    tags: ["economy", "vault", "balance"],
    solution: (
      <>
        <p>
          Check the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">economy.provider</code> setting
          in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code>.
        </p>
        <CodeBlock
          language="yaml"
          code={`economy:
  provider: "justplugin"   # Uses built-in economy
  # provider: "vault"      # Requires Vault + an economy plugin`}
        />
        <p className="mt-2">
          If set to <strong>vault</strong>, ensure both Vault and a compatible economy plugin are installed.
          If you want to use JustPlugin&apos;s built-in economy, set the provider to <strong>justplugin</strong>.
        </p>
      </>
    ),
  },
  {
    problem: "Scoreboard not showing",
    tags: ["scoreboard", "display"],
    solution: (
      <>
        <p>Check the following:</p>
        <ol className="list-decimal list-inside mt-2 space-y-2">
          <li>
            Verify <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">enabled: true</code> in{" "}
            <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">scoreboard.yml</code>
          </li>
          <li>
            Run <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/reloadscoreboard</code> to
            force a refresh
          </li>
          <li>Check the server console for YAML parsing errors in scoreboard.yml</li>
          <li>
            Ensure placeholders in the scoreboard config are valid (typos will show as literal text)
          </li>
        </ol>
        <CodeBlock
          language="yaml"
          filename="scoreboard.yml"
          code={`enabled: true
update-interval: 20`}
        />
      </>
    ),
  },
  {
    problem: "Commands say 'Unknown command'",
    tags: ["commands", "config"],
    solution: (
      <>
        <p>
          JustPlugin allows individual commands to be disabled. Check the{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">command-settings</code> section in{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code>.
        </p>
        <CodeBlock
          language="yaml"
          code={`command-settings:
  tpa: true      # Enabled
  wild: true     # Enabled
  pay: false     # Disabled - will show "Unknown command"
  trade: false   # Disabled`}
        />
        <p className="mt-2">
          Set the command to <strong>true</strong> and run{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpreload</code> or restart the
          server. Disabled commands are fully unregistered.
        </p>
      </>
    ),
  },
  {
    problem: "Folia compatibility issues",
    tags: ["folia", "scheduler", "threading"],
    solution: (
      <>
        <p>
          Ensure you are running <strong>JustPlugin v{PLUGIN_VERSION}</strong> (latest). Folia support was added in v1.4
          and completed in v1.5.
        </p>
        <p className="mt-2">
          Check the server console for <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">SchedulerUtil</code> errors.
          JustPlugin automatically detects Folia at startup and switches to region-threaded schedulers.
          No configuration is needed.
        </p>
        <p className="mt-2">
          If you see scheduling errors, ensure no other plugins are conflicting with JustPlugin&apos;s
          scheduler detection. Try removing other plugins temporarily to isolate the issue.
        </p>
      </>
    ),
  },
  {
    problem: "Database connection failed",
    tags: ["database", "mysql", "sqlite"],
    solution: (
      <>
        <p>
          Check your credentials in{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">database.yml</code>.
        </p>
        <CodeBlock
          language="yaml"
          filename="database.yml"
          code={`type: mysql
mysql:
  host: "localhost"
  port: 3306
  database: "justplugin"
  username: "root"
  password: "your_password"`}
        />
        <p className="mt-2">For MySQL specifically:</p>
        <ul className="list-disc list-inside mt-1 space-y-1">
          <li>Verify the host, port, and database name are correct</li>
          <li>Ensure the MySQL user has CREATE, SELECT, INSERT, UPDATE, DELETE privileges</li>
          <li>Test the connection manually with a MySQL client</li>
          <li>Check that the database exists (JustPlugin will create tables but not the database itself)</li>
        </ul>
        <p className="mt-2">
          For a simpler setup, switch to <strong>sqlite</strong> or <strong>yaml</strong> which require no external database.
        </p>
      </>
    ),
  },
  {
    problem: "Players can't use /tpr in Nether/End",
    tags: ["teleportation", "wild", "permissions"],
    solution: (
      <>
        <p>
          Random teleportation in the Nether and End requires additional permissions beyond the base{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.wild</code> node.
        </p>
        <CodeBlock
          language="yaml"
          code={`# Required permissions for /tpr in other dimensions:
justplugin.wild.nether   # Allows /tpr in the Nether
justplugin.wild.end      # Allows /tpr in the End`}
        />
        <p className="mt-2">
          Grant these permissions via LuckPerms or your permissions plugin to players who should be
          able to random teleport in those dimensions.
        </p>
      </>
    ),
  },
  {
    problem: "Vanished players still visible",
    tags: ["vanish", "visibility"],
    solution: (
      <>
        <p>
          Other plugins may override JustPlugin&apos;s vanish state. Check for conflicting vanish plugins
          (SuperVanish, PremiumVanish, etc.) and remove them.
        </p>
        <p className="mt-2">
          For complete invisibility, use <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/supervanish</code> instead
          of <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/vanish</code>. Super Vanish puts the player into
          a spectator-like ghost mode where they are completely undetectable &mdash; invisible, no tab list
          entry, no join/quit messages, and unable to interact with the world.
        </p>
      </>
    ),
  },
  {
    problem: "Web editor not working",
    tags: ["web editor", "config"],
    solution: (
      <>
        <p>
          Ensure the web editor is enabled in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code>:
        </p>
        <CodeBlock
          language="yaml"
          code={`web-editor:
  enabled: true
  host: "localhost"
  port: 8585`}
        />
        <p className="mt-2">Troubleshooting steps:</p>
        <ul className="list-disc list-inside mt-1 space-y-1">
          <li>
            Access the editor at{" "}
            <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">http://localhost:8585</code>
          </li>
          <li>Check that port 8585 is not blocked by your firewall or used by another service</li>
          <li>For remote access, change the host from &quot;localhost&quot; to &quot;0.0.0.0&quot; and configure firewall rules</li>
          <li>Check the server console for binding errors on startup</li>
          <li>Only enable the web editor on trusted networks</li>
        </ul>
      </>
    ),
  },
  {
    problem: "Chat formatting looks wrong",
    tags: ["chat", "formatting", "minimessage"],
    solution: (
      <>
        <p>
          JustPlugin uses <strong>MiniMessage</strong> formatting, not legacy{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;</code> color codes.
        </p>
        <p className="mt-2">
          If your config files still use <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;c</code>,{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;l</code>, etc., convert them to MiniMessage tags:
        </p>
        <CodeBlock
          code={`# Legacy (won't work in configs):
&cThis is red &lbold text

# MiniMessage (correct):
<red>This is red <bold>bold text</bold></red>`}
        />
        <p className="mt-2">
          See the <a href="/formatting" className="text-[var(--accent-hover)] hover:underline">Formatting Guide</a> for
          a complete reference on MiniMessage syntax including gradients, hex colors, click events, and hover tooltips.
        </p>
      </>
    ),
  },
  {
    problem: "Kit cooldowns not working",
    tags: ["kits", "cooldowns"],
    solution: (
      <>
        <p>
          Cooldowns are configured per-kit in the kit configuration. Ensure each kit has a cooldown value set.
        </p>
        <p className="mt-2">
          The permission <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.kit.cooldownbypass</code> bypasses
          <strong> ALL</strong> kit cooldowns. This permission is intentionally excluded from the{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.*</code> wildcard. Check that
          your players or groups do not have this permission if cooldowns should apply.
        </p>
      </>
    ),
  },
  {
    problem: "Tab list not updating",
    tags: ["tab list", "display"],
    solution: (
      <>
        <p>
          Check the tab list configuration in{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code> under the{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">tab-list</code> section.
        </p>
        <CodeBlock
          language="yaml"
          code={`tab-list:
  enabled: true
  update-interval: 20
  header: "\\n<gradient:#00aaff:#00ffaa><bold>  My Server  </bold></gradient>\\n"
  footer: "\\n<gray>Players: <yellow>{online}<gray>/<yellow>{max_players}\\n"`}
        />
        <p className="mt-2">
          Run <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/tab</code> to force a refresh.
          Ensure placeholders in the header/footer are valid. Check the console for YAML parsing errors.
        </p>
      </>
    ),
  },
];

function ExpandableItem({
  item,
  isOpen,
  onToggle,
}: {
  item: TroubleshootItem;
  isOpen: boolean;
  onToggle: () => void;
}) {
  return (
    <div className="border border-[var(--border)] rounded-lg overflow-hidden">
      <button
        onClick={onToggle}
        className="w-full flex items-center justify-between gap-4 px-5 py-4 text-left hover:bg-[var(--bg-hover)] transition-colors"
      >
        <div className="flex items-center gap-3 min-w-0">
          <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--red)]/15 text-[var(--red)] text-xs font-bold flex items-center justify-center">
            !
          </span>
          <span className="font-semibold text-sm">{item.problem}</span>
        </div>
        <span
          className={`flex-shrink-0 text-[var(--text-muted)] transition-transform duration-200 ${
            isOpen ? "rotate-180" : ""
          }`}
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M4 6L8 10L12 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </span>
      </button>
      {isOpen && (
        <div className="px-5 pb-5 text-sm text-[var(--text-secondary)] leading-relaxed border-t border-[var(--border)] pt-4">
          {item.solution}
        </div>
      )}
    </div>
  );
}

export default function TroubleshootingPage() {
  const [openItems, setOpenItems] = useState<Set<number>>(new Set());

  const toggleItem = (index: number) => {
    setOpenItems((prev) => {
      const next = new Set(prev);
      if (next.has(index)) {
        next.delete(index);
      } else {
        next.add(index);
      }
      return next;
    });
  };

  const expandAll = () => {
    setOpenItems(new Set(troubleshootItems.map((_, i) => i)));
  };

  const collapseAll = () => {
    setOpenItems(new Set());
  };

  return (
    <div>
      <PageHeader
        title="Troubleshooting"
        description="Common issues and solutions for JustPlugin. Click any issue to expand the solution."
      />

      <div className="flex gap-2 mb-6">
        <button
          onClick={expandAll}
          className="px-3 py-1.5 text-xs font-medium rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--bg-hover)] transition-colors"
        >
          Expand All
        </button>
        <button
          onClick={collapseAll}
          className="px-3 py-1.5 text-xs font-medium rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--bg-hover)] transition-colors"
        >
          Collapse All
        </button>
      </div>

      {/* Quick links */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-8">
        <h2 className="text-lg font-semibold mb-3">Quick Links</h2>
        <p className="text-sm text-[var(--text-secondary)] mb-3">
          Can&apos;t find your issue here? Try these resources:
        </p>
        <div className="flex flex-wrap gap-3">
          <a
            href="/faq"
            className="px-4 py-2 rounded-lg bg-[var(--bg-tertiary)] text-sm text-[var(--accent-hover)] hover:bg-[var(--bg-hover)] transition-colors"
          >
            FAQ
          </a>
          <a
            href="https://discord.gg/QCArmUbaJ8"
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 rounded-lg bg-[var(--bg-tertiary)] text-sm text-[var(--accent-hover)] hover:bg-[var(--bg-hover)] transition-colors"
          >
            Discord Support
          </a>
          <a
            href="https://github.com/Jestriker/JustPlugin/issues"
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 rounded-lg bg-[var(--bg-tertiary)] text-sm text-[var(--accent-hover)] hover:bg-[var(--bg-hover)] transition-colors"
          >
            GitHub Issues
          </a>
        </div>
      </div>

      <div className="space-y-3">
        {troubleshootItems.map((item, index) => (
          <ExpandableItem
            key={index}
            item={item}
            isOpen={openItems.has(index)}
            onToggle={() => toggleItem(index)}
          />
        ))}
      </div>
    </div>
  );
}
