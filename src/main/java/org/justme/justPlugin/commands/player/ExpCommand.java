package org.justme.justPlugin.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public ExpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.error("Only players can use this command."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(CC.info("Your XP: <yellow>Level " + player.getLevel() + "</yellow> (<gray>" + player.getTotalExperience() + " total XP</gray>)"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(CC.error("Usage: /exp <set | give> <levels | orbs> <amount> [player]"));
            return true;
        }
        String action = args[0].toLowerCase();
        String type = args[1].toLowerCase();
        if (!type.equals("levels") && !type.equals("orbs") && !type.equals("level") && !type.equals("orb") && !type.equals("l") && !type.equals("o")) {
            player.sendMessage(CC.error("Type must be <yellow>levels</yellow> or <yellow>orbs</yellow>."));
            return true;
        }
        boolean isLevels = type.startsWith("l");
        try {
            int amount = Integer.parseInt(args[2]);
            Player target = player;
            if (args.length >= 4) {
                if (!player.hasPermission("justplugin.exp.others")) {
                    player.sendMessage(CC.error("You don't have permission to modify other players' experience."));
                    return true;
                }
                target = Bukkit.getPlayer(args[3]);
                if (target == null) {
                    player.sendMessage(CC.error("Player not found!"));
                    return true;
                }
            }
            switch (action) {
                case "set" -> {
                    if (isLevels) {
                        target.setLevel(amount);
                        target.setExp(0);
                        player.sendMessage(CC.success("Set <yellow>" + target.getName() + "</yellow>'s level to <yellow>" + amount + "</yellow>."));
                        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> set <yellow>" + target.getName() + "</yellow>'s level to <yellow>" + amount + "</yellow>");
                    } else {
                        target.setTotalExperience(0);
                        target.setLevel(0);
                        target.setExp(0);
                        target.giveExp(amount);
                        player.sendMessage(CC.success("Set <yellow>" + target.getName() + "</yellow>'s XP to <yellow>" + amount + "</yellow> orbs."));
                        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> set <yellow>" + target.getName() + "</yellow>'s XP to <yellow>" + amount + "</yellow> orbs");
                    }
                }
                case "give", "add" -> {
                    if (isLevels) {
                        target.setLevel(target.getLevel() + amount);
                        player.sendMessage(CC.success("Gave <yellow>" + amount + "</yellow> levels to <yellow>" + target.getName() + "</yellow>. (Now level " + target.getLevel() + ")"));
                        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> gave <yellow>" + amount + "</yellow> levels to <yellow>" + target.getName() + "</yellow>");
                    } else {
                        target.giveExp(amount);
                        player.sendMessage(CC.success("Gave <yellow>" + amount + "</yellow> XP orbs to <yellow>" + target.getName() + "</yellow>."));
                        plugin.getLogManager().log("admin", "<yellow>" + player.getName() + "</yellow> gave <yellow>" + amount + "</yellow> XP orbs to <yellow>" + target.getName() + "</yellow>");
                    }
                }
                default -> player.sendMessage(CC.error("Usage: /exp <set | give> <levels | orbs> <amount> [player]"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(CC.error("Invalid amount!"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return Stream.of("set", "give")
                .filter(n -> n.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2) return Stream.of("levels", "orbs")
                .filter(n -> n.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        if (args.length == 3) return List.of("<amount>");
        if (args.length == 4) return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[3].toLowerCase())).collect(Collectors.toList());
        return List.of();
    }
}

