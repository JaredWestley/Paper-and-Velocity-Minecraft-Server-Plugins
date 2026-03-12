package me.atomoyo.atomhub.gui.server;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LagPreventionMenu {

    private final AtomHub plugin;
    private final Player player;

    public LagPreventionMenu(AtomHub plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("LAG_PREVENTION"), 45,
                Component.text("§8Lag Prevention Settings"));

        boolean entityLimitEnabled = plugin.getConfig().getBoolean("lag-prevention.entity-limit.enabled", true);
        boolean mobCapEnabled = plugin.getConfig().getBoolean("lag-prevention.mob-cap.enabled", true);
        boolean hopperLimitEnabled = plugin.getConfig().getBoolean("lag-prevention.hopper-limit.enabled", false);
        boolean tickLimiterEnabled = plugin.getConfig().getBoolean("lag-prevention.tick-limiter.enabled", false);

        int entityLimit = plugin.getConfig().getInt("lag-prevention.entity-limit.per-chunk", 30);
        int mobCap = plugin.getConfig().getInt("lag-prevention.mob-cap.per-world", 100);
        int hopperLimit = plugin.getConfig().getInt("lag-prevention.hopper-limit.per-chunk", 1);

        // Header info
        inv.setItem(4, createInfoItem(Material.BARRIER, "§6§lLag Prevention",
                "§7Configure entity and performance limits",
                "§7Current Server: §f" + Bukkit.getServer().getName()));

        // Entity limits section
        inv.setItem(10, createToggleItem(Material.ZOMBIE_HEAD, "§cEntity Limit",
                "§7Limit entities per chunk", entityLimitEnabled));
        inv.setItem(11, createValueItem(Material.PAPER, "§f" + entityLimit,
                "§7Entities per chunk"));
        inv.setItem(12, createActionItem(Material.ACACIA_SIGN, "§7-5", "§eDecrease by 5"));
        inv.setItem(13, createActionItem(Material.BIRCH_SIGN, "§a+5", "§aIncrease by 5"));

        // Mob cap section
        inv.setItem(19, createToggleItem(Material.SKELETON_SKULL, "§eMob Cap",
                "§7Limit mobs per world", mobCapEnabled));
        inv.setItem(20, createValueItem(Material.SPAWNER, "§f" + mobCap,
                "§7Mobs per world"));
        inv.setItem(21, createActionItem(Material.DARK_OAK_SIGN, "§7-25", "§eDecrease by 25"));
        inv.setItem(22, createActionItem(Material.JUNGLE_SIGN, "§a+25", "§aIncrease by 25"));

        // Hopper limit section
        inv.setItem(28, createToggleItem(Material.HOPPER, "§bHopper Limit",
                "§7Limit hoppers per chunk", hopperLimitEnabled));
        inv.setItem(29, createValueItem(Material.HOPPER_MINECART, "§f" + hopperLimit,
                "§7Hoppers per chunk"));
        inv.setItem(30, createActionItem(Material.OAK_SIGN, "§7-1", "§eDecrease by 1"));
        inv.setItem(31, createActionItem(Material.SPRUCE_SIGN, "§a+1", "§aIncrease by 1"));

        // Tick limiter section
        inv.setItem(37, createToggleItem(Material.CLOCK, "§4Tick Limiter",
                "§7Limit tick time (experimental)", tickLimiterEnabled));

        // Quick actions
        inv.setItem(19, createActionItem(Material.TNT, "§c§lEMERGENCY CLEAN",
                "§7Remove all items and mobs"));
        inv.setItem(20, createActionItem(Material.IRON_SWORD, "§eKill Mobs",
                "§7Kill all hostile mobs"));
        inv.setItem(21, createActionItem(Material.ARMOR_STAND, "§aClear Items",
                "§7Remove all dropped items"));

        // Status info
        inv.setItem(40, createInfoItem(Material.CHEST, "§6§lCurrent Status",
                "§7Loaded Chunks: §f" + getTotalChunks(),
                "§7Total Entities: §f" + getTotalEntities(),
                "§7Players: §f" + Bukkit.getOnlinePlayers().size()));

        // Back button
        inv.setItem(44, createNavItem(Material.KNOWLEDGE_BOOK, "§7Back"));

        player.openInventory(inv);
    }

    public void handleClick(Material clicked) {
        if (clicked == Material.KNOWLEDGE_BOOK) {
            new PerformanceMenu(player, plugin).open();
            return;
        }

        switch (clicked) {
            case ZOMBIE_HEAD -> toggleSetting("lag-prevention.entity-limit.enabled");
            case SKELETON_SKULL -> toggleSetting("lag-prevention.mob-cap.enabled");
            case HOPPER -> toggleSetting("lag-prevention.hopper-limit.enabled");
            case CLOCK -> toggleSetting("lag-prevention.tick-limiter.enabled");

            case ACACIA_SIGN -> adjustValue("lag-prevention.entity-limit.per-chunk", -5);
            case BIRCH_SIGN -> adjustValue("lag-prevention.entity-limit.per-chunk", 5);
            case DARK_OAK_SIGN -> adjustValue("lag-prevention.mob-cap.per-world", -25);
            case JUNGLE_SIGN -> adjustValue("lag-prevention.mob-cap.per-world", 25);
            case OAK_SIGN -> adjustValue("lag-prevention.hopper-limit.per-chunk", -1);
            case SPRUCE_SIGN -> adjustValue("lag-prevention.hopper-limit.per-chunk", 1);

            case TNT -> emergencyClean();
            case IRON_SWORD -> killMobs();
            case ARMOR_STAND -> clearItems();

            default -> open();
        }
    }

    private void toggleSetting(String path) {
        boolean current = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !current);
        plugin.saveConfig();
        open();
        String name = path.replace("lag-prevention.", "").replace(".enabled", "");
        player.sendMessage(Component.text("§6[AtomHub] - §7" + name + " " + 
            (!current ? "§aenabled" : "§cdisabled")));
    }

    private void adjustValue(String path, int amount) {
        int current = plugin.getConfig().getInt(path, 30);
        int newValue = Math.max(1, current + amount);
        plugin.getConfig().set(path, newValue);
        plugin.saveConfig();
        player.sendMessage(Component.text("§6[AtomHub] - §7Value changed to §f" + newValue));
        open();
    }

    private void emergencyClean() {
        int items = 0, mobs = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e instanceof org.bukkit.entity.Item) {
                    e.remove();
                    items++;
                } else if (e instanceof org.bukkit.entity.Monster) {
                    e.remove();
                    mobs++;
                }
            }
        }
        Bukkit.broadcast(Component.text("§c⚠ Emergency cleanup performed! §7Removed §f" + items + "§7 items and §f" + mobs + "§7 mobs"));
        player.sendMessage(Component.text("§6[AtomHub] - §7Removed §f" + items + "§7 items and §f" + mobs + "§7 mobs"));
        open();
    }

    private void killMobs() {
        int killed = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e instanceof org.bukkit.entity.Monster) {
                    e.remove();
                    killed++;
                }
            }
        }
        player.sendMessage(Component.text("§6[AtomHub] - §7Killed §f" + killed + "§7 hostile mobs"));
        open();
    }

    private void clearItems() {
        int cleared = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e instanceof org.bukkit.entity.Item) {
                    e.remove();
                    cleared++;
                }
            }
        }
        player.sendMessage(Component.text("§6[AtomHub] - §7Cleared §f" + cleared + "§7 dropped items"));
        open();
    }

    private int getTotalChunks() {
        return Bukkit.getWorlds().stream().mapToInt(w -> w.getLoadedChunks().length).sum();
    }

    private int getTotalEntities() {
        return Bukkit.getWorlds().stream().mapToInt(w -> w.getEntities().size()).sum();
    }

    private ItemStack createToggleItem(Material mat, String name, String desc, boolean enabled) {
        ItemStack i = new ItemStack(enabled ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text(desc),
                Component.text("§7Status: " + (enabled ? "§aEnabled" : "§cDisabled")),
                Component.text("§eClick to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createValueItem(Material mat, String value, String desc) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text("§fValue: " + value));
        meta.lore(Arrays.asList(
                Component.text(desc)
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createActionItem(Material mat, String name, String desc) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text(desc)
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createInfoItem(Material mat, String name, String... lines) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        List<Component> lore = new ArrayList<>();
        for (String line : lines) {
            lore.add(Component.text(line));
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
