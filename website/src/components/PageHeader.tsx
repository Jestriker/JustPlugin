export default function PageHeader({
  title,
  description,
  badge,
}: {
  title: string;
  description?: string;
  badge?: string;
}) {
  return (
    <div className="mb-8 pb-6 border-b border-[var(--border)]">
      <div className="flex items-center gap-3 mb-2">
        <h1 className="text-3xl font-bold">{title}</h1>
        {badge && (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-[var(--accent)] text-white">
            {badge}
          </span>
        )}
      </div>
      {description && (
        <p className="text-[var(--text-secondary)] text-lg leading-relaxed">{description}</p>
      )}
    </div>
  );
}
