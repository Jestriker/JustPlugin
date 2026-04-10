import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import DocsLayout from "@/components/DocsLayout";
import ThemeProvider from "@/components/ThemeProvider";
import { PLUGIN_VERSION } from "@/data/constants";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

const siteUrl = "https://justplugin.dev";
const siteTitle = `JustPlugin Wiki v${PLUGIN_VERSION}`;
const siteDescription = `Documentation for JustPlugin v${PLUGIN_VERSION} - The only essentials plugin you'll ever need. 200+ commands, economy, teleportation, moderation, and more for Paper, Purpur, and Folia.`;

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: siteTitle,
    template: `%s | JustPlugin`,
  },
  description: siteDescription,
  keywords: [
    "JustPlugin",
    "Minecraft plugin",
    "Paper plugin",
    "Purpur plugin",
    "Folia plugin",
    "server management",
    "essentials plugin",
    "economy plugin",
    "teleportation",
    "moderation",
    "Minecraft server",
  ],
  authors: [{ name: "Jestriker" }],
  creator: "Jestriker",
  robots: {
    index: true,
    follow: true,
  },
  openGraph: {
    type: "website",
    locale: "en_US",
    url: siteUrl,
    siteName: "JustPlugin Wiki",
    title: siteTitle,
    description: siteDescription,
    images: [
      {
        url: "/plugins-image.png",
        width: 512,
        height: 512,
        alt: "JustPlugin Logo",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: siteTitle,
    description: siteDescription,
    images: ["/plugins-image.png"],
    creator: "@Jestriker",
  },
  alternates: {
    canonical: siteUrl,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      suppressHydrationWarning
    >
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{var t=localStorage.getItem('jp-theme');if(t)document.documentElement.setAttribute('data-theme',t)}catch(e){}})()`,
          }}
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@graph": [
                {
                  "@type": "Organization",
                  "@id": `${siteUrl}/#organization`,
                  name: "JustPlugin",
                  url: siteUrl,
                  logo: {
                    "@type": "ImageObject",
                    url: `${siteUrl}/plugins-image.png`,
                    width: 512,
                    height: 512,
                  },
                  sameAs: [
                    "https://github.com/Jestriker/JustPlugin",
                    "https://modrinth.com/plugin/justplugin",
                    "https://discord.gg/QCArmUbaJ8",
                  ],
                },
                {
                  "@type": "SoftwareApplication",
                  "@id": `${siteUrl}/#software`,
                  name: "JustPlugin",
                  description: siteDescription,
                  applicationCategory: "GamePlugin",
                  operatingSystem: "Paper, Purpur, Folia (Minecraft 1.21.11+)",
                  softwareVersion: PLUGIN_VERSION,
                  offers: {
                    "@type": "Offer",
                    price: "0",
                    priceCurrency: "USD",
                  },
                  author: {
                    "@type": "Person",
                    name: "Jestriker",
                    url: "https://github.com/Jestriker",
                  },
                  license: "https://opensource.org/licenses/MIT",
                  downloadUrl: "https://modrinth.com/plugin/justplugin",
                  image: `${siteUrl}/plugins-image.png`,
                },
                {
                  "@type": "WebSite",
                  "@id": `${siteUrl}/#website`,
                  url: siteUrl,
                  name: "JustPlugin Wiki",
                  description: siteDescription,
                  publisher: { "@id": `${siteUrl}/#organization` },
                  inLanguage: "en-US",
                },
              ],
            }),
          }}
        />
      </head>
      <body suppressHydrationWarning>
        <ThemeProvider>
          <DocsLayout>{children}</DocsLayout>
        </ThemeProvider>
      </body>
    </html>
  );
}
