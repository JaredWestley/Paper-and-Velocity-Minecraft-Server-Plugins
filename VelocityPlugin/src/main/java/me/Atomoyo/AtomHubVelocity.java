package me.Atomoyo;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Plugin(
        id = "atomhubvelocity",
        name = "AtomHubVelocity",
        version = "1.0.0",
        authors = {"Atomoyo"}
)
public class AtomHubVelocity {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private Connection connection;

    private String dbHost;
    private String dbPort;
    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private boolean useSSL;

    private static final String CHANNEL_BAN = "network:ban";
    private static final String CHANNEL_KICK = "network:kick";
    private static final String CHANNEL_MUTE = "network:mute";
    private static final String CHANNEL_UNMUTE = "network:unmute";
    private static final String CHANNEL_TRANSFER = "network:transfer";
    private static final String CHANNEL_BROADCAST = "network:broadcast";
    private static final String CHANNEL_SERVER_INFO = "network:serverinfo";
    private static final String CHANNEL_PLAYER_LIST = "network:playerlist";


    private MinecraftChannelIdentifier CHANNEL_BAN_ID = MinecraftChannelIdentifier.from(CHANNEL_BAN);
    private MinecraftChannelIdentifier CHANNEL_KICK_ID = MinecraftChannelIdentifier.from(CHANNEL_KICK);
    private MinecraftChannelIdentifier CHANNEL_MUTE_ID = MinecraftChannelIdentifier.from(CHANNEL_MUTE);
    private MinecraftChannelIdentifier CHANNEL_UNMUTE_ID = MinecraftChannelIdentifier.from(CHANNEL_UNMUTE);
    private MinecraftChannelIdentifier CHANNEL_TRANSFER_ID = MinecraftChannelIdentifier.from(CHANNEL_TRANSFER);
    private MinecraftChannelIdentifier CHANNEL_BROADCAST_ID = MinecraftChannelIdentifier.from(CHANNEL_BROADCAST);
    private MinecraftChannelIdentifier CHANNEL_SERVER_INFO_ID = MinecraftChannelIdentifier.from(CHANNEL_SERVER_INFO);
    private MinecraftChannelIdentifier CHANNEL_PLAYER_LIST_ID = MinecraftChannelIdentifier.from(CHANNEL_PLAYER_LIST);


    @Inject
    public AtomHubVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    // ==========================================================
    //  INIT + MYSQL SETUP
    // ==========================================================

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        loadConfig();

        proxy.getChannelRegistrar().register(CHANNEL_BAN_ID);
        proxy.getChannelRegistrar().register(CHANNEL_KICK_ID);
        proxy.getChannelRegistrar().register(CHANNEL_MUTE_ID);
        proxy.getChannelRegistrar().register(CHANNEL_UNMUTE_ID);
        proxy.getChannelRegistrar().register(CHANNEL_TRANSFER_ID);
        proxy.getChannelRegistrar().register(CHANNEL_BROADCAST_ID);
        proxy.getChannelRegistrar().register(CHANNEL_SERVER_INFO_ID);
        proxy.getChannelRegistrar().register(CHANNEL_PLAYER_LIST_ID);

        connectToMySQL();
        createTables();

        logger.info("[AtomHubVelocity] - Plugin enabled + MySQL connected.");
    }

    // ==========================================================
    //  CONFIGURATION HANDLING
    // ==========================================================

    private void loadConfig() {
        logger.info("[AtomHubVelocity] - Starting config loading process...");
//        logger.info("Data directory: {}", dataDirectory.toAbsolutePath());

        Path configPath = dataDirectory.resolve("config.yml");
//        logger.info("Config path: {}", configPath.toAbsolutePath());

        // Create directory if it doesn't exist
        if (!Files.exists(dataDirectory)) {
            try {
//                logger.info("Creating directory: {}", dataDirectory);
                Files.createDirectories(dataDirectory);
                logger.info("[AtomHubVelocity] - Created plugin directory successfully");
            } catch (IOException e) {
                logger.error("[AtomHubVelocity] - Failed to create plugin directory!", e);
                return;
            }
        } else {
//            logger.info("[AtomHubVelocity] - Directory already exists");
        }

        // Create default config if it doesn't exist
        if (!Files.exists(configPath)) {
            logger.info("[AtomHubVelocity] - Config file doesn't exist, creating default...");
            createDefaultConfig(configPath);
        }

        // Load the config
        try {
            Yaml yaml = new Yaml();
//            logger.info("[AtomHubVelocity] - Attempting to read config file...");

            if (!Files.exists(configPath)) {
                logger.error("[AtomHubVelocity] - Config file STILL doesn't exist after creation attempt!");
                return;
            }

            InputStream inputStream = Files.newInputStream(configPath);
            Map<String, Object> config = yaml.load(inputStream);

            // Get database configuration
            if (config != null && config.containsKey("database")) {
                Map<String, Object> database = (Map<String, Object>) config.get("database");

                if (database != null) {
                    dbHost = (String) database.get("host");
                    dbPort = (String) database.get("port");
                    dbName = (String) database.get("database");
                    dbUsername = (String) database.get("username");
                    dbPassword = (String) database.get("password");
                    useSSL = (Boolean) database.getOrDefault("useSSL", false);

                    logger.info("[AtomHubVelocity] - Configuration loaded successfully!");
                } else {
                    logger.error("[AtomHubVelocity] - Database configuration is null!");
                }
            } else {
                logger.error("[AtomHubVelocity] - Database configuration not found in config.yml!");
            }

            inputStream.close();
        } catch (IOException e) {
            logger.error("[AtomHubVelocity] - Failed to load config.yml!", e);
        } catch (ClassCastException e) {
            logger.error("[AtomHubVelocity] - Config.yml has invalid structure!", e);
        } catch (Exception e) {
            logger.error("[AtomHubVelocity] - Unexpected error loading config!", e);
        }
    }

    private void createDefaultConfig(Path configPath) {
        logger.info("[AtomHubVelocity] - Creating default config file");

        // First check if we can write to the directory
        if (!Files.isWritable(dataDirectory)) {
            logger.error("[AtomHubVelocity] - Cannot write to directory: {}", dataDirectory);
            return;
        }

        try {
            String defaultConfig =
                    "# AtomHubVelocity Configuration\n" +
                            "# Database Settings\n" +
                            "database:\n" +
                            "  # MySQL host address\n" +
                            "  host: \"localhost\"\n" +
                            "  # MySQL port\n" +
                            "  port: \"3306\"\n" +
                            "  # Database name\n" +
                            "  database: \"database\"\n" +
                            "  # Database username\n" +
                            "  username: \"username\"\n" +
                            "  # Database password\n" +
                            "  password: \"password\"\n" +
                            "  # Use SSL for connection (true/false)\n" +
                            "  useSSL: false\n" +
                            "\n" +
                            "# Plugin Settings\n" +
                            "plugin:\n" +
                            "  # Log plugin messages to console\n" +
                            "  debug: false\n" +
                            "  # Message format for bans/kicks\n" +
                            "  ban-message: \"&cYou are banned from the network.\\n&7Reason: &f{reason}\"\n" +
                            "  kick-message: \"&cYou were kicked from the network.\\n&7Reason: &f{reason}\"\n" +
                            "  mute-message: \"&cYou have been muted network-wide.\\n&7Reason: &f{reason}\"";

            // Write with explicit UTF-8 encoding
            Files.writeString(configPath, defaultConfig);
            logger.info("[AtomHubVelocity] - Successfully created default configuration file");

            // Verify it was created
            if (!Files.exists(configPath)) {
                logger.error("[AtomHubVelocity] - Config file creation failed - file doesn't exist after write!");
            }

        } catch (IOException e) {
            logger.error("[AtomHubVelocity] - Failed to create default config.yml!", e);

            // Try alternative method
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                String defaultConfig =
                        "# AtomHubVelocity Configuration\n" +
                                "database:\n" +
                                "  host: \"localhost\"\n" +
                                "  port: \"3306\"\n" +
                                "  database: \"database\"\n" +
                                "  username: \"username\"\n" +
                                "  password: \"password\"\n" +
                                "  useSSL: false";

                writer.write(defaultConfig);
                logger.info("[AtomHubVelocity] - Created config using alternative method");
            } catch (IOException ex) {
                logger.error("[AtomHubVelocity] - Both config creation methods failed!", ex);
            }
        }
    }

    private void connectToMySQL() {

        logger.info("[AtomHubVelocity] - Connecting to MySQL with: {}@{}:{}/{}",
                dbUsername, dbHost, dbPort, dbName);

        try {
            Class.forName("me.atomoyo.shaded.mysql.cj.jdbc.Driver");

            String connectionString = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=true&autoReconnect=true",
                    dbHost, dbPort, dbName, useSSL
            );

            connection = DriverManager.getConnection(connectionString, dbUsername, dbPassword);
            logger.info("[AtomHubVelocity] - Connected to MySQL successfully.");
        } catch (ClassNotFoundException e) {
            logger.error("[AtomHubVelocity] - MySQL Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            logger.error("[AtomHubVelocity] - MySQL connection failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement st = connection.createStatement()) {
            // Bans table
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS atomhub_bans (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "uuid VARCHAR(36) NOT NULL," +
                            "banned_by VARCHAR(36) NOT NULL," +
                            "reason TEXT NOT NULL," +
                            "ban_time BIGINT NOT NULL" +
                            ");"
            );

            // Mutes table
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS atomhub_mutes (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "uuid VARCHAR(36) NOT NULL," +
                            "server VARCHAR(50), " +         // null = network mute, otherwise server name
                            "muted_by VARCHAR(36) NOT NULL," +
                            "reason TEXT NOT NULL," +
                            "mute_time BIGINT NOT NULL" +
                            ");"
            );

            // Alerts Menu
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS network_alerts (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "title VARCHAR(128)," +
                            "message TEXT," +
                            "server VARCHAR(64)," +
                            "level VARCHAR(32)," +
                            "timestamp BIGINT," +
                            "acknowledged BOOLEAN DEFAULT FALSE" +
                            ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    //  RECEIVE PLUGIN MESSAGE FROM PAPER
    // ==========================================================

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        MinecraftChannelIdentifier channel = (MinecraftChannelIdentifier) event.getIdentifier();

        if (!(channel.equals(CHANNEL_BAN_ID) ||
                channel.equals(CHANNEL_KICK_ID) ||
                channel.equals(CHANNEL_MUTE_ID) ||
                channel.equals(CHANNEL_UNMUTE_ID) ||
                channel.equals(CHANNEL_TRANSFER_ID) ||
                channel.equals(CHANNEL_BROADCAST_ID) ||
                channel.equals(CHANNEL_SERVER_INFO_ID) ||
                channel.equals(CHANNEL_PLAYER_LIST_ID))) {
            return;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
        DataInputStream in = new DataInputStream(bais);

        try {
            String channelId = channel.getId();

            if (channelId.equals(CHANNEL_TRANSFER)) {
                handleTransferMessage(in);
            } else if (channelId.equals(CHANNEL_SERVER_INFO)) {
                handleServerInfoRequest(in);
            } else if (channelId.equals(CHANNEL_PLAYER_LIST)) {
                handlePlayerListRequest(in);
            } else if (channelId.equals(CHANNEL_BROADCAST)) {
                handleBroadcastMessage(in);
            } else {
                // Handle existing messages (ban, kick, mute, unmute)
                String uuidString = in.readUTF();
                String reason = in.readUTF();
                String server = in.readUTF();
                String staff = in.readUTF();

                logger.info("[Network] Received " + channelId +
                        " | UUID: " + uuidString +
                        " | Reason: " + reason +
                        " | Server: " + server +
                        " | Staff: " + staff);

                if (channel.equals(CHANNEL_BAN_ID)) {
                    handleNetworkBan(uuidString, reason, staff);
                } else if (channel.equals(CHANNEL_KICK_ID)) {
                    handleNetworkKick(uuidString, reason, staff);
                } else if (channel.equals(CHANNEL_MUTE_ID)) {
                    handleNetworkMute(uuidString, server, reason, staff);
                } else if (channel.equals(CHANNEL_UNMUTE_ID)) {
                    handleNetworkUnmute(uuidString, server, staff);
                }
            }

        } catch (IOException e) {
            logger.error("Failed to read plugin message from " + channel.getId(), e);
            e.printStackTrace();
        }
    }

    // ==========================================================
    //  SERVER TRANSFER HANDLING
    // ==========================================================

    private void handleTransferMessage(DataInputStream in) throws IOException {
        String targetUUID = in.readUTF();
        String targetName = in.readUTF();
        String serverName = in.readUTF();
        String adminUUID = in.readUTF();
        String adminName = in.readUTF();

        logger.info("[Network Transfer] Request: {} ({}) -> {} by {} ({})",
                targetName, targetUUID, serverName, adminName, adminUUID);

        // Find the target player
        UUID playerUUID = UUID.fromString(targetUUID);
        Player targetPlayer = proxy.getPlayer(playerUUID).orElse(null);

        if (targetPlayer == null) {
            logger.warn("[Network Transfer] Player {} not found online", targetName);
            sendTransferResponse(adminUUID, targetName, serverName, "Player offline");
            return;
        }

        // Find the target server
        RegisteredServer targetServer = proxy.getServer(serverName).orElse(null);

        if (targetServer == null) {
            logger.warn("[Network Transfer] Server {} not found", serverName);
            sendTransferResponse(adminUUID, targetName, serverName, "Server not found");
            return;
        }

        // Check if player is already on that server
        if (targetPlayer.getCurrentServer().isPresent() &&
                targetPlayer.getCurrentServer().get().getServer().getServerInfo().getName().equals(serverName)) {
            sendTransferResponse(adminUUID, targetName, serverName, "Already on server");
            return;
        }

        // Perform the transfer
        ConnectionRequestBuilder connectionRequest = targetPlayer.createConnectionRequest(targetServer);
        connectionRequest.connect().thenAccept(result -> {
            final String[] statusHolder = new String[2]; // Array to hold mutable values

            switch (result.getStatus()) {
                case SUCCESS:
                    statusHolder[0] = "Success";
                    logger.info("[Network Transfer] Success: {} transferred to {}", targetName, serverName);
                    // Notify the player
                    targetPlayer.sendMessage(Component.text("§a[Network] §7Transferring to §e" + serverName + "§7 server..."));
                    break;
                case ALREADY_CONNECTED:
                    statusHolder[0] = "Already connected";
                    break;
                case CONNECTION_IN_PROGRESS:
                    statusHolder[0] = "Connection in progress";
                    break;
                case CONNECTION_CANCELLED:
                    statusHolder[0] = "Connection cancelled";
                    break;
                case SERVER_DISCONNECTED:
                    statusHolder[0] = "Server disconnected";
                    result.getReasonComponent().ifPresent(reason -> statusHolder[1] = reason.toString());
                    break;
                default:
                    statusHolder[0] = "Failed";
            }

            // Build the final message
            String finalMessage = statusHolder[0];
            if (statusHolder[1] != null && !statusHolder[1].isEmpty()) {
                finalMessage += ": " + statusHolder[1];
            }

            sendTransferResponse(adminUUID, targetName, serverName, finalMessage);
        }).exceptionally(throwable -> {
            logger.error("[Network Transfer] Error transferring {} to {}: {}",
                    targetName, serverName, throwable.getMessage());
            sendTransferResponse(adminUUID, targetName, serverName, "Error: " + throwable.getMessage());
            return null;
        });
    }

    private void sendTransferResponse(String adminUUID, String targetName, String serverName, String status) {
        // Send response back to the requesting server
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeUTF(adminUUID);
            out.writeUTF(targetName);
            out.writeUTF(serverName);
            out.writeUTF(status);

            // Find which server sent the request and send response back
            // Note: This requires tracking which server sent the message
            // For simplicity, we'll broadcast to all connected servers
            for (RegisteredServer server : proxy.getAllServers()) {
                server.sendPluginMessage(CHANNEL_TRANSFER_ID, baos.toByteArray());
            }
        } catch (IOException e) {
            logger.error("Failed to send transfer response", e);
        }
    }

    // ==========================================================
    //  SERVER INFORMATION REQUEST
    // ==========================================================

    private void handleServerInfoRequest(DataInputStream in) throws IOException {
        String requestId = in.readUTF();
        String requestingServer = in.readUTF();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeUTF(requestId);
            out.writeUTF("server_info_response");

            // Get server list
            java.util.List<RegisteredServer> servers = new java.util.ArrayList<>(proxy.getAllServers());
            out.writeInt(servers.size());

            for (RegisteredServer server : servers) {
                out.writeUTF(server.getServerInfo().getName());

                // Get player count for this server
                int playerCount = server.getPlayersConnected().size();
                out.writeInt(playerCount);

                // Try to get ping information
                server.ping().thenAccept(ping -> {
                    // Async - we can't use this here. We'll just use 0 for max players
                }).exceptionally(throwable -> null);

                // Use 0 for max players since we can't get it synchronously
                out.writeInt(0); // maxPlayers placeholder
                out.writeBoolean(true); // assume online
            }

            byte[] data = baos.toByteArray();

            // Send to requesting server
            proxy.getServer(requestingServer).ifPresent(server -> {
                server.sendPluginMessage(CHANNEL_SERVER_INFO_ID, data);
            });

        } catch (IOException e) {
            logger.error("Failed to send server info response", e);
        }
    }

    // ==========================================================
    //  PLAYER LIST REQUEST
    // ==========================================================

    private void handlePlayerListRequest(DataInputStream in) throws IOException {
        String requestId = in.readUTF();
        String requestingServer = in.readUTF();
        boolean includeAllServers = in.readBoolean();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeUTF(requestId);
            out.writeUTF("player_list_response");

            if (includeAllServers) {
                // Get all players across network
                out.writeInt(proxy.getPlayerCount());

                for (Player player : proxy.getAllPlayers()) {
                    out.writeUTF(player.getUsername());
                    out.writeUTF(player.getUniqueId().toString());
                    out.writeUTF(player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("unknown"));
                    out.writeLong(player.getPing());
                }
            } else {
                // Get players from specific server
                String targetServer = in.readUTF();
                proxy.getServer(targetServer).ifPresent(server -> {
                    try {
                        out.writeInt(server.getPlayersConnected().size());
                        for (Player player : server.getPlayersConnected()) {
                            out.writeUTF(player.getUsername());
                            out.writeUTF(player.getUniqueId().toString());
                            out.writeUTF(server.getServerInfo().getName());
                            out.writeLong(player.getPing());
                        }
                    } catch (IOException e) {
                        logger.error("Error writing player list", e);
                    }
                });
            }

            // Send response
            proxy.getServer(requestingServer).ifPresent(server -> {
                server.sendPluginMessage(CHANNEL_PLAYER_LIST_ID, baos.toByteArray());
            });

        } catch (IOException e) {
            logger.error("Failed to send player list response", e);
        }
    }

    // ==========================================================
    //  BROADCAST HANDLER
    // ==========================================================

    private void handleBroadcastMessage(DataInputStream in) throws IOException {
        String message = in.readUTF();
        String serverFilter = in.readUTF(); // "ALL" or specific server name
        String type = in.readUTF(); // "CHAT", "TITLE", "ACTIONBAR"

        logger.info("[Network Broadcast] {}: {} to {}", type, message, serverFilter);

        Component broadcastMessage = Component.text("§d[Network] §f" + message);

        if (serverFilter.equals("ALL")) {
            // Send to all players on all servers
            for (Player player : proxy.getAllPlayers()) {
                sendBroadcastToPlayer(player, broadcastMessage, type);
            }
        } else {
            // Send to specific server
            proxy.getServer(serverFilter).ifPresent(server -> {
                for (Player player : server.getPlayersConnected()) {
                    sendBroadcastToPlayer(player, broadcastMessage, type);
                }
            });
        }
    }

    private void sendBroadcastToPlayer(Player player, Component message, String type) {
        switch (type.toUpperCase()) {
            case "CHAT":
                player.sendMessage(message);
                break;
            case "TITLE":
                player.showTitle(Title.title(
                        Component.text("§6Network Announcement"),
                        message,
                        Title.Times.times(
                                Duration.ofMillis(500),
                                Duration.ofSeconds(3),
                                Duration.ofMillis(500)
                        )
                ));
                break;
            case "ACTIONBAR":
                player.sendActionBar(message);
                break;
            default:
                player.sendMessage(message);
        }
    }

    // ==========================================================
    //  UTILITY METHODS
    // ==========================================================

    public Map<String, RegisteredServer> getServers() {
        // Returns a map of server names to server objects
        return proxy.getAllServers().stream()
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.getServerInfo().getName(),
                        s -> s
                ));
    }

    public List<String> getOnlinePlayerNames() {
        return proxy.getAllPlayers().stream()
                .map(Player::getUsername)
                .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
    }

    public List<String> getServerNames() {
        return proxy.getAllServers().stream()
                .map(s -> s.getServerInfo().getName())
                .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
    }

    // ==========================================================
    //  BAN HANDLER
    // ==========================================================

    private void handleNetworkBan(String uuidStr, String reason, String staff) {
        UUID uuid = UUID.fromString(uuidStr);
        saveNetworkBan(uuidStr, reason, staff);

        proxy.getPlayer(uuid).ifPresent(player ->
                player.disconnect(Component.text("§cYou are banned from the network.\n§7Reason: §f" + reason))
        );

        System.out.println("[AtomHubVelocity] Network-wide ban: " + uuidStr + " Reason: " + reason);
    }

    private void saveNetworkBan(String uuid, String reason, String staff) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO atomhub_bans (uuid, banned_by, reason, ban_time) VALUES (?, ?, ?, ?)"
        )) {
            ps.setString(1, uuid);
            ps.setString(2, staff);
            ps.setString(3, reason);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isBanned(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM atomhub_bans WHERE uuid = ? LIMIT 1"
        )) {
            ps.setString(1, uuid);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==========================================================
    //  KICK HANDLER
    // ==========================================================

    private void handleNetworkKick(String uuidStr, String reason, String staff) {
        UUID uuid = UUID.fromString(uuidStr);

        proxy.getPlayer(uuid).ifPresent(player ->
                player.disconnect(Component.text("§cYou were kicked from the network.\n§7Reason: §f" + reason))
        );

        System.out.println("[AtomHubVelocity] Network-wide kick: " + uuidStr + " Reason: " + reason);
    }

    // ==========================================================
    //  MUTE HANDLER
    // ==========================================================

    public void handleNetworkMute(String playerUuid, @Nullable String server, String adminUuid, String reason) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO atomhub_mutes (uuid, server, muted_by, reason, mute_time) VALUES (?, ?, ?, ?, ?)"
        )) {
            ps.setString(1, playerUuid.toString());
            if (server != null) ps.setString(2, server);
            else ps.setNull(2, Types.VARCHAR); // network mute
            ps.setString(3, adminUuid.toString());
            ps.setString(4, reason);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ==========================================================
    //  UNMUTE HANDLER
    // ==========================================================
    private void handleNetworkUnmute(String uuidStr, String server, String staffUuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM atomhub_mutes WHERE uuid = ? AND (server IS NULL OR server = ?)"
        )) {
            ps.setString(1, uuidStr);
            if (server != null && !server.equals("")) {
                ps.setString(2, server);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    //  BLOCK BANNED PLAYERS ON LOGIN
    // ==========================================================

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (isBanned(uuid.toString())) {
            event.getPlayer().disconnect(Component.text(
                    "§cYou are banned from the network."
            ));
        }
    }

    // ==========================================================
    //  SHUTDOWN
    // ==========================================================

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}
