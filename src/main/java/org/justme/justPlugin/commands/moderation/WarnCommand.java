package org.justme.justPlugin.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.managers.WarnManager;
import org.justme.justPlugin.util.CC;
import org.justme.justPlugin.util.TimeUtil;

import java.util.*;
import java.util.stream.Collectors;

public class WarnCommand implements TabExecutor {

    private final JustPlugin plugin;
    // Pending removal confirmations: sender UUID -> (target UUID, warn index)
    private final Map<UUID, long[]> pendingRemovals = new HashMap<>(); // senderUuid -> [targetUuidMsb, targetUuidLsb, warnIndex, expireTime]

    public WarnCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(CC.error("Usage: /warn <add | remove | list> <player> [reason | index]"));
            return true;
        }

        String sub = args[0].toLowerCase();
        String executedBy = sender instanceof Player ? sender.getName() : "Console";

        switch (sub) {
            case "add" -> handleAdd(sender, args, executedBy);
            case "remove" -> handleRemove(sender, args, executedBy);
            case "list" -> handleList(sender, args);
            case "confirm" -> handleConfirm(sender, executedBy);
            case "cancel" -> handleCancel(sender);
            default -> sender.sendMessage(CC.error("Usage: /warn <add | remove | list> <player> [reason | index]"));
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args, String executedBy) {
        if (args.length < 2) {
            sender.sendMessage(CC.error("Usage: /warn add <player> [reason]"));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[1];

        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

        WarnManager.WarnEntry entry = plugin.getWarnManager().addWarn(uuid, name, reason, executedBy);
        int activeCount = plugin.getWarnManager().getActiveWarnCount(uuid);

        sender.sendMessage(CC.success("Warned <yellow>" + name + "</yellow>. <gray>(Warning #" + activeCount + ")"));
        sender.sendMessage(CC.line("Reason: <white>" + entry.reason));
        sender.sendMessage(CC.line("Punishment: <yellow>" + entry.punishment + (entry.punishmentDetail.isEmpty() ? "" : " " + entry.punishmentDetail)));

        plugin.getLogManager().log("warn", "<yellow>" + executedBy + "</yellow> warned <yellow>" + name + "</yellow> (#" + activeCount + "). Reason: <gray>" + entry.reason + " | Punishment: " + entry.punishment);
    }

    private void handleRemove(CommandSender sender, String[] args, String executedBy) {
        if (args.length < 3) {
            sender.sendMessage(CC.error("Usage: /warn remove <player> <index> [reason]"));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[1];

        int index;
        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(CC.error("Invalid warning index! Use /warn list <player> to see indices."));
            return;
        }

        WarnManager.WarnEntry entry = plugin.getWarnManager().getWarn(uuid, index);
        if (entry == null) {
            sender.sendMessage(CC.error("Warning #" + index + " not found for <yellow>" + name + "</yellow>."));
            return;
        }

        if (entry.lifted) {
            sender.sendMessage(CC.error("Warning #" + index + " has already been lifted."));
            return;
        }

        // Show details and ask for confirmation
        sender.sendMessage(CC.warning("Are you sure you want to lift warning #<yellow>" + index + "</yellow> for <yellow>" + name + "</yellow>?"));
        sender.sendMessage(CC.line("Reason: <white>" + entry.reason));
        sender.sendMessage(CC.line("Punishment: <yellow>" + entry.punishment + (entry.punishmentDetail.isEmpty() ? "" : " " + entry.punishmentDetail)));
        sender.sendMessage(CC.line("Warned by: <white>" + entry.warnedBy));
        sender.sendMessage(CC.line("Date: <white>" + plugin.getWarnManager().formatDate(entry.timestamp)));

        String liftReason = args.length >= 4 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : null;

        if (sender instanceof Player p) {
            // Store pending removal
            pendingRemovals.put(p.getUniqueId(), new long[]{
                    uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(),
                    index, System.currentTimeMillis() + 30000 // 30s timeout
            });

            boolean clickable = plugin.getConfig().getBoolean("clickable-commands.team", true);
            String confirmBtn = CC.clickCmd("<green>[✔ Confirm]</green>", "/warn confirm", clickable);
            String cancelBtn = CC.clickCmd("<red>[✕ Cancel]</red>", "/warn cancel", clickable);
            sender.sendMessage(CC.translate(" " + confirmBtn + "  " + cancelBtn));

            // Store lift reason for later
            if (liftReason != null) {
                // Use player data temporarily
                p.setMetadata("warnLiftReason", new org.bukkit.metadata.FixedMetadataValue(plugin, liftReason));
            }
        } else {
            // Console - just do it
            String finalReason = liftReason != null ? liftReason : "Lifted by console";
            plugin.getWarnManager().liftWarn(uuid, index, executedBy, finalReason);
            sender.sendMessage(CC.success("Warning #" + index + " for <yellow>" + name + "</yellow> has been lifted."));
            plugin.getLogManager().log("warn", "<yellow>" + executedBy + "</yellow> lifted warning #" + index + " for <yellow>" + name + "</yellow>. Reason: " + finalReason);
        }
    }

    private void handleConfirm(CommandSender sender, String executedBy) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(CC.error("Only players can confirm."));
            return;
        }
        long[] data = pendingRemovals.remove(p.getUniqueId());
        if (data == null || System.currentTimeMillis() > data[3]) {
            sender.sendMessage(CC.error("No pending warning removal to confirm."));
            return;
        }

        UUID targetUuid = new UUID(data[0], data[1]);
        int index = (int) data[2];

        String liftReason = "No reason";
        if (p.hasMetadata("warnLiftReason")) {
            liftReason = p.getMetadata("warnLiftReason").get(0).asString();
            p.removeMetadata("warnLiftReason", plugin);
        }

        String name = plugin.getWarnManager().getPlayerName(targetUuid);
        if (name == null) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(targetUuid);
            name = offP.getName() != null ? offP.getName() : targetUuid.toString();
        }

        if (plugin.getWarnManager().liftWarn(targetUuid, index, executedBy, liftReason)) {
            sender.sendMessage(CC.success("Warning #" + index + " for <yellow>" + name + "</yellow> has been lifted."));
            plugin.getLogManager().log("warn", "<yellow>" + executedBy + "</yellow> lifted warning #" + index + " for <yellow>" + name + "</yellow>. Reason: " + liftReason);
        } else {
            sender.sendMessage(CC.error("Failed to lift warning. It may have already been lifted."));
        }
    }

    private void handleCancel(CommandSender sender) {
        if (sender instanceof Player p) {
            pendingRemovals.remove(p.getUniqueId());
            p.removeMetadata("warnLiftReason", plugin);
        }
        sender.sendMessage(CC.info("Warning removal cancelled."));
    }

    private void handleList(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.error("Usage: /warn list <player>"));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offP = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = offP.getUniqueId();
        String name = offP.getName() != null ? offP.getName() : args[1];

        List<WarnManager.WarnEntry> warns = plugin.getWarnManager().getWarns(uuid);
        if (warns.isEmpty()) {
            sender.sendMessage(CC.info("<yellow>" + name + "</yellow> has no warnings."));
            return;
        }

        int active = plugin.getWarnManager().getActiveWarnCount(uuid);
        sender.sendMessage(CC.prefixed("<yellow>" + name + "</yellow>'s Warnings <gray>(" + active + " active, " + warns.size() + " total)"));

        for (WarnManager.WarnEntry entry : warns) {
            String status = entry.lifted ? "<red>[LIFTED]</red>" : "<green>[ACTIVE]</green>";
            sender.sendMessage(CC.line(status + " <gray>#" + entry.index + " <dark_gray>| <white>" + entry.reason));
            sender.sendMessage(CC.translate("   <dark_gray>  Punishment: <yellow>" + entry.punishment
                    + (entry.punishmentDetail.isEmpty() ? "" : " " + entry.punishmentDetail)
                    + " <dark_gray>| By: <white>" + entry.warnedBy
                    + " <dark_gray>| " + plugin.getWarnManager().formatDate(entry.timestamp)));
            if (entry.lifted) {
                sender.sendMessage(CC.translate("   <dark_gray>  Lifted by: <white>" + entry.liftedBy
                        + " <dark_gray>| Reason: <white>" + (entry.liftReason != null ? entry.liftReason : "N/A")
                        + " <dark_gray>| " + plugin.getWarnManager().formatDate(entry.liftedAt)));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("add", "remove", "list").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("list"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

