package me.atomoyo.atomhub.managers;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodManager {

    private final Set<UUID> godPlayers = new HashSet<>();

    /** Toggles god mode for a player. Returns true if god mode is now ON. */
    public boolean toggleGod(Player player) {
        UUID id = player.getUniqueId();
        if (godPlayers.contains(id)) {
            godPlayers.remove(id);
            player.setInvulnerable(false);
            return false;
        } else {
            godPlayers.add(id);
            player.setInvulnerable(true);
            return true;
        }
    }

    public boolean isGod(Player player) {
        return godPlayers.contains(player.getUniqueId());
    }

}
