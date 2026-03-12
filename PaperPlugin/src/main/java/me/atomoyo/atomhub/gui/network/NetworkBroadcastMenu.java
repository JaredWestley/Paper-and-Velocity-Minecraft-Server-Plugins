// File: me/atomoyo/atomhub/gui/network/NetworkBroadcastMenu.java
package me.atomoyo.atomhub.gui.network;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NetworkBroadcastMenu {

    private final AtomHub plugin;
    private final Player admin;

    public NetworkBroadcastMenu(AtomHub plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_BROADCAST"), 54,
                Component.text("§8Network Broadcast"));

        // Header info
        inv.setItem(4, createInfoItem(Material.PAPER, "§6§lNetwork Broadcast",
                "§7Send messages to all servers and players",
                "§7Current servers: §f3",
                "§7Total players: §f" + Bukkit.getOnlinePlayers().size(),
                "§7Last broadcast: §f5 minutes ago"));

        // Quick message templates
        inv.setItem(10, createQuickMessage(Material.ENDER_EYE, "§dAnnouncement",
                "§7[Network] §f{message}", "announcement"));

        inv.setItem(11, createQuickMessage(Material.BEACON, "§eWarning",
                "§c⚠ §7[Warning] §f{message}", "warning"));

        inv.setItem(12, createQuickMessage(Material.EMERALD, "§aInformation",
                "§aℹ §7[Info] §f{message}", "information"));

        inv.setItem(13, createQuickMessage(Material.FIREWORK_ROCKET, "§6Event",
                "§6🎉 §7[Event] §f{message}", "event"));

        inv.setItem(14, createQuickMessage(Material.REDSTONE_TORCH, "§cEmergency",
                "§4🚨 §7[EMERGENCY] §f{message}", "emergency"));

        inv.setItem(15, createQuickMessage(Material.BOOK, "§fCustom",
                "§7Enter custom message", "custom"));

        // Target selection
        inv.setItem(19, createTargetItem(Material.GRASS_BLOCK, "§aAll Servers", "ALL"));
        inv.setItem(20, createTargetItem(Material.NETHERRACK, "§cSurvival Only", "SURVIVAL"));
        inv.setItem(21, createTargetItem(Material.QUARTZ_BLOCK, "§fCreative Only", "CREATIVE"));
        inv.setItem(22, createTargetItem(Material.SLIME_BLOCK, "§eMinigames Only", "MINIGAMES"));
        inv.setItem(23, createTargetItem(Material.WHITE_WOOL, "§7Lobby Only", "LOBBY"));
        inv.setItem(24, createTargetItem(Material.PLAYER_HEAD, "§dSpecific Players", "PLAYERS"));

        // Broadcast type
        inv.setItem(28, createTypeItem(Material.WRITTEN_BOOK, "§fChat Message", "CHAT"));
        inv.setItem(29, createTypeItem(Material.NAME_TAG, "§eAction Bar", "ACTIONBAR"));
        inv.setItem(30, createTypeItem(Material.PAPER, "§6Title Message", "TITLE"));
        inv.setItem(31, createTypeItem(Material.WRITABLE_BOOK, "§dSubtitle", "SUBTITLE"));
        inv.setItem(32, createTypeItem(Material.GLOWSTONE_DUST, "§bBoss Bar", "BOSSBAR"));
        inv.setItem(33, createTypeItem(Material.FIREWORK_ROCKET, "§cToast Notification", "TOAST"));

        // Sound effects
        inv.setItem(37, createSoundItem(Material.NOTE_BLOCK, "§aPlay Sound", "SOUND"));
        inv.setItem(38, createSoundItem(Material.JUKEBOX, "§eSound Effects", "EFFECTS"));
        inv.setItem(39, createSoundItem(Material.BELL, "§6Bell Ring", "BELL"));
        inv.setItem(40, createSoundItem(Material.GOAT_HORN, "§cHorn Blast", "HORN"));
        inv.setItem(41, createSoundItem(Material.MUSIC_DISC_11, "§dCustom Sound", "CUSTOM"));

        // Timing options
        inv.setItem(45, createTimingItem(Material.CLOCK, "§eInstant", "INSTANT"));
        inv.setItem(46, createTimingItem(Material.REPEATER, "§6Delayed", "DELAYED"));
        inv.setItem(47, createTimingItem(Material.COMPARATOR, "§bScheduled", "SCHEDULED"));
        inv.setItem(48, createTimingItem(Material.REDSTONE, "§cRepeating", "REPEATING"));

        // Send options
        inv.setItem(49, createSendItem(Material.GREEN_WOOL, "§a§lSEND NOW", "SEND"));
        inv.setItem(50, createSendItem(Material.YELLOW_WOOL, "§e§lPREVIEW", "PREVIEW"));
        inv.setItem(51, createSendItem(Material.RED_WOOL, "§c§lCANCEL", "CANCEL"));

        // History
        inv.setItem(52, createHistoryItem());

        // Back button
        inv.setItem(53, createNavItem(Material.ARROW, "§7Back to Network"));

        admin.openInventory(inv);
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

    private ItemStack createQuickMessage(Material mat, String name, String format, String type) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Format: " + format),
                Component.text("§8Type: " + type),
                Component.text(""),
                Component.text("§aLeft-click: Use template"),
                Component.text("§eRight-click: Edit template")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createTargetItem(Material mat, String name, String target) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Send to: " + target),
                Component.text("§8Target: " + target),
                Component.text("§eClick to select")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createTypeItem(Material mat, String name, String type) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Display type: " + type),
                Component.text("§8Type: " + type),
                Component.text("§eClick to select")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createSoundItem(Material mat, String name, String sound) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Play sound with message"),
                Component.text("§8Sound: " + sound),
                Component.text("§eClick to select")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createTimingItem(Material mat, String name, String timing) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Broadcast timing: " + timing),
                Component.text("§8Timing: " + timing),
                Component.text("§eClick to select")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createSendItem(Material mat, String name, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Action: " + action.toLowerCase()),
                Component.text("§8Action: " + action),
                Component.text(action.equals("CANCEL") ? "§cClick to cancel" : "§aClick to " + action.toLowerCase())
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createHistoryItem() {
        ItemStack i = new ItemStack(Material.BOOK);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text("§6Broadcast History"));
        meta.lore(Arrays.asList(
                Component.text("§7View previous broadcasts"),
                Component.text("§8Total broadcasts: §f24"),
                Component.text("§8Last 24h: §f5"),
                Component.text(""),
                Component.text("§aClick to view history"),
                Component.text("§eRight-click to clear history")
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