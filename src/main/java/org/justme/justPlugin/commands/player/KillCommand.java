package org.justme.justPlugin.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.*;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class KillCommand implements TabExecutor {

    private final JustPlugin plugin;

    private static final List<String> SPECIAL_ARGS = List.of(
            "mobs", "hostile", "entities", "friendly", "items", "players", "allplayers", "everything"
    );

    public KillCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1) {
            String arg = args[0].toLowerCase();

            // Check for special arguments
            if (SPECIAL_ARGS.contains(arg)) {
                if (!sender.hasPermission("justplugin.kill.others")) {
                    sender.sendMessage(plugin.getMessageManager().error("player.kill.no-permission-mass"));
                    return true;
                }

                Player senderPlayer = sender instanceof Player ? (Player) sender : null;

                switch (arg) {
                    case "mobs" -> {
                        int count = 0;
                        for (World world : Bukkit.getWorlds()) {
                            for (Entity entity : world.getEntities()) {
                                if (entity instanceof Mob && !(entity instanceof Player)) {
                                    entity.remove();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-mobs", "{amount}", String.valueOf(count)));
                    }
                    case "hostile" -> {
                        int count = 0;
                        for (World world : Bukkit.getWorlds()) {
                            for (Entity entity : world.getEntities()) {
                                if (isHostile(entity)) {
                                    entity.remove();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-hostile", "{amount}", String.valueOf(count)));
                    }
                    case "entities" -> {
                        int count = 0;
                        for (World world : Bukkit.getWorlds()) {
                            for (Entity entity : world.getEntities()) {
                                if (!(entity instanceof Player)) {
                                    entity.remove();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-entities", "{amount}", String.valueOf(count)));
                    }
                    case "friendly" -> {
                        int count = 0;
                        for (World world : Bukkit.getWorlds()) {
                            for (Entity entity : world.getEntities()) {
                                if (entity instanceof Mob && !(entity instanceof Player) && !isHostile(entity)) {
                                    entity.remove();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-friendly", "{amount}", String.valueOf(count)));
                    }
                    case "items" -> {
                        int count = 0;
                        for (World world : Bukkit.getWorlds()) {
                            for (Entity entity : world.getEntities()) {
                                if (entity instanceof Item) {
                                    entity.remove();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.cleared-items", "{amount}", String.valueOf(count)));
                    }
                    case "players" -> {
                        int count = 0;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.equals(senderPlayer)) continue;
                            p.setHealth(0);
                            p.sendMessage(plugin.getMessageManager().error("player.kill.killed-by", "{player}", sender.getName()));
                            count++;
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-players", "{amount}", String.valueOf(count)));
                    }
                    case "allplayers" -> {
                        int count = 0;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.setHealth(0);
                            if (!p.equals(senderPlayer)) {
                                p.sendMessage(plugin.getMessageManager().error("player.kill.killed-by", "{player}", sender.getName()));
                            }
                            count++;
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-allplayers", "{amount}", String.valueOf(count)));
                    }
                    case "everything" -> {
                        int entityCount = 0;
                        for (World world : Bukkit.getWorlds()) {
                            for (Entity entity : world.getEntities()) {
                                if (!(entity instanceof Player)) {
                                    entity.remove();
                                    entityCount++;
                                }
                            }
                        }
                        int playerCount = 0;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.setHealth(0);
                            if (!p.equals(senderPlayer)) {
                                p.sendMessage(plugin.getMessageManager().error("player.kill.killed-by", "{player}", sender.getName()));
                            }
                            playerCount++;
                        }
                        sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-everything", "{entities}", String.valueOf(entityCount), "{players}", String.valueOf(playerCount)));
                    }
                }
                return true;
            }

            // Normal player kill
            if (!sender.hasPermission("justplugin.kill.others")) {
                sender.sendMessage(plugin.getMessageManager().error("player.kill.no-permission-others"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getMessageManager().error("general.player-not-found"));
                return true;
            }
            target.setHealth(0);
            sender.sendMessage(plugin.getMessageManager().success("player.kill.killed-other", "{player}", target.getName()));
            target.sendMessage(plugin.getMessageManager().error("player.kill.killed-by", "{player}", sender.getName()));
            String killedBy = sender instanceof Player ? sender.getName() : "Console";
            plugin.getLogManager().log("admin", "<yellow>" + killedBy + "</yellow> killed <yellow>" + target.getName() + "</yellow>");
            return true;
        }

        // Self
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().error("player.kill.usage-console"));
            return true;
        }
        player.setHealth(0);
        player.sendMessage(plugin.getMessageManager().info("player.kill.killed-self"));
        return true;
    }

    private boolean isHostile(Entity entity) {
        return entity instanceof Monster || entity instanceof Slime
                || entity instanceof Phantom || entity instanceof Ghast
                || entity instanceof Shulker || entity instanceof EnderDragon
                || entity instanceof Hoglin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("justplugin.kill.others")) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (String s : SPECIAL_ARGS) {
                if (s.startsWith(input)) suggestions.add(s);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getVanishManager().isVanished(p.getUniqueId()) && !sender.hasPermission("justplugin.vanish.see")) continue;
                if (p.getName().toLowerCase().startsWith(input)) suggestions.add(p.getName());
            }
            return suggestions;
        }
        return List.of();
    }
}
