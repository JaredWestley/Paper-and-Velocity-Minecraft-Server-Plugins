package me.atomoyo.atomhub.gui.player;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class PlayerMovementMenu {

    private final Player admin;
    private final Player target;
    private final AtomHub plugin;

    public PlayerMovementMenu(AtomHub plugin, Player admin, Player target) {
        this.admin = admin;
        this.target = target;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("PLAYER_MOVEMENT"), 27, Component.text("§8Movement Control: §f" + target.getName()));

        inv.setItem(10, createSpeedItem(Material.TURTLE_HELMET, "§cVery Slow", "Speed 0.2x", 0.2));
        inv.setItem(11, createSpeedItem(Material.IRON_BOOTS, "§6Slow", "Speed 0.5x", 0.5));
        inv.setItem(12, createSpeedItem(Material.LEATHER_BOOTS, "§eNormal", "Speed 1x", 1.0));
        inv.setItem(13, createSpeedItem(Material.GOLDEN_BOOTS, "§aFast", "Speed 2x", 2.0));
        inv.setItem(14, createSpeedItem(Material.DIAMOND_BOOTS, "§bVery Fast", "Speed 3x", 3.0));
        inv.setItem(15, createSpeedItem(Material.NETHERITE_BOOTS, "§dExtreme", "Speed 5x", 5.0));

        inv.setItem(16, createSpeedItem(Material.SPLASH_POTION, "§fSuper Speed", "Speed 10x", 10.0));

        inv.setItem(16, item(Material.FEATHER, "§dToggle Flight"));

        inv.setItem(22, item(Material.ARROW, "§7Back to player menu"));

        admin.openInventory(inv);
    }

    private ItemStack createSpeedItem(Material mat, String name, String lore, double multiplier) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + lore),
                Component.text("§8Click to apply")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack item(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }

    public void handleClick(Player admin, Material clicked) {
        switch (clicked) {
            case TURTLE_HELMET -> setSpeed(0.2, "§cVery Slow");
            case IRON_BOOTS -> setSpeed(0.5, "§6Slow");
            case LEATHER_BOOTS -> setSpeed(1.0, "§eNormal");
            case GOLDEN_BOOTS -> setSpeed(2.0, "§aFast");
            case DIAMOND_BOOTS -> setSpeed(3.0, "§bVery Fast");
            case NETHERITE_BOOTS -> setSpeed(5.0, "§dExtreme");
            case SPLASH_POTION -> setSpeed(10.0, "§fSuper Speed");
            case FEATHER -> toggleFlight();
            case ARROW -> plugin.getGuiManager().openPlayerMenu(admin, target);
        }
    }


    private void setSpeed(double multiplier, String name) {
        // Remove any existing speed effects
        target.removePotionEffect(PotionEffectType.SPEED);

        if (multiplier == 1.0) {
            // Normal speed - no effect needed
            target.sendMessage(Component.text("§a[World] - §7Your speed has been reset to normal"));
            admin.sendMessage(Component.text("§6[AtomHub] - §7Reset §f" + target.getName() + "'s§7 speed to normal"));
        } else {
            // Calculate amplifier (each level = 20% increase)
            int amplifier = (int) ((multiplier - 1.0) / 0.2);
            amplifier = Math.max(0, Math.min(amplifier, 255)); // Clamp to valid range

            target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, amplifier, true, false));
            target.sendMessage(Component.text("§a[World] - §7Your speed is now " + name + "§7!"));
            admin.sendMessage(Component.text("§6[AtomHub] - §7Set §f" + target.getName() + "'s§7 speed to " + name));
        }

        plugin.getGuiManager().openPlayerMenu(admin, target);
    }

    private void toggleFlight() {
        boolean canFly = !target.getAllowFlight();
        target.setAllowFlight(canFly);
        if (canFly) {
            target.setFlying(true);
            target.sendMessage(Component.text("§f[World] - §7You can now fly!"));
            admin.sendMessage(Component.text("§6[AtomHub] - §7Enabled flight for §f" + target.getName()));
        } else {
            target.setFlying(false);
            target.sendMessage(Component.text("§f[World] - §7Flight disabled"));
            admin.sendMessage(Component.text("§6[AtomHub] - §7Disabled flight for §f" + target.getName()));
        }
    }
}