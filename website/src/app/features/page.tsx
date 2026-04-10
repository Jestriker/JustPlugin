import type { Metadata } from "next";
import Link from "next/link";
import PageHeader from "@/components/PageHeader";
import { icons } from "@/components/Icons";

export const metadata: Metadata = {
  title: "Features Overview",
  description:
    "Explore all 22 feature modules in JustPlugin: economy, teleportation, moderation, kits, teams, scoreboard, and more for your Minecraft server.",
};

interface FeatureCard {
  title: string;
  href: string;
  description: string;
  iconKey: string;
}

const features: FeatureCard[] = [
  { title: "Economy", href: "/features/economy", description: "Full balance system, Vault integration, PayNotes, Baltop GUI", iconKey: "economy" },
  { title: "Teleportation", href: "/features/teleportation", description: "TPA, random TP, safe teleport protection", iconKey: "teleportation" },
  { title: "Warps & Homes", href: "/features/warps-and-homes", description: "Global warps, per-player homes, Home GUI", iconKey: "warps" },
  { title: "Moderation", href: "/features/moderation", description: "Bans, mutes, warns, kick, invsee", iconKey: "moderation" },
  { title: "Jail System", href: "/features/jail", description: "Multiple jail locations, temp/permanent", iconKey: "jail" },
  { title: "Kit System", href: "/features/kits", description: "GUI selection, lifecycle, cooldowns", iconKey: "kits" },
  { title: "Vanish", href: "/features/vanish", description: "Standard and super vanish", iconKey: "vanish" },
  { title: "Teams", href: "/features/teams", description: "Team management, team chat", iconKey: "teams" },
  { title: "Trading", href: "/features/trading", description: "Hypixel SkyBlock-style GUI", iconKey: "trading" },
  { title: "Skin Restorer", href: "/features/skins", description: "Set/clear skins, skin bans", iconKey: "skins" },
  { title: "Maintenance Mode", href: "/features/maintenance", description: "Block joins, custom MOTD", iconKey: "maintenance" },
  { title: "Scoreboard", href: "/features/scoreboard", description: "50+ placeholders, animations", iconKey: "scoreboard" },
  { title: "Tab List", href: "/features/tab-list", description: "Custom header/footer", iconKey: "tablist" },
  { title: "MOTD Profiles", href: "/features/motd", description: "Static, cycle, random modes", iconKey: "motd" },
  { title: "Chat & Messaging", href: "/features/chat", description: "Private messages, hover stats, ignore", iconKey: "chat" },
  { title: "Mail System", href: "/features/mail", description: "Offline messaging", iconKey: "mail" },
  { title: "Nicknames & Tags", href: "/features/nicknames-tags", description: "MiniMessage, tag GUI", iconKey: "nicknames" },
  { title: "AFK System", href: "/features/afk", description: "Auto-AFK, idle kick", iconKey: "afk" },
  { title: "Automated Messages", href: "/features/automated-messages", description: "Scheduled broadcasts", iconKey: "automessages" },
  { title: "Virtual Inventories", href: "/features/virtual-inventories", description: "Anvil, craft, etc.", iconKey: "virtualinv" },
  { title: "World Management", href: "/features/world-management", description: "Weather, time, entity clear", iconKey: "world" },
  { title: "Backup & Export", href: "/features/backup", description: "Full data backup/restore", iconKey: "backup" },
];

export default function FeaturesPage() {
  return (
    <>
      <PageHeader
        title="Features"
        description="Explore every feature JustPlugin has to offer. Click a card to view full documentation, commands, permissions, and configuration for each module."
        badge="22 Modules"
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {features.map((f) => (
          <Link
            key={f.href}
            href={f.href}
            className="group block rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-5 hover:border-[var(--accent)] hover:bg-[var(--bg-hover)] transition-all"
          >
            <div className="flex items-start gap-3">
              <span className="text-[var(--accent)] group-hover:text-[var(--accent-hover)] transition-colors mt-0.5">{icons[f.iconKey]}</span>
              <div className="min-w-0">
                <h3 className="font-semibold text-sm group-hover:text-[var(--accent)] transition-colors">
                  {f.title}
                </h3>
                <p className="text-xs text-[var(--text-muted)] mt-1 leading-relaxed">
                  {f.description}
                </p>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </>
  );
}
