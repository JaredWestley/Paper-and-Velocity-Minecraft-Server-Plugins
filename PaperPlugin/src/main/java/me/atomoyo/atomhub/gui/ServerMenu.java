package me.atomoyo.atomhub.gui;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.Arrays;

public class ServerMenu {

    private final Player player;
    private final AtomHub plugin;

    public ServerMenu(Player player, AtomHub plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("SERVER"), 54, Component.text("§8Server Management"));

        // Row 1: Server Control
        inv.setItem(10, createServerControlItem(Material.REDSTONE, "§cStop Server",
                "Safely shut down the server", "stop"));
        inv.setItem(11, createServerControlItem(Material.REPEATER, "§6Restart Server",
                "Restart the server", "restart"));
        inv.setItem(12, createServerControlItem(Material.BARRIER, "§4Force Stop",
                "Immediate shutdown", "force"));

        // Maintenance Mode toggle
        Material maintenanceMat = plugin.isMaintenanceMode() ? Material.REDSTONE_TORCH : Material.LEVER;
        String maintenanceStatus = plugin.isMaintenanceMode() ? "§aENABLED" : "§cDISABLED";
        inv.setItem(13, createToggleItem(maintenanceMat, "§eMaintenance Mode",
                "Only OPs can join", "maintenance", maintenanceStatus));

        inv.setItem(14, createServerControlItem(Material.WHITE_BANNER, "§fWhitelist",
                "Manage whitelist", "whitelist"));
        inv.setItem(15, createServerControlItem(Material.BOOK, "§6Server Info",
                "View server statistics", "info"));
        inv.setItem(16, createServerControlItem(Material.CLOCK, "§ePerformance",
                "Performance monitoring", "performance"));

        // Row 2: World Management
        inv.setItem(19, createWorldItem(Material.GRASS_BLOCK, "§aWorld Manager",
                "Manage server worlds", "worlds"));
        inv.setItem(20, createWorldItem(Material.BEDROCK, "§7Backup World",
                "Create world backup", "backup"));
        inv.setItem(21, createWorldItem(Material.TNT, "§cReset World",
                "Reset world to default", "reset"));
        inv.setItem(22, createWorldItem(Material.MAP, "§bWorld Borders",
                "Configure world borders", "borders"));
        inv.setItem(23, createWorldItem(Material.SPAWNER, "§8Mob Control",
                "Manage mob spawning", "mobs"));
        inv.setItem(24, createWorldItem(Material.CHEST, "§6Loot Control",
                "Configure loot tables", "loot"));
        inv.setItem(25, createWorldItem(Material.BEACON, "§dWorld Effects",
                "Global world effects", "effects"));

        // Row 3: Performance & Settings
        inv.setItem(28, createPerformanceItem(Material.COMPARATOR, "§aTPS Monitor",
                "Monitor server performance", "tps"));
        inv.setItem(29, createPerformanceItem(Material.REDSTONE_LAMP, "§eLag Prevention",
                "Anti-lag measures", "lag"));
        inv.setItem(32, createPerformanceItem(Material.IRON_BARS, "§7Chunk Control",
                "Manage loaded chunks", "chunks"));
        inv.setItem(33, createPerformanceItem(Material.ENDER_EYE, "§dAuto-Save",
                "Configure auto-save", "autosave"));

        // Row 4: Advanced Settings
        inv.setItem(37, createAdvancedItem(Material.COMMAND_BLOCK, "§6Command Control",
                "Manage server commands", "commands"));
        inv.setItem(38, createAdvancedItem(Material.ENCHANTING_TABLE, "§5Plugin Manager",
                "Manage plugins", "plugins"));
        inv.setItem(39, createAdvancedItem(Material.WRITABLE_BOOK, "§eConfig Editor",
                "Edit server configs", "config"));
        inv.setItem(40, createAdvancedItem(Material.NETHER_STAR, "§dServer Properties",
                "Edit server.properties", "properties"));
        inv.setItem(41, createAdvancedItem(Material.CLOCK, "§bLog Viewer",
                "View server logs", "logs"));
        inv.setItem(42, createAdvancedItem(Material.FIREWORK_ROCKET, "§cEmergency Tools",
                "Emergency server tools", "emergency"));

        // Info Panel
        inv.setItem(49, createInfoItem(Material.PAPER, "§6Server Status",
                "Online Players: §f" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(),
                "TPS: §f" + formatTPS(Bukkit.getTPS()),
                "Memory: §f" + getMemoryUsage(),
                "Uptime: §f" + formatUptime(),
                "Worlds: §f" + Bukkit.getWorlds().size(),
                "Difficulty: §f" + player.getWorld().getDifficulty(),
                "Time: §f" + formatTime(player.getWorld().getTime())));

        // Back button
        inv.setItem(53, createNavItem(Material.ARROW, "§7Back to Main Menu", "Return to main menu"));

        player.openInventory(inv);
    }

    private ItemStack createServerControlItem(Material mat, String name, String description, String action) {
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

    private ItemStack createWorldItem(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Action: " + action),
                Component.text("§eClick to open")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createPerformanceItem(Material mat, String name, String description, String action) {
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

    private ItemStack createAdvancedItem(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Action: " + action),
                Component.text("§eClick to open"),
                Component.text("§c§lRequires OP permissions")
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

    private ItemStack createNavItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
        i.setItemMeta(meta);
        return i;
    }

    // Utility methods for info display
    private String formatTPS(double[] tps) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(tps[0]) + "/" + df.format(tps[1]) + "/" + df.format(tps[2]);
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long max = runtime.maxMemory() / 1024 / 1024;
        long free = max - used;
        return used + "MB/" + max + "MB (Free: " + free + "MB)";
    }

    private String formatUptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    private String formatTime(long time) {
        long hours = (time / 1000 + 6) % 24;
        long minutes = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes) +
                (time < 13000 && time > 0 ? " (Day)" : " (Night)");
    }

    // You can also add these helper methods for creating specific items with colors
    private ItemStack createDangerItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§4§lWARNING: This action is irreversible!"),
                Component.text("§cShift + Click to confirm")
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
                Component.text("§6§lWARNING: Use with caution!"),
                Component.text("§eClick to proceed")
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
                Component.text("§a§lSAFE: This action is reversible"),
                Component.text("§aClick to use")
        ));
        i.setItemMeta(meta);
        return i;
    }
}