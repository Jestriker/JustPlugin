import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

const nickCommands = [
  { command: "/nick <name>", desc: "Set your nickname with MiniMessage formatting" },
  { command: "/nick off", desc: "Remove your nickname" },
  { command: "/nick reset", desc: "Reset your nickname to your username" },
];

const tagCommands = [
  { command: "/tag", desc: "Open the tag selection GUI" },
  { command: "/tagcreate <id> <prefix|suffix> <display>", desc: "Create a new tag" },
  { command: "/tagdelete <id>", desc: "Delete a tag" },
  { command: "/taglist", desc: "List all available tags" },
];

const nickConfig = `nicknames:
  max-length: 16
  allow-duplicates: false
  blocked-words:
    - admin
    - moderator
    - server
    - console`;

const permissions = [
  { perm: "justplugin.nick", desc: "Set your own nickname" },
  { perm: "justplugin.nick.color", desc: "Use color codes in nicknames" },
  { perm: "justplugin.nick.format", desc: "Use formatting codes (bold, italic, etc.)" },
  { perm: "justplugin.nick.rainbow", desc: "Use rainbow/gradient formatting in nicknames" },
  { perm: "justplugin.nick.other", desc: "Set another player's nickname" },
  { perm: "justplugin.tag", desc: "Open the tag GUI and select tags" },
  { perm: "justplugin.tag.create", desc: "Create new tags" },
  { perm: "justplugin.tag.delete", desc: "Delete tags" },
  { perm: "justplugin.tag.list", desc: "List all available tags" },
];

export default function NicknamesTagsPage() {
  return (
    <>
      <PageHeader
        title="Nicknames & Tags"
        description="Custom display names with MiniMessage formatting, and a tag system with GUI selection for chat prefixes and suffixes."
      />

      {/* Nicknames */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Nicknames</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Players can set custom nicknames that appear in chat, the tab list, and other displays. Nicknames
          support MiniMessage formatting including colors, gradients, and text decorations. A configurable
          max length and blocked word list keep nicknames appropriate.
        </p>

        <h3 className="text-sm font-semibold mb-2 text-[var(--text-secondary)]">Commands</h3>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)] mb-4">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Command</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {nickCommands.map((c) => (
                <tr key={c.command} className="border-b border-[var(--border)] last:border-0">
                  <td className="px-4 py-3 font-mono text-[var(--accent)] text-xs">{c.command}</td>
                  <td className="px-4 py-3 text-[var(--text-secondary)]">{c.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <h3 className="text-sm font-semibold mb-2 text-[var(--text-secondary)]">Configuration</h3>
        <CodeBlock code={nickConfig} language="yaml" filename="config.yml" />
      </section>

      {/* Tags */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold mb-3">Tags</h2>
        <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
          Tags are chat prefixes or suffixes that players can select from a GUI. Admins create tags with a
          unique ID, type (prefix or suffix), and display text. Players browse and equip tags through the{" "}
          <code className="text-[var(--accent)] bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">/tag</code> GUI.
        </p>

        <h3 className="text-sm font-semibold mb-2 text-[var(--text-secondary)]">Commands</h3>
        <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-[var(--bg-tertiary)]">
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Command</th>
                <th className="text-left px-4 py-3 font-semibold border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {tagCommands.map((c) => (
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
