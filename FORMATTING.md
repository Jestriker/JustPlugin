# JustPlugin Formatting Guide

JustPlugin uses **MiniMessage** for all text formatting. This guide covers all available formatting tags with examples.

---

## 🎨 Named Colors

| Tag | Preview | Description |
|-----|---------|-------------|
| `<black>` | ⬛ Black | |
| `<dark_blue>` | Dark Blue | |
| `<dark_green>` | Dark Green | |
| `<dark_aqua>` | Dark Aqua | |
| `<dark_red>` | Dark Red | |
| `<dark_purple>` | Dark Purple | |
| `<gold>` | Gold / Orange | |
| `<gray>` | Gray | |
| `<dark_gray>` | Dark Gray | |
| `<blue>` | Blue | |
| `<green>` | Green | |
| `<aqua>` | Aqua / Cyan | |
| `<red>` | Red | |
| `<light_purple>` | Light Purple / Pink | |
| `<yellow>` | Yellow | |
| `<white>` | White | |

### Example
```
<red>This is red text</red> and <green>this is green</green>!
```

---

## 🌈 Hex Colors

Use any hex color code with the `<#RRGGBB>` tag.

### Examples
```
<#ff5555>Custom red color</#ff5555>
<#55ff55>Custom green color</#55ff55>
<#5555ff>Custom blue color</#5555ff>
<#ff69b4>Hot pink!</#ff69b4>
<color:#ff6600>Orange using color tag</color>
```

---

## 🌊 Gradients

Smoothly transition between two or more colors.

### Syntax
```
<gradient:color1:color2[:color3...]>text</gradient>
```

### Examples
```
<gradient:#ff0000:#0000ff>Red to Blue gradient</gradient>
<gradient:#00aaff:#00ffaa>Aqua to Green gradient</gradient>
<gradient:red:gold:yellow>Multi-color gradient</gradient>
<gradient:#ff6b6b:#ee5a24>Warm sunset gradient</gradient>
```

---

## 🌈 Rainbow

Apply a full rainbow effect to text.

### Example
```
<rainbow>This text is a rainbow!</rainbow>
<rainbow:!>Reversed rainbow!</rainbow>
<rainbow:2>Phase-shifted rainbow</rainbow>
```

---

## ✨ Text Decorations

| Tag | Effect | Closing Tag |
|-----|--------|-------------|
| `<bold>` or `<b>` | **Bold** | `</bold>` or `</b>` |
| `<italic>` or `<em>` or `<i>` | *Italic* | `</italic>` or `</em>` or `</i>` |
| `<underlined>` or `<u>` | Underlined | `</underlined>` or `</u>` |
| `<strikethrough>` or `<st>` | ~~Strikethrough~~ | `</strikethrough>` or `</st>` |
| `<obfuscated>` or `<obf>` | M̷̢̛a̶̡g̸̢i̵c̶ | `</obfuscated>` or `</obf>` |

### Examples
```
<bold>Bold text</bold>
<italic>Italic text</italic>
<underlined>Underlined text</underlined>
<strikethrough>Strikethrough text</strikethrough>
<obfuscated>Magic text</obfuscated>
<bold><red>Bold and red!</red></bold>
<b><gradient:#ff0000:#00ff00>Bold gradient!</gradient></b>
```

---

## 🔄 Reset

Remove all previously applied formatting.

### Example
```
<red><bold>Red and bold <reset>back to normal
```

---

## 🖱️ Click Events

Make text clickable with various actions.

| Action | Description |
|--------|-------------|
| `run_command` | Runs a command when clicked |
| `suggest_command` | Puts text in chat box |
| `copy_to_clipboard` | Copies text to clipboard |
| `open_url` | Opens a URL in browser |

### Examples
```
<click:run_command:/spawn>Click to teleport to spawn!</click>
<click:suggest_command:/msg >Click to start a message</click>
<click:copy_to_clipboard:Hello!>Click to copy</click>
<click:open_url:https://example.com>Visit website</click>
```

---

## 💬 Hover Events

Show text when hovering over a message.

### Examples
```
<hover:show_text:'<red>Warning! This is dangerous!'>Hover over me</hover>
<hover:show_text:'<gray>Click to accept'>
  <click:run_command:/tpaccept><green>[Accept]</green></click>
</hover>
```

---

## 📝 Insertion

Inserts text into the chat box when shift-clicked.

### Example
```
<insertion:/help>Shift-click to insert /help</insertion>
```

---

## ↩️ Newline

Add line breaks in messages.

### Example
```
Line one<newline>Line two<newline>Line three
<br>Also works with br tag
```

---

## 🔗 Transition

Smoothly transition between colors based on a phase value.

### Example
```
<transition:#ff0000:#00ff00:0.5>Halfway between red and green</transition>
```

---

## 🧩 Combining Tags

Tags can be nested and combined for complex formatting.

### Examples
```
<bold><gradient:#ff6b6b:#ee5a24>Bold Gradient Header</gradient></bold>

<hover:show_text:'<yellow>Click to teleport!'>
  <click:run_command:/warp shop>
    <green><bold>[Shop]</bold></green>
  </click>
</hover>

<gray>Balance: <green><bold>$1,000.00</bold></green>

<dark_aqua>[Team] <aqua>PlayerName <dark_gray>» <white>Hello team!

<gradient:#00aaff:#00ffaa><bold>JustPlugin</bold></gradient> <dark_gray>» <green>Welcome!
```

---

## 📋 Usage in JustPlugin

### Announcements (`/announce`)
```
/announce <rainbow>Server event starting now!</rainbow>
/announce <gradient:#ff0000:#ffff00><bold>SALE!</bold></gradient> <gray>50% off at <click:run_command:/warp shop><green>/warp shop</green></click>
```

### MOTD (`config.yml`)
```yaml
motd: "<gradient:#00aaff:#00ffaa><bold>Welcome, {player}!</bold></gradient>\n<gray>Type <yellow>/help</yellow> for commands."
```

### Tab Header/Footer (`config.yml`)
```yaml
tab:
  header: "\n<gradient:#00aaff:#00ffaa><bold>  My Server  </bold></gradient>\n"
  footer: "\n<gray>Players: <yellow>{online}<gray>/<yellow>{max}\n"
```

---

## ⚠️ Important Notes

- Tags are **case-sensitive** — use lowercase: `<red>` not `<Red>`
- Always close tags: `<red>text</red>` (though unclosed tags apply to the rest of the line)
- Escape literal `<` characters with `\<` if you don't want them parsed as tags
- Legacy `&` color codes (e.g., `&c`, `&l`) also work in player-facing input
- Some contexts (like item names) may not support click/hover events

