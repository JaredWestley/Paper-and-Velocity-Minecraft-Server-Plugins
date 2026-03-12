// File: me/atomoyo/atomhub/arena/Arena.java
package me.atomoyo.atomhub.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Arena {
    private final String name;
    private final Location center;
    private final int radius;
    private final ArenaPreset preset;
    private final String rules;
    private final String creator;
    private final Date createdAt;
    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final int maxPlayers;
    private boolean active;
    private boolean inProgress;

    public Arena(String name, Location center, int radius, ArenaPreset preset, String rules, String creator) {
        this.name = name;
        this.center = center;
        this.radius = radius;
        this.preset = preset;
        this.rules = rules;
        this.creator = creator;
        this.createdAt = new Date();
        this.maxPlayers = 16; // Default max players
        this.active = true;
        this.inProgress = false;
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= maxPlayers || inProgress) {
            return false;
        }

        if (!players.contains(player)) {
            players.add(player);
            return true;
        }
        return false;
    }

    public boolean removePlayer(Player player) {
        return players.remove(player);
    }

    public Location getSpawnPoint() {
        // Calculate spawn point around center
        double angle = Math.random() * 2 * Math.PI;
        int distance = radius / 2;

        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;

        return new Location(center.getWorld(), x, center.getY() + 2, z);
    }

    // Getters
    public String getName() { return name; }
    public Location getCenter() { return center; }
    public int getRadius() { return radius; }
    public ArenaPreset getPreset() { return preset; }
    public String getRules() { return rules; }
    public String getCreator() { return creator; }
    public Date getCreatedAt() { return createdAt; }
    public List<Player> getPlayers() { return players; }
    public int getPlayerCount() { return players.size(); }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isActive() { return active; }
    public boolean isInProgress() { return inProgress; }

    // Setters
    public void setActive(boolean active) { this.active = active; }
    public void setInProgress(boolean inProgress) { this.inProgress = inProgress; }
}