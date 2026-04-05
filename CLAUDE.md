# JustPlugin - Agent Instructions

## Project Overview

JustPlugin is a lightweight, fully configurable all-in-one server management plugin for Paper, Purpur, and Folia (Minecraft 1.21.11+). Built with Java 21, Gradle, and the Paper API.

## MANDATORY: Documentation Sync on Every Change

**Every agent working on this project MUST update all relevant documentation when making ANY change to the plugin — whether adding, modifying, or removing features, commands, permissions, configs, or API methods.**

### What to update:

#### 1. Wiki Website (`website/src/app/`)
The wiki is a Next.js + pnpm + Tailwind site in the `website/` folder. Update the relevant pages:

- **Commands page** (`website/src/app/commands/page.tsx`) — Add/remove/modify command entries in the correct category
- **Permissions page** (`website/src/app/permissions/page.tsx`) — Add/remove/modify permission entries in the correct category
- **Configuration page** (`website/src/app/configuration/page.tsx`) — Update config examples and descriptions
- **API Reference page** (`website/src/app/api/page.tsx`) — Update API methods, interfaces, and examples
- **Feature pages** (`website/src/app/features/<feature>/page.tsx`) — Update the specific feature page(s) affected
- **Features index** (`website/src/app/features/page.tsx`) — Update if a feature is added or removed
- **Home page** (`website/src/app/page.tsx`) — Update feature cards if features are added/removed
- **FAQ page** (`website/src/app/faq/page.tsx`) — Update if the change affects any FAQ answers
- **Version Support page** (`website/src/app/version-support/page.tsx`) — Update if platform support changes
- **Sidebar navigation** (`website/src/components/Sidebar.tsx`) — Update if pages are added/removed

#### 2. Project Documentation (root markdown files)
- **`README.md`** — Update feature highlights, command counts, permission counts, and any relevant sections
- **`COMMANDS.md`** — Add/remove/modify command entries with full details (usage, description, permission, aliases)
- **`PERMISSIONS.md`** — Add/remove/modify permission entries with hierarchy updates
- **`ECOSYSTEM.md`** — Update if API interfaces or methods change
- **`SCOREBOARD.md`** — Update if placeholders or scoreboard features change
- **`KITS.md`** — Update if kit system changes
- **`FORMATTING.md`** — Update if formatting support changes
- **`CHANGELOG.md`** — Add entry for the change under the current version

#### 3. Description Files
- **`DESCRIPTION.md`** — Update the short markdown description if the change is significant (new feature, removed feature, major change)
- **`DESCRIPTION_PLAIN.txt`** — Update the plain-text description to match

### Rules:
- **Never skip documentation updates.** If you change a command, its entry must be updated in COMMANDS.md, the wiki commands page, the relevant feature page, and permissions if applicable.
- **If you remove a feature/command/permission**, remove it from ALL locations — wiki pages, markdown docs, descriptions, sidebar nav.
- **If you add a feature**, add it to ALL relevant locations — wiki feature page (create one if needed), sidebar nav, features index, commands page, permissions page, config page, README, COMMANDS.md, PERMISSIONS.md, descriptions.
- **Verify the wiki builds** after changes: `cd website && npx next build`
- **Keep counts accurate** — command counts (200+), permission counts (150+), placeholder counts (50+) in README and home page.

## Project Structure

```
JustPlugin/
├── src/main/java/org/justme/justPlugin/   # Java source
│   ├── api/                                # Public API (EconomyAPI, PunishmentAPI, VanishAPI)
│   ├── commands/                           # All commands organized by category
│   ├── listeners/                          # Event listeners
│   ├── gui/                                # GUI classes
│   ├── managers/                           # 40+ manager classes
│   └── util/                               # Utilities
├── src/main/resources/
│   ├── plugin.yml                          # Plugin descriptor
│   └── [config defaults]
├── website/                                # Next.js documentation wiki
│   ├── src/app/                            # Pages (App Router)
│   └── src/components/                     # Shared components
├── README.md                               # GitHub README
├── COMMANDS.md                             # Full command reference
├── PERMISSIONS.md                          # Full permission reference
├── ECOSYSTEM.md                            # Developer API docs
├── SCOREBOARD.md                           # Scoreboard placeholder reference
├── KITS.md                                 # Kit system guide
├── FORMATTING.md                           # MiniMessage formatting guide
├── CHANGELOG.md                            # Version history
├── DESCRIPTION.md                          # Short markdown description
└── DESCRIPTION_PLAIN.txt                   # Short plain-text description
```

## Build Commands

- **Plugin**: `./gradlew build` (output in `build/libs/`)
- **Wiki dev**: `cd website && pnpm dev` (http://localhost:3000)
- **Wiki build**: `cd website && npx next build`
- **Tests**: `./gradlew test`

## Key Technical Details

- Java 21, Paper API 1.21.11-R0.1-SNAPSHOT
- Gradle with ShadowJar (shades bStats, HikariCP, SLF4J)
- Optional deps: Vault, LuckPerms, PlaceholderAPI
- Folia support via SchedulerUtil
- Storage: YAML, SQLite, or MySQL (configured in database.yml)
- Wiki: Next.js 16, TypeScript, Tailwind CSS, pnpm
