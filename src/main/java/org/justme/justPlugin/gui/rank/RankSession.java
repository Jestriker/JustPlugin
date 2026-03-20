package org.justme.justPlugin.gui.rank;

import java.util.UUID;

/**
 * Tracks a player's navigation state inside the /rank GUI system.
 */
public class RankSession {

    public enum Screen {
        MAIN_MENU,
        GROUP_LIST,
        GROUP_ACTIONS,
        GROUP_PERMISSIONS,
        PLAYER_LIST,
        PLAYER_ACTIONS,
        PLAYER_GROUPS,
        PLAYER_PERMISSIONS,
        SELECT_GROUP          // Picking a group to add a player to
    }

    private Screen screen = Screen.MAIN_MENU;
    private int page = 0;
    private String searchFilter = null;

    // Currently selected group name (for group actions / perms)
    private String selectedGroup = null;
    // Currently selected player UUID string (for player actions / perms)
    private String selectedPlayerUUID = null;
    private String selectedPlayerName = null;

    // For permission node actions
    private String selectedPermNode = null;

    public Screen getScreen() { return screen; }
    public void setScreen(Screen screen) { this.screen = screen; this.page = 0; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); }
    public String getSearchFilter() { return searchFilter; }
    public void setSearchFilter(String filter) { this.searchFilter = filter; this.page = 0; }
    public void clearSearch() { this.searchFilter = null; this.page = 0; }

    public String getSelectedGroup() { return selectedGroup; }
    public void setSelectedGroup(String group) { this.selectedGroup = group; }

    public String getSelectedPlayerUUID() { return selectedPlayerUUID; }
    public void setSelectedPlayerUUID(String uuid) { this.selectedPlayerUUID = uuid; }
    public String getSelectedPlayerName() { return selectedPlayerName; }
    public void setSelectedPlayerName(String name) { this.selectedPlayerName = name; }

    public String getSelectedPermNode() { return selectedPermNode; }
    public void setSelectedPermNode(String node) { this.selectedPermNode = node; }
}

