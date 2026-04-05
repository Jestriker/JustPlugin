"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

interface PageEntry {
  href: string;
  label: string;
}

const pageOrder: PageEntry[] = [
  { href: "/", label: "Home" },
  { href: "/features", label: "Features Overview" },
  { href: "/features/economy", label: "Economy" },
  { href: "/features/teleportation", label: "Teleportation" },
  { href: "/features/warps-and-homes", label: "Warps & Homes" },
  { href: "/features/moderation", label: "Moderation" },
  { href: "/features/jail", label: "Jail System" },
  { href: "/features/kits", label: "Kit System" },
  { href: "/features/vanish", label: "Vanish" },
  { href: "/features/teams", label: "Teams" },
  { href: "/features/trading", label: "Trading" },
  { href: "/features/skins", label: "Skin Restorer" },
  { href: "/features/maintenance", label: "Maintenance Mode" },
  { href: "/features/scoreboard", label: "Scoreboard" },
  { href: "/features/tab-list", label: "Tab List" },
  { href: "/features/motd", label: "MOTD Profiles" },
  { href: "/features/chat", label: "Chat & Messaging" },
  { href: "/features/mail", label: "Mail System" },
  { href: "/features/nicknames-tags", label: "Nicknames & Tags" },
  { href: "/features/afk", label: "AFK System" },
  { href: "/features/automated-messages", label: "Automated Messages" },
  { href: "/features/virtual-inventories", label: "Virtual Inventories" },
  { href: "/features/world-management", label: "World Management" },
  { href: "/features/backup", label: "Backup & Export" },
  { href: "/commands", label: "Commands" },
  { href: "/permissions", label: "Permissions" },
  { href: "/configuration", label: "Configuration" },
  { href: "/api", label: "API Reference" },
  { href: "/version-support", label: "Version Support" },
  { href: "/faq", label: "FAQ" },
];

export default function PrevNext() {
  const pathname = usePathname();
  const currentIndex = pageOrder.findIndex((p) => p.href === pathname);

  if (currentIndex === -1) return null;

  const prev = currentIndex > 0 ? pageOrder[currentIndex - 1] : null;
  const next = currentIndex < pageOrder.length - 1 ? pageOrder[currentIndex + 1] : null;

  if (!prev && !next) return null;

  return (
    <div className="mt-16 pt-8 border-t border-[var(--border)] grid grid-cols-2 gap-4">
      {prev ? (
        <Link
          href={prev.href}
          className="group flex items-center gap-3 px-4 py-3 rounded-lg border border-[var(--border)] hover:border-[var(--accent)] transition-colors"
        >
          <svg
            className="w-4 h-4 text-[var(--text-muted)] group-hover:text-[var(--accent)] transition-colors shrink-0"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          <div className="min-w-0">
            <div className="text-[11px] uppercase tracking-wider text-[var(--text-muted)]">Previous</div>
            <div className="text-sm font-medium text-[var(--text-secondary)] group-hover:text-[var(--accent)] transition-colors truncate">
              {prev.label}
            </div>
          </div>
        </Link>
      ) : (
        <div />
      )}

      {next ? (
        <Link
          href={next.href}
          className="group flex items-center justify-end gap-3 px-4 py-3 rounded-lg border border-[var(--border)] hover:border-[var(--accent)] transition-colors text-right"
        >
          <div className="min-w-0">
            <div className="text-[11px] uppercase tracking-wider text-[var(--text-muted)]">Next</div>
            <div className="text-sm font-medium text-[var(--text-secondary)] group-hover:text-[var(--accent)] transition-colors truncate">
              {next.label}
            </div>
          </div>
          <svg
            className="w-4 h-4 text-[var(--text-muted)] group-hover:text-[var(--accent)] transition-colors shrink-0"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
          </svg>
        </Link>
      ) : (
        <div />
      )}
    </div>
  );
}
