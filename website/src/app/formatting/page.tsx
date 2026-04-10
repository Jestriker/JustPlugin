import PageHeader from "@/components/PageHeader";
import CodeBlock from "@/components/CodeBlock";

function ColorSwatch({ color, label, tag }: { color: string; label: string; tag: string }) {
  return (
    <div className="flex items-center gap-3 bg-[var(--bg-tertiary)] rounded-lg p-3">
      <div className="w-8 h-8 rounded-md flex-shrink-0" style={{ backgroundColor: color }} />
      <div className="min-w-0">
        <div className="font-semibold text-sm">{label}</div>
        <code className="text-xs text-[var(--text-muted)]">{tag}</code>
      </div>
    </div>
  );
}

function MinecraftPreview({ children, label }: { children: React.ReactNode; label?: string }) {
  return (
    <div className="bg-[#2b2b2b] rounded-lg border border-[var(--border)] overflow-hidden my-4">
      {label && (
        <div className="px-4 py-2 border-b border-[var(--border)] bg-[#1e1e1e]">
          <span className="text-xs text-[var(--text-muted)]">{label}</span>
        </div>
      )}
      <div className="px-4 py-3 font-mono text-sm leading-relaxed">
        {children}
      </div>
    </div>
  );
}

export default function FormattingPage() {
  return (
    <div>
      <PageHeader
        title="Formatting Guide"
        description={<><span className="text-[var(--accent)]">JustPlugin</span> uses MiniMessage for all text formatting. This guide covers colors, gradients, decorations, click/hover events, and more.</>}
      />

      {/* Overview */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-8">
        <h2 className="text-lg font-semibold mb-3">What is MiniMessage?</h2>
        <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
          MiniMessage is the modern text formatting system used by Paper and Adventure. Instead of legacy{" "}
          <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;</code> color codes,
          MiniMessage uses XML-like tags that support hex colors, gradients, rainbow effects, click events,
          hover tooltips, and more. All JustPlugin text configuration uses MiniMessage.
        </p>
      </div>

      {/* Named Colors */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Named Colors</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Use named color tags to apply standard Minecraft colors. Tags are case-sensitive &mdash; always use lowercase.
        </p>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 mb-4">
          <ColorSwatch color="#000000" label="Black" tag="<black>" />
          <ColorSwatch color="#0000AA" label="Dark Blue" tag="<dark_blue>" />
          <ColorSwatch color="#00AA00" label="Dark Green" tag="<dark_green>" />
          <ColorSwatch color="#00AAAA" label="Dark Aqua" tag="<dark_aqua>" />
          <ColorSwatch color="#AA0000" label="Dark Red" tag="<dark_red>" />
          <ColorSwatch color="#AA00AA" label="Dark Purple" tag="<dark_purple>" />
          <ColorSwatch color="#FFAA00" label="Gold" tag="<gold>" />
          <ColorSwatch color="#AAAAAA" label="Gray" tag="<gray>" />
          <ColorSwatch color="#555555" label="Dark Gray" tag="<dark_gray>" />
          <ColorSwatch color="#5555FF" label="Blue" tag="<blue>" />
          <ColorSwatch color="#55FF55" label="Green" tag="<green>" />
          <ColorSwatch color="#55FFFF" label="Aqua" tag="<aqua>" />
          <ColorSwatch color="#FF5555" label="Red" tag="<red>" />
          <ColorSwatch color="#FF55FF" label="Light Purple" tag="<light_purple>" />
          <ColorSwatch color="#FFFF55" label="Yellow" tag="<yellow>" />
          <ColorSwatch color="#FFFFFF" label="White" tag="<white>" />
        </div>

        <CodeBlock
          code={`<red>This is red text</red> and <green>this is green</green>!`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ color: "#FF5555" }}>This is red text</span>
          <span style={{ color: "#AAAAAA" }}> and </span>
          <span style={{ color: "#55FF55" }}>this is green</span>
          <span style={{ color: "#AAAAAA" }}>!</span>
        </MinecraftPreview>
      </div>

      {/* Hex Colors */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Hex Colors</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Use any hex color code for precise color control. Two syntaxes are supported.
        </p>

        <CodeBlock
          code={`<#FF5733>Custom orange text</#FF5733>
<color:#FF5733>Also custom orange</color>
<#ff69b4>Hot pink!</#ff69b4>
<#5555ff>Custom blue</#5555ff>`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ color: "#FF5733" }}>Custom orange text</span>
          <br />
          <span style={{ color: "#FF5733" }}>Also custom orange</span>
          <br />
          <span style={{ color: "#ff69b4" }}>Hot pink!</span>
          <br />
          <span style={{ color: "#5555ff" }}>Custom blue</span>
        </MinecraftPreview>
      </div>

      {/* Gradients */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Gradients</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Smoothly transition between two or more colors. Supports both named colors and hex codes.
        </p>

        <CodeBlock
          code={`<gradient:gold:yellow>Gold to Yellow gradient</gradient>
<gradient:#FF0000:#00FF00>Red to Green gradient</gradient>
<gradient:#ff6b6b:#ee5a24>Warm sunset gradient</gradient>
<gradient:red:gold:yellow>Multi-color gradient</gradient>`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ background: "linear-gradient(to right, #FFAA00, #FFFF55)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            Gold to Yellow gradient
          </span>
          <br />
          <span style={{ background: "linear-gradient(to right, #FF0000, #00FF00)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            Red to Green gradient
          </span>
          <br />
          <span style={{ background: "linear-gradient(to right, #ff6b6b, #ee5a24)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            Warm sunset gradient
          </span>
          <br />
          <span style={{ background: "linear-gradient(to right, #FF5555, #FFAA00, #FFFF55)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            Multi-color gradient
          </span>
        </MinecraftPreview>
      </div>

      {/* Rainbow */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Rainbow</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Apply a full rainbow spectrum to text.
        </p>

        <CodeBlock
          code={`<rainbow>This text is a rainbow!</rainbow>
<rainbow:!>Reversed rainbow!</rainbow>
<rainbow:2>Phase-shifted rainbow</rainbow>`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ background: "linear-gradient(to right, #FF5555, #FFAA00, #FFFF55, #55FF55, #55FFFF, #5555FF, #FF55FF)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            This text is a rainbow!
          </span>
          <br />
          <span style={{ background: "linear-gradient(to right, #FF55FF, #5555FF, #55FFFF, #55FF55, #FFFF55, #FFAA00, #FF5555)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
            Reversed rainbow!
          </span>
        </MinecraftPreview>
      </div>

      {/* Text Decorations */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Text Decorations</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Apply formatting styles to text. Each has a short alias.
        </p>

        <div className="overflow-x-auto mb-4">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Tag</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Short Alias</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Effect</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Preview</th>
              </tr>
            </thead>
            <tbody>
              {[
                { tag: "<bold>", alias: "<b>", effect: "Bold text", style: "font-bold" },
                { tag: "<italic>", alias: "<em> / <i>", effect: "Italic text", style: "italic" },
                { tag: "<underlined>", alias: "<u>", effect: "Underlined text", style: "underline" },
                { tag: "<strikethrough>", alias: "<st>", effect: "Strikethrough text", style: "line-through" },
                { tag: "<obfuscated>", alias: "<obf>", effect: "Obfuscated (magic) text", style: "" },
              ].map((row) => (
                <tr key={row.tag} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono">{row.tag}</code>
                  </td>
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono">{row.alias}</code>
                  </td>
                  <td className="py-3 px-4 text-sm text-[var(--text-secondary)]">{row.effect}</td>
                  <td className="py-3 px-4 text-sm font-mono">
                    <span className={row.style === "font-bold" ? "font-bold" : row.style === "italic" ? "italic" : row.style === "underline" ? "underline" : row.style === "line-through" ? "line-through" : ""}>
                      {row.style ? "Sample Text" : "M\u0337a\u0336g\u0335i\u0334c"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <CodeBlock
          code={`<bold>Bold text</bold>
<italic>Italic text</italic>
<underlined>Underlined text</underlined>
<strikethrough>Strikethrough text</strikethrough>
<obfuscated>Magic text</obfuscated>
<bold><red>Bold and red!</red></bold>
<b><gradient:#ff0000:#00ff00>Bold gradient!</gradient></b>`}
        />
      </div>

      {/* Reset */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Reset</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Remove all previously applied formatting with the <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&lt;reset&gt;</code> tag.
        </p>

        <CodeBlock
          code={`<red><bold>Red and bold <reset>back to normal`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ color: "#FF5555", fontWeight: "bold" }}>Red and bold </span>
          <span style={{ color: "#AAAAAA" }}>back to normal</span>
        </MinecraftPreview>
      </div>

      {/* Click Events */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Click Events</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Make text interactive with click actions.
        </p>

        <div className="overflow-x-auto mb-4">
          <table className="w-full">
            <thead>
              <tr>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Action</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-[var(--text-muted)] border-b border-[var(--border)]">Description</th>
              </tr>
            </thead>
            <tbody>
              {[
                { action: "run_command", desc: "Runs a command when the player clicks the text" },
                { action: "suggest_command", desc: "Puts text into the player's chat box without sending" },
                { action: "copy_to_clipboard", desc: "Copies text to the player's clipboard" },
                { action: "open_url", desc: "Opens a URL in the player's browser" },
              ].map((row) => (
                <tr key={row.action} className="border-b border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors">
                  <td className="py-3 px-4">
                    <code className="bg-[var(--bg-tertiary)] px-2 py-0.5 rounded text-xs font-mono text-[var(--accent-hover)]">{row.action}</code>
                  </td>
                  <td className="py-3 px-4 text-sm text-[var(--text-secondary)]">{row.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <CodeBlock
          code={`<click:run_command:/spawn>Click to teleport to spawn!</click>
<click:suggest_command:/msg >Click to start a message</click>
<click:copy_to_clipboard:Hello!>Click to copy</click>
<click:open_url:https://example.com>Visit website</click>`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ color: "#55FFFF", textDecoration: "underline", cursor: "pointer" }}>Click to teleport to spawn!</span>
          <br />
          <span style={{ color: "#55FFFF", textDecoration: "underline", cursor: "pointer" }}>Click to start a message</span>
          <br />
          <span style={{ color: "#55FFFF", textDecoration: "underline", cursor: "pointer" }}>Click to copy</span>
          <br />
          <span style={{ color: "#55FFFF", textDecoration: "underline", cursor: "pointer" }}>Visit website</span>
        </MinecraftPreview>
      </div>

      {/* Hover Events */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Hover Events</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Show tooltip text when hovering over a message. Can be combined with click events.
        </p>

        <CodeBlock
          code={`<hover:show_text:'<red>Warning! This is dangerous!'>Hover over me</hover>
<hover:show_text:'<gray>Click to accept'>
  <click:run_command:/tpaccept><green>[Accept]</green></click>
</hover>`}
        />

        <MinecraftPreview label="In-game preview (hover not functional on web)">
          <span style={{ color: "#AAAAAA", borderBottom: "1px dashed #555", cursor: "help" }}>Hover over me</span>
          <br />
          <span style={{ color: "#55FF55", cursor: "pointer" }}>[Accept]</span>
        </MinecraftPreview>
      </div>

      {/* Insertion */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Insertion</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Inserts text into the player&apos;s chat box when shift-clicked.
        </p>

        <CodeBlock
          code={`<insertion:/help>Shift-click to insert /help</insertion>`}
        />
      </div>

      {/* Newlines */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Newlines</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Add line breaks in messages.
        </p>

        <CodeBlock
          code={`Line one<newline>Line two<newline>Line three
<br>Also works with br tag`}
        />
      </div>

      {/* Combining Tags */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Combining Tags</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          Tags can be nested and combined for complex formatting.
        </p>

        <CodeBlock
          code={`<bold><gradient:#ff6b6b:#ee5a24>Bold Gradient Header</gradient></bold>

<hover:show_text:'<yellow>Click to teleport!'>
  <click:run_command:/warp shop>
    <green><bold>[Shop]</bold></green>
  </click>
</hover>

<gray>Balance: <green><bold>$1,000.00</bold></green>

<dark_aqua>[Team] <aqua>PlayerName <dark_gray>\u00BB <white>Hello team!

<gradient:#00aaff:#00ffaa><bold>JustPlugin</bold></gradient> <dark_gray>\u00BB <green>Welcome!`}
        />

        <MinecraftPreview label="In-game preview">
          <span style={{ background: "linear-gradient(to right, #ff6b6b, #ee5a24)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent", fontWeight: "bold" }}>
            Bold Gradient Header
          </span>
          <br /><br />
          <span style={{ color: "#55FF55", fontWeight: "bold", cursor: "pointer" }}>[Shop]</span>
          <br /><br />
          <span style={{ color: "#AAAAAA" }}>Balance: </span>
          <span style={{ color: "#55FF55", fontWeight: "bold" }}>$1,000.00</span>
          <br /><br />
          <span style={{ color: "#00AAAA" }}>[Team] </span>
          <span style={{ color: "#55FFFF" }}>PlayerName </span>
          <span style={{ color: "#555555" }}>{"\u00BB"} </span>
          <span style={{ color: "#FFFFFF" }}>Hello team!</span>
          <br /><br />
          <span style={{ background: "linear-gradient(to right, #00aaff, #00ffaa)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent", fontWeight: "bold" }}>
            JustPlugin
          </span>
          <span style={{ color: "#555555" }}> {"\u00BB"} </span>
          <span style={{ color: "#55FF55" }}>Welcome!</span>
        </MinecraftPreview>
      </div>

      {/* Where MiniMessage Works */}
      <div className="mb-10">
        <h2 className="text-2xl font-bold mb-4 pb-2 border-b border-[var(--border)]">Where MiniMessage Works</h2>
        <p className="text-[var(--text-secondary)] mb-4">
          MiniMessage formatting is supported across all text-based features in JustPlugin.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 mb-4">
          {[
            "Nicknames",
            "Tags",
            "Announcements",
            "MOTD profiles",
            "Scoreboard lines & title",
            "Tab list header & footer",
            "Automated messages",
            "Kit names & lore",
            "Join / quit messages",
            "Mail messages",
            "All text config files",
            "Chat formatting",
          ].map((item) => (
            <div key={item} className="flex items-center gap-2 bg-[var(--bg-tertiary)] rounded-lg p-3">
              <span className="text-[var(--green)] flex-shrink-0">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M3 8L6.5 11.5L13 4.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </span>
              <span className="text-sm">{item}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Important Notes */}
      <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6">
        <h2 className="text-lg font-semibold mb-4">Important Notes</h2>
        <ul className="space-y-3 text-sm text-[var(--text-secondary)]">
          <li className="flex items-start gap-2">
            <span className="text-[var(--yellow)] mt-0.5 flex-shrink-0">&#9679;</span>
            Tags are <strong>case-sensitive</strong> &mdash; use lowercase: <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&lt;red&gt;</code> not <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&lt;Red&gt;</code>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-[var(--yellow)] mt-0.5 flex-shrink-0">&#9679;</span>
            Always close tags: <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&lt;red&gt;text&lt;/red&gt;</code> (unclosed tags apply to the rest of the line)
          </li>
          <li className="flex items-start gap-2">
            <span className="text-[var(--yellow)] mt-0.5 flex-shrink-0">&#9679;</span>
            Escape literal <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&lt;</code> characters with <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">\&lt;</code> if you don&apos;t want them parsed as tags
          </li>
          <li className="flex items-start gap-2">
            <span className="text-[var(--yellow)] mt-0.5 flex-shrink-0">&#9679;</span>
            Legacy <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;</code> color codes (e.g., <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;c</code>, <code className="bg-[var(--bg-tertiary)] px-1.5 py-0.5 rounded text-xs">&amp;l</code>) also work in player-facing input
          </li>
          <li className="flex items-start gap-2">
            <span className="text-[var(--yellow)] mt-0.5 flex-shrink-0">&#9679;</span>
            Some contexts (like item names) may not support click/hover events
          </li>
        </ul>
      </div>
    </div>
  );
}
