#!/usr/bin/env node
/**
 * sync-docs.js — Audits documentation sync between plugin.yml and markdown/wiki docs.
 * Usage: node scripts/sync-docs.js
 * Exits with code 1 if missing entries are found.
 */

const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
let warnings = 0;

// Simple YAML parser for plugin.yml command/permission sections
function parsePluginYml(content) {
  const commands = {};
  const permissions = {};
  let section = null; // "commands" or "permissions"
  let currentItem = null;

  for (const rawLine of content.split("\n")) {
    // Strip comments
    const line = rawLine.replace(/#.*$/, "").trimEnd();
    if (!line.trim()) continue;

    // Top-level section detection (no indent)
    if (/^[a-z]/.test(line)) {
      if (line.startsWith("commands:")) { section = "commands"; currentItem = null; continue; }
      if (line.startsWith("permissions:")) { section = "permissions"; currentItem = null; continue; }
      section = null;
      currentItem = null;
      continue;
    }
    if (!section) continue;

    // 2-space indent = new item (command or permission name)
    const itemMatch = line.match(/^  ([a-zA-Z][\w.*-]*):\s*$/);
    if (itemMatch) {
      currentItem = itemMatch[1];
      if (section === "commands") commands[currentItem] = {};
      if (section === "permissions") permissions[currentItem] = {};
      continue;
    }

    // 4-space indent = property of current item
    if (currentItem) {
      const kvMatch = line.match(/^\s{4}(\w[\w-]*):\s*(.+)/);
      if (kvMatch) {
        const [, key, value] = kvMatch;
        const clean = value.replace(/^["']|["']$/g, "").trim();
        const target = section === "commands" ? commands : permissions;
        if (target[currentItem]) target[currentItem][key] = clean;
      }
    }
  }
  return { commands, permissions };
}

// Read plugin.yml
const pluginYml = fs.readFileSync(path.join(ROOT, "src/main/resources/plugin.yml"), "utf8");
const { commands, permissions } = parsePluginYml(pluginYml);

const cmdCount = Object.keys(commands).length;
const permCount = Object.keys(permissions).length;

console.log(`plugin.yml: ${cmdCount} commands, ${permCount} permissions\n`);

// Check COMMANDS.md
const commandsMd = fs.readFileSync(path.join(ROOT, "COMMANDS.md"), "utf8").toLowerCase();
const missingCmds = [];
for (const cmd of Object.keys(commands)) {
  if (!commandsMd.includes(`/${cmd}`) && !commandsMd.includes(`| ${cmd}`) && !commandsMd.includes(cmd.toLowerCase())) {
    missingCmds.push(cmd);
  }
}

if (missingCmds.length > 0) {
  console.log(`COMMANDS.md missing ${missingCmds.length} commands:`);
  missingCmds.forEach(c => console.log(`  - /${c}`));
  warnings += missingCmds.length;
} else {
  console.log("COMMANDS.md: All commands found.");
}

// Check PERMISSIONS.md
const permsMd = fs.readFileSync(path.join(ROOT, "PERMISSIONS.md"), "utf8").toLowerCase();
const missingPerms = [];
for (const perm of Object.keys(permissions)) {
  if (!permsMd.includes(perm.toLowerCase())) {
    missingPerms.push(perm);
  }
}

if (missingPerms.length > 0) {
  console.log(`\nPERMISSIONS.md missing ${missingPerms.length} permissions:`);
  missingPerms.forEach(p => console.log(`  - ${p}`));
  warnings += missingPerms.length;
} else {
  console.log("PERMISSIONS.md: All permissions found.");
}

console.log(`\n${warnings === 0 ? "All docs in sync." : `${warnings} missing entries found!`}`);
process.exit(warnings > 0 ? 1 : 0);
