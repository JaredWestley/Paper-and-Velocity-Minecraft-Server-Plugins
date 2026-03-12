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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkKickMenu {

    private final Player admin;
    private final AtomHub plugin;
    private int page;

    public NetworkKickMenu(AtomHub plugin, Player admin, int page) {
        this.admin = admin;
        this.plugin = plugin;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_KICK"), 54,
                Component.text("§8Network Kick - Page " + page));

        List<Player> players = new ArrayList<>();
        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            players.add((Player) p);
        }

        int playersPerPage = 45;
        int startIndex = (page - 1) * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, players.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Player target = players.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text("§f" + target.getName()));
            meta.lore(Arrays.asList(
                    Component.text("§7Click to kick this player"),
                    Component.text("§7from the entire network"),
                    Component.text(""),
                    Component.text("§8Server: " + target.getWorld().getName())
            ));
            head.setItemMeta(meta);
            inv.setItem(slot, head);
            slot++;
        }

        // Quick kick reasons
        inv.setItem(45, createKickReasonItem(Material.BOOK, "§6Language", "Inappropriate language"));
        inv.setItem(46, createKickReasonItem(Material.IRON_SWORD, "§cBehavior", "Disruptive behavior"));
        inv.setItem(47, createKickReasonItem(Material.REDSTONE_TORCH, "§eSpam", "Chat/spam violations"));
        inv.setItem(48, createKickReasonItem(Material.PAPER, "§aWarning", "Warning kick"));
        inv.setItem(49, createKickReasonItem(Material.BARRIER, "§cCustom", "Custom reason"));

        // Navigation
        if (page > 1) {
            inv.setItem(50, createNavItem(Material.ARROW, "§fPrevious Page"));
        }
        if (endIndex < players.size()) {
            inv.setItem(51, createNavItem(Material.ARROW, "§fNext Page"));
        }

        // Mass kick options
        inv.setItem(52, createMassActionItem(Material.TNT, "§4Kick All Non-Staff", "Kick all non-staff players"));

        inv.setItem(53, createNavItem(Material.ARROW, "§7Back"));

        admin.openInventory(inv);
    }

    private ItemStack createKickReasonItem(Material mat, String name, String reason) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + reason),
                Component.text("§8Quick kick with this reason")
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

    private ItemStack createMassActionItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
        i.setItemMeta(meta);
        return i;
    }
}