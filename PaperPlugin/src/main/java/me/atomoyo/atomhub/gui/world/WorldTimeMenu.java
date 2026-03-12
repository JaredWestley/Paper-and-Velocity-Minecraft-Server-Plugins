package me.atomoyo.atomhub.gui.world;

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

public class WorldTimeMenu {

    private final Player player;
    private final World world;
    private final AtomHub plugin;

    public WorldTimeMenu(AtomHub plugin, Player player, World world) {
        this.player = player;
        this.world = world;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD_TIME"), 36, Component.text("§8World Time Settings"));

        // Standard times
        inv.setItem(10, createTimeItem(Material.SUNFLOWER, "§eDawn", "Set to 23000 (dawn)", 23000));
        inv.setItem(11, createTimeItem(Material.CLOCK, "§6Sunrise", "Set to 0 (sunrise)", 0));
        inv.setItem(12, createTimeItem(Material.YELLOW_CONCRETE, "§eMorning", "Set to 1000 (morning)", 1000));
        inv.setItem(13, createTimeItem(Material.ORANGE_CONCRETE, "§6Midday", "Set to 6000 (noon)", 6000));
        inv.setItem(14, createTimeItem(Material.RED_CONCRETE, "§cAfternoon", "Set to 9000 (afternoon)", 9000));
        inv.setItem(15, createTimeItem(Material.PURPLE_CONCRETE, "§dSunset", "Set to 12000 (sunset)", 12000));
        inv.setItem(16, createTimeItem(Material.BLUE_CONCRETE, "§9Evening", "Set to 13000 (evening)", 13000));
        inv.setItem(19, createTimeItem(Material.BLACK_CONCRETE, "§8Night", "Set to 14000 (night)", 14000));
        inv.setItem(20, createTimeItem(Material.NETHER_STAR, "§5Midnight", "Set to 18000 (midnight)", 18000));
        inv.setItem(21, createTimeItem(Material.LIGHT_BLUE_CONCRETE, "§bLate Night", "Set to 22000 (late night)", 22000));

        // Special time effects
        inv.setItem(23, createSpecialItem(Material.LIGHTNING_ROD, "§eToggle Time Cycle", "Freeze/unfreeze time"));
        inv.setItem(24, createSpecialItem(Material.CHAIN_COMMAND_BLOCK, "§aSpeed Up Time", "2x time speed"));
        inv.setItem(25, createSpecialItem(Material.REPEATER, "§cSlow Down Time", "0.5x time speed"));
        inv.setItem(26, createSpecialItem(Material.COMMAND_BLOCK, "§6Reset Time Speed", "Normal time speed"));

        inv.setItem(31, item(Material.ARROW, "§7Back to World Menu"));
        inv.setItem(32, createSpecialItem(Material.BOOK, "§eCurrent Time", "Time: " + world.getTime() + " ticks"));

        player.openInventory(inv);
    }

    private ItemStack createTimeItem(Material mat, String name, String lore, long time) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore), Component.text("§8Click to set time")));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createSpecialItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
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
            case SUNFLOWER -> setTime(23000, "Dawn");
            case CLOCK -> setTime(0, "Sunrise");
            case YELLOW_CONCRETE -> setTime(1000, "Morning");
            case ORANGE_CONCRETE -> setTime(6000, "Midday");
            case RED_CONCRETE -> setTime(9000, "Afternoon");
            case PURPLE_CONCRETE -> setTime(12000, "Sunset");
            case BLUE_CONCRETE -> setTime(13000, "Evening");
            case BLACK_CONCRETE -> setTime(14000, "Night");
            case NETHER_STAR -> setTime(18000, "Midnight");
            case LIGHT_BLUE_CONCRETE -> setTime(22000, "Late Night");

            // Special time controls
            case LIGHTNING_ROD -> toggleTimeCycle();
            case CHAIN_COMMAND_BLOCK -> speedUpTime();
            case REPEATER -> slowDownTime();
            case COMMAND_BLOCK -> resetTimeSpeed();
            case BOOK -> showCurrentTime();

            case ARROW -> plugin.getGuiManager().openWorldMenu(player);
        }
    }

    private void setTime(long ticks, String name) {
        world.setTime(ticks);
        player.sendMessage(Component.text("§6[AtomHub] - §7Time set to §e" + name + "§7 (§f" + ticks + " ticks§7)"));
    }

    private void toggleTimeCycle() {
        boolean wasFrozen = world.isGameRule("doDaylightCycle");
        world.setGameRuleValue("doDaylightCycle", wasFrozen ? "false" : "true");
        player.sendMessage(Component.text("§6[AtomHub] - §7Time cycle " +
                (wasFrozen ? "§aunfrozen§7 (normal speed)" : "§cfrozen§7 (stopped)")));
    }

    private void speedUpTime() {
        world.setTime(world.getTime() + 100);
        player.sendMessage(Component.text("§6[AtomHub] - §7Time sped up by 100 ticks"));
    }

    private void slowDownTime() {
        world.setTime(world.getTime() - 100);
        player.sendMessage(Component.text("§6[AtomHub] - §7Time slowed down by 100 ticks"));
    }

    private void resetTimeSpeed() {
        world.setGameRuleValue("doDaylightCycle", "true");
        player.sendMessage(Component.text("§6[AtomHub] - §7Time speed reset to normal"));
    }

    private void showCurrentTime() {
        long ticks = world.getTime();
        long hours = (ticks / 1000 + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;

        player.sendMessage(Component.text("§6[AtomHub] - §7Current Time:"));
        player.sendMessage(Component.text("§7• Ticks: §f" + ticks));
        player.sendMessage(Component.text("§7• In-game: §f" + String.format("%02d:%02d", hours, minutes)));
        player.sendMessage(Component.text("§7• Time cycle: §f" +
                (world.isGameRule("doDaylightCycle") ? "Active" : "Frozen")));
    }
}
