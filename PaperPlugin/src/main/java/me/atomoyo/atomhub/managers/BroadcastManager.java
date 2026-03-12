// File: me/atomoyo/atomhub/managers/BroadcastManager.java
package me.atomoyo.atomhub.managers;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BroadcastManager {

    private final AtomHub plugin;
    private final Map<UUID, BroadcastSession> activeSessions = new HashMap<>();

    public BroadcastManager(AtomHub plugin) {
        this.plugin = plugin;
    }

    public void setBroadcastType(Player admin, String type) {
        activeSessions.put(admin.getUniqueId(), new BroadcastSession(type));
        admin.sendMessage(Component.text("§6[AtomHub] §7Type your broadcast message in chat:"));
        admin.sendMessage(Component.text("§7Type 'cancel' to cancel"));
    }

    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void handleChatInput(Player player, String message) {
        if (!hasActiveSession(player)) return;

        if (message.equalsIgnoreCase("cancel")) {
            activeSessions.remove(player.getUniqueId());
            player.sendMessage(Component.text("§6[AtomHub] §cBroadcast cancelled"));
            return;
        }

        BroadcastSession session = activeSessions.get(player.getUniqueId());

        // Send the broadcast
        Component broadcastMessage = formatBroadcast(session.getType(), message);
        Bukkit.broadcast(broadcastMessage);

        // Log and notify
        plugin.getLogger().info("Network broadcast by " + player.getName() + " (" + session.getType() + "): " + message);
        player.sendMessage(Component.text("§6[AtomHub] §aBroadcast sent successfully!"));

        // Clear session
        activeSessions.remove(player.getUniqueId());
    }

    private Component formatBroadcast(String type, String message) {
        switch (type.toLowerCase()) {
            case "announcement":
                return Component.text("§d[Network Announcement] §f" + message);
            case "warning":
                return Component.text("§c⚠ [Network Warning] §f" + message);
            case "information":
                return Component.text("§aℹ [Network Info] §f" + message);
            case "event":
                return Component.text("§6🎉 [Network Event] §f" + message);
            case "emergency":
                return Component.text("§4🚨 [NETWORK EMERGENCY] §f" + message);
            default:
                return Component.text("§7[Network] §f" + message);
        }
    }

    private static class BroadcastSession {
        private final String type;

        public BroadcastSession(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}