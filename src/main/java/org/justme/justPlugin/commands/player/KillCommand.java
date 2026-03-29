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
                    sender.sendMessage(CC.error("You don't have permission to use mass kill commands."));
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
                        sender.sendMessage(CC.success("Killed <yellow>" + count + "</yellow> mobs (hostile + friendly)."));
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
                        sender.sendMessage(CC.success("Killed <yellow>" + count + "</yellow> hostile mobs."));
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
                        sender.sendMessage(CC.success("Killed <yellow>" + count + "</yellow> entities (mobs + items)."));
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
                        sender.sendMessage(CC.success("Killed <yellow>" + count + "</yellow> friendly mobs."));
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
                        sender.sendMessage(CC.success("Cleared <yellow>" + count + "</yellow> dropped items."));
                    }
                    case "players" -> {
                        int count = 0;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.equals(senderPlayer)) continue;
                            p.setHealth(0);
                            p.sendMessage(CC.error("You have been killed by <yellow>" + sender.getName() + "</yellow>."));
                            count++;
                        }
                        sender.sendMessage(CC.success("Killed <yellow>" + count + "</yellow> players (excluding yourself)."));
                    }
                    case "allplayers" -> {
                        int count = 0;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.setHealth(0);
                            if (!p.equals(senderPlayer)) {
                                p.sendMessage(CC.error("You have been killed by <yellow>" + sender.getName() + "</yellow>."));
                            }
                            count++;
                        }
                        sender.sendMessage(CC.success("Killed <yellow>" + count + "</yellow> players (including yourself)."));
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
                                p.sendMessage(CC.error("You have been killed by <yellow>" + sender.getName() + "</yellow>."));
                            }
                            playerCount++;
                        }
                        sender.sendMessage(CC.success("Killed <yellow>" + entityCount + "</yellow> entities and <yellow>" + playerCount + "</yellow> players."));
                    }
                }
                return true;
            }

            // Normal player kill
            if (!sender.hasPermission("justplugin.kill.others")) {
                sender.sendMessage(CC.error("You don't have permission to kill other players."));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(CC.error(plugin.getMessageManager().raw("general.player-not-found")));
                return true;
            }
            target.setHealth(0);
            sender.sendMessage(CC.success("Killed <yellow>" + target.getName() + "</yellow>."));
            target.sendMessage(CC.error("You have been killed by <yellow>" + sender.getName() + "</yellow>."));
            String killedBy = sender instanceof Player ? sender.getName() : "Console";
            plugin.getLogManager().log("admin", "<yellow>" + killedBy + "</yellow> killed <yellow>" + target.getName() + "</yellow>");
            return true;
        }

        // Self
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Usage: /kill <player | mobs | hostile | friendly | items | entities | players | allplayers | everything>"));
            return true;
        }
        player.setHealth(0);
        player.sendMessage(CC.info("You killed yourself."));
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
