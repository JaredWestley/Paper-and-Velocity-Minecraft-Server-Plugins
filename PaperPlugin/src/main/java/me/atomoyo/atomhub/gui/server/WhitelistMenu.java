// File: me/atomoyo/atomhub/gui/WhitelistMenu.java
package me.atomoyo.atomhub.gui.server;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class WhitelistMenu {

    private final Player admin;
    private final AtomHub plugin;
    private final int page;

    public WhitelistMenu(Player admin, AtomHub plugin, int page) {
        this.admin = admin;
        this.plugin = plugin;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WHITELIST"), 54,
                Component.text("§8Whitelist Management - Page " + page));

        // Get all whitelisted players
        Set<OfflinePlayer> whitelistedPlayers = Bukkit.getWhitelistedPlayers();
        List<OfflinePlayer> sortedPlayers = new ArrayList<>(whitelistedPlayers);
        sortedPlayers.sort(Comparator.comparing(OfflinePlayer::getName));

        int itemsPerPage = 45;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, sortedPlayers.size());

        // Display whitelisted players
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            OfflinePlayer player = sortedPlayers.get(i);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(player);

            String status = player.isOnline() ? "§a● Online" : "§7● Offline";
            String lastPlayed = player.getLastPlayed() > 0 ?
                    formatTime(System.currentTimeMillis() - player.getLastPlayed()) + " ago" : "Never";

            meta.displayName(Component.text("§f" + player.getName()));
            meta.lore(Arrays.asList(
                    Component.text("§7UUID: §f" + player.getUniqueId()),
                    Component.text("§7Status: " + status),
                    Component.text("§7Last played: §f" + lastPlayed),
                    Component.text(""),
                    Component.text("§aLeft-click: Remove from whitelist"),
                    Component.text("§eRight-click: More info")
            ));
            skull.setItemMeta(meta);

            inv.setItem(slot, skull);
            slot++;
        }

        // Whitelist toggle
        boolean whitelistEnabled = Bukkit.hasWhitelist();
        Material toggleMaterial = whitelistEnabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String toggleStatus = whitelistEnabled ? "§aENABLED" : "§cDISABLED";

        inv.setItem(45, createToggleItem(toggleMaterial, "§eWhitelist Status",
                "Toggle server whitelist", "toggle", toggleStatus));

        // Add player to whitelist
        inv.setItem(46, createActionItem(Material.NAME_TAG, "§aAdd Player",
                "Add player to whitelist", "add"));

        // Mass add players
        inv.setItem(47, createActionItem(Material.BOOK, "§6Mass Add",
                "Add multiple players", "massadd"));

        // Import from file
        inv.setItem(48, createActionItem(Material.MAP, "§bImport",
                "Import whitelist from file", "import"));

        // Export to file
        inv.setItem(49, createActionItem(Material.WRITABLE_BOOK, "§dExport",
                "Export whitelist to file", "export"));

        // Reload whitelist
        inv.setItem(50, createActionItem(Material.ENDER_EYE, "§5Reload",
                "Reload whitelist from file", "reload"));

        // Navigation
        if (page > 1) {
            inv.setItem(51, createNavItem(Material.ARROW, "§7Previous Page", "page-" + (page - 1)));
        }

        if (endIndex < sortedPlayers.size()) {
            inv.setItem(52, createNavItem(Material.ARROW, "§7Next Page", "page-" + (page + 1)));
        }

        // Back button
        inv.setItem(53, createNavItem(Material.ARROW, "§7Back to Server Menu", "back"));

        admin.openInventory(inv);
    }

    private ItemStack createToggleItem(Material mat, String name, String description, String action, String status) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Status: " + status),
                Component.text("§8Action: " + action),
                Component.text("§eClick to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createActionItem(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Action: " + action),
                Component.text("§eClick to use")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createNavItem(Material mat, String name, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Collections.singletonList(Component.text("§8Action: " + action)));
        i.setItemMeta(meta);
        return i;
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + " seconds";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minutes";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hours";

        long days = hours / 24;
        return days + " days";
    }
}