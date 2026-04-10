import type { MetadataRoute } from "next";

const BASE_URL = "https://justplugin.dev";

export default function sitemap(): MetadataRoute.Sitemap {
  const now = new Date();

  const staticPages = [
    { path: "", priority: 1, changeFrequency: "weekly" as const },
    { path: "/features", priority: 0.9, changeFrequency: "weekly" as const },
    { path: "/commands", priority: 0.9, changeFrequency: "weekly" as const },
    { path: "/permissions", priority: 0.9, changeFrequency: "weekly" as const },
    { path: "/configuration", priority: 0.8, changeFrequency: "monthly" as const },
    { path: "/api", priority: 0.8, changeFrequency: "monthly" as const },
    { path: "/comparison", priority: 0.7, changeFrequency: "monthly" as const },
    { path: "/faq", priority: 0.7, changeFrequency: "monthly" as const },
    { path: "/migration", priority: 0.7, changeFrequency: "monthly" as const },
    { path: "/troubleshooting", priority: 0.7, changeFrequency: "monthly" as const },
    { path: "/contact", priority: 0.5, changeFrequency: "yearly" as const },
    { path: "/changelog", priority: 0.6, changeFrequency: "weekly" as const },
    { path: "/formatting", priority: 0.6, changeFrequency: "monthly" as const },
    { path: "/placeholders", priority: 0.6, changeFrequency: "monthly" as const },
    { path: "/version-support", priority: 0.5, changeFrequency: "monthly" as const },
  ];

  const featurePages = [
    "economy",
    "teleportation",
    "warps-and-homes",
    "moderation",
    "jail",
    "kits",
    "vanish",
    "teams",
    "trading",
    "skins",
    "maintenance",
    "scoreboard",
    "tab-list",
    "motd",
    "chat",
    "mail",
    "nicknames-tags",
    "afk",
    "automated-messages",
    "virtual-inventories",
    "world-management",
    "backup",
  ];

  return [
    ...staticPages.map((page) => ({
      url: `${BASE_URL}${page.path}`,
      lastModified: now,
      changeFrequency: page.changeFrequency,
      priority: page.priority,
    })),
    ...featurePages.map((slug) => ({
      url: `${BASE_URL}/features/${slug}`,
      lastModified: now,
      changeFrequency: "monthly" as const,
      priority: 0.7,
    })),
  ];
}
