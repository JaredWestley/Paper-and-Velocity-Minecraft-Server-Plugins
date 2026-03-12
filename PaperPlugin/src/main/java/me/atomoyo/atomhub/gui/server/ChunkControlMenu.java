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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkControlMenu {

    private final AtomHub plugin;
    private final Player player;

    public ChunkControlMenu(AtomHub plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("CHUNK_CONTROL"), 45,
                Component.text("§8Chunk Control"));

        int totalChunks = 0;
        int entities = 0;
        int tiles = 0;
        int emptyChunks = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            totalChunks += w.getLoadedChunks().length;
            for (org.bukkit.Chunk c : w.getLoadedChunks()) {
                entities += c.getEntities().length;
                tiles += c.getTileEntities().length;
                if (c.getEntities().length == 0 && c.getTileEntities().length == 0) {
                    emptyChunks++;
                }
            }
        }

        // Header with stats
        inv.setItem(4, createInfoItem(Material.CHEST, "§6§lChunk Statistics",
                "§7Total Chunks: §f" + totalChunks,
                "§7Total Entities: §f" + entities,
                "§7Tile Entities: §f" + tiles,
                "§7Empty Chunks: §f" + emptyChunks));

        // Chunk unloading options
        inv.setItem(10, createActionItem(Material.REDSTONE, "§cUnload Empty Chunks",
                "§7Remove chunks with no entities"));
        inv.setItem(11, createActionItem(Material.REDSTONE_BLOCK, "§4Unload Far Chunks",
                "§7Unload chunks far from spawn"));
        inv.setItem(12, createActionItem(Material.TNT, "§cUnload All Chunks",
                "§7WARNING: Dangerous!"));

        // Force loading options
        inv.setItem(19, createActionItem(Material.LAPIS_BLOCK, "§9Keep Spawn Loaded",
                "§7Keep spawn chunks always loaded"));
        inv.setItem(20, createActionItem(Material.DIAMOND_BLOCK, "§bForce Load Spawn",
                "§7Force load spawn chunks"));
        inv.setItem(21, createActionItem(Material.EMERALD_BLOCK, "§aPreload Chunks",
                "§7Preload chunks around players"));

        // Regeneration options
        inv.setItem(28, createActionItem(Material.COMPARATOR, "§eRegenerate Spawn",
                "§7Regenerate spawn chunks"));
        inv.setItem(29, createActionItem(Material.REPEATER, "§6Fix Lighting",
                "§7Fix chunk lighting issues"));
        inv.setItem(30, createActionItem(Material.TORCH, "§aRespawn Chunks",
                "§7Respawn nearby chunks"));

        // World specific
        inv.setItem(37, createInfoItem(Material.GLOBE_BANNER_PATTERN, "§6§lWorlds",
                "§7Worlds loaded: §f" + Bukkit.getWorlds().size()));
        inv.setItem(38, createActionItem(Material.COMPASS, "§eWorld List",
                "§7View all loaded worlds"));
        inv.setItem(39, createActionItem(Material.MAP, "§5Teleport to Chunk",
                "§7Teleport to specific chunk"));

        // Refresh and info
        inv.setItem(40, createActionItem(Material.ENDER_EYE, "§bRefresh Stats",
                "§7Update chunk statistics"));

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
            case REDSTONE -> unloadEmptyChunks();
            case REDSTONE_BLOCK -> unloadFarChunks();
            case TNT -> {
                player.sendMessage(Component.text("§c[AtomHub] - §7Unloading ALL chunks is extremely dangerous!"));
                player.sendMessage(Component.text("§7This can cause server crashes and world corruption!"));
                player.sendMessage(Component.text("§cUse §f/flush §cinstead for safe chunk management"));
            }

            case LAPIS_BLOCK -> toggleSpawnChunks();
            case DIAMOND_BLOCK -> forceLoadSpawn();
            case EMERALD_BLOCK -> preloadChunks();

            case COMPARATOR -> {
                player.sendMessage(Component.text("§6[AtomHub] - §7Spawn regeneration is disabled for safety"));
                player.sendMessage(Component.text("§7Use §f/regen §7command instead"));
            }
            case REPEATER -> fixLighting();
            case TORCH -> respawnChunks();

            case COMPASS -> showWorldList();
            case MAP -> {
                player.sendMessage(Component.text("§6[AtomHub] - §7Enter chunk coordinates in chat: X Z"));
                player.closeInventory();
            }

            case ENDER_EYE -> open();

            default -> open();
        }
    }

    private void unloadEmptyChunks() {
        int unloaded = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk c : w.getLoadedChunks()) {
                if (c.getEntities().length == 0 && c.getTileEntities().length == 0) {
                    c.unload(true);
                    unloaded++;
                }
            }
        }
        player.sendMessage(Component.text("§6[AtomHub] - §7Unloaded §f" + unloaded + "§7 empty chunks"));
        open();
    }

    private void unloadFarChunks() {
        if (Bukkit.getWorlds().isEmpty()) {
            player.sendMessage(Component.text("§c[AtomHub] - §7No worlds loaded"));
            return;
        }
        
        org.bukkit.World world = player.getWorld();
        org.bukkit.Location spawn = world.getSpawnLocation();
        int unloaded = 0;
        int distance = 5; // chunks from spawn
        
        for (org.bukkit.Chunk c : world.getLoadedChunks()) {
            int cx = c.getX() * 16 + 8;
            int cz = c.getZ() * 16 + 8;
            double dist = Math.sqrt(Math.pow(cx - spawn.getX(), 2) + Math.pow(cz - spawn.getZ(), 2));
            
            if (dist > distance * 16 && c.getEntities().length == 0) {
                c.unload(true);
                unloaded++;
            }
        }
        
        player.sendMessage(Component.text("§6[AtomHub] - §7Unloaded §f" + unloaded + "§7 chunks far from spawn"));
        open();
    }

    private void toggleSpawnChunks() {
        boolean current = plugin.getConfig().getBoolean("chunk-control.keep-spawn-loaded", true);
        plugin.getConfig().set("chunk-control.keep-spawn-loaded", !current);
        plugin.saveConfig();
        
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            w.setKeepSpawnInMemory(!current);
        }
        
        player.sendMessage(Component.text("§6[AtomHub] - §7Spawn chunks " + 
            (!current ? "§anow kept loaded" : "§cno longer kept loaded")));
        open();
    }

    private void forceLoadSpawn() {
        org.bukkit.World world = player.getWorld();
        org.bukkit.Location spawn = world.getSpawnLocation();
        
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                world.getChunkAt(spawn.getChunk().getX() + x, spawn.getChunk().getZ() + z);
            }
        }
        
        player.sendMessage(Component.text("§6[AtomHub] - §7Force loaded spawn chunks"));
        open();
    }

    private void preloadChunks() {
        int loaded = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            org.bukkit.Chunk chunk = p.getLocation().getChunk();
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z).load()) {
                        loaded++;
                    }
                }
            }
        }
        
        player.sendMessage(Component.text("§6[AtomHub] - §7Preloaded §f" + loaded + "§7 chunks around players"));
        open();
    }

    private void fixLighting() {
        int fixed = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk c : w.getLoadedChunks()) {
                c.unload(false);
                c.load(false);
                fixed++;
            }
        }
        
        player.sendMessage(Component.text("§6[AtomHub] - §7Fixed lighting for §f" + fixed + "§7 chunks"));
        open();
    }

    private void respawnChunks() {
        player.sendMessage(Component.text("§6[AtomHub] - §7Respawning chunks around you..."));
        
        org.bukkit.Chunk chunk = player.getLocation().getChunk();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                chunk.getWorld().refreshChunk(chunk.getX() + x, chunk.getZ() + z);
            }
        }
        
        player.sendMessage(Component.text("§6[AtomHub] - §7Respawned chunks"));
        open();
    }

    private void showWorldList() {
        player.sendMessage(Component.text("§6§lLoaded Worlds"));
        player.sendMessage(Component.text("§7────────────────────"));
        
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            int chunks = w.getLoadedChunks().length;
            int entities = w.getEntities().size();
            String env = w.getEnvironment().name();
            
            player.sendMessage(Component.text("§e" + w.getName() + " §7- §f" + chunks + "§7 chunks, §f" + entities + "§7 entities (§f" + env + "§7)"));
        }
        
        player.sendMessage(Component.text("§7────────────────────"));
        open();
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

    private ItemStack createActionItem(Material mat, String name, String desc) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text(desc),
                Component.text("§eClick to execute")
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
}
