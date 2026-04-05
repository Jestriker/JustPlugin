/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { icons } from "@/components/Icons";
import HomeSearch from "@/components/HomeSearch";
import { PLUGIN_VERSION } from "@/data/constants";

const features = [
  { iconKey: "economy", title: "Economy", desc: "Full balance system, Vault integration, PayNotes, Baltop GUI, Transaction History", href: "/features/economy" },
  { iconKey: "teleportation", title: "Teleportation", desc: "TPA, random TP with dimension GUI, safe teleport protection", href: "/features/teleportation" },
  { iconKey: "warps", title: "Warps & Homes", desc: "Global warps, per-player homes with interactive GUI", href: "/features/warps-and-homes" },
  { iconKey: "moderation", title: "Moderation", desc: "Bans, mutes, warns, kick, invsee, punishment escalation", href: "/features/moderation" },
  { iconKey: "jail", title: "Jail System", desc: "Multiple jail locations, temp/permanent, full restriction", href: "/features/jail" },
  { iconKey: "kits", title: "Kit System", desc: "GUI selection, lifecycle management, cooldowns, auto-equip", href: "/features/kits" },
  { iconKey: "vanish", title: "Vanish", desc: "Standard and super vanish with spectator ghost mode", href: "/features/vanish" },
  { iconKey: "teams", title: "Teams", desc: "Create teams, team chat, shared homes", href: "/features/teams" },
  { iconKey: "trading", title: "Trading", desc: "Hypixel SkyBlock-style trading GUI with balance transfer", href: "/features/trading" },
  { iconKey: "scoreboard", title: "Scoreboard", desc: "50+ placeholders, animated titles, 5 animation types", href: "/features/scoreboard" },
  { iconKey: "maintenance", title: "Maintenance", desc: "Block joins, custom MOTD, LuckPerms group bypass", href: "/features/maintenance" },
  { iconKey: "chat", title: "Chat", desc: "Private messages, hover stats, ignore system, announcements", href: "/features/chat" },
  { iconKey: "mail", title: "Mail", desc: "Offline messaging, pagination, login notifications", href: "/features/mail" },
  { iconKey: "nicknames", title: "Nicknames & Tags", desc: "MiniMessage formatting, tag GUI, prefix/suffix support", href: "/features/nicknames-tags" },
  { iconKey: "afk", title: "AFK System", desc: "Auto-AFK detection, idle kick, tab list integration", href: "/features/afk" },
  { iconKey: "automessages", title: "Auto Messages", desc: "Scheduled broadcasts with 4 timing modes", href: "/features/automated-messages" },
];

const quickLinks = [
  { label: "Commands", href: "/commands", desc: "200+ commands reference" },
  { label: "Permissions", href: "/permissions", desc: "Full permission tree" },
  { label: "Configuration", href: "/configuration", desc: "All config files" },
  { label: "API Reference", href: "/api", desc: "Developer integration" },
];

export default function Home() {
  return (
    <div>
      {/* Hero */}
      <div className="mb-10">
        <div className="flex items-center gap-3 mb-4">
          <img src="/justplugin-icon.png" alt="JustPlugin" width={48} height={48} className="rounded-xl" />
          <div>
            <h1 className="text-4xl font-bold">JustPlugin</h1>
            <p className="text-[var(--text-muted)] text-sm">v{PLUGIN_VERSION} &middot; Paper / Purpur / Folia</p>
          </div>
        </div>
        <p className="text-xl text-[var(--text-secondary)] leading-relaxed max-w-2xl">
          The only essentials plugin you&apos;ll ever need. A lightweight, fully configurable all-in-one server management plugin with <strong className="text-[var(--text-primary)]">200+ commands</strong>, <strong className="text-[var(--text-primary)]">150+ permissions</strong>, and <strong className="text-[var(--text-primary)]">50+ placeholders</strong>.
        </p>
      </div>

      {/* Search bar */}
      <HomeSearch />

      {/* Quick start */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-10">
        <h2 className="text-lg font-semibold mb-3">Quick Start</h2>
        <ol className="space-y-2 text-[var(--text-secondary)]">
          <li className="flex gap-3">
            <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--accent)] text-white text-xs flex items-center justify-center font-bold">1</span>
            <span>Drop <code>JustPlugin.jar</code> into your server&apos;s <code>plugins/</code> folder</span>
          </li>
          <li className="flex gap-3">
            <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--accent)] text-white text-xs flex items-center justify-center font-bold">2</span>
            <span>Start the server &mdash; all config files are auto-generated with comments</span>
          </li>
          <li className="flex gap-3">
            <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--accent)] text-white text-xs flex items-center justify-center font-bold">3</span>
            <span>Customize <code>config.yml</code>, <code>database.yml</code>, <code>scoreboard.yml</code>, and more</span>
          </li>
          <li className="flex gap-3">
            <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--accent)] text-white text-xs flex items-center justify-center font-bold">4</span>
            <span>Set up permissions with <a href="https://luckperms.net" target="_blank" rel="noopener noreferrer" className="text-[var(--accent-hover)] hover:underline">LuckPerms</a> or any permissions plugin</span>
          </li>
          <li className="flex gap-3">
            <span className="flex-shrink-0 w-6 h-6 rounded-full bg-[var(--accent)] text-white text-xs flex items-center justify-center font-bold">5</span>
            <span>Optionally configure Discord webhook logging with <code>/setlogswebhook &lt;url&gt;</code></span>
          </li>
        </ol>
      </div>

      {/* Quick links */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-10">
        {quickLinks.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className="group bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4 hover:border-[var(--accent)] transition-colors"
          >
            <div className="font-semibold text-sm group-hover:text-[var(--accent-hover)] transition-colors">{link.label}</div>
            <div className="text-xs text-[var(--text-muted)] mt-0.5">{link.desc}</div>
          </Link>
        ))}
      </div>

      {/* Features grid */}
      <h2 className="text-2xl font-bold mb-4">Features</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {features.map((f) => (
          <Link
            key={f.href}
            href={f.href}
            className="group bg-[var(--bg-card)] rounded-lg border border-[var(--border)] p-4 hover:border-[var(--accent)] transition-colors"
          >
            <div className="flex items-center gap-2.5 mb-1">
              <span className="text-[var(--accent)] group-hover:text-[var(--accent-hover)] transition-colors">{icons[f.iconKey]}</span>
              <span className="font-semibold text-sm group-hover:text-[var(--accent-hover)] transition-colors">{f.title}</span>
            </div>
            <p className="text-xs text-[var(--text-muted)] leading-relaxed">{f.desc}</p>
          </Link>
        ))}
      </div>

      {/* Requirements */}
      <div className="mt-10 bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6">
        <h2 className="text-lg font-semibold mb-3">Requirements</h2>
        <ul className="space-y-1.5 text-sm text-[var(--text-secondary)]">
          <li><strong className="text-[var(--text-primary)]">Paper</strong> 1.21.11 or newer (or any Paper fork like Purpur)</li>
          <li><strong className="text-[var(--text-primary)]">Java</strong> 21 or newer</li>
          <li>No required external dependencies &mdash; everything is self-contained</li>
        </ul>
        <h3 className="text-sm font-semibold mt-4 mb-2">Optional Dependencies</h3>
        <div className="overflow-x-auto">
          <table>
            <thead>
              <tr>
                <th>Plugin</th>
                <th>Purpose</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="font-mono text-xs">Vault</td>
                <td>Use an external economy provider instead of the built-in balance system</td>
              </tr>
              <tr>
                <td className="font-mono text-xs">LuckPerms</td>
                <td>Enable /rank GUI, chat prefix/suffix, staff group detection, maintenance group bypass</td>
              </tr>
              <tr>
                <td className="font-mono text-xs">PlaceholderAPI</td>
                <td>Expose all JustPlugin placeholders to other plugins</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
