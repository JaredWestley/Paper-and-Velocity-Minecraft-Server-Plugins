package me.atomoyo.atomhub.gui.server;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class AutoSaveMenu {

    private final AtomHub plugin;
    private final Player player;

    public AutoSaveMenu(AtomHub plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("AUTOSAVE"), 27,
                Component.text("§8Auto-Save Settings"));

        int saveInterval = plugin.getConfig().getInt("autosave.interval", 10);

        inv.setItem(4, createInfoItem(Material.CLOCK, "§6§lAuto-Save Status",
                "§7Interval: §f" + saveInterval + " minutes",
                "§7Status: §aEnabled"));

        inv.setItem(11, createActionItem(Material.LIME_CONCRETE, "§aSave Now",
                "§7Force world save"));
        inv.setItem(12, createActionItem(Material.YELLOW_CONCRETE, "§e5 Minutes",
                "§7Set save interval"));
        inv.setItem(13, createActionItem(Material.ORANGE_CONCRETE, "§610 Minutes",
                "§7Set save interval"));
        inv.setItem(14, createActionItem(Material.RED_CONCRETE, "§c30 Minutes",
                "§7Set save interval"));

        inv.setItem(26, createNavItem(Material.ARROW, "§7Back"));

        player.openInventory(inv);
    }

    public void handleClick(Material clicked) {
        if (clicked == Material.ARROW) {
            new PerformanceMenu(player, plugin).open();
            return;
        }

        switch (clicked) {
            case LIME_CONCRETE -> {
                for (org.bukkit.World w : Bukkit.getWorlds()) {
                    w.save();
                }
                player.sendMessage(Component.text("§a[AtomHub] - All worlds saved"));
            }
            case YELLOW_CONCRETE -> {
                plugin.getConfig().set("autosave.interval", 5);
                plugin.saveConfig();
                player.sendMessage(Component.text("§6[AtomHub] - §7Auto-save interval set to 5 minutes"));
            }
            case ORANGE_CONCRETE -> {
                plugin.getConfig().set("autosave.interval", 10);
                plugin.saveConfig();
                player.sendMessage(Component.text("§6[AtomHub] - §7Auto-save interval set to 10 minutes"));
            }
            case RED_CONCRETE -> {
                plugin.getConfig().set("autosave.interval", 30);
                plugin.saveConfig();
                player.sendMessage(Component.text("§6[AtomHub] - §7Auto-save interval set to 30 minutes"));
            }
        }
    }

    private ItemStack createInfoItem(Material mat, String name, String... lines) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        java.util.List<Component> lore = new java.util.ArrayList<>();
        for (String line : lines) {
            lore.add(Component.text(line));
        }
        meta.lore(lore);
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createActionItem(Material mat, String name, String desc) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text(desc),
                Component.text("§eClick to execute")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }
}
