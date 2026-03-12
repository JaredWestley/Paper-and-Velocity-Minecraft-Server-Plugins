package me.atomoyo.atomhub.gui.player;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class PlayerHealthMenu {

    private final Player admin;
    private final Player target;
    private final AtomHub plugin;

    public PlayerHealthMenu(AtomHub plugin, Player admin, Player target) {
        this.admin = admin;
        this.target = target;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("PLAYER_HEALTH"), 36, Component.text("§8Health: §f" + target.getName()));

        // Healing options
        inv.setItem(10, createHealthItem(Material.PORKCHOP, "§aHeal", "Restore full health", "heal"));
        inv.setItem(11, createHealthItem(Material.GOLDEN_APPLE, "§6Super Heal", "Heal + give absorption", "super_heal"));
        inv.setItem(12, createHealthItem(Material.ENCHANTED_GOLDEN_APPLE, "§cGod Heal", "Full heal + effects", "god_heal"));

        // Damage options
        inv.setItem(14, createHealthItem(Material.IRON_SWORD, "§7Half Heart", "Damage 1 heart", "damage_1"));
        inv.setItem(15, createHealthItem(Material.DIAMOND_SWORD, "§c5 Hearts", "Damage 10 hearts", "damage_10"));
        inv.setItem(16, createHealthItem(Material.NETHERITE_SWORD, "§4Kill", "Instant death", "kill"));

        // Special options
        inv.setItem(19, createHealthItem(Material.TOTEM_OF_UNDYING, "§eRevive", "Bring back from dead", "revive"));
        inv.setItem(20, createHealthItem(Material.GHAST_TEAR, "§dRegeneration", "Give regen effect", "regen"));
        inv.setItem(21, createHealthItem(Material.SPIDER_EYE, "§2Poison", "Apply poison", "poison"));
        inv.setItem(28, createHealthItem(Material.WITHER_ROSE, "§8Wither", "Apply wither", "wither"));

        // Health manipulation
        inv.setItem(24, createHealthItem(Material.REDSTONE, "§cSet 1 Heart", "Set to 2 health", "set_2"));
        inv.setItem(25, createHealthItem(Material.GLOWSTONE_DUST, "§6Set Half", "Set to half health", "set_half"));
        inv.setItem(26, createHealthItem(Material.EMERALD, "§aSet Full", "Set to max health", "set_max"));

        // Info
        inv.setItem(30, createInfoItem(Material.PAPER, "§6Health Info",
                "Current: " + String.format("%.1f", target.getHealth()) + " / " + String.format("%.1f", target.getMaxHealth()),
                "Food: " + target.getFoodLevel() + "/20",
                "Saturation: " + String.format("%.1f", target.getSaturation())));

        inv.setItem(31, item(Material.ARROW, "§7Back"));

        admin.openInventory(inv);
    }

    private ItemStack createHealthItem(Material mat, String name, String lore, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + lore),
                Component.text("§8Click to use")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createInfoItem(Material mat, String name, String... lines) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        java.util.List<Component> lore = new java.util.ArrayList<>();
        for (String line : lines) {
            lore.add(Component.text("§7" + line));
        }
        meta.lore(lore);
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
            case APPLE -> healPlayer();
            case GOLDEN_APPLE -> superHeal();
            case ENCHANTED_GOLDEN_APPLE -> godHeal();
            case IRON_SWORD -> damagePlayer(2.0);
            case DIAMOND_SWORD -> damagePlayer(20.0);
            case NETHERITE_SWORD -> killPlayer();
            case TOTEM_OF_UNDYING -> revivePlayer();
            case GHAST_TEAR -> giveRegen();
            case SPIDER_EYE -> givePoison();
            case WITHER_ROSE -> giveWither();
            case REDSTONE -> setHealth(2.0);
            case GLOWSTONE_DUST -> setHealth(target.getMaxHealth() / 2);
            case EMERALD -> setHealth(target.getMaxHealth());
            case ARROW -> plugin.getGuiManager().openPlayerMenu(admin, target);
        }
    }

    private void healPlayer() {
        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.sendMessage(Component.text("§a[World] §7You have been healed!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Healed §f" + target.getName()));
    }

    private void superHeal() {
        healPlayer();
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.ABSORPTION, 1200, 3, true, false
        ));
        target.sendMessage(Component.text("§a[World] §7Super healing applied!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Super healed §f" + target.getName()));
    }

    private void godHeal() {
        healPlayer();
        // Add multiple beneficial effects
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, 600, 2, true, false
        ));
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                PotionEffectType.RESISTANCE, 600, 1, true, false
        ));
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 1200, 0, true, false
        ));
        target.sendMessage(Component.text("§6[World] §7God healing applied!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7God healed §f" + target.getName()));
    }

    private void damagePlayer(double amount) {
        double newHealth = Math.max(0, target.getHealth() - amount);
        target.setHealth(newHealth);
        target.sendMessage(Component.text("§c[World] §7You took " + amount + " damage!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Damaged §f" + target.getName() + "§7 by §c" + amount));
    }

    private void killPlayer() {
        target.setHealth(0);
        target.sendMessage(Component.text("§4[World] §7You were killed by an admin!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Killed §f" + target.getName()));
    }

    private void revivePlayer() {
        if (!target.isDead()) {
            admin.sendMessage(Component.text("§c" + target.getName() + " is not dead!"));
            return;
        }
        // In reality, you'd need to respawn the player
        target.spigot().respawn();
        healPlayer();
        target.sendMessage(Component.text("§e[World] §7You have been revived!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Revived §f" + target.getName()));
    }

    private void giveRegen() {
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, 600, 1, true, false
        ));
        target.sendMessage(Component.text("§d[World] §7You feel regenerative!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Gave regeneration to §f" + target.getName()));
    }

    private void givePoison() {
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.POISON, 200, 1, true, false
        ));
        target.sendMessage(Component.text("§2[World] §7You feel poisoned!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Poisoned §f" + target.getName()));
    }

    private void giveWither() {
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WITHER, 200, 1, true, false
        ));
        target.sendMessage(Component.text("§8[World] §7You feel withered!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Withered §f" + target.getName()));
    }

    private void setHealth(double health) {
        target.setHealth(Math.min(health, target.getMaxHealth()));
        target.sendMessage(Component.text("§a[World] §7Your health was set to " + String.format("%.1f", health)));
        admin.sendMessage(Component.text("§6[AtomHub] §7Set §f" + target.getName() + "'s§7 health to §a" + String.format("%.1f", health)));
    }
}