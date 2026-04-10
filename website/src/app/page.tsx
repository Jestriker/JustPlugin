"use client";

/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { useEffect, useState } from "react";
import MarketingNav from "@/components/MarketingNav";
import { PLUGIN_VERSION } from "@/data/constants";

// Checks a shields.io badge image for #9f9f9f (gray = "Widget Disabled" / error state)
// If found, returns false (hide). If the badge has real data, returns true (show).
function DiscordBadge({ url, alt }: { url: string; alt: string }) {
  const [show, setShow] = useState(true); // optimistic — show while checking

  useEffect(() => {
    const img = new Image();
    img.crossOrigin = "anonymous";
    img.onload = () => {
      try {
        const canvas = document.createElement("canvas");
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;
        ctx.drawImage(img, 0, 0);
        const data = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
        // Scan for #9f9f9f (159, 159, 159) — the gray color used in error/disabled badges
        for (let i = 0; i < data.length; i += 4) {
          const r = data[i], g = data[i + 1], b = data[i + 2];
          if (r === 159 && g === 159 && b === 159) {
            setShow(false);
            return;
          }
        }
        setShow(true);
      } catch {
        // CORS or canvas error — keep showing
        setShow(true);
      }
    };
    img.onerror = () => setShow(false);
    img.src = url;
  }, [url]);

  if (!show) return null;
  return <img src={url} alt={alt} className="h-8" />;
}

function AnimatedVersion({ versions }: { versions: string[] }) {
  const [index, setIndex] = useState(0);
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (versions.length <= 1) { setDone(true); return; }
    let i = 0;
    const interval = setInterval(() => {
      i++;
      if (i >= versions.length - 1) {
        setIndex(versions.length - 1);
        setDone(true);
        clearInterval(interval);
      } else {
        setIndex(i);
      }
    }, 400);
    return () => clearInterval(interval);
  }, [versions]);

  return <span className={done ? "" : "opacity-80"}>v{versions[index]}</span>;
}

function AnimatedCounter({ target, suffix = "" }: { target: number; suffix?: string }) {
  const [count, setCount] = useState(0);
  useEffect(() => {
    let frame: number;
    const duration = 1500;
    const start = performance.now();
    function step(now: number) {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setCount(Math.floor(eased * target));
      if (progress < 1) frame = requestAnimationFrame(step);
    }
    frame = requestAnimationFrame(step);
    return () => cancelAnimationFrame(frame);
  }, [target]);
  return <>{count}{suffix}</>;
}

const featureCards = [
  {
    title: "Economy",
    desc: "Full balance system, Vault integration, PayNotes, Baltop GUI, transaction history with 6 types.",
    href: "/features/economy",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    title: "Teleportation",
    desc: "TPA, random TP with dimension GUI, safe teleport protection, warmup countdown, per-command cooldowns.",
    href: "/features/teleportation",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
      </svg>
    ),
  },
  {
    title: "Moderation",
    desc: "Bans, IP bans with CIDR, mutes, progressive warnings, kick, sudo, invsee, punishment escalation.",
    href: "/features/moderation",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
      </svg>
    ),
  },
  {
    title: "Kits & Items",
    desc: "Create and distribute item kits with cooldowns, GUI selection, auto-equip armor, lifecycle management.",
    href: "/features/kits",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
      </svg>
    ),
  },
  {
    title: "Teams & Chat",
    desc: "Team creation, team chat, shared homes. Private messaging, hover stats, announcements, ignore system.",
    href: "/features/teams",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
      </svg>
    ),
  },
  {
    title: "Scoreboard & Tab",
    desc: "50+ placeholders, animated wave gradient title, 5 text animation types, conditional lines, tab list.",
    href: "/features/scoreboard",
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25H12" />
      </svg>
    ),
  },
];

const replacedPlugins = [
  "EssentialsX", "Vault Economy", "TeleportPlugin", "SetHome", "BanManager",
  "ChatManager", "KitPlugin", "AFKPlus", "Scoreboard", "TabList", "VanishPlugin", "TradePlugin",
];

export default function Home() {
  return (
    <div className="min-h-screen">
      <MarketingNav />

      {/* Hero */}
      <main>
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <img src="/plugins-image.png" alt="JustPlugin - All-in-one Minecraft server management plugin for Paper, Purpur, and Folia" width={96} height={96} className="mx-auto mb-6 rounded-2xl shadow-lg shadow-[var(--accent)]/20" />
          <h1 className="text-5xl md:text-6xl font-bold leading-tight mb-6">
            The Only Server Plugin<br />
            <span data-glow-text="" className="text-[var(--accent)]">You&apos;ll Ever Need</span>
          </h1>
          <p className="text-xl text-[var(--text-secondary)] max-w-2xl mx-auto mb-10 leading-relaxed">
            Replace 12+ plugins with a single, lightweight JAR. Economy, teleportation, moderation, kits, and 200+ commands &mdash; built from scratch for modern Minecraft servers.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <a
              href="https://modrinth.com/plugin/justplugin"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:opacity-90 transition-opacity"
            >
              <img src="/modrinth-badge.svg" alt="Download on Modrinth" width={200} height={68} className="h-14" />
            </a>
            <Link
              href="/commands"
              className="px-8 py-3.5 rounded-xl bg-[var(--bg-card)] border border-[var(--border)] font-semibold text-lg hover:border-[var(--accent)] transition-colors"
            >
              Get Started
            </Link>
          </div>

          {/* Stats */}
          <div className="flex flex-wrap justify-center gap-12 mt-16">
            <div className="text-center">
              <div className="text-4xl font-bold text-[var(--accent)]"><AnimatedCounter target={200} suffix="+" /></div>
              <div className="text-sm text-[var(--text-muted)] mt-1">Commands</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-[var(--accent)]"><AnimatedCounter target={150} suffix="+" /></div>
              <div className="text-sm text-[var(--text-muted)] mt-1">Permissions</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-[var(--accent)]"><AnimatedCounter target={50} suffix="+" /></div>
              <div className="text-sm text-[var(--text-muted)] mt-1">Placeholders</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-[var(--accent)]"><AnimatedVersion versions={["1.0", "1.1", "1.2", PLUGIN_VERSION]} /></div>
              <div className="text-sm text-[var(--text-muted)] mt-1">Current Version</div>
            </div>
          </div>
        </div>
      </section>

      {/* Replace your plugin stack */}
      <section className="py-20 px-6 bg-[var(--bg-secondary)]">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-4">Replace Your Plugin Stack</h2>
          <p className="text-center text-[var(--text-secondary)] mb-12 max-w-xl mx-auto">
            Stop managing 15 plugins. Stop debugging version conflicts. Stop reading 15 different wikis.
          </p>

          <div className="flex flex-col md:flex-row items-stretch gap-4 md:gap-0">
            {/* Before */}
            <div className="flex-1 bg-[var(--bg-primary)] rounded-xl border border-red-500/30 p-6 flex flex-col">
              <div className="text-sm font-semibold text-red-400 mb-4 flex items-center gap-2">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                Before: 12+ separate JARs
              </div>
              <div className="flex flex-wrap gap-2 flex-1">
                {replacedPlugins.map((p) => (
                  <span key={p} className="px-2.5 py-1 rounded-md bg-red-500/10 text-red-400 text-xs font-mono h-fit">{p}</span>
                ))}
              </div>
            </div>

            {/* Arrow */}
            <div className="flex items-center justify-center px-4 py-2 md:py-0">
              {/* Right arrow on desktop, down arrow on mobile */}
              <svg className="hidden md:block w-8 h-8 text-[var(--text-muted)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
              <svg className="md:hidden w-8 h-8 text-[var(--text-muted)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 13.5L12 21m0 0l-7.5-7.5M12 21V3" />
              </svg>
            </div>

            {/* After */}
            <div className="flex-1 bg-[var(--bg-primary)] rounded-xl border border-green-500/30 p-6 flex flex-col">
              <div className="text-sm font-semibold text-green-400 mb-4 flex items-center gap-2">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
                After: 1 JAR
              </div>
              <div className="flex items-center gap-3 flex-1">
                <span data-glow-box="" className="px-4 py-2.5 rounded-lg bg-[var(--accent)]/15 text-[var(--accent)] text-lg font-mono font-bold">JustPlugin.jar</span>
                <span className="text-sm text-[var(--text-muted)]">That&apos;s it.</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Key differentiators */}
      <section className="py-20 px-6">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-12">Why <span data-glow-text="" className="text-[var(--accent)]">JustPlugin</span>?</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 hover:border-[var(--accent)] transition-colors">
              <div className="w-10 h-10 rounded-lg bg-[var(--accent)]/15 flex items-center justify-center mb-4">
                <svg className="w-5 h-5 text-[var(--accent)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
              </div>
              <h3 className="font-bold text-lg mb-2">Folia Native</h3>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">The only essentials plugin with native Folia support. No forks, no wrappers &mdash; built from the ground up for regionized multithreading.</p>
            </div>
            <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 hover:border-[var(--accent)] transition-colors">
              <div className="w-10 h-10 rounded-lg bg-[var(--accent)]/15 flex items-center justify-center mb-4">
                <svg className="w-5 h-5 text-[var(--accent)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M10.5 6h9.75M10.5 6a1.5 1.5 0 11-3 0m3 0a1.5 1.5 0 10-3 0M3.75 6H7.5m3 12h9.75m-9.75 0a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m-3.75 0H7.5m9-6h3.75m-3.75 0a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m-9.75 0h9.75" /></svg>
              </div>
              <h3 className="font-bold text-lg mb-2">Configure Everything</h3>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">200+ individually toggleable commands, a web-based config editor, and granular permissions. Enable only what you need.</p>
            </div>
            <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 hover:border-[var(--accent)] transition-colors">
              <div className="w-10 h-10 rounded-lg bg-[var(--accent)]/15 flex items-center justify-center mb-4">
                <svg className="w-5 h-5 text-[var(--accent)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" /></svg>
              </div>
              <h3 className="font-bold text-lg mb-2">Modern & Lightweight</h3>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">Java 21, Paper API, MiniMessage formatting. No legacy baggage, no deprecated APIs. Clean, async, and performance-conscious.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Feature grid */}
      <section id="features" className="py-20 px-6 bg-[var(--bg-secondary)]">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-4">Feature Highlights</h2>
          <p className="text-center text-[var(--text-secondary)] mb-12">Everything you need to run a server, in one plugin.</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {featureCards.map((f) => (
              <Link
                key={f.href}
                href={f.href}
                className="group bg-[var(--bg-primary)] rounded-xl border border-[var(--border)] p-5 hover:border-[var(--accent)] transition-all hover:shadow-lg hover:shadow-[var(--accent)]/5"
              >
                <div className="text-[var(--accent)] mb-3">{f.icon}</div>
                <h3 className="font-semibold mb-1.5 group-hover:text-[var(--accent-hover)] transition-colors">{f.title}</h3>
                <p className="text-sm text-[var(--text-muted)] leading-relaxed">{f.desc}</p>
                <span className="inline-block mt-3 text-xs text-[var(--accent)] font-medium">Learn more &rarr;</span>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Social proof */}
      <section className="py-16 px-6">
        <div className="max-w-3xl mx-auto flex flex-wrap justify-center gap-4">
          <img src="https://img.shields.io/bstats/servers/30446?style=for-the-badge&color=f97316&label=Servers" alt="bStats Servers" className="h-8" />
          <DiscordBadge url="https://img.shields.io/discord/1482382694443913249?style=for-the-badge&color=5865F2&label=Discord" alt="Discord" />
          <img src="https://img.shields.io/github/stars/Jestriker/JustPlugin?style=for-the-badge&color=f97316&label=Stars" alt="GitHub Stars" className="h-8" />
          <img src={`https://img.shields.io/badge/License-MIT-f97316?style=for-the-badge`} alt="MIT License" className="h-8" />
        </div>
      </section>

      </main>

      {/* Footer */}
      <footer className="border-t border-[var(--border)] py-12 px-6">
        <div className="max-w-5xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="text-sm text-[var(--text-muted)]">
            Built with Java 21 for Paper, Purpur, and Folia &mdash; MIT Licensed
          </div>
          <div className="flex items-center gap-6 text-sm">
            <Link href="https://discord.gg/QCArmUbaJ8" target="_blank" rel="noopener noreferrer" className="text-[var(--text-secondary)] hover:text-[var(--text-primary)]">Discord</Link>
            <Link href="https://github.com/Jestriker/JustPlugin" target="_blank" rel="noopener noreferrer" className="text-[var(--text-secondary)] hover:text-[var(--text-primary)]">GitHub</Link>
            <Link href="https://modrinth.com/plugin/justplugin" target="_blank" rel="noopener noreferrer" className="text-[var(--text-secondary)] hover:text-[var(--text-primary)]">Modrinth</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
