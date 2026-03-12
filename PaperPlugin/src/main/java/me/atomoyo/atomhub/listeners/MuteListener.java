package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.managers.MuteManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class MuteListener implements Listener {

    private final MuteManager muteManager;

    public MuteListener(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (muteManager.isMuted(e.getPlayer())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Component.text("§cYou are muted and cannot chat."));
        }
    }
}
