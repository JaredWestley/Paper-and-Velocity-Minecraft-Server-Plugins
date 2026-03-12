package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.managers.FreezeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {

    private final FreezeManager freezeManager;

    public FreezeListener(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (!freezeManager.isFrozen(p)) return;

        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (freezeManager.isFrozen(p)) {
            event.setCancelled(true);
        }
    }
}
