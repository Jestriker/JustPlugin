"use client";

import { LATEST_VERSION } from "@/data/versions";
import { useVersion } from "./VersionProvider";

export default function VersionBanner() {
  const { version } = useVersion();

  if (version === LATEST_VERSION) return null;

  return (
    <div className="mb-6 px-4 py-2.5 rounded-lg border border-[var(--yellow)]/30 bg-[var(--yellow)]/10 text-sm text-[var(--yellow)]">
      You are viewing docs for <strong>v{version}</strong>. Latest is{" "}
      <strong>v{LATEST_VERSION}</strong>.
    </div>
  );
}
