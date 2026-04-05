"use client";

import { useEffect, useState } from "react";

export default function HomeSearch() {
  const [isMac, setIsMac] = useState(false);

  useEffect(() => {
    setIsMac(navigator.platform.toUpperCase().includes("MAC"));
  }, []);

  const triggerSearch = () => {
    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "k", metaKey: true, ctrlKey: true, bubbles: true })
    );
  };

  return (
    <button
      onClick={triggerSearch}
      className="w-full mb-10 flex items-center gap-3 px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--bg-card)] hover:border-[var(--accent)] hover:bg-[var(--bg-hover)] transition-all text-left group"
    >
      <svg
        className="w-5 h-5 text-[var(--text-muted)] group-hover:text-[var(--accent)] transition-colors flex-shrink-0"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
      <span className="text-[var(--text-muted)] text-sm flex-1">
        Search commands, permissions, features...
      </span>
      <kbd className="hidden sm:inline-flex items-center gap-0.5 px-2 py-0.5 rounded bg-[var(--bg-tertiary)] border border-[var(--border)] text-[10px] text-[var(--text-muted)] font-mono">
        {isMac ? "⌘" : "Ctrl"}+K
      </kbd>
    </button>
  );
}
