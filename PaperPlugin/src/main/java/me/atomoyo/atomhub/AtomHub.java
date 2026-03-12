package me.atomoyo.atomhub;

import me.atomoyo.atomhub.arena.ArenaManager;
import me.atomoyo.atomhub.commands.AdminPanelCommand;
import me.atomoyo.atomhub.commands.ArenaCommand;
import me.atomoyo.atomhub.gui.GUIManager;
import me.atomoyo.atomhub.gui.world.WorldEffectsMenu;
import me.atomoyo.atomhub.listeners.*;
import me.atomoyo.atomhub.managers.*;
import me.atomoyo.atomhub.network.NetworkMessenger;
import me.atomoyo.atomhub.util.PerformanceTracker;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

public class AtomHub extends JavaPlugin {

    private InventoryClickListener inventoryClickListener;
    private FreezeManager freezeManager;
    private PlayerPageManager pageManager;
    private GUIManager guiManager;
    private NetworkMessenger messenger;
    private BroadcastManager broadcastManager;
    private ViewDistanceManager viewDistanceManager;
    private ArenaManager  arenaManager;
    private Connection connection;
    private MuteManager muteManager;
    private PerformanceTracker performanceTracker;
    public final GodManager godManager = new GodManager();
    public final GlowManager glowManager = new GlowManager();

    private BukkitTask freezeParticleTask;

    private boolean maintenanceMode = false;

    // Configuration fields
    private String dbHost;
    private String dbPort;
    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private boolean useSSL;
    private boolean debugMode;
    private String serverName;

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        loadConfig();

        try {
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?useSSL=%s",
                            dbHost, dbPort, dbName, useSSL),
                    dbUsername,
                    dbPassword
            );
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to MySQL database!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.inventoryClickListener = new InventoryClickListener(this);
//        getServer().getPluginManager().registerEvents(inventoryClickListener, this);

        // Managers
        this.freezeManager = new FreezeManager();
        this.pageManager = new PlayerPageManager();

        // Messaging (registers outgoing channels)
        this.messenger = new NetworkMessenger(this);

        // GUI manager
        this.guiManager = new GUIManager(this);

        this.performanceTracker = new PerformanceTracker(this);
        this.viewDistanceManager = new ViewDistanceManager(this);
        this.broadcastManager = new BroadcastManager(this);
        this.arenaManager = new ArenaManager(this);

        registerCommands();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(freezeManager), this);

        muteManager = new MuteManager(connection, getServer().getName());
        getServer().getPluginManager().registerEvents(new MuteListener(muteManager), this);

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        Bukkit.getPluginManager().registerEvents(new WorldEffectsListener(), this);

        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

        // Network message listener for handling responses from Velocity
        NetworkMessageListener networkMessageListener = new NetworkMessageListener(this);
        networkMessageListener.registerChannels();

        // Particle task for frozen players (run async-safe on main thread)
        this.freezeParticleTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            Set<Player> frozen = freezeManager.getFrozenPlayers();
            for (Player p : frozen) {
                if (p != null && p.isOnline()) {
                    p.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            p.getLocation().add(0, 1, 0),
                            10, 0.5, 0.5, 0.5, 0.05
                    );
                }
            }
        }, 0L, 10L);

        if (debugMode) {
            getLogger().info("Database configured: " + dbHost + ":" + dbPort + "/" + dbName);
            getLogger().info("Server name: " + serverName);
        }

        getLogger().info("AtomHub enabled.");
    }

    public void reloadConfiguration() {
        reloadConfig();
        loadConfig();
        getLogger().info("Configuration reloaded!");
    }

    private void registerCommands() {
        // Commands
        getCommand("atomhub").setExecutor(new AdminPanelCommand(this));
        getCommand("atomhub").setTabCompleter((sender, cmd, label, args) -> {
            if (args.length == 1) {
                return java.util.Arrays.asList(
                        "admin",
                        "server",
                        "network",
                        "world",
                        "performance",
                        "viewdistance",
                        "whitelist",
                        "reload",
                        "stopserver",
                        "restartserver",
                        "infoserver",
                        "maintenancemode",
                        "whitelistcmd",
                        "clearmemory",
                        "cleanentities",
                        "emergencyclean",
                        "killmobs",
                        "setviewdistance",
                        "viewdistanceinfo",
                        "help"
                );
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
                return null;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("player")) {
                return null; // default player name suggestions
            } else if (args.length == 3 && args[0].equalsIgnoreCase("player")) {
                return java.util.Arrays.asList("ban", "kick", "mute", "teleport", "heal");
            } else if (args.length == 4 && args[0].equalsIgnoreCase("player") && args[3].equalsIgnoreCase("gamemode")) {
                return java.util.Arrays.asList("ban", "kick", "mute", "teleport", "heal");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("stopserver")) {
                return java.util.Arrays.asList("confirm", "cancel", "<30s>", "<5m>", "<2h>", "<1d>", "<1d5m30s>", "<32m45s>");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("restartserver")) {
                return java.util.Arrays.asList("confirm", "cancel", "<30s>", "<5m>", "<2h>", "<1d>", "<1d5m30s>", "<32m45s>");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("maintenancemode")) {
                return java.util.Arrays.asList("on", "off");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("whitelistcmd")) {
                return java.util.Arrays.asList("add", "remove", "list", "on", "off");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("setviewdistance")) {
                return Collections.singletonList("<2-32>");
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("setviewdistance")) {
                return Collections.singletonList("[world]");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("worldcmd")) {
                return java.util.Arrays.asList("time", "weather");
            } else if (args.length == 3 && args[0].equalsIgnoreCase("worldcmd") && args[1].equalsIgnoreCase("time")) {
                return java.util.Arrays.asList("day", "night");
            } else if  (args.length == 3 && args[0].equalsIgnoreCase("worldcmd") && args[1].equalsIgnoreCase("weather")) {
                return java.util.Arrays.asList("clear", "rain", "storm");
            }

            return java.util.Collections.emptyList();
        });

        // Register /arena command
        PluginCommand arenaCmd = getCommand("arena");
        if (arenaCmd != null) {
            ArenaCommand arenaCommand = new ArenaCommand(this);
            arenaCmd.setExecutor(arenaCommand);
            arenaCmd.setTabCompleter(arenaCommand);
        } else {
            getLogger().warning("Failed to register /arena command!");
        }
    }

    private void loadConfig() {
        // Database settings
        dbHost = getConfig().getString("database.host", "localhost");
        dbPort = getConfig().getString("database.port", "3306");
        dbName = getConfig().getString("database.database", "minecraft_network");
        dbUsername = getConfig().getString("database.username", "mcuser");
        dbPassword = getConfig().getString("database.password", "password");
        useSSL = getConfig().getBoolean("database.useSSL", false);

        // Plugin settings
        debugMode = getConfig().getBoolean("plugin.debug", false);
        serverName = getConfig().getString("plugin.server-name", getServer().getName());

        // Maintenance mode
        maintenanceMode = getConfig().getBoolean("maintenance.enabled", false);
    }

    @Override
    public void onDisable() {
        // Cancel repeating task
        if (freezeParticleTask != null) freezeParticleTask.cancel();

        // Unregister outgoing/incoming plugin channels that NetworkMessenger registered.
        // NetworkMessenger registers channels in its constructor, but it's safe to attempt unregistering here.
        try {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:ban");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:kick");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:mute");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:unmute");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:transfer");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:serverinfo");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:playerlist");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "network:broadcast");

            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:ban");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:kick");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:mute");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:unmute");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:transfer");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:serverinfo");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:playerlist");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "network:broadcast");
        } catch (Exception ignored) {}

        for (World world : getServer().getWorlds()) {
            WorldEffectsMenu.cleanupWorldEffects(world);
        }

        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}

        getLogger().info("AtomHub disabled.");
    }

    // ----------------------------
    // Accessors for other classes
    // ----------------------------
    public InventoryClickListener getInventoryClickListener() {
        return inventoryClickListener;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public PlayerPageManager getPageManager() {
        return pageManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public NetworkMessenger getMessenger() {
        return messenger;
    }

    public GodManager getGodManager() {
        return godManager;
    }

    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    public GlowManager getGlowManager() {
        return glowManager;
    }

    public PerformanceTracker getPerformanceTracker(AtomHub atomhub) {
        return performanceTracker;
    }
    public ViewDistanceManager getViewDistanceManager() {
        return viewDistanceManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
