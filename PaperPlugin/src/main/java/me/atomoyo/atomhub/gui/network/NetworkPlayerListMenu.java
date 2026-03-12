package me.atomoyo.atomhub.gui.network;

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
import java.util.List;

public class NetworkPlayerListMenu {

    private final Player admin;
    private final int page;

    public NetworkPlayerListMenu(Player admin, int page) {
        this.admin = admin;
        this.page = page;
    }

    public void open() {

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        int perPage = 21;
        int maxPage = (int) Math.ceil((double) players.size() / perPage);

        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_PLAYERLIST"), 54, Component.text("Player List (Page " + page + ")"));


        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, players.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            Player p = players.get(i);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(p);
            meta.displayName(Component.text(p.getName()));
            head.setItemMeta(meta);

            inv.setItem(slot++, head);
        }

        // Navigation
        if (page > 1) inv.setItem(21, nav(Material.ARROW, "Previous Page"));
        if (page < maxPage) inv.setItem(23, nav(Material.ARROW, "Next Page"));

        // Back Button
        inv.setItem(22, nav(Material.ARROW, "§7Back to Network Menu"));

        admin.openInventory(inv);
    }

    private ItemStack nav(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta(); // use ItemMeta, not SkullMeta
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }

}
