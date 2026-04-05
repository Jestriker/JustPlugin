"use client";

import { useState } from "react";
import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

function AccordionItem({
  question,
  children,
  isOpen,
  onToggle,
}: {
  question: string;
  children: React.ReactNode;
  isOpen: boolean;
  onToggle: () => void;
}) {
  return (
    <div className="border border-[var(--border)] rounded-lg overflow-hidden">
      <button
        onClick={onToggle}
        className="w-full flex items-center justify-between gap-4 px-5 py-4 text-left hover:bg-[var(--bg-hover)] transition-colors"
      >
        <span className="font-semibold text-sm">{question}</span>
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
        <div className="px-5 pb-4 text-sm text-[var(--text-secondary)] leading-relaxed border-t border-[var(--border)] pt-4">
          {children}
        </div>
      )}
    </div>
  );
}

const faqItems: {
  question: string;
  answer: React.ReactNode;
}[] = [
  {
    question: "What Minecraft versions are supported?",
    answer: (
      <>
        <p>
          JustPlugin requires <strong>Minecraft 1.21.11 or newer</strong>. It is built exclusively for
          the <strong>Paper</strong>, <strong>Purpur</strong>, and <strong>Folia</strong> server platforms.
        </p>
        <p className="mt-2">
          Older Minecraft versions are not supported. The plugin uses modern Paper APIs and Java 21 features
          that are not available on older versions.
        </p>
      </>
    ),
  },
  {
    question: "Does JustPlugin work with Spigot?",
    answer: (
      <>
        <p>
          <strong>No.</strong> JustPlugin relies on Paper-exclusive APIs including the Adventure component library
          for MiniMessage formatting, async event handling, and enhanced scheduler APIs. These are not available
          on Spigot or Bukkit.
        </p>
        <p className="mt-2">
          We recommend using <a href="https://papermc.io" target="_blank" rel="noopener noreferrer" className="text-[var(--accent-hover)] hover:underline">Paper</a> as
          a drop-in replacement for Spigot. All Spigot plugins remain compatible on Paper.
        </p>
      </>
    ),
  },
  {
    question: "Do OPs automatically get all JustPlugin permissions?",
    answer: (
      <>
        <p>
          <strong>No.</strong> Unlike many plugins, OPs do not automatically receive JustPlugin permissions.
          You must explicitly grant permissions using a permissions manager like LuckPerms.
        </p>
        <p className="mt-2">
          To grant all permissions at once, assign the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.*</code> wildcard
          permission node to the desired group or player.
        </p>
      </>
    ),
  },
  {
    question: "How do I set up the economy system?",
    answer: (
      <>
        <p>
          JustPlugin includes a full built-in economy system that works out of the box. You can also integrate
          with an external economy plugin via Vault.
        </p>
        <p className="mt-3 mb-1 font-semibold text-[var(--text-primary)]">In config.yml:</p>
        <CodeBlock
          language="yaml"
          code={`economy:
  provider: "justplugin"   # Built-in economy
  # provider: "vault"      # External economy via Vault
  starting-balance: 100.0
  currency-symbol: "$"`}
        />
        <p className="mt-2">
          The built-in provider stores balances in your configured database (YAML, SQLite, or MySQL).
          Switch to <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">vault</code> if you want to use
          another economy plugin like EssentialsX Economy.
        </p>
      </>
    ),
  },
  {
    question: "Can I disable specific commands?",
    answer: (
      <>
        <p>
          Yes. Every command can be individually enabled or disabled in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code> under
          the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">command-settings</code> section.
        </p>
        <CodeBlock
          language="yaml"
          code={`command-settings:
  tpa: true
  wild: true
  pay: false    # Disables /pay completely
  trade: false  # Disables /trade completely`}
        />
        <p className="mt-2">
          Disabled commands are fully unregistered from the server. A server restart or <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpreload</code> is
          required after changing command settings.
        </p>
      </>
    ),
  },
  {
    question: "How do warnings and punishment escalation work?",
    answer: (
      <>
        <p>
          Warnings use a progressive escalation system. Each warning level triggers a configurable action,
          from chat messages to permanent bans.
        </p>
        <CodeBlock
          language="yaml"
          code={`warnings:
  escalation:
    1:
      action: "ChatMessage"
    2:
      action: "Kick"
    3:
      action: "TempBan"
      duration: "5m"
    4:
      action: "TempBan"
      duration: "1h"
    5:
      action: "TempBan"
      duration: "1d"
    6:
      action: "Ban"`}
        />
        <p className="mt-2">
          Available actions are <strong>ChatMessage</strong>, <strong>Kick</strong>, <strong>TempBan</strong> (requires
          a duration), and <strong>Ban</strong>. Warnings accumulate per player and persist across sessions.
        </p>
      </>
    ),
  },
  {
    question: "How do I set up Discord logging?",
    answer: (
      <>
        <p>
          Discord logging sends moderation actions, economy transactions, and other events to a Discord channel
          via webhooks. Setup is simple:
        </p>
        <ol className="list-decimal list-inside mt-2 space-y-1">
          <li>Create a webhook in your Discord channel settings</li>
          <li>Copy the webhook URL</li>
          <li>Run <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/setlogswebhook &lt;url&gt;</code> in-game</li>
        </ol>
        <p className="mt-2">
          You can configure which events are logged in the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">discord-logging</code> section
          of <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code>.
        </p>
      </>
    ),
  },
  {
    question: "What databases are supported?",
    answer: (
      <>
        <p>
          JustPlugin supports three database backends, configured in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">database.yml</code>:
        </p>
        <ul className="list-disc list-inside mt-2 space-y-1">
          <li><strong>YAML</strong> (default) &mdash; flat-file storage, no setup required</li>
          <li><strong>SQLite</strong> &mdash; single-file database, better performance than YAML</li>
          <li><strong>MySQL</strong> &mdash; remote database, ideal for networks or large servers</li>
        </ul>
        <p className="mt-2">
          MySQL uses HikariCP for connection pooling. All three backends store the same data &mdash; you can
          switch between them at any time (though data migration is manual).
        </p>
      </>
    ),
  },
  {
    question: "Is LuckPerms required?",
    answer: (
      <>
        <p>
          <strong>No.</strong> LuckPerms is entirely optional. JustPlugin works with any permissions plugin that
          supports the standard permission API.
        </p>
        <p className="mt-2">
          However, LuckPerms unlocks additional features:
        </p>
        <ul className="list-disc list-inside mt-2 space-y-1">
          <li>The <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/rank</code> GUI for managing player ranks</li>
          <li>Chat prefix and suffix display from LuckPerms groups</li>
          <li>Staff group detection for staff-only features</li>
          <li>Maintenance mode bypass by LuckPerms group</li>
        </ul>
      </>
    ),
  },
  {
    question: "How do I configure the scoreboard?",
    answer: (
      <>
        <p>
          The scoreboard is configured in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">scoreboard.yml</code>. It supports 50+ built-in
          placeholders, MiniMessage formatting, animated titles with wave effects, and custom frame animations.
        </p>
        <CodeBlock
          language="yaml"
          code={`enabled: true
update-interval: 20   # Ticks (20 = 1 second)
title:
  text: "<gradient:#ff6b6b:#ffa500>My Server</gradient>"
  wave-animation: true
lines:
  - "<white>Balance:</white> <green>%economy_balance_formatted%</green>"
  - "<white>Online:</white> <yellow>%online%</yellow>"`}
        />
        <p className="mt-2">
          PlaceholderAPI placeholders are also supported if the plugin is installed.
        </p>
      </>
    ),
  },
  {
    question: "Can players have multiple homes?",
    answer: (
      <>
        <p>
          Yes. The default maximum is <strong>3 homes</strong> per player, configurable in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">config.yml</code>:
        </p>
        <CodeBlock
          language="yaml"
          code={`homes:
  max-homes: 3`}
        />
        <p className="mt-2">
          You can grant more homes to specific players or groups using the permission
          node <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.homes.max.&lt;number&gt;</code>. For example,{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.homes.max.10</code> allows up to 10 homes.
        </p>
      </>
    ),
  },
  {
    question: "How does safe teleport work?",
    answer: (
      <>
        <p>
          When safe teleport is enabled, JustPlugin checks a <strong>3x3 area</strong> around the destination
          for hazards before teleporting. It checks for:
        </p>
        <ul className="list-disc list-inside mt-2 space-y-1">
          <li>Lava and fire</li>
          <li>Void (below Y=0 in the overworld)</li>
          <li>Suffocation in solid blocks</li>
          <li>Unsafe landing surfaces</li>
        </ul>
        <p className="mt-2">
          If the destination is unsafe, the teleport is cancelled and the player is notified. Players with
          the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">justplugin.teleport.unsafe</code> permission can bypass safety checks.
        </p>
      </>
    ),
  },
  {
    question: "How do I use the web config editor?",
    answer: (
      <>
        <p>
          The web config editor provides a browser-based GUI for editing all JustPlugin configuration files.
        </p>
        <p className="mt-2 mb-1 font-semibold text-[var(--text-primary)]">Enable it in config.yml:</p>
        <CodeBlock
          language="yaml"
          code={`web-editor:
  enabled: true
  host: "localhost"
  port: 8585`}
        />
        <p className="mt-2">
          Once enabled, access the editor at <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">http://localhost:8585</code> from a browser
          on the same machine as the server. For remote access, configure your host and firewall accordingly.
          Only enable on trusted networks.
        </p>
      </>
    ),
  },
  {
    question: "What's the difference between vanish and super vanish?",
    answer: (
      <>
        <p>
          JustPlugin offers two vanish modes:
        </p>
        <ul className="mt-2 space-y-2">
          <li>
            <strong>Regular Vanish</strong> (<code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/vanish</code>) &mdash; Makes
            the player invisible to other players. You remain in survival/creative mode and can still interact
            with the world. Other players cannot see you in the tab list or in-game.
          </li>
          <li>
            <strong>Super Vanish</strong> (<code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/supervanish</code>) &mdash; Puts
            the player into spectator-like ghost mode. You can fly through blocks, are completely undetectable,
            and cannot interact with the world. Ideal for silently monitoring players.
          </li>
        </ul>
        <p className="mt-2">
          Both modes hide the player from join/quit messages, the tab list, and other visibility checks.
        </p>
      </>
    ),
  },
  {
    question: "How do auto messages work?",
    answer: (
      <>
        <p>
          Auto messages support four scheduling modes, configured in <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">automessages.yml</code>:
        </p>
        <ul className="mt-2 space-y-2">
          <li>
            <strong>Interval</strong> &mdash; Broadcasts at a fixed interval (e.g., every 5 minutes). Messages cycle in order.
          </li>
          <li>
            <strong>Schedule</strong> &mdash; Broadcasts at specific times of day (e.g., 06:00, 18:00). Uses the configured timezone.
          </li>
          <li>
            <strong>On-the-Hour</strong> &mdash; Automatically broadcasts at the top of every hour (XX:00).
          </li>
          <li>
            <strong>On-the-Half-Hour</strong> &mdash; Automatically broadcasts at the bottom of every hour (XX:30).
          </li>
        </ul>
        <p className="mt-2">
          Each message group can use a different mode. Multiple messages within a group rotate sequentially.
        </p>
      </>
    ),
  },
  {
    question: "How do I backup my data?",
    answer: (
      <>
        <p>
          JustPlugin includes a built-in backup system accessible via the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpbackup</code> command:
        </p>
        <ul className="mt-2 space-y-1">
          <li>
            <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpbackup export</code> &mdash; Creates a full backup of all plugin data
          </li>
          <li>
            <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpbackup import</code> &mdash; Restores data from a backup file
          </li>
          <li>
            <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpbackup list</code> &mdash; Lists all available backups
          </li>
          <li>
            <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/jpbackup delete</code> &mdash; Removes a specific backup file
          </li>
        </ul>
        <p className="mt-2">
          Backups include all configuration files, player data, warps, homes, economy balances, and kits.
          They are stored in the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">plugins/JustPlugin/backups/</code> folder.
        </p>
      </>
    ),
  },
];

export default function FAQPage() {
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
    setOpenItems(new Set(faqItems.map((_, i) => i)));
  };

  const collapseAll = () => {
    setOpenItems(new Set());
  };

  return (
    <div>
      <PageHeader
        title="Frequently Asked Questions"
        description="Common questions and answers about installing, configuring, and using JustPlugin."
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

      <div className="space-y-3">
        {faqItems.map((item, index) => (
          <AccordionItem
            key={index}
            question={item.question}
            isOpen={openItems.has(index)}
            onToggle={() => toggleItem(index)}
          >
            {item.answer}
          </AccordionItem>
        ))}
      </div>
    </div>
  );
}
