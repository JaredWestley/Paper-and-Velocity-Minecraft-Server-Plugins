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

public class NetworkBanMenu {

    private final Player admin;
    private final AtomHub plugin;
    private int page;

    public NetworkBanMenu(AtomHub plugin, Player admin, int page) {
        this.admin = admin;
        this.plugin = plugin;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_BAN"), 54,
                Component.text("§8Network Ban - Page " + page));

        // Get all online players
        @SuppressWarnings("unchecked")
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers().stream()
                .map(player -> (Player) player)
                .toList();
        int playersPerPage = 45;
        int startIndex = (page - 1) * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, players.size());

        // Add player heads
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Player target = players.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text("§f" + target.getName()));
            meta.lore(Arrays.asList(
                    Component.text("§7Click to ban this player"),
                    Component.text("§7from the entire network"),
                    Component.text(""),
                    Component.text("§8UUID: " + target.getUniqueId())
            ));
            head.setItemMeta(meta);
            inv.setItem(slot, head);
            slot++;
        }

        // Ban reasons
        inv.setItem(45, createBanReasonItem(Material.TNT, "§cHacking", "Use of cheats/hacks"));
        inv.setItem(46, createBanReasonItem(Material.BOOK, "§6Advertising", "Server advertisement"));
        inv.setItem(47, createBanReasonItem(Material.IRON_SWORD, "§4Exploiting", "Game exploits"));
        inv.setItem(48, createBanReasonItem(Material.PAPER, "§eHarassment", "Player harassment"));
        inv.setItem(49, createBanReasonItem(Material.BARRIER, "§cCustom Reason", "Enter custom reason"));

        // Navigation
        if (page > 1) {
            inv.setItem(50, createNavItem(Material.ARROW, "§fPrevious Page", "Go to page " + (page - 1)));
        }
        if (endIndex < players.size()) {
            inv.setItem(51, createNavItem(Material.ARROW, "§fNext Page", "Go to page " + (page + 1)));
        }

        inv.setItem(53, createNavItem(Material.ARROW, "§7Back", "Return to network menu"));

        admin.openInventory(inv);
    }

    private ItemStack createBanReasonItem(Material mat, String name, String reason) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Reason: " + reason),
                Component.text("§8Click to use this reason")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createNavItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
        i.setItemMeta(meta);
        return i;
    }
}