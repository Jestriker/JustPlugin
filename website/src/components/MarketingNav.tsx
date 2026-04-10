"use client";

import { useState, useCallback } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import ThemeToggle from "./ThemeToggle";

/* eslint-disable @next/next/no-img-element */

const navLinks = [
  { label: "Home", href: "/" },
  { label: "Features", href: "/features" },
  // { label: "Demo", href: "/demo" }, // Hidden — WIP, re-enable when ready
  { label: "Commands", href: "/commands" },
  { label: "Modrinth", href: "https://modrinth.com/plugin/justplugin", external: true },
  { label: "Contact", href: "/contact" },
];

export default function MarketingNav({ hideHome }: { hideHome?: boolean } = {}) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const pathname = usePathname();

  const handleClick = useCallback((e: React.MouseEvent<HTMLAnchorElement>, href: string) => {
    // Smooth scroll for hash links on the same page
    if (href.startsWith("#")) {
      e.preventDefault();
      const el = document.querySelector(href);
      if (el) {
        el.scrollIntoView({ behavior: "smooth", block: "start" });
      }
      setMobileOpen(false);
    }
  }, []);

  const isActive = (href: string) => {
    if (href.startsWith("#") || href.startsWith("http")) return false;
    if (href === "/") return pathname === "/";
    return pathname === href || pathname.startsWith(href + "/");
  };

  const visibleLinks = hideHome ? navLinks.filter(l => l.label !== "Home") : navLinks;

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-[var(--bg-primary)]/80 backdrop-blur-lg border-b border-[var(--border)]">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-2.5">
          <img src="/plugins-image.png" alt="JustPlugin logo" width={32} height={32} className="rounded-lg" />
          <span className="font-bold text-lg">JustPlugin</span>
        </Link>

        {/* Desktop nav */}
        <nav className="hidden md:flex items-center gap-8">
          {visibleLinks.map((link) => (
            <Link
              key={link.label}
              href={link.href}
              onClick={(e) => handleClick(e, link.href)}
              {...(link.external ? { target: "_blank", rel: "noopener noreferrer" } : {})}
              className={`text-sm transition-colors ${
                isActive(link.href)
                  ? "text-[var(--accent)] font-medium"
                  : "text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
              }`}
            >
              {link.label}
            </Link>
          ))}
          <ThemeToggle />
          <Link
            href="/commands"
            data-glow-box=""
            className="px-4 py-2 rounded-lg bg-[var(--accent)] text-white text-sm font-medium hover:bg-[var(--accent-hover)] transition-colors"
          >
            Get Started
          </Link>
        </nav>

        {/* Mobile menu button */}
        <div className="flex md:hidden items-center gap-2">
          <ThemeToggle />
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label="Toggle menu"
            className="p-2 rounded-lg text-[var(--text-secondary)] hover:bg-[var(--bg-hover)]"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              {mobileOpen ? (
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              ) : (
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
              )}
            </svg>
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <nav className="md:hidden border-t border-[var(--border)] bg-[var(--bg-primary)] px-6 py-4 space-y-3">
          {visibleLinks.map((link) => (
            <Link
              key={link.label}
              href={link.href}
              onClick={(e) => { handleClick(e, link.href); setMobileOpen(false); }}
              className={`block text-sm ${
                isActive(link.href)
                  ? "text-[var(--accent)] font-medium"
                  : "text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
              }`}
            >
              {link.label}
            </Link>
          ))}
          <Link
            href="/commands"
            className="block w-full text-center px-4 py-2 rounded-lg bg-[var(--accent)] text-white text-sm font-medium"
          >
            Get Started
          </Link>
        </nav>
      )}
    </header>
  );
}
