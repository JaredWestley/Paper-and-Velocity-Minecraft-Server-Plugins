package me.atomoyo.atomhub.gui.network;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class NetworkServerTransferMenu {

    private final Player admin;
    private final Player target;
    private final AtomHub plugin;

    public NetworkServerTransferMenu(Player admin, Player target, AtomHub plugin) {
        this.admin = admin;
        this.target = target;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_TRANSFER"), 36,
                Component.text("§8Transfer: §f" + target.getName()));

        // Server destinations
        inv.setItem(10, createServerItem(Material.COMPASS, "§aLobby Server", "lobby", "§7Network lobby"));
        inv.setItem(11, createServerItem(Material.DIAMOND_BLOCK, "§bCreative Server", "creative", "§7Creative building"));
        inv.setItem(12, createServerItem(Material.GRASS_BLOCK, "§cSkyblock Server", "Skyblock", "§7Floating Islands"));
        inv.setItem(13, createServerItem(Material.END_STONE, "§dSMP Server", "smp", "§7Survival world"));
        inv.setItem(14, createServerItem(Material.DIAMOND_SWORD, "§6KitPvP", "kitpvp", "§7PvP Arena"));

        // Custom server entry
        inv.setItem(22, createCustomServerItem());

        // Player info
        inv.setItem(31, createInfoItem(Material.PAPER, "§6Player Info",
                "Name: " + target.getName(),
                "Current Server: " + target.getServer().getName(),
                "Ping: " + target.getPing() + "ms",
                "GameMode: " + target.getGameMode()));

        inv.setItem(35, createNavItem(Material.ARROW, "§7Back to player list"));

        admin.openInventory(inv);
    }

    private ItemStack createServerItem(Material mat, String name, String server, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text(lore),
                Component.text(""),
                Component.text("§8Server: " + server),
                Component.text("§8Status: §aOnline"),
                Component.text("§8Players: 24/50"),
                Component.text(""),
                Component.text("§aClick to transfer player")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createCustomServerItem() {
        ItemStack i = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text("§eCustom Server"));
        meta.lore(Arrays.asList(
                Component.text("§7Transfer to a custom server"),
                Component.text("§8Enter server name manually"),
                Component.text(""),
                Component.text("§aClick to enter server name")
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

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }
}