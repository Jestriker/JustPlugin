#!/usr/bin/env node
/**
 * generate-docs.js — Generates documentation JSON files from plugin.yml.
 *
 * Reads src/main/resources/plugin.yml and outputs:
 *   - website/src/data/generated-commands.json  (matches the TSX Category[] format)
 *   - website/src/data/generated-permissions.json (matches the TSX PermSection[] format)
 *
 * Usage: node scripts/generate-docs.js [--check]
 *   --check  Compare generated output with existing files, exit 1 if they differ.
 *
 * No npm dependencies — uses only Node.js built-in modules.
 */

const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
const PLUGIN_YML = path.join(ROOT, "src", "main", "resources", "plugin.yml");
const DATA_DIR = path.join(ROOT, "website", "src", "data");
const COMMANDS_OUT = path.join(DATA_DIR, "generated-commands.json");
const PERMISSIONS_OUT = path.join(DATA_DIR, "generated-permissions.json");

const CHECK_MODE = process.argv.includes("--check");

/* ------------------------------------------------------------------ */
/*  Category mapping                                                    */
/* ------------------------------------------------------------------ */

// Maps the "# === Category ===" comment labels from plugin.yml to the id/title
// used by the TSX pages. Order here determines output order.
const COMMAND_CATEGORY_MAP = [
  { label: "Teleportation", id: "teleportation", title: "Teleportation" },
  { label: "Warps", id: "warps", title: "Warps" },
  { label: "Homes", id: "homes", title: "Homes" },
  { label: "Economy", id: "economy", title: "Economy" },
  { label: "Moderation", id: "moderation", title: "Moderation" },
  { label: "Jail", id: "jail", title: "Jail" },
  { label: "Player", id: "player", title: "Player" },
  { label: "AFK", id: "player", title: "Player" }, // AFK merges into Player
  { label: "Chat", id: "chat", title: "Chat" },
  { label: "Virtual Inventories", id: "virtual-inventories", title: "Virtual Inventories" },
  { label: "Info", id: "info", title: "Info" },
  { label: "Items", id: "items", title: "Items" },
  { label: "World", id: "world", title: "World" },
  { label: "Teams", id: "teams", title: "Teams" },
  { label: "Misc", id: "misc", title: "Misc" },
  { label: "Kits", id: "kits", title: "Kits" },
  { label: "Auto Messages", id: "misc", title: "Misc" }, // merge into Misc
  { label: "Backup", id: "misc", title: "Misc" }, // merge into Misc
  { label: "Overrides", id: "overrides", title: "Overrides" },
  { label: "Ranks (LuckPerms integration)", id: "misc", title: "Misc" },
  { label: "Stats", id: "misc", title: "Misc" },
  { label: "Maintenance", id: "misc", title: "Misc" },
  { label: "Skins", id: "misc", title: "Misc" },
  { label: "Nicknames & Tags", id: "personalization", title: "Personalization" },
];

// Maps category labels to permission section ids/titles. Permissions in
// plugin.yml don't have category comments, so we map by permission prefix.
const PERM_CATEGORY_RULES = [
  { prefix: "justplugin.*", section: "wildcard" },
  { prefix: "justplugin.player", section: "wildcard" },
  { prefix: "justplugin.tpa", section: "teleportation" },
  { prefix: "justplugin.tpaccept", section: "teleportation" },
  { prefix: "justplugin.tpacancel", section: "teleportation" },
  { prefix: "justplugin.tpreject", section: "teleportation" },
  { prefix: "justplugin.tpahere", section: "teleportation" },
  { prefix: "justplugin.tppos", section: "teleportation" },
  { prefix: "justplugin.wild", section: "teleportation" },
  { prefix: "justplugin.back", section: "teleportation" },
  { prefix: "justplugin.spawn", section: "teleportation" },
  { prefix: "justplugin.setspawn", section: "teleportation" },
  { prefix: "justplugin.teleport", section: "teleportation" },
  { prefix: "justplugin.tpoff", section: "offline-player" },
  { prefix: "justplugin.getposoff", section: "offline-player" },
  { prefix: "justplugin.getdeathposoff", section: "offline-player" },
  { prefix: "justplugin.invseeoff", section: "offline-player" },
  { prefix: "justplugin.echestseeoff", section: "offline-player" },
  { prefix: "justplugin.warp", section: "warp" },
  { prefix: "justplugin.setwarp", section: "warp" },
  { prefix: "justplugin.delwarp", section: "warp" },
  { prefix: "justplugin.renamewarp", section: "warp" },
  { prefix: "justplugin.home", section: "home" },
  { prefix: "justplugin.sethome", section: "home" },
  { prefix: "justplugin.delhome", section: "home" },
  { prefix: "justplugin.balance", section: "economy" },
  { prefix: "justplugin.pay", section: "economy" },
  { prefix: "justplugin.paytoggle", section: "economy" },
  { prefix: "justplugin.paynote", section: "economy" },
  { prefix: "justplugin.addcash", section: "economy" },
  { prefix: "justplugin.baltop", section: "economy" },
  { prefix: "justplugin.baltophide", section: "economy" },
  { prefix: "justplugin.transactions", section: "economy" },
  { prefix: "justplugin.ban", section: "moderation" },
  { prefix: "justplugin.tempban", section: "moderation" },
  { prefix: "justplugin.unban", section: "moderation" },
  { prefix: "justplugin.vanish", section: "moderation" },
  { prefix: "justplugin.supervanish", section: "moderation" },
  { prefix: "justplugin.sudo", section: "moderation" },
  { prefix: "justplugin.invsee", section: "moderation" },
  { prefix: "justplugin.echestsee", section: "moderation" },
  { prefix: "justplugin.mute", section: "moderation" },
  { prefix: "justplugin.tempmute", section: "moderation" },
  { prefix: "justplugin.unmute", section: "moderation" },
  { prefix: "justplugin.warn", section: "moderation" },
  { prefix: "justplugin.kick", section: "moderation" },
  { prefix: "justplugin.setlogswebhook", section: "moderation" },
  { prefix: "justplugin.applyedits", section: "moderation" },
  { prefix: "justplugin.deathitems", section: "moderation" },
  { prefix: "justplugin.oplist", section: "moderation" },
  { prefix: "justplugin.banlist", section: "moderation" },
  { prefix: "justplugin.announce", section: "moderation" },
  { prefix: "justplugin.jail", section: "jail" },
  { prefix: "justplugin.unjail", section: "jail" },
  { prefix: "justplugin.setjail", section: "jail" },
  { prefix: "justplugin.deljail", section: "jail" },
  { prefix: "justplugin.jails", section: "jail" },
  { prefix: "justplugin.jailinfo", section: "jail" },
  { prefix: "justplugin.fly", section: "player" },
  { prefix: "justplugin.gamemode", section: "player" },
  { prefix: "justplugin.gmcheck", section: "player" },
  { prefix: "justplugin.god", section: "player" },
  { prefix: "justplugin.speed", section: "player" },
  { prefix: "justplugin.heal", section: "player" },
  { prefix: "justplugin.feed", section: "player" },
  { prefix: "justplugin.kill", section: "player" },
  { prefix: "justplugin.hat", section: "player" },
  { prefix: "justplugin.exp", section: "player" },
  { prefix: "justplugin.skull", section: "player" },
  { prefix: "justplugin.suicide", section: "player" },
  { prefix: "justplugin.getpos", section: "player" },
  { prefix: "justplugin.getdeathpos", section: "player" },
  { prefix: "justplugin.afk", section: "player" },
  { prefix: "justplugin.near", section: "player" },
  { prefix: "justplugin.repair", section: "player" },
  { prefix: "justplugin.enchant", section: "player" },
  { prefix: "justplugin.msg", section: "chat" },
  { prefix: "justplugin.ignore", section: "chat" },
  { prefix: "justplugin.sharecoords", section: "chat" },
  { prefix: "justplugin.sharedeathcoords", section: "chat" },
  { prefix: "justplugin.chat", section: "chat" },
  { prefix: "justplugin.clearchat", section: "chat" },
  { prefix: "justplugin.mail", section: "chat" },
  { prefix: "justplugin.kit", section: "kits" },
  { prefix: "justplugin.nick", section: "personalization" },
  { prefix: "justplugin.tag", section: "personalization" },
  { prefix: "justplugin.anvil", section: "virtual-inventories" },
  { prefix: "justplugin.grindstone", section: "virtual-inventories" },
  { prefix: "justplugin.enderchest", section: "virtual-inventories" },
  { prefix: "justplugin.craft", section: "virtual-inventories" },
  { prefix: "justplugin.stonecutter", section: "virtual-inventories" },
  { prefix: "justplugin.loom", section: "virtual-inventories" },
  { prefix: "justplugin.smithingtable", section: "virtual-inventories" },
  { prefix: "justplugin.enchantingtable", section: "virtual-inventories" },
  { prefix: "justplugin.vault", section: "virtual-inventories" },
  { prefix: "justplugin.playerinfo", section: "info" },
  { prefix: "justplugin.playerlist", section: "info" },
  { prefix: "justplugin.staff", section: "info" },
  { prefix: "justplugin.motd", section: "info" },
  { prefix: "justplugin.info", section: "info" },
  { prefix: "justplugin.help", section: "info" },
  { prefix: "justplugin.list", section: "info" },
  { prefix: "justplugin.clock", section: "info" },
  { prefix: "justplugin.date", section: "info" },
  { prefix: "justplugin.itemname", section: "items" },
  { prefix: "justplugin.shareitem", section: "items" },
  { prefix: "justplugin.setspawner", section: "items" },
  { prefix: "justplugin.weather", section: "world" },
  { prefix: "justplugin.time", section: "world" },
  { prefix: "justplugin.freezegame", section: "world" },
  { prefix: "justplugin.unfreezegame", section: "world" },
  { prefix: "justplugin.clearentities", section: "world" },
  { prefix: "justplugin.friendlyfire", section: "world" },
  { prefix: "justplugin.team", section: "teams" },
  { prefix: "justplugin.teamhome", section: "teams" },
  { prefix: "justplugin.trade", section: "misc" },
  { prefix: "justplugin.discord", section: "misc" },
  { prefix: "justplugin.tab", section: "misc" },
  { prefix: "justplugin.scoreboard", section: "misc" },
  { prefix: "justplugin.stats", section: "misc" },
  { prefix: "justplugin.maintenance", section: "misc" },
  { prefix: "justplugin.skin", section: "misc" },
  { prefix: "justplugin.skinban", section: "misc" },
  { prefix: "justplugin.skinunban", section: "misc" },
  { prefix: "justplugin.backup", section: "misc" },
  { prefix: "justplugin.automessage", section: "misc" },
  { prefix: "justplugin.plugins", section: "misc" },
  { prefix: "justplugin.rank", section: "misc" },
  { prefix: "justplugin.spawnprotection", section: "protection" },
  { prefix: "justplugin.seedprotection", section: "protection" },
  { prefix: "justplugin.log", section: "log" },
];

// Permission section display order and titles
const PERM_SECTIONS_ORDER = [
  { id: "teleportation", title: "Teleportation" },
  { id: "offline-player", title: "Offline Player" },
  { id: "warp", title: "Warp" },
  { id: "home", title: "Home" },
  { id: "economy", title: "Economy" },
  { id: "moderation", title: "Moderation" },
  { id: "jail", title: "Jail" },
  { id: "player", title: "Player" },
  { id: "chat", title: "Chat" },
  { id: "kits", title: "Kits" },
  { id: "personalization", title: "Personalization" },
  { id: "virtual-inventories", title: "Virtual Inventories" },
  { id: "info", title: "Info" },
  { id: "items", title: "Items" },
  { id: "world", title: "World" },
  { id: "teams", title: "Teams" },
  { id: "misc", title: "Misc" },
  { id: "protection", title: "Protection" },
  { id: "safe-teleport-bypass", title: "Safe Teleport Bypass" },
  { id: "cooldown-bypass", title: "Cooldown Bypass" },
  { id: "delay-bypass", title: "Delay Bypass" },
  { id: "log", title: "Log" },
];

/* ------------------------------------------------------------------ */
/*  YAML parser (minimal, tailored for plugin.yml)                      */
/* ------------------------------------------------------------------ */

/**
 * Parses plugin.yml and extracts commands with category comments,
 * and permissions with their properties and children.
 */
function parsePluginYml(content) {
  const commands = [];     // { name, description, usage, permission, aliases, category }
  const permissions = [];  // { name, description, default, children }

  let section = null;      // "commands" or "permissions"
  let currentItem = null;
  let currentCategory = "Uncategorized";

  const lines = content.split("\n");

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i];
    const trimmed = rawLine.trimEnd();

    // Detect category comments: "  # === Category ===" within commands section
    if (section === "commands") {
      const catMatch = trimmed.match(/^\s*#\s*===\s*(.+?)\s*===\s*$/);
      if (catMatch) {
        currentCategory = catMatch[1].trim();
        continue;
      }
    }

    // Strip inline comments for property parsing, but preserve the raw line
    // for indent detection
    const line = trimmed.replace(/#.*$/, "").trimEnd();
    if (!line.trim()) continue;

    // Top-level section detection (no indent)
    if (/^[a-z]/.test(line)) {
      if (line.startsWith("commands:")) {
        section = "commands";
        currentItem = null;
        currentCategory = "Uncategorized";
        continue;
      }
      if (line.startsWith("permissions:")) {
        section = "permissions";
        currentItem = null;
        continue;
      }
      section = null;
      currentItem = null;
      continue;
    }
    if (!section) continue;

    // 2-space indent = new item (command or permission name)
    // Permission names may contain dots and asterisks, command names may contain ?
    const itemMatch = line.match(/^  ([a-zA-Z?][\w.*?-]*):\s*$/);
    if (itemMatch) {
      const name = itemMatch[1];
      if (section === "commands") {
        currentItem = {
          name,
          description: "",
          usage: "",
          permission: "",
          aliases: [],
          category: currentCategory,
        };
        commands.push(currentItem);
      } else if (section === "permissions") {
        currentItem = {
          name,
          description: "",
          default: "false",
          children: {},
        };
        permissions.push(currentItem);
      }
      continue;
    }

    // Handle "name:" with quotes, e.g. "?":
    if (section === "permissions" || section === "commands") {
      const quotedMatch = line.match(/^  "([^"]+)":\s*$/);
      if (quotedMatch) {
        const name = quotedMatch[1];
        if (section === "commands") {
          currentItem = {
            name,
            description: "",
            usage: "",
            permission: "",
            aliases: [],
            category: currentCategory,
          };
          commands.push(currentItem);
        } else {
          currentItem = {
            name,
            description: "",
            default: "false",
            children: {},
          };
          permissions.push(currentItem);
        }
        continue;
      }
    }

    // 4-space indent = property of current item
    if (currentItem) {
      const kvMatch = line.match(/^\s{4}(\w[\w.-]*):\s*(.+)/);
      if (kvMatch) {
        const [, key, rawValue] = kvMatch;
        let value = rawValue.trim();

        // Strip surrounding quotes
        value = value.replace(/^["']|["']$/g, "").trim();

        if (section === "commands") {
          if (key === "description") currentItem.description = value;
          else if (key === "usage") currentItem.usage = value;
          else if (key === "permission") currentItem.permission = value;
          else if (key === "aliases") {
            // Parse YAML inline array: [a, b, c]
            const arrMatch = rawValue.match(/^\s*\[([^\]]*)\]/);
            if (arrMatch) {
              currentItem.aliases = arrMatch[1]
                .split(",")
                .map((s) => s.trim().replace(/^["']|["']$/g, ""))
                .filter(Boolean);
            } else {
              currentItem.aliases = [value];
            }
          }
        } else if (section === "permissions") {
          if (key === "description") currentItem.description = value;
          else if (key === "default") currentItem.default = value;
          // Children are at 6-space indent, handled below
        }
        continue;
      }

      // 6-space indent = children of a permission (e.g. "      justplugin.fly: true")
      if (section === "permissions") {
        const childMatch = line.match(/^\s{6}([\w.*-]+):\s*(true|false)/);
        if (childMatch) {
          currentItem.children[childMatch[1]] = childMatch[2] === "true";
        }
      }
    }
  }

  return { commands, permissions };
}

/* ------------------------------------------------------------------ */
/*  Generate commands JSON                                              */
/* ------------------------------------------------------------------ */

function generateCommandsJson(commands) {
  // Build a map of categories with stable ordering
  const catMap = new Map(); // id -> { id, title, commands[] }

  // Pre-populate from COMMAND_CATEGORY_MAP to set the order
  const seenIds = new Set();
  for (const mapping of COMMAND_CATEGORY_MAP) {
    if (!seenIds.has(mapping.id)) {
      catMap.set(mapping.id, { id: mapping.id, title: mapping.title, commands: [] });
      seenIds.add(mapping.id);
    }
  }

  // Resolve each command's category label to an id
  for (const cmd of commands) {
    let targetId = "misc"; // fallback
    for (const mapping of COMMAND_CATEGORY_MAP) {
      if (cmd.category === mapping.label) {
        targetId = mapping.id;
        break;
      }
    }

    if (!catMap.has(targetId)) {
      catMap.set(targetId, { id: targetId, title: cmd.category, commands: [] });
    }

    const aliasStr = cmd.aliases.length > 0 ? cmd.aliases.join(", ") : "\u2014";

    catMap.get(targetId).commands.push({
      command: `/${cmd.name}`,
      usage: cmd.usage || `/${cmd.name}`,
      description: cmd.description,
      permission: cmd.permission,
      aliases: aliasStr,
    });
  }

  // Filter out empty categories and return ordered array
  const result = [];
  for (const [, cat] of catMap) {
    if (cat.commands.length > 0) {
      result.push(cat);
    }
  }
  return result;
}

/* ------------------------------------------------------------------ */
/*  Generate permissions JSON                                           */
/* ------------------------------------------------------------------ */

function classifyPermSection(permName) {
  // Special groupings that override prefix matching
  if (permName.endsWith(".unsafetp")) return "safe-teleport-bypass";
  if (permName.endsWith(".cooldownbypass")) return "cooldown-bypass";
  if (permName.endsWith(".delaybypass")) return "delay-bypass";

  // Skip wildcard/parent permissions (justplugin.* and justplugin.player)
  if (permName === "justplugin.*" || permName === "justplugin.player") return null;

  // Find the best (longest) prefix match
  let bestMatch = null;
  let bestLen = 0;
  for (const rule of PERM_CATEGORY_RULES) {
    if (
      (permName === rule.prefix || permName.startsWith(rule.prefix + ".")) &&
      rule.prefix.length > bestLen
    ) {
      bestMatch = rule.section;
      bestLen = rule.prefix.length;
    }
  }
  return bestMatch || "misc";
}

function resolvePermDefault(perm, allPermsMap) {
  // Check if this perm is a child of justplugin.player
  const playerPerm = allPermsMap.get("justplugin.player");
  if (playerPerm && playerPerm.children[perm.name]) return "Player";

  // Check the default field
  const def = perm.default;
  if (def === "op") return "Admin";
  if (def === "true") return "Player";
  if (def === "false") return "Admin";

  return "Admin";
}

function generatePermissionsJson(permissions) {
  // Build a lookup map
  const permMap = new Map();
  for (const p of permissions) {
    permMap.set(p.name, p);
  }

  // Build sections
  const sectionMap = new Map();
  for (const s of PERM_SECTIONS_ORDER) {
    sectionMap.set(s.id, { title: s.title, id: s.id, perms: [] });
  }

  // Player-level permissions (children of justplugin.player)
  const playerPerm = permMap.get("justplugin.player");
  const playerChildren = playerPerm ? Object.keys(playerPerm.children) : [];
  const playerChildSet = new Set(playerChildren);

  for (const perm of permissions) {
    const sectionId = classifyPermSection(perm.name);
    if (sectionId === null) continue; // skip wildcard/player parent perms

    if (!sectionMap.has(sectionId)) {
      sectionMap.set(sectionId, {
        title: sectionId.charAt(0).toUpperCase() + sectionId.slice(1),
        id: sectionId,
        perms: [],
      });
    }

    const def = playerChildSet.has(perm.name) ? "Player" : "Admin";

    // Determine associated commands from the permission name
    // This is a best-effort heuristic
    let commands = "";
    const basePerm = perm.name.replace(/^justplugin\./, "");
    if (!basePerm.includes(".") || basePerm.endsWith(".others")) {
      // Simple permission like justplugin.fly or justplugin.fly.others
      const cmdBase = basePerm.replace(/\.others$/, "").replace(/\..*$/, "");
      commands = `/${cmdBase}`;
    }

    sectionMap.get(sectionId).perms.push({
      perm: perm.name,
      desc: perm.description,
      def,
      commands,
    });
  }

  // Filter out empty sections and return ordered array
  const result = [];
  for (const s of PERM_SECTIONS_ORDER) {
    const section = sectionMap.get(s.id);
    if (section && section.perms.length > 0) {
      result.push(section);
    }
  }

  // Add any sections not in the predefined order
  for (const [id, section] of sectionMap) {
    if (!PERM_SECTIONS_ORDER.find((s) => s.id === id) && section.perms.length > 0) {
      result.push(section);
    }
  }

  return result;
}

/* ------------------------------------------------------------------ */
/*  Main                                                                */
/* ------------------------------------------------------------------ */

function main() {
  // Read plugin.yml
  if (!fs.existsSync(PLUGIN_YML)) {
    console.error(`ERROR: plugin.yml not found at ${PLUGIN_YML}`);
    process.exit(1);
  }

  const content = fs.readFileSync(PLUGIN_YML, "utf8");
  const { commands, permissions } = parsePluginYml(content);

  console.log(`Parsed plugin.yml: ${commands.length} commands, ${permissions.length} permissions`);

  // Generate JSON
  const commandsJson = generateCommandsJson(commands);
  const permissionsJson = generatePermissionsJson(permissions);

  const totalCmds = commandsJson.reduce((sum, cat) => sum + cat.commands.length, 0);
  const totalPerms = permissionsJson.reduce((sum, sec) => sum + sec.perms.length, 0);

  console.log(`\nGenerated commands JSON: ${commandsJson.length} categories, ${totalCmds} commands`);
  for (const cat of commandsJson) {
    console.log(`  ${cat.title}: ${cat.commands.length} commands`);
  }

  console.log(`\nGenerated permissions JSON: ${permissionsJson.length} sections, ${totalPerms} permissions`);
  for (const sec of permissionsJson) {
    console.log(`  ${sec.title}: ${sec.perms.length} permissions`);
  }

  const commandsStr = JSON.stringify(commandsJson, null, 2) + "\n";
  const permsStr = JSON.stringify(permissionsJson, null, 2) + "\n";

  if (CHECK_MODE) {
    // Compare with existing files
    let drift = false;

    if (fs.existsSync(COMMANDS_OUT)) {
      const existing = fs.readFileSync(COMMANDS_OUT, "utf8");
      if (existing !== commandsStr) {
        console.error("\nERROR: generated-commands.json is out of date!");
        console.error("Run 'node scripts/generate-docs.js' to regenerate.");
        drift = true;
      } else {
        console.log("\ngenerated-commands.json is up to date.");
      }
    } else {
      console.error("\nERROR: generated-commands.json does not exist!");
      console.error("Run 'node scripts/generate-docs.js' to generate it.");
      drift = true;
    }

    if (fs.existsSync(PERMISSIONS_OUT)) {
      const existing = fs.readFileSync(PERMISSIONS_OUT, "utf8");
      if (existing !== permsStr) {
        console.error("\nERROR: generated-permissions.json is out of date!");
        console.error("Run 'node scripts/generate-docs.js' to regenerate.");
        drift = true;
      } else {
        console.log("generated-permissions.json is up to date.");
      }
    } else {
      console.error("\nERROR: generated-permissions.json does not exist!");
      console.error("Run 'node scripts/generate-docs.js' to generate it.");
      drift = true;
    }

    if (drift) {
      process.exit(1);
    } else {
      console.log("\nAll generated docs are up to date.");
    }
  } else {
    // Write output files
    if (!fs.existsSync(DATA_DIR)) {
      fs.mkdirSync(DATA_DIR, { recursive: true });
    }

    fs.writeFileSync(COMMANDS_OUT, commandsStr, "utf8");
    console.log(`\nWrote ${COMMANDS_OUT}`);

    fs.writeFileSync(PERMISSIONS_OUT, permsStr, "utf8");
    console.log(`Wrote ${PERMISSIONS_OUT}`);

    console.log("\nDone! Generated documentation files are ready.");
  }
}

main();
