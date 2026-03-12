 package org.justme.justPlugin.commands.info;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.util.List;

public class HelpCommand implements TabExecutor {

    private final JustPlugin plugin;

    public HelpCommand(JustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        int page = 1;
        if (args.length >= 1) {
            try { page = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }

        boolean c = plugin.getConfig().getBoolean("clickable-commands.help", true);

        // Each entry: {label, suggestCommand, description}
        String[][][] helpPages = {
            {
                {"/tpa <player>", "/tpa ", "Request to teleport to a player"},
                {"/tpaccept", "/tpaccept", "Accept a teleport request"},
                {"/tpreject", "/tpreject", "Reject a teleport request"},
                {"/tpahere <player>", "/tpahere ", "Request a player to teleport to you"},
                {"/tpacancel", "/tpacancel", "Cancel your teleport request"},
                {"/back", "/back", "Return to your last location"},
                {"/home [name]", "/home ", "Teleport to a home"},
                {"/sethome [name]", "/sethome ", "Set a home"},
                {"/delhome <name>", "/delhome ", "Delete a home"},
            },
            {
                {"/warp [name]", "/warp ", "Teleport to or list warps"},
                {"/setwarp <name>", "/setwarp ", "Create a warp"},
                {"/delwarp <name>", "/delwarp ", "Delete a warp"},
                {"/spawn", "/spawn", "Teleport to spawn"},
                {"/tpr", "/tpr", "Random teleport"},
                {"/bal [player]", "/bal ", "Check balance"},
                {"/pay <player> <amount>", "/pay ", "Pay a player"},
                {"/msg <player> <msg>", "/msg ", "Private message"},
                {"/r <msg>", "/r ", "Reply to last message"},
            },
            {
                {"/fly [player]", "/fly ", "Toggle flight"},
                {"/gm <mode>", "/gm ", "Change gamemode"},
                {"/god", "/god", "Toggle god mode"},
                {"/vanish", "/vanish", "Toggle vanish"},
                {"/speed <1-10>", "/speed ", "Set speed"},
                {"/hat", "/hat", "Wear item as hat"},
                {"/skull [player]", "/skull ", "Get a player head"},
                {"/anvil", "/anvil", "Open virtual anvil"},
                {"/craft", "/craft", "Open virtual workbench"},
            },
            {
                {"/ban <player> [reason]", "/ban ", "Ban a player"},
                {"/tempban <player> <time>", "/tempban ", "Temp ban"},
                {"/unban <player>", "/unban ", "Unban a player"},
                {"/team create <name>", "/team create ", "Create a team"},
                {"/announce <msg>", "/announce ", "Broadcast message"},
                {"/sharecoords [all | team]", "/sharecoords ", "Share location"},
                {"/chat <all | team>", "/chat ", "Switch chat mode"},
                {"/invsee <player>", "/invsee ", "View inventory"},
                {"/sudo <player> <cmd>", "/sudo ", "Force command"},
            }
        };

        page = Math.max(1, Math.min(page, helpPages.length));

        sender.sendMessage(CC.translate("<gold><bold>JustPlugin Help (Page " + page + "/" + helpPages.length + ")</bold></gold>"));
        for (String[] entry : helpPages[page - 1]) {
            String cmdLabel = CC.suggestCmd("<yellow>" + entry[0] + "</yellow>", entry[1], c);
            sender.sendMessage(CC.translate(cmdLabel + " <gray>- " + entry[2]));
        }

        if (page < helpPages.length) {
            int next = page + 1;
            String nextBtn = CC.clickCmd("<yellow>/help " + next + "</yellow>", "/help " + next, c);
            sender.sendMessage(CC.info("Type " + nextBtn + " for the next page."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("1", "2", "3", "4");
        return List.of();
    }
}

