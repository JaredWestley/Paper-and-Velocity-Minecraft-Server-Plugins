// File: me/atomoyo/atomhub/gui/PerformanceMenu.java
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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.*;

public class PerformanceMenu {

    private final Player player;
    private final AtomHub plugin;
    private final DecimalFormat df = new DecimalFormat("0.00");

    public PerformanceMenu(Player player, AtomHub plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("PERFORMANCE"), 54,
                Component.text("§8Performance Monitoring"));

        // Get performance data
        double[] tps = Bukkit.getTPS();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long freeMemory = maxMemory - usedMemory;
        double memoryPercent = ((double) usedMemory / maxMemory) * 100;

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();

        int totalChunks = Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getLoadedChunks().length)
                .sum();

        // Header Info
        inv.setItem(4, createInfoItem(Material.NETHER_STAR, "§6§lLive Performance",
                "§7TPS: §f" + df.format(tps[0]) + "/" + df.format(tps[1]) + "/" + df.format(tps[2]),
                "§7Memory: §f" + usedMemory + "MB/" + maxMemory + "MB",
                "§7CPU Load: §f" + (cpuLoad >= 0 ? df.format(cpuLoad) : "N/A"),
                "§7Entities: §f" + Bukkit.getWorlds().stream()
                        .flatMap(w -> w.getEntities().stream()).count(),
                "§7Chunks Loaded: §f" + totalChunks,
                "§7Players Online: §f" + Bukkit.getOnlinePlayers().size()));

        // Row 1: TPS Monitoring
        inv.setItem(10, createTPSItem(tps[0], "1m TPS", "§aExcellent (>18)"));
        inv.setItem(11, createTPSItem(tps[1], "5m TPS", "§aGood (>15)"));
        inv.setItem(12, createTPSItem(tps[2], "15m TPS", "§aStable (>12)"));

        // Row 2: Memory Management
        inv.setItem(19, createMemoryItem(usedMemory, maxMemory, memoryPercent));
        inv.setItem(20, createPerformanceAction(Material.REDSTONE_LAMP, "§eLag Prevention",
                "Configure anti-lag measures", "lag"));
        inv.setItem(21, createPerformanceAction(Material.ENDER_CHEST, "§5Memory Clean",
                "Clear unused memory", "memory"));
        inv.setItem(22, createPerformanceAction(Material.ANVIL, "§cEntity Cleanup",
                "Remove excess entities", "entities"));
        inv.setItem(23, createPerformanceAction(Material.IRON_BARS, "§7Chunk Control",
                "Manage loaded chunks", "chunks"));
        inv.setItem(24, createPerformanceAction(Material.CLOCK, "§bAuto-Save",
                "Configure auto-save", "autosave"));
        inv.setItem(25, createPerformanceAction(Material.SPYGLASS, "§3View Distance",
                "Set view distance", "viewdistance"));

        // Row 3: Advanced Monitoring
        inv.setItem(28, createMonitoringItem(Material.COMPARATOR, "§aTPS History",
                "View TPS over time", "tpshistory"));
        inv.setItem(29, createMonitoringItem(Material.MAP, "§eTimings Report",
                "Open timings report", "timings"));
        inv.setItem(30, createMonitoringItem(Material.BEACON, "§dOptimization Tips",
                "Get optimization tips", "tips"));

        // Row 4: Maintenance Actions
        inv.setItem(37, createDangerItem(Material.TNT, "§c§lEMERGENCY CLEAN",
                "Remove all ground items"));
        inv.setItem(38, createDangerItem(Material.BARRIER, "§4§lKILL ALL MOBS",
                "Remove all hostile mobs"));
        inv.setItem(39, createDangerItem(Material.FIRE_CHARGE, "§6§lCLEAR DROPS",
                "Clear all item drops"));
        inv.setItem(40, createWarningItem(Material.REDSTONE_TORCH, "§e§lRESTART IF NEEDED",
                "Auto-restart if TPS < 10"));
        inv.setItem(41, createWarningItem(Material.LAVA_BUCKET, "§6§lFLUSH CHUNKS",
                "Unload unused chunks"));
        inv.setItem(42, createSafeItem(Material.WATER_BUCKET, "§a§lSOFT CLEANUP",
                "Safe cleanup operations"));

        // Back button
        inv.setItem(53, createNavItem(Material.ARROW, "§7Back to Server Menu"));

        player.openInventory(inv);
    }

    private ItemStack createTPSItem(double tpsValue, String label, String status) {
        Material mat;
        String color;

        if (tpsValue >= 18) {
            mat = Material.LIME_WOOL;
            color = "§a";
        } else if (tpsValue >= 15) {
            mat = Material.YELLOW_WOOL;
            color = "§e";
        } else if (tpsValue >= 12) {
            mat = Material.ORANGE_WOOL;
            color = "§6";
        } else {
            mat = Material.RED_WOOL;
            color = "§c";
        }

        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(color + label));
        meta.lore(Arrays.asList(
                Component.text("§7Current: " + color + df.format(tpsValue)),
                Component.text("§8" + status),
                Component.text(""),
                Component.text("§eClick for details")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createMemoryItem(long used, long max, double percent) {
        Material mat;
        String color;

        if (percent < 50) {
            mat = Material.LIME_STAINED_GLASS_PANE;
            color = "§a";
        } else if (percent < 75) {
            mat = Material.YELLOW_STAINED_GLASS_PANE;
            color = "§e";
        } else if (percent < 90) {
            mat = Material.ORANGE_STAINED_GLASS_PANE;
            color = "§6";
        } else {
            mat = Material.RED_STAINED_GLASS_PANE;
            color = "§c";
        }

        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(color + "Memory Usage"));
        meta.lore(Arrays.asList(
                Component.text("§7Used: §f" + used + "MB"),
                Component.text("§7Max: §f" + max + "MB"),
                Component.text("§7Free: §f" + (max - used) + "MB"),
                Component.text("§7Usage: " + color + df.format(percent) + "%"),
                Component.text(""),
                Component.text("§aLeft-click: Force GC"),
                Component.text("§eRight-click: Memory graph")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createPerformanceAction(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Action: " + action),
                Component.text("§eClick to configure")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createMonitoringItem(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Action: " + action),
                Component.text("§aClick to view"),
                Component.text("§eShift-click for advanced")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createDangerItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§4§lWARNING: Can cause lag!"),
                Component.text("§cShift + Click to execute")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createWarningItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§6§lWARNING: Temporary solution!"),
                Component.text("§eClick to configure")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createSafeItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§a§lSAFE: No performance impact"),
                Component.text("§aClick to execute")
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