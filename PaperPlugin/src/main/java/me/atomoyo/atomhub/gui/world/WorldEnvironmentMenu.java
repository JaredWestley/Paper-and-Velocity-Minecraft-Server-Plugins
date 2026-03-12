package me.atomoyo.atomhub.gui.world;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class WorldEnvironmentMenu {

    private final Player player;
    private final World world;
    private final AtomHub plugin;

    public WorldEnvironmentMenu(AtomHub plugin, Player player, World world) {
        this.player = player;
        this.world = world;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD_ENV"), 45, Component.text("§8Environment Settings"));

        // Difficulty
        inv.setItem(10, createDifficultyItem(Material.WOODEN_SWORD, "§aPeaceful", Difficulty.PEACEFUL));
        inv.setItem(11, createDifficultyItem(Material.STONE_SWORD, "§eEasy", Difficulty.EASY));
        inv.setItem(12, createDifficultyItem(Material.IRON_SWORD, "§6Normal", Difficulty.NORMAL));
        inv.setItem(13, createDifficultyItem(Material.DIAMOND_SWORD, "§cHard", Difficulty.HARD));

        // Game Rules
        inv.setItem(15, createRuleItem(Material.TNT, "§4Toggle TNT", "doTileDrops", "TNT explosions"));
        inv.setItem(16, createRuleItem(Material.FIRE_CHARGE, "§cToggle Fire Spread", "doFireTick", "Fire spread"));
        inv.setItem(24, createEffectItem(Material.GOLDEN_APPLE, "§6God Mode All", "Temporary god mode"));
        inv.setItem(25, createRuleItem(Material.SPAWNER, "§6Mob Griefing", "mobGriefing", "Mob block damage"));
        inv.setItem(34, createRuleItem(Material.ENDER_PEARL, "§dKeep Inventory", "keepInventory", "Keep items on death"));

        // World Borders
        inv.setItem(28, createBorderItem(Material.BARRIER, "§bSet Border 500", 500.0));
        inv.setItem(29, createBorderItem(Material.IRON_BARS, "§bSet Border 1000", 1000.0));
        inv.setItem(30, createBorderItem(Material.GLASS, "§bSet Border 5000", 5000.0));
        inv.setItem(31, createBorderItem(Material.BEACON, "§bRemove Border", 0.0));

        // World Effects
        inv.setItem(33, createEffectItem(Material.POTION, "§5Night Vision All", "Give night vision to all"));

        inv.setItem(40, item(Material.ARROW, "§7Back to World Menu"));

        player.openInventory(inv);
    }

    private ItemStack createDifficultyItem(Material mat, String name, Difficulty diff) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Current: " + world.getDifficulty().toString()),
                Component.text("§8Click to set")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createRuleItem(Material mat, String name, String rule, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        boolean current = Boolean.parseBoolean(world.getGameRuleValue(rule));
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§7Current: " + (current ? "§aON" : "§cOFF")),
                Component.text("§8Click to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createBorderItem(Material mat, String name, double size) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Current: " + world.getWorldBorder().getSize()),
                Component.text("§8Click to set")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createEffectItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + description)));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack item(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }

    public void handleClick(Player player, Material clicked) {
        switch (clicked) {
            case WOODEN_SWORD -> setDifficulty(Difficulty.PEACEFUL);
            case STONE_SWORD -> setDifficulty(Difficulty.EASY);
            case IRON_SWORD -> setDifficulty(Difficulty.NORMAL);
            case DIAMOND_SWORD -> setDifficulty(Difficulty.HARD);

            case TNT -> toggleGameRule("doTileDrops", "TNT drops");
            case FIRE_CHARGE -> toggleGameRule("doFireTick", "Fire spread");
            case SPAWNER -> toggleGameRule("mobGriefing", "Mob griefing");
            case ENDER_PEARL -> toggleGameRule("keepInventory", "Keep inventory");

            case BARRIER -> setWorldBorder(500.0);
            case IRON_BARS -> setWorldBorder(1000.0);
            case GLASS -> setWorldBorder(5000.0);
            case BEACON -> setWorldBorder(0.0);

            case POTION -> giveNightVisionAll();
            case GOLDEN_APPLE -> giveGodModeAll();

            case ARROW -> plugin.getGuiManager().openWorldMenu(player);
        }
    }

    private void setDifficulty(Difficulty diff) {
        world.setDifficulty(diff);
        player.sendMessage(Component.text("§6[AtomHub] - §7Difficulty set to §e" + diff.toString()));
    }

    private void toggleGameRule(String rule, String name) {
        boolean current = Boolean.parseBoolean(world.getGameRuleValue(rule));
        world.setGameRuleValue(rule, String.valueOf(!current));
        player.sendMessage(Component.text("§6[AtomHub] - §7" + name + " " +
                (!current ? "§aenabled" : "§cdisabled")));
    }

    private void setWorldBorder(double size) {
        if (size == 0) {
            world.getWorldBorder().reset();
            player.sendMessage(Component.text("§6[AtomHub] - §7World border removed"));
        } else {
            world.getWorldBorder().setSize(size);
            player.sendMessage(Component.text("§6[AtomHub] - §7World border set to §f" + size + " blocks"));
        }
    }

    private void giveNightVisionAll() {
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 0, true, false));
            p.sendMessage(Component.text("§5[World] - §7You gained night vision!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Night vision given to all players"));
    }

    private void giveGodModeAll() {
        world.getPlayers().forEach(p -> {
            p.setInvulnerable(true);
            p.sendMessage(Component.text("§6[World] - §7You are invulnerable for 30 seconds!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Temporary god mode enabled for all"));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            world.getPlayers().forEach(p -> {
                p.setInvulnerable(false);
                p.sendMessage(Component.text("§6[World] - §7God mode has worn off"));
            });
        }, 600L); // 30 seconds
    }
}