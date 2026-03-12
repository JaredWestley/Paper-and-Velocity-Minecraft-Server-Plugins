package me.atomoyo.atomhub.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class NetworkMenu {

    private final Player player;

    public NetworkMenu(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK"), 54, Component.text("§8Network Management"));

        // Row 1: Player Management
        inv.setItem(10, item(Material.ANVIL, "§4Network Ban", "Ban players from entire network"));
        inv.setItem(11, item(Material.BARRIER, "§cNetwork Kick", "Kick players from network"));
        inv.setItem(12, item(Material.PAPER, "§6Mute Management", "Manage chat permissions"));
        inv.setItem(13, item(Material.COMPASS, "§bServer Transfer", "Send players to other servers"));
        inv.setItem(14, item(Material.PLAYER_HEAD, "§eNetwork Players", "View all online players"));

        // Row 2: Server Management
        inv.setItem(19, item(Material.REDSTONE_BLOCK, "§cServer Control", "Start/stop network servers"));
        inv.setItem(20, item(Material.COMMAND_BLOCK, "§aServer Commands", "Send commands to servers"));
        inv.setItem(21, item(Material.BOOK, "§6Network Info", "View network statistics"));
        inv.setItem(22, item(Material.ENDER_EYE, "§dNetwork Broadcast", "Send message to all servers"));
        inv.setItem(23, item(Material.BEACON, "§bNetwork Alerts", "Configure alerts"));

        // Row 3: Network Settings
        inv.setItem(28, item(Material.LEVER, "§fMaintenance Mode", "Toggle network maintenance"));
        inv.setItem(29, item(Material.IRON_CHAIN, "§7Network Rules", "Configure network rules"));
        inv.setItem(30, item(Material.NAME_TAG, "§ePlayer Whitelist", "Manage whitelisted players"));
        inv.setItem(31, item(Material.CLOCK, "§6Schedule Actions", "Schedule network tasks"));
        inv.setItem(32, item(Material.CHEST, "§aNetwork Inventory", "Shared network storage"));

        // Row 4: Advanced Features
        inv.setItem(37, item(Material.ENCHANTING_TABLE, "§5Network Permissions", "Manage permissions"));
        inv.setItem(38, item(Material.EXPERIENCE_BOTTLE, "§2Network Economy", "Global economy system"));
        inv.setItem(39, item(Material.TOTEM_OF_UNDYING, "§cNetwork Protection", "Anti-cheat & security"));
        inv.setItem(40, item(Material.MAP, "§bNetwork Map", "View server connections"));
        inv.setItem(41, item(Material.NETHER_STAR, "§dNetwork Events", "Global events & tournaments"));

        // Info & Back
        inv.setItem(49, createInfoItem(Material.PAPER, "§6Network Status",
                "Connected Servers: " + getConnectedServers(),
                "Total Players: " + getTotalPlayers(),
                "Network Uptime: 24h 30m",
                "Status: §aOnline"));

        inv.setItem(53, item(Material.ARROW, "§7Back to Main Menu"));

        player.openInventory(inv);
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
        return item(mat, name, "Click to open");
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

    // Placeholder methods - these would connect to your Velocity proxy
    private String getConnectedServers() {
        return "3"; // Example: Survival, Creative, Lobby
    }

    private String getTotalPlayers() {
        return String.valueOf(Bukkit.getOnlinePlayers().size()); // Local only
    }
}