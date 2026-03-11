package org.justme.justPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.justme.justPlugin.commands.*;
import org.justme.justPlugin.listeners.*;
import org.justme.justPlugin.managers.*;

public final class JustPlugin extends JavaPlugin {

    private TpaManager tpaManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private EconomyManager economyManager;
    private TeamManager teamManager;
    private BackManager backManager;
    private VanishManager vanishManager;
    private TemporaryBanManager temporaryBanManager;
    private IgnoreManager ignoreManager;
    private GodManager godManager;
    private FlyToggleManager flyToggleManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Init managers
        tpaManager = new TpaManager(this);
        homeManager = new HomeManager(this);
        warpManager = new WarpManager(this);
        economyManager = new EconomyManager(this);
        teamManager = new TeamManager();
        backManager = new BackManager();
        vanishManager = new VanishManager(this);
        temporaryBanManager = new TemporaryBanManager(this);
        ignoreManager = new IgnoreManager();
        godManager = new GodManager();
        flyToggleManager = new FlyToggleManager();

        // Register teleportation commands
        registerCommand("tpa", new TpaCommand(this));
        registerCommand("tpaccept", new TpacceptCommand(this));
        registerCommand("tpacancel", new TpacancelCommand(this));
        registerCommand("tpreject", new TprejectCommand(this));
        registerCommand("tpahere", new TpahereCommand(this));
        registerCommand("tppos", new TpposCommand(this));
        registerCommand("tpr", new TprCommand(this));
        registerCommand("back", new BackCommand(this));
        registerCommand("spawn", new SpawnCommand(this));
        registerCommand("setspawn", new SetspawnCommand());

        // Home commands
        registerCommand("home", new HomeCommand(this));
        registerCommand("sethome", new SethomeCommand(this));
        registerCommand("delhome", new DelhomeCommand(this));

        // Warp commands
        registerCommand("warp", new WarpCommand(this));
        registerCommand("setwarp", new SetwarpCommand(this));
        registerCommand("delwarp", new DelwarpCommand(this));
        registerCommand("renamewarp", new RenamewarpCommand(this));

        // Economy commands
        registerCommand("balance", new BalanceCommand(this));
        registerCommand("pay", new PayCommand(this));
        registerCommand("paytoggle", new PaytoggleCommand(this));
        registerCommand("paynote", new PaynoteCommand(this));

        // Chat/Communication commands
        registerCommand("msg", new MsgCommand(this));
        registerCommand("reply", new ReplyCommand(this));
        registerCommand("ignore", new IgnoreCommand(this));
        registerCommand("chat", new ChatCommand(this));
        registerCommand("team", new TeamCommand(this));
        registerCommand("sharecoords", new SharecoordsCommand(this));
        registerCommand("shareitem", new ShareitemCommand(this));
        registerCommand("announce", new AnnounceCommand());
        registerCommand("discord", new DiscordCommand(this));

        // Moderation commands
        registerCommand("ban", new BanCommand());
        registerCommand("unban", new UnbanCommand());
        registerCommand("banip", new BanipCommand());
        registerCommand("unbanip", new UnbanipCommand());
        registerCommand("tempban", new TempbanCommand(this));
        registerCommand("tempbanip", new TempbanipCommand(this));
        registerCommand("vanish", new VanishCommand(this));
        registerCommand("sudo", new SudoCommand());
        registerCommand("invsee", new InvseeCommand());
        registerCommand("echestsee", new EchestseeCommand());

        // Virtual block commands
        registerCommand("anvil", new AnvilCommand());
        registerCommand("grindstone", new GrindstoneCommand());
        registerCommand("enderchest", new EnderchestCommand());
        registerCommand("craft", new CraftCommand());
        registerCommand("stonecutter", new StonecutterCommand());
        registerCommand("loom", new LoomCommand());
        registerCommand("smithingtable", new SmithingtableCommand());
        registerCommand("enchantingtable", new EnchantingtableCommand());

        // Utility commands
        registerCommand("trade", new TradeCommand(this));
        registerCommand("exp", new ExpCommand());
        registerCommand("fly", new FlyCommand(this));
        registerCommand("gm", new GmCommand());
        registerCommand("god", new GodCommand(this));
        registerCommand("getpos", new GetposCommand());
        registerCommand("hat", new HatCommand());
        registerCommand("skull", new SkullCommand());
        registerCommand("speed", new SpeedCommand());
        registerCommand("weather", new WeatherCommand());
        registerCommand("time", new TimeCommand());
        registerCommand("itemname", new ItemnameCommand());
        registerCommand("setspawner", new SetspawnerCommand());
        registerCommand("suicide", new SuicideCommand());
        registerCommand("clock", new ClockCommand());
        registerCommand("date", new DateCommand());

        // Info commands
        registerCommand("info", new InfoCommand(this));
        registerCommand("jphelp", new HelpCommand(this));
        registerCommand("playerinfo", new PlayerinfoCommand());
        registerCommand("list", new ListCommand());
        registerCommand("motd", new MotdCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new TempBanListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new GodModeListener(this), this);

        // Schedule temp ban cleanup every 5 minutes
        getServer().getScheduler().runTaskTimer(this, () -> temporaryBanManager.checkExpired(), 6000L, 6000L);

        getLogger().info("JustPlugin enabled! " + getDescription().getCommands().size() + " commands loaded.");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) homeManager.save();
        if (warpManager != null) warpManager.save();
        if (economyManager != null) economyManager.save();
        if (temporaryBanManager != null) temporaryBanManager.save();
        getLogger().info("JustPlugin disabled.");
    }

    private void registerCommand(String name, Object handler) {
        org.bukkit.command.PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command not found in plugin.yml: " + name);
            return;
        }
        if (handler instanceof org.bukkit.command.CommandExecutor executor) {
            cmd.setExecutor(executor);
        }
        if (handler instanceof org.bukkit.command.TabCompleter completer) {
            cmd.setTabCompleter(completer);
        }
    }

    public TpaManager getTpaManager() { return tpaManager; }
    public HomeManager getHomeManager() { return homeManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public BackManager getBackManager() { return backManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public TemporaryBanManager getTemporaryBanManager() { return temporaryBanManager; }
    public IgnoreManager getIgnoreManager() { return ignoreManager; }
    public GodManager getGodManager() { return godManager; }
    public FlyToggleManager getFlyToggleManager() { return flyToggleManager; }
}
