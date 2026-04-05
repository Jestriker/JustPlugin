"use client";

import { useState, useCallback } from "react";

interface CopyTextProps {
  text: string;
  children?: React.ReactNode;
}

export default function CopyText({ text, children }: CopyTextProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }, [text]);

  return (
    <span
      onClick={handleCopy}
      className="relative inline-flex items-center gap-1 cursor-pointer group font-mono text-[0.85em] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded"
      title="Click to copy"
    >
      {children ?? text}
      <svg
        className="w-3 h-3 text-[var(--text-muted)] opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
        />
      </svg>
      {copied && (
        <span className="absolute -top-7 left-1/2 -translate-x-1/2 text-[10px] bg-[var(--bg-secondary)] border border-[var(--border)] text-[var(--green)] px-1.5 py-0.5 rounded shadow-lg whitespace-nowrap z-50">
          Copied!
        </span>
      )}
    </span>
  );
}
