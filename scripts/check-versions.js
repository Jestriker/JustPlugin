#!/usr/bin/env node
/**
 * check-versions.js — Verifies version consistency across all docs and config files.
 * Usage: node scripts/check-versions.js
 * Exits with code 1 if any mismatches are found.
 */

const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
let errors = 0;

// 1. Read canonical version from build.gradle
const gradle = fs.readFileSync(path.join(ROOT, "build.gradle"), "utf8");
const versionMatch = gradle.match(/version\s*=\s*'([^']+)'/);
if (!versionMatch) {
  console.error("ERROR: Could not find version in build.gradle");
  process.exit(1);
}
const CANONICAL = versionMatch[1];
console.log(`Canonical version (build.gradle): ${CANONICAL}\n`);

// 2. Check each file for version consistency
const checks = [
  {
    file: "COMMANDS.md",
    pattern: /\*\*Version:\*\*\s*([\d.]+)/,
    label: "COMMANDS.md header",
  },
  {
    file: "PERMISSIONS.md",
    pattern: /\*\*Version:\*\*\s*([\d.]+)/,
    label: "PERMISSIONS.md header",
  },
  {
    file: "SCOREBOARD.md",
    pattern: /\*\*Version:\*\*\s*([\d.]+)/,
    label: "SCOREBOARD.md header",
  },
  {
    file: "ECOSYSTEM.md",
    pattern: /\*\*Version:\*\*\s*([\d.]+)/,
    label: "ECOSYSTEM.md header",
  },
  {
    file: "website/src/data/versions.ts",
    pattern: /LATEST_VERSION\s*=\s*"([^"]+)"/,
    label: "versions.ts LATEST_VERSION",
  },
  {
    file: "website/src/data/constants.ts",
    pattern: /PLUGIN_VERSION\s*=\s*"([^"]+)"/,
    label: "constants.ts PLUGIN_VERSION",
  },
  {
    file: "README.md",
    pattern: /Release%20Version-([\d.]+)-/,
    label: "README.md badge",
  },
  {
    file: "DESCRIPTION.md",
    pattern: /Release%20Version-([\d.]+)-/,
    label: "DESCRIPTION.md badge",
  },
];

for (const check of checks) {
  const filePath = path.join(ROOT, check.file);
  if (!fs.existsSync(filePath)) {
    console.log(`  SKIP  ${check.label} — file not found`);
    continue;
  }
  const content = fs.readFileSync(filePath, "utf8");
  const match = content.match(check.pattern);
  if (!match) {
    console.log(`  WARN  ${check.label} — pattern not found`);
    continue;
  }
  const found = match[1];
  if (found === CANONICAL) {
    console.log(`  OK    ${check.label}: ${found}`);
  } else {
    console.log(`  FAIL  ${check.label}: found ${found}, expected ${CANONICAL}`);
    errors++;
  }
}

console.log(`\n${errors === 0 ? "All versions consistent." : `${errors} mismatch(es) found!`}`);
process.exit(errors > 0 ? 1 : 0);
