package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Random;

public class PlayerJoinListener implements Listener {

    private final AtomHub plugin;

    private final Particle[] particles = {
            Particle.FLAME,
            Particle.HEART,
            Particle.HAPPY_VILLAGER,
            Particle.END_ROD,
            Particle.CRIT,
            Particle.CLOUD,
            Particle.FIREWORK
    };

    public PlayerJoinListener(AtomHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        p.sendMessage(Component.text("Hello " + p.getName() + ", Welcome to AtomHub!"));

        Particle particle = particles[new Random().nextInt(particles.length)];

        p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0),
                40, 0.5, 0.5, 0.5, 0.05);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (plugin.isMaintenanceMode() && !event.getPlayer().isOp() || plugin.isMaintenanceMode() && event.getPlayer().hasPermission("atomhub.maintenance.bypass")) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    "§cServer is currently in maintenance mode. Only OPs can join.");
        }
    }
}
