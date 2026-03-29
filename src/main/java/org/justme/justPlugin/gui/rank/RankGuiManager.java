package org.justme.justPlugin.gui.rank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.DisplayNameNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.justme.justPlugin.JustPlugin;
import org.justme.justPlugin.util.CC;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Full LuckPerms management GUI system accessed via /rank.
 * Supports group management (CRUD, prefix, suffix, parent, permissions)
 * and player management (add/remove groups, permissions).
 */
public class RankGuiManager implements Listener {

    public static final String GUI_PREFIX = "<dark_gray><bold>Rank Manager</bold></dark_gray>";
    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7 items
    private static final int PAGE_SIZE_PERMS = 28;
    /** Typing this keyword clears the value (prefix, suffix, display name). */
    private static final String CLEAR_KEYWORD = "clear";

    private final JustPlugin plugin;
    private final Map<UUID, RankSession> sessions = new HashMap<>();
    private final Map<UUID, Consumer<String>> signCallbacks = new HashMap<>();

    public RankGuiManager(JustPlugin plugin) {
        this.plugin = plugin;
    }

    // ======================== Entry Points ========================

    public void open(Player player) {
        RankSession session = new RankSession();
        sessions.put(player.getUniqueId(), session);
        openMainMenu(player, session);
    }

    private LuckPerms lp() {
        return LuckPermsProvider.get();
    }

    // ======================== Sign Input ========================

    public void requestSignInput(Player player, String prompt, Consumer<String> callback) {
        signCallbacks.put(player.getUniqueId(), callback);
        player.closeInventory();
        player.sendMessage(CC.info("Type your input in chat. <gray>(" + prompt + ")"));
        player.sendMessage(CC.info("Type <yellow>cancel</yellow> to go back, or <yellow>clear</yellow> to remove the value."));
    }

    /**
     * Called from PlayerListener when a player with a pending sign callback chats.
     * Returns true if the message was consumed.
     */
    public boolean handleChatInput(Player player, String message) {
        Consumer<String> cb = signCallbacks.remove(player.getUniqueId());
        if (cb == null) return false;
        if (message.equalsIgnoreCase("cancel")) {
            // Re-open current screen
            RankSession session = sessions.get(player.getUniqueId());
            if (session != null) {
                Bukkit.getScheduler().runTask(plugin, () -> renderCurrentScreen(player, session));
            }
            return true;
        }
        Bukkit.getScheduler().runTask(plugin, () -> cb.accept(message));
        return true;
    }

    public boolean hasPendingInput(UUID uuid) {
        return signCallbacks.containsKey(uuid);
    }

    // ======================== Screen Rendering ========================

    private void renderCurrentScreen(Player player, RankSession session) {
        switch (session.getScreen()) {
            case MAIN_MENU -> openMainMenu(player, session);
            case GROUP_LIST -> openGroupList(player, session);
            case GROUP_ACTIONS -> openGroupActions(player, session);
            case GROUP_PERMISSIONS -> openGroupPermissions(player, session);
            case PLAYER_LIST -> openPlayerList(player, session);
            case PLAYER_ACTIONS -> openPlayerActions(player, session);
            case PLAYER_GROUPS -> openPlayerGroupsList(player, session);
            case PLAYER_PERMISSIONS -> openPlayerPermissions(player, session);
            case SELECT_GROUP -> openGroupSelectForPlayer(player, session);
        }
    }

    // ========== MAIN MENU ==========
    private void openMainMenu(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.MAIN_MENU);
        Inventory inv = Bukkit.createInventory(null, 27, CC.translate(GUI_PREFIX + " - Main"));

        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Groups button (slot 11)
        ItemStack groups = item(Material.BOOKSHELF, "<gold><bold>Groups</bold>",
                "<gray>View and manage server ranks.",
                "",
                player.hasPermission("justplugin.rank.groups")
                        ? "<yellow>Click to open"
                        : "<red>No permission");
        inv.setItem(11, groups);

        // Players button (slot 15)
        ItemStack players = item(Material.PLAYER_HEAD, "<aqua><bold>Players</bold>",
                "<gray>View and manage player permissions.",
                "",
                player.hasPermission("justplugin.rank.players")
                        ? "<yellow>Click to open"
                        : "<red>No permission");
        inv.setItem(15, players);

        // Close button (slot 22)
        inv.setItem(22, item(Material.BARRIER, "<red>Close"));

        player.openInventory(inv);
    }

    // ========== GROUP LIST ==========
    private void openGroupList(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.GROUP_LIST);
        Inventory inv = Bukkit.createInventory(null, 54, CC.translate(GUI_PREFIX + " - Groups"));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        Collection<Group> allGroups = lp().getGroupManager().getLoadedGroups();
        List<Group> sorted = allGroups.stream()
                .sorted((a, b) -> {
                    // Default group first
                    if (a.getName().equals("default")) return -1;
                    if (b.getName().equals("default")) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .filter(g -> session.getSearchFilter() == null ||
                        g.getName().toLowerCase().contains(session.getSearchFilter().toLowerCase()))
                .toList();

        int start = session.getPage() * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, sorted.size());

        // Default group on slot 4 (top row, center) if on page 0 and default exists
        int slotIdx = 0;
        int[] slots = getContentSlots();

        if (session.getPage() == 0) {
            Optional<Group> defGroup = sorted.stream().filter(g -> g.getName().equals("default")).findFirst();
            if (defGroup.isPresent()) {
                inv.setItem(4, makeGroupItem(defGroup.get(), true));
            }
        }

        // Fill groups in content area
        List<Group> pageGroups = sorted.stream()
                .filter(g -> session.getPage() > 0 || !g.getName().equals("default"))
                .skip(Math.max(0, start - (session.getPage() == 0 ? 0 : 0)))
                .limit(ITEMS_PER_PAGE)
                .toList();

        // Simpler: just paginate the non-default groups starting from row 1
        List<Group> nonDefault = sorted.stream().filter(g -> !g.getName().equals("default")).toList();
        int totalPages = Math.max(1, (int) Math.ceil(nonDefault.size() / (double) ITEMS_PER_PAGE));
        int pageStart = session.getPage() * ITEMS_PER_PAGE;

        for (int i = pageStart; i < Math.min(pageStart + ITEMS_PER_PAGE, nonDefault.size()); i++) {
            if (slotIdx < slots.length) {
                inv.setItem(slots[slotIdx], makeGroupItem(nonDefault.get(i), false));
                slotIdx++;
            }
        }

        // Bottom row navigation
        if (session.getPage() > 0) {
            inv.setItem(45, item(Material.ARROW, "<yellow>← Previous Page"));
        }
        inv.setItem(48, item(Material.OAK_SIGN, "<green>Search",
                "<gray>Click to search groups by name.",
                session.getSearchFilter() != null ? "<yellow>Filter: " + session.getSearchFilter() : ""));
        if (session.getSearchFilter() != null) {
            inv.setItem(47, item(Material.WATER_BUCKET, "<red>Clear Search"));
        }

        // Create group button
        if (player.hasPermission("justplugin.rank.groups.create")) {
            inv.setItem(50, item(Material.EMERALD, "<green><bold>+ Create Group</bold>",
                    "<gray>Click to create a new group."));
        }

        if (session.getPage() < totalPages - 1) {
            inv.setItem(53, item(Material.ARROW, "<yellow>Next Page →"));
        }
        inv.setItem(49, item(Material.DARK_OAK_DOOR, "<red>Back to Menu"));

        // Page indicator
        inv.setItem(52, item(Material.PAPER, "<gray>Page " + (session.getPage() + 1) + "/" + totalPages));

        player.openInventory(inv);
    }

    private ItemStack makeGroupItem(Group group, boolean isDefault) {
        Material mat = isDefault ? Material.GOLDEN_APPLE : Material.EMERALD_BLOCK;
        String displayName = group.getDisplayName() != null ? group.getDisplayName() : group.getName();
        String prefix = getGroupMeta(group, "prefix");
        String suffix = getGroupMeta(group, "suffix");

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Group: <white>" + group.getName());
        if (prefix != null) lore.add("<gray>Prefix: <white>" + prefix);
        if (suffix != null) lore.add("<gray>Suffix: <white>" + suffix);
        lore.add("<gray>Weight: <white>" + getGroupWeight(group));
        long permCount = group.getNodes().stream().filter(n -> n.getType() == NodeType.PERMISSION).count();
        lore.add("<gray>Permissions: <white>" + permCount);
        // Parents
        List<String> parents = group.getNodes(NodeType.INHERITANCE).stream()
                .map(n -> n.getGroupName()).toList();
        if (!parents.isEmpty()) lore.add("<gray>Parents: <white>" + String.join(", ", parents));
        if (isDefault) lore.add("");
        if (isDefault) lore.add("<gold>★ Default Group");
        lore.add("");
        lore.add("<yellow>Click to manage");
        return item(mat, (isDefault ? "<gold><bold>" : "<green><bold>") + displayName, lore.toArray(new String[0]));
    }

    // ========== GROUP ACTIONS ==========
    private void openGroupActions(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.GROUP_ACTIONS);
        String groupName = session.getSelectedGroup();
        Group group = lp().getGroupManager().getGroup(groupName);
        if (group == null) {
            player.sendMessage(CC.error("Group <yellow>" + groupName + "</yellow> no longer exists."));
            session.setScreen(RankSession.Screen.GROUP_LIST);
            openGroupList(player, session);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, CC.translate(GUI_PREFIX + " - " + groupName));
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);

        // Group info header (slot 4)
        String displayName = group.getDisplayName() != null ? group.getDisplayName() : group.getName();
        inv.setItem(4, item(Material.NAME_TAG, "<gold><bold>" + displayName,
                "<gray>Internal name: <white>" + group.getName(),
                "<gray>Weight: <white>" + getGroupWeight(group)));

        // Action buttons
        if (player.hasPermission("justplugin.rank.groups.rename")) {
            inv.setItem(19, item(Material.WRITABLE_BOOK, "<yellow>Rename",
                    "<gray>Change the group's display name.",
                    "<dark_gray>Right-click to change internal name."));
        }
        if (player.hasPermission("justplugin.rank.groups.prefix")) {
            String cur = getGroupMeta(group, "prefix");
            inv.setItem(20, item(Material.OAK_SIGN, "<aqua>Change Prefix",
                    "<gray>Current: <white>" + (cur != null ? cur : "None"),
                    "<yellow>Click to change"));
        }
        if (player.hasPermission("justplugin.rank.groups.suffix")) {
            String cur = getGroupMeta(group, "suffix");
            inv.setItem(21, item(Material.OAK_SIGN, "<blue>Change Suffix",
                    "<gray>Current: <white>" + (cur != null ? cur : "None"),
                    "<yellow>Click to change"));
        }
        if (player.hasPermission("justplugin.rank.groups.parent")) {
            List<String> parents = group.getNodes(NodeType.INHERITANCE).stream()
                    .map(InheritanceNode::getGroupName).toList();
            inv.setItem(23, item(Material.IRON_BARS, "<light_purple>Set Parent",
                    "<gray>Current parents: <white>" + (parents.isEmpty() ? "None" : String.join(", ", parents)),
                    "",
                    "<yellow>Left-click to add a parent group",
                    "<red>Right-click to remove all parents"));
        }
        if (player.hasPermission("justplugin.rank.groups.permissions")) {
            long count = group.getNodes().stream().filter(n -> n.getType() == NodeType.PERMISSION).count();
            inv.setItem(25, item(Material.BOOK, "<green>Manage Permissions",
                    "<gray>Nodes: <white>" + count,
                    "<yellow>Click to view/edit permission nodes"));
        }
        if (player.hasPermission("justplugin.rank.groups.delete") && !groupName.equals("default")) {
            inv.setItem(40, item(Material.TNT, "<red><bold>Delete Group</bold>",
                    "<gray>Permanently delete this group.",
                    "<dark_red>This action cannot be undone!"));
        }

        inv.setItem(49, item(Material.DARK_OAK_DOOR, "<red>Back to Groups"));
        player.openInventory(inv);
    }

    // ========== GROUP PERMISSIONS ==========
    private void openGroupPermissions(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.GROUP_PERMISSIONS);
        Group group = lp().getGroupManager().getGroup(session.getSelectedGroup());
        if (group == null) {
            player.sendMessage(CC.error("Group no longer exists."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, CC.translate(GUI_PREFIX + " - Perms: " + session.getSelectedGroup()));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Get permission nodes (not inheritance/meta)
        List<Node> permNodes = group.getNodes().stream()
                .filter(n -> n.getType() == NodeType.PERMISSION)
                .filter(n -> session.getSearchFilter() == null ||
                        n.getKey().toLowerCase().contains(session.getSearchFilter().toLowerCase()))
                .sorted(Comparator.comparing(Node::getKey))
                .toList();

        renderPermissionPage(inv, permNodes, session, player, "group");
        player.openInventory(inv);
    }

    // ========== PLAYER LIST ==========
    private void openPlayerList(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.PLAYER_LIST);
        Inventory inv = Bukkit.createInventory(null, 54, CC.translate(GUI_PREFIX + " - Players"));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Get all known users from LuckPerms
        Set<UUID> uniqueUsers = lp().getUserManager().getUniqueUsers().join();
        List<OfflinePlayer> players = uniqueUsers.stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(op -> op.getName() != null)
                .filter(op -> session.getSearchFilter() == null ||
                        op.getName().toLowerCase().contains(session.getSearchFilter().toLowerCase()))
                .sorted(Comparator.comparing(op -> op.getName().toLowerCase()))
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(players.size() / (double) ITEMS_PER_PAGE));
        int[] slots = getContentSlots();
        int pageStart = session.getPage() * ITEMS_PER_PAGE;

        for (int i = pageStart; i < Math.min(pageStart + ITEMS_PER_PAGE, players.size()); i++) {
            int slotIdx = i - pageStart;
            if (slotIdx < slots.length) {
                OfflinePlayer op = players.get(i);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(op);
                    meta.displayName(CC.translate("<white>" + op.getName()));
                    List<Component> lore = new ArrayList<>();
                    lore.add(CC.translate("<gray>UUID: <dark_gray>" + op.getUniqueId().toString().substring(0, 8) + "..."));
                    lore.add(CC.translate(op.isOnline() ? "<green>Online" : "<red>Offline"));
                    lore.add(Component.empty());
                    lore.add(CC.translate("<yellow>Click to manage"));
                    meta.lore(lore);
                    head.setItemMeta(meta);
                }
                inv.setItem(slots[slotIdx], head);
            }
        }

        // Bottom nav
        if (session.getPage() > 0) inv.setItem(45, item(Material.ARROW, "<yellow>← Previous Page"));
        inv.setItem(48, item(Material.OAK_SIGN, "<green>Search",
                "<gray>Click to search players by name.",
                session.getSearchFilter() != null ? "<yellow>Filter: " + session.getSearchFilter() : ""));
        if (session.getSearchFilter() != null) inv.setItem(47, item(Material.WATER_BUCKET, "<red>Clear Search"));
        if (session.getPage() < totalPages - 1) inv.setItem(53, item(Material.ARROW, "<yellow>Next Page →"));
        inv.setItem(49, item(Material.DARK_OAK_DOOR, "<red>Back to Menu"));
        inv.setItem(52, item(Material.PAPER, "<gray>Page " + (session.getPage() + 1) + "/" + totalPages));

        player.openInventory(inv);
    }

    // ========== PLAYER ACTIONS ==========
    private void openPlayerActions(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.PLAYER_ACTIONS);
        String name = session.getSelectedPlayerName();
        Inventory inv = Bukkit.createInventory(null, 36, CC.translate(GUI_PREFIX + " - " + name));
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);

        // Player head info
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(session.getSelectedPlayerUUID())));
            sm.displayName(CC.translate("<gold><bold>" + name));
            sm.lore(List.of(CC.translate("<gray>UUID: <dark_gray>" + session.getSelectedPlayerUUID())));
            head.setItemMeta(sm);
        }
        inv.setItem(4, head);

        // Actions
        if (player.hasPermission("justplugin.rank.players.addgroup")) {
            inv.setItem(11, item(Material.EMERALD, "<green>Add to Group",
                    "<gray>Add this player to a LuckPerms group."));
        }
        if (player.hasPermission("justplugin.rank.players.listgroups")) {
            inv.setItem(13, item(Material.BOOK, "<aqua>View Groups",
                    "<gray>See all groups this player belongs to."));
        }
        if (player.hasPermission("justplugin.rank.players.removegroup")) {
            inv.setItem(15, item(Material.REDSTONE, "<red>Remove from Group",
                    "<gray>Remove this player from a group."));
        }
        if (player.hasPermission("justplugin.rank.players.permissions")) {
            inv.setItem(22, item(Material.WRITABLE_BOOK, "<yellow>Manage Permissions",
                    "<gray>View/edit this player's permission nodes."));
        }

        inv.setItem(31, item(Material.DARK_OAK_DOOR, "<red>Back to Players"));
        player.openInventory(inv);
    }

    // ========== PLAYER GROUPS LIST ==========
    private void openPlayerGroupsList(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.PLAYER_GROUPS);
        UUID targetUUID = UUID.fromString(session.getSelectedPlayerUUID());

        lp().getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Inventory inv = Bukkit.createInventory(null, 36,
                        CC.translate(GUI_PREFIX + " - " + session.getSelectedPlayerName() + "'s Groups"));
                fill(inv, Material.BLACK_STAINED_GLASS_PANE);

                List<InheritanceNode> groups = user.getNodes(NodeType.INHERITANCE).stream().toList();
                int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
                int idx = 0;
                for (InheritanceNode node : groups) {
                    if (idx >= slots.length) break;
                    boolean hasExpiry = node.hasExpiry();
                    String expiryStr = hasExpiry ? formatExpiry(node.getExpiry()) : "Permanent";
                    boolean isDefault = node.getGroupName().equalsIgnoreCase("default");
                    String removeLine;
                    if (isDefault) {
                        removeLine = "<dark_gray>Cannot be removed (default group)";
                    } else if (player.hasPermission("justplugin.rank.players.removegroup")) {
                        removeLine = "<red>Click to remove";
                    } else {
                        removeLine = "";
                    }
                    inv.setItem(slots[idx], item(
                            node.getValue() ? Material.LIME_WOOL : Material.RED_WOOL,
                            "<white>" + node.getGroupName(),
                            "<gray>Status: " + (node.getValue() ? "<green>Active" : "<red>Denied"),
                            "<gray>Duration: <white>" + expiryStr,
                            "",
                            removeLine));
                    idx++;
                }

                inv.setItem(31, item(Material.DARK_OAK_DOOR, "<red>Back"));
                player.openInventory(inv);
            });
        });
    }

    // ========== PLAYER PERMISSIONS ==========
    private void openPlayerPermissions(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.PLAYER_PERMISSIONS);
        UUID targetUUID = UUID.fromString(session.getSelectedPlayerUUID());

        lp().getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Inventory inv = Bukkit.createInventory(null, 54,
                        CC.translate(GUI_PREFIX + " - Perms: " + session.getSelectedPlayerName()));
                fill(inv, Material.BLACK_STAINED_GLASS_PANE);

                List<Node> permNodes = user.getNodes().stream()
                        .filter(n -> n.getType() == NodeType.PERMISSION)
                        .filter(n -> session.getSearchFilter() == null ||
                                n.getKey().toLowerCase().contains(session.getSearchFilter().toLowerCase()))
                        .sorted(Comparator.comparing(Node::getKey))
                        .toList();

                renderPermissionPage(inv, permNodes, session, player, "player");
                player.openInventory(inv);
            });
        });
    }

    // ========== SELECT GROUP (for adding player to group) ==========
    private void openGroupSelectForPlayer(Player player, RankSession session) {
        session.setScreen(RankSession.Screen.SELECT_GROUP);
        Inventory inv = Bukkit.createInventory(null, 54, CC.translate(GUI_PREFIX + " - Select Group"));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        List<Group> groups = lp().getGroupManager().getLoadedGroups().stream()
                .sorted(Comparator.comparing(g -> g.getName().toLowerCase()))
                .filter(g -> session.getSearchFilter() == null ||
                        g.getName().toLowerCase().contains(session.getSearchFilter().toLowerCase()))
                .toList();

        int[] slots = getContentSlots();
        int totalPages = Math.max(1, (int) Math.ceil(groups.size() / (double) ITEMS_PER_PAGE));
        int pageStart = session.getPage() * ITEMS_PER_PAGE;

        for (int i = pageStart; i < Math.min(pageStart + ITEMS_PER_PAGE, groups.size()); i++) {
            int slotIdx = i - pageStart;
            if (slotIdx < slots.length) {
                Group g = groups.get(i);
                inv.setItem(slots[slotIdx], item(Material.EMERALD_BLOCK, "<green>" + g.getName(),
                        "<gray>Click to add <white>" + session.getSelectedPlayerName() + "</white> to this group."));
            }
        }

        if (session.getPage() > 0) inv.setItem(45, item(Material.ARROW, "<yellow>← Previous Page"));
        if (session.getPage() < totalPages - 1) inv.setItem(53, item(Material.ARROW, "<yellow>Next Page →"));
        inv.setItem(49, item(Material.DARK_OAK_DOOR, "<red>Back"));
        inv.setItem(52, item(Material.PAPER, "<gray>Page " + (session.getPage() + 1) + "/" + totalPages));

        player.openInventory(inv);
    }

    // ======================== Permission Page Helper ========================

    private void renderPermissionPage(Inventory inv, List<Node> permNodes, RankSession session, Player player, String type) {
        int[] slots = getContentSlots();
        int totalPages = Math.max(1, (int) Math.ceil(permNodes.size() / (double) PAGE_SIZE_PERMS));
        int pageStart = session.getPage() * PAGE_SIZE_PERMS;

        String addPerm = type.equals("group") ? "justplugin.rank.groups.permissions.add" : "justplugin.rank.players.permissions.add";
        String togglePerm = type.equals("group") ? "justplugin.rank.groups.permissions.toggle" : "justplugin.rank.players.permissions.toggle";
        String removePerm = type.equals("group") ? "justplugin.rank.groups.permissions.remove" : "justplugin.rank.players.permissions.remove";

        for (int i = pageStart; i < Math.min(pageStart + PAGE_SIZE_PERMS, permNodes.size()); i++) {
            int slotIdx = i - pageStart;
            if (slotIdx >= slots.length) break;
            Node node = permNodes.get(i);
            boolean enabled = node.getValue();
            boolean hasExpiry = node.hasExpiry();
            String expiryStr = hasExpiry ? formatExpiry(node.getExpiry()) : "Permanent";

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Status: " + (enabled ? "<green>Enabled" : "<red>Disabled"));
            lore.add("<gray>Duration: <white>" + expiryStr);
            lore.add("");
            if (player.hasPermission(togglePerm)) lore.add("<yellow>Left-click to toggle");
            if (player.hasPermission(removePerm)) lore.add("<red>Right-click to remove");

            inv.setItem(slots[slotIdx], item(
                    enabled ? Material.LIME_DYE : Material.GRAY_DYE,
                    "<white>" + node.getKey(),
                    lore.toArray(new String[0])));
        }

        // Bottom nav
        if (session.getPage() > 0) inv.setItem(45, item(Material.ARROW, "<yellow>← Previous Page"));
        inv.setItem(48, item(Material.OAK_SIGN, "<green>Search Permissions",
                session.getSearchFilter() != null ? "<yellow>Filter: " + session.getSearchFilter() : ""));
        if (session.getSearchFilter() != null) inv.setItem(47, item(Material.WATER_BUCKET, "<red>Clear Search"));
        if (player.hasPermission(addPerm)) {
            inv.setItem(50, item(Material.EMERALD, "<green><bold>+ Add Permission</bold>",
                    "<gray>Click to add a new permission node."));
        }
        if (session.getPage() < totalPages - 1) inv.setItem(53, item(Material.ARROW, "<yellow>Next Page →"));
        inv.setItem(49, item(Material.DARK_OAK_DOOR, "<red>Back"));
        inv.setItem(52, item(Material.PAPER, "<gray>Page " + (session.getPage() + 1) + "/" + totalPages));
    }

    // ======================== Click Handler ========================

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.startsWith("Rank Manager")) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0) return;

        RankSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        switch (session.getScreen()) {
            case MAIN_MENU -> handleMainMenuClick(player, session, slot);
            case GROUP_LIST -> handleGroupListClick(player, session, slot, event);
            case GROUP_ACTIONS -> handleGroupActionsClick(player, session, slot, event);
            case GROUP_PERMISSIONS -> handleGroupPermsClick(player, session, slot, event);
            case PLAYER_LIST -> handlePlayerListClick(player, session, slot, event);
            case PLAYER_ACTIONS -> handlePlayerActionsClick(player, session, slot);
            case PLAYER_GROUPS -> handlePlayerGroupsClick(player, session, slot, event);
            case PLAYER_PERMISSIONS -> handlePlayerPermsClick(player, session, slot, event);
            case SELECT_GROUP -> handleSelectGroupClick(player, session, slot, event);
        }
    }

    // --- Main Menu ---
    private void handleMainMenuClick(Player player, RankSession session, int slot) {
        if (slot == 11 && player.hasPermission("justplugin.rank.groups")) {
            openGroupList(player, session);
        } else if (slot == 15 && player.hasPermission("justplugin.rank.players")) {
            openPlayerList(player, session);
        } else if (slot == 22) {
            player.closeInventory();
            sessions.remove(player.getUniqueId());
        }
    }

    // --- Group List ---
    private void handleGroupListClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        if (slot == 49) { openMainMenu(player, session); return; }
        if (slot == 45 && session.getPage() > 0) { session.setPage(session.getPage() - 1); openGroupList(player, session); return; }
        if (slot == 53) { session.setPage(session.getPage() + 1); openGroupList(player, session); return; }
        if (slot == 48) {
            requestSignInput(player, "Group name to search", input -> {
                session.setSearchFilter(input);
                openGroupList(player, session);
            });
            return;
        }
        if (slot == 47 && session.getSearchFilter() != null) { session.clearSearch(); openGroupList(player, session); return; }
        if (slot == 50 && player.hasPermission("justplugin.rank.groups.create")) {
            requestSignInput(player, "New group name", name -> {
                lp().getGroupManager().createAndLoadGroup(name.toLowerCase().replaceAll("\\s+", "_"))
                        .thenRunAsync(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(CC.success("Group <yellow>" + name + "</yellow> created!"));
                            openGroupList(player, session);
                        }));
            });
            return;
        }

        // Clicked a group item
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
        if (clicked.getType() == Material.EMERALD_BLOCK || clicked.getType() == Material.GOLDEN_APPLE
                || clicked.getType() == Material.ARROW || clicked.getType() == Material.PAPER) {
            // Extract group name from lore
            String groupName = extractGroupName(clicked);
            if (groupName != null) {
                session.setSelectedGroup(groupName);
                openGroupActions(player, session);
            }
        }
    }

    // --- Group Actions ---
    private void handleGroupActionsClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        String groupName = session.getSelectedGroup();
        if (slot == 49) { openGroupList(player, session); return; }

        // Rename (slot 19)
        if (slot == 19 && player.hasPermission("justplugin.rank.groups.rename")) {
            requestSignInput(player, "New display name", newName -> {
                Group group = lp().getGroupManager().getGroup(groupName);
                if (group == null) return;
                // Clear existing display name node
                group.data().clear(NodeType.DISPLAY_NAME::matches);
                if (newName.equalsIgnoreCase(CLEAR_KEYWORD)) {
                    // Clear display name - don't add a new node
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Display name cleared for <yellow>" + groupName + "</yellow>."));
                                openGroupActions(player, session);
                            }));
                } else {
                    group.data().add(DisplayNameNode.builder(newName).build());
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Display name updated to <yellow>" + newName + "</yellow>!"));
                                openGroupActions(player, session);
                            }));
                }
            });
            return;
        }
        // Prefix (slot 20)
        if (slot == 20 && player.hasPermission("justplugin.rank.groups.prefix")) {
            requestSignInput(player, "New prefix (use & for colors)", prefix -> {
                Group group = lp().getGroupManager().getGroup(groupName);
                if (group == null) return;
                group.data().clear(NodeType.PREFIX::matches);
                if (prefix.equalsIgnoreCase(CLEAR_KEYWORD)) {
                    // Clear prefix - don't add a new node
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Prefix cleared for <yellow>" + groupName + "</yellow>."));
                                openGroupActions(player, session);
                            }));
                } else {
                    group.data().add(PrefixNode.builder(prefix, getGroupWeight(group)).build());
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Prefix updated to <yellow>" + prefix + "</yellow>!"));
                                openGroupActions(player, session);
                            }));
                }
            });
            return;
        }
        // Suffix (slot 21)
        if (slot == 21 && player.hasPermission("justplugin.rank.groups.suffix")) {
            requestSignInput(player, "New suffix (use & for colors)", suffix -> {
                Group group = lp().getGroupManager().getGroup(groupName);
                if (group == null) return;
                group.data().clear(NodeType.SUFFIX::matches);
                if (suffix.equalsIgnoreCase(CLEAR_KEYWORD)) {
                    // Clear suffix - don't add a new node
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Suffix cleared for <yellow>" + groupName + "</yellow>."));
                                openGroupActions(player, session);
                            }));
                } else {
                    group.data().add(SuffixNode.builder(suffix, getGroupWeight(group)).build());
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Suffix updated to <yellow>" + suffix + "</yellow>!"));
                                openGroupActions(player, session);
                            }));
                }
            });
            return;
        }
        // Parent (slot 23)
        if (slot == 23 && player.hasPermission("justplugin.rank.groups.parent")) {
            if (event.isRightClick()) {
                // Right-click: remove all parent groups
                Group group = lp().getGroupManager().getGroup(groupName);
                if (group == null) return;
                List<String> parents = group.getNodes(NodeType.INHERITANCE).stream()
                        .map(InheritanceNode::getGroupName).toList();
                if (parents.isEmpty()) {
                    player.sendMessage(CC.error("This group has no parent groups to remove."));
                    return;
                }
                group.data().clear(NodeType.INHERITANCE::matches);
                lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(CC.success("Removed all parent groups from <yellow>" + groupName + "</yellow>."));
                            openGroupActions(player, session);
                        }));
            } else {
                // Left-click: add a parent group
                requestSignInput(player, "Parent group name to add", parentName -> {
                    Group group = lp().getGroupManager().getGroup(groupName);
                    Group parent = lp().getGroupManager().getGroup(parentName.toLowerCase());
                    if (group == null) return;
                    if (parent == null) {
                        player.sendMessage(CC.error("Group <yellow>" + parentName + "</yellow> does not exist."));
                        openGroupActions(player, session);
                        return;
                    }
                    group.data().add(InheritanceNode.builder(parentName.toLowerCase()).build());
                    lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Added parent <yellow>" + parentName + "</yellow>!"));
                                openGroupActions(player, session);
                            }));
                });
            }
            return;
        }
        // Permissions (slot 25)
        if (slot == 25 && player.hasPermission("justplugin.rank.groups.permissions")) {
            session.clearSearch();
            openGroupPermissions(player, session);
            return;
        }
        // Delete (slot 40)
        if (slot == 40 && player.hasPermission("justplugin.rank.groups.delete") && !groupName.equals("default")) {
            lp().getGroupManager().deleteGroup(lp().getGroupManager().getGroup(groupName))
                    .thenRunAsync(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(CC.success("Group <yellow>" + groupName + "</yellow> deleted!"));
                        openGroupList(player, session);
                    }));
        }
    }

    // --- Group Perms ---
    private void handleGroupPermsClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        if (slot == 49) { openGroupActions(player, session); return; }
        if (slot == 45) { session.setPage(session.getPage() - 1); openGroupPermissions(player, session); return; }
        if (slot == 53) { session.setPage(session.getPage() + 1); openGroupPermissions(player, session); return; }
        if (slot == 48) {
            requestSignInput(player, "Permission to search", input -> {
                session.setSearchFilter(input);
                openGroupPermissions(player, session);
            });
            return;
        }
        if (slot == 47) { session.clearSearch(); openGroupPermissions(player, session); return; }
        if (slot == 50 && player.hasPermission("justplugin.rank.groups.permissions.add")) {
            requestSignInput(player, "Permission node to add", perm -> {
                Group group = lp().getGroupManager().getGroup(session.getSelectedGroup());
                if (group == null) return;
                group.data().add(Node.builder(perm).build());
                lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(CC.success("Added permission <yellow>" + perm + "</yellow>!"));
                            openGroupPermissions(player, session);
                        }));
            });
            return;
        }

        // Clicked a perm node
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        if (clicked.getType() != Material.LIME_DYE && clicked.getType() != Material.GRAY_DYE) return;
        String permKey = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
        Group group = lp().getGroupManager().getGroup(session.getSelectedGroup());
        if (group == null) return;

        if (event.isLeftClick() && player.hasPermission("justplugin.rank.groups.permissions.toggle")) {
            // Toggle
            Node existing = group.getNodes().stream().filter(n -> n.getKey().equals(permKey)).findFirst().orElse(null);
            if (existing != null) {
                group.data().remove(existing);
                group.data().add(existing.toBuilder().value(!existing.getValue()).build());
                lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> openGroupPermissions(player, session)));
            }
        } else if (event.isRightClick() && player.hasPermission("justplugin.rank.groups.permissions.remove")) {
            // Remove
            group.data().clear(n -> n.getKey().equals(permKey) && n.getType() == NodeType.PERMISSION);
            lp().getGroupManager().saveGroup(group).thenRunAsync(() ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(CC.success("Removed permission <yellow>" + permKey + "</yellow>."));
                        openGroupPermissions(player, session);
                    }));
        }
    }

    // --- Player List ---
    private void handlePlayerListClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        if (slot == 49) { openMainMenu(player, session); return; }
        if (slot == 45) { session.setPage(session.getPage() - 1); openPlayerList(player, session); return; }
        if (slot == 53) { session.setPage(session.getPage() + 1); openPlayerList(player, session); return; }
        if (slot == 48) {
            requestSignInput(player, "Player name to search", input -> {
                session.setSearchFilter(input);
                openPlayerList(player, session);
            });
            return;
        }
        if (slot == 47) { session.clearSearch(); openPlayerList(player, session); return; }

        // Clicked a player head
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;
        SkullMeta meta = (SkullMeta) clicked.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;
        OfflinePlayer target = meta.getOwningPlayer();
        session.setSelectedPlayerUUID(target.getUniqueId().toString());
        session.setSelectedPlayerName(target.getName() != null ? target.getName() : "Unknown");
        openPlayerActions(player, session);
    }

    // --- Player Actions ---
    private void handlePlayerActionsClick(Player player, RankSession session, int slot) {
        if (slot == 31) { openPlayerList(player, session); return; }
        if (slot == 11 && player.hasPermission("justplugin.rank.players.addgroup")) {
            session.clearSearch();
            openGroupSelectForPlayer(player, session);
            return;
        }
        if (slot == 13 && player.hasPermission("justplugin.rank.players.listgroups")) {
            openPlayerGroupsList(player, session);
            return;
        }
        if (slot == 15 && player.hasPermission("justplugin.rank.players.removegroup")) {
            openPlayerGroupsList(player, session);
            return;
        }
        if (slot == 22 && player.hasPermission("justplugin.rank.players.permissions")) {
            session.clearSearch();
            openPlayerPermissions(player, session);
        }
    }

    // --- Player Groups ---
    private void handlePlayerGroupsClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        if (slot == 31) { openPlayerActions(player, session); return; }
        if (!player.hasPermission("justplugin.rank.players.removegroup")) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || (clicked.getType() != Material.LIME_WOOL && clicked.getType() != Material.RED_WOOL)) return;
        String groupName = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());

        // Block removal of the default group
        if (groupName.equalsIgnoreCase("default")) {
            player.sendMessage(CC.error("The <yellow>default</yellow> group cannot be removed from a player."));
            return;
        }

        UUID targetUUID = UUID.fromString(session.getSelectedPlayerUUID());

        lp().getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            user.data().clear(NodeType.INHERITANCE.predicate(n -> n.getGroupName().equalsIgnoreCase(groupName)));
            lp().getUserManager().saveUser(user).thenRunAsync(() ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(CC.success("Removed <yellow>" + session.getSelectedPlayerName() +
                                "</yellow> from group <yellow>" + groupName + "</yellow>."));
                        openPlayerGroupsList(player, session);
                    }));
        });
    }

    // --- Player Perms ---
    private void handlePlayerPermsClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        if (slot == 49) { openPlayerActions(player, session); return; }
        if (slot == 45) { session.setPage(session.getPage() - 1); openPlayerPermissions(player, session); return; }
        if (slot == 53) { session.setPage(session.getPage() + 1); openPlayerPermissions(player, session); return; }
        if (slot == 48) {
            requestSignInput(player, "Permission to search", input -> {
                session.setSearchFilter(input);
                openPlayerPermissions(player, session);
            });
            return;
        }
        if (slot == 47) { session.clearSearch(); openPlayerPermissions(player, session); return; }
        if (slot == 50 && player.hasPermission("justplugin.rank.players.permissions.add")) {
            requestSignInput(player, "Permission node to add", perm -> {
                UUID targetUUID = UUID.fromString(session.getSelectedPlayerUUID());
                lp().getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
                    user.data().add(Node.builder(perm).build());
                    lp().getUserManager().saveUser(user).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(CC.success("Added permission <yellow>" + perm + "</yellow>!"));
                                openPlayerPermissions(player, session);
                            }));
                });
            });
            return;
        }

        // Clicked a perm node
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        if (clicked.getType() != Material.LIME_DYE && clicked.getType() != Material.GRAY_DYE) return;
        String permKey = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
        UUID targetUUID = UUID.fromString(session.getSelectedPlayerUUID());

        lp().getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            if (event.isLeftClick() && player.hasPermission("justplugin.rank.players.permissions.toggle")) {
                Node existing = user.getNodes().stream().filter(n -> n.getKey().equals(permKey)).findFirst().orElse(null);
                if (existing != null) {
                    user.data().remove(existing);
                    user.data().add(existing.toBuilder().value(!existing.getValue()).build());
                    lp().getUserManager().saveUser(user).thenRunAsync(() ->
                            Bukkit.getScheduler().runTask(plugin, () -> openPlayerPermissions(player, session)));
                }
            } else if (event.isRightClick() && player.hasPermission("justplugin.rank.players.permissions.remove")) {
                user.data().clear(n -> n.getKey().equals(permKey) && n.getType() == NodeType.PERMISSION);
                lp().getUserManager().saveUser(user).thenRunAsync(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(CC.success("Removed permission <yellow>" + permKey + "</yellow>."));
                            openPlayerPermissions(player, session);
                        }));
            }
        });
    }

    // --- Select Group (for adding player to group) ---
    private void handleSelectGroupClick(Player player, RankSession session, int slot, InventoryClickEvent event) {
        if (slot == 49) { openPlayerActions(player, session); return; }
        if (slot == 45) { session.setPage(session.getPage() - 1); openGroupSelectForPlayer(player, session); return; }
        if (slot == 53) { session.setPage(session.getPage() + 1); openGroupSelectForPlayer(player, session); return; }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.EMERALD_BLOCK) return;
        String groupName = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
        UUID targetUUID = UUID.fromString(session.getSelectedPlayerUUID());

        lp().getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            user.data().add(InheritanceNode.builder(groupName.toLowerCase()).build());
            lp().getUserManager().saveUser(user).thenRunAsync(() ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(CC.success("Added <yellow>" + session.getSelectedPlayerName() +
                                "</yellow> to group <yellow>" + groupName + "</yellow>!"));
                        openPlayerActions(player, session);
                    }));
        });
    }

    // ======================== Event Cleanup ========================

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // Keep session alive for sign callbacks
        if (signCallbacks.containsKey(event.getPlayer().getUniqueId())) return;
        // Keep session for re-opening
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
        signCallbacks.remove(event.getPlayer().getUniqueId());
    }

    // ======================== Utilities ========================

    private String extractGroupName(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().lore() == null) return null;
        for (Component line : item.getItemMeta().lore()) {
            String plain = PlainTextComponentSerializer.plainText().serialize(line);
            if (plain.startsWith("Group: ")) {
                return plain.substring(7);
            }
        }
        return null;
    }

    private String getGroupMeta(Group group, String key) {
        if (key.equals("prefix")) {
            return group.getNodes(NodeType.PREFIX).stream()
                    .max(Comparator.comparingInt(n -> n.getPriority()))
                    .map(PrefixNode::getMetaValue)
                    .orElse(null);
        }
        if (key.equals("suffix")) {
            return group.getNodes(NodeType.SUFFIX).stream()
                    .max(Comparator.comparingInt(n -> n.getPriority()))
                    .map(SuffixNode::getMetaValue)
                    .orElse(null);
        }
        return null;
    }

    private int getGroupWeight(Group group) {
        var weight = group.getWeight();
        return weight.isPresent() ? weight.getAsInt() : 0;
    }

    private String formatExpiry(Instant expiry) {
        if (expiry == null) return "Permanent";
        Duration remaining = Duration.between(Instant.now(), expiry);
        if (remaining.isNegative()) return "Expired";
        long days = remaining.toDays();
        long hours = remaining.toHours() % 24;
        long minutes = remaining.toMinutes() % 60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    /**
     * Content slots: rows 1-4, columns 1-7 (avoiding edges).
     * For a 54-slot inventory (6 rows): slots 10-16, 19-25, 28-34, 37-43.
     */
    private int[] getContentSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    private void fill(Inventory inv, Material mat) {
        ItemStack pane = item(mat, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }
    }

    private ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(CC.translate(name));
            if (lore.length > 0) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(CC.translate(line));
                }
                meta.lore(loreComponents);
            }
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }
}



