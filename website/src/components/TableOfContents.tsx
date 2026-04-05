"use client";

import { useEffect, useState, useRef } from "react";

interface TocItem {
  id: string;
  text: string;
  level: 2 | 3;
}

export default function TableOfContents() {
  const [items, setItems] = useState<TocItem[]>([]);
  const [activeId, setActiveId] = useState<string>("");
  const observerRef = useRef<IntersectionObserver | null>(null);

  useEffect(() => {
    const headings = Array.from(
      document.querySelectorAll("h2[id], h3[id]")
    ) as HTMLElement[];

    const tocItems: TocItem[] = headings.map((h) => ({
      id: h.id,
      text: h.textContent?.replace(/^#\s*/, "") || "",
      level: h.tagName === "H2" ? 2 : 3,
    }));

    setItems(tocItems);

    if (observerRef.current) observerRef.current.disconnect();

    const visibleIds = new Set<string>();

    observerRef.current = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            visibleIds.add(entry.target.id);
          } else {
            visibleIds.delete(entry.target.id);
          }
        });

        // Pick the first visible heading in document order
        const ordered = headings.filter((h) => visibleIds.has(h.id));
        if (ordered.length > 0) {
          setActiveId(ordered[0].id);
        }
      },
      { rootMargin: "-80px 0px -60% 0px", threshold: 0 }
    );

    headings.forEach((h) => observerRef.current!.observe(h));

    return () => observerRef.current?.disconnect();
  }, []);

  if (items.length === 0) return null;

  return (
    <nav className="hidden xl:block w-[200px] shrink-0 sticky top-8 self-start max-h-[calc(100vh-4rem)] overflow-y-auto">
      <p className="text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-3">
        On this page
      </p>
      <ul className="space-y-1">
        {items.map((item) => (
          <li key={item.id}>
            <a
              href={`#${item.id}`}
              onClick={(e) => {
                e.preventDefault();
                document.getElementById(item.id)?.scrollIntoView({ behavior: "smooth" });
              }}
              className={`block text-xs py-1 transition-colors border-l-2 ${
                activeId === item.id
                  ? "text-[var(--accent)] border-[var(--accent)]"
                  : "text-[var(--text-muted)] hover:text-[var(--text-secondary)] border-transparent"
              }`}
              style={{ paddingLeft: item.level === 3 ? "1.25rem" : "0.5rem" }}
            >
              {item.text}
            </a>
          </li>
        ))}
      </ul>
    </nav>
  );
}
