package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.gui.world.WorldEffectsMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class WorldEffectsListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            double xpMultiplier = WorldEffectsMenu.getXPMultiplier(killer.getWorld());
            if (xpMultiplier > 1) {
                // Multiply XP
                int originalXP = event.getDroppedExp();
                int multipliedXP = (int)(originalXP * xpMultiplier);
                event.setDroppedExp(multipliedXP);

                // Show multiplier message
                if (xpMultiplier > 1) {
                    killer.sendActionBar(org.bukkit.ChatColor.GREEN + "✧ " + xpMultiplier + "x XP!");
                }
            }

            double lootMultiplier = WorldEffectsMenu.getLootMultiplier(killer.getWorld());
            if (lootMultiplier > 1) {
                // Multiply drops
                for (ItemStack drop : event.getDrops()) {
                    if (drop != null) {
                        drop.setAmount((int)(drop.getAmount() * lootMultiplier));
                    }
                }

                if (lootMultiplier > 1) {
                    killer.sendActionBar(org.bukkit.ChatColor.GREEN + "✦ " + lootMultiplier + "x Loot!");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (WorldEffectsMenu.isFastMiningEnabled() && !event.getPlayer().isOp()) {
            // Make blocks break instantly for non-OPs when fast mining is enabled
            event.getBlock().breakNaturally();
            event.setCancelled(true); // Cancel the original break to prevent duplication
        }
    }
}