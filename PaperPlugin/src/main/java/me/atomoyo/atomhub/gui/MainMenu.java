package me.atomoyo.atomhub.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenu {

    private final Player player;

    public MainMenu(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("MAIN"), 27, Component.text("Admin Panel"));

        inv.setItem(10, item(Material.COMMAND_BLOCK, "Server Settings"));
        inv.setItem(12, item(Material.NETHER_STAR, "Network Settings"));
        inv.setItem(14, item(Material.GRASS_BLOCK, "World Settings"));
        inv.setItem(16, item(Material.PLAYER_HEAD, "Player Settings"));

        player.openInventory(inv);
    }

    private ItemStack item(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.text(name));
        i.setItemMeta(m);
        return i;
    }
}
