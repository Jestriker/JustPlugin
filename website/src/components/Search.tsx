"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { useRouter } from "next/navigation";
import { searchIndex, type SearchEntry } from "@/data/searchIndex";

const CATEGORY_ORDER = ["Command", "Permission", "Feature", "Page", "Config"] as const;

function groupResults(results: SearchEntry[]) {
  const groups: Record<string, SearchEntry[]> = {};
  for (const entry of results) {
    if (!groups[entry.category]) groups[entry.category] = [];
    groups[entry.category].push(entry);
  }
  const ordered: { category: string; entries: SearchEntry[] }[] = [];
  for (const cat of CATEGORY_ORDER) {
    if (groups[cat]) ordered.push({ category: cat + "s", entries: groups[cat] });
  }
  return ordered;
}

function search(query: string): SearchEntry[] {
  const q = query.toLowerCase().trim();
  if (!q) return [];
  const terms = q.split(/\s+/);
  const scored: { entry: SearchEntry; score: number }[] = [];

  for (const entry of searchIndex) {
    const titleLower = entry.title.toLowerCase();
    const descLower = entry.description.toLowerCase();
    const kwLower = (entry.keywords || "").toLowerCase();
    const blob = `${titleLower} ${descLower} ${kwLower} ${entry.category.toLowerCase()}`;

    let score = 0;
    let allMatch = true;
    for (const term of terms) {
      if (!blob.includes(term)) { allMatch = false; break; }
      if (titleLower === term) score += 10;
      else if (titleLower.startsWith(term) || titleLower.startsWith("/" + term)) score += 6;
      else if (titleLower.includes(term)) score += 4;
      else if (kwLower.includes(term)) score += 2;
      else score += 1;
    }
    if (allMatch) scored.push({ entry, score });
  }

  scored.sort((a, b) => b.score - a.score);
  return scored.slice(0, 20).map((s) => s.entry);
}

export function SearchTrigger({ onClick }: { onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-[var(--text-muted)] bg-[var(--bg-primary)] border border-[var(--border)] hover:border-[var(--accent)] hover:text-[var(--text-secondary)] transition-colors"
    >
      <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
      </svg>
      <span className="flex-1 text-left">Search...</span>
      <kbd className="hidden sm:inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded text-[10px] font-medium bg-[var(--bg-secondary)] border border-[var(--border)] text-[var(--text-muted)]">
        <span className="text-xs">&#8984;</span>K
      </kbd>
    </button>
  );
}

export default function Search({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [query, setQuery] = useState("");
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const router = useRouter();

  const results = search(query);
  const groups = groupResults(results);
  const flatResults = groups.flatMap((g) => g.entries);

  const navigate = useCallback(
    (href: string) => {
      onClose();
      setQuery("");
      setActiveIndex(0);
      router.push(href);
    },
    [onClose, router]
  );

  // Global keyboard shortcut
  useEffect(() => {
    function handleGlobal(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        if (open) onClose();
        else {
          // Parent must handle opening; this component only handles close
        }
      }
      if (e.key === "Escape" && open) {
        e.preventDefault();
        onClose();
        setQuery("");
        setActiveIndex(0);
      }
    }
    window.addEventListener("keydown", handleGlobal);
    return () => window.removeEventListener("keydown", handleGlobal);
  }, [open, onClose]);

  // Focus input on open
  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 10);
    }
  }, [open]);

  // Keyboard nav inside modal
  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveIndex((i) => Math.min(i + 1, flatResults.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveIndex((i) => Math.max(i - 1, 0));
    } else if (e.key === "Enter" && flatResults[activeIndex]) {
      e.preventDefault();
      navigate(flatResults[activeIndex].href);
    }
  }

  // Scroll active item into view
  useEffect(() => {
    const el = listRef.current?.querySelector(`[data-idx="${activeIndex}"]`);
    el?.scrollIntoView({ block: "nearest" });
  }, [activeIndex]);

  // Reset active index when results change
  useEffect(() => {
    setActiveIndex(0);
  }, [query]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-start justify-center pt-[15vh]">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      {/* Modal */}
      <div role="dialog" aria-modal="true" aria-label="Search documentation" className="relative w-full max-w-lg mx-4 bg-[var(--bg-secondary)] border border-[var(--border)] rounded-xl shadow-2xl overflow-hidden">
        {/* Search input */}
        <div className="flex items-center gap-3 px-4 border-b border-[var(--border)]">
          <svg className="w-5 h-5 text-[var(--text-muted)] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Search commands, features, pages..."
            aria-label="Search documentation"
            className="flex-1 bg-transparent py-3.5 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none"
          />
          <kbd
            className="px-1.5 py-0.5 rounded text-[10px] font-medium bg-[var(--bg-primary)] border border-[var(--border)] text-[var(--text-muted)] cursor-pointer"
            onClick={onClose}
          >
            ESC
          </kbd>
        </div>

        {/* Results */}
        <div ref={listRef} aria-live="polite" className="max-h-80 overflow-y-auto p-2">
          {query && flatResults.length === 0 && (
            <div className="px-3 py-8 text-center text-sm text-[var(--text-muted)]">
              No results for &ldquo;{query}&rdquo;
            </div>
          )}

          {!query && (
            <div className="px-3 py-8 text-center text-sm text-[var(--text-muted)]">
              Type to search documentation...
            </div>
          )}

          {groups.map((group) => {
            return (
              <div key={group.category} className="mb-2">
                <div className="px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-[var(--text-muted)]">
                  {group.category}
                </div>
                {group.entries.map((entry) => {
                  const idx = flatResults.indexOf(entry);
                  const isActive = idx === activeIndex;
                  return (
                    <button
                      key={entry.title + entry.href}
                      data-idx={idx}
                      onClick={() => navigate(entry.href)}
                      onMouseEnter={() => setActiveIndex(idx)}
                      className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-left text-sm transition-colors ${
                        isActive
                          ? "bg-[var(--accent)] text-white"
                          : "text-[var(--text-secondary)] hover:bg-[var(--bg-hover)]"
                      }`}
                    >
                      <span className="font-medium truncate">{entry.title}</span>
                      <span className={`ml-auto text-xs truncate ${isActive ? "text-white/70" : "text-[var(--text-muted)]"}`}>
                        {entry.description}
                      </span>
                    </button>
                  );
                })}
              </div>
            );
          })}
        </div>

        {/* Footer hint */}
        <div className="px-4 py-2 border-t border-[var(--border)] flex items-center gap-4 text-[10px] text-[var(--text-muted)]">
          <span className="flex items-center gap-1">
            <kbd className="px-1 py-0.5 rounded bg-[var(--bg-primary)] border border-[var(--border)]">&uarr;</kbd>
            <kbd className="px-1 py-0.5 rounded bg-[var(--bg-primary)] border border-[var(--border)]">&darr;</kbd>
            navigate
          </span>
          <span className="flex items-center gap-1">
            <kbd className="px-1 py-0.5 rounded bg-[var(--bg-primary)] border border-[var(--border)]">&crarr;</kbd>
            open
          </span>
          <span className="flex items-center gap-1">
            <kbd className="px-1 py-0.5 rounded bg-[var(--bg-primary)] border border-[var(--border)]">esc</kbd>
            close
          </span>
        </div>
      </div>
    </div>
  );
}
