package me.atomoyo.atomhub.gui.world;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldEffectsMenu {

    private final Player player;
    private final World world;
    private final AtomHub plugin;

    // Store active effects for tracking
    private static final Map<UUID, BukkitTask> activeEffects = new HashMap<>();
    private static final Map<UUID, Integer> xpMultipliers = new HashMap<>();
    private static final Map<UUID, Double> lootMultipliers = new HashMap<>();
    private static boolean fastMiningEnabled = false;

    public WorldEffectsMenu(AtomHub plugin, Player player, World world) {
        this.player = player;
        this.world = world;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD_EFFECTS"), 54, Component.text("§8World Effects"));

        // Visual Effects
        inv.setItem(10, createEffectItem(Material.GLOWSTONE_DUST, "§eGlow All Players", "Make everyone glow", true));
        inv.setItem(11, createEffectItem(Material.FIREWORK_STAR, "§6Firework Trail", "Players leave firework trails", true));
        inv.setItem(12, createEffectItem(Material.HEART_OF_THE_SEA, "§bBubble Effect", "Surround players with bubbles", true));

        // Audio Effects
        inv.setItem(14, createEffectItem(Material.JUKEBOX, "§dGlobal Music", "Play music for everyone", true));
        inv.setItem(15, createEffectItem(Material.NOTE_BLOCK, "§5Ambient Sounds", "Play ambient sounds", true));

        // Gameplay Effects
        inv.setItem(16, createEffectItem(Material.SUGAR, "§aSpeed Boost All", "Give speed to everyone", true));
        inv.setItem(19, createEffectItem(Material.GOLDEN_APPLE, "§6Strength Boost", "Give strength to everyone", true));
        inv.setItem(20, createEffectItem(Material.FEATHER, "§fJump Boost All", "Give jump boost to everyone", true));
        inv.setItem(21, createEffectItem(Material.TURTLE_HELMET, "§bWater Breathing", "Give water breathing to everyone", true));

        // Environmental Effects
        inv.setItem(23, createEffectItem(Material.LAPIS_LAZULI, "§9XP Multiplier", "Double XP gains", true));
        inv.setItem(24, createEffectItem(Material.EMERALD, "§aLoot Boost", "Better loot from mobs/chests", true));
        inv.setItem(25, createEffectItem(Material.REDSTONE, "§cFast Mining", "Instant break blocks", true));

        // Stop All Effects Button
        inv.setItem(48, createEffectItem(Material.BARRIER, "§cStop All Effects", "Clear all active effects", false));

        inv.setItem(49, item(Material.ARROW, "§7Back to World Menu"));

        // Status Info
        inv.setItem(50, createInfoItem(Material.BOOK, "§6Active Effects",
                "Glowing: " + (hasGlowingEffect() ? "§aON" : "§cOFF"),
                "Firework Trails: " + (hasActiveEffect("firework") ? "§aON" : "§cOFF"),
                "XP Multiplier: " + (hasXPMultiplier() ? "§a2x" : "§c1x"),
                "Fast Mining: " + (fastMiningEnabled ? "§aON" : "§cOFF")));

        player.openInventory(inv);
    }

    private ItemStack createEffectItem(Material mat, String name, String description, boolean isActivate) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + description),
                Component.text(isActivate ? "§aClick to activate" : "§cClick to deactivate")
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

    private ItemStack item(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }

    public void handleClick(Player player, Material clicked) {
        switch (clicked) {
            case GLOWSTONE_DUST -> glowAllPlayers();
            case FIREWORK_STAR -> toggleFireworkTrails();
            case HEART_OF_THE_SEA -> toggleBubbleEffect();
            case JUKEBOX -> playGlobalMusic();
            case NOTE_BLOCK -> playAmbientSounds();
            case SUGAR -> giveSpeedBoost();
            case GOLDEN_APPLE -> giveStrengthBoost();
            case FEATHER -> giveJumpBoost();
            case TURTLE_HELMET -> giveWaterBreathing();
            case LAPIS_LAZULI -> toggleXPMultiplier();
            case EMERALD -> toggleLootBoost();
            case REDSTONE -> toggleFastMining();
            case BARRIER -> stopAllEffects();
            case ARROW -> plugin.getGuiManager().openWorldMenu(player);
        }
    }

    private void glowAllPlayers() {
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 6000, 0, true, false));
            p.sendMessage(Component.text("§e[World] - §7You are now glowing!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Made all players glow for 5 minutes"));
    }

    private boolean hasGlowingEffect() {
        return world.getPlayers().stream()
                .anyMatch(p -> p.hasPotionEffect(PotionEffectType.GLOWING));
    }

    private void toggleFireworkTrails() {
        UUID worldId = world.getUID();

        if (activeEffects.containsKey(worldId) && activeEffects.get(worldId) != null) {
            // Stop existing firework trails
            activeEffects.get(worldId).cancel();
            activeEffects.remove(worldId);
            player.sendMessage(Component.text("§6[AtomHub] - §7Firework trails §cdisabled"));
            broadcastToWorld("§6[World] - §7Firework trails have ended");
        } else {
            // Start new firework trails
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Player p : world.getPlayers()) {
                    if (p.isOnline() && Math.random() < 0.3) { // 30% chance each tick
                        spawnFirework(p.getLocation().add(0, 1, 0));
                    }
                }
            }, 0L, 10L); // Every 0.5 seconds

            activeEffects.put(worldId, task);
            player.sendMessage(Component.text("§6[AtomHub] - §7Firework trails §aenabled"));
            broadcastToWorld("§6[World] - §7Firework trails activated!");
        }
    }

    private void spawnFirework(Location loc) {
        Firework fw = world.spawn(loc, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();

        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        if (Math.random() < 0.3) type = FireworkEffect.Type.BALL_LARGE;
        else if (Math.random() < 0.5) type = FireworkEffect.Type.STAR;
        else if (Math.random() < 0.7) type = FireworkEffect.Type.BURST;
        else type = FireworkEffect.Type.CREEPER;

        FireworkEffect effect = FireworkEffect.builder()
                .with(type)
                .withColor(Color.fromRGB((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)))
                .withFade(Color.fromRGB((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)))
                .withFlicker()
                .withTrail()
                .build();

        fwm.addEffect(effect);
        fwm.setPower(0); // Instant explosion
        fw.setFireworkMeta(fwm);

        // Remove firework after explosion
        Bukkit.getScheduler().runTaskLater(plugin, fw::remove, 2L);
    }

    private boolean hasActiveEffect(String effectType) {
        return activeEffects.containsKey(world.getUID());
    }

    private void toggleBubbleEffect() {
        world.getPlayers().forEach(p -> {
            if (p.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) {
                p.removePotionEffect(PotionEffectType.CONDUIT_POWER);
                p.sendMessage(Component.text("§b[World] - §7Bubble effect removed"));
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 6000, 0, true, false));
                p.sendMessage(Component.text("§b[World] - §7You are surrounded by bubbles!"));
            }
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Bubble effect toggled"));
    }

    private void playGlobalMusic() {
        // Play different music based on time of day
        Sound music;
        if (world.getTime() < 13000) { // Day
            music = Sound.MUSIC_DISC_11;
        } else { // Night
            music = Sound.MUSIC_DISC_13;
        }

        for (Player p : world.getPlayers()) {
            p.stopSound(Sound.MUSIC_DISC_11);
            p.stopSound(Sound.MUSIC_DISC_13);
            p.stopSound(Sound.MUSIC_DISC_BLOCKS);
            p.playSound(p.getLocation(), music, 1.0f, 1.0f);
            p.sendMessage(Component.text("§d[World] - §7Now playing global music!"));
        }
        player.sendMessage(Component.text("§6[AtomHub] - §7Playing global music"));

        // Stop music after 3 minutes
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : world.getPlayers()) {
                p.stopSound(music);
                p.sendMessage(Component.text("§d[World] - §7Music has ended"));
            }
        }, 3600L); // 3 minutes
    }

    private void playAmbientSounds() {
        Sound[] ambientSounds = {
                Sound.AMBIENT_CAVE,
                Sound.AMBIENT_BASALT_DELTAS_ADDITIONS,
                Sound.AMBIENT_CRIMSON_FOREST_MOOD,
                Sound.AMBIENT_WARPED_FOREST_MOOD
        };

        Sound randomSound = ambientSounds[(int)(Math.random() * ambientSounds.length)];

        for (Player p : world.getPlayers()) {
            p.playSound(p.getLocation(), randomSound, 0.5f, 1.0f);
            p.sendMessage(Component.text("§5[World] - §7Ambient sounds playing"));
        }
        player.sendMessage(Component.text("§6[AtomHub] - §7Playing ambient sounds"));
    }

    private void giveSpeedBoost() {
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1, true, false));
            p.sendMessage(Component.text("§a[World] - §7You feel faster!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Speed boost given to all players"));
    }

    private void giveStrengthBoost() {
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 1, true, false));
            p.sendMessage(Component.text("§6[World] - §7You feel stronger!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Strength boost given to all players"));
    }

    private void giveJumpBoost() {
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 6000, 1, true, false));
            p.sendMessage(Component.text("§f[World] - §7You can jump higher!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Jump boost given to all players"));
    }

    private void giveWaterBreathing() {
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 6000, 0, true, false));
            p.sendMessage(Component.text("§b[World] - §7You can breathe underwater!"));
        });
        player.sendMessage(Component.text("§6[AtomHub] - §7Water breathing given to all players"));
    }

    private void toggleXPMultiplier() {
        UUID worldId = world.getUID();

        if (xpMultipliers.containsKey(worldId)) {
            xpMultipliers.remove(worldId);
            player.sendMessage(Component.text("§6[AtomHub] - §7XP multiplier §cdisabled"));
            broadcastToWorld("§9[World] - §7XP multiplier has been removed");
        } else {
            xpMultipliers.put(worldId, 2); // 2x XP
            player.sendMessage(Component.text("§6[AtomHub] - §7XP multiplier §aenabled (2x XP)"));
            broadcastToWorld("§9[World] - §72x XP multiplier activated!");
        }
    }

    private boolean hasXPMultiplier() {
        return xpMultipliers.containsKey(world.getUID());
    }

    private void toggleLootBoost() {
        UUID worldId = world.getUID();

        if (lootMultipliers.containsKey(worldId)) {
            lootMultipliers.remove(worldId);
            player.sendMessage(Component.text("§6[AtomHub] - §7Loot boost §cdisabled"));
            broadcastToWorld("§a[World] - §7Loot boost has been removed");
        } else {
            lootMultipliers.put(worldId, 2.0); // Double loot
            player.sendMessage(Component.text("§6[AtomHub] - §7Loot boost §aenabled (2x loot)"));
            broadcastToWorld("§a[World] - §7Loot boost activated! Better drops from mobs and chests!");
        }
    }

    private void toggleFastMining() {
        fastMiningEnabled = !fastMiningEnabled;

        if (fastMiningEnabled) {
            player.sendMessage(Component.text("§6[AtomHub] - §7Fast mining §aenabled"));
            broadcastToWorld("§c[World] - §7Fast mining activated! Blocks break instantly!");
        } else {
            player.sendMessage(Component.text("§6[AtomHub] - §7Fast mining §cdisabled"));
            broadcastToWorld("§c[World] - §7Fast mining deactivated");
        }
    }

    private void stopAllEffects() {
        // Clear all potion effects from players
        world.getPlayers().forEach(p -> {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
        });

        // Stop all active tasks
        for (BukkitTask task : activeEffects.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activeEffects.clear();

        // Clear multipliers
        xpMultipliers.clear();
        lootMultipliers.clear();
        fastMiningEnabled = false;

        // Stop all sounds
        for (Player p : world.getPlayers()) {
            for (Sound sound : Sound.values()) {
                p.stopSound(sound);
            }
        }

        player.sendMessage(Component.text("§6[AtomHub] - §cAll effects stopped"));
        broadcastToWorld("§c[World] - §7All active effects have been cleared");
    }

    private void broadcastToWorld(String message) {
        world.getPlayers().forEach(p -> p.sendMessage(Component.text(message)));
    }

    // ============================================
    // STATIC METHODS FOR OTHER CLASSES TO ACCESS
    // ============================================

    public static double getXPMultiplier(World world) {
        return xpMultipliers.getOrDefault(world.getUID(), 1);
    }

    public static double getLootMultiplier(World world) {
        return lootMultipliers.getOrDefault(world.getUID(), 1.0);
    }

    public static boolean isFastMiningEnabled() {
        return fastMiningEnabled;
    }

    public static void cleanupWorldEffects(World world) {
        UUID worldId = world.getUID();
        if (activeEffects.containsKey(worldId)) {
            activeEffects.get(worldId).cancel();
            activeEffects.remove(worldId);
        }
        xpMultipliers.remove(worldId);
        lootMultipliers.remove(worldId);
    }
}