package me.atomoyo.atomhub.gui.player;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.Arrays;

public class PlayerOptionsMenu {

    private final Player admin;
    private final Player target;
    private final AtomHub plugin;

    public PlayerOptionsMenu(AtomHub plugin, Player admin, Player target) {
        this.admin = admin;
        this.target = target;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("PLAYER_OPTIONS"), 27, Component.text("Player Options"));

        boolean flying = target.getAllowFlight();
        Material flyingIcon = flying ? Material.FEATHER : Material.LEATHER_BOOTS;
        String flyingText = flying ? "Flight Enabled" : "Flight Disabled";
        inv.setItem(10, item(flyingIcon, flyingText, "Toggle flight ability"));

        boolean glowing = plugin.glowManager.isGlowing(target);
        Material glowingIcon = glowing ? Material.GLOWSTONE : Material.REDSTONE_LAMP;
        String glowingText = glowing ? "Glowing Enabled" : "Glowing Disabled";
        inv.setItem(12, item(glowingIcon, glowingText, "Toggle glowing effect"));

        inv.setItem(14, item(Material.FIREWORK_ROCKET, "Launch Up", "Launch player into air"));
        inv.setItem(14, item(Material.TNT, "Explode", "Explode player"));
        inv.setItem(14, item(Material.LIGHTNING_ROD, "Strike Lightning", "Strike player with lightning"));
        inv.setItem(16, item(Material.ENDER_EYE, "Random Teleport", "Teleport player randomly"));

        inv.setItem(22, item(Material.ARROW, "Back", "Return to player menu"));

        admin.openInventory(inv);
    }

    private ItemStack item(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack item(Material mat, String name) {
        return item(mat, name, "Click to use");
    }

    public void handleClick(Player admin, Material clicked) {
        switch (clicked) {
            case FEATHER, LEATHER_BOOTS -> {
                boolean flying = !target.getAllowFlight();
                target.setAllowFlight(flying);
                if (flying) {
                    target.setFlying(true);
                    admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " can now fly."));
                    target.sendMessage(Component.text("§aYou can now fly!"));
                } else {
                    target.setFlying(false);
                    admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " can no longer fly."));
                    target.sendMessage(Component.text("§cFlight disabled."));
                }
                open(); // Refresh the menu
            }
            case GLOWSTONE, REDSTONE_LAMP -> {
                boolean glowing = plugin.getGlowManager().toggleGlow(target);
                if (glowing) {
                    admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " is now glowing."));
                    target.sendMessage(Component.text("§aYou are now glowing!"));
                } else {
                    admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " is no longer glowing."));
                    target.sendMessage(Component.text("§cGlow disabled."));
                }
                open(); // Refresh the menu
            }
            case FIREWORK_ROCKET -> {
                target.setVelocity(target.getLocation().getDirection().multiply(2).setY(2));
                admin.sendMessage(Component.text("[AtomHub] - Launched " + target.getName() + " into the air!"));
                target.sendMessage(Component.text("§cYou were launched by an admin."));
            }
            case ENDER_EYE -> {
                World world = target.getWorld();
                double x = Math.random() * world.getWorldBorder().getSize() - (world.getWorldBorder().getSize()/2);
                double z = Math.random() * world.getWorldBorder().getSize() - (world.getWorldBorder().getSize()/2);
                double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
                target.teleport(new Location(world, x, y, z));
                admin.sendMessage(Component.text("[AtomHub] - Teleported " + target.getName() + " randomly!"));
                target.sendMessage(Component.text("§cYou were randomly teleported by an admin."));
            }
            case ARROW -> {
                plugin.getGuiManager().openPlayerMenu(admin, target);
            }
        }
    }
}