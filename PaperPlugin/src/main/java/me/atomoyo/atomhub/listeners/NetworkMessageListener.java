package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.Messenger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class NetworkMessageListener implements Listener {

    private final AtomHub plugin;

    public NetworkMessageListener(AtomHub plugin) {
        this.plugin = plugin;
    }

    public void registerChannels() {
        Messenger messenger = plugin.getServer().getMessenger();
        messenger.registerIncomingPluginChannel(plugin, "network:transfer", (channel, player, message) -> {
            handleTransferResponse(message, player);
        });
        messenger.registerIncomingPluginChannel(plugin, "network:serverinfo", (channel, player, message) -> {
            handleServerInfoResponse(message, player);
        });
        messenger.registerIncomingPluginChannel(plugin, "network:playerlist", (channel, player, message) -> {
            handlePlayerListResponse(message, player);
        });
        messenger.registerIncomingPluginChannel(plugin, "network:broadcast", (channel, player, message) -> {
            handleBroadcastMessage(message, player);
        });
    }

    private void handleTransferResponse(byte[] message, Player player) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(message);
             DataInputStream in = new DataInputStream(bais)) {

            String adminUUID = in.readUTF();
            String targetName = in.readUTF();
            String serverName = in.readUTF();
            String status = in.readUTF();

            if (!player.getUniqueId().toString().equals(adminUUID)) {
                return;
            }

            if (status.equals("Success")) {
                player.sendMessage(Component.text("§a[Network] §7Successfully transferred §f" + targetName + "§7 to §e" + serverName));
            } else {
                player.sendMessage(Component.text("§c[Network] §7Transfer failed: " + status));
            }

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read transfer response: " + e.getMessage());
        }
    }

    private void handleServerInfoResponse(byte[] message, Player player) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(message);
             DataInputStream in = new DataInputStream(bais)) {

            String requestId = in.readUTF();
            String responseType = in.readUTF();

            if (!responseType.equals("server_info_response")) {
                return;
            }

            int serverCount = in.readInt();

            player.sendMessage(Component.text("§6§lServer Network Status"));
            player.sendMessage(Component.text("§7────────────────────"));

            for (int i = 0; i < serverCount; i++) {
                String serverName = in.readUTF();
                int playerCount = in.readInt();
                int maxPlayers = in.readInt();
                boolean online = in.readBoolean();

                String status = online ? "§aOnline" : "§cOffline";
                player.sendMessage(Component.text("§e" + serverName + ": §f" + playerCount + "/" + maxPlayers + " §7(" + status + ")"));
            }

            player.sendMessage(Component.text("§7────────────────────"));

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read server info response: " + e.getMessage());
        }
    }

    private void handlePlayerListResponse(byte[] message, Player player) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(message);
             DataInputStream in = new DataInputStream(bais)) {

            String requestId = in.readUTF();
            String responseType = in.readUTF();

            if (!responseType.equals("player_list_response")) {
                return;
            }

            int playerCount = in.readInt();

            player.sendMessage(Component.text("§6§lNetwork Players (" + playerCount + ")"));
            player.sendMessage(Component.text("§7────────────────────"));

            for (int i = 0; i < playerCount; i++) {
                String playerName = in.readUTF();
                String uuid = in.readUTF();
                String server = in.readUTF();
                long ping = in.readLong();

                String pingColor = ping < 50 ? "§a" : (ping < 100 ? "§e" : "§c");
                player.sendMessage(Component.text("§f" + playerName + " §7- §e" + server + " §7[" + pingColor + ping + "ms§7]"));
            }

            player.sendMessage(Component.text("§7────────────────────"));

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read player list response: " + e.getMessage());
        }
    }

    private void handleBroadcastMessage(byte[] message, Player player) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(message);
             DataInputStream in = new DataInputStream(bais)) {

            String messageText = in.readUTF();
            String type = in.readUTF();

            Component broadcastComponent = Component.text("§d[Network] §f" + messageText);

            switch (type.toUpperCase()) {
                case "TITLE" -> player.showTitle(net.kyori.adventure.title.Title.title(
                        Component.text("§6Network Announcement"),
                        broadcastComponent,
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(500),
                                java.time.Duration.ofSeconds(3),
                                java.time.Duration.ofMillis(500)
                        )
                ));
                case "ACTIONBAR" -> player.sendActionBar(broadcastComponent);
                default -> player.sendMessage(broadcastComponent);
            }

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read broadcast message: " + e.getMessage());
        }
    }
}
