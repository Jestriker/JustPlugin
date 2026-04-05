"use client";

import { useState, useMemo } from "react";
import PageHeader from "@/components/PageHeader";
import { PLUGIN_VERSION } from "@/data/constants";

/* ------------------------------------------------------------------ */
/*  Types                                                              */
/* ------------------------------------------------------------------ */

interface Command {
  command: string;
  usage: string;
  description: string;
  permission: string;
  aliases: string;
}

interface Category {
  id: string;
  title: string;
  commands: Command[];
}

/* ------------------------------------------------------------------ */
/*  Data                                                               */
/* ------------------------------------------------------------------ */

const categories: Category[] = [
  {
    id: "teleportation",
    title: "Teleportation",
    commands: [
      { command: "/tpa", usage: "/tpa <player>", description: "Send a teleport request to a player", permission: "justplugin.tpa", aliases: "—" },
      { command: "/tpaccept", usage: "/tpaccept", description: "Accept an incoming teleport request", permission: "justplugin.tpaccept", aliases: "tpyes" },
      { command: "/tpacancel", usage: "/tpacancel", description: "Cancel your outgoing teleport request", permission: "justplugin.tpacancel", aliases: "—" },
      { command: "/tpreject", usage: "/tpreject", description: "Reject an incoming teleport request", permission: "justplugin.tpreject", aliases: "tpdeny, tpno" },
      { command: "/tpahere", usage: "/tpahere <player>", description: "Request a player to teleport to you", permission: "justplugin.tpahere", aliases: "—" },
      { command: "/tppos", usage: "/tppos <x> <y> <z> [world]", description: "Teleport to exact coordinates", permission: "justplugin.tppos", aliases: "tpposition" },
      { command: "/tpr", usage: "/tpr", description: "Teleport to a random location in the wild", permission: "justplugin.tpr", aliases: "wild, rtp" },
      { command: "/back", usage: "/back", description: "Return to your previous location", permission: "justplugin.back", aliases: "return" },
      { command: "/spawn", usage: "/spawn [player]", description: "Teleport to the server spawn point", permission: "justplugin.spawn", aliases: "—" },
      { command: "/setspawn", usage: "/setspawn", description: "Set the server spawn point", permission: "justplugin.setspawn", aliases: "—" },
    ],
  },
  {
    id: "offline-player",
    title: "Offline Player Commands",
    commands: [
      { command: "/tpoff", usage: "/tpoff <player>", description: "Teleport to an offline player's last known location", permission: "justplugin.tpoff", aliases: "—" },
      { command: "/getposoff", usage: "/getposoff <player>", description: "Get the last known position of an offline player", permission: "justplugin.getposoff", aliases: "—" },
      { command: "/getdeathposoff", usage: "/getdeathposoff <player>", description: "Get the last death position of an offline player", permission: "justplugin.getdeathposoff", aliases: "—" },
      { command: "/invseeoff", usage: "/invseeoff <player>", description: "View the inventory of an offline player", permission: "justplugin.invseeoff", aliases: "—" },
      { command: "/echestseeoff", usage: "/echestseeoff <player>", description: "View the ender chest of an offline player", permission: "justplugin.echestseeoff", aliases: "—" },
    ],
  },
  {
    id: "warps",
    title: "Warps",
    commands: [
      { command: "/warp", usage: "/warp <name>", description: "Teleport to a warp location", permission: "justplugin.warp", aliases: "—" },
      { command: "/warps", usage: "/warps", description: "List all available warps", permission: "justplugin.warps", aliases: "warplist" },
      { command: "/setwarp", usage: "/setwarp <name>", description: "Create a new warp at your location", permission: "justplugin.setwarp", aliases: "createwarp" },
      { command: "/delwarp", usage: "/delwarp <name>", description: "Delete an existing warp", permission: "justplugin.delwarp", aliases: "removewarp, rmwarp" },
      { command: "/renamewarp", usage: "/renamewarp <old> <new>", description: "Rename an existing warp", permission: "justplugin.renamewarp", aliases: "—" },
    ],
  },
  {
    id: "homes",
    title: "Homes",
    commands: [
      { command: "/home", usage: "/home [name]", description: "Teleport to your home", permission: "justplugin.home", aliases: "h" },
      { command: "/sethome", usage: "/sethome [name]", description: "Set a home at your current location", permission: "justplugin.sethome", aliases: "sh, createhome" },
      { command: "/delhome", usage: "/delhome <name>", description: "Delete one of your homes", permission: "justplugin.delhome", aliases: "removehome, rmhome" },
    ],
  },
  {
    id: "economy",
    title: "Economy",
    commands: [
      { command: "/balance", usage: "/balance [player]", description: "Check your or another player's balance", permission: "justplugin.balance", aliases: "bal, money" },
      { command: "/pay", usage: "/pay <player> <amount>", description: "Send money to another player", permission: "justplugin.pay", aliases: "—" },
      { command: "/paytoggle", usage: "/paytoggle", description: "Toggle whether you can receive payments", permission: "justplugin.paytoggle", aliases: "—" },
      { command: "/paynote", usage: "/paynote <player> <amount>", description: "Create a physical pay note item", permission: "justplugin.paynote", aliases: "—" },
      { command: "/addcash", usage: "/addcash <player> <amount>", description: "Add money to a player's balance", permission: "justplugin.addcash", aliases: "givemoney, addmoney, addbal" },
      { command: "/baltop", usage: "/baltop [page]", description: "View the balance leaderboard", permission: "justplugin.baltop", aliases: "balancetop, moneytop, topbal" },
      { command: "/baltophide", usage: "/baltophide", description: "Hide yourself from the balance leaderboard", permission: "justplugin.baltophide", aliases: "hidebaltop, balancetophide" },
      { command: "/transactions", usage: "/transactions [player]", description: "View transaction history GUI with pagination", permission: "justplugin.transactions", aliases: "txhistory, transactionhistory" },
    ],
  },
  {
    id: "moderation",
    title: "Moderation",
    commands: [
      { command: "/ban", usage: "/ban <player> [reason]", description: "Permanently ban a player", permission: "justplugin.ban", aliases: "—" },
      { command: "/banip", usage: "/banip <player|ip> [reason]", description: "Ban a player by IP address", permission: "justplugin.banip", aliases: "ban-ip" },
      { command: "/tempban", usage: "/tempban <player> <duration> [reason]", description: "Temporarily ban a player", permission: "justplugin.tempban", aliases: "tban" },
      { command: "/tempbanip", usage: "/tempbanip <player|ip> <duration> [reason]", description: "Temporarily ban a player by IP", permission: "justplugin.tempbanip", aliases: "tbanip" },
      { command: "/unban", usage: "/unban <player>", description: "Unban a player", permission: "justplugin.unban", aliases: "pardon" },
      { command: "/unbanip", usage: "/unbanip <player|ip>", description: "Unban an IP address", permission: "justplugin.unbanip", aliases: "pardon-ip, unban-ip" },
      { command: "/vanish", usage: "/vanish [player]", description: "Toggle vanish mode (invisible to players)", permission: "justplugin.vanish", aliases: "v" },
      { command: "/supervanish", usage: "/supervanish [player]", description: "Toggle super vanish with spectator ghost mode", permission: "justplugin.supervanish", aliases: "sv" },
      { command: "/sudo", usage: "/sudo <player> <command|message>", description: "Force a player to run a command or send a message", permission: "justplugin.sudo", aliases: "—" },
      { command: "/invsee", usage: "/invsee <player>", description: "Open and view a player's inventory", permission: "justplugin.invsee", aliases: "openinv" },
      { command: "/echestsee", usage: "/echestsee <player>", description: "Open and view a player's ender chest", permission: "justplugin.echestsee", aliases: "openec" },
      { command: "/mute", usage: "/mute <player> [reason]", description: "Permanently mute a player", permission: "justplugin.mute", aliases: "—" },
      { command: "/tempmute", usage: "/tempmute <player> <duration> [reason]", description: "Temporarily mute a player", permission: "justplugin.tempmute", aliases: "tmute" },
      { command: "/unmute", usage: "/unmute <player>", description: "Unmute a player", permission: "justplugin.unmute", aliases: "—" },
      { command: "/warn", usage: "/warn <player> [reason]", description: "Issue a warning to a player or view warnings", permission: "justplugin.warn", aliases: "warning, warnings" },
      { command: "/kick", usage: "/kick <player> [reason]", description: "Kick a player from the server", permission: "justplugin.kick", aliases: "—" },
      { command: "/setlogswebhook", usage: "/setlogswebhook <url>", description: "Set the Discord webhook URL for logging", permission: "justplugin.setlogswebhook", aliases: "logwebhook, webhooklog" },
      { command: "/deathitems", usage: "/deathitems <player>", description: "View a player's items from their last death", permission: "justplugin.deathitems", aliases: "di, deathinv" },
      { command: "/oplist", usage: "/oplist", description: "List all server operators", permission: "justplugin.oplist", aliases: "ops, operators" },
      { command: "/banlist", usage: "/banlist", description: "View the list of banned players", permission: "justplugin.banlist", aliases: "bans" },
      { command: "/baniplist", usage: "/baniplist", description: "View the list of IP-banned players", permission: "justplugin.baniplist", aliases: "ipbans, ipbanlist" },
    ],
  },
  {
    id: "jail",
    title: "Jail",
    commands: [
      { command: "/jail", usage: "/jail <player> <jail> [duration]", description: "Send a player to jail", permission: "justplugin.jail", aliases: "—" },
      { command: "/unjail", usage: "/unjail <player>", description: "Release a player from jail", permission: "justplugin.unjail", aliases: "—" },
      { command: "/setjail", usage: "/setjail <name>", description: "Create a jail at your current location", permission: "justplugin.setjail", aliases: "—" },
      { command: "/deljail", usage: "/deljail <name>", description: "Delete a jail location", permission: "justplugin.deljail", aliases: "—" },
      { command: "/jails", usage: "/jails", description: "List all jail locations", permission: "justplugin.jails", aliases: "jaillist" },
      { command: "/jailinfo", usage: "/jailinfo <jail>", description: "View detailed info about a jail", permission: "justplugin.jailinfo", aliases: "—" },
    ],
  },
  {
    id: "player",
    title: "Player",
    commands: [
      { command: "/fly", usage: "/fly [player]", description: "Toggle flight mode", permission: "justplugin.fly", aliases: "—" },
      { command: "/gm", usage: "/gm <mode> [player]", description: "Change game mode", permission: "justplugin.gamemode", aliases: "gamemode" },
      { command: "/gmc", usage: "/gmc [player]", description: "Switch to creative mode", permission: "justplugin.gamemode.creative", aliases: "—" },
      { command: "/gms", usage: "/gms [player]", description: "Switch to survival mode", permission: "justplugin.gamemode.survival", aliases: "—" },
      { command: "/gma", usage: "/gma [player]", description: "Switch to adventure mode", permission: "justplugin.gamemode.adventure", aliases: "—" },
      { command: "/gmsp", usage: "/gmsp [player]", description: "Switch to spectator mode", permission: "justplugin.gamemode.spectator", aliases: "—" },
      { command: "/gmcheck", usage: "/gmcheck <player>", description: "Check a player's current game mode", permission: "justplugin.gmcheck", aliases: "checkgm, gamemodecheck" },
      { command: "/god", usage: "/god [player]", description: "Toggle god mode (invincibility)", permission: "justplugin.god", aliases: "godmode, tgm" },
      { command: "/speed", usage: "/speed <1-10> [player]", description: "Set fly or walk speed", permission: "justplugin.speed", aliases: "—" },
      { command: "/flyspeed", usage: "/flyspeed <1-10> [player]", description: "Set fly speed specifically", permission: "justplugin.flyspeed", aliases: "fspeed" },
      { command: "/walkspeed", usage: "/walkspeed <1-10> [player]", description: "Set walk speed specifically", permission: "justplugin.walkspeed", aliases: "wspeed" },
      { command: "/hat", usage: "/hat", description: "Wear the item in your hand as a hat", permission: "justplugin.hat", aliases: "—" },
      { command: "/exp", usage: "/exp <give|set|take> <player> <amount>", description: "Manage a player's experience points", permission: "justplugin.exp", aliases: "xp" },
      { command: "/skull", usage: "/skull [player]", description: "Get a player's head as an item", permission: "justplugin.skull", aliases: "head, playerhead" },
      { command: "/suicide", usage: "/suicide", description: "Kill yourself", permission: "justplugin.suicide", aliases: "—" },
      { command: "/kill", usage: "/kill <player>", description: "Kill another player", permission: "justplugin.kill", aliases: "—" },
      { command: "/heal", usage: "/heal [player]", description: "Restore full health", permission: "justplugin.heal", aliases: "—" },
      { command: "/feed", usage: "/feed [player]", description: "Restore full hunger", permission: "justplugin.feed", aliases: "—" },
      { command: "/getpos", usage: "/getpos [player]", description: "Get your or a player's current position", permission: "justplugin.getpos", aliases: "whereami, position, getcoords, coords" },
      { command: "/getdeathpos", usage: "/getdeathpos [player]", description: "Get your or a player's last death position", permission: "justplugin.getdeathpos", aliases: "getdeathcoords, deathpos, deathcoords" },
      { command: "/afk", usage: "/afk", description: "Toggle AFK status", permission: "justplugin.afk", aliases: "away" },
      { command: "/near", usage: "/near [radius]", description: "Show nearby players with distance and direction", permission: "justplugin.near", aliases: "nearby" },
      { command: "/repair", usage: "/repair [player]", description: "Repair the held item to max durability", permission: "justplugin.repair", aliases: "fix" },
      { command: "/enchant", usage: "/enchant <enchantment> [level]", description: "Apply an enchantment to the held item", permission: "justplugin.enchant", aliases: "—" },
    ],
  },
  {
    id: "chat",
    title: "Chat",
    commands: [
      { command: "/msg", usage: "/msg <player> <message>", description: "Send a private message to a player", permission: "justplugin.msg", aliases: "tell, whisper, w, m, pm, dm" },
      { command: "/r", usage: "/r <message>", description: "Reply to the last private message", permission: "justplugin.reply", aliases: "reply" },
      { command: "/ignore", usage: "/ignore <player>", description: "Toggle ignoring a player's messages", permission: "justplugin.ignore", aliases: "—" },
      { command: "/announce", usage: "/announce <message>", description: "Broadcast a server-wide announcement", permission: "justplugin.announce", aliases: "broadcast, bcast" },
      { command: "/sharecoords", usage: "/sharecoords", description: "Share your current coordinates in chat", permission: "justplugin.sharecoords", aliases: "sendcoords" },
      { command: "/sharedeathcoords", usage: "/sharedeathcoords", description: "Share your last death coordinates in chat", permission: "justplugin.sharedeathcoords", aliases: "senddeathcoords" },
      { command: "/chat", usage: "/chat <channel>", description: "Switch chat channels", permission: "justplugin.chat", aliases: "—" },
      { command: "/teammsg", usage: "/teammsg <message>", description: "Send a message to your team", permission: "justplugin.teammsg", aliases: "tmsg, tm" },
      { command: "/clearchat", usage: "/clearchat", description: "Clear the chat for all players", permission: "justplugin.clearchat", aliases: "cc, chatclear" },
      { command: "/mail", usage: "/mail <send|read|clear> [player] [message]", description: "Send and manage offline mail", permission: "justplugin.mail", aliases: "—" },
    ],
  },
  {
    id: "kits",
    title: "Kits",
    commands: [
      { command: "/kit", usage: "/kit [name]", description: "Open the kit GUI or claim a specific kit", permission: "justplugin.kit", aliases: "—" },
      { command: "/kitpreview", usage: "/kitpreview <name>", description: "Preview the contents of a kit", permission: "justplugin.kitpreview", aliases: "—" },
      { command: "/kitcreate", usage: "/kitcreate <name>", description: "Create a new kit from your inventory", permission: "justplugin.kitcreate", aliases: "—" },
      { command: "/kitedit", usage: "/kitedit <name>", description: "Edit an existing kit's contents", permission: "justplugin.kitedit", aliases: "—" },
      { command: "/kitrename", usage: "/kitrename <old> <new>", description: "Rename an existing kit", permission: "justplugin.kitrename", aliases: "—" },
      { command: "/kitdelete", usage: "/kitdelete <name>", description: "Delete a kit", permission: "justplugin.kitdelete", aliases: "—" },
      { command: "/kitpublish", usage: "/kitpublish <name>", description: "Publish a kit to make it available", permission: "justplugin.kitpublish", aliases: "—" },
      { command: "/kitdisable", usage: "/kitdisable <name>", description: "Disable a kit without deleting it", permission: "justplugin.kitdisable", aliases: "—" },
      { command: "/kitenable", usage: "/kitenable <name>", description: "Re-enable a disabled kit", permission: "justplugin.kitenable", aliases: "—" },
      { command: "/kitarchive", usage: "/kitarchive <name>", description: "Archive a kit", permission: "justplugin.kitarchive", aliases: "—" },
      { command: "/kitlist", usage: "/kitlist", description: "List all available kits", permission: "justplugin.kitlist", aliases: "—" },
    ],
  },
  {
    id: "personalization",
    title: "Personalization",
    commands: [
      { command: "/nick", usage: "/nick <name|off> [player]", description: "Set a custom display name", permission: "justplugin.nick", aliases: "nickname" },
      { command: "/tag", usage: "/tag [tag]", description: "Select a tag from the GUI or set one directly", permission: "justplugin.tag", aliases: "—" },
      { command: "/tagcreate", usage: "/tagcreate <id> <display>", description: "Create a new tag", permission: "justplugin.tagcreate", aliases: "—" },
      { command: "/tagdelete", usage: "/tagdelete <id>", description: "Delete an existing tag", permission: "justplugin.tagdelete", aliases: "—" },
      { command: "/taglist", usage: "/taglist", description: "List all available tags", permission: "justplugin.taglist", aliases: "—" },
    ],
  },
  {
    id: "virtual-inventories",
    title: "Virtual Inventories",
    commands: [
      { command: "/anvil", usage: "/anvil", description: "Open a virtual anvil", permission: "justplugin.anvil", aliases: "—" },
      { command: "/grindstone", usage: "/grindstone", description: "Open a virtual grindstone", permission: "justplugin.grindstone", aliases: "—" },
      { command: "/enderchest", usage: "/enderchest", description: "Open your ender chest anywhere", permission: "justplugin.enderchest", aliases: "echest, ec" },
      { command: "/craft", usage: "/craft", description: "Open a virtual crafting table", permission: "justplugin.craft", aliases: "workbench, wb" },
      { command: "/stonecutter", usage: "/stonecutter", description: "Open a virtual stonecutter", permission: "justplugin.stonecutter", aliases: "—" },
      { command: "/loom", usage: "/loom", description: "Open a virtual loom", permission: "justplugin.loom", aliases: "—" },
      { command: "/smithingtable", usage: "/smithingtable", description: "Open a virtual smithing table", permission: "justplugin.smithingtable", aliases: "smithtable" },
      { command: "/enchantingtable", usage: "/enchantingtable", description: "Open a virtual enchanting table", permission: "justplugin.enchantingtable", aliases: "enchtable" },
      { command: "/pv", usage: "/pv [number]", description: "Open a player vault (54-slot virtual storage)", permission: "justplugin.vault", aliases: "playervault, vault" },
    ],
  },
  {
    id: "info",
    title: "Info",
    commands: [
      { command: "/jpinfo", usage: "/jpinfo", description: "Display plugin version and info", permission: "justplugin.jpinfo", aliases: "about" },
      { command: "/jphelp", usage: "/jphelp", description: "Show the help menu", permission: "justplugin.jphelp", aliases: "—" },
      { command: "/playerinfo", usage: "/playerinfo <player>", description: "View detailed info about a player", permission: "justplugin.playerinfo", aliases: "whois, seen" },
      { command: "/plist", usage: "/plist", description: "View a list of online players", permission: "justplugin.plist", aliases: "who, online, players" },
      { command: "/playerlist", usage: "/playerlist", description: "View the full player list with details", permission: "justplugin.playerlist", aliases: "pls" },
      { command: "/playerlisthide", usage: "/playerlisthide [player]", description: "Hide a player from the player list", permission: "justplugin.playerlisthide", aliases: "plhide, hideplayer" },
      { command: "/motd", usage: "/motd", description: "View the message of the day", permission: "justplugin.motd", aliases: "—" },
      { command: "/resetmotd", usage: "/resetmotd", description: "Reset the MOTD to default", permission: "justplugin.resetmotd", aliases: "—" },
      { command: "/clock", usage: "/clock", description: "Display the real-world time", permission: "justplugin.clock", aliases: "realtime, rltime" },
      { command: "/date", usage: "/date", description: "Display the real-world date", permission: "justplugin.date", aliases: "realdate, rldate" },
    ],
  },
  {
    id: "items",
    title: "Items",
    commands: [
      { command: "/itemname", usage: "/itemname <name>", description: "Rename the item in your hand", permission: "justplugin.itemname", aliases: "rename, iname" },
      { command: "/shareitem", usage: "/shareitem", description: "Share the item in your hand in chat", permission: "justplugin.shareitem", aliases: "show, showitem" },
      { command: "/setspawner", usage: "/setspawner <mob>", description: "Change the mob type of a spawner", permission: "justplugin.setspawner", aliases: "changespawner" },
    ],
  },
  {
    id: "world",
    title: "World",
    commands: [
      { command: "/weather", usage: "/weather <clear|rain|thunder> [duration]", description: "Change the weather", permission: "justplugin.weather", aliases: "—" },
      { command: "/time", usage: "/time <set|add> <value>", description: "Change the world time", permission: "justplugin.time", aliases: "—" },
      { command: "/freezegame", usage: "/freezegame", description: "Freeze the game tick (stop time, mobs, etc.)", permission: "justplugin.freezegame", aliases: "tf" },
      { command: "/unfreezegame", usage: "/unfreezegame", description: "Unfreeze the game tick", permission: "justplugin.unfreezegame", aliases: "unft" },
      { command: "/clearentities", usage: "/clearentities [radius]", description: "Remove all non-player entities", permission: "justplugin.clearentities", aliases: "ce, entityclear, clearlag" },
      { command: "/friendlyfire", usage: "/friendlyfire", description: "Toggle PvP / friendly fire", permission: "justplugin.friendlyfire", aliases: "ff, pvp, pvptoggle" },
    ],
  },
  {
    id: "teams",
    title: "Teams",
    commands: [
      { command: "/team create", usage: "/team create <name>", description: "Create a new team", permission: "justplugin.team.create", aliases: "—" },
      { command: "/team disband", usage: "/team disband", description: "Disband your team", permission: "justplugin.team.disband", aliases: "—" },
      { command: "/team invite", usage: "/team invite <player>", description: "Invite a player to your team", permission: "justplugin.team.invite", aliases: "—" },
      { command: "/team join", usage: "/team join <team>", description: "Join a team you were invited to", permission: "justplugin.team.join", aliases: "—" },
      { command: "/team leave", usage: "/team leave", description: "Leave your current team", permission: "justplugin.team.leave", aliases: "—" },
      { command: "/team kick", usage: "/team kick <player>", description: "Kick a member from your team", permission: "justplugin.team.kick", aliases: "—" },
      { command: "/team info", usage: "/team info [team]", description: "View team information", permission: "justplugin.team.info", aliases: "—" },
      { command: "/team list", usage: "/team list", description: "List all teams", permission: "justplugin.team.list", aliases: "—" },
    ],
  },
  {
    id: "misc",
    title: "Misc",
    commands: [
      { command: "/trade", usage: "/trade <player>", description: "Send a trade request to a player", permission: "justplugin.trade", aliases: "—" },
      { command: "/discord", usage: "/discord", description: "Display the server's Discord link", permission: "justplugin.discord", aliases: "dc" },
      { command: "/applyedits", usage: "/applyedits", description: "Apply pending config edits from the web editor", permission: "justplugin.applyedits", aliases: "configapply, webapply" },
      { command: "/tab", usage: "/tab", description: "Manage tab list settings", permission: "justplugin.tab", aliases: "—" },
      { command: "/reloadscoreboard", usage: "/reloadscoreboard", description: "Reload the scoreboard configuration", permission: "justplugin.reloadscoreboard", aliases: "reloadsb" },
      { command: "/rank", usage: "/rank", description: "Open the rank GUI (requires LuckPerms)", permission: "justplugin.rank", aliases: "ranks" },
      { command: "/stats", usage: "/stats [player]", description: "View player statistics", permission: "justplugin.stats", aliases: "statistics" },
      { command: "/maintenance", usage: "/maintenance [on|off]", description: "Toggle server maintenance mode", permission: "justplugin.maintenance", aliases: "maint" },
      { command: "/skin", usage: "/skin <player|url|reset>", description: "Change your skin", permission: "justplugin.skin", aliases: "setskin" },
      { command: "/skinban", usage: "/skinban <player>", description: "Ban a player from changing their skin", permission: "justplugin.skinban", aliases: "—" },
      { command: "/skinunban", usage: "/skinunban <player>", description: "Unban a player from changing their skin", permission: "justplugin.skinunban", aliases: "—" },
      { command: "/jpbackup", usage: "/jpbackup", description: "Create a backup of all plugin data", permission: "justplugin.jpbackup", aliases: "backup" },
      { command: "/automessage", usage: "/automessage <list|reload>", description: "Manage automated broadcast messages", permission: "justplugin.automessage", aliases: "—" },
    ],
  },
];

/* ------------------------------------------------------------------ */
/*  Component                                                          */
/* ------------------------------------------------------------------ */

export default function CommandsPage() {
  const [search, setSearch] = useState("");

  const totalCommands = useMemo(
    () => categories.reduce((sum, cat) => sum + cat.commands.length, 0),
    [],
  );

  const filtered = useMemo(() => {
    if (!search.trim()) return categories;
    const q = search.toLowerCase();
    return categories
      .map((cat) => ({
        ...cat,
        commands: cat.commands.filter(
          (cmd) =>
            cmd.command.toLowerCase().includes(q) ||
            cmd.description.toLowerCase().includes(q) ||
            cmd.permission.toLowerCase().includes(q) ||
            cmd.aliases.toLowerCase().includes(q) ||
            cmd.usage.toLowerCase().includes(q),
        ),
      }))
      .filter((cat) => cat.commands.length > 0);
  }, [search]);

  const matchCount = filtered.reduce((s, c) => s + c.commands.length, 0);

  return (
    <div>
      <PageHeader
        title="Commands"
        description={`Complete reference for all ${totalCommands} commands available in JustPlugin.`}
        badge={`v${PLUGIN_VERSION}`}
      />

      {/* Search */}
      <div className="mb-8">
        <div className="relative">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--text-muted)]"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <input
            type="text"
            placeholder="Search commands, descriptions, permissions, or aliases..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-[var(--border)] bg-[var(--bg-card)] text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] focus:outline-none focus:border-[var(--accent-hover)] transition-colors"
          />
        </div>
        {search && (
          <p className="mt-2 text-xs text-[var(--text-muted)]">
            Showing {matchCount} command{matchCount !== 1 ? "s" : ""} matching &ldquo;{search}&rdquo;
          </p>
        )}
      </div>

      {/* Table of Contents */}
      {!search && (
        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-10">
          <h2 className="text-lg font-semibold mb-3">Table of Contents</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
            {categories.map((cat) => (
              <a
                key={cat.id}
                href={`#${cat.id}`}
                className="text-sm text-[var(--text-secondary)] hover:text-[var(--accent-hover)] transition-colors"
              >
                {cat.title}
                <span className="text-[var(--text-muted)] ml-1">({cat.commands.length})</span>
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Command Tables */}
      <div className="space-y-10">
        {filtered.map((cat) => (
          <section key={cat.id} id={cat.id}>
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              <a
                href={`#${cat.id}`}
                className="hover:text-[var(--accent-hover)] transition-colors"
              >
                {cat.title}
              </a>
              <span className="text-xs font-normal text-[var(--text-muted)] bg-[var(--bg-tertiary)] px-2 py-0.5 rounded-full">
                {cat.commands.length}
              </span>
            </h2>
            <div className="overflow-x-auto rounded-lg border border-[var(--border)]">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-[var(--bg-tertiary)]">
                    <th className="text-left px-4 py-2.5 font-semibold text-[var(--text-secondary)] whitespace-nowrap">
                      Command
                    </th>
                    <th className="text-left px-4 py-2.5 font-semibold text-[var(--text-secondary)] whitespace-nowrap">
                      Usage
                    </th>
                    <th className="text-left px-4 py-2.5 font-semibold text-[var(--text-secondary)]">
                      Description
                    </th>
                    <th className="text-left px-4 py-2.5 font-semibold text-[var(--text-secondary)] whitespace-nowrap">
                      Permission
                    </th>
                    <th className="text-left px-4 py-2.5 font-semibold text-[var(--text-secondary)] whitespace-nowrap">
                      Aliases
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {cat.commands.map((cmd, i) => (
                    <tr
                      key={cmd.command}
                      className={`border-t border-[var(--border)] hover:bg-[var(--bg-hover)] transition-colors ${
                        i % 2 === 0 ? "bg-[var(--bg-card)]" : "bg-transparent"
                      }`}
                    >
                      <td className="px-4 py-2.5 font-mono text-xs font-semibold text-[var(--accent-hover)] whitespace-nowrap">
                        {cmd.command}
                      </td>
                      <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-secondary)] whitespace-nowrap">
                        {cmd.usage}
                      </td>
                      <td className="px-4 py-2.5 text-[var(--text-secondary)]">
                        {cmd.description}
                      </td>
                      <td className="px-4 py-2.5 font-mono text-xs text-[var(--text-muted)] whitespace-nowrap">
                        {cmd.permission}
                      </td>
                      <td className="px-4 py-2.5 text-xs text-[var(--text-muted)] whitespace-nowrap">
                        {cmd.aliases}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        ))}
      </div>

      {/* No results */}
      {filtered.length === 0 && (
        <div className="text-center py-16">
          <p className="text-[var(--text-muted)] text-lg">No commands found matching &ldquo;{search}&rdquo;</p>
          <button
            onClick={() => setSearch("")}
            className="mt-3 text-sm text-[var(--accent-hover)] hover:underline"
          >
            Clear search
          </button>
        </div>
      )}

      {/* Back to top */}
      {!search && (
        <div className="mt-10 text-center">
          <a
            href="#"
            className="text-sm text-[var(--text-muted)] hover:text-[var(--accent-hover)] transition-colors"
          >
            Back to top
          </a>
        </div>
      )}
    </div>
  );
}
