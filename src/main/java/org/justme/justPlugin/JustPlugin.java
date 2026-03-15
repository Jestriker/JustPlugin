package org.justme.justPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
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
import org.justme.justPlugin.managers.*;
import org.justme.justPlugin.util.CC;

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

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        // Save default config
        saveDefaultConfig();

        // Migrate config - add any missing keys from the default config while preserving existing values
        migrateConfig();

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
        webEditorManager = new WebEditorManager(this);
        webEditorManager.start();

        // Register listeners
        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(new VanillaCommandLogger(this), this);

        // Register commands
        registerCommands();

        // Tab list update task (every 30 seconds)
        tabCommand = new TabCommand(this);
        registerCmd("tab", tabCommand);
        Bukkit.getScheduler().runTaskTimer(this, () -> tabCommand.applyTabToAll(), 20L * 5, 20L * 30);

        // Initialize API for external plugins
        JustPluginProvider.set(new JustPluginAPIImpl(this));

        // Check dependency warnings
        checkDependencies();

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
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Economy system loaded"));
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Team system loaded"));
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Warp system loaded <dark_gray>(" + warpManager.getWarpNames().size() + " warps)"));
        console.sendMessage(CC.translate("                        <green>✔</green> <gray> Punishment system loaded <dark_gray>(bans, mutes, warns)"));
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
        if (webEditorManager != null && webEditorManager.isRunning()) {
            console.sendMessage(CC.translate("                        <green>✔</green> <gray> Web editor <green>active</green> <dark_gray>(port " + webEditorManager.getPort() + ")"));
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
                sender.sendMessage(CC.error("This command is currently disabled."));
                return true;
            }
            // 2. Check permission from config
            String perm = commandSettings.getPermission(name);
            if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
                sender.sendMessage(CC.error("You don't have permission to use this command."));
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

    // === Dependency Warnings ===
    private void checkDependencies() {
        var console = Bukkit.getConsoleSender();
        boolean anyWarning = false;

        // Pay requires balance
        if (commandSettings.isEnabled("pay") && !commandSettings.isEnabled("balance")) {
            console.sendMessage(CC.translate("  <red><bold>⚠ WARNING:</bold></red> <yellow>/pay</yellow> <gray>is enabled but <yellow>/balance</yellow> is disabled! Economy features may not work properly."));
            anyWarning = true;
        }
        // Trade requires economy
        if (commandSettings.isEnabled("trade") && !commandSettings.isEnabled("balance")) {
            console.sendMessage(CC.translate("  <red><bold>⚠ WARNING:</bold></red> <yellow>/trade</yellow> <gray>is enabled but <yellow>/balance</yellow> is disabled! Trade currency features won't work."));
            anyWarning = true;
        }
        // TeamMsg requires team
        if (commandSettings.isEnabled("teammsg") && !commandSettings.isEnabled("team")) {
            console.sendMessage(CC.translate("  <red><bold>⚠ WARNING:</bold></red> <yellow>/teammsg</yellow> <gray>is enabled but <yellow>/team</yellow> is disabled!"));
            anyWarning = true;
        }
        // Mute blocking /msg requires msg to be enabled
        if (commandSettings.isEnabled("mute") && !commandSettings.isEnabled("msg")) {
            console.sendMessage(CC.translate("  <red><bold>⚠ WARNING:</bold></red> <yellow>/mute</yellow> <gray>is enabled but <yellow>/msg</yellow> is disabled. Muted players won't be blocked from DMs."));
            anyWarning = true;
        }
        if (anyWarning) {
            console.sendMessage(CC.translate("  <gray>These are recommendations only — the plugin will still work."));
        }
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
    public PlayerListener getPlayerListener() { return playerListener; }
}
