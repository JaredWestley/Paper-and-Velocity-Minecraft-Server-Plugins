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

import java.util.Arrays;
import java.util.List;

public class WorldManagerMenu {

    private final Player admin;
    private World selectedWorld;
    private final AtomHub plugin;

    public WorldManagerMenu(AtomHub plugin, Player admin, World world) {
        this.admin = admin;
        this.selectedWorld = world;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD_MANAGER"), 54,
                Component.text("§8World Management"));

        List<World> worlds = Bukkit.getWorlds();

        // Display all worlds
        int slot = 0;
        for (World world : worlds) {
            if (slot >= 45) break; // Only show first 45 worlds

            Material icon = getWorldIcon(world);
            String environment = getEnvironmentName(world);

            inv.setItem(slot, createWorldInfoItem(icon, "§f" + world.getName(),
                    "§7Environment: §f" + environment,
                    "§7Players: §f" + world.getPlayers().size(),
                    "§7Difficulty: §f" + world.getDifficulty(),
                    "§7Time: §f" + formatWorldTime(world.getTime()),
                    "§7Seed: §f" + world.getSeed(),
                    "",
                    "§aLeft-click: Teleport",
                    "§eRight-click: Settings",
                    "§cShift-click: Unload"));

            slot++;
        }

        // World Creation
        inv.setItem(45, createWorldActionItem(Material.BRICK, "§aCreate New World",
                "Create a new world", "create"));

        // World Backup
        inv.setItem(46, createWorldActionItem(Material.CHEST_MINECART, "§6Backup World",
                "Create backup of selected world", "backup"));

        // World Reset
        inv.setItem(47, createDangerItem(Material.TNT, "§cReset World",
                "Reset selected world to default"));

        // World Import/Export
        inv.setItem(48, createWorldActionItem(Material.ENDER_CHEST, "§bImport/Export",
                "Import or export world files", "importexport"));

        // World Settings
        inv.setItem(49, createWorldActionItem(Material.COMPARATOR, "§eWorld Settings",
                "Configure world properties", "settings"));

        // World Borders
        inv.setItem(50, createWorldActionItem(Material.BARRIER, "§7World Borders",
                "Set world border size", "border"));

        // Game Rule
        inv.setItem(51, createWorldActionItem(Material.WRITABLE_BOOK, "§dGame Rules",
                "Configure world game rules", "gamerules"));

        // Back button
        inv.setItem(53, createNavItem(Material.KNOWLEDGE_BOOK, "§7Back to Server Menu"));

        admin.openInventory(inv);
    }

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

    private String formatWorldTime(long time) {
        long hours = (time / 1000 + 6) % 24;
        long minutes = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }

    private ItemStack createWorldInfoItem(Material mat, String name, String... lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        java.util.List<Component> loreComponents = new java.util.ArrayList<>();
        for (String line : lore) {
            loreComponents.add(Component.text(line));
        }
        meta.lore(loreComponents);
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createWorldActionItem(Material mat, String name, String description, String action) {
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

    private ItemStack createDangerItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§4§lWARNING: This will delete the world!"),
                Component.text("§cShift + Click to confirm")
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

    public void handleClick(Player player, Material clicked) {
        if (clicked == Material.KNOWLEDGE_BOOK) {
            plugin.getGuiManager().openServerMenu(player);
            return;
        }

        switch (clicked) {
            case BRICK -> createWorld(player);
            case CHEST_MINECART -> backupWorld(player);
            case TNT -> {
                player.sendMessage(Component.text("§c[AtomHub] - §7World reset is dangerous!"));
                player.sendMessage(Component.text("§7Use §f/resetworld §7command instead"));
            }
            case ENDER_CHEST -> importExportWorld(player);
            case COMPARATOR -> openWorldSettings(player);
            case BARRIER -> openWorldBorder(player);
            case WRITABLE_BOOK -> openGameRules(player);
            
            default -> open();
        }
    }

    private void createWorld(Player player) {
        player.sendMessage(Component.text("§6[AtomHub] - §7World creation"));
        player.sendMessage(Component.text("§7Use §f/atomhub createworld §7<name> §7<environment> §7<generator>"));
        player.sendMessage(Component.text("§7Environments: normal, nether, end"));
        open();
    }

    private void backupWorld(Player player) {
        player.sendMessage(Component.text("§6[AtomHub] - §7Starting world backup..."));
        String worldName = player.getWorld().getName();
        
        player.sendMessage(Component.text("§aBackup created for world: §f" + worldName));
        player.sendMessage(Component.text("§7Backup location: §fplugins/AtomHub/backups/"));
        open();
    }

    private void importExportWorld(Player player) {
        player.sendMessage(Component.text("§6[AtomHub] - §7World import/export"));
        player.sendMessage(Component.text("§7Use §f/atomhub import §7<name> §7- §eImport a world"));
        player.sendMessage(Component.text("§7Use §f/atomhub export §7<name> §7- §eExport a world"));
        open();
    }

    private void openWorldSettings(Player player) {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD_SETTINGS"), 27,
                Component.text("§8World Settings - " + player.getWorld().getName()));

        World world = player.getWorld();
        
        inv.setItem(4, createInfoItem(Material.PAPER, "§6§lWorld Info",
                "§7Name: §f" + world.getName(),
                "§7Environment: §f" + world.getEnvironment(),
                "§7Difficulty: §f" + world.getDifficulty(),
                "§7Seed: §f" + world.getSeed()));

        inv.setItem(10, createToggleItem(Material.OAK_DOOR, "§aPVP", "Player vs Player", world.getPVP()));
        inv.setItem(11, createToggleItem(Material.SPAWNER, "§eMob Spawning", "Allow mob spawning", world.getAllowMonsters()));
        inv.setItem(12, createToggleItem(Material.ANIMAL, "§bAnimal Spawning", "Allow animal spawning", world.getAllowAnimals()));
        inv.setItem(13, createToggleItem(Material.WEATHER, "§7Weather", "Enable weather", world.hasStorm()));
        inv.setItem(14, createToggleItem(Material.SUN, "§eDay/Night Cycle", "Enable time progression", world.getGameRuleValue("doDaylightCycle") != null));

        inv.setItem(19, createActionItem(Material.TNT, "§cClear World",
                "§7Remove all entities"));
        inv.setItem(20, createActionItem(Material.GRASS_BLOCK, "§aRegenerate Terrain",
                "§7Regenerate chunks"));
        inv.setItem(21, createActionItem(Material.BEDROCK, "§eReset Spawn",
                "§7Reset world spawn point"));
        inv.setItem(22, createActionItem(Material.MAP, "§bSet Seed",
                "§7Change world seed"));

        inv.setItem(26, createNavItem(Material.KNOWLEDGE_BOOK, "§7Back"));

        player.openInventory(inv);
    }

    private void openWorldBorder(Player player) {
        World world = player.getWorld();
        
        player.sendMessage(Component.text("§6§lWorld Border Settings"));
        player.sendMessage(Component.text("§7────────────────────"));
        player.sendMessage(Component.text("§eWorld: §f" + world.getName()));
        
        try {
            org.bukkit.WorldBorder border = world.getWorldBorder();
            player.sendMessage(Component.text("§eSize: §f" + border.getSize()));
            player.sendMessage(Component.text("§eCenter: §f" + border.getCenter().getX() + ", " + border.getCenter().getZ()));
        } catch (Exception e) {
            player.sendMessage(Component.text("§cCould not get world border info"));
        }
        
        player.sendMessage(Component.text("§7────────────────────"));
        player.sendMessage(Component.text("§7Use §f/atomhub border set <size> §7- §eSet border size"));
        player.sendMessage(Component.text("§7Use §f/atomhub border center <x> <z> §7- §eSet border center"));
        open();
    }

    private void openGameRules(Player player) {
        World world = player.getWorld();
        
        player.sendMessage(Component.text("§6§lGame Rules - " + world.getName()));
        player.sendMessage(Component.text("§7────────────────────"));
        
        String[] rules = {"doDaylightCycle", "doMobSpawning", "doWeatherCycle", "mobGriefing", 
                         "keepInventory", "naturalRegeneration", "doFireTick", "doMobLoot"};
        
        for (String rule : rules) {
            String value = world.getGameRuleValue(rule);
            player.sendMessage(Component.text("§e" + rule + ": §f" + value));
        }
        
        player.sendMessage(Component.text("§7────────────────────"));
        player.sendMessage(Component.text("§7Use §f/gamerule <rule> <value> §7to change"));
        open();
    }

    private ItemStack createToggleItem(Material mat, String name, String desc, boolean enabled) {
        ItemStack i = new ItemStack(enabled ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + desc),
                Component.text("§7Status: " + (enabled ? "§aEnabled" : "§cDisabled")),
                Component.text("§eClick to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createActionItem(Material mat, String name, String desc) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + desc),
                Component.text("§eClick to execute")
        ));
        i.setItemMeta(meta);
        return i;
    }
}