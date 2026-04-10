import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "200+ Commands Reference",
  description:
    "Complete reference for all 200+ JustPlugin commands organized by category. Usage, permissions, aliases, and examples for every command.",
};

export default function CommandsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
