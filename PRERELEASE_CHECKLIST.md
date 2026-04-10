# Pre-Release Verification Checklist

> **This file is instructions for Claude Code.** Before publishing any new version, the user will ask you to "go over the checklist" or "run the balance file." Follow these instructions exactly.

---

## How to Execute

1. **Launch a separate agent for EACH task below** — run them all **in parallel** for speed
2. **Launch an Agents Orchestrator** as the head agent — it reviews ALL agent results when they complete
3. The orchestrator must verify each agent completed its task fully and correctly
4. **Print a summary to the user** with:
   - Each task: PASS or FAIL with details
   - Any missing items, errors, or inconsistencies found
   - A final GO / NO-GO recommendation for release

---

## Task 1: Java Build & Test Coverage
**Agent type:** Code Reviewer

- Run `./gradlew clean build` — must pass with zero errors
- Run `./gradlew test` — all tests must pass (report count: X/X passing)
- Check for any new Java source files that don't have corresponding tests
- Report test coverage gaps (managers, commands, listeners without tests)
- Check for any `@SuppressWarnings` that might be hiding real issues
- Verify no `catch (Exception e) {}` silent catches were added

---

## Task 2: Version Consistency
**Agent type:** Technical Writer

- Run `node scripts/check-versions.js` — all 8 files must match
- Run `node scripts/sync-docs.js` — all commands and permissions must be in sync
- Run `node scripts/generate-docs.js` — verify generated JSON matches current state
- Check `build.gradle` version matches `CHANGELOG.md` latest entry
- Check `plugin.yml` version placeholder is correct
- Verify the changelog has an entry for the current version with correct date

---

## Task 3: Documentation Accuracy
**Agent type:** Technical Writer

- Read COMMANDS.md — verify every command listed exists in plugin.yml and vice versa
- Read PERMISSIONS.md — verify every permission listed exists in plugin.yml and vice versa
- Read ECOSYSTEM.md — verify API method signatures match actual Java interfaces
- Read SCOREBOARD.md — verify all placeholders listed are implemented
- Read KITS.md — verify kit commands and permissions are current
- Read FORMATTING.md — verify MiniMessage examples are correct
- Read DESCRIPTION.md and DESCRIPTION_PLAIN.txt — verify feature list is current
- Read README.md — verify feature highlights, command counts, and badges are accurate
- Check for any features that were added/removed but not updated in docs

---

## Task 4: Spelling & Grammar Check
**Agent type:** Technical Writer

- Scan ALL .md files for spelling errors, typos, and grammatical issues
- Scan ALL wiki page .tsx files for spelling errors in user-visible text
- Scan ALL Java files for spelling errors in user-facing messages (CC.error, CC.success, etc.)
- Scan config.yml comments for spelling errors
- Scan plugin.yml descriptions for spelling errors
- Check for inconsistent capitalization (JustPlugin vs justplugin vs Justplugin)
- Check for inconsistent terminology (e.g., "teleport" vs "tp", "permission" vs "perm")
- Report every issue found with file path and line number

---

## Task 5: Website Build & Link Verification
**Agent type:** Frontend Developer

- Run `cd website && npx next build` — must pass with zero errors
- Check all internal links in the wiki (href values) point to existing pages
- Check all external links (GitHub, Discord, Modrinth) are correct URLs
- Verify the sitemap.xml includes all pages
- Verify robots.txt is correct
- Check that no page has broken imports or missing components
- Verify the marketing home page renders without errors
- Verify the contact form API route compiles
- Check that the 404 and error pages work correctly

---

## Task 6: SEO & Metadata Verification
**Agent type:** SEO Specialist

- Verify every page has a unique `<title>` tag
- Verify every page has a meta description
- Verify Open Graph tags are present in the root layout
- Verify Twitter card meta tags are present (invisible, head only)
- Verify JSON-LD structured data is valid (Organization, SoftwareApplication, FAQPage)
- Verify sitemap.xml lists all public pages with correct priorities
- Verify no duplicate meta tags across layout and page-level metadata
- Check heading hierarchy on all pages (one h1, proper h2/h3 nesting)

---

## Task 7: Security Audit
**Agent type:** Security Engineer

- Verify SudoCommand has privilege escalation protection (self-sudo, op check, blocked commands)
- Verify BanIpCommand uses permission-filtered broadcast (not Bukkit.broadcast to all)
- Verify WebEditorManager auth token is not logged in full
- Verify WebEditorManager CSRF validation rejects when Origin+Referer are both absent
- Verify all SQL operations use PreparedStatement (no string concatenation in queries)
- Verify SQLite and MySQL savePlayerData use transactions (setAutoCommit false + commit/rollback)
- Verify HikariCP has leak detection enabled
- Verify economy pay() uses synchronized locks
- Verify .env files are in .gitignore
- Check for any hardcoded secrets, API keys, or tokens in source code
- Check contact form has rate limiting and input validation

---

## Task 8: StorageProvider Completeness
**Agent type:** Database Optimizer

- Verify StorageProvider interface has methods for ALL data systems (player data, warps, bans, teams, jails, kits, mutes, warns, mail, homes, nicknames, tags, transactions, vaults, ignores)
- Verify SQLiteStorageProvider implements ALL interface methods
- Verify MySQLStorageProvider implements ALL interface methods
- Verify YamlStorageProvider implements ALL interface methods
- Verify ALL managers route through StorageProvider when database is configured:
  - EconomyManager, MuteManager, WarnManager, JailManager, KitManager
  - MailManager, HomeManager, NickManager, TagManager, TransactionManager
  - VaultManager, IgnoreManager
- Check that no manager still has hardcoded YAML-only paths when database is configured

---

## Task 9: Accessibility Audit
**Agent type:** Accessibility Auditor

- Verify skip-to-content link exists on docs pages
- Verify all interactive elements have aria-labels
- Verify search modal has dialog role and aria-modal
- Verify sidebar has proper aria attributes
- Verify code block copy button is keyboard-accessible (focus:opacity-100)
- Verify focus-visible styles are defined in globals.css
- Verify color contrast meets WCAG AA (check --text-muted on dark backgrounds)
- Verify all images have descriptive alt text

---

## Task 10: Configuration & Plugin.yml Audit
**Agent type:** Code Reviewer

- Verify plugin.yml has correct entries for ALL registered commands
- Verify plugin.yml has correct entries for ALL permissions with proper defaults
- Verify config.yml default values are sensible for first-time users
- Verify database.yml has correct default configuration
- Verify all command-settings toggles in config.yml match registered commands
- Check for any commands registered in Java but missing from plugin.yml
- Check for any permissions used in code but missing from plugin.yml
- Verify the web editor config file list is complete

---

## Task 11: Performance Check
**Agent type:** Performance Benchmarker

- Search for any synchronous savePlayerData calls in event handlers (should be async)
- Search for any Bukkit.getOfflinePlayer(name) calls on the main thread (blocks for Mojang API)
- Search for any .join() calls on CompletableFuture in listeners
- Verify scoreboard/tab updates use reasonable intervals
- Check for any unbounded collections that could leak memory
- Verify DataManager cache is properly loaded on join and unloaded on quit

---

## Task 12: Config Text Strings & Placeholder Audit
**Agent type:** Code Reviewer

- For every feature that was changed/added/edited, verify that ALL user-facing text strings are configurable via the message files in `plugins/JustPlugin/texts/`
- Search Java command and listener files for hardcoded user-facing strings (CC.error, CC.success, CC.warning with inline text) that should instead come from MessageManager
- Verify placeholders are available where they make sense:
  - Economy messages should support `{amount}`, `{balance}`, `{player}` placeholders
  - Moderation messages should support `{player}`, `{reason}`, `{duration}` placeholders
  - Teleport messages should support `{player}`, `{location}` placeholders
  - Kit messages should support `{kit}`, `{cooldown}` placeholders
- Check that the scoreboard placeholder list in SCOREBOARD.md matches what PlaceholderResolver.java actually supports
- Verify the web editor's config file list covers all config files that exist in the plugin
- Check that config.yml comments describe all available placeholders for each text field

---

## Task 13: Placeholder Documentation Completeness
**Agent type:** Technical Writer

- Read `src/main/java/org/justme/justPlugin/util/PlaceholderResolver.java` — extract EVERY placeholder from the resolveKey() method (the switch/if-else chain). This is the authoritative source.
- Read `website/src/app/placeholders/page.tsx` — verify EVERY placeholder from the Java code appears on the wiki page with correct name and description
- Read `SCOREBOARD.md` — verify placeholder list matches the Java code
- Check for phantom placeholders (documented on wiki but don't exist in code) — these must be removed
- Check for missing placeholders (exist in code but not documented) — these must be added
- Verify placeholder aliases are documented (e.g., `{bal}` is an alias for `{balance}`)
- Verify the placeholder count claim in README.md and the wiki home page matches the actual count

---

## Final Step: Orchestrator Summary

The Agents Orchestrator reviews ALL agent results and prints:

```
=== PRE-RELEASE VERIFICATION REPORT ===
Version: X.X
Date: YYYY-MM-DD

Task 1 (Build & Tests):     PASS/FAIL — details
Task 2 (Version Sync):      PASS/FAIL — details
Task 3 (Doc Accuracy):      PASS/FAIL — details
Task 4 (Spelling/Grammar):  PASS/FAIL — details
Task 5 (Website Build):     PASS/FAIL — details
Task 6 (SEO):               PASS/FAIL — details
Task 7 (Security):          PASS/FAIL — details
Task 8 (StorageProvider):   PASS/FAIL — details
Task 9 (Accessibility):     PASS/FAIL — details
Task 10 (Config/Plugin):    PASS/FAIL — details
Task 11 (Performance):      PASS/FAIL — details
Task 12 (Config Strings):      PASS/FAIL — details
Task 13 (Placeholder Docs):    PASS/FAIL — details

OVERALL: GO / NO-GO
Issues to fix before release: [list]
```
