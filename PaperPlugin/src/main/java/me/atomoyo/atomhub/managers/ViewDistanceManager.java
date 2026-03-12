// File: me/atomoyo/atomhub/managers/ViewDistanceManager.java
package me.atomoyo.atomhub.managers;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.server.ViewDistanceMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewDistanceManager {

    private final AtomHub plugin;
    private final Map<UUID, String> waitingForInput = new HashMap<>();

    public ViewDistanceManager(AtomHub plugin) {
        this.plugin = plugin;
    }

    public void setWaitingForInput(Player player, String type) {
        waitingForInput.put(player.getUniqueId(), type);
    }

    public boolean handleChatInput(Player player, String message) {
        if (!waitingForInput.containsKey(player.getUniqueId())) {
            return false;
        }

        String type = waitingForInput.remove(player.getUniqueId());

        try {
            int distance = Integer.parseInt(message.trim());

            if (distance < 2 || distance > 32) {
                player.sendMessage(Component.text("§cView distance must be between 2 and 32"));
                return true;
            }

            if ("all-worlds".equals(type)) {
                // Set all worlds view distance
                ViewDistanceMenu.setAllWorldsViewDistance(distance);
                player.sendMessage(Component.text(
                        "§6[AtomHub] §aAll worlds view distance set to §f" + distance
                ));

                // Log the change
                plugin.getLogger().info("All worlds view distance set to " + distance +
                        " by " + player.getName());
            }
            else if (type.startsWith("world-")) {
                // Set specific world view distance
                String worldName = type.replace("world-", "");
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    ViewDistanceMenu.setWorldViewDistance(world, distance);
                    player.sendMessage(Component.text(
                            "§6[AtomHub] §aWorld §f" + worldName +
                                    "§a view distance set to §f" + distance
                    ));
                }
            }

            return true;

        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("§cPlease enter a valid number (2-32)"));
            return true;
        }
    }
}