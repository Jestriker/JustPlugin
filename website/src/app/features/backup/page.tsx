import PageHeader from "@/components/PageHeader";

const commands = [
  { command: "/jpbackup export", desc: "Export all plugin data to a backup file" },
  { command: "/jpbackup import <file>", desc: "Import plugin data from a backup file (requires confirmation)" },
  { command: "/jpbackup list", desc: "List all available backup files" },
  { command: "/jpbackup delete <file>", desc: "Delete a specific backup file" },
];

const permissions = [
  { perm: "justplugin.backup", desc: "Access the backup system" },
  { perm: "justplugin.backup.export", desc: "Export plugin data" },
  { perm: "justplugin.backup.import", desc: "Import plugin data" },
  { perm: "justplugin.backup.list", desc: "List backup files" },
  { perm: "justplugin.backup.delete", desc: "Delete backup files" },
];

export default function BackupPage() {
  return (
    <>
      <PageHeader
        title="Backup & Export"
        description="Full plugin data backup and restore with async I/O, import confirmation, and simple file management."
      />

      {/* Overview */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Overview</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed">
          The backup system lets you export and import all JustPlugin data. Backups include player data,
          economy, warps, homes, kits, and all other plugin state. All file operations use async I/O to
          prevent server lag during backup and restore operations.
        </p>
      </section>

      {/* Features */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Features</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          {[
            { title: "Full Data Backup", desc: "Exports all plugin data including player data, economy, warps, homes, kits, and configuration." },
            { title: "Async I/O", desc: "All file operations run asynchronously off the main thread, preventing any server lag." },
            { title: "Import Confirmation", desc: "Importing a backup requires confirmation to prevent accidental data overwrites." },
            { title: "File Management", desc: "List and delete old backups directly from in-game commands." },
          ].map((f) => (
            <div key={f.title} className="rounded-lg border border-[var(--border)] bg-[var(--bg-card)] p-4">
              <h3 className="font-semibold text-sm mb-1">{f.title}</h3>
              <p className="text-sm text-[var(--text-secondary)]">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Commands */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Commands</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Command</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {commands.map((c) => (
                <tr key={c.command} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{c.command}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{c.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Permissions */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Permissions</h2>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Permission</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {permissions.map((p) => (
                <tr key={p.perm} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{p.perm}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{p.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
