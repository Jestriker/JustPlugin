"use client";

import { useState, useMemo } from "react";
import PageHeader from "@/components/PageHeader";

/* ---------- data types ---------- */

interface Perm {
  perm: string;
  desc: string;
  def: string;
  commands?: string;
}

interface PermSection {
  title: string;
  id: string;
  perms: Perm[];
}

/* ---------- permission data ---------- */

const sections: PermSection[] = [
  {
    title: "Teleportation",
    id: "teleportation",
    perms: [
      { perm: "justplugin.tpa", desc: "Send a teleport request to a player", def: "Player", commands: "/tpa" },
      { perm: "justplugin.tpahere", desc: "Request a player to teleport to you", def: "Player", commands: "/tpahere" },
      { perm: "justplugin.tppos", desc: "Teleport to specific coordinates", def: "Admin", commands: "/tppos" },
      { perm: "justplugin.wild", desc: "Teleport to a random location in the overworld", def: "Player", commands: "/wild" },
      { perm: "justplugin.wild.nether", desc: "Teleport to a random location in the nether", def: "Player", commands: "/wild" },
      { perm: "justplugin.wild.end", desc: "Teleport to a random location in the end", def: "Player", commands: "/wild" },
      { perm: "justplugin.back", desc: "Teleport to your last location", def: "Player", commands: "/back" },
      { perm: "justplugin.spawn", desc: "Teleport to the server spawn", def: "Player", commands: "/spawn" },
      { perm: "justplugin.setspawn", desc: "Set the server spawn location", def: "Admin", commands: "/setspawn" },
      { perm: "justplugin.teleport.bypass", desc: "Bypass teleport cooldowns and delays", def: "Admin", commands: "" },
    ],
  },
  {
    title: "Offline Player",
    id: "offline-player",
    perms: [
      { perm: "justplugin.tpoff", desc: "Teleport to an offline player's last location", def: "Admin", commands: "/tpoff" },
      { perm: "justplugin.getposoff", desc: "Get an offline player's last known position", def: "Admin", commands: "/getposoff" },
      { perm: "justplugin.getdeathposoff", desc: "Get an offline player's last death position", def: "Admin", commands: "/getdeathposoff" },
      { perm: "justplugin.invseeoff", desc: "View an offline player's inventory", def: "Admin", commands: "/invseeoff" },
      { perm: "justplugin.echestseeoff", desc: "View an offline player's ender chest", def: "Admin", commands: "/echestseeoff" },
    ],
  },
  {
    title: "Warp",
    id: "warp",
    perms: [
      { perm: "justplugin.warp", desc: "Teleport to a warp location", def: "Player", commands: "/warp" },
      { perm: "justplugin.setwarp", desc: "Create a new warp location", def: "Admin", commands: "/setwarp" },
      { perm: "justplugin.delwarp", desc: "Delete a warp location", def: "Admin", commands: "/delwarp" },
      { perm: "justplugin.renamewarp", desc: "Rename a warp location", def: "Admin", commands: "/renamewarp" },
    ],
  },
  {
    title: "Home",
    id: "home",
    perms: [
      { perm: "justplugin.home", desc: "Teleport to your home", def: "Player", commands: "/home" },
      { perm: "justplugin.sethome", desc: "Set your home location", def: "Player", commands: "/sethome" },
      { perm: "justplugin.delhome", desc: "Delete a home location", def: "Player", commands: "/delhome" },
    ],
  },
  {
    title: "Economy",
    id: "economy",
    perms: [
      { perm: "justplugin.balance", desc: "Check your own balance", def: "Player", commands: "/balance" },
      { perm: "justplugin.balance.others", desc: "Check another player's balance", def: "Admin", commands: "/balance <player>" },
      { perm: "justplugin.pay", desc: "Send money to another player", def: "Player", commands: "/pay" },
      { perm: "justplugin.paytoggle", desc: "Toggle receiving payments", def: "Player", commands: "/paytoggle" },
      { perm: "justplugin.paynote", desc: "Send a note with a payment", def: "Player", commands: "/pay <player> <amount> <note>" },
      { perm: "justplugin.addcash", desc: "Add cash to your own balance", def: "Admin", commands: "/addcash" },
      { perm: "justplugin.addcash.others", desc: "Add cash to another player's balance", def: "Admin", commands: "/addcash <player>" },
      { perm: "justplugin.baltophide", desc: "Hide yourself from the baltop leaderboard", def: "Player", commands: "/baltophide" },
      { perm: "justplugin.baltophide.others", desc: "Hide another player from the baltop leaderboard", def: "Admin", commands: "/baltophide <player>" },
      { perm: "justplugin.baltophide.notify", desc: "Get notified when someone hides from baltop", def: "Admin", commands: "" },
      { perm: "justplugin.baltop.viewhidden", desc: "View hidden players on the baltop leaderboard", def: "Admin", commands: "/baltop" },
      { perm: "justplugin.transactions", desc: "View your own transaction history", def: "Player", commands: "/transactions" },
      { perm: "justplugin.transactions.others", desc: "View another player's transaction history", def: "Admin", commands: "/transactions <player>" },
    ],
  },
  {
    title: "Moderation",
    id: "moderation",
    perms: [
      { perm: "justplugin.ban", desc: "Permanently ban a player", def: "Admin", commands: "/ban" },
      { perm: "justplugin.banip", desc: "Ban a player by IP address", def: "Admin", commands: "/banip" },
      { perm: "justplugin.tempban", desc: "Temporarily ban a player", def: "Admin", commands: "/tempban" },
      { perm: "justplugin.tempbanip", desc: "Temporarily ban a player by IP", def: "Admin", commands: "/tempbanip" },
      { perm: "justplugin.unban", desc: "Unban a player", def: "Admin", commands: "/unban" },
      { perm: "justplugin.unbanip", desc: "Unban an IP address", def: "Admin", commands: "/unbanip" },
      { perm: "justplugin.vanish", desc: "Toggle vanish for yourself", def: "Admin", commands: "/vanish" },
      { perm: "justplugin.vanish.others", desc: "Toggle vanish for another player", def: "Admin", commands: "/vanish <player>" },
      { perm: "justplugin.vanish.see", desc: "See vanished players", def: "Admin", commands: "" },
      { perm: "justplugin.supervanish", desc: "Toggle super vanish (spectator ghost mode) for yourself", def: "Admin", commands: "/supervanish" },
      { perm: "justplugin.supervanish.others", desc: "Toggle super vanish for another player", def: "Admin", commands: "/supervanish <player>" },
      { perm: "justplugin.sudo", desc: "Force a player to run a command or send a message", def: "Admin", commands: "/sudo" },
      { perm: "justplugin.invsee", desc: "View and edit a player's inventory", def: "Admin", commands: "/invsee" },
      { perm: "justplugin.echestsee", desc: "View and edit a player's ender chest", def: "Admin", commands: "/echestsee" },
      { perm: "justplugin.mute", desc: "Permanently mute a player", def: "Admin", commands: "/mute" },
      { perm: "justplugin.tempmute", desc: "Temporarily mute a player", def: "Admin", commands: "/tempmute" },
      { perm: "justplugin.unmute", desc: "Unmute a player", def: "Admin", commands: "/unmute" },
      { perm: "justplugin.warn", desc: "Warn a player", def: "Admin", commands: "/warn" },
      { perm: "justplugin.warn.notify", desc: "Get notified when a player is warned", def: "Admin", commands: "" },
      { perm: "justplugin.kick", desc: "Kick a player from the server", def: "Admin", commands: "/kick" },
      { perm: "justplugin.setlogswebhook", desc: "Set the Discord webhook URL for logs", def: "Admin", commands: "/setlogswebhook" },
      { perm: "justplugin.applyedits", desc: "Apply web editor configuration edits", def: "Admin", commands: "/applyedits" },
    ],
  },
  {
    title: "Jail",
    id: "jail",
    perms: [
      { perm: "justplugin.jail", desc: "Jail a player", def: "Admin", commands: "/jail" },
      { perm: "justplugin.unjail", desc: "Release a player from jail", def: "Admin", commands: "/unjail" },
      { perm: "justplugin.setjail", desc: "Create a jail location", def: "Admin", commands: "/setjail" },
      { perm: "justplugin.deljail", desc: "Delete a jail location", def: "Admin", commands: "/deljail" },
      { perm: "justplugin.jails", desc: "List all jail locations", def: "Admin", commands: "/jails" },
      { perm: "justplugin.jailinfo", desc: "View info about a jailed player", def: "Admin", commands: "/jailinfo" },
    ],
  },
  {
    title: "Player",
    id: "player",
    perms: [
      { perm: "justplugin.fly", desc: "Toggle flight for yourself", def: "Player", commands: "/fly" },
      { perm: "justplugin.fly.others", desc: "Toggle flight for another player", def: "Admin", commands: "/fly <player>" },
      { perm: "justplugin.gamemode", desc: "Change your own game mode", def: "Admin", commands: "/gamemode" },
      { perm: "justplugin.gamemode.others", desc: "Change another player's game mode", def: "Admin", commands: "/gamemode <mode> <player>" },
      { perm: "justplugin.gmcheck", desc: "Check a player's current game mode", def: "Admin", commands: "/gmcheck" },
      { perm: "justplugin.god", desc: "Toggle god mode for yourself", def: "Admin", commands: "/god" },
      { perm: "justplugin.god.others", desc: "Toggle god mode for another player", def: "Admin", commands: "/god <player>" },
      { perm: "justplugin.speed", desc: "Set your own walk/fly speed", def: "Player", commands: "/speed" },
      { perm: "justplugin.speed.others", desc: "Set another player's walk/fly speed", def: "Admin", commands: "/speed <value> <player>" },
      { perm: "justplugin.heal", desc: "Heal yourself", def: "Player", commands: "/heal" },
      { perm: "justplugin.heal.others", desc: "Heal another player", def: "Admin", commands: "/heal <player>" },
      { perm: "justplugin.feed", desc: "Feed yourself", def: "Player", commands: "/feed" },
      { perm: "justplugin.feed.others", desc: "Feed another player", def: "Admin", commands: "/feed <player>" },
      { perm: "justplugin.kill", desc: "Kill yourself", def: "Player", commands: "/kill" },
      { perm: "justplugin.kill.others", desc: "Kill another player", def: "Admin", commands: "/kill <player>" },
      { perm: "justplugin.hat", desc: "Wear an item on your head", def: "Player", commands: "/hat" },
      { perm: "justplugin.exp", desc: "Manage your own experience", def: "Admin", commands: "/exp" },
      { perm: "justplugin.exp.others", desc: "Manage another player's experience", def: "Admin", commands: "/exp <player>" },
      { perm: "justplugin.skull", desc: "Get a player head item", def: "Player", commands: "/skull" },
      { perm: "justplugin.suicide", desc: "Kill yourself", def: "Player", commands: "/suicide" },
      { perm: "justplugin.getpos.others", desc: "Get another player's position", def: "Admin", commands: "/getpos <player>" },
      { perm: "justplugin.getdeathpos", desc: "Get your own last death position", def: "Player", commands: "/getdeathpos" },
      { perm: "justplugin.getdeathpos.others", desc: "Get another player's last death position", def: "Admin", commands: "/getdeathpos <player>" },
      { perm: "justplugin.afk", desc: "Toggle AFK status", def: "Player", commands: "/afk" },
      { perm: "justplugin.afk.kickbypass", desc: "Bypass AFK auto-kick", def: "Admin", commands: "" },
      { perm: "justplugin.near", desc: "Show nearby players with distance and direction", def: "Admin", commands: "/near" },
      { perm: "justplugin.repair", desc: "Repair your own held item", def: "Admin", commands: "/repair" },
      { perm: "justplugin.repair.others", desc: "Repair another player's held item", def: "Admin", commands: "/repair <player>" },
      { perm: "justplugin.enchant", desc: "Enchant your held item", def: "Admin", commands: "/enchant" },
      { perm: "justplugin.enchant.bypass", desc: "Bypass enchantment restrictions (any enchant on any item at any level)", def: "Admin", commands: "/enchant" },
    ],
  },
  {
    title: "Chat",
    id: "chat",
    perms: [
      { perm: "justplugin.msg", desc: "Send private messages", def: "Player", commands: "/msg, /r" },
      { perm: "justplugin.ignore", desc: "Ignore a player's messages", def: "Player", commands: "/ignore" },
      { perm: "justplugin.announce", desc: "Send a server-wide announcement", def: "Admin", commands: "/announce" },
      { perm: "justplugin.sharecoords", desc: "Share your current coordinates in chat", def: "Player", commands: "/sharecoords" },
      { perm: "justplugin.sharedeathcoords", desc: "Share your last death coordinates in chat", def: "Player", commands: "/sharedeathcoords" },
      { perm: "justplugin.chat", desc: "Access chat management commands", def: "Admin", commands: "/chat" },
      { perm: "justplugin.mail", desc: "Read your mail", def: "Player", commands: "/mail" },
      { perm: "justplugin.mail.send", desc: "Send mail to other players", def: "Player", commands: "/mail send" },
    ],
  },
  {
    title: "Kits",
    id: "kits",
    perms: [
      { perm: "justplugin.kit", desc: "Open the kit selection GUI", def: "Player", commands: "/kit" },
      { perm: "justplugin.kit.preview", desc: "Preview a kit's contents", def: "Player", commands: "/kit preview" },
      { perm: "justplugin.kits.<kitName>", desc: "Permission to claim a specific kit", def: "Varies", commands: "/kit <name>" },
      { perm: "justplugin.kit.create", desc: "Create a new kit", def: "Admin", commands: "/kit create" },
      { perm: "justplugin.kit.edit", desc: "Edit an existing kit", def: "Admin", commands: "/kit edit" },
      { perm: "justplugin.kit.rename", desc: "Rename an existing kit", def: "Admin", commands: "/kit rename" },
      { perm: "justplugin.kit.delete", desc: "Delete a kit (moves to archive)", def: "Admin", commands: "/kit delete" },
      { perm: "justplugin.kit.delete.permanent", desc: "Permanently delete a kit", def: "Admin", commands: "/kit delete --permanent" },
      { perm: "justplugin.kit.publish", desc: "Publish a draft kit", def: "Admin", commands: "/kit publish" },
      { perm: "justplugin.kit.disable", desc: "Disable or enable a kit", def: "Admin", commands: "/kit disable" },
      { perm: "justplugin.kit.archive", desc: "Archive a kit", def: "Admin", commands: "/kit archive" },
      { perm: "justplugin.kit.archive.restore", desc: "Restore a kit from the archive", def: "Admin", commands: "/kit archive restore" },
      { perm: "justplugin.kit.archive.delete", desc: "Delete a kit from the archive permanently", def: "Admin", commands: "/kit archive delete" },
      { perm: "justplugin.kit.list", desc: "List all kits including unpublished", def: "Admin", commands: "/kit list" },
      { perm: "justplugin.kit.cooldownbypass", desc: "Bypass kit cooldowns", def: "Never (explicit only)", commands: "" },
    ],
  },
  {
    title: "Personalization",
    id: "personalization",
    perms: [
      { perm: "justplugin.nick", desc: "Set your nickname", def: "Player", commands: "/nick" },
      { perm: "justplugin.nick.color", desc: "Use color codes in nicknames", def: "Player", commands: "/nick" },
      { perm: "justplugin.nick.format", desc: "Use formatting codes in nicknames (bold, italic, etc.)", def: "Player", commands: "/nick" },
      { perm: "justplugin.nick.rainbow", desc: "Use rainbow gradient in nicknames", def: "Player", commands: "/nick" },
      { perm: "justplugin.tag", desc: "Select a tag from the GUI", def: "Player", commands: "/tag" },
      { perm: "justplugin.tag.create", desc: "Create a new tag", def: "Admin", commands: "/tag create" },
      { perm: "justplugin.tag.delete", desc: "Delete a tag", def: "Admin", commands: "/tag delete" },
      { perm: "justplugin.tag.list", desc: "List all available tags", def: "Player", commands: "/tag list" },
    ],
  },
  {
    title: "Virtual Inventories",
    id: "virtual-inventories",
    perms: [
      { perm: "justplugin.anvil", desc: "Open a virtual anvil", def: "Player", commands: "/anvil" },
      { perm: "justplugin.grindstone", desc: "Open a virtual grindstone", def: "Player", commands: "/grindstone" },
      { perm: "justplugin.enderchest", desc: "Open your ender chest anywhere", def: "Player", commands: "/enderchest" },
      { perm: "justplugin.craft", desc: "Open a virtual crafting table", def: "Player", commands: "/craft" },
      { perm: "justplugin.stonecutter", desc: "Open a virtual stonecutter", def: "Player", commands: "/stonecutter" },
      { perm: "justplugin.loom", desc: "Open a virtual loom", def: "Player", commands: "/loom" },
      { perm: "justplugin.smithingtable", desc: "Open a virtual smithing table", def: "Player", commands: "/smithingtable" },
      { perm: "justplugin.enchantingtable", desc: "Open a virtual enchanting table", def: "Player", commands: "/enchantingtable" },
      { perm: "justplugin.vault", desc: "Open your own player vaults", def: "Player", commands: "/pv" },
      { perm: "justplugin.vault.others", desc: "Open another player's vaults", def: "Admin", commands: "/pv <player> <number>" },
      { perm: "justplugin.vaults.<number>", desc: "Bypass server max vault limit up to <number>", def: "Varies", commands: "/pv" },
    ],
  },
  {
    title: "Info",
    id: "info",
    perms: [
      { perm: "justplugin.playerinfo", desc: "View detailed information about a player", def: "Admin", commands: "/playerinfo" },
      { perm: "justplugin.playerinfo.ip", desc: "View a player's IP address in playerinfo", def: "Admin", commands: "/playerinfo" },
      { perm: "justplugin.playerlist", desc: "View the player list", def: "Player", commands: "/playerlist" },
      { perm: "justplugin.playerlist.hide", desc: "Hide yourself from the player list", def: "Admin", commands: "/playerlist hide" },
      { perm: "justplugin.playerlist.hide.others", desc: "Hide another player from the player list", def: "Admin", commands: "/playerlist hide <player>" },
      { perm: "justplugin.playerlist.hide.notify", desc: "Get notified when someone hides from the player list", def: "Admin", commands: "" },
      { perm: "justplugin.staff", desc: "View the online staff list", def: "Player", commands: "/staff" },
      { perm: "justplugin.motd.set", desc: "Set the server MOTD", def: "Admin", commands: "/motd set" },
      { perm: "justplugin.info", desc: "View plugin info", def: "Player", commands: "/jpinfo, /about" },
      { perm: "justplugin.help", desc: "View plugin help", def: "Player", commands: "/jphelp, /help, /?" },
      { perm: "justplugin.list", desc: "List online players", def: "Player", commands: "/plist, /who, /online" },
      { perm: "justplugin.clock", desc: "Show current real-world time", def: "Player", commands: "/clock, /realtime" },
      { perm: "justplugin.date", desc: "Show current real-world date and time", def: "Player", commands: "/date, /realdate" },
    ],
  },
  {
    title: "Items",
    id: "items",
    perms: [
      { perm: "justplugin.itemname", desc: "Rename the item in your hand", def: "Player", commands: "/itemname" },
      { perm: "justplugin.shareitem", desc: "Share the item in your hand in chat", def: "Player", commands: "/shareitem" },
      { perm: "justplugin.setspawner", desc: "Change a spawner's entity type", def: "Admin", commands: "/setspawner" },
    ],
  },
  {
    title: "World",
    id: "world",
    perms: [
      { perm: "justplugin.weather", desc: "Change the server weather", def: "Admin", commands: "/weather" },
      { perm: "justplugin.time", desc: "Change the server time", def: "Admin", commands: "/time" },
    ],
  },
  {
    title: "Teams",
    id: "teams",
    perms: [
      { perm: "justplugin.team", desc: "Access team commands (create, invite, chat, etc.)", def: "Player", commands: "/team" },
      { perm: "justplugin.team.list", desc: "List all teams on the server", def: "Admin", commands: "/team list" },
      { perm: "justplugin.teamhome.cooldownbypass", desc: "Bypass team home pre-teleport countdown", def: "Admin", commands: "/team home" },
      { perm: "justplugin.teamhome.delaybypass", desc: "Bypass team home delay between uses", def: "Admin", commands: "/team home" },
      { perm: "justplugin.teamhome.unsafetp", desc: "Bypass team home safe teleport protection", def: "Admin", commands: "/team home" },
    ],
  },
  {
    title: "Misc",
    id: "misc",
    perms: [
      { perm: "justplugin.admin", desc: "Receive staff notifications on join (dependency warnings, update checks)", def: "Admin", commands: "" },
      { perm: "justplugin.announce.jail", desc: "Receive jail punishment announcements", def: "Admin", commands: "" },
      { perm: "justplugin.trade", desc: "Send a trade request to a player", def: "Player", commands: "/trade" },
      { perm: "justplugin.discord.set", desc: "Set the Discord invite link", def: "Admin", commands: "/discord set" },
      { perm: "justplugin.applyedits", desc: "Apply web editor configuration edits", def: "Admin", commands: "/applyedits" },
      { perm: "justplugin.tab", desc: "Access tab list management", def: "Admin", commands: "/tab" },
      { perm: "justplugin.scoreboard.reload", desc: "Reload the scoreboard configuration", def: "Admin", commands: "/scoreboard reload" },
      { perm: "justplugin.stats", desc: "View your own statistics", def: "Player", commands: "/stats" },
      { perm: "justplugin.stats.others", desc: "View another player's statistics", def: "Admin", commands: "/stats <player>" },
      { perm: "justplugin.maintenance", desc: "Toggle maintenance mode", def: "Admin", commands: "/maintenance" },
      { perm: "justplugin.maintenance.bypass", desc: "Join the server during maintenance", def: "Admin", commands: "" },
      { perm: "justplugin.skin", desc: "Change your own skin", def: "Player", commands: "/skin" },
      { perm: "justplugin.skin.others", desc: "Change another player's skin", def: "Admin", commands: "/skin <player>" },
      { perm: "justplugin.skin.bypassban", desc: "Bypass skin bans", def: "Admin", commands: "" },
      { perm: "justplugin.skinban", desc: "Ban a skin from being used", def: "Admin", commands: "/skinban" },
      { perm: "justplugin.skinunban", desc: "Unban a skin", def: "Admin", commands: "/skinunban" },
      { perm: "justplugin.backup", desc: "Access backup management", def: "Admin", commands: "/backup" },
      { perm: "justplugin.backup.export", desc: "Export a backup", def: "Admin", commands: "/backup export" },
      { perm: "justplugin.backup.import", desc: "Import a backup", def: "Admin", commands: "/backup import" },
      { perm: "justplugin.backup.list", desc: "List all backups", def: "Admin", commands: "/backup list" },
      { perm: "justplugin.backup.delete", desc: "Delete a backup", def: "Admin", commands: "/backup delete" },
      { perm: "justplugin.automessage", desc: "Access automated message commands", def: "Admin", commands: "/automessage" },
      { perm: "justplugin.automessage.reload", desc: "Reload automated messages config", def: "Admin", commands: "/automessage reload" },
      { perm: "justplugin.automessage.list", desc: "List all automated messages", def: "Admin", commands: "/automessage list" },
      { perm: "justplugin.automessage.toggle", desc: "Toggle an automated message on/off", def: "Admin", commands: "/automessage toggle" },
      { perm: "justplugin.automessage.send", desc: "Manually send an automated message", def: "Admin", commands: "/automessage send" },
    ],
  },
  {
    title: "Protection",
    id: "protection",
    perms: [
      { perm: "justplugin.spawnprotection.bypass", desc: "Bypass spawn protection radius", def: "Admin", commands: "" },
      { perm: "justplugin.seedprotection.bypass", desc: "Bypass /seed protection (view seed)", def: "Admin", commands: "/seed" },
      { perm: "justplugin.seedprotection.notify", desc: "Get notified when someone tries to use /seed", def: "Admin", commands: "" },
    ],
  },
  {
    title: "Safe Teleport Bypass",
    id: "safe-teleport-bypass",
    perms: [
      { perm: "justplugin.tpa.unsafetp", desc: "Bypass safe teleport checks for TPA", def: "Never (explicit only)", commands: "" },
      { perm: "justplugin.tpahere.unsafetp", desc: "Bypass safe teleport checks for TPA Here", def: "Never (explicit only)", commands: "" },
      { perm: "justplugin.warp.unsafetp", desc: "Bypass safe teleport checks for warps", def: "Never (explicit only)", commands: "" },
      { perm: "justplugin.spawn.unsafetp", desc: "Bypass safe teleport checks for spawn", def: "Never (explicit only)", commands: "" },
      { perm: "justplugin.home.unsafetp", desc: "Bypass safe teleport checks for homes", def: "Never (explicit only)", commands: "" },
      { perm: "justplugin.back.unsafetp", desc: "Bypass safe teleport checks for /back", def: "Never (explicit only)", commands: "" },
    ],
  },
  {
    title: "Log",
    id: "log",
    perms: [
      { perm: "justplugin.log.moderation", desc: "View moderation logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.economy", desc: "View economy logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.teleport", desc: "View teleport logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.vanish", desc: "View vanish logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.gamemode", desc: "View gamemode change logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.player", desc: "View player activity logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.admin", desc: "View admin action logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.item", desc: "View item-related logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.warn", desc: "View warning logs", def: "Admin", commands: "" },
      { perm: "justplugin.log.mute", desc: "View mute logs", def: "Admin", commands: "" },
    ],
  },
];

/* ---------- player-level permissions for the hierarchy display ---------- */

const playerPerms = [
  "justplugin.tpa",
  "justplugin.tpahere",
  "justplugin.wild",
  "justplugin.wild.nether",
  "justplugin.wild.end",
  "justplugin.back",
  "justplugin.spawn",
  "justplugin.warp",
  "justplugin.home",
  "justplugin.sethome",
  "justplugin.delhome",
  "justplugin.balance",
  "justplugin.pay",
  "justplugin.paytoggle",
  "justplugin.paynote",
  "justplugin.baltophide",
  "justplugin.fly",
  "justplugin.speed",
  "justplugin.heal",
  "justplugin.feed",
  "justplugin.kill",
  "justplugin.hat",
  "justplugin.skull",
  "justplugin.suicide",
  "justplugin.getdeathpos",
  "justplugin.afk",
  "justplugin.msg",
  "justplugin.ignore",
  "justplugin.sharecoords",
  "justplugin.sharedeathcoords",
  "justplugin.mail",
  "justplugin.mail.send",
  "justplugin.kit",
  "justplugin.kit.preview",
  "justplugin.nick",
  "justplugin.nick.color",
  "justplugin.nick.format",
  "justplugin.nick.rainbow",
  "justplugin.tag",
  "justplugin.tag.list",
  "justplugin.anvil",
  "justplugin.grindstone",
  "justplugin.enderchest",
  "justplugin.craft",
  "justplugin.stonecutter",
  "justplugin.loom",
  "justplugin.smithingtable",
  "justplugin.enchantingtable",
  "justplugin.playerlist",
  "justplugin.staff",
  "justplugin.itemname",
  "justplugin.shareitem",
  "justplugin.team",
  "justplugin.trade",
  "justplugin.stats",
  "justplugin.skin",
  "justplugin.vault",
  "justplugin.transactions",
];

/* ---------- component ---------- */

function DefBadge({ value }: { value: string }) {
  const colors: Record<string, string> = {
    Player: "bg-green-500/15 text-green-400 border-green-500/25",
    Admin: "bg-orange-500/15 text-orange-400 border-orange-500/25",
    Varies: "bg-blue-500/15 text-blue-400 border-blue-500/25",
    "Never (explicit only)": "bg-red-500/15 text-red-400 border-red-500/25",
  };
  return (
    <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium border ${colors[value] ?? "bg-gray-500/15 text-gray-400 border-gray-500/25"}`}>
      {value}
    </span>
  );
}

export default function PermissionsPage() {
  const [search, setSearch] = useState("");

  const allPerms = useMemo(() => sections.flatMap((s) => s.perms), []);
  const totalCount = allPerms.length;

  const filtered = useMemo(() => {
    if (!search.trim()) return sections;
    const q = search.toLowerCase();
    return sections
      .map((s) => ({
        ...s,
        perms: s.perms.filter(
          (p) =>
            p.perm.toLowerCase().includes(q) ||
            p.desc.toLowerCase().includes(q) ||
            (p.commands ?? "").toLowerCase().includes(q)
        ),
      }))
      .filter((s) => s.perms.length > 0);
  }, [search]);

  const matchCount = useMemo(
    () => filtered.reduce((sum, s) => sum + s.perms.length, 0),
    [filtered]
  );

  return (
    <div>
      <PageHeader
        title="Permissions"
        description="Complete permission reference for JustPlugin. All permissions must be explicitly granted through a permissions plugin."
        badge="150+ Permissions"
      />

      {/* Important notice */}
      <div className="bg-red-500/10 border border-red-500/25 rounded-xl p-5 mb-8">
        <h3 className="text-red-400 font-semibold text-sm mb-2">Important: OPs Do NOT Have Permissions By Default</h3>
        <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
          JustPlugin does <strong className="text-[var(--text-primary)]">not</strong> grant any permissions to OPs automatically.
          All permissions must be assigned through a permissions plugin such as{" "}
          <a href="https://luckperms.net" target="_blank" rel="noopener noreferrer" className="text-[var(--accent-hover)] hover:underline">
            LuckPerms
          </a>
          . Grant <code className="px-1.5 py-0.5 rounded bg-[var(--bg-code)] text-xs font-mono">justplugin.player</code> to
          your <strong className="text-[var(--text-primary)]">default group</strong> for basic player permissions, and{" "}
          <code className="px-1.5 py-0.5 rounded bg-[var(--bg-code)] text-xs font-mono">justplugin.*</code> to your{" "}
          <strong className="text-[var(--text-primary)]">admin group</strong> for full access.
        </p>
      </div>

      {/* Search */}
      <div className="mb-8">
        <div className="relative">
          <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--text-muted)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            placeholder="Search permissions, descriptions, or commands..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-lg bg-[var(--bg-card)] border border-[var(--border)] text-sm text-[var(--text-primary)] placeholder-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors"
          />
        </div>
        <p className="text-xs text-[var(--text-muted)] mt-2">
          {search.trim()
            ? `Showing ${matchCount} of ${totalCount} permissions`
            : `${totalCount} permissions total`}
        </p>
      </div>

      {/* Permission Hierarchy */}
      {!search.trim() && (
        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6 mb-8">
          <h2 className="text-xl font-bold mb-4">Permission Hierarchy</h2>
          <div className="space-y-4">
            {/* Wildcard */}
            <div>
              <div className="flex items-center gap-2 mb-1">
                <code className="text-sm font-mono text-orange-400">justplugin.*</code>
                <DefBadge value="Admin" />
              </div>
              <p className="text-sm text-[var(--text-secondary)] ml-4">
                Grants all permissions. Must be explicitly assigned &mdash; <strong className="text-[var(--text-primary)]">NOT</strong> default for OPs.
              </p>
            </div>

            {/* Player bundle */}
            <div>
              <div className="flex items-center gap-2 mb-1">
                <code className="text-sm font-mono text-green-400">justplugin.player</code>
                <DefBadge value="Player" />
              </div>
              <p className="text-sm text-[var(--text-secondary)] ml-4 mb-3">
                Basic player permissions bundle. Must be explicitly granted to your default group.
              </p>
              <div className="ml-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-1">
                {playerPerms.map((p) => (
                  <code key={p} className="text-xs font-mono text-[var(--text-muted)] before:content-['├─_'] before:text-[var(--border)]">
                    {p}
                  </code>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Exclusion warning */}
      {!search.trim() && (
        <div className="bg-yellow-500/10 border border-yellow-500/25 rounded-xl p-5 mb-8">
          <h3 className="text-yellow-400 font-semibold text-sm mb-2">Excluded from justplugin.* Wildcard</h3>
          <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
            The following permissions are <strong className="text-[var(--text-primary)]">never</strong> included in{" "}
            <code className="px-1.5 py-0.5 rounded bg-[var(--bg-code)] text-xs font-mono">justplugin.*</code> and must always be assigned explicitly:
          </p>
          <ul className="mt-2 space-y-1 text-sm">
            <li className="flex items-center gap-2">
              <code className="font-mono text-xs text-red-400">justplugin.kit.cooldownbypass</code>
              <span className="text-[var(--text-muted)]">&mdash; Bypass kit cooldowns</span>
            </li>
            <li className="flex items-center gap-2">
              <code className="font-mono text-xs text-red-400">justplugin.*.unsafetp</code>
              <span className="text-[var(--text-muted)]">&mdash; Bypass safe teleport checks (tpa, tpahere, warp, spawn, home, back)</span>
            </li>
          </ul>
        </div>
      )}

      {/* Quick nav */}
      {!search.trim() && (
        <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-5 mb-8">
          <h3 className="text-sm font-semibold mb-3">Jump to Category</h3>
          <div className="flex flex-wrap gap-2">
            {sections.map((s) => (
              <a
                key={s.id}
                href={`#${s.id}`}
                className="px-2.5 py-1 rounded-md bg-[var(--bg-main)] border border-[var(--border)] text-xs text-[var(--text-secondary)] hover:border-[var(--accent)] hover:text-[var(--accent-hover)] transition-colors"
              >
                {s.title}
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Permission tables */}
      <div className="space-y-8">
        {filtered.map((section) => (
          <div key={section.id} id={section.id} className="scroll-mt-20">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              {section.title}
              <span className="text-xs font-normal text-[var(--text-muted)]">({section.perms.length})</span>
            </h2>
            <div className="bg-[var(--bg-card)] rounded-xl border border-[var(--border)] overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-[var(--border)]">
                      <th className="text-left text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider px-4 py-3">Permission</th>
                      <th className="text-left text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider px-4 py-3">Description</th>
                      <th className="text-left text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider px-4 py-3">Default</th>
                      <th className="text-left text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider px-4 py-3">Commands</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[var(--border)]">
                    {section.perms.map((p) => (
                      <tr key={p.perm} className="hover:bg-[var(--bg-main)] transition-colors">
                        <td className="px-4 py-2.5">
                          <code className="text-xs font-mono text-[var(--accent-hover)] break-all">{p.perm}</code>
                        </td>
                        <td className="px-4 py-2.5 text-sm text-[var(--text-secondary)]">{p.desc}</td>
                        <td className="px-4 py-2.5">
                          <DefBadge value={p.def} />
                        </td>
                        <td className="px-4 py-2.5">
                          {p.commands ? (
                            <code className="text-xs font-mono text-[var(--text-muted)]">{p.commands}</code>
                          ) : (
                            <span className="text-xs text-[var(--text-muted)]">&mdash;</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        ))}
      </div>

      {filtered.length === 0 && (
        <div className="text-center py-12">
          <p className="text-[var(--text-muted)]">No permissions match your search.</p>
        </div>
      )}

      {/* LuckPerms example */}
      {!search.trim() && (
        <div className="mt-10 bg-[var(--bg-card)] rounded-xl border border-[var(--border)] p-6">
          <h2 className="text-lg font-semibold mb-3">LuckPerms Quick Setup</h2>
          <p className="text-sm text-[var(--text-secondary)] mb-4">
            Example commands to set up permissions for your server using LuckPerms:
          </p>
          <div className="space-y-3">
            <div>
              <p className="text-xs text-[var(--text-muted)] mb-1">Grant basic player permissions to the default group:</p>
              <code className="block bg-[var(--bg-main)] border border-[var(--border)] rounded-lg px-4 py-2.5 text-sm font-mono text-green-400">
                /lp group default permission set justplugin.player true
              </code>
            </div>
            <div>
              <p className="text-xs text-[var(--text-muted)] mb-1">Grant all permissions to the admin group:</p>
              <code className="block bg-[var(--bg-main)] border border-[var(--border)] rounded-lg px-4 py-2.5 text-sm font-mono text-orange-400">
                /lp group admin permission set justplugin.* true
              </code>
            </div>
            <div>
              <p className="text-xs text-[var(--text-muted)] mb-1">Grant a specific kit permission:</p>
              <code className="block bg-[var(--bg-main)] border border-[var(--border)] rounded-lg px-4 py-2.5 text-sm font-mono text-blue-400">
                /lp group default permission set justplugin.kits.starter true
              </code>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
