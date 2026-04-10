"use client";

import { useEffect, useRef } from "react";

export default function GlobalError({
  error,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const barRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    console.error(error);
    requestAnimationFrame(() => {
      if (barRef.current) barRef.current.style.width = "100%";
    });
    const timer = setTimeout(() => {
      window.location.href = "/";
    }, 10000);
    return () => clearTimeout(timer);
  }, [error]);

  return (
    <html lang="en">
      <body style={{ margin: 0, background: "#0f1117", color: "#e8e8e8", fontFamily: "system-ui, sans-serif" }}>
        <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", padding: "1.5rem" }}>
          <div style={{ textAlign: "center", maxWidth: "24rem" }}>
            <div style={{ fontSize: "7rem", fontWeight: 900, color: "#ef4444", opacity: 0.15, lineHeight: 1, marginBottom: "0.5rem", userSelect: "none" }}>
              ERR
            </div>

            <h1 style={{ fontSize: "1.25rem", fontWeight: 700, marginBottom: "0.5rem" }}>
              Something Went Wrong
            </h1>
            <p style={{ color: "#6b7280", fontSize: "0.875rem", marginBottom: "2.5rem" }}>
              An unexpected error occurred.
            </p>

            <div style={{ marginBottom: "2rem" }}>
              <div style={{ height: 4, borderRadius: 999, background: "#1a1d2e", overflow: "hidden" }}>
                <div
                  ref={barRef}
                  style={{ height: "100%", borderRadius: 999, background: "#ef4444", width: "0%", transition: "width 10s linear" }}
                />
              </div>
              <p style={{ fontSize: "0.75rem", color: "#6b7280", marginTop: "0.5rem" }}>
                Redirecting home...
              </p>
            </div>

            <a
              href="/"
              style={{
                display: "inline-flex", alignItems: "center", gap: "0.5rem",
                padding: "0.625rem 1.5rem", borderRadius: 8, background: "#f97316",
                color: "white", fontSize: "0.875rem", fontWeight: 500, textDecoration: "none",
              }}
            >
              Go Home
            </a>
          </div>
        </div>
      </body>
    </html>
  );
}
