package org.justme.justPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.justme.justPlugin.commands.chat.*;
import org.justme.justPlugin.commands.economy.*;
import org.justme.justPlugin.commands.home.*;
import org.justme.justPlugin.commands.info.*;
import org.justme.justPlugin.commands.inventory.*;
import org.justme.justPlugin.commands.item.*;
import org.justme.justPlugin.commands.misc.*;
import org.justme.justPlugin.commands.moderation.*;
import org.justme.justPlugin.commands.player.*;
import org.justme.justPlugin.commands.team.*;
import org.justme.justPlugin.commands.teleport.*;
import org.justme.justPlugin.commands.warp.*;
import org.justme.justPlugin.commands.world.*;
import org.justme.justPlugin.api.JustPluginAPIImpl;
import org.justme.justPlugin.api.JustPluginProvider;
import org.justme.justPlugin.listeners.PlayerListener;
import org.justme.justPlugin.listeners.VanillaCommandLogger;
import org.justme.justPlugin.gui.BaltopGui;
import org.justme.justPlugin.gui.HomeGui;
import org.justme.justPlugin.gui.RtpGui;
import org.justme.justPlugin.gui.rank.RankGuiManager;
import org.justme.justPlugin.managers.*;
import org.justme.justPlugin.util.CC;
import org.bstats.bukkit.Metrics;

import java.io.InputStream;
import java.io.InputStreamReader;

public final class JustPlugin extends JavaPlugin {

    private DataManager dataManager;
    private CommandSettings commandSettings;
    private EconomyManager economyManager;
    private WarpManager warpManager;
    private HomeManager homeManager;
    private TeleportManager teleportManager;
    private TeamManager teamManager;
    private BanManager banManager;
    private VanishManager vanishManager;
    private IgnoreManager ignoreManager;
    private ChatManager chatManager;
    private PlayerStateManager playerStateManager;
    private TradeManager tradeManager;
    private CooldownManager cooldownManager;
    private LogManager logManager;
    private MuteManager muteManager;
    private WarnManager warnManager;
    private WebhookManager webhookManager;
    private DeathInventoryManager deathInventoryManager;
    private EntityClearManager entityClearManager;
    private PlayerListener playerListener;
    private TabCommand tabCommand;
    private WebEditorManager webEditorManager;
    private MotdManager motdManager;
    private ScoreboardManager scoreboardManager;
    private HomeGui homeGui;
    private BaltopGui baltopGui;
    private RtpGui rtpGui;
    private RankGuiManager rankGuiManager;
    private org.justme.justPlugin.gui.StatsGui statsGui;
    private MaintenanceManager maintenanceManager;
    private IconManager iconManager;
    private SkinManager skinManager;
    private MessageManager messageManager;
    private boolean luckPermsAvailable = false;
    /** Startup warnings that will be shown to staff on their first join after each restart */
    private final java.util.List<String> startupWarnings = new java.util.ArrayList<>();
    private final java.util.Set<java.util.UUID> warnedStaff = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        // Save default config
        saveDefaultConfig();

        // Save default stats.yml if not present
        if (!new java.io.File(getDataFolder(), "stats.yml").exists()) {
            saveResource("stats.yml", false);
        }

        // Migrate config - add any missing keys from the default config while preserving existing values
        migrateConfig();

        // Initialize message manager (loads all configurable texts from texts/ folder)
        messageManager = new MessageManager(this);

        // Initialize managers
        dataManager = new DataManager(this);
        commandSettings = new CommandSettings(this);
        logManager = new LogManager(this);
        economyManager = new EconomyManager(this);
        warpManager = new WarpManager(this);
        homeManager = new HomeManager(this);
        teleportManager = new TeleportManager(this);
        teamManager = new TeamManager(this);
        banManager = new BanManager(this);
        vanishManager = new VanishManager(this);
        ignoreManager = new IgnoreManager(this);
        chatManager = new ChatManager(this);
        playerStateManager = new PlayerStateManager(this);
        tradeManager = new TradeManager(this);
        cooldownManager = new CooldownManager(this);
        muteManager = new MuteManager(this);
        warnManager = new WarnManager(this);
        webhookManager = new WebhookManager(this);
        deathInventoryManager = new DeathInventoryManager(this);
        entityClearManager = new EntityClearManager(this);
        entityClearManager.start();
        motdManager = new MotdManager(this);
        webEditorManager = new WebEditorManager(this);
        webEditorManager.start();

        // Try to hook into Vault if configured
        economyManager.setupVault();

        // Initialize scoreboard system (needs all managers ready)
        scoreboardManager = new ScoreboardManager(this);

        // Register listeners
        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(new VanillaCommandLogger(this), this);

        // Register categorized sub-listeners
        Bukkit.getPluginManager().registerEvents(new org.justme.justPlugin.listeners.connection.ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new org.justme.justPlugin.listeners.chat.ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new org.justme.justPlugin.listeners.combat.CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new org.justme.justPlugin.listeners.player.PlayerEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new org.justme.justPlugin.listeners.server.ServerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new org.justme.justPlugin.listeners.inventory.InventoryListener(this), this);

        // Detect LuckPerms (must happen before RankGuiManager which depends on it)
        luckPermsAvailable = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;

        // Initialize maintenance manager (after LuckPerms detection for group bypass support)
        maintenanceManager = new MaintenanceManager(this);

        // Initialize server icon manager (fetches from URL if configured)
        iconManager = new IconManager(this);

        // Initialize skin manager
        skinManager = new SkinManager(this);

        // Initialize and register GUI listeners
        homeGui = new HomeGui(this);
        baltopGui = new BaltopGui(this);
        rtpGui = new RtpGui(this);
        Bukkit.getPluginManager().registerEvents(homeGui, this);
        Bukkit.getPluginManager().registerEvents(baltopGui, this);
        Bukkit.getPluginManager().registerEvents(rtpGui, this);

        // Initialize stats GUI
        statsGui = new org.justme.justPlugin.gui.StatsGui(this);
        Bukkit.getPluginManager().registerEvents(statsGui, this);

        // Only load RankGuiManager if LuckPerms is present (it references LP classes directly)
        if (luckPermsAvailable) {
            rankGuiManager = new RankGuiManager(this);
            Bukkit.getPluginManager().registerEvents(rankGuiManager, this);
        }

        // Register commands
        registerCommands();

        // Tab list update task (configurable interval, default every 5 seconds)
        tabCommand = new TabCommand(this);
        registerCmd("tab", tabCommand);
        int tabRefresh = tabCommand.getRefreshInterval();
        if (tabRefresh > 0) {
            Bukkit.getScheduler().runTaskTimer(this, () -> tabCommand.applyTabToAll(), 20L * 5, 20L * tabRefresh);
        }

        // Start scoreboard update task
        scoreboardManager.start();

        // Initialize API for external plugins
        JustPluginProvider.set(new JustPluginAPIImpl(this));

        // Enforce dependency checks - auto-disable features with missing dependencies
        enforceDependencies();

        // Initialize bStats metrics (https://bstats.org)
        int pluginId = 30446;
        Metrics metrics = new Metrics(this, pluginId);

        long elapsed = System.currentTimeMillis() - startTime;
        printBanner(elapsed);
    }

    @Override
    public void onDisable() {
        // Clear API
        JustPluginProvider.clear();

        // Stop web editor
        if (webEditorManager != null) {
            webEditorManager.stop();
        }

        // Stop scoreboard
        if (scoreboardManager != null) {
            scoreboardManager.stop();
        }

        // Stop MOTD cycle task
        if (motdManager != null) {
            motdManager.shutdown();
        }

        // Save all player states before shutdown
        if (playerStateManager != null) {
            playerStateManager.saveAll();
        }

        var console = Bukkit.getConsoleSender();
        console.sendMessage(net.kyori.adventure.text.Component.empty());
        console.sendMessage(CC.translate("  <gray>[ <gradient:#ff6b6b:#ee5a24>JustPlugin</gradient> <gray>] <red>Plugin disabled. Goodbye!"));
        console.sendMessage(net.kyori.adventure.text.Component.empty());
    }

    @SuppressWarnings("deprecation")
    private void printBanner(long loadTimeMs) {
        var console = Bukkit.getConsoleSender();
        String version = getDescription().getVersion();
        String serverVersion = Bukkit.getVersion();
        String apiVersion = Bukkit.getBukkitVersion();
        long enabledCmds = 0;
        long disabledCmds = 0;
        for (String cmd : getDescription().getCommands().keySet()) {
            if (commandSettings.isEnabled(cmd)) enabledCmds++;
            else disabledCmds++;
        }

        console.sendMessage(net.kyori.adventure.text.Component.empty());
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa>     ██╗ ██████╗ </gradient>"));
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa>     ██║ ██╔══██╗</gradient>    <white><bold>JustPlugin</bold></white> <gray>v" + version));
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa>     ██║ ██████╔╝</gradient>    <gray>Running on <aqua>" + serverVersion));
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa>██   ██║ ██╔═══╝ </gradient>    <gray>API: <aqua>" + apiVersion));
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa>╚█████╔╝ ██║     </gradient>"));
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa> ╚════╝  ╚═╝     </gradient>     <green>✔</green> <gray>" + " " + enabledCmds + " commands enabled"));
        if (disabledCmds > 0) {
            console.sendMessage(CC.translate("                        <red>✘</red> <gray>" + " " + disabledCmds + " commands disabled"));
        }
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Economy system loaded <dark_gray>(" + economyManager.getProviderName() + ")"));
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Team system loaded"));
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Warp system loaded <dark_gray>(" + warpManager.getWarpNames().size() + " warps)"));
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Punishment system loaded <dark_gray>(bans, mutes, warns)"));
        if (commandSettings.isEnabled("maintenance")) {
            if (maintenanceManager.isActive()) {
                console.sendMessage(CC.translate("                        <red>✘</red> <gray> Maintenance mode <red>ACTIVE</red> <dark_gray>(" + maintenanceManager.getAllowedUsers().size() + " whitelisted)"));
            } else {
                console.sendMessage(CC.translate("                        <green>✔</green> <gray> Maintenance system <green>ready</green> <dark_gray>(mode: open)"));
            }
        }
        if (webhookManager.isEnabled()) {
            String url = webhookManager.getWebhookUrl();
            if (url != null && !url.isEmpty()) {
                console.sendMessage(CC.translate("                        <green>✔</green> <gray> Discord webhook logging <green>active</green>"));
            } else {
                console.sendMessage(CC.translate("                        <yellow> ○</yellow> <gray> Discord webhook logging <yellow>not set up yet</yellow>"));
                console.sendMessage(CC.translate("                          <dark_gray>Use <yellow>/setlogswebhook <url></yellow> to configure it."));
            }
        } else {
            console.sendMessage(CC.translate("                        <red>✘</red> <dark_gray> Discord webhook logging <red>disabled</red> <dark_gray>(enable in config)"));
        }
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> API ecosystem loaded"));
        if (org.justme.justPlugin.util.PAPIHook.isAvailable()) {
            console.sendMessage(CC.translate("                        <green>✔</green> <gray> PlaceholderAPI <green>hooked</green>"));
        }
        if (scoreboardManager != null && scoreboardManager.isEnabled()) {
            console.sendMessage(CC.translate("                        <green>✔</green> <gray> Scoreboard system <green>active</green>"));
        } else {
            console.sendMessage(CC.translate("                        <dark_gray>○</dark_gray> <dark_gray> Scoreboard system <gray>disabled</gray> <dark_gray>(enable in scoreboard.yml)"));
        }
        if (webEditorManager != null && webEditorManager.isRunning()) {
            console.sendMessage(CC.translate("                        <green>✔</green> <gray> Web editor <green>active</green> <dark_gray>(port " + webEditorManager.getPort() + ")"));
            console.sendMessage(CC.translate("                          <dark_gray>Auth token: <yellow>" + webEditorManager.getAuthToken() + "</yellow>"));
        } else if (getConfig().getBoolean("web-editor.enabled", false)) {
            console.sendMessage(CC.translate("                        <red>✘</red> <gray> Web editor <red>failed to start</red>"));
        } else {
            console.sendMessage(CC.translate("                        <dark_gray>○</dark_gray> <dark_gray> Web editor <gray>disabled</gray> <dark_gray>(enable in config: web-editor.enabled)"));
        }
        console.sendMessage(net.kyori.adventure.text.Component.empty());
        console.sendMessage(CC.translate("  <gradient:#00aaff:#00ffaa>Successfully enabled.</gradient> <gray>(took " + loadTimeMs + "ms)"));
        console.sendMessage(net.kyori.adventure.text.Component.empty());
    }

    private void registerCommands() {
        // Teleportation
        registerCmd("tpa", new TpaCommand(this));
        registerCmd("tpaccept", new TpAcceptCommand(this));
        registerCmd("tpacancel", new TpaCancelCommand(this));
        registerCmd("tpreject", new TpaRejectCommand(this));
        registerCmd("tppos", new TpPosCommand(this));
        registerCmd("tpr", new TprCommand(this));
        registerCmd("tpahere", new TpaHereCommand(this));
        registerCmd("back", new BackCommand(this));
        registerCmd("spawn", new SpawnCommand(this));
        registerCmd("setspawn", new SetSpawnCommand(this));
        registerCmd("tpsafecheck", new TpSafeCheckCommand(this));
        registerCmd("tpunsafeconfirm", new TpUnsafeConfirmCommand(this));

        // Warps
        registerCmd("warp", new WarpCommand(this));
        registerCmd("warps", new WarpListCommand(this));
        registerCmd("setwarp", new SetWarpCommand(this));
        registerCmd("delwarp", new DelWarpCommand(this));
        registerCmd("renamewarp", new RenameWarpCommand(this));

        // Homes
        registerCmd("home", new HomeCommand(this));
        registerCmd("sethome", new SetHomeCommand(this));
        registerCmd("delhome", new DelHomeCommand(this));

        // Economy
        registerCmd("balance", new BalanceCommand(this));
        registerCmd("pay", new PayCommand(this));
        registerCmd("paytoggle", new PayToggleCommand(this));
        registerCmd("paynote", new PayNoteCommand(this));
        registerCmd("addcash", new AddCashCommand(this));
        registerCmd("baltop", new BaltopCommand(this));
        registerCmd("baltophide", new BaltopHideCommand(this));

        // Moderation
        registerCmd("ban", new BanCommand(this));
        registerCmd("banip", new BanIpCommand(this));
        registerCmd("tempban", new TempBanCommand(this));
        registerCmd("tempbanip", new TempBanIpCommand(this));
        registerCmd("unban", new UnbanCommand(this));
        registerCmd("unbanip", new UnbanIpCommand(this));
        registerCmd("vanish", new VanishCommand(this));
        registerCmd("supervanish", new SuperVanishCommand(this));
        registerCmd("sudo", new SudoCommand(this));
        registerCmd("invsee", new InvseeCommand(this));
        registerCmd("echestsee", new EchestSeeCommand(this));
        registerCmd("mute", new MuteCommand(this));
        registerCmd("tempmute", new TempMuteCommand(this));
        registerCmd("unmute", new UnmuteCommand(this));
        registerCmd("warn", new WarnCommand(this));
        registerCmd("kick", new KickCommand(this));
        registerCmd("setlogswebhook", new SetLogsWebhookCommand(this));
        registerCmd("deathitems", new DeathItemsCommand(this));
        registerCmd("oplist", new OpListCommand(this));
        registerCmd("banlist", new BanListCommand(this));

        // Player
        registerCmd("fly", new FlyCommand(this));
        GameModeCommand gmCmd = new GameModeCommand(this);
        registerCmd("gm", gmCmd);
        registerCmd("gmc", gmCmd);
        registerCmd("gms", gmCmd);
        registerCmd("gma", gmCmd);
        registerCmd("gmsp", gmCmd);
        registerCmd("gmcheck", new GameModeCheckCommand(this));
        registerCmd("god", new GodCommand(this));
        registerCmd("speed", new SpeedCommand(this));
        SpeedCommand speedCmd = new SpeedCommand(this);
        registerCmd("flyspeed", speedCmd);
        registerCmd("walkspeed", speedCmd);
        registerCmd("hat", new HatCommand(this));
        registerCmd("exp", new ExpCommand(this));
        registerCmd("skull", new SkullCommand(this));
        registerCmd("suicide", new SuicideCommand(this));
        registerCmd("heal", new HealCommand(this));
        registerCmd("feed", new FeedCommand(this));
        registerCmd("kill", new KillCommand(this));
        registerCmd("getpos", new GetPosCommand(this));
        registerCmd("getdeathpos", new GetDeathPosCommand(this));

        // Chat
        registerCmd("msg", new MsgCommand(this));
        registerCmd("r", new ReplyCommand(this));
        registerCmd("ignore", new IgnoreCommand(this));
        registerCmd("announce", new AnnounceCommand(this));
        registerCmd("sharecoords", new ShareCoordsCommand(this));
        registerCmd("sharedeathcoords", new ShareDeathCoordsCommand(this));
        registerCmd("chat", new ChatCommand(this));
        registerCmd("teammsg", new TeamMsgCommand(this));

        // Virtual Inventories
        registerCmd("anvil", new AnvilCommand());
        registerCmd("grindstone", new GrindstoneCommand());
        registerCmd("enderchest", new EnderChestCommand());
        registerCmd("craft", new CraftCommand());
        registerCmd("stonecutter", new StonecutterCommand());
        registerCmd("loom", new LoomCommand());
        registerCmd("smithingtable", new SmithingTableCommand());
        registerCmd("enchantingtable", new EnchantingTableCommand());

        // Info
        registerCmd("jpinfo", new InfoCommand(this));
        registerCmd("jphelp", new HelpCommand(this));
        registerCmd("playerinfo", new PlayerInfoCommand(this));
        registerCmd("plist", new ListCommand(this));
        registerCmd("playerlist", new PlayerListCommand(this));
        registerCmd("playerlisthide", new PlayerListHideCommand(this));
        registerCmd("motd", new MotdCommand(this));
        registerCmd("resetmotd", new ResetMotdCommand(this));
        registerCmd("clock", new ClockCommand(this));
        registerCmd("date", new DateCommand(this));

        // Items
        registerCmd("itemname", new ItemNameCommand(this));
        registerCmd("shareitem", new ShareItemCommand());
        registerCmd("setspawner", new SetSpawnerCommand(this));

        // World
        registerCmd("weather", new WeatherCommand(this));
        registerCmd("time", new TimeCommand(this));
        registerCmd("freezegame", new FreezeGameCommand(this));
        registerCmd("unfreezegame", new UnfreezeGameCommand(this));
        registerCmd("clearentities", new ClearEntitiesCommand(this));
        registerCmd("friendlyfire", new FriendlyFireCommand(this));
        registerCmd("clearchat", new ClearChatCommand(this));

        // Teams
        registerCmd("team", new TeamCommand(this));

        // Misc
        registerCmd("trade", new TradeCommand(this));
        registerCmd("discord", new DiscordCommand(this));
        registerCmd("applyedits", new ApplyEditsCommand(this));
        registerCmd("reloadscoreboard", new ReloadScoreboardCommand(this));
        registerCmd("rank", new RankCommand(this));
        registerCmd("stats", new org.justme.justPlugin.commands.info.StatsCommand(this));
        registerCmd("maintenance", new org.justme.justPlugin.commands.moderation.MaintenanceCommand(this));

        // Skins
        registerCmd("skin", new org.justme.justPlugin.commands.player.SkinCommand(this));
        registerCmd("skinban", new org.justme.justPlugin.commands.moderation.SkinBanCommand(this));
        registerCmd("skinunban", new org.justme.justPlugin.commands.moderation.SkinUnbanCommand(this));

        // Overrides (replace vanilla commands)
        registerCmd("help", new HelpCommand(this));
        registerCmd("plugins", new PluginsCommand());
    }

    /**
     * Registers a command with automatic enabled/permission checks from config.
     * This wrapper runs BEFORE the command handler, so individual commands
     * do NOT need their own permission checks.
     */
    private void registerCmd(String name, Object executor) {
        var cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command '" + name + "' not found in plugin.yml!");
            return;
        }

        CommandExecutor original = (CommandExecutor) executor;

        // Wrap with config-based enabled check and permission check
        CommandExecutor wrapped = (sender, command, label, args) -> {
            // 1. Check if command is enabled in config
            if (!commandSettings.isEnabled(name)) {
                sender.sendMessage(messageManager.error("general.command-disabled"));
                return true;
            }
            // 2. Check permission from config
            String perm = commandSettings.getPermission(name);
            if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
                sender.sendMessage(messageManager.error("general.no-permission"));
                return true;
            }
            // 3. Delegate to actual command handler
            return original.onCommand(sender, command, label, args);
        };

        cmd.setExecutor(wrapped);
        if (executor instanceof TabCompleter tc) {
            cmd.setTabCompleter(tc);
        }
    }

    // === Config Migration ===
    /**
     * Merges any missing keys from the default config (inside the JAR) into the
     * server's existing config.yml, preserving all current values.
     * This ensures that upgrading the plugin version automatically adds new settings
     * without wiping existing configuration.
     */
    private void migrateConfig() {
        InputStream defaultStream = getResource("config.yml");
        if (defaultStream == null) return;

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
        boolean changed = false;
        int added = 0;

        for (String key : defaults.getKeys(true)) {
            // Only add leaf keys (actual values, not section headers)
            if (defaults.isConfigurationSection(key)) continue;

            if (!getConfig().contains(key, true)) {
                getConfig().set(key, defaults.get(key));
                changed = true;
                added++;
                getLogger().info("[Config Migration] Added missing key: " + key);
            }
        }

        if (changed) {
            saveConfig();
            getLogger().info("[Config Migration] Added " + added + " new config key(s). Existing settings preserved.");
        }
    }

    // === Dependency Enforcement ===
    /**
     * Checks all optional dependencies at startup and automatically disables
     * features whose requirements are not met.  Changes are persisted to config.yml
     * and recorded in {@link #startupWarnings} so staff are alerted on join.
     */
    private void enforceDependencies() {
        var console = Bukkit.getConsoleSender();
        startupWarnings.clear();
        warnedStaff.clear();

        // ── LuckPerms-dependent features ─────────────────────────────
        if (!luckPermsAvailable) {
            // /rank command
            if (commandSettings.isEnabled("rank")) {
                commandSettings.disableCommand("rank");
                String msg = "/rank was <red>auto-disabled</red> <gray>- LuckPerms is not installed.";
                console.sendMessage(CC.translate("  <red><bold>⚠</bold></red> <gray>" + msg));
                startupWarnings.add(msg);
            }
        }

        // ── Vault-dependent features ─────────────────────────────────
        String ecoProvider = getConfig().getString("economy.provider", "justplugin").toLowerCase();
        boolean vaultInstalled = Bukkit.getPluginManager().getPlugin("Vault") != null;
        if ("vault".equals(ecoProvider) && (!vaultInstalled || !economyManager.isUsingVault())) {
            // Vault was configured but is missing or has no economy provider - revert to JustPlugin economy
            getConfig().set("economy.provider", "justplugin");
            saveConfig();
            String msg = "economy.provider was set to <yellow>vault</yellow> <gray>but Vault " +
                    (vaultInstalled ? "has no economy provider" : "is not installed") +
                    ". <red>Auto-switched</red> <gray>to <green>justplugin</green> <gray>built-in economy.";
            console.sendMessage(CC.translate("  <red><bold>⚠</bold></red> <gray>" + msg));
            startupWarnings.add(msg);
        }

        // ── Soft dependency warnings (not auto-disabled, just recommendations) ──
        if (commandSettings.isEnabled("pay") && !commandSettings.isEnabled("balance")) {
            String msg = "/pay is enabled but /balance is disabled - economy features may not work properly.";
            console.sendMessage(CC.translate("  <yellow><bold>⚠</bold></yellow> <gray>" + msg));
            startupWarnings.add(msg);
        }
        if (commandSettings.isEnabled("trade") && !commandSettings.isEnabled("balance")) {
            String msg = "/trade is enabled but /balance is disabled - trade currency features won't work.";
            console.sendMessage(CC.translate("  <yellow><bold>⚠</bold></yellow> <gray>" + msg));
            startupWarnings.add(msg);
        }
        if (commandSettings.isEnabled("teammsg") && !commandSettings.isEnabled("team")) {
            String msg = "/teammsg is enabled but /team is disabled.";
            console.sendMessage(CC.translate("  <yellow><bold>⚠</bold></yellow> <gray>" + msg));
            startupWarnings.add(msg);
        }
        if (commandSettings.isEnabled("mute") && !commandSettings.isEnabled("msg")) {
            String msg = "/mute is enabled but /msg is disabled - muted players won't be blocked from DMs.";
            console.sendMessage(CC.translate("  <yellow><bold>⚠</bold></yellow> <gray>" + msg));
            startupWarnings.add(msg);
        }

        if (!startupWarnings.isEmpty()) {
            console.sendMessage(CC.translate("  <gray>Features with missing dependencies were auto-disabled. Check config.yml for details."));
        }
    }

    /**
     * Returns the list of startup warnings to display to staff on their first join.
     */
    public java.util.List<String> getStartupWarnings() {
        return startupWarnings;
    }

    /**
     * Returns the set of staff UUIDs that have already been warned this session.
     */
    public java.util.Set<java.util.UUID> getWarnedStaff() {
        return warnedStaff;
    }

    // === Getters ===
    public DataManager getDataManager() { return dataManager; }
    @SuppressWarnings("unused") // Public API for external plugins
    public CommandSettings getCommandSettings() { return commandSettings; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public HomeManager getHomeManager() { return homeManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public BanManager getBanManager() { return banManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public IgnoreManager getIgnoreManager() { return ignoreManager; }
    public ChatManager getChatManager() { return chatManager; }
    public PlayerStateManager getPlayerStateManager() { return playerStateManager; }
    public TradeManager getTradeManager() { return tradeManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public LogManager getLogManager() { return logManager; }
    public MuteManager getMuteManager() { return muteManager; }
    public WarnManager getWarnManager() { return warnManager; }
    public WebhookManager getWebhookManager() { return webhookManager; }
    public DeathInventoryManager getDeathInventoryManager() { return deathInventoryManager; }
    public EntityClearManager getEntityClearManager() { return entityClearManager; }
    public WebEditorManager getWebEditorManager() { return webEditorManager; }
    public MotdManager getMotdManager() { return motdManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public PlayerListener getPlayerListener() { return playerListener; }
    public TabCommand getTabCommand() { return tabCommand; }
    public HomeGui getHomeGui() { return homeGui; }
    public BaltopGui getBaltopGui() { return baltopGui; }
    public RtpGui getRtpGui() { return rtpGui; }
    public RankGuiManager getRankGuiManager() { return rankGuiManager; }
    public org.justme.justPlugin.gui.StatsGui getStatsGui() { return statsGui; }
    public MaintenanceManager getMaintenanceManager() { return maintenanceManager; }
    public IconManager getIconManager() { return iconManager; }
    public SkinManager getSkinManager() { return skinManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public boolean isLuckPermsAvailable() { return luckPermsAvailable; }
}
