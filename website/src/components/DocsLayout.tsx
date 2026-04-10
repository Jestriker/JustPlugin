"use client";

/* eslint-disable @next/next/no-img-element */
import { useState, useCallback, useEffect } from "react";
import { usePathname } from "next/navigation";
import Sidebar from "./Sidebar";
import TableOfContents from "./TableOfContents";
import Breadcrumbs from "./Breadcrumbs";
import PrevNext from "./PrevNext";
import Search from "./Search";
import MarketingNav from "./MarketingNav";

export default function DocsLayout({ children }: { children: React.ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const pathname = usePathname();
  const isRoot = pathname === "/";

  const openSearch = useCallback(() => setSearchOpen(true), []);
  const closeSearch = useCallback(() => setSearchOpen(false), []);

  // Global Cmd+K / Ctrl+K handler
  useEffect(() => {
    function handleKey(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setSearchOpen((prev) => !prev);
      }
    }
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, []);

  const editUrl = `https://github.com/Jestriker/JustPlugin/tree/master/website/src/app${pathname}/page.tsx`;

  // Marketing/demo pages - render children directly without docs layout
  if (isRoot || pathname === "/contact") {
    return <>{children}</>;
  }

  return (
    <div className="min-h-screen">
      <a href="#main-content" className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-[100] focus:px-4 focus:py-2 focus:bg-[var(--accent)] focus:text-white focus:rounded-md focus:outline-none">
        Skip to content
      </a>

      {/* Top navbar — shared with marketing site, no Home link in wiki */}
      <MarketingNav />

      <Sidebar mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} onSearchOpen={openSearch} />

      {/* Top bar (mobile — sidebar toggle, below navbar) */}
      <header className="lg:hidden fixed top-16 left-0 right-0 z-30 h-12 bg-[var(--bg-secondary)] border-b border-[var(--border)] flex items-center px-4 gap-3">
        <button
          onClick={() => setMobileOpen(true)}
          aria-label="Open menu"
          className="p-1.5 rounded-md text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)]"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <div className="flex items-center gap-2">
          <span className="font-semibold text-sm text-[var(--text-muted)]">Docs</span>
        </div>
      </header>

      {/* Main content — offset for navbar (h-16) + mobile sidebar bar (h-12) */}
      <main id="main-content" role="main" aria-label="Documentation content" className="lg:pl-72 pt-28 lg:pt-16">
        <div className="flex justify-center gap-8 px-6 py-10 lg:py-12">
          <div className="max-w-5xl w-full min-w-0">
            <Breadcrumbs />
            {children}

            <PrevNext />

            {/* Edit on GitHub link */}
            {!isRoot && (
              <div className="mt-12 pt-6 border-t border-[var(--border)]">
                <a
                  href={editUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 text-xs text-[var(--text-muted)] hover:text-[var(--text-secondary)] transition-colors"
                >
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                  Edit this page on GitHub
                </a>
              </div>
            )}
          </div>

          <TableOfContents />
        </div>
      </main>
      {/* Search modal */}
      <Search open={searchOpen} onClose={closeSearch} />
    </div>
  );
}
