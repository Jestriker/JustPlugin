export interface SearchEntry {
  title: string;
  category: "Command" | "Permission" | "Feature" | "Page" | "Config";
  description: string;
  href: string;
  keywords?: string;
}

export const searchIndex: SearchEntry[] = [
  // ── Pages ──────────────────────────────────────────────
  { title: "Home", category: "Page", description: "JustPlugin documentation homepage", href: "/", keywords: "start intro overview" },
  { title: "Features Overview", category: "Page", description: "Overview of all JustPlugin features", href: "/features", keywords: "list all" },
  { title: "Commands Reference", category: "Page", description: "Full list of all commands", href: "/commands", keywords: "list all commands" },
  { title: "Permissions Reference", category: "Page", description: "Full list of all permission nodes", href: "/permissions", keywords: "perms nodes" },
  { title: "Configuration", category: "Page", description: "Plugin configuration guide", href: "/configuration", keywords: "config settings yml yaml" },
  { title: "API Reference", category: "Page", description: "Developer API documentation", href: "/api", keywords: "developer api hooks events" },
  { title: "Version Support", category: "Page", description: "Supported Minecraft versions and platforms", href: "/version-support", keywords: "minecraft paper purpur folia spigot" },
  { title: "FAQ", category: "Page", description: "Frequently asked questions", href: "/faq", keywords: "help questions troubleshoot" },

  // ── Features ───────────────────────────────────────────
  { title: "Economy", category: "Feature", description: "Player balances, payments, Vault integration, and transaction history", href: "/features/economy", keywords: "money balance pay vault bank transactions" },
  { title: "Teleportation", category: "Feature", description: "TPA, random teleport, spawn, and back", href: "/features/teleportation", keywords: "tpa tpr spawn back tp" },
  { title: "Warps & Homes", category: "Feature", description: "Server warps and player home locations", href: "/features/warps-and-homes", keywords: "warp home sethome setwarp" },
  { title: "Moderation", category: "Feature", description: "Ban, kick, mute, warn, and freeze players", href: "/features/moderation", keywords: "ban kick mute warn freeze tempban" },
  { title: "Jail System", category: "Feature", description: "Jail and unjail players as punishment", href: "/features/jail", keywords: "jail unjail prison punishment" },
  { title: "Kit System", category: "Feature", description: "Configurable item kits with cooldowns", href: "/features/kits", keywords: "kit kits items cooldown" },
  { title: "Vanish", category: "Feature", description: "Become invisible to other players", href: "/features/vanish", keywords: "vanish invisible hidden" },
  { title: "Teams", category: "Feature", description: "Player teams and groups", href: "/features/teams", keywords: "team group clan party" },
  { title: "Trading", category: "Feature", description: "Player-to-player item trading", href: "/features/trading", keywords: "trade exchange items" },
  { title: "Skin Restorer", category: "Feature", description: "Player skin management", href: "/features/skins", keywords: "skin restore change texture" },
  { title: "Maintenance Mode", category: "Feature", description: "Lock the server for maintenance", href: "/features/maintenance", keywords: "maintenance lock whitelist" },
  { title: "Scoreboard", category: "Feature", description: "Custom sidebar scoreboards", href: "/features/scoreboard", keywords: "scoreboard sidebar display stats" },
  { title: "Tab List", category: "Feature", description: "Customizable tab list header and footer", href: "/features/tab-list", keywords: "tablist tab header footer player list" },
  { title: "MOTD Profiles", category: "Feature", description: "Server list message of the day", href: "/features/motd", keywords: "motd server list ping message" },
  { title: "Chat & Messaging", category: "Feature", description: "Chat formatting, private messages, and spy", href: "/features/chat", keywords: "chat msg message private whisper format" },
  { title: "Mail System", category: "Feature", description: "Offline messaging between players", href: "/features/mail", keywords: "mail send read offline message" },
  { title: "Nicknames & Tags", category: "Feature", description: "Custom display names and tags", href: "/features/nicknames-tags", keywords: "nick nickname tag prefix suffix display name" },
  { title: "AFK System", category: "Feature", description: "Auto-AFK detection and manual toggle", href: "/features/afk", keywords: "afk away idle timeout" },
  { title: "Automated Messages", category: "Feature", description: "Scheduled broadcast messages", href: "/features/automated-messages", keywords: "broadcast auto message scheduled announcement" },
  { title: "Virtual Inventories", category: "Feature", description: "Portable crafting, enderchest, player vaults, and disposal", href: "/features/virtual-inventories", keywords: "workbench crafting enderchest disposal trash anvil vault pv" },
  { title: "World Management", category: "Feature", description: "World creation, loading, and per-world settings", href: "/features/world-management", keywords: "world create load teleport multiworld" },
  { title: "Backup & Export", category: "Feature", description: "Server backup and data export tools", href: "/features/backup", keywords: "backup export save restore" },

  // ── Commands ───────────────────────────────────────────
  { title: "/tpa", category: "Command", description: "Request to teleport to another player", href: "/features/teleportation", keywords: "teleport request" },
  { title: "/tpahere", category: "Command", description: "Request another player to teleport to you", href: "/features/teleportation", keywords: "teleport here request" },
  { title: "/tpr", category: "Command", description: "Teleport to a random location", href: "/features/teleportation", keywords: "random teleport wild" },
  { title: "/back", category: "Command", description: "Return to your last location", href: "/features/teleportation", keywords: "return previous location death" },
  { title: "/spawn", category: "Command", description: "Teleport to the server spawn", href: "/features/teleportation", keywords: "spawn teleport" },
  { title: "/ban", category: "Command", description: "Ban a player from the server", href: "/features/moderation", keywords: "ban player punish" },
  { title: "/tempban", category: "Command", description: "Temporarily ban a player", href: "/features/moderation", keywords: "temp ban temporary" },
  { title: "/kick", category: "Command", description: "Kick a player from the server", href: "/features/moderation", keywords: "kick remove" },
  { title: "/mute", category: "Command", description: "Mute a player in chat", href: "/features/moderation", keywords: "mute silence chat" },
  { title: "/warn", category: "Command", description: "Issue a warning to a player", href: "/features/moderation", keywords: "warn warning" },
  { title: "/jail", category: "Command", description: "Send a player to jail", href: "/features/jail", keywords: "jail prison" },
  { title: "/warp", category: "Command", description: "Teleport to a named warp point", href: "/features/warps-and-homes", keywords: "warp teleport" },
  { title: "/home", category: "Command", description: "Teleport to your saved home", href: "/features/warps-and-homes", keywords: "home teleport" },
  { title: "/sethome", category: "Command", description: "Set your home location", href: "/features/warps-and-homes", keywords: "set home save" },
  { title: "/kit", category: "Command", description: "Claim a configured item kit", href: "/features/kits", keywords: "kit items claim" },
  { title: "/balance", category: "Command", description: "Check your or another player's balance", href: "/features/economy", keywords: "bal money check" },
  { title: "/pay", category: "Command", description: "Send money to another player", href: "/features/economy", keywords: "pay transfer money send" },
  { title: "/vanish", category: "Command", description: "Toggle invisibility", href: "/features/vanish", keywords: "vanish hide invisible" },
  { title: "/fly", category: "Command", description: "Toggle creative flight", href: "/commands", keywords: "fly flight creative" },
  { title: "/god", category: "Command", description: "Toggle god mode (invincibility)", href: "/commands", keywords: "god invincible immortal" },
  { title: "/heal", category: "Command", description: "Restore full health", href: "/commands", keywords: "heal health restore" },
  { title: "/feed", category: "Command", description: "Restore full hunger", href: "/commands", keywords: "feed food hunger" },
  { title: "/msg", category: "Command", description: "Send a private message", href: "/features/chat", keywords: "message whisper tell pm dm" },
  { title: "/mail", category: "Command", description: "Send or read offline mail", href: "/features/mail", keywords: "mail offline message" },
  { title: "/nick", category: "Command", description: "Set your display name", href: "/features/nicknames-tags", keywords: "nickname display name" },
  { title: "/tag", category: "Command", description: "Set your custom tag", href: "/features/nicknames-tags", keywords: "tag prefix suffix" },
  { title: "/trade", category: "Command", description: "Start a trade with another player", href: "/features/trading", keywords: "trade exchange" },
  { title: "/maintenance", category: "Command", description: "Toggle server maintenance mode", href: "/features/maintenance", keywords: "maintenance lock" },
  { title: "/afk", category: "Command", description: "Toggle AFK status", href: "/features/afk", keywords: "afk away" },
  { title: "/pv", category: "Command", description: "Open a player vault", href: "/features/virtual-inventories", keywords: "vault playervault storage" },
  { title: "/near", category: "Command", description: "Show nearby players with distance and direction", href: "/commands", keywords: "near nearby players radar" },
  { title: "/transactions", category: "Command", description: "View economy transaction history", href: "/features/economy", keywords: "transactions history txhistory economy log" },
  { title: "/repair", category: "Command", description: "Repair the held item to max durability", href: "/commands", keywords: "repair fix durability" },
  { title: "/enchant", category: "Command", description: "Apply an enchantment to the held item", href: "/commands", keywords: "enchant enchantment" },

  // ── Permissions ────────────────────────────────────────
  { title: "justplugin.*", category: "Permission", description: "Grants access to all JustPlugin commands", href: "/permissions", keywords: "all wildcard star admin" },
  { title: "justplugin.player", category: "Permission", description: "Default player permission set", href: "/permissions", keywords: "default player basic" },
  { title: "justplugin.ban", category: "Permission", description: "Permission to ban and unban players", href: "/permissions", keywords: "ban moderation" },
  { title: "justplugin.fly", category: "Permission", description: "Permission to use /fly command", href: "/permissions", keywords: "fly flight" },
  { title: "justplugin.gamemode", category: "Permission", description: "Permission to change game modes", href: "/permissions", keywords: "gamemode creative survival adventure spectator" },
  { title: "justplugin.vault", category: "Permission", description: "Permission to use player vaults", href: "/permissions", keywords: "vault pv storage" },
  { title: "justplugin.near", category: "Permission", description: "Permission to use /near command", href: "/permissions", keywords: "near nearby players" },
  { title: "justplugin.transactions", category: "Permission", description: "Permission to view transaction history", href: "/permissions", keywords: "transactions history economy" },
  { title: "justplugin.repair", category: "Permission", description: "Permission to repair items", href: "/permissions", keywords: "repair fix" },
  { title: "justplugin.enchant", category: "Permission", description: "Permission to enchant items", href: "/permissions", keywords: "enchant enchantment" },

  // ── Config ─────────────────────────────────────────────
  { title: "config.yml", category: "Config", description: "Main plugin configuration file", href: "/configuration", keywords: "config settings main" },
  { title: "messages.yml", category: "Config", description: "Customizable message templates", href: "/configuration", keywords: "messages lang language locale" },
];
