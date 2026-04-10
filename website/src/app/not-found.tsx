"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function NotFound() {
  const router = useRouter();
  const [fading, setFading] = useState(false);
  const barRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Start smooth fill immediately
    requestAnimationFrame(() => {
      if (barRef.current) barRef.current.style.width = "100%";
    });
    const timer = setTimeout(() => {
      setFading(true);
      setTimeout(() => router.push("/"), 500);
    }, 10000);
    return () => clearTimeout(timer);
  }, [router]);

  return (
    <div className={`fixed inset-0 z-[999] flex items-center justify-center px-6 bg-[var(--bg-primary)] transition-opacity duration-500 ${fading ? "opacity-0" : "opacity-100"}`}>
      <div className="text-center max-w-sm">
        <div className="text-[7rem] font-black leading-none text-[var(--accent)] opacity-15 select-none mb-2">
          404
        </div>

        <h1 className="text-xl font-bold mb-2">Page Not Found</h1>
        <p className="text-sm text-[var(--text-muted)] mb-10">
          This block doesn&apos;t exist in our world.
        </p>

        <div className="mb-8">
          <div className="h-1 rounded-full bg-[var(--bg-tertiary)] overflow-hidden">
            <div
              ref={barRef}
              className="h-full rounded-full bg-[var(--accent)]"
              style={{ width: "0%", transition: "width 10s linear" }}
            />
          </div>
          <p className="text-xs text-[var(--text-muted)] mt-2">Redirecting home...</p>
        </div>

        <Link
          href="/"
          className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg bg-[var(--accent)] text-white text-sm font-medium hover:bg-[var(--accent-hover)] transition-colors"
        >
          Go Home
        </Link>
      </div>
    </div>
  );
}
