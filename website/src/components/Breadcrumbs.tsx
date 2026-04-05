"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const segmentNames: Record<string, string> = {
  features: "Features",
  commands: "Commands",
  permissions: "Permissions",
  configuration: "Configuration",
  api: "API Reference",
  "version-support": "Version Support",
  faq: "FAQ",
  economy: "Economy",
  teleportation: "Teleportation",
  "warps-and-homes": "Warps & Homes",
  moderation: "Moderation",
  jail: "Jail System",
  kits: "Kit System",
  vanish: "Vanish",
  teams: "Teams",
  trading: "Trading",
  skins: "Skin Restorer",
  maintenance: "Maintenance Mode",
  scoreboard: "Scoreboard",
  "tab-list": "Tab List",
  motd: "MOTD Profiles",
  chat: "Chat & Messaging",
  mail: "Mail System",
  "nicknames-tags": "Nicknames & Tags",
  afk: "AFK System",
  "automated-messages": "Automated Messages",
  "virtual-inventories": "Virtual Inventories",
  "world-management": "World Management",
  backup: "Backup & Export",
};

export default function Breadcrumbs() {
  const pathname = usePathname();

  if (pathname === "/") return null;

  const segments = pathname.split("/").filter(Boolean);
  const crumbs = segments.map((seg, i) => {
    const href = "/" + segments.slice(0, i + 1).join("/");
    const label = segmentNames[seg] || seg.replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase());
    const isLast = i === segments.length - 1;
    return { href, label, isLast };
  });

  return (
    <nav className="flex items-center gap-1.5 text-sm text-[var(--text-muted)] mb-6">
      <Link href="/" className="hover:text-[var(--accent)] transition-colors">
        Home
      </Link>
      {crumbs.map((crumb) => (
        <span key={crumb.href} className="flex items-center gap-1.5">
          <svg className="w-3 h-3 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
          </svg>
          {crumb.isLast ? (
            <span className="text-[var(--text-secondary)] font-medium">{crumb.label}</span>
          ) : (
            <Link href={crumb.href} className="hover:text-[var(--accent)] transition-colors">
              {crumb.label}
            </Link>
          )}
        </span>
      ))}
    </nav>
  );
}
