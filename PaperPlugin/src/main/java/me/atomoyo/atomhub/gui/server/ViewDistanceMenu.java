// File: me/atomoyo/atomhub/gui/ViewDistanceMenu.java
package me.atomoyo.atomhub.gui.server;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ViewDistanceMenu {

    private final Player admin;
    private final AtomHub plugin;
    private final int currentServerViewDistance;

    public ViewDistanceMenu(Player admin, AtomHub plugin) {
        this.admin = admin;
        this.plugin = plugin;
        // Get default world's view distance as server baseline
        this.currentServerViewDistance = Bukkit.getWorlds().get(0).getViewDistance();
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("VIEW_DISTANCE"), 54,
                Component.text("§8View Distance Configuration"));

        // Get simulation distance (if available on this server version)
        int simulationDistance = 0;
        try {
            simulationDistance = Bukkit.getWorlds().get(0).getSimulationDistance();
        } catch (NoSuchMethodError e) {
            // Method not available on older versions
            simulationDistance = -1;
        }

        // Current status
        List<String> infoLines = new ArrayList<>();
        infoLines.add("§7Default World View Distance: §f" + currentServerViewDistance);
        if (simulationDistance != -1) {
            infoLines.add("§7Simulation Distance: §f" + simulationDistance);
        }
        infoLines.add("§7Recommended: §a2-10§7 (higher = more lag)");
        infoLines.add("");
        infoLines.add("§eEach chunk = 16x16 blocks");
        infoLines.add("§eHigher values: Better visuals, More lag");
        infoLines.add("§eLower values: Less lag, Pop-in effect");

        inv.setItem(4, createInfoItem(Material.SPYGLASS, "§6§lCurrent Settings",
                infoLines.toArray(new String[0])));

        // Quick presets (Row 2)
        inv.setItem(10, createViewDistancePreset(1, "§7Ultra Low", Material.BARRIER,
                "§7Extreme lag prevention", "§cVery limited visibility"));
        inv.setItem(11, createViewDistancePreset(2, "§7Very Low", Material.GRAY_WOOL,
                "§7Low-end servers", "§cPoor visuals"));
        inv.setItem(12, createViewDistancePreset(4, "§aLow", Material.LIME_WOOL,
                "§7Balanced performance", "§aGood for survival"));
        inv.setItem(13, createViewDistancePreset(6, "§6Normal", Material.ORANGE_WOOL,
                "§7Default setting", "§eBest balance"));
        inv.setItem(14, createViewDistancePreset(8, "§eHigh", Material.YELLOW_WOOL,
                "§7Good visuals", "§eNoticeable lag"));
        inv.setItem(15, createViewDistancePreset(10, "§cVery High", Material.RED_WOOL,
                "§7Maximum view", "§cHigh server load"));
        inv.setItem(16, createViewDistancePreset(12, "§5Extreme", Material.PURPLE_WOOL,
                "§7For powerful servers", "§cVery high lag"));

        // Per-world configuration (Row 3)
        int slot = 19;
        for (World world : Bukkit.getWorlds()) {
            if (slot > 25) break;

            Material icon = getWorldIcon(world);
            int worldViewDistance = world.getViewDistance();

            inv.setItem(slot, createWorldViewItem(icon, world.getName(), worldViewDistance));
            slot++;
        }

        // Advanced settings (Row 4)
        inv.setItem(28, createToggleItem(Material.OBSERVER, "§ePer-Player View Distance",
                "Allow players to set own distance", "player"));
        inv.setItem(29, createToggleItem(Material.COMPARATOR, "§bDynamic View Distance",
                "Auto-adjust based on TPS", "dynamic"));
        inv.setItem(30, createNumberItem(Material.PAPER, "§fCustom Value",
                "Set custom view distance", "custom"));
        inv.setItem(31, createActionItem(Material.REDSTONE_TORCH, "§6Reset All Worlds",
                "Reset all worlds to default", "reset"));
        inv.setItem(32, createActionItem(Material.BOOK, "§dView Distance Calculator",
                "Calculate chunk loading impact", "calculator"));
        inv.setItem(33, createActionItem(Material.CLOCK, "§eSchedule Changes",
                "Schedule automatic adjustments", "schedule"));

        // Performance impact (Row 5)
        inv.setItem(37, createImpactItem(currentServerViewDistance - 2, "§cReduce (-2)",
                "Reduces server load", "decrease-2"));
        inv.setItem(38, createImpactItem(currentServerViewDistance - 1, "§6Reduce (-1)",
                "Slight performance gain", "decrease-1"));
        inv.setItem(39, createImpactItem(currentServerViewDistance, "§eCurrent",
                "No change", "current"));
        inv.setItem(40, createImpactItem(currentServerViewDistance + 1, "§aIncrease (+1)",
                "Better visuals", "increase-1"));
        inv.setItem(41, createImpactItem(currentServerViewDistance + 2, "§2Increase (+2)",
                "Significantly better visuals", "increase-2"));

        // World-specific actions
        inv.setItem(45, createActionItem(Material.GLOBE_BANNER_PATTERN, "§bApply to All Worlds",
                "Apply current distance to all worlds", "apply-all"));
        inv.setItem(46, createActionItem(Material.COMPASS, "§eTest Current Settings",
                "Teleport to test area", "test"));
        inv.setItem(47, createWarningItem(Material.TNT, "§c§lEmergency Reduce",
                "Immediately reduce view distance"));
        inv.setItem(48, createActionItem(Material.ENDER_EYE, "§5Monitor Impact",
                "Monitor performance impact", "monitor"));
        inv.setItem(49, createInfoItem(Material.LECTERN, "§6Chunk Statistics",
                "§7Current chunks loaded: §f" + getTotalLoadedChunks(),
                "§7Estimated with +1: §f" + estimateChunks(currentServerViewDistance + 1),
                "§7Estimated with -1: §f" + estimateChunks(currentServerViewDistance - 1),
                "§7Players affected: §f" + Bukkit.getOnlinePlayers().size()));

        // Back button
        inv.setItem(53, createNavItem(Material.ARROW, "§7Back to Server Menu"));

        admin.openInventory(inv);
    }

    private ItemStack createViewDistancePreset(int distance, String name, Material mat, String... description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();

        boolean isCurrent = distance == currentServerViewDistance;
        String status = isCurrent ? "§a● CURRENT" : "§7● SET";

        meta.displayName(Component.text(name + " (§f" + distance + "§7)"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7View Distance: §f" + distance + " chunks"));
        lore.add(Component.text("§7Blocks visible: §f" + (distance * 16) + " blocks"));
        for (String line : description) {
            lore.add(Component.text(line));
        }
        lore.add(Component.text(""));
        lore.add(Component.text(status));
        lore.add(Component.text("§8Action: set-" + distance));
        lore.add(Component.text(isCurrent ? "§7Already set" : "§eClick to apply"));

        meta.lore(lore);
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createWorldViewItem(Material icon, String worldName, int viewDistance) {
        ItemStack i = new ItemStack(icon);
        ItemMeta meta = i.getItemMeta();

        String statusColor = viewDistance >= 8 ? "§c" : viewDistance >= 6 ? "§e" : "§a";

        meta.displayName(Component.text("§f" + worldName));
        meta.lore(Arrays.asList(
                Component.text("§7Current View Distance: " + statusColor + viewDistance),
                Component.text("§7Players: §f" + Bukkit.getWorld(worldName).getPlayers().size()),
                Component.text("§7Environment: §f" + getEnvironmentName(Bukkit.getWorld(worldName))),
                Component.text(""),
                Component.text("§aLeft-click: Increase"),
                Component.text("§cRight-click: Decrease"),
                Component.text("§eMiddle-click: Set to default"),
                Component.text("§8Action: world-" + worldName)
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createToggleItem(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Action: toggle-" + action),
                Component.text("§eClick to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createNumberItem(Material mat, String name, String description, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Current: §f" + currentServerViewDistance),
                Component.text("§8Action: " + action),
                Component.text("§eClick to enter custom value")
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

    private ItemStack createImpactItem(int distance, String name, String description, String action) {
        ItemStack i = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = i.getItemMeta();

        // Clamp distance to valid range
        distance = Math.max(2, Math.min(32, distance));

        meta.displayName(Component.text(name + " (§f" + distance + "§7)"));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§7New distance: §f" + distance + " chunks"),
                Component.text("§7Impact: " + getImpactLevel(currentServerViewDistance, distance)),
                Component.text(""),
                Component.text("§8Action: " + action),
                Component.text("§eClick to preview")
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
                Component.text("§4§lWARNING: Causes chunk reloading!"),
                Component.text("§cShift + Click to execute")
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

    // Utility methods
    private Material getWorldIcon(World world) {
        switch (world.getEnvironment()) {
            case NORMAL: return Material.GRASS_BLOCK;
            case NETHER: return Material.NETHERRACK;
            case THE_END: return Material.END_STONE;
            case CUSTOM: return Material.MAP;
            default: return Material.GRASS_BLOCK;
        }
    }

    private String getEnvironmentName(World world) {
        switch (world.getEnvironment()) {
            case NORMAL: return "Overworld";
            case NETHER: return "Nether";
            case THE_END: return "The End";
            case CUSTOM: return "Custom";
            default: return "Unknown";
        }
    }

    private int getTotalLoadedChunks() {
        return Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getLoadedChunks().length)
                .sum();
    }

    private int estimateChunks(int newDistance) {
        int players = Bukkit.getOnlinePlayers().size();
        // Rough estimate: each player loads approximately (2d+1)^2 chunks
        return players * (int)Math.pow(2 * newDistance + 1, 2);
    }

    private String getImpactLevel(int oldDist, int newDist) {
        if (newDist > oldDist) {
            int diff = newDist - oldDist;
            if (diff >= 3) return "§c§lHIGH (Lag likely)";
            if (diff == 2) return "§6MEDIUM (Some lag)";
            return "§eLOW (Minor impact)";
        } else {
            int diff = oldDist - newDist;
            if (diff >= 3) return "§a§lHIGH (Big performance gain)";
            if (diff == 2) return "§2MEDIUM (Good gain)";
            return "§aLOW (Small gain)";
        }
    }

    // Public method to handle view distance changes
    public static void setAllWorldsViewDistance(int distance) {
        distance = Math.max(2, Math.min(32, distance)); // Clamp to valid range

        for (World world : Bukkit.getWorlds()) {
            setWorldViewDistance(world, distance);
        }
    }

    public static void setWorldViewDistance(World world, int distance) {
        distance = Math.max(2, Math.min(32, distance));
        world.setViewDistance(distance);

        // Try to set simulation distance if available (Paper/Spigot 1.17+)
        try {
            int simulationDistance = Math.min(distance, 10); // Max 10 for simulation
            world.setSimulationDistance(simulationDistance);
        } catch (NoSuchMethodError e) {
            // Method not available on older versions
        }
    }
}