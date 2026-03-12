package me.atomoyo.atomhub.gui;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class PlayerMenu {

    private final Player admin;
    private final Player target;
    private final AtomHub plugin;

    public PlayerMenu(Player admin, Player target, AtomHub plugin) {
        this.admin = admin;
        this.target = target;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("PLAYER"), 54, Component.text("§8Player: §f" + target.getName()));

        inv.setItem(0, item(Material.BOOK, "§6Player Info", "View " + target.getName() + "'s statistics"));
        inv.setItem(1, item(Material.COMPASS, "§bCompass Track", "Make compass track " + target.getName()));
        inv.setItem(9, item(Material.CHEST, "§7View Inventory", "View " + target.getName() + "'s inventory"));
        inv.setItem(10, item(Material.ENDER_CHEST, "§5View Ender Chest", "View " + target.getName() + "'s ender chest"));

        // Row 4: Gamemode & Inventory
        inv.setItem(3, createGamemodeItem(Material.GRASS_BLOCK, "§aSurvival", GameMode.SURVIVAL));
        inv.setItem(4, createGamemodeItem(Material.DIAMOND, "§bCreative", GameMode.CREATIVE));
        inv.setItem(12, createGamemodeItem(Material.SPYGLASS, "§6Spectator", GameMode.SPECTATOR));
        inv.setItem(13, createGamemodeItem(Material.WOODEN_AXE, "§eAdventure", GameMode.ADVENTURE));

        // Row 2: Teleport & Movement/ Health & Effects
        inv.setItem(6, item(Material.ENDER_PEARL, "§5Teleport To", "Teleport to "+ target.getName()));
        inv.setItem(7, item(Material.ENDER_EYE, "§dBring Here", "Teleport " + target.getName() + " to you"));
        inv.setItem(8, item(Material.GLOWSTONE_DUST, "§bGlow Toggle", "Make " + target.getName() + "'s glow"));
        inv.setItem(15, item(Material.SUGAR, "§aSpeed Control", "Adjust " + target.getName() + "'s speed"));
        inv.setItem(16, item(Material.GOLDEN_APPLE, "§eHealth Options", "Heal, damage, kill"));
        inv.setItem(17, item(Material.POTION, "§dEffect Control", "Manage potion effects"));

        // Row 5: Special Actions
        inv.setItem(30, item(Material.FIREWORK_ROCKET, "§cLaunch Up", "Launch " + target.getName() + " into the air"));
        inv.setItem(31, item(Material.TNT, "§4Explode", "Blow " + target.getName() + " up"));
        inv.setItem(32, item(Material.LIGHTNING_ROD, "§eStrike Lightning", "Strike " + target.getName() + " with lightning"));

        // Punishments
        inv.setItem(36, item(Material.IRON_BARS, "§cServer Ban", "Ban " + target.getName() + " from this server"));
        inv.setItem(37, item(Material.BLAZE_ROD, "§6Server Kick", "Kick " + target.getName() + " from this server"));

        boolean isMuted = plugin.getMuteManager().isMuted(target);
        Material muteIcon = isMuted ? Material.LIME_DYE : Material.RED_DYE;
        String muteText = isMuted ? "§aUnmute Player" : "§cMute Player";
        inv.setItem(27, item(muteIcon, muteText));

        boolean isFrozen = plugin.getFreezeManager().isFrozen(target);
        Material freezeIcon = isFrozen ? Material.PACKED_ICE : Material.ICE;
        String freezeText = isFrozen ? "§aUnfreeze " + target.getName() : "§9Freeze " + target.getName();
        inv.setItem(28, createStatusItem(freezeIcon, freezeText, "Movement control"));

        inv.setItem(45, item(Material.ANVIL, "§4Network Ban", "Ban " + target.getName() + " from entire network"));
        inv.setItem(46, item(Material.BARRIER, "§cNetwork Kick", "Kick " + target.getName() + " from entire network"));

        inv.setItem(49, item(Material.ARROW, "§7Back to Player List"));

        inv.setItem(43, item(Material.LEAD, "§fLeash Control", "Leash/unleash " + target.getName()));
        inv.setItem(44, item(Material.NAME_TAG, "§eRename Player", "Change " + target.getName() + "'s display name"));
        inv.setItem(52, item(Material.CLOCK, "§bTime Control", "Set " + target.getName() + "'s time"));
        inv.setItem(53, item(Material.WEATHERED_COPPER, "§9Weather Control", "Set " + target.getName() + "'s weather"));

        admin.openInventory(inv);
    }

    private ItemStack item(Material mat, String name, String lore) {
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

    private ItemStack createStatusItem(Material mat, String name, String description) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text("§8Click to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createGamemodeItem(Material mat, String name, GameMode gamemode) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        boolean isCurrent = target.getGameMode() == gamemode;
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Set player to " + gamemode.toString().toLowerCase()),
                Component.text(isCurrent ? "§aCurrently active" : "§8Click to set")
        ));
        i.setItemMeta(meta);
        return i;
    }
}