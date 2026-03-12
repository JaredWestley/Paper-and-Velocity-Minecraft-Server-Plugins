package me.atomoyo.atomhub.gui.player;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class PlayerEffectsMenu {

    private final Player admin;
    private final Player target;
    private final AtomHub plugin;

    public PlayerEffectsMenu(AtomHub plugin, Player admin, Player target) {
        this.admin = admin;
        this.target = target;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("PLAYER_EFFECTS"), 54, Component.text("§8Effects: §f" + target.getName()));

        // Row 1: Beneficial Effects
        inv.setItem(0, createEffectItem(Material.GOLDEN_APPLE, "§6Strength", PotionEffectType.STRENGTH));
        inv.setItem(1, createEffectItem(Material.SUGAR, "§aSpeed", PotionEffectType.SPEED));
        inv.setItem(2, createEffectItem(Material.FEATHER, "§fJump Boost", PotionEffectType.JUMP_BOOST));
        inv.setItem(3, createEffectItem(Material.GOLDEN_CARROT, "§eNight Vision", PotionEffectType.NIGHT_VISION));
        inv.setItem(4, createEffectItem(Material.TURTLE_HELMET, "§bWater Breathing", PotionEffectType.WATER_BREATHING));
        inv.setItem(5, createEffectItem(Material.ENCHANTED_GOLDEN_APPLE, "§cRegeneration", PotionEffectType.REGENERATION));
        inv.setItem(6, createEffectItem(Material.SHIELD, "§9Resistance", PotionEffectType.RESISTANCE));
        inv.setItem(7, createEffectItem(Material.FIRE_CHARGE, "§6Fire Resistance", PotionEffectType.FIRE_RESISTANCE));
        inv.setItem(8, createEffectItem(Material.GLOWSTONE_DUST, "§bGlowing", PotionEffectType.GLOWING));

        // Row 2: Harmful Effects
        inv.setItem(9, createEffectItem(Material.POISONOUS_POTATO, "§2Poison", PotionEffectType.POISON));
        inv.setItem(10, createEffectItem(Material.WITHER_ROSE, "§8Wither", PotionEffectType.WITHER));
        inv.setItem(11, createEffectItem(Material.SPIDER_EYE, "§cNausea", PotionEffectType.NAUSEA));
        inv.setItem(12, createEffectItem(Material.LEAD, "§7Slowness", PotionEffectType.SLOWNESS));
        inv.setItem(13, createEffectItem(Material.FERMENTED_SPIDER_EYE, "§5Weakness", PotionEffectType.WEAKNESS));
        inv.setItem(14, createEffectItem(Material.BONE, "§fBlindness", PotionEffectType.BLINDNESS));
        inv.setItem(15, createEffectItem(Material.ROTTEN_FLESH, "§4Hunger", PotionEffectType.HUNGER));
        inv.setItem(16, createEffectItem(Material.IRON_CHAIN, "§8Mining Fatigue", PotionEffectType.MINING_FATIGUE));
        inv.setItem(17, createEffectItem(Material.PUFFERFISH, "§dLevitation", PotionEffectType.LEVITATION));

        // Row 3: Special Effects
        inv.setItem(18, createEffectItem(Material.HEART_OF_THE_SEA, "§3Dolphin's Grace", PotionEffectType.DOLPHINS_GRACE));
        inv.setItem(19, createEffectItem(Material.CONDUIT, "§bConduit Power", PotionEffectType.CONDUIT_POWER));
        inv.setItem(20, createEffectItem(Material.PHANTOM_MEMBRANE, "§fSlow Falling", PotionEffectType.SLOW_FALLING));
        inv.setItem(21, createEffectItem(Material.HONEY_BOTTLE, "§6Haste", PotionEffectType.HASTE));
        inv.setItem(22, createEffectItem(Material.TOTEM_OF_UNDYING, "§aHealth Boost", PotionEffectType.HEALTH_BOOST));
        inv.setItem(23, createEffectItem(Material.NAUTILUS_SHELL, "§9Absorption", PotionEffectType.ABSORPTION));
        inv.setItem(24, createEffectItem(Material.TURTLE_SCUTE, "§2Hero of the Village", PotionEffectType.HERO_OF_THE_VILLAGE));
        inv.setItem(25, createEffectItem(Material.SOUL_LANTERN, "§8Bad Omen", PotionEffectType.BAD_OMEN));

        // Row 4: Control Buttons
        inv.setItem(37, createControlItem(Material.REDSTONE, "§cMake Temporary", "Make effects temporary"));
        inv.setItem(45, createControlItem(Material.MILK_BUCKET, "§fClear All Effects", "Remove all effects"));
        inv.setItem(46, createControlItem(Material.CLOCK, "§eMake Permanent", "Make effects permanent"));
        inv.setItem(48, createControlItem(Material.BOOK, "§6Current Effects", "View active effects"));

        inv.setItem(49, item(Material.ARROW, "§7- Return to player menu"));

        admin.openInventory(inv);
    }

    private ItemStack createEffectItem(Material mat, String name, PotionEffectType effect) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        boolean hasEffect = target.hasPotionEffect(effect);
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Effect: " + effect.getName()),
                Component.text(hasEffect ? "§aCurrently active" : "§8Click to apply"),
                Component.text("§8Duration: 3 minutes")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createControlItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
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
        // Handle effect clicks
        // You would map each material to its corresponding PotionEffectType
        // For example:
        if (clicked == Material.GOLDEN_APPLE) {
            toggleEffect(PotionEffectType.STRENGTH, "Strength", 1, 3600);
        } else if (clicked == Material.SUGAR) {
            toggleEffect(PotionEffectType.SPEED, "Speed", 1, 3600);
        } else if (clicked == Material.FEATHER) {
            toggleEffect(PotionEffectType.JUMP_BOOST, "Jump Boost", 1, 3600);
        } else if (clicked == Material.GOLDEN_CARROT) {
            toggleEffect(PotionEffectType.NIGHT_VISION, "Night Vision", 1, 3600);
        } else if (clicked == Material.TURTLE_HELMET) {
            toggleEffect(PotionEffectType.WATER_BREATHING, "Water Breathing", 1, 3600);
        } else if (clicked == Material.ENCHANTED_GOLDEN_APPLE) {
            toggleEffect(PotionEffectType.REGENERATION, "Regeneration", 1, 3600);
        } else if (clicked == Material.SHIELD) {
            toggleEffect(PotionEffectType.RESISTANCE, "Resistance", 1, 3600);
        } else if (clicked == Material.FIRE_CHARGE) {
            toggleEffect(PotionEffectType.FIRE_RESISTANCE, "Fire Resistance", 1, 3600);
        } else if (clicked == Material.GLOWSTONE_DUST) {
            toggleEffect(PotionEffectType.GLOWING, "Glowing", 1, 3600);
        } else if (clicked == Material.POISONOUS_POTATO) {
            toggleEffect(PotionEffectType.POISON, "Poison", 1, 3600);
        } else if (clicked == Material.WITHER_ROSE) {
            toggleEffect(PotionEffectType.WITHER, "Wither", 1, 3600);
        } else if (clicked == Material.SPIDER_EYE) {
            toggleEffect(PotionEffectType.NAUSEA, "Nausea", 1, 3600);
        } else if (clicked == Material.LEAD) {
            toggleEffect(PotionEffectType.SLOWNESS, "Slowness", 1, 3600);
        } else if (clicked == Material.FERMENTED_SPIDER_EYE) {
            toggleEffect(PotionEffectType.WEAKNESS, "Weakness", 1, 3600);
        } else if (clicked == Material.BONE) {
            toggleEffect(PotionEffectType.BLINDNESS, "Blindness", 1, 3600);
        } else if (clicked == Material.ROTTEN_FLESH) {
            toggleEffect(PotionEffectType.HUNGER, "Hunger", 1, 3600);
        } else if (clicked == Material.IRON_CHAIN) {
            toggleEffect(PotionEffectType.MINING_FATIGUE, "Mining Fatigue", 1, 3600);
        } else if (clicked == Material.PUFFERFISH) {
            toggleEffect(PotionEffectType.LEVITATION, "Levitation", 1, 3600);
        } else if (clicked == Material.HEART_OF_THE_SEA) {
            toggleEffect(PotionEffectType.DOLPHINS_GRACE, "Dolphin's Grace", 1, 3600);
        } else if (clicked == Material.CONDUIT) {
            toggleEffect(PotionEffectType.CONDUIT_POWER, "Conduit Power", 1, 3600);
        } else if (clicked == Material.PHANTOM_MEMBRANE) {
            toggleEffect(PotionEffectType.SLOW_FALLING, "Slow Falling", 1, 3600);
        } else if (clicked == Material.HONEY_BOTTLE) {
            toggleEffect(PotionEffectType.HASTE, "Haste", 1, 3600);
        } else if (clicked == Material.TOTEM_OF_UNDYING) {
            toggleEffect(PotionEffectType.HEALTH_BOOST, "Health Boost", 1, 3600);
        } else if (clicked == Material.NAUTILUS_SHELL) {
            toggleEffect(PotionEffectType.ABSORPTION, "Absorption", 1, 3600);
        } else if (clicked == Material.TURTLE_SCUTE) {
            toggleEffect(PotionEffectType.HERO_OF_THE_VILLAGE, "Hero Of The Village", 1, 3600);
        } else if (clicked == Material.SOUL_LANTERN) {
            toggleEffect(PotionEffectType.BAD_OMEN, "BAD Omen", 1, 3600);
        } else if (clicked == Material.MILK_BUCKET) {
            clearAllEffects();
        } else if (clicked == Material.ARROW) {
            plugin.getGuiManager().openPlayerMenu(admin, target);
        }
        // Add cases for all other effects...
    }

    private void toggleEffect(PotionEffectType effect, String name, int amplifier, int duration) {
        if (target.hasPotionEffect(effect)) {
            target.removePotionEffect(effect);
            target.sendMessage(Component.text("§a[World] §7" + name + " effect removed"));
            admin.sendMessage(Component.text("§6[AtomHub] §7Removed " + name + " from §f" + target.getName()));
        } else {
            target.addPotionEffect(new PotionEffect(effect, duration, amplifier, true, false));
            target.sendMessage(Component.text("§a[World] §7You gained " + name + " effect!"));
            admin.sendMessage(Component.text("§6[AtomHub] §7Applied " + name + " to §f" + target.getName()));
        }
    }

    private void clearAllEffects() {
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
        target.sendMessage(Component.text("§f[World] §7All effects cleared!"));
        admin.sendMessage(Component.text("§6[AtomHub] §7Cleared all effects from §f" + target.getName()));
    }
}