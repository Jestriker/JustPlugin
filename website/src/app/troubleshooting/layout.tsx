import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Troubleshooting",
  description:
    "Troubleshoot common JustPlugin issues. Solutions for startup errors, database problems, permission conflicts, and performance optimization.",
};

export default function TroubleshootingLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
