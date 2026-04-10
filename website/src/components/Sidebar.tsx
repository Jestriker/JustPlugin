"use client";

/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import ThemeToggle from "./ThemeToggle";
import { SearchTrigger } from "./Search";
import { PLUGIN_VERSION } from "@/data/constants";

import React from "react";

interface NavItem {
  label: string;
  href?: string;
  children?: NavItem[];
  icon?: React.ReactNode;
}

const ic = "w-4 h-4 flex-shrink-0";

const navigation: NavItem[] = [
  { label: "Features", href: "/features", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" /></svg>, children: [
    { label: "Economy", href: "/features/economy" },
    { label: "Teleportation", href: "/features/teleportation" },
    { label: "Warps & Homes", href: "/features/warps-and-homes" },
    { label: "Moderation", href: "/features/moderation" },
    { label: "Jail System", href: "/features/jail" },
    { label: "Kit System", href: "/features/kits" },
    { label: "Vanish", href: "/features/vanish" },
    { label: "Teams", href: "/features/teams" },
    { label: "Trading", href: "/features/trading" },
    { label: "Skin Restorer", href: "/features/skins" },
    { label: "Maintenance Mode", href: "/features/maintenance" },
    { label: "Scoreboard", href: "/features/scoreboard" },
    { label: "Tab List", href: "/features/tab-list" },
    { label: "MOTD Profiles", href: "/features/motd" },
    { label: "Chat & Messaging", href: "/features/chat" },
    { label: "Mail System", href: "/features/mail" },
    { label: "Nicknames & Tags", href: "/features/nicknames-tags" },
    { label: "AFK System", href: "/features/afk" },
    { label: "Automated Messages", href: "/features/automated-messages" },
    { label: "Virtual Inventories", href: "/features/virtual-inventories" },
    { label: "World Management", href: "/features/world-management" },
    { label: "Backup & Export", href: "/features/backup" },
  ]},
  { label: "Commands", href: "/commands", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M6.75 7.5l3 2.25-3 2.25m4.5 0h3m-9 8.25h13.5A2.25 2.25 0 0021 18V6a2.25 2.25 0 00-2.25-2.25H5.25A2.25 2.25 0 003 6v12a2.25 2.25 0 002.25 2.25z" /></svg> },
  { label: "Permissions", href: "/permissions", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z" /></svg> },
  { label: "Configuration", href: "/configuration", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 011.37.49l1.296 2.247a1.125 1.125 0 01-.26 1.431l-1.003.827c-.293.24-.438.613-.431.992a6.759 6.759 0 010 .255c-.007.378.138.75.43.99l1.005.828c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 01-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 01-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.941-1.11.941h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 01-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 01-1.369-.49l-1.297-2.247a1.125 1.125 0 01.26-1.431l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 010-.255c.007-.378-.138-.75-.43-.99l-1.004-.828a1.125 1.125 0 01-.26-1.43l1.297-2.247a1.125 1.125 0 011.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.281z" /><path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg> },
  { label: "Placeholders", href: "/placeholders", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M17.25 6.75L22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3l-4.5 16.5" /></svg> },
  { label: "Formatting", href: "/formatting", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9.53 16.122a3 3 0 00-5.78 1.128 2.25 2.25 0 01-2.4 2.245 4.5 4.5 0 008.4-2.245c0-.399-.078-.78-.22-1.128zm0 0a15.998 15.998 0 003.388-1.62m-5.043-.025a15.994 15.994 0 011.622-3.395m3.42 3.42a15.995 15.995 0 004.764-4.648l3.876-5.814a1.151 1.151 0 00-1.597-1.597L14.146 6.32a15.996 15.996 0 00-4.649 4.763m3.42 3.42a6.776 6.776 0 00-3.42-3.42" /></svg> },
  { label: "API Reference", href: "/api", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M14.25 9.75L16.5 12l-2.25 2.25m-4.5 0L7.5 12l2.25-2.25M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z" /></svg> },
  { label: "Version Support", href: "/version-support", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" /></svg> },
  { label: "FAQ", href: "/faq", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9 5.25h.008v.008H12v-.008z" /></svg> },
  { label: "Changelog", href: "/changelog", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" /></svg> },
  { label: "Migration Guide", href: "/migration", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M7.5 21L3 16.5m0 0L7.5 12M3 16.5h13.5m0-13.5L21 7.5m0 0L16.5 12M21 7.5H7.5" /></svg> },
  { label: "Troubleshooting", href: "/troubleshooting", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M11.42 15.17l-5.1 5.1a2.121 2.121 0 11-3-3l5.1-5.1m0 0L15.17 4.42a2.121 2.121 0 113 3l-7.75 7.75z" /></svg> },
  { label: "Comparison", href: "/comparison", icon: <svg className={ic} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" /></svg> },
];

function ChevronIcon({ open }: { open: boolean }) {
  return (
    <svg
      className={`w-3.5 h-3.5 transition-transform duration-200 ${open ? "rotate-90" : ""}`}
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
      strokeWidth={2.5}
    >
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
    </svg>
  );
}

function NavSection({ item }: { item: NavItem }) {
  const pathname = usePathname();
  const isActive = item.href === pathname;
  const hasActiveChild = item.children?.some(
    (c) => c.href === pathname
  );
  const [open, setOpen] = useState(hasActiveChild || isActive);

  if (!item.children) {
    return (
      <Link
        href={item.href || "/"}
        className={`flex items-center gap-2.5 px-3 py-1.5 rounded-md text-sm transition-colors ${
          isActive
            ? "bg-[var(--accent-dim)] text-[var(--accent-hover)] font-medium"
            : "text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)]"
        }`}
      >
        {item.icon && <span className={isActive ? "text-[var(--accent)]" : "text-[var(--text-muted)]"}>{item.icon}</span>}
        {item.label}
      </Link>
    );
  }

  return (
    <div>
      <div className="flex items-center">
        <Link
          href={item.href || "/"}
          className={`flex-1 flex items-center gap-2.5 px-3 py-1.5 rounded-md text-sm transition-colors ${
            isActive
              ? "bg-[var(--accent-dim)] text-[var(--accent-hover)] font-medium"
              : "text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)]"
          }`}
        >
          {item.icon && <span className={isActive || hasActiveChild ? "text-[var(--accent)]" : "text-[var(--text-muted)]"}>{item.icon}</span>}
          {item.label}
        </Link>
        <button
          onClick={() => setOpen(!open)}
          aria-label="Toggle section"
          aria-expanded={open}
          className="p-1.5 rounded text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)] transition-colors"
        >
          <ChevronIcon open={open} />
        </button>
      </div>
      {open && (
        <div className="ml-3 mt-0.5 pl-3 border-l border-[var(--border)] space-y-0.5">
          {item.children.map((child) => {
            const childActive = child.href === pathname;
            return (
              <Link
                key={child.href}
                href={child.href || "/"}
                className={`block px-3 py-1 rounded-md text-sm transition-colors ${
                  childActive
                    ? "text-[var(--accent-hover)] font-medium"
                    : "text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)]"
                }`}
              >
                {child.label}
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default function Sidebar({ mobileOpen, onClose, onSearchOpen }: { mobileOpen: boolean; onClose: () => void; onSearchOpen?: () => void }) {
  return (
    <>
      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={onClose} />
      )}

      <aside
        role={mobileOpen ? "dialog" : undefined}
        aria-modal={mobileOpen ? "true" : undefined}
        aria-label={mobileOpen ? "Navigation menu" : undefined}
        className={`fixed top-0 left-0 z-50 w-72 bg-[var(--bg-secondary)] border-r border-[var(--border)] overflow-y-auto transition-transform duration-300 lg:translate-x-0 lg:z-30 lg:top-16 h-full lg:h-[calc(100vh-4rem)] ${
          mobileOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Logo */}
        <div className="sticky top-0 bg-[var(--bg-secondary)] border-b border-[var(--border)] px-5 py-4 flex items-center gap-3">
          <img src="/plugins-image.png" alt="JustPlugin" width={32} height={32} className="rounded-lg" />
          <div>
            <div className="font-semibold text-sm">JustPlugin</div>
            <div className="text-xs text-[var(--text-muted)] flex items-center gap-1.5">
              v{PLUGIN_VERSION} <span className="px-1.5 py-0.5 rounded bg-[var(--green)] text-white text-[9px] font-bold leading-none uppercase">Latest</span>
            </div>
          </div>
          <div className="ml-auto flex items-center gap-1">
            <ThemeToggle />
            <button
              onClick={onClose}
              aria-label="Close menu"
              className="lg:hidden p-1 rounded text-[var(--text-muted)] hover:text-[var(--text-primary)]"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Search button */}
        <div className="px-3 pt-3">
          <SearchTrigger onClick={() => onSearchOpen?.()} />
        </div>

        {/* Nav */}
        <nav aria-label="Documentation navigation" className="p-3 space-y-1">
          {navigation.map((item) => (
            <NavSection key={item.label} item={item} />
          ))}
        </nav>

        {/* Modrinth badge */}
        <div className="px-5 mt-2 flex justify-center">
          <a href="https://modrinth.com/plugin/justplugin" target="_blank" rel="noopener noreferrer" className="hover:opacity-90 transition-opacity">
            <img src="/modrinth-badge.svg" alt="Available on Modrinth" width={140} height={48} className="h-10 rounded" />
          </a>
        </div>

        {/* Footer links */}
        <div className="border-t border-[var(--border)] mx-3 mt-4 pt-4 pb-6 space-y-1">
          <a
            href="https://github.com/Jestriker/JustPlugin"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)] transition-colors"
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.73.083-.73 1.205.085 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12c0-6.63-5.37-12-12-12z"/></svg>
            GitHub
          </a>
          <a
            href="https://discord.gg/QCArmUbaJ8"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)] transition-colors"
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/></svg>
            Discord
          </a>
        </div>
      </aside>
    </>
  );
}
