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

        String[][] helpPages = {
            {
                "<gold><bold>JustPlugin Help (Page 1/4)</bold></gold>",
                "<yellow>/tpa <player></yellow> <gray>- Request to teleport to a player",
                "<yellow>/tpaccept</yellow> <gray>- Accept a teleport request",
                "<yellow>/tpreject</yellow> <gray>- Reject a teleport request",
                "<yellow>/tpahere <player></yellow> <gray>- Request a player to teleport to you",
                "<yellow>/tpacancel</yellow> <gray>- Cancel your teleport request",
                "<yellow>/back</yellow> <gray>- Return to your last location",
                "<yellow>/home [name]</yellow> <gray>- Teleport to a home",
                "<yellow>/sethome [name]</yellow> <gray>- Set a home",
                "<yellow>/delhome <name></yellow> <gray>- Delete a home",
            },
            {
                "<gold><bold>JustPlugin Help (Page 2/4)</bold></gold>",
                "<yellow>/warp [name]</yellow> <gray>- Teleport to or list warps",
                "<yellow>/setwarp <name></yellow> <gray>- Create a warp",
                "<yellow>/delwarp <name></yellow> <gray>- Delete a warp",
                "<yellow>/spawn</yellow> <gray>- Teleport to spawn",
                "<yellow>/tpr</yellow> <gray>- Random teleport",
                "<yellow>/bal [player]</yellow> <gray>- Check balance",
                "<yellow>/pay <player> <amount></yellow> <gray>- Pay a player",
                "<yellow>/msg <player> <msg></yellow> <gray>- Private message",
                "<yellow>/r <msg></yellow> <gray>- Reply to last message",
            },
            {
                "<gold><bold>JustPlugin Help (Page 3/4)</bold></gold>",
                "<yellow>/fly [player]</yellow> <gray>- Toggle flight",
                "<yellow>/gm <mode></yellow> <gray>- Change gamemode",
                "<yellow>/god</yellow> <gray>- Toggle god mode",
                "<yellow>/vanish</yellow> <gray>- Toggle vanish",
                "<yellow>/speed <1-10></yellow> <gray>- Set speed",
                "<yellow>/hat</yellow> <gray>- Wear item as hat",
                "<yellow>/skull [player]</yellow> <gray>- Get a player head",
                "<yellow>/anvil</yellow> <gray>- Open virtual anvil",
                "<yellow>/craft</yellow> <gray>- Open virtual workbench",
            },
            {
                "<gold><bold>JustPlugin Help (Page 4/4)</bold></gold>",
                "<yellow>/ban <player> [reason]</yellow> <gray>- Ban a player",
                "<yellow>/tempban <player> <time></yellow> <gray>- Temp ban",
                "<yellow>/unban <player></yellow> <gray>- Unban a player",
                "<yellow>/team create <name></yellow> <gray>- Create a team",
                "<yellow>/announce <msg></yellow> <gray>- Broadcast message",
                "<yellow>/sharecoords [all | team]</yellow> <gray>- Share location",
                "<yellow>/chat <all | team></yellow> <gray>- Switch chat mode",
                "<yellow>/invsee <player></yellow> <gray>- View inventory",
                "<yellow>/sudo <player> <cmd></yellow> <gray>- Force command",
            }
        };

        page = Math.max(1, Math.min(page, helpPages.length));
        for (String line : helpPages[page - 1]) {
            sender.sendMessage(CC.translate(line));
        }
        if (page < helpPages.length) {
            sender.sendMessage(CC.info("Type <yellow>/help " + (page + 1) + "</yellow> for the next page."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("1", "2", "3", "4");
        return List.of();
    }
}

