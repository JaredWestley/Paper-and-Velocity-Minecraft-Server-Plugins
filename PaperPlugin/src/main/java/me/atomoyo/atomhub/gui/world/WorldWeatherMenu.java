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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class WorldWeatherMenu {

    private final Player player;
    private final World world;
    private final AtomHub plugin;

    public WorldWeatherMenu(AtomHub plugin, Player player, World world) {
        this.player = player;
        this.world = world;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new MenuHolder("WORLD_WEATHER"), 36, Component.text("§8World Weather Settings"));

        // Weather types
        inv.setItem(10, createWeatherItem(Material.SUNFLOWER, "§eClear Sky", "No rain or storm"));
        inv.setItem(11, createWeatherItem(Material.WATER_BUCKET, "§9Light Rain", "Gentle rain"));
        inv.setItem(12, createWeatherItem(Material.SNOWBALL, "§fSnowfall", "Snow in cold biomes"));
        inv.setItem(13, createWeatherItem(Material.LIGHTNING_ROD, "§6Thunderstorm", "Storm with lightning"));
        inv.setItem(14, createWeatherItem(Material.END_CRYSTAL, "§5Mystical Fog", "Thick fog effect"));

        // Weather intensity
        inv.setItem(16, createIntensityItem(Material.REDSTONE, "§cHeavy Storm", "Intense rain & thunder"));
        inv.setItem(25, createIntensityItem(Material.GLOWSTONE_DUST, "§eLight Showers", "Light rain"));

        // Weather effects
        inv.setItem(19, createEffectItem(Material.TOTEM_OF_UNDYING, "§aHealing Rain", "Rain heals players"));
        inv.setItem(20, createEffectItem(Material.POISONOUS_POTATO, "§cAcid Rain", "Rain damages players"));
        inv.setItem(21, createEffectItem(Material.LAPIS_LAZULI, "§9XP Rain", "Rain drops XP"));
        inv.setItem(22, createEffectItem(Material.GOLD_NUGGET, "§6Golden Rain", "Rain drops gold"));
        inv.setItem(23, createEffectItem(Material.ENDER_EYE, "§dEnder Storm", "Teleporting rain"));

        // Controls
        inv.setItem(29, createControlItem(Material.CLOCK, "§eProlong Weather", "Extend weather duration"));
        inv.setItem(30, createControlItem(Material.BARRIER, "§cStop Weather", "Clear all weather effects"));
        inv.setItem(31, item(Material.ARROW, "§7Back to World Menu"));
        inv.setItem(32, createControlItem(Material.BOOK, "§6Weather Info", "Current weather status"));

        player.openInventory(inv);
    }

    private ItemStack createWeatherItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + lore),
                Component.text("§8Click to activate")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createIntensityItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(Component.text("§7" + lore)));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createEffectItem(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + lore),
                Component.text("§8Experimental effect")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createControlItem(Material mat, String name, String lore) {
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
            case SUNFLOWER -> setClearWeather();
            case WATER_BUCKET -> setLightRain();
            case SNOWBALL -> setSnowfall();
            case LIGHTNING_ROD -> setThunderstorm();
            case END_CRYSTAL -> createMysticalFog();

            case REDSTONE -> setHeavyStorm();
            case GLOWSTONE_DUST -> setLightShowers();

            case TOTEM_OF_UNDYING -> startHealingRain();
            case POISONOUS_POTATO -> startAcidRain();
            case LAPIS_LAZULI -> startXPRain();
            case GOLD_NUGGET -> startGoldenRain();
            case ENDER_EYE -> startEnderStorm();

            case BARRIER -> stopAllWeather();
            case CLOCK -> prolongWeather();
            case BOOK -> showWeatherInfo();

            case ARROW -> plugin.getGuiManager().openWorldMenu(player);
        }
    }

    private void setClearWeather() {
        world.setStorm(false);
        world.setThundering(false);
        player.sendMessage(Component.text("§6[AtomHub] - §7Weather set to §eclear sky§7."));
    }

    private void setLightRain() {
        world.setStorm(true);
        world.setThundering(false);
        world.setWeatherDuration(1200); // 1 minute
        player.sendMessage(Component.text("§6[AtomHub] - §7Light rain started for 1 minute."));
    }

    private void setSnowfall() {
        world.setStorm(true);
        world.setThundering(false);
        world.setWeatherDuration(2400); // 2 minutes
        player.sendMessage(Component.text("§6[AtomHub] - §7Snowfall started for 2 minutes."));
    }

    private void setThunderstorm() {
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(1800); // 1.5 minutes
        world.setThunderDuration(1200); // 1 minute of thunder
        player.sendMessage(Component.text("§6[AtomHub] - §7Thunderstorm with lightning started!"));
    }

    private void createMysticalFog() {
        world.setStorm(true);
        player.sendMessage(Component.text("§6[AtomHub] - §7Mystical fog created!"));

        // Add fog effect to nearby players
        world.getPlayers().forEach(p -> {
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1, false, false));
        });
    }

    private void setHeavyStorm() {
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(3600); // 3 minutes
        world.setThunderDuration(3600); // 3 minutes of thunder
        player.sendMessage(Component.text("§6[AtomHub] - §7Heavy storm with intense lightning!"));
    }

    private void setLightShowers() {
        world.setStorm(true);
        world.setThundering(false);
        world.setWeatherDuration(600); // 30 seconds
        player.sendMessage(Component.text("§6[AtomHub] - §7Light showers for 30 seconds."));
    }

    private void startHealingRain() {
        world.setStorm(true);
        player.sendMessage(Component.text("§6[AtomHub] - §7Healing rain active!"));

        // Schedule healing effect
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.hasStorm()) {
                world.getPlayers().forEach(p -> {
                    if (p.getHealth() < p.getMaxHealth()) {
                        p.setHealth(Math.min(p.getHealth() + 1, p.getMaxHealth()));
                        p.sendActionBar(Component.text("§a♥ Healing from the rain!"));
                    }
                });
            }
        }, 0L, 40L); // Every 2 seconds
    }

    private void startAcidRain() {
        world.setStorm(true);
        player.sendMessage(Component.text("§6[AtomHub] - §cAcid rain warning! Take cover!"));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.hasStorm()) {
                world.getPlayers().forEach(p -> {
                    if (!p.isOp() && !p.hasPermission("atomhub.weather.bypass")) {
                        p.damage(1.0);
                        p.sendActionBar(Component.text("§c☠ Acid rain damage!"));
                    }
                });
            }
        }, 0L, 40L);
    }

    private void startXPRain() {
        world.setStorm(true);
        player.sendMessage(Component.text("§6[AtomHub] §9XP raining from the sky!"));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.hasStorm()) {
                world.getPlayers().forEach(p -> {
                    p.giveExp(5);
                    p.sendActionBar(Component.text("§9✨ +5 XP from rain"));
                });
            }
        }, 0L, 60L); // Every 3 seconds
    }

    private void startGoldenRain() {
        world.setStorm(true);
        player.sendMessage(Component.text("§6[AtomHub] - §6Gold nuggets raining!"));
    }

    private void startEnderStorm() {
        world.setStorm(true);
        world.setThundering(true);
        player.sendMessage(Component.text("§6[AtomHub] - §dEnder storm - teleportation chaos!"));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.hasStorm()) {
                world.getPlayers().forEach(p -> {
                    if (Math.random() < 0.1) { // 10% chance each tick
                        p.teleport(p.getLocation().add(
                                (Math.random() - 0.5) * 10,
                                0,
                                (Math.random() - 0.5) * 10
                        ));
                        p.sendActionBar(Component.text("§d🌀 Ender storm teleported you!"));
                    }
                });
            }
        }, 0L, 20L); // Every second
    }

    private void stopAllWeather() {
        world.setStorm(false);
        world.setThundering(false);
        player.sendMessage(Component.text("§6[AtomHub] - §7All weather effects stopped."));
    }

    private void prolongWeather() {
        if (world.hasStorm()) {
            world.setWeatherDuration(world.getWeatherDuration() + 1200); // Add 1 minute
            player.sendMessage(Component.text("§6[AtomHub] - §7Weather extended by 1 minute."));
        }
    }

    private void showWeatherInfo() {
        String weather = world.hasStorm() ?
                (world.isThundering() ? "§cThunderstorm" : "§9Rain/Snow") :
                "§eClear";

        player.sendMessage(Component.text("§6[AtomHub] - §7Weather Information:"));
        player.sendMessage(Component.text("§7• Status: " + weather));
        if (world.hasStorm()) {
            player.sendMessage(Component.text("§7• Duration left: §f" + (world.getWeatherDuration() / 20) + "s"));
            if (world.isThundering()) {
                player.sendMessage(Component.text("§7• Thunder left: §f" + (world.getThunderDuration() / 20) + "s"));
            }
        }
    }
}
