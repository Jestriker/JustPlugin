"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import Link from "next/link";
import MarketingNav from "@/components/MarketingNav";

// Cloudflare Turnstile site key — replace with your real key
// Get one free at: https://dash.cloudflare.com/?to=/:account/turnstile
const TURNSTILE_SITE_KEY = process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY || "";

const categories = [
  "Bug Report",
  "Feature Request",
  "General Question",
  "API / Developer",
  "Configuration Help",
  "Other",
];

type FormState = "idle" | "sending" | "sent" | "error";

export default function ContactPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [discord, setDiscord] = useState("");
  const [category, setCategory] = useState("");
  const [subject, setSubject] = useState("");
  const [description, setDescription] = useState("");
  const [turnstileToken, setTurnstileToken] = useState("");
  const [formState, setFormState] = useState<FormState>("idle");
  const [errorMsg, setErrorMsg] = useState("");
  const turnstileRef = useRef<HTMLDivElement>(null);

  // Load Turnstile script and render widget
  useEffect(() => {
    if (!TURNSTILE_SITE_KEY) return;

    // Load script if not already loaded
    const existingScript = document.getElementById("cf-turnstile-script");
    if (!existingScript) {
      const script = document.createElement("script");
      script.id = "cf-turnstile-script";
      script.src = "https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit";
      script.async = true;
      document.head.appendChild(script);
    }

    // Poll for turnstile to be ready, then render
    const interval = setInterval(() => {
      const w = window as unknown as Record<string, unknown>;
      if (w.turnstile && turnstileRef.current && !turnstileRef.current.hasChildNodes()) {
        const ts = w.turnstile as { render: (el: HTMLElement, opts: Record<string, unknown>) => string };
        ts.render(turnstileRef.current, {
          sitekey: TURNSTILE_SITE_KEY,
          theme: "dark",
          callback: (token: unknown) => setTurnstileToken(token as string),
          "expired-callback": () => setTurnstileToken(""),
          "error-callback": () => setTurnstileToken(""),
        });
        clearInterval(interval);
      }
    }, 200);

    return () => clearInterval(interval);
  }, []);

  const resetTurnstile = useCallback(() => {
    const w = window as unknown as Record<string, unknown>;
    if (w.turnstile && turnstileRef.current) {
      const ts = w.turnstile as { reset: (el: HTMLElement) => void };
      ts.reset(turnstileRef.current);
    }
    setTurnstileToken("");
  }, []);

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim() || !email.trim() || !category || !subject.trim() || !description.trim()) {
      setErrorMsg("Please fill in all required fields.");
      return;
    }

    if (TURNSTILE_SITE_KEY && !turnstileToken) {
      setErrorMsg("Please complete the captcha.");
      return;
    }

    setFormState("sending");
    setErrorMsg("");

    try {
      const res = await fetch("/api/contact", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name.trim(),
          email: email.trim(),
          discord: discord.trim(),
          category,
          subject: subject.trim(),
          description: description.trim(),
          turnstileToken,
          userAgent: navigator.userAgent,
          language: navigator.language,
          platform: navigator.platform,
          screenRes: `${screen.width}x${screen.height}`,
          timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        }),
      });

      if (res.ok) {
        setFormState("sent");
        // Reset form after success
        setTimeout(() => {
          setName(""); setEmail(""); setDiscord("");
          setCategory(""); setSubject(""); setDescription("");
          setFormState("idle");
          resetTurnstile();
        }, 4000);
      } else {
        const data = await res.json().catch(() => ({}));
        setErrorMsg(data.error || "Failed to send. Please try again.");
        setFormState("error");
        resetTurnstile();
      }
    } catch (err) {
      const msg = err instanceof TypeError && err.message === "Failed to fetch"
        ? "Could not connect to the server. Please check your internet connection and try again."
        : "Something went wrong. Please try again.";
      setErrorMsg(msg);
      setFormState("error");
      resetTurnstile();
    }
  }, [name, email, discord, category, subject, description, turnstileToken, resetTurnstile]);

  return (
    <div className="min-h-screen bg-[var(--bg-primary)]">
      <MarketingNav />

      <div className="pt-28 pb-8 px-6">
        <div className="max-w-2xl mx-auto">
          <h1 className="text-3xl font-bold mb-2 text-center">Contact Us</h1>
          <p className="text-[var(--text-muted)] text-center mb-12">
            Got a question, bug, or idea? Here&apos;s how to reach us.
          </p>

          {/* Quick options — GitHub & Discord */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-12">
            {/* GitHub Issues */}
            <Link
              href="https://github.com/Jestriker/JustPlugin/issues/new/choose"
              target="_blank"
              rel="noopener noreferrer"
              className="group bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-5 hover:border-[var(--accent)] transition-colors"
            >
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-lg bg-[var(--bg-tertiary)] flex items-center justify-center">
                  <svg className="w-5 h-5 text-[var(--text-primary)]" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.73.083-.73 1.205.085 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12c0-6.63-5.37-12-12-12z"/></svg>
                </div>
                <div>
                  <div className="font-semibold text-sm group-hover:text-[var(--accent-hover)] transition-colors">Submit a GitHub Issue</div>
                  <div className="text-xs text-[var(--text-muted)]">Best for bug reports & feature requests</div>
                </div>
              </div>
              <p className="text-xs text-[var(--text-muted)] leading-relaxed">
                Open an issue on GitHub with our templates. You&apos;ll get direct responses from the development team.
              </p>
              <span className="inline-block mt-3 text-xs text-[var(--accent)] font-medium">Open GitHub Issues &rarr;</span>
            </Link>

            {/* Discord */}
            <Link
              href="https://discord.gg/QCArmUbaJ8"
              target="_blank"
              rel="noopener noreferrer"
              className="group bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-5 hover:border-[#5865F2] transition-colors"
            >
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-lg bg-[#5865F2]/15 flex items-center justify-center">
                  <svg className="w-5 h-5 text-[#5865F2]" fill="currentColor" viewBox="0 0 24 24"><path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/></svg>
                </div>
                <div>
                  <div className="font-semibold text-sm group-hover:text-[#5865F2] transition-colors">Join our Discord</div>
                  <div className="text-xs text-[var(--text-muted)]">Best for quick help & community chat</div>
                </div>
              </div>
              <p className="text-xs text-[var(--text-muted)] leading-relaxed">
                Get real-time help from the community and developers. Ask questions, share configs, and stay updated.
              </p>
              <span className="inline-block mt-3 text-xs text-[#5865F2] font-medium">Join Discord Server &rarr;</span>
            </Link>
          </div>

          {/* Divider */}
          <div className="flex items-center gap-4 mb-10">
            <div className="flex-1 h-px bg-[var(--border)]" />
            <span className="text-xs text-[var(--text-muted)] uppercase tracking-wider">or send us a message</span>
            <div className="flex-1 h-px bg-[var(--border)]" />
          </div>

          {/* Contact Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Name & Email */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label htmlFor="name" className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                  Name <span className="text-red-400">*</span>
                </label>
                <input
                  id="name"
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  className="w-full px-3 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--accent)] transition-colors"
                  placeholder="Your name"
                />
              </div>
              <div>
                <label htmlFor="email" className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                  Email <span className="text-red-400">*</span>
                </label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  pattern="[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}"
                  title="Please enter a valid email address (e.g. you@example.com)"
                  className="w-full px-3 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--accent)] transition-colors"
                  placeholder="you@example.com"
                />
              </div>
            </div>

            {/* Discord & Category */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label htmlFor="discord" className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                  Discord Username or ID
                </label>
                <input
                  id="discord"
                  type="text"
                  value={discord}
                  onChange={(e) => setDiscord(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--accent)] transition-colors"
                  placeholder="@JustMe.png or 706249082158710855"
                />
              </div>
              <div>
                <label htmlFor="category" className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                  Category <span className="text-red-400">*</span>
                </label>
                <select
                  id="category"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  required
                  className="w-full px-3 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] outline-none focus:border-[var(--accent)] transition-colors appearance-none"
                  style={{ backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%236b7280' d='M2 4l4 4 4-4'/%3E%3C/svg%3E")`, backgroundRepeat: "no-repeat", backgroundPosition: "right 12px center" }}
                >
                  <option value="" disabled>Select a category</option>
                  {categories.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
              </div>
            </div>

            {/* Subject */}
            <div>
              <label htmlFor="subject" className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                Subject <span className="text-red-400">*</span>
              </label>
              <input
                id="subject"
                type="text"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                required
                className="w-full px-3 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--accent)] transition-colors"
                placeholder="Brief summary of your message"
              />
            </div>

            {/* Description */}
            <div>
              <label htmlFor="description" className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                Description <span className="text-red-400">*</span>
              </label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
                rows={6}
                className="w-full px-3 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--accent)] transition-colors resize-y min-h-[120px]"
                placeholder="Describe your question, bug, or suggestion in detail..."
              />
            </div>

            {/* Turnstile captcha */}
            {TURNSTILE_SITE_KEY && (
              <div className="flex justify-center">
                <div ref={turnstileRef} />
              </div>
            )}

            {/* Error message */}
            {errorMsg && (
              <div className="px-4 py-3 rounded-lg bg-red-500/10 border border-red-500/20 text-sm text-red-400">
                {errorMsg}
              </div>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={formState === "sending" || formState === "sent"}
              className={`w-full py-3 rounded-xl font-semibold text-sm transition-all duration-300 ${
                formState === "sent"
                  ? "bg-green-500 text-white scale-[1.02]"
                  : formState === "sending"
                  ? "bg-[var(--accent)]/70 text-white/70 cursor-wait"
                  : "bg-[var(--accent)] text-white hover:bg-[var(--accent-hover)] hover:scale-[1.02] hover:shadow-lg hover:shadow-[var(--accent)]/20 active:scale-[0.98]"
              }`}
            >
              {formState === "sending" ? (
                <span className="inline-flex items-center gap-2">
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Sending...
                </span>
              ) : formState === "sent" ? (
                <span className="inline-flex items-center gap-2">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                  Message Sent!
                </span>
              ) : (
                "Send Message"
              )}
            </button>
          </form>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-[var(--border)] py-4 px-6">
        <div className="max-w-2xl mx-auto text-center text-xs text-[var(--text-muted)]">
          We typically respond within 24-48 hours. For urgent issues, join our <a href="https://discord.gg/QCArmUbaJ8" target="_blank" rel="noopener noreferrer" className="text-[var(--accent)] hover:underline">Discord</a>.
        </div>
      </footer>
    </div>
  );
}
