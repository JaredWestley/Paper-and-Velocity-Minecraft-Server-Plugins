package me.atomoyo.atomhub.gui;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WorldMenu {

    private final Player player;
    private final AtomHub plugin;

    public WorldMenu(Player player, AtomHub plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD"), 54, Component.text("§8World Management"));

        // Section 1: Time & Weather
        inv.setItem(10, createSectionItem(Material.CLOCK, "§eTime Control", "Adjust time and day/night"));
        inv.setItem(11, createSectionItem(Material.PAPER, "§9Weather Control", "Control rain, storms, and effects"));

        // Section 2: Environment
        inv.setItem(12, createSectionItem(Material.GRASS_BLOCK, "§aEnvironment", "Biome, difficulty, game rules"));
        inv.setItem(13, createSectionItem(Material.BEACON, "§dWorld Effects", "Special world-wide effects"));

        // Section 3: World Events
        inv.setItem(14, createSectionItem(Material.FIREWORK_ROCKET, "§cWorld Events", "Trigger special events"));
        inv.setItem(15, createSectionItem(Material.ENDER_CHEST, "§5Mystical Events", "Magical world events"));

        // Quick Actions Row 1
        inv.setItem(28, createActionItem(Material.DAYLIGHT_DETECTOR, "§eSet Day", "Fast day set"));
        inv.setItem(29, createActionItem(Material.REDSTONE_TORCH, "§8Set Night", "Fast night set"));
        inv.setItem(30, createActionItem(Material.BARRIER, "§bToggle Weather", "Quick weather toggle"));
        inv.setItem(31, createActionItem(Material.TNT, "§cKill Hostile Mobs", "Clear hostile mobs"));
        inv.setItem(32, createActionItem(Material.SPAWNER, "§6Toggle Mob Spawn", "Enable/disable mob spawn"));

        // Quick Actions Row 2
        inv.setItem(33, createActionItem(Material.TOTEM_OF_UNDYING, "§aHeal All Players", "Restore health to all"));
        inv.setItem(34, createActionItem(Material.EXPERIENCE_BOTTLE, "§2Give All XP", "Give XP to all players"));
        inv.setItem(35, createActionItem(Material.ENDER_PEARL, "§dRandom TP All", "Teleport everyone randomly"));
        inv.setItem(36, createActionItem(Material.IRON_SWORD, "§4PvP Mode", "Toggle PvP globally"));
        inv.setItem(37, createActionItem(Material.EMERALD, "§eEco Storm", "Money/item rain"));

        // Back button
        inv.setItem(49, createActionItem(Material.ARROW, "§7Back", "Return to main menu"));

        // Information
        inv.setItem(50, createInfoItem(Material.BOOK, "§6World Info",
                "World: " + player.getWorld().getName(),
                "Time: " + player.getWorld().getTime(),
                "Weather: " + (player.getWorld().hasStorm() ? "Storm" : "Clear"),
                "Players: " + player.getWorld().getPlayers().size()));

        player.openInventory(inv);
    }

    private ItemStack createSectionItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + lore),
                Component.text("§8Click to open")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createActionItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
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
}
