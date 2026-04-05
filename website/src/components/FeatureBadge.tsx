type BadgeVariant = "enabled" | "opt-in" | "requires-luckperms" | "disabled";

const config: Record<BadgeVariant, { label: string; color: string; bg: string }> = {
  enabled: {
    label: "Enabled by default",
    color: "var(--green)",
    bg: "rgba(34,197,94,0.12)",
  },
  "opt-in": {
    label: "Opt-in",
    color: "var(--yellow)",
    bg: "rgba(234,179,8,0.12)",
  },
  "requires-luckperms": {
    label: "Requires LuckPerms",
    color: "var(--blue)",
    bg: "rgba(59,130,246,0.12)",
  },
  disabled: {
    label: "Disabled by default",
    color: "var(--text-muted)",
    bg: "rgba(107,114,128,0.12)",
  },
};

interface FeatureBadgeProps {
  variant: BadgeVariant;
}

export default function FeatureBadge({ variant }: FeatureBadgeProps) {
  const { label, color, bg } = config[variant];

  return (
    <span
      className="inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full"
      style={{ color, backgroundColor: bg }}
    >
      <span
        className="w-1.5 h-1.5 rounded-full"
        style={{ backgroundColor: color }}
      />
      {label}
    </span>
  );
}
