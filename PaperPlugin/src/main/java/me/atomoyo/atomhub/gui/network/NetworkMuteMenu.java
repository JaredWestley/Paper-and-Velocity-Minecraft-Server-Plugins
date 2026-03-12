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

public class NetworkMuteMenu {

    private final Player admin;
    private final AtomHub plugin;
    private int page;

    public NetworkMuteMenu(AtomHub plugin, Player admin, int page) {
        this.admin = admin;
        this.plugin = plugin;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_MUTE"), 54,
                Component.text("§8Mute Management - Page " + page));

        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .map(p -> (Player) p)
                .collect(java.util.stream.Collectors.toList());
        int playersPerPage = 45;
        int startIndex = (page - 1) * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, players.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Player target = players.get(i);
            boolean isMuted = plugin.getMuteManager().isNetworkMuted(target.getUniqueId());

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);

            String status = isMuted ? "§cMUTED" : "§aUNMUTED";
            Material statusMat = isMuted ? Material.RED_DYE : Material.LIME_DYE;

            meta.displayName(Component.text("§f" + target.getName() + " " + status));
            meta.lore(Arrays.asList(
                    Component.text("§7Status: " + status),
                    Component.text("§7Click to " + (isMuted ? "unmute" : "mute")),
                    Component.text("§7this player network-wide"),
                    Component.text(""),
                    Component.text("§8Left-click: Toggle mute"),
                    Component.text("§8Right-click: View mute history")
            ));
            head.setItemMeta(meta);
            inv.setItem(slot, head);
            slot++;
        }

        // Mute durations
        inv.setItem(45, createDurationItem(Material.CLOCK, "§e5 Minutes", "5 minute mute"));
        inv.setItem(46, createDurationItem(Material.CLOCK, "§630 Minutes", "30 minute mute"));
        inv.setItem(47, createDurationItem(Material.CLOCK, "§c1 Hour", "1 hour mute"));
        inv.setItem(48, createDurationItem(Material.CLOCK, "§41 Day", "24 hour mute"));
        inv.setItem(49, createDurationItem(Material.CLOCK, "§8Permanent", "Permanent mute"));

        // Mass mute options
        inv.setItem(50, createMassActionItem(Material.PAPER, "§6Mute All", "Mute all players"));
        inv.setItem(51, createMassActionItem(Material.MILK_BUCKET, "§fUnmute All", "Unmute all players"));

        // Navigation
        if (page > 1) {
            inv.setItem(52, createNavItem(Material.ARROW, "§fPrevious Page"));
        }
        if (endIndex < players.size()) {
            inv.setItem(53, createNavItem(Material.ARROW, "§fNext Page"));
        }

        admin.openInventory(inv);
    }

    private ItemStack createDurationItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
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

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }
}