"use client";

import { useState, useCallback } from "react";
import dynamic from "next/dynamic";
import MarketingNav from "@/components/MarketingNav";

const MinecraftRagdoll = dynamic(() => import("@/components/MinecraftRagdoll"), { ssr: false });

export default function DemoPage() {
  const [username, setUsername] = useState("LiamWho");
  const [activeUsername, setActiveUsername] = useState("LiamWho");
  const [inputFocused, setInputFocused] = useState(false);

  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    if (username.trim()) {
      setActiveUsername(username.trim());
    }
  }, [username]);

  return (
    <div className="min-h-screen relative overflow-hidden">
      <MarketingNav />

      {/* 3D skin viewer — full screen background */}
      <MinecraftRagdoll
        key={activeUsername}
        username={activeUsername}
        className="absolute inset-0 w-full h-full z-0"
      />

      {/* UI overlay */}
      <div className="relative z-10 pt-24 px-6 pointer-events-none">
        <div className="max-w-sm mx-auto text-center">
          <p className="text-[var(--text-secondary)] text-sm mb-4 pointer-events-auto">
            Grab your skin and fling it around!
          </p>

          {/* Username input */}
          <form onSubmit={handleSubmit} className="pointer-events-auto">
            <div className={`flex gap-2 bg-[var(--bg-card)]/90 backdrop-blur-md rounded-xl border p-2 transition-colors ${
              inputFocused ? "border-[var(--accent)]" : "border-[var(--border)]"
            }`}>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                onFocus={() => setInputFocused(true)}
                onBlur={() => setInputFocused(false)}
                placeholder="Minecraft username..."
                className="flex-1 bg-transparent px-3 py-2 text-sm outline-none text-[var(--text-primary)] placeholder:text-[var(--text-muted)]"
              />
              <button
                type="submit"
                className="px-5 py-2 rounded-lg bg-[var(--accent)] text-white text-sm font-medium hover:bg-[var(--accent-hover)] transition-colors"
              >
                Load Skin
              </button>
            </div>
          </form>

          <p className="text-xs text-[var(--text-muted)] mt-2 pointer-events-auto">
            Showing: <span className="text-[var(--accent)] font-medium">{activeUsername}</span>
          </p>
        </div>
      </div>
    </div>
  );
}
