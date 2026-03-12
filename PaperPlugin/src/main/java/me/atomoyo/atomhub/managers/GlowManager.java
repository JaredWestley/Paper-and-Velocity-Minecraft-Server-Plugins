package me.atomoyo.atomhub.managers;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlowManager {

    private final Set<UUID> glowingPlayers = new HashSet<>();

    /** Toggles glow effect. Returns true if glowing is now ON. */
    public boolean toggleGlow(Player player) {
        UUID id = player.getUniqueId();
        boolean nowGlowing;
        if (glowingPlayers.contains(id)) {
            glowingPlayers.remove(id);
            nowGlowing = false;
        } else {
            glowingPlayers.add(id);
            nowGlowing = true;
        }
        player.setGlowing(nowGlowing);
        return nowGlowing;
    }

    public boolean isGlowing(Player player) {
        return glowingPlayers.contains(player.getUniqueId());
    }

}
