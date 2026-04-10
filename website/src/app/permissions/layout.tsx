import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "150+ Permissions Reference",
  description:
    "Complete permissions reference for JustPlugin. Browse all 150+ permission nodes organized by category with hierarchy details.",
};

export default function PermissionsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
