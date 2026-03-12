// File: me/atomoyo/atomhub/listeners/ChatListener.java
package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final AtomHub plugin;

    public ChatListener(AtomHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if player has an active broadcast session
        if (plugin.getBroadcastManager().hasActiveSession(player)) {
            event.setCancelled(true);

            // Handle on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getBroadcastManager().handleChatInput(player, event.getMessage());
            });
        }

        // Check if player has a pending transfer
        if (plugin.getInventoryClickListener().hasPendingTransfer(player.getUniqueId())) {
            event.setCancelled(true);

            String input = event.getMessage().trim();

            // Run on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                handleTransferChatInput(player, input);
            });
        }
    }

    private void handleTransferChatInput(Player admin, String input) {
        if (input.equalsIgnoreCase("cancel")) {
            admin.sendMessage(Component.text("§cTransfer cancelled."));
            plugin.getInventoryClickListener().clearPendingTransfer(admin.getUniqueId());
            return;
        }

        Player target = plugin.getInventoryClickListener().getPendingTransfer(admin.getUniqueId());
        if (target == null || !target.isOnline()) {
            admin.sendMessage(Component.text("§cTarget player is no longer online!"));
            plugin.getInventoryClickListener().clearPendingTransfer(admin.getUniqueId());
            return;
        }

        // Transfer to custom server
        transferPlayer(admin, target, input);
        plugin.getInventoryClickListener().clearPendingTransfer(admin.getUniqueId());
    }

    private void transferPlayer(Player admin, Player target, String server) {
        admin.sendMessage(Component.text("§6[AtomHub] - §7Transferring §f" + target.getName() +
                "§7 to §e" + server + "§7 server..."));
        target.sendMessage(Component.text("§a[Network] - §7Transferring to §e" + server + "§7 server..."));

        // Use NetworkMessenger to send the transfer request
        plugin.getMessenger().sendTransfer(admin, target, server);
    }
}