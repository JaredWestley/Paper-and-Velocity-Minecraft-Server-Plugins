package me.atomoyo.atomhub.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private final Set<UUID> frozen = new HashSet<>();

    public boolean isFrozen(Player p) {
        return frozen.contains(p.getUniqueId());
    }

    public void toggle(Player p) {
        if (isFrozen(p)) frozen.remove(p.getUniqueId());
        else frozen.add(p.getUniqueId());
    }

    public Set<Player> getFrozenPlayers() {
        Set<Player> players = new HashSet<>();
        for (UUID id : frozen) {
            Player p = org.bukkit.Bukkit.getPlayer(id);
            if (p != null) players.add(p);
        }
        return players;
    }
}
