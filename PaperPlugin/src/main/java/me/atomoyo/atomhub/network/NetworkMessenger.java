package me.atomoyo.atomhub.network;

import me.atomoyo.atomhub.AtomHub;
import org.bukkit.entity.Player;

import java.io.*;

public class NetworkMessenger {

    private final AtomHub plugin;

    public NetworkMessenger(AtomHub plugin) {
        this.plugin = plugin;

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:ban");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:kick");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:mute");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:unmute");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:transfer");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:serverinfo");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:playerlist");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "network:broadcast");
    }

    public void send(String channel, Player admin, Player target, String reason) {
        send(channel, admin, target, reason, "");
    }

    public void send(String channel, Player admin, Player target, String reason, String server) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);

            // Write data in the EXACT order expected by Velocity
            out.writeUTF(target.getUniqueId().toString()); // 1. UUID
            out.writeUTF(reason);                          // 2. Reason
            out.writeUTF(server);                          // 3. Server name (empty string for ban/kick)
            out.writeUTF(admin.getName());                 // 4. Admin name

            // Debug logging
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("[NetworkMessenger] Sending " + channel +
                        " | UUID: " + target.getUniqueId() +
                        " | Reason: " + reason +
                        " | Server: " + server +
                        " | Admin: " + admin.getName());
            }

            admin.sendPluginMessage(plugin, channel, bytes.toByteArray());

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to send plugin message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendTransfer(Player admin, Player target, String serverName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);

            // Write data for transfer
            out.writeUTF(target.getUniqueId().toString()); // Target UUID
            out.writeUTF(target.getName());                // Target name
            out.writeUTF(serverName);                      // Target server name
            out.writeUTF(admin.getUniqueId().toString());  // Admin UUID
            out.writeUTF(admin.getName());                 // Admin name

            // Debug logging
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("[NetworkMessenger] Sending transfer: " +
                        admin.getName() + " -> " + target.getName() + " to " + serverName);
            }

            admin.sendPluginMessage(plugin, "network:transfer", bytes.toByteArray());

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to send transfer plugin message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}