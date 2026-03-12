package me.atomoyo.atomhub.listeners;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.MenuHolder;
import me.atomoyo.atomhub.gui.server.ViewDistanceMenu;
import me.atomoyo.atomhub.gui.server.PerformanceMenu;
import me.atomoyo.atomhub.gui.server.WhitelistMenu;
import me.atomoyo.atomhub.gui.server.WorldManagerMenu;
import me.atomoyo.atomhub.gui.server.LagPreventionMenu;
import me.atomoyo.atomhub.gui.server.ChunkControlMenu;
import me.atomoyo.atomhub.gui.server.AutoSaveMenu;
import me.atomoyo.atomhub.gui.network.*;
import me.atomoyo.atomhub.gui.server.PerformanceMenu;
import me.atomoyo.atomhub.gui.server.WhitelistMenu;
import me.atomoyo.atomhub.gui.server.WorldManagerMenu;
import me.atomoyo.atomhub.gui.player.PlayerEffectsMenu;
import me.atomoyo.atomhub.gui.player.PlayerHealthMenu;
import me.atomoyo.atomhub.gui.player.PlayerMovementMenu;
import me.atomoyo.atomhub.gui.player.PlayerOptionsMenu;
import me.atomoyo.atomhub.gui.world.WorldEffectsMenu;
import me.atomoyo.atomhub.gui.world.WorldEnvironmentMenu;
import me.atomoyo.atomhub.gui.world.WorldTimeMenu;
import me.atomoyo.atomhub.gui.world.WorldWeatherMenu;
import me.atomoyo.atomhub.util.PerformanceTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

public class InventoryClickListener implements Listener {

    private final AtomHub plugin;

    private final Map<UUID, Player> OpenMenus = new HashMap<>();
    private final Map<UUID, Player> pendingTransfers = new HashMap<>();
    private final Map<UUID, Player> pendingBans = new HashMap<>();
    private final Map<UUID, Player> pendingKicks = new HashMap<>();


    public InventoryClickListener(AtomHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player admin)) return;
        ItemStack item = ev.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (!(ev.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;

        ev.setCancelled(true); // Always cancel clicks in plugin menus
        String type = holder.getType();
        Material clicked = item.getType();

        switch (type) {
            case "MAIN" -> {
                switch (clicked) {
                    case PLAYER_HEAD -> plugin.getGuiManager().openPlayerList(admin, 1);
                    case COMMAND_BLOCK -> plugin.getGuiManager().openServerMenu(admin);
                    case NETHER_STAR -> plugin.getGuiManager().openNetworkMenu(admin);
                    case GRASS_BLOCK -> plugin.getGuiManager().openWorldMenu(admin);
                }
            }

            case "SERVER" -> {
                if (clicked == Material.ARROW) plugin.getGuiManager().openMainMenu(admin);

                World world = admin.getWorld();
                if (!checkPermission(admin, "atomhub.world.manage")) return;

                switch (clicked) {
                    // Stop Server
                    case REDSTONE -> {

                        if (!checkPermission(admin, "atomhub.server.stop")) return;

                        admin.sendMessage(Component.text("[AtomHub] §7Stopping server..."));
                        Bukkit.broadcast(Component.text("§c⚠ Server is stopping in 5 seconds!"));

                        for (int i = 1; i <= 5; i++) {
                            int timeLeft = 5 - i;

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {

                                if (timeLeft > 0) {
                                    Bukkit.broadcast(Component.text("§c⚠ Server stopping in " + timeLeft + " seconds..."));

                                    // Play a tick sound for all players
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                                    }
                                }

                            }, i * 20L);
                        }

                        // Final second → Play PLING then shutdown
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {

                            Bukkit.broadcast(Component.text("§c⚠ Shutting down now!"));

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                            }

                            plugin.getServer().shutdown();

                        }, 100L);
                        admin.closeInventory();
                    }

                    case REPEATER -> {

                        if (!checkPermission(admin, "atomhub.server.restart")) return;

                        admin.sendMessage(Component.text("[AtomHub] §7Restarting server..."));
                        Bukkit.broadcast(Component.text("§c⚠ Server is restarting in 5 seconds!"));

                        for (int i = 1; i <= 5; i++) {
                            int timeLeft = 5 - i;

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {

                                if (timeLeft > 0) {
                                    Bukkit.broadcast(Component.text("§c⚠ Restarting in " + timeLeft + " seconds..."));

                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                                    }
                                }

                            }, i * 20L);
                        }

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {

                            Bukkit.broadcast(Component.text("§e⚠ Restarting now!"));

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                            }

                            // This triggers restart instead of shutdown (Paper supports this)
                            plugin.getServer().restart();

                        }, 100L);
                        admin.closeInventory();
                    }

                    case BOOK -> {
                        if (!checkPermission(admin, "atomhub.server.info")) return;
                        // TPS
                        double[] tps = Bukkit.getServer().getTPS();

                        Runtime rt = Runtime.getRuntime();
                        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                        long max = rt.maxMemory() / 1024 / 1024;

                        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
                        long uptimeMins = uptimeMs / 1000 / 60;

                        admin.sendMessage(Component.text(
                                "§6§lAtomHub Server Info\n" +
                                        "§7----------------------------\n" +
                                        "§eTPS: §f" + String.format("%.1f / %.1f / %.1f", tps[0], tps[1], tps[2]) + "\n" +
                                        "§ePlayers Online: §f" + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers() + "\n" +
                                        "§eMemory Usage: §f" + used + "MB / " + max + "MB\n" +
                                        "§eUptime: §f" + uptimeMins + " minutes\n" +
                                        "§eServer Name: §f" + Bukkit.getServer().getName() + "\n" +
                                        "§eIP: §f" + Bukkit.getIp() + ":" + Bukkit.getPort() + "\n" +
                                        "§7----------------------------"
                        ));
                        admin.closeInventory();
                    }

                    case LEVER -> {
                        // Toggle maintenance mode
                        if (!checkPermission(admin, "atomhub.server.maintenance")) return;

                        boolean newState = !plugin.isMaintenanceMode();
                        plugin.setMaintenanceMode(newState);

                        if (newState) {
                            Bukkit.broadcast(Component.text("§6[AtomHubVelocity] - §c⚠ Server is now in maintenance mode. Only OPs can join."));
                            // Kick all non-OPs
                            Bukkit.getOnlinePlayers().forEach(p -> {
                                if (!p.isOp()) {
                                    p.kick(Component.text("§cServer is in maintenance mode. Only OPs can join."));
                                }
                            });
                        } else {
                            Bukkit.broadcast(Component.text("§6[AtomHubVelocity] - §a ✅ Server maintenance mode disabled. All players can join."));
                        }
                    }

                    case GRASS_BLOCK -> new WorldManagerMenu(plugin, admin, world).open();

                    case WHITE_BANNER -> {
                        // Whitelist Management
                        if (!checkPermission(admin, "atomhub.server.whitelist")) return;
                        new WhitelistMenu(admin, plugin, 1).open();
                    }

                    case CLOCK -> {
                        // Performance Monitoring
                        if (!checkPermission(admin, "atomhub.server.performance")) return;
                        new PerformanceMenu(admin, plugin).open();
                    }

                    // Add handlers for other ServerMenu items:
                    case REDSTONE_LAMP -> {
                        // Lag Prevention
                        if (!checkPermission(admin, "atomhub.server.lag")) return;
                        new LagPreventionMenu(plugin, admin).open();
                    }

//                    case IRON_BARS -> {
//                        // Chunk Control
//                        if (!checkPermission(admin, "atomhub.server.chunks")) return;
//                        openChunkControlMenu(admin);
//                    }
//
//                    case ENDER_EYE -> {
//                        // Auto-Save Config
//                        if (!checkPermission(admin, "atomhub.server.autosave")) return;
//                        configureAutoSave(admin);
//                    }
                }
            }

            case "WHITELIST" -> handleWhitelistMenu(admin, clicked, ev);
            case "VIEW_DISTANCE" -> handleViewDistanceMenu(admin, clicked, ev);
            case "PERFORMANCE" -> handlePerformanceMenu(admin, clicked, ev);
            case "LAG_PREVENTION" -> new LagPreventionMenu(plugin, admin).handleClick(clicked);
            case "CHUNK_CONTROL" -> new ChunkControlMenu(plugin, admin).handleClick(clicked);
            case "AUTOSAVE" -> new AutoSaveMenu(plugin, admin).handleClick(clicked);
            case "WORLD_MANAGER" -> new WorldManagerMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);
            case "WORLD_SETTINGS" -> handleWorldSettingsMenu(admin, clicked);


            case "NETWORK" -> {
                if (clicked == Material.ARROW) {
                    plugin.getGuiManager().openMainMenu(admin);
                    return;
                }

                switch (clicked) {
                    case ANVIL -> new NetworkBanMenu(plugin, admin, 1).open();
                    case BARRIER -> new NetworkKickMenu(plugin, admin, 1).open();
                    case PAPER -> new NetworkMuteMenu(plugin, admin, 1).open();
                    case COMPASS -> {
                        // First open player selector for server transfer
                        admin.sendMessage(Component.text("§6[AtomHub] §7Select a player to transfer"));
                        plugin.getGuiManager().openNetworkPlayerList(admin, 1);
                    }
                    case PLAYER_HEAD -> {
                        // Network Players
                        new NetworkPlayersMenu(plugin, admin, 1, true).open();
                    }
//                    case REDSTONE_BLOCK -> {
//                        // Server Control
//                        openNetworkServerControl(admin);
//                    }
//                    case COMMAND_BLOCK -> {
//                        // Network Commands
//                        openNetworkCommands(admin);
//                    }
                    case BOOK -> {
                        // Network Info
                        showNetworkInfo(admin);
                    }
                    case ENDER_EYE -> {
                        // Network Broadcast
                        new NetworkBroadcastMenu(plugin, admin).open();
                    }

                    case LEVER -> {
                        // Network Maintenance Mode
                        toggleNetworkMaintenance(admin);
                    }
                    default -> admin.sendMessage(Component.text("§cFeature not implemented yet"));
                }
            }

            case "NETWORK_BAN" -> handleNetworkBanMenu(admin, clicked, ev);
            case "NETWORK_KICK" -> handleNetworkKickMenu(admin, clicked, ev);
            case "NETWORK_MUTE" -> handleNetworkMuteMenu(admin, clicked, ev);
            case "NETWORK_TRANSFER" -> handleNetworkTransferMenu(admin, clicked, ev);
            case "NETWORK_PLAYERS" -> handleNetworkPlayersMenu(admin, clicked, ev);

            case "NETWORK_PLAYERLIST" -> {
                int page = 1;
                String title = PlainTextComponentSerializer.plainText().serialize(ev.getView().title());
                try {
                    String pageStr = title.replaceAll("[^0-9]", "");
                    if (!pageStr.isEmpty()) page = Integer.parseInt(pageStr);
                } catch (NumberFormatException ignored) {}

                if (clicked == Material.PLAYER_HEAD) {
                    String targetName = item.getItemMeta().getDisplayName().toString();
                    Player target = Bukkit.getPlayerExact(targetName);
                    if (target != null) plugin.getGuiManager().openNetworkTransferMenu(admin, target);
                } else if (clicked == Material.ARROW) {
                    int slot = ev.getSlot();
                    if (slot == 21 && page > 1) plugin.getGuiManager().openNetworkPlayerList(admin, page - 1);
                    else if (slot == 23) plugin.getGuiManager().openNetworkPlayerList(admin, page + 1);
                    else plugin.getGuiManager().openNetworkMenu(admin);
                }
            }

            case "NETWORK_BROADCAST" -> handleNetworkBroadcastMenu(admin, clicked, ev);

            case "WORLD" -> {
                if (clicked == Material.ARROW) {
                    plugin.getGuiManager().openMainMenu(admin);
                    return;
                }

                World world = admin.getWorld();
                if (!checkPermission(admin, "atomhub.world.manage")) return;

                switch (clicked) {
                    case CLOCK -> new WorldTimeMenu(plugin, admin, world).open();   // open time menu
                    case PAPER -> new WorldWeatherMenu(plugin, admin, world).open(); // open weather menu
                    case GRASS_BLOCK -> new WorldEnvironmentMenu(plugin, admin, world).open(); // open environment menu
                    case BEACON -> new WorldEffectsMenu(plugin, admin, world).open(); // open
//                    case FIREWORK_ROCKET -> new WorldEventsMenu(plugin, admin, world).open();
//                    case ENDER_CHEST -> new MysticalEventsMenu(plugin, admin, world).open();

                    // Quick set day
                    case DAYLIGHT_DETECTOR -> {
                        world.setTime(1000);
                        admin.sendMessage(Component.text("§6[AtomHub] - §aTime set to day."));
                    }
                    case REDSTONE_TORCH -> {
                        world.setTime(14000);
                        admin.sendMessage(Component.text("§6[AtomHub] - §aTime set to night."));
                    }
                    // Toggle weather
                    case BARRIER -> {
                        boolean newWeather = !world.hasStorm();
                        world.setStorm(newWeather);
                        if (newWeather) {
                            admin.sendMessage(Component.text("§6[AtomHub] - §aWeather enabled (storming)."));
                        } else {
                            admin.sendMessage(Component.text("§6[AtomHub] - §aWeather disabled (clear)."));
                        }
                    }
                    // Kill all mobs
                    case TNT -> {
                        long killed = world.getEntities().stream()
                                .filter(e -> e instanceof org.bukkit.entity.Mob)
                                .peek(org.bukkit.entity.Entity::remove)
                                .count();
                        admin.sendMessage(Component.text("[AtomHub] - §aKilled " + killed + " mobs in the world."));
                    }

                    // New: Toggle mob spawning
                    case SPAWNER -> {
                        boolean allowSpawn = world.getAllowMonsters();
                        world.setSpawnFlags(!allowSpawn, world.getAllowAnimals());
                        admin.sendMessage(Component.text("§6[AtomHub] - §aMob spawning " +
                                (!allowSpawn ? "enabled" : "disabled") + "."));
                    }
                    case TOTEM_OF_UNDYING -> {
                        int healed = 0;
                        for (Player p : world.getPlayers()) {
                            if (p.isOnline()) {
                                p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getValue());
                                p.setFoodLevel(20);
                                p.setSaturation(20f);
                                p.removePotionEffect(PotionEffectType.POISON);
                                p.removePotionEffect(PotionEffectType.WITHER);
                                p.sendMessage(Component.text("§a[World] - §7You have been healed by an admin: " + admin.getName()));
                                healed++;
                            }
                        }
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Healed §a" + healed + "§7 players"));
                    }
                    case EXPERIENCE_BOTTLE -> {
                        int xpGiven = 0;
                        for (Player p : world.getPlayers()) {
                            if (p.isOnline()) {
                                p.giveExp(100); // Give 100 XP points
                                p.sendMessage(Component.text("§a[World] - §7You received §e100 XP§7 from an admin: " + admin.getName()));
                                xpGiven++;
                            }
                        }
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Gave §e100 XP§7 to §a" + xpGiven + "§7 players"));
                    }
                    case ENDER_PEARL -> {
                        int teleported = 0;
                        for (Player p : world.getPlayers()) {
                            if (p.isOnline() && p != admin) {
                                double x = (Math.random() - 0.5) * 500; // Random within ±250 blocks
                                double z = (Math.random() - 0.5) * 500;
                                Location loc = new Location(world, x, world.getHighestBlockYAt((int)x, (int)z) + 1, z);
                                p.teleport(loc);
                                p.sendMessage(Component.text("§c[World] - §7You were randomly teleported!"));
                                teleported++;
                            }
                        }
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Randomly teleported §d" + teleported + "§7 players"));
                    }
                    case IRON_SWORD -> {
                        boolean newPvP = !world.getPVP();
                        world.setPVP(newPvP);

                        String status = newPvP ? "§cENABLED" : "§aDISABLED";
                        Component message = Component.text("§4[World] - §7PvP is now " + status + "§7!");

                        // Broadcast to all players in the world
                        for (Player p : world.getPlayers()) {
                            p.sendMessage(message);
                        }

                        admin.sendMessage(Component.text("§6[AtomHub] - §7PvP " + (newPvP ? "§cenabled" : "§adisabled") + "§7 globally"));
                    }
                    case EMERALD -> {
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Starting §eEco Storm§7 for 30 seconds!"));

                        // Broadcast to all players
                        for (Player p : world.getPlayers()) {
                            p.sendMessage(Component.text("§e[World] - §6ECO STORM §7incoming! Items will rain from the sky!"));
                        }

                        // Schedule item drops
                        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                            for (Player p : world.getPlayers()) {
                                if (p.isOnline()) {
                                    Location dropLoc = p.getLocation().add(
                                            (Math.random() - 0.5) * 10,
                                            10,
                                            (Math.random() - 0.5) * 10
                                    );

                                    // Drop random items
                                    Material[] valuableItems = {
                                            Material.GOLD_INGOT, Material.IRON_INGOT, Material.DIAMOND,
                                            Material.EMERALD, Material.LAPIS_LAZULI, Material.REDSTONE,
                                            Material.COAL, Material.EXPERIENCE_BOTTLE
                                    };

                                    Material randomItem = valuableItems[(int)(Math.random() * valuableItems.length)];
                                    world.dropItem(dropLoc, new ItemStack(randomItem, 1 + (int)(Math.random() * 3)));

                                    // Show particle effects
                                    world.spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 2, 0), 20, 0.5, 1, 0.5, 0.1);
                                }
                            }
                        }, 0L, 20L); // Every second

                        // Stop after 30 seconds
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            Bukkit.getScheduler().cancelTask(taskId);
                            for (Player p : world.getPlayers()) {
                                p.sendMessage(Component.text("§e[World] - §7The §6Eco Storm §7has ended!"));
                            }
                            admin.sendMessage(Component.text("§6[AtomHub] - §7Eco Storm ended"));
                        }, 600L); // 30 seconds
                    }
                }
            }

            case "WORLD_TIME" -> new WorldTimeMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);
            case "WORLD_WEATHER" -> new WorldWeatherMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);
            case "WORLD_ENV" -> new WorldEnvironmentMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);
            case "WORLD_EFFECTS" -> new WorldEffectsMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);
//            case "WORLD_EVENTS" -> new WorldEventsMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);
//            case "MYSTICAL_EVENTS" -> new MysticalEventsMenu(plugin, admin, admin.getWorld()).handleClick(admin, clicked);

            case "PLAYER" -> {
                String targetName = PlainTextComponentSerializer.plainText().serialize(ev.getView().title())
                        .replace("§8Player: §f", "");
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) { admin.sendMessage(Component.text("Target offline.")); admin.closeInventory(); return; }

                if (!checkPermission(admin, "atomhub.player.manage")) return;

                switch (clicked) {
                    // ----------------------------
                    // Ban Options
                    // ----------------------------
                    case IRON_BARS -> {
                        String reason = "Server banned by admin: " + admin.getName();
                        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, admin.getName());
                        target.kick(Component.text("You were server banned: " + reason));

                        admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " has been server banned."));
                        admin.closeInventory();
                    }

                    case ANVIL -> {
                        String reason = "Network banned by admin: " + admin.getName();
                        plugin.getMessenger().send("network:ban", admin, target, reason, "");
                        target.kick(Component.text("You were network banned: " + reason));
                        admin.sendMessage(Component.text("§6[AtomHub] - §7" + target.getName() + " has been network banned."));
                        admin.closeInventory();
                    }

                    // ----------------------------
                    // Kick Options
                    // ----------------------------
                    case BLAZE_ROD -> {
                        target.kick(Component.text("Server kicked by admin: " + admin.getName()));
                        admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " has been server kicked."));
                        admin.closeInventory();
                    }

                    case BARRIER -> {
                        String reason = "Network kicked by admin: " + admin.getName();
                        plugin.getMessenger().send("network:kick", admin, target, reason, "");
                        admin.sendMessage(Component.text("§6[AtomHub] - §7" + target.getName() + " has been network kicked."));
                        admin.closeInventory();
                    }

                    // ----------------------------
                    // Teleport Options
                    // ----------------------------
                    case ENDER_PEARL -> {
                        admin.teleport(target.getLocation());
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Teleported to §f" + target.getName()));
                    }
                    case ENDER_EYE -> {
                        target.teleport(admin.getLocation());
                        target.sendMessage(Component.text("§5[World] - §7You were teleported to §f" + admin.getName()));
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Teleported §f" + target.getName() + "§7 to you"));
                    }

                    // ----------------------------
                    // Freeze Options
                    // ----------------------------
                    case ICE, PACKED_ICE -> {
                        boolean frozenNow = !plugin.getFreezeManager().isFrozen(target); // determine new state

                        // toggle freeze
                        plugin.getFreezeManager().toggle(target);

                        if (frozenNow) {
                            admin.sendMessage(Component.text("[AtomHub] - Player " + target.getName() + " has been frozen."));
                            target.sendMessage(Component.text("§cYou have been frozen by admin: " + admin.getName()));
                        } else {
                            admin.sendMessage(Component.text("[AtomHub] - Player " + target.getName() + " has been unfrozen."));
                            target.sendMessage(Component.text("§aYou have been unfrozen by admin: " + admin.getName()));
                        }

                        // refresh the menu so the freeze icon updates
                        plugin.getGuiManager().openPlayerMenu(admin, target);
                    }

                    // ----------------------------
                    // Mute Options
                    // ----------------------------
                    case RED_DYE, LIME_DYE -> {
                        // Toggle network mute for the target
                        boolean nowMuted = !plugin.getMuteManager().isNetworkMuted(target.getUniqueId());
                        plugin.getMuteManager().setNetworkMute(target.getUniqueId(), nowMuted);

                        if (nowMuted) {
                            admin.sendMessage(Component.text("§6[AtomHub] - §7" + target.getName() + " has been muted network-wide."));
                            target.sendMessage(Component.text("§cYou have been muted by admin: " + admin.getName()));

                            // Notify Velocity / network - pass server name
                            String serverName = plugin.getConfig().getString("plugin.server-name", plugin.getServer().getName());
                            plugin.getMessenger().send("network:mute", admin, target, "Muted by admin: " + admin.getName(), serverName);
                        } else {
                            admin.sendMessage(Component.text("§6[AtomHub] - §7" + target.getName() + " has been unmuted."));
                            target.sendMessage(Component.text("§aYou have been unmuted by admin: " + admin.getName()));

                            String serverName = plugin.getConfig().getString("plugin.server-name", plugin.getServer().getName());
                            plugin.getMessenger().send("network:unmute", admin, target, "Unmuted by admin: " + admin.getName(), serverName);
                        }

                        // Refresh the player menu to update button states
                        plugin.getGuiManager().openPlayerMenu(admin, target);
                    }

                    // ----------------------------
                    // Health Options
                    // ----------------------------
                    case GOLDEN_APPLE -> {
                        OpenMenus.put(admin.getUniqueId(), target); // store target for this admin
                        new PlayerHealthMenu(plugin, admin, target).open();
                    }

                    case SUGAR -> {
                        OpenMenus.put(admin.getUniqueId(), target);
                        new PlayerMovementMenu(plugin, admin, target).open();
                    }

                    // New: Effects menu
                    case POTION -> {
                        OpenMenus.put(admin.getUniqueId(), target);
                        new PlayerEffectsMenu(plugin, admin, target).open();
                    }

                    case FIREWORK_ROCKET -> {
                        target.setVelocity(target.getLocation().getDirection().multiply(2).setY(2));
                        admin.sendMessage(Component.text("[AtomHub] - Launched " + target.getName() + " into the air!"));
                        target.sendMessage(Component.text("§cYou were launched by an admin."));
                    }

                    case TNT -> {
                        try {
                            // Create explosion at player's location
                            Location explosionLoc = target.getLocation();
                            boolean explosionSuccess = target.getWorld().createExplosion(
                                    explosionLoc,
                                    4.0f,  // Power (radius)
                                    false, // Set fire
                                    false  // Break blocks
                            );

                            if (explosionSuccess) {
                                // Damage the player (explosions by default don't damage the source)
                                target.damage(15.0, admin); // 7.5 hearts of damage

                                // Add visual effects
                                target.getWorld().spawnParticle(Particle.EXPLOSION,
                                        explosionLoc, 10, 0.5, 0.5, 0.5, 0.1);
                                target.getWorld().spawnParticle(Particle.EXPLOSION,
                                        explosionLoc, 20, 1, 1, 1, 0.2);

                                // Play explosion sound
                                target.playSound(explosionLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE,
                                        1.0f, 1.0f);

                                // Send messages
                                target.sendMessage(Component.text("§4[World] §c§lBOOM! §7You were exploded by an admin!"));
                                admin.sendMessage(Component.text("§6[AtomHub] §7Created explosion at §f" + target.getName()));

                                // Add fire resistance to prevent death by burning
                                target.addPotionEffect(new PotionEffect(
                                        PotionEffectType.FIRE_RESISTANCE,
                                        200, // 10 seconds
                                        0,
                                        true,
                                        false
                                ));
                            }
                        } catch (Exception e) {
                            admin.sendMessage(Component.text("§cFailed to create explosion: " + e.getMessage()));
                            plugin.getLogger().warning("Explosion failed: " + e.getMessage());
                        }
                    }

                    case LIGHTNING_ROD -> {
                        try {
                            Location strikeLoc = target.getLocation();

                            // Strike actual lightning (causes damage and fire)
                            org.bukkit.entity.LightningStrike lightning = target.getWorld().strikeLightning(strikeLoc);

                            // Additional visual effects
                            target.getWorld().strikeLightningEffect(strikeLoc.add(2, 0, 0)); // Additional effect bolt
                            target.getWorld().strikeLightningEffect(strikeLoc.add(-2, 0, 2)); // Another effect bolt

                            // Play thunder sound
                            target.playSound(strikeLoc, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                                    1.0f, 1.0f);
                            target.playSound(strikeLoc, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_IMPACT,
                                    1.0f, 1.0f);

                            // Visual particles
//                            target.getWorld().spawnParticle(org.bukkit.Particle.FLASH,
//                                    strikeLoc.add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0);

                            // Damage the player (lightning does 5 damage = 2.5 hearts)
                            // But we'll add a bit more for effect
                            target.damage(8.0, admin); // 4 hearts

                            // Send messages
                            target.sendMessage(Component.text("§e[World] - §7⚡You were struck by lightning!"));
                            admin.sendMessage(Component.text("§6[AtomHub] - §7Struck §f" + target.getName() + "§7 with lightning"));

                            // Add small fire effect (will be put out by rain or after time)
                            strikeLoc.getBlock().getRelative(0, 1, 0).setType(Material.FIRE);

                            // Give fire resistance to prevent death
                            target.addPotionEffect(new PotionEffect(
                                    PotionEffectType.FIRE_RESISTANCE,
                                    100, // 5 seconds
                                    0,
                                    true,
                                    false
                            ));

                        } catch (Exception e) {
                            admin.sendMessage(Component.text("§cFailed to strike lightning: " + e.getMessage()));
                            plugin.getLogger().warning("Lightning strike failed: " + e.getMessage());
                        }
                    }

                    case STICK -> {
                        OpenMenus.put(admin.getUniqueId(), target); // store target for this admin
                        new PlayerOptionsMenu(plugin, admin, target).open();
                    }

                    case GLOWSTONE_DUST -> {
                        boolean glowing = plugin.getGlowManager().toggleGlow(target);
                        if (glowing) {
                            target.sendMessage(Component.text("§b[World] - §7You are now glowing!"));
                            admin.sendMessage(Component.text("§6[AtomHub] - §7Glow enabled for §f" + target.getName()));
                        } else {
                            target.sendMessage(Component.text("§b[World] - §7Glow disabled"));
                            admin.sendMessage(Component.text("§6[AtomHub] - §7Glow disabled for §f" + target.getName()));
                        }
                    }

                    // ----------------------------
                    // Gamemode Options
                    // ----------------------------
                    case GRASS_BLOCK -> setGamemode(target, GameMode.SURVIVAL, admin);
                    case DIAMOND -> setGamemode(target, GameMode.CREATIVE, admin);
                    case SPYGLASS -> setGamemode(target, GameMode.SPECTATOR, admin);
                    case WOODEN_AXE -> setGamemode(target, GameMode.ADVENTURE, admin);

                    // ----------------------------
                    // Inventory Options
                    // ----------------------------
                    case CHEST -> {
                        admin.openInventory(target.getInventory());
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Viewing §f" + target.getName() + "'s§7 inventory"));
                    }

                    case ENDER_CHEST -> {
                        admin.openInventory(target.getEnderChest());
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Viewing §f" + target.getName() + "'s§7 ender chest"));
                    }

                    case COMPASS -> {
                        admin.setCompassTarget(target.getLocation());
                        admin.sendMessage(Component.text("§6[AtomHub] - §7Compass now tracks §f" + target.getName()));
                    }

                    case BOOK -> showPlayerInfo(target, admin);
                    case LEAD -> toggleLeash(target, admin);

                    // ----------------------------
                    // Back
                    // ----------------------------
                    case ARROW -> plugin.getGuiManager().openPlayerList(admin, 1); // back to player list
                }
            }

            case "PLAYER_OPTIONS" -> {
                Player target = OpenMenus.get(admin.getUniqueId());
                if (target != null) {
                    switch (clicked) {
                        case LEATHER_BOOTS, FEATHER -> {
                            boolean flying = !target.getAllowFlight();
                            target.setAllowFlight(flying);
                            target.setFlying(flying);
                            if (target.getAllowFlight()) {
                                admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " can now fly."));
                                target.sendMessage(Component.text("§aYou can now fly!"));
                            } else {
                                admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " can no longer fly."));
                                target.sendMessage(Component.text("§cFlight disabled."));
                            }
                            new PlayerOptionsMenu(plugin, admin, target).open();
                        }
                        case GLOWSTONE, REDSTONE_LAMP -> {
                            boolean glowing = plugin.getGlowManager().toggleGlow(target);

                            if (glowing) {
                                admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " is now glowing."));
                                target.sendMessage(Component.text("§aYou are now glowing!"));
                            } else {
                                admin.sendMessage(Component.text("[AtomHub] - " + target.getName() + " is no longer glowing."));
                                target.sendMessage(Component.text("§cGlow disabled."));
                            }

                            new PlayerOptionsMenu(plugin, admin, target).open();
                        }
                        case ENDER_EYE -> {
                            World world = target.getWorld();
                            double x = Math.random() * world.getWorldBorder().getSize() - (world.getWorldBorder().getSize()/2);
                            double z = Math.random() * world.getWorldBorder().getSize() - (world.getWorldBorder().getSize()/2);
                            double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
                            target.teleport(new Location(world, x, y, z));
                            admin.sendMessage(Component.text("[AtomHub] - Teleported " + target.getName() + " randomly!"));
                            target.sendMessage(Component.text("§cYou were randomly teleported by an admin."));
                        }
                        case ARROW -> {
                            plugin.getGuiManager().openPlayerMenu(admin, target);
                            OpenMenus.remove(admin.getUniqueId()); // remove from map
                        }
                    }
                }
            }

            case "PLAYER_HEALTH" -> {
                Player target = OpenMenus.get(admin.getUniqueId());
                if (target != null) {
                    new PlayerHealthMenu(plugin, admin, target).handleClick(admin, clicked);
                }
            }

            case "PLAYER_EFFECTS" -> {
                Player target = OpenMenus.get(admin.getUniqueId());
                if (target != null) {
                    new PlayerEffectsMenu(plugin, admin, target).handleClick(admin, clicked);
                }
            }

            case "PLAYER_MOVEMENT" -> {
                Player target = OpenMenus.get(admin.getUniqueId());
                if (target != null) {
                    new PlayerMovementMenu(plugin, admin, target).handleClick(admin, clicked);
                }
            }

            case "PLAYERLIST" -> {
                int page = 1;
                String title = PlainTextComponentSerializer.plainText().serialize(ev.getView().title());
                try {
                    String pageStr = title.replaceAll("[^0-9]", "");
                    if (!pageStr.isEmpty()) page = Integer.parseInt(pageStr);
                } catch (NumberFormatException ignored) {}

                if (clicked == Material.PLAYER_HEAD) {
                    String targetName = item.getItemMeta().getDisplayName().toString();
                    Player target = Bukkit.getPlayerExact(targetName);
                    if (target != null) plugin.getGuiManager().openPlayerMenu(admin, target);
                } else if (clicked == Material.ARROW) {
                    int slot = ev.getSlot();
                    if (slot == 21 && page > 1) plugin.getGuiManager().openPlayerList(admin, page - 1);
                    else if (slot == 23) plugin.getGuiManager().openPlayerList(admin, page + 1);
                    else plugin.getGuiManager().openMainMenu(admin);
                }
            }
        }
    }

    // Helper methods
    private boolean checkPermission(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            player.sendMessage(Component.text("§cYou don't have permission to do that!"));
            return false;
        }
        return true;
    }

    private void setGamemode(Player target, GameMode gamemode, Player admin) {
        target.setGameMode(gamemode);
        target.sendMessage(Component.text("§a[World] - §7Your gamemode was changed to §f" + gamemode.toString().toLowerCase()));
        admin.sendMessage(Component.text("§6[AtomHub] - §7Set §f" + target.getName() + "'s§7 gamemode to §f" + gamemode.toString().toLowerCase()));
        plugin.getGuiManager().openPlayerMenu(admin, target);
    }

    private void showPlayerInfo(Player target, Player admin) {
        admin.sendMessage(Component.text("§6§lPlayer Information"));
        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§eName: §f" + target.getName()));
        admin.sendMessage(Component.text("§eUUID: §f" + target.getUniqueId()));
        admin.sendMessage(Component.text("§eHealth: §f" + String.format("%.1f", target.getHealth()) + "§7/§f" + String.format("%.1f", target.getMaxHealth())));
        admin.sendMessage(Component.text("§eFood: §f" + target.getFoodLevel() + "§7/§f20"));
        admin.sendMessage(Component.text("§eLevel: §f" + target.getLevel() + "§7 (§f" + String.format("%.1f", target.getExp()) + "§7)"));
        admin.sendMessage(Component.text("§eGamemode: §f" + target.getGameMode()));
        admin.sendMessage(Component.text("§eWorld: §f" + target.getWorld().getName()));
        admin.sendMessage(Component.text("§eLocation: §f" +
                String.format("%.0f, %.0f, %.0f", target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ())));
        admin.sendMessage(Component.text("§ePing: §f" + target.getPing() + "ms"));
        admin.sendMessage(Component.text("§eIP: §f" + target.getAddress().getAddress().getHostAddress()));
        admin.sendMessage(Component.text("§eFirst Played: §f" +
                new java.util.Date(target.getFirstPlayed()).toString()));
        admin.sendMessage(Component.text("§7────────────────────"));
    }

    private void toggleLeash(Player target, Player admin) {
        // This is a visual effect - in real implementation you'd use entities
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2, true, false));
        target.sendMessage(Component.text("§f[World] - §7You feel restrained!"));
        admin.sendMessage(Component.text("§6[AtomHub] - §7Applied leash effect to §f" + target.getName()));
    }

    private void handleNetworkBanMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openNetworkMenu(admin);
            pendingBans.remove(admin.getUniqueId());
            return;
        }

        if (clicked == Material.PLAYER_HEAD) {
            String targetName = ev.getCurrentItem().getItemMeta().getDisplayName();
            targetName = targetName.replace("§f", "");
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                pendingBans.put(admin.getUniqueId(), target);
                admin.sendMessage(Component.text("§6[AtomHub] - §7Select a ban reason for §f" + target.getName()));
            }
            return;
        }

        // Handle ban reason items
        Player target = pendingBans.get(admin.getUniqueId());
        if (target == null) {
            admin.sendMessage(Component.text("§c[AtomHub] - §7No player selected. Click a player first."));
            return;
        }

        switch (clicked) {
            case TNT -> banPlayerWithReason(admin, target, "Hacking");
            case BOOK -> banPlayerWithReason(admin, target, "Advertising");
            case IRON_SWORD -> banPlayerWithReason(admin, target, "Exploiting");
            case PAPER -> banPlayerWithReason(admin, target, "Harassment");
            case BARRIER -> {
                admin.sendMessage(Component.text("§6[AtomHub] - §7Enter ban reason in chat:"));
                admin.closeInventory();
            }
        }
    }

    private void banPlayerWithReason(Player admin, Player target, String reason) {
        String fullReason = reason + " - Banned by: " + admin.getName();
        plugin.getMessenger().send("network:ban", admin, target, fullReason, "");
        target.kick(Component.text("§cYou have been network banned.\n§7Reason: §f" + reason));
        admin.sendMessage(Component.text("§6[AtomHub] - §7" + target.getName() + " has been network banned."));
        admin.closeInventory();
        pendingBans.remove(admin.getUniqueId());
    }

    private void handleNetworkKickMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openNetworkMenu(admin);
            pendingKicks.remove(admin.getUniqueId());
            return;
        }

        if (clicked == Material.PLAYER_HEAD) {
            String targetName = ev.getCurrentItem().getItemMeta().getDisplayName();
            targetName = targetName.replace("§f", "");
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                pendingKicks.put(admin.getUniqueId(), target);
                admin.sendMessage(Component.text("§6[AtomHub] - §7Select a kick reason for §f" + target.getName()));
            }
            return;
        }

        Player target = pendingKicks.get(admin.getUniqueId());

        if (clicked == Material.TNT) {
            if (target != null) {
                kickPlayerWithReason(admin, target, "General violation");
            } else {
                // Mass kick all non-staff
                int kicked = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.isOp() && !p.hasPermission("atomhub.staff")) {
                        p.kick(Component.text("§cMass kick by network admin"));
                        kicked++;
                    }
                }
                admin.sendMessage(Component.text("§6[AtomHub] - §7Kicked §c" + kicked + "§7 non-staff players"));
            }
            return;
        }

        if (target == null) {
            admin.sendMessage(Component.text("§c[AtomHub] - §7No player selected. Click a player first."));
            return;
        }

        // Kick reasons
        String reason = "Kicked by admin";
        switch (clicked) {
            case IRON_SWORD -> reason = "Hacking";
            case PAPER -> reason = "Harassment";
            case BARRIER -> reason = "Rule violation";
            case BOOK -> reason = "Advertising";
        }

        kickPlayerWithReason(admin, target, reason);
    }

    private void kickPlayerWithReason(Player admin, Player target, String reason) {
        String fullReason = reason + " - By: " + admin.getName();
        plugin.getMessenger().send("network:kick", admin, target, fullReason, "");
        target.kick(Component.text("§cYou have been network kicked.\n§7Reason: §f" + reason));
        admin.sendMessage(Component.text("§6[AtomHub] - §7" + target.getName() + " has been network kicked."));
        admin.closeInventory();
        pendingKicks.remove(admin.getUniqueId());
    }

    private void handleNetworkMuteMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openNetworkMenu(admin);
            return;
        }

        if (clicked == Material.PLAYER_HEAD) {
            String targetName = ev.getCurrentItem().getItemMeta().getDisplayName();
            targetName = targetName.replace("§f", "").split(" ")[0]; // Remove status
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                boolean isMuted = plugin.getMuteManager().isNetworkMuted(target.getUniqueId());
                plugin.getMuteManager().setNetworkMute(target.getUniqueId(), !isMuted);

                if (!isMuted) {
                    admin.sendMessage(Component.text("§6[AtomHub] - §7Muted §f" + target.getName()));
                    target.sendMessage(Component.text("§cYou have been muted network-wide"));
                } else {
                    admin.sendMessage(Component.text("§6[AtomHub] - §7Unmuted §f" + target.getName()));
                    target.sendMessage(Component.text("§aYou have been unmuted"));
                }
            }
        }

        if (clicked == Material.PAPER) {
            // Mass mute all
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp()) {
                    plugin.getMuteManager().setNetworkMute(p.getUniqueId(), true);
                }
            }
            admin.sendMessage(Component.text("§6[AtomHub] - §7Muted all non-OP players"));
        }

        if (clicked == Material.MILK_BUCKET) {
            // Mass unmute all
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getMuteManager().setNetworkMute(p.getUniqueId(), false);
            }
            admin.sendMessage(Component.text("§6[AtomHub] - §7Unmuted all players"));
        }
    }

    private void handleNetworkTransferMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        String targetName = PlainTextComponentSerializer.plainText().serialize(ev.getView().title())
                .replace("§8Transfer: §f", "");
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            admin.sendMessage(Component.text("Target offline."));
            admin.closeInventory(); return;
        }

        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openNetworkPlayerList(admin, 1);
            OpenMenus.remove(admin.getUniqueId());
            return;
        }

        // Handle server transfers
        switch (clicked) {
            case COMPASS -> transferPlayer(admin, target, "lobby");
            case DIAMOND_BLOCK -> transferPlayer(admin, target, "creative");
            case GRASS_BLOCK -> transferPlayer(admin, target, "skyblock");
            case END_STONE -> transferPlayer(admin, target, "smp");
            case DIAMOND_SWORD -> transferPlayer(admin, target, "kitpvp");
            case NAME_TAG -> {
                // Custom server name via chat
                admin.sendMessage(Component.text("§6[AtomHub] - §7Enter server name to transfer §f" +
                        target.getName() + "§7 to:"));
                admin.sendMessage(Component.text("§7(Type 'cancel' to cancel)"));
                admin.closeInventory();

                // Store the admin and target for chat input
                pendingTransfers.put(admin.getUniqueId(), target);
            }
        }
    }

    private void showNetworkInfo(Player admin) {
        admin.sendMessage(Component.text("§6§lNetwork Information"));
        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§eNetwork Name: §fAtomHub Network"));
        admin.sendMessage(Component.text("§eConnected Servers: §f3"));
        admin.sendMessage(Component.text("§eTotal Players: §f" + Bukkit.getOnlinePlayers().size()));
        admin.sendMessage(Component.text("§eNetwork Uptime: §f24h 30m"));
        admin.sendMessage(Component.text("§eMaintenance Mode: §f" + (plugin.isMaintenanceMode() ? "§cON" : "§aOFF")));
        admin.sendMessage(Component.text("§eActive Bans: §f12"));
        admin.sendMessage(Component.text("§eActive Mutes: §f3"));
        admin.sendMessage(Component.text("§7────────────────────"));
    }

    private void toggleNetworkMaintenance(Player admin) {
        boolean newState = !plugin.isMaintenanceMode();
        plugin.setMaintenanceMode(newState);

        if (newState) {
            Bukkit.broadcast(Component.text("§6[Network] - §c⚠ Network maintenance enabled!"));
            // Kick all non-OPs from all servers (would need proxy communication)
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp()) {
                    p.kick(Component.text("§cNetwork maintenance in progress"));
                }
            }
            admin.sendMessage(Component.text("§6[AtomHub] - §7Network maintenance §cenabled"));
        } else {
            Bukkit.broadcast(Component.text("§6[Network] - §a✅ Network maintenance disabled"));
            admin.sendMessage(Component.text("§6[AtomHub] - §7Network maintenance §adisabled"));
        }
    }

    private void transferPlayer(Player admin, Player target, String server) {
        // This sends a plugin message to Velocity to transfer the player
        admin.sendMessage(Component.text("§6[AtomHub] - §7Transferring §f" + target.getName() +
                "§7 to §e" + server + "§7 server..."));
        target.sendMessage(Component.text("§a[Network] - §7Transferring to §e" + server + "§7 server..."));

        // Send the transfer request via NetworkMessenger
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);

            // Write data in the order expected by Velocity
            out.writeUTF(target.getUniqueId().toString()); // Target UUID
            out.writeUTF(target.getName());                // Target name
            out.writeUTF(server);                          // Target server name
            out.writeUTF(admin.getUniqueId().toString());  // Admin UUID
            out.writeUTF(admin.getName());                 // Admin name

            // Send the plugin message
            plugin.getMessenger().sendTransfer(admin, target, server);

            // Optional: Log the transfer
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("[Transfer] " + admin.getName() + " transferring " +
                        target.getName() + " to server: " + server);
            }

        } catch (IOException e) {
            admin.sendMessage(Component.text("§c✗ Error sending transfer request: " + e.getMessage()));
            plugin.getLogger().severe("Failed to send transfer plugin message: " + e.getMessage());
        }
    }

    // Add these methods to InventoryClickListener:

    private void handleNetworkPlayersMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            String action = getActionFromItem(ev.getCurrentItem());
            if ("back".equals(action)) {
                plugin.getGuiManager().openNetworkMenu(admin);
            } else if ("previous".equals(action)) {
                int currentPage = getCurrentPageFromTitle(ev.getView().getTitle());
                new NetworkPlayersMenu(plugin, admin, currentPage - 1, true).open();
            } else if ("next".equals(action)) {
                int currentPage = getCurrentPageFromTitle(ev.getView().getTitle());
                new NetworkPlayersMenu(plugin, admin, currentPage + 1, true).open();
            }
            return;
        }

        if (clicked == Material.PLAYER_HEAD) {
            String playerName = ev.getCurrentItem().getItemMeta().getDisplayName();
            playerName = playerName.replace("§f", "");

            if (ev.isLeftClick()) {
                // Quick actions menu
                openNetworkPlayerQuickActions(admin, playerName);
            } else if (ev.isRightClick()) {
                // Player info
                showNetworkPlayerInfo(admin, playerName);
            } else if (ev.isShiftClick()) {
                // Network actions
                openNetworkPlayerActions(admin, playerName);
            }
            return;
        }

        switch (clicked) {
            case LIME_DYE:
            case GRAY_DYE:
                // Toggle filter
                boolean currentOnlineOnly = clicked == Material.LIME_DYE;
                new NetworkPlayersMenu(plugin, admin, 1, !currentOnlineOnly).open();
                break;

            case NAME_TAG:
            case CLOCK:
            case COMPARATOR:
            case ANVIL:
                // Sort options
                String action = getActionFromItem(ev.getCurrentItem());
                if (action != null && action.startsWith("sort-")) {
                    String sortType = action.replace("sort-", "");
                    admin.sendMessage(Component.text("§6[AtomHub] §7Sorting by: §f" + sortType));
                    // Implement sorting logic
                    new NetworkPlayersMenu(plugin, admin, 1, true).open();
                }
                break;

            case PAPER:
                // Refresh stats
                new NetworkPlayersMenu(plugin, admin, 1, true).open();
                break;

            case COMPASS:
                // Search player
                admin.sendMessage(Component.text("§6[AtomHub] §7Enter player name to search:"));
                admin.closeInventory();
                // You'll need to implement a chat listener for this
                break;

            case CHEST:
                // Mass actions
                openNetworkMassActions(admin);
                break;
        }
    }

    private void handleViewDistanceMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openServerMenu(admin);
            return;
        }

        // Get the action from the item's lore
        String action = null;
        if (ev.getCurrentItem() != null && ev.getCurrentItem().hasItemMeta()) {
            ItemMeta meta = ev.getCurrentItem().getItemMeta();
            if (meta.hasLore()) {
                for (Component line : meta.lore()) {
                    String loreText = PlainTextComponentSerializer.plainText().serialize(line);
                    if (loreText.contains("Action: ")) {
                        action = loreText.split("Action: ")[1];
                        break;
                    }
                }
            }
        }

        if (action == null) {
            // Check if it's a world item (by checking if it has a world name in display name)
            if (ev.getCurrentItem() != null && ev.getCurrentItem().hasItemMeta()) {
                String displayName = PlainTextComponentSerializer.plainText().serialize(
                        ev.getCurrentItem().getItemMeta().displayName()
                );

                // Check if this is a world item by looking for world names
                for (World world : Bukkit.getWorlds()) {
                    if (displayName.contains(world.getName())) {
                        action = "world-" + world.getName();
                        break;
                    }
                }
            }

            if (action == null) return;
        }

        // Handle different actions
        if (action.startsWith("set-")) {
            try {
                int distance = Integer.parseInt(action.replace("set-", ""));
                setAllWorldsViewDistance(admin, distance);
                new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
            } catch (NumberFormatException e) {
                admin.sendMessage(Component.text("§cInvalid view distance"));
            }
        }
        else if (action.startsWith("world-")) {
            String worldName = action.replace("world-", "");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                admin.sendMessage(Component.text("§cWorld not found: " + worldName));
                return;
            }

            int current = world.getViewDistance();

            if (ev.isLeftClick()) {
                // Increase
                int newDistance = Math.min(32, current + 1);
                setWorldViewDistance(admin, world, newDistance);
            } else if (ev.isRightClick()) {
                // Decrease
                int newDistance = Math.max(2, current - 1);
                setWorldViewDistance(admin, world, newDistance);
            } else if (ev.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
                // Set to default (6 chunks)
                setWorldViewDistance(admin, world, 6);
            } else {
                // Default click - show info
                admin.sendMessage(Component.text("§6[AtomHub] §7World: §f" + worldName));
                admin.sendMessage(Component.text("§7Current View Distance: §f" + current + " chunks"));
                admin.sendMessage(Component.text("§7Players in world: §f" + world.getPlayers().size()));
            }
            new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
        }
        else if (action.startsWith("toggle-")) {
            String feature = action.replace("toggle-", "");
            toggleViewDistanceFeature(admin, feature);
            new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
        }
        else if (action.equals("custom")) {
            admin.sendMessage(Component.text("§6[AtomHub] §7Enter new view distance (2-32):"));
            admin.closeInventory();
            // Store admin for chat input
            plugin.getViewDistanceManager().setWaitingForInput(admin, "all-worlds");
        }
        else if (action.equals("reset")) {
            resetAllWorldViewDistances(admin);
            new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
        }
        else if (action.equals("calculator")) {
            openViewDistanceCalculator(admin);
        }
        else if (action.equals("apply-all")) {
            applyDefaultToAllWorlds(admin);
            new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
        }
        else if (action.equals("test")) {
            testViewDistance(admin);
        }
        else if (action.equals("emergency")) {
            if (ev.isShiftClick()) {
                emergencyReduceViewDistance(admin);
                new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
            } else {
                admin.sendMessage(Component.text("§cShift-click to confirm emergency reduction!"));
            }
        }
        else if (action.startsWith("decrease-") || action.startsWith("increase-")) {
            handleQuickAdjust(admin, action);
            new ViewDistanceMenu(admin, plugin).open(); // Refresh menu
        }
        else if (action.equals("current") || action.equals("monitor") || action.equals("schedule")) {
            // These are info/placeholder actions
            admin.sendMessage(Component.text("§6[AtomHub] §7" + action + " - Feature coming soon!"));
        }
    }

    private void setAllWorldsViewDistance(Player admin, int distance) {
        if (distance < 2 || distance > 32) {
            admin.sendMessage(Component.text("§cView distance must be between 2 and 32"));
            return;
        }

        World defaultWorld = Bukkit.getWorlds().get(0);
        int oldDistance = defaultWorld.getViewDistance();

        // Show impact warning for large changes
        if (Math.abs(distance - oldDistance) >= 3) {
            admin.sendMessage(Component.text("§6⚠ Large change detected! This may cause temporary lag."));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ViewDistanceMenu.setAllWorldsViewDistance(distance);

                // Notify all online ops
                for (Player op : Bukkit.getOnlinePlayers()) {
                    if (op.isOp()) {
                        op.sendMessage(Component.text(
                                "§6[AtomHub] §7View distance changed from §f" + oldDistance +
                                        "§7 to §f" + distance + "§7 by §f" + admin.getName()
                        ));
                    }
                }

                admin.sendMessage(Component.text(
                        "§6[AtomHub] §aAll worlds view distance set to §f" + distance + " chunks"
                ));

                // Log the change
                plugin.getLogger().info("View distance changed from " + oldDistance + " to " +
                        distance + " by " + admin.getName());
            }, 20L); // 1 second delay for warning
        } else {
            ViewDistanceMenu.setAllWorldsViewDistance(distance);
            admin.sendMessage(Component.text(
                    "§6[AtomHub] §aAll worlds view distance set to §f" + distance + " chunks"
            ));
        }
    }

    private void setWorldViewDistance(Player admin, World world, int distance) {
        if (distance < 2 || distance > 32) {
            admin.sendMessage(Component.text("§cView distance must be between 2 and 32"));
            return;
        }

        int oldDistance = world.getViewDistance();

        if (distance == oldDistance) {
            admin.sendMessage(Component.text("§6[AtomHub] §7World §f" + world.getName() +
                    "§7 already has view distance §f" + distance));
            return;
        }

        ViewDistanceMenu.setWorldViewDistance(world, distance);

        // Notify players in that world
        for (Player player : world.getPlayers()) {
            player.sendMessage(Component.text(
                    "§6[World] §7View distance changed from §f" + oldDistance +
                            "§7 to §f" + distance + "§7 chunks"
            ));
        }

        admin.sendMessage(Component.text(
                "§6[AtomHub] §aWorld §f" + world.getName() +
                        "§a view distance set to §f" + distance + " chunks"
        ));
    }

    private void toggleViewDistanceFeature(Player admin, String feature) {
        switch (feature) {
            case "player":
                boolean enabled = plugin.getConfig().getBoolean("view-distance.per-player", false);
                plugin.getConfig().set("view-distance.per-player", !enabled);
                plugin.saveConfig();

                admin.sendMessage(Component.text("§6[AtomHub] §7Per-player view distance: " +
                        (!enabled ? "§aENABLED" : "§cDISABLED")));
                break;

            case "dynamic":
                boolean dynamic = plugin.getConfig().getBoolean("view-distance.dynamic", false);
                plugin.getConfig().set("view-distance.dynamic", !dynamic);
                plugin.saveConfig();

                admin.sendMessage(Component.text("§6[AtomHub] §7Dynamic view distance: " +
                        (!dynamic ? "§aENABLED" : "§cDISABLED")));
                break;

            default:
                admin.sendMessage(Component.text("§cUnknown feature: " + feature));
        }
    }

    private void resetAllWorldViewDistances(Player admin) {
        int defaultDistance = 6; // Default Minecraft view distance
        int changed = 0;

        for (World world : Bukkit.getWorlds()) {
            if (world.getViewDistance() != defaultDistance) {
                ViewDistanceMenu.setWorldViewDistance(world, defaultDistance);
                changed++;
            }
        }

        admin.sendMessage(Component.text(
                "§6[AtomHub] §aReset §f" + changed + "§a worlds to default view distance (§f" +
                        defaultDistance + "§a chunks)"
        ));
    }

    private void openViewDistanceCalculator(Player admin) {
        admin.sendMessage(Component.text("§6§lView Distance Calculator"));
        admin.sendMessage(Component.text("§7────────────────────"));

        int current = Bukkit.getWorlds().get(0).getViewDistance();
        int players = Bukkit.getOnlinePlayers().size();

        // Calculate chunks for different distances
        for (int d = 2; d <= 12; d += 2) {
            int chunksPerPlayer = (int)Math.pow(2 * d + 1, 2);
            int totalChunks = chunksPerPlayer * players;

            String status = d == current ? "§a● CURRENT" : "";
            admin.sendMessage(Component.text(
                    "§e" + d + " chunks: §f" + chunksPerPlayer +
                            "§7 chunks/player, §f" + totalChunks +
                            "§7 total " + status
            ));
        }

        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§eFormula: (2d + 1)² × players"));
        admin.sendMessage(Component.text("§7d = view distance in chunks"));
        admin.sendMessage(Component.text("§7Higher values = more RAM usage & potential lag"));
    }

    private void applyDefaultToAllWorlds(Player admin) {
        World defaultWorld = Bukkit.getWorlds().get(0);
        int defaultDistance = defaultWorld.getViewDistance();
        int applied = 0;

        for (World world : Bukkit.getWorlds()) {
            if (world != defaultWorld && world.getViewDistance() != defaultDistance) {
                ViewDistanceMenu.setWorldViewDistance(world, defaultDistance);
                applied++;
            }
        }

        if (applied > 0) {
            admin.sendMessage(Component.text(
                    "§6[AtomHub] §aApplied view distance §f" + defaultDistance +
                            "§a to §f" + applied + "§a other worlds"
            ));

            // Broadcast to all players
            Bukkit.broadcast(Component.text(
                    "§6[Server] §7View distance standardized to §f" + defaultDistance +
                            "§7 chunks on all worlds"
            ));
        } else {
            admin.sendMessage(Component.text(
                    "§6[AtomHub] §7All worlds already have view distance §f" + defaultDistance
            ));
        }
    }

    private void testViewDistance(Player admin) {
        World world = admin.getWorld();
        Location testLocation = new Location(world, 0, 100, 0);

        // Find a safe Y level
        int safeY = world.getHighestBlockYAt(0, 0) + 10;
        testLocation.setY(safeY);

        admin.teleport(testLocation);
        admin.sendMessage(Component.text(
                "§6[AtomHub] §7Teleported to view distance test area"
        ));
        admin.sendMessage(Component.text("§7Current view distance: §f" + world.getViewDistance() + " chunks"));
        admin.sendMessage(Component.text("§7Each chunk = 16 blocks. Look around to see pop-in."));
    }

    private void emergencyReduceViewDistance(Player admin) {
        World defaultWorld = Bukkit.getWorlds().get(0);
        int current = defaultWorld.getViewDistance();
        int emergencyDistance = Math.max(2, current - 4);

        if (emergencyDistance == current) {
            admin.sendMessage(Component.text("§6[AtomHub] §7View distance already at minimum safe level"));
            return;
        }

        // Force immediate reduction
        ViewDistanceMenu.setAllWorldsViewDistance(emergencyDistance);

        Bukkit.broadcast(Component.text(
                "§c⚠ EMERGENCY: View distance reduced from §f" + current +
                        "§c to §f" + emergencyDistance + "§c chunks to reduce lag"
        ));

        admin.sendMessage(Component.text(
                "§6[AtomHub] §cEmergency view distance reduction applied"
        ));

        // Log the emergency action
        plugin.getLogger().warning("EMERGENCY view distance reduction from " +
                current + " to " + emergencyDistance + " by " + admin.getName());
    }

    private void handleQuickAdjust(Player admin, String action) {
        World defaultWorld = Bukkit.getWorlds().get(0);
        int current = defaultWorld.getViewDistance();
        int change = 0;

        if (action.startsWith("decrease-")) {
            change = -Integer.parseInt(action.replace("decrease-", ""));
        } else if (action.startsWith("increase-")) {
            change = Integer.parseInt(action.replace("increase-", ""));
        }

        int newDistance = Math.max(2, Math.min(32, current + change));

        if (newDistance != current) {
            setAllWorldsViewDistance(admin, newDistance);
        } else {
            admin.sendMessage(Component.text("§6[AtomHub] §7View distance already at limit"));
        }
    }

    private void handleNetworkBroadcastMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openNetworkMenu(admin);
            return;
        }

        if (ev.getCurrentItem() != null && ev.getCurrentItem().hasItemMeta()) {
            ItemMeta meta = ev.getCurrentItem().getItemMeta();
            String displayName = meta.getDisplayName();

            // Handle quick messages
            if (displayName.contains("Announcement") || displayName.contains("Warning") ||
                    displayName.contains("Information") || displayName.contains("Event") ||
                    displayName.contains("Emergency") || displayName.contains("Custom")) {

                if (ev.isLeftClick()) {
                    // Use template
                    String type = displayName.split(" ")[0].replaceAll("§.", "").toLowerCase();
                    admin.sendMessage(Component.text("§6[AtomHub] §7Using " + type + " template"));
                    admin.sendMessage(Component.text("§7Enter your broadcast message:"));
                    admin.closeInventory();
                    // Store broadcast type for chat listener
                    plugin.getBroadcastManager().setBroadcastType(admin, type);
                } else if (ev.isRightClick()) {
                    // Edit template
                    admin.sendMessage(Component.text("§6[AtomHub] §7Editing template..."));
                }
                return;
            }

            // Handle send actions
            if (displayName.contains("SEND NOW")) {
                sendNetworkBroadcast(admin, "Test broadcast message", "ALL", "CHAT");
            } else if (displayName.contains("PREVIEW")) {
                previewNetworkBroadcast(admin);
            } else if (displayName.contains("CANCEL")) {
                plugin.getGuiManager().openNetworkMenu(admin);
            }
        }
    }

    // Helper methods for network features
    private void openNetworkPlayerQuickActions(Player admin, String playerName) {
        admin.sendMessage(Component.text("§6[AtomHub] §7Quick actions for: §f" + playerName));
        // You could open another menu here with quick actions
    }

    private void showNetworkPlayerInfo(Player admin, String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null) {
            admin.sendMessage(Component.text("§6§lNetwork Player Info"));
            admin.sendMessage(Component.text("§7────────────────────"));
            admin.sendMessage(Component.text("§eName: §f" + target.getName()));
            admin.sendMessage(Component.text("§eServer: §f" + "Survival")); // This would come from Velocity
            admin.sendMessage(Component.text("§ePing: §f" + target.getPing() + "ms"));
            admin.sendMessage(Component.text("§eRank: §f" + getPlayerRank(target)));
            admin.sendMessage(Component.text("§ePlaytime: §f" + "2h 30m"));
            admin.sendMessage(Component.text("§7────────────────────"));
        }
    }

    private void openNetworkPlayerActions(Player admin, String playerName) {
        admin.sendMessage(Component.text("§6[AtomHub] §7Network actions for: §f" + playerName));
        // Open network actions menu
    }

    private void openNetworkMassActions(Player admin) {
        admin.sendMessage(Component.text("§6[AtomHub] §7Opening mass actions menu..."));
        // Implement mass actions menu
    }

    private void sendNetworkBroadcast(Player admin, String message, String target, String type) {
        // This would send a plugin message to Velocity
        Component broadcastMessage = Component.text("§d[Network] §f" + message);

        if ("ALL".equals(target)) {
            Bukkit.broadcast(broadcastMessage);
            admin.sendMessage(Component.text("§6[AtomHub] §aBroadcast sent to all servers"));
        } else {
            // Send to specific server
            Bukkit.broadcast(broadcastMessage);
            admin.sendMessage(Component.text("§6[AtomHub] §aBroadcast sent to " + target + " server"));
        }

        // Log the broadcast
        plugin.getLogger().info("Network broadcast by " + admin.getName() + ": " + message);
    }

    private void previewNetworkBroadcast(Player admin) {
        Component preview = Component.text("§6[Preview] §7This is how your broadcast will look:");
        Component message = Component.text("§d[Network Announcement] §fThis is a test broadcast message!");

        admin.sendMessage(preview);
        admin.sendMessage(message);
        admin.sendMessage(Component.text("§6[AtomHub] §7This is a preview only"));
    }

    private String getPlayerRank(Player player) {
        if (player.isOp()) return "§cOperator";
        if (player.hasPermission("atomhub.admin")) return "§4Admin";
        if (player.hasPermission("atomhub.mod")) return "§9Moderator";
        if (player.hasPermission("atomhub.vip")) return "§6VIP";
        return "§7Member";
    }

    private int getCurrentPageFromTitle(String title) {
        try {
            String pageStr = title.replaceAll("[^0-9]", "");
            if (!pageStr.isEmpty()) {
                return Integer.parseInt(pageStr.split("/")[0]);
            }
        } catch (NumberFormatException e) {
            return 1;
        }
        return 1;
    }


    private void handleWhitelistMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            String action = getActionFromItem(ev.getCurrentItem());
            if (action != null && action.startsWith("page-")) {
                int page = Integer.parseInt(action.replace("page-", ""));
                new WhitelistMenu(admin, plugin, page).open();
            } else if ("back".equals(action)) {
                plugin.getGuiManager().openServerMenu(admin);
            }
            return;
        }

        if (clicked == Material.PLAYER_HEAD) {
            String playerName = ev.getCurrentItem().getItemMeta().getDisplayName();
            playerName = playerName.replace("§f", "");

            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (ev.isLeftClick()) {
                // Remove from whitelist
                target.setWhitelisted(false);
                admin.sendMessage(Component.text("§6[AtomHub] §7Removed §f" + playerName + "§7 from whitelist"));
                new WhitelistMenu(admin, plugin, 1).open();
            } else if (ev.isRightClick()) {
                // Show player info
                showWhitelistPlayerInfo(admin, target);
            }
            return;
        }

        switch (clicked) {
            case LIME_DYE:
            case GRAY_DYE:
                // Toggle whitelist
                boolean newState = !Bukkit.hasWhitelist();
                Bukkit.setWhitelist(newState);

                if (newState) {
                    admin.sendMessage(Component.text("§6[AtomHub] §aWhitelist enabled"));
                    Bukkit.broadcast(Component.text("§c⚠ Whitelist is now enabled!"));
                } else {
                    admin.sendMessage(Component.text("§6[AtomHub] §cWhitelist disabled"));
                    Bukkit.broadcast(Component.text("§a✅ Whitelist is now disabled!"));
                }
                new WhitelistMenu(admin, plugin, 1).open();
                break;

            case NAME_TAG:
                // Add player
                admin.sendMessage(Component.text("§6[AtomHub] §7Enter player name to add to whitelist:"));
                admin.closeInventory();
                // You'll need to implement a chat listener for this
                break;

//            case BOOK:
//                // Mass add
//                openMassWhitelistMenu(admin);
//                break;
//
//            case MAP:
//                // Import
//                importWhitelist(admin);
//                break;
//
//            case WRITABLE_BOOK:
//                // Export
//                exportWhitelist(admin);
//                break;

            case ENDER_EYE:
                // Reload
                Bukkit.reloadWhitelist();
                admin.sendMessage(Component.text("§6[AtomHub] §aWhitelist reloaded from file"));
                new WhitelistMenu(admin, plugin, 1).open();
                break;
        }
    }

    private void handlePerformanceMenu(Player admin, Material clicked, InventoryClickEvent ev) {
        if (clicked == Material.ARROW) {
            plugin.getGuiManager().openServerMenu(admin);
            return;
        }

        switch (clicked) {
            case REDSTONE_LAMP -> {
                if (!checkPermission(admin, "atomhub.server.memory")) return;
                new LagPreventionMenu(plugin, admin).open();
            }
            case ENDER_CHEST -> {
                if (!checkPermission(admin, "atomhub.server.memory")) return;
                cleanMemory(admin);
            }

            case ANVIL -> {
                if (!checkPermission(admin, "atomhub.server.entities")) return;
                cleanupEntities(admin);
            }

            case IRON_BARS -> {
                if (!checkPermission(admin, "atomhub.server.chunks")) return;
                new ChunkControlMenu(plugin, admin).open();
            }

            case CLOCK -> {
                if (!checkPermission(admin, "atomhub.server.autosave")) return;
                new AutoSaveMenu(plugin, admin).open();
            }

            case SPYGLASS -> {
                if (!checkPermission(admin, "atomhub.server.viewdistance")) return;
                new ViewDistanceMenu(admin, plugin).open();
            }

            case COMPARATOR -> showTPSHistory(admin);

            case MAP -> openTimingsReport(admin);

            case BEACON -> showOptimizationTips(admin);

            case LIME_WOOL, YELLOW_WOOL, ORANGE_WOOL, RED_WOOL -> showTPSDetails(admin);

            case LIME_STAINED_GLASS_PANE, YELLOW_STAINED_GLASS_PANE, ORANGE_STAINED_GLASS_PANE, RED_STAINED_GLASS_PANE -> {
                if (ev.isLeftClick()) {
                    System.gc();
                    admin.sendMessage(Component.text("§6[AtomHub] §aForced garbage collection"));
                    new PerformanceMenu(admin, plugin).open();
                } else if (ev.isRightClick()) {
                    showMemoryGraph(admin);
                }
            }

            case TNT -> {
                if (ev.isShiftClick()) {
                    emergencyClean(admin);
                } else {
                    admin.sendMessage(Component.text("§c[AtomHub] - §7Shift+Click to perform emergency clean"));
                }
            }

            case BARRIER -> {
                if (ev.isShiftClick()) {
                    killAllMobs(admin);
                } else {
                    admin.sendMessage(Component.text("§c[AtomHub] - §7Shift+Click to kill all mobs"));
                }
            }

            case FIRE_CHARGE -> {
                if (ev.isShiftClick()) {
                    clearDrops(admin);
                } else {
                    admin.sendMessage(Component.text("§c[AtomHub] - §7Shift+Click to clear drops"));
                }
            }

            case REDSTONE_TORCH -> {
                if (ev.isShiftClick()) {
                    scheduleRestartIfNeeded(admin);
                } else {
                    admin.sendMessage(Component.text("§6[AtomHub] - §7Shift+Click to schedule restart if TPS is low"));
                }
            }

            case LAVA_BUCKET -> flushChunks(admin);

            case WATER_BUCKET -> softCleanup(admin);
        }
    }

    private void showWhitelistPlayerInfo(Player admin, OfflinePlayer player) {
        admin.sendMessage(Component.text("§6§lWhitelist Player Info"));
        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§eName: §f" + player.getName()));
        admin.sendMessage(Component.text("§eUUID: §f" + player.getUniqueId()));
        admin.sendMessage(Component.text("§eWhitelisted: §f" + player.isWhitelisted()));
        admin.sendMessage(Component.text("§eBanned: §f" + player.isBanned()));
        admin.sendMessage(Component.text("§eLast Played: §f" +
                (player.getLastPlayed() > 0 ? new Date(player.getLastPlayed()).toString() : "Never")));
        admin.sendMessage(Component.text("§eFirst Played: §f" +
                (player.getFirstPlayed() > 0 ? new Date(player.getFirstPlayed()).toString() : "Unknown")));
        admin.sendMessage(Component.text("§7────────────────────"));
    }

    private void cleanMemory(Player admin) {
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freed = (before - after) / 1024 / 1024;

        admin.sendMessage(Component.text("§6[AtomHub] §aMemory cleaned! Freed §f" + freed + "MB"));
    }

    private void cleanupEntities(Player admin) {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            removed += world.getEntities().stream()
                    .filter(e -> e instanceof Item ||
                            (e instanceof Monster && e.getTicksLived() > 6000) ||
                            (e instanceof Animals && e.getTicksLived() > 12000))
                    .peek(Entity::remove)
                    .count();
        }

        admin.sendMessage(Component.text("§6[AtomHub] §aCleaned up §f" + removed + "§a entities"));
    }

    private void emergencyClean(Player admin) {
        int cleaned = 0;
        for (World world : Bukkit.getWorlds()) {
            cleaned += world.getEntities().stream()
                    .filter(e -> e instanceof Item)
                    .peek(Entity::remove)
                    .count();
        }

        Bukkit.broadcast(Component.text("§c⚠ Emergency item cleanup performed by admin"));
        admin.sendMessage(Component.text("§6[AtomHub] §cEmergency clean removed §f" + cleaned + "§c items"));
    }

    private void killAllMobs(Player admin) {
        int killed = 0;
        for (World world : Bukkit.getWorlds()) {
            killed += world.getEntities().stream()
                    .filter(e -> e instanceof Monster)
                    .peek(Entity::remove)
                    .count();
        }

        admin.sendMessage(Component.text("§6[AtomHub] §cKilled §f" + killed + "§c hostile mobs"));
    }

    private void clearDrops(Player admin) {
        int cleared = 0;
        for (World world : Bukkit.getWorlds()) {
            cleared += world.getEntities().stream()
                    .filter(e -> e instanceof Item)
                    .peek(Entity::remove)
                    .count();
        }

        admin.sendMessage(Component.text("§6[AtomHub] §6Cleared §f" + cleared + "§6 item drops"));
    }

    private void showTPSHistory(Player admin) {
        PerformanceTracker tracker = plugin.getPerformanceTracker(plugin);
        if (tracker == null) {
            admin.sendMessage(Component.text("§c[AtomHub] - Performance tracker not available"));
            return;
        }

        admin.sendMessage(Component.text("§6§lTPS History (Last 100 samples)"));
        admin.sendMessage(Component.text("§7────────────────────"));

        Queue<Double> history = tracker.getTPSHistory();
        if (history.isEmpty()) {
            admin.sendMessage(Component.text("§7No data available yet"));
            return;
        }

        double avg = tracker.getAverageTPS();
        int excellent = 0, good = 0, fair = 0, poor = 0;

        for (Double tps : history) {
            if (tps >= 18) excellent++;
            else if (tps >= 15) good++;
            else if (tps >= 12) fair++;
            else poor++;
        }

        admin.sendMessage(Component.text("§eAverage TPS: §f" + String.format("%.2f", avg)));
        admin.sendMessage(Component.text("§aExcellent (>18): §f" + excellent + "§7/§f" + history.size()));
        admin.sendMessage(Component.text("§eGood (15-18): §f" + good + "§7/§f" + history.size()));
        admin.sendMessage(Component.text("§6Fair (12-15): §f" + fair + "§7/§f" + history.size()));
        admin.sendMessage(Component.text("§cPoor (<12): §f" + poor + "§7/§f" + history.size()));
        admin.sendMessage(Component.text("§7────────────────────"));
        new PerformanceMenu(admin, plugin).open();
    }

    private void showTPSDetails(Player admin) {
        double[] tps = Bukkit.getTPS();
        admin.sendMessage(Component.text("§6§lTPS Details"));
        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§a1m: §f" + String.format("%.2f", tps[0])));
        admin.sendMessage(Component.text("§e5m: §f" + String.format("%.2f", tps[1])));
        admin.sendMessage(Component.text("§615m: §f" + String.format("%.2f", tps[2])));
        admin.sendMessage(Component.text("§7────────────────────"));
    }

    private void showMemoryGraph(Player admin) {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        double percent = (double) used / max * 100;

        admin.sendMessage(Component.text("§6§lMemory Usage"));
        admin.sendMessage(Component.text("§7[" + "█".repeat((int)(percent / 5)) + "░".repeat(20 - (int)(percent / 5)) + "]"));
        admin.sendMessage(Component.text("§7" + used + "MB / " + max + "MB (§f" + String.format("%.1f", percent) + "§7%)"));
    }

    private void openTimingsReport(Player admin) {
        admin.sendMessage(Component.text("§6§lTimings Report"));
        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§eTo view detailed timings, use:"));
        admin.sendMessage(Component.text("§f/timings §7- §eFull timings report"));
        admin.sendMessage(Component.text("§f/timings paste §7- §eView latest report"));
        admin.sendMessage(Component.text("§7────────────────────"));
        
        double[] tps = Bukkit.getTPS();
        admin.sendMessage(Component.text("§6Current Server Stats:"));
        admin.sendMessage(Component.text("§eTPS: §f" + String.format("%.1f", tps[0]) + " §7/ §f" + String.format("%.1f", tps[1]) + " §7/ §f" + String.format("%.1f", tps[2])));
        
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        admin.sendMessage(Component.text("§eMemory: §f" + used + "MB §7/ §f" + max + "MB"));
        
        int chunks = Bukkit.getWorlds().stream().mapToInt(w -> w.getLoadedChunks().length).sum();
        admin.sendMessage(Component.text("§eLoaded Chunks: §f" + chunks));
        admin.sendMessage(Component.text("§7────────────────────"));
        new PerformanceMenu(admin, plugin).open();
    }

    private void showOptimizationTips(Player admin) {
        admin.sendMessage(Component.text("§6§lOptimization Tips"));
        admin.sendMessage(Component.text("§7────────────────────"));
        admin.sendMessage(Component.text("§e1. §7Keep view distance low (8-12)"));
        admin.sendMessage(Component.text("§e2. §7Use mob spawners instead of natural spawns"));
        admin.sendMessage(Component.text("§e3. §7Disable hopper counting with paper.yml"));
        admin.sendMessage(Component.text("§e4. §7Use LED for entity tracking"));
        admin.sendMessage(Component.text("§e5. §7Set entity activation range lower"));
        admin.sendMessage(Component.text("§e6. §7Remove old chunks from world border"));
        admin.sendMessage(Component.text("§e7. §7Use async tickers for plugins"));
        admin.sendMessage(Component.text("§e8. §7Limit redstone devices"));
        admin.sendMessage(Component.text("§7────────────────────"));
    }

    private void scheduleRestartIfNeeded(Player admin) {
        admin.sendMessage(Component.text("§6[AtomHub] - §7Monitoring TPS..."));
        admin.sendMessage(Component.text("§7If TPS drops below 10 for 30 seconds,"));
        admin.sendMessage(Component.text("§7the server will automatically restart."));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean shouldRestart = false;
            for (int i = 0; i < 30; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                double[] tps = Bukkit.getTPS();
                if (tps[0] < 10) {
                    shouldRestart = true;
                    break;
                }
            }
            if (shouldRestart) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.broadcast(Component.text("§c⚠ Automatic restart due to low TPS"));
                    plugin.getServer().restart();
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    admin.sendMessage(Component.text("§a[AtomHub] - TPS remained stable, no restart needed"));
                });
            }
        });
    }

    private void flushChunks(Player admin) {
        int unloaded = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk c : w.getLoadedChunks()) {
                if (c.getEntities().length == 0 && c.getTileEntities().length == 0) {
                    c.unload(true);
                    unloaded++;
                }
            }
        }
        admin.sendMessage(Component.text("§6[AtomHub] - §7Unloaded §f" + unloaded + "§7 empty chunks"));
        new PerformanceMenu(admin, plugin).open();
    }

    private void softCleanup(Player admin) {
        int cleaned = 0;

        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk c : w.getLoadedChunks()) {
                for (org.bukkit.entity.Entity e : c.getEntities()) {
                    if (e instanceof Item && e.getTicksLived() > 6000) {
                        e.remove();
                        cleaned++;
                    }
                }
            }
        }

        Runtime rt = Runtime.getRuntime();
        long before = rt.totalMemory() - rt.freeMemory();
        System.gc();
        long after = rt.totalMemory() - rt.freeMemory();
        long freed = (before - after) / 1024 / 1024;

        admin.sendMessage(Component.text("§6[AtomHub] - §aSoft cleanup complete"));
        admin.sendMessage(Component.text("§7Items removed: §f" + cleaned));
        admin.sendMessage(Component.text("§7Memory freed: §f" + freed + "MB"));
        new PerformanceMenu(admin, plugin).open();
    }

    private String getActionFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return null;

        for (Component line : meta.lore()) {
            String loreText = PlainTextComponentSerializer.plainText().serialize(line);
            if (loreText.contains("Action: ")) {
                return loreText.split("Action: ")[1];
            }
        }

        return null;
    }

//    private boolean canBypassMaintenance(Player player) {
//        if (player.isOp()) return true;
//        if (player.hasPermission("atomhub.maintenance.bypass")) return true;
//
//        // Check whitelist from config
//        for (String name : plugin.getConfig().getStringList("maintenance.whitelist")) {
//            if (player.getName().equalsIgnoreCase(name)) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void handleWorldSettingsMenu(Player admin, Material clicked) {
        if (clicked == Material.KNOWLEDGE_BOOK) {
            new WorldManagerMenu(plugin, admin, admin.getWorld()).open();
            return;
        }

        World world = admin.getWorld();

        switch (clicked) {
            case OAK_DOOR -> {
                world.setPVP(!world.getPVP());
                admin.sendMessage(Component.text("§6[AtomHub] - §7PVP " + (world.getPVP() ? "§aenabled" : "§cdisabled")));
                new WorldManagerMenu(plugin, admin, world).open();
            }
            case SPAWNER -> {
                world.setallo(!world.getAllowMonsters());
                admin.sendMessage(Component.text("§6[AtomHub] - §7Mob spawning " + (world.getAllowMonsters() ? "§aenabled" : "§cdisabled")));
                new WorldManagerMenu(plugin, admin, world).open();
            }
            case ANIMAL -> {
                world.setAllowAnimals(!world.getAllowAnimals());
                admin.sendMessage(Component.text("§6[AtomHub] - §7Animal spawning " + (world.getAllowAnimals() ? "§aenabled" : "§cdisabled")));
                new WorldManagerMenu(plugin, admin, world).open();
            }
            case TNT -> clearWorldEntities(admin, world);
            case GRASS_BLOCK -> {
                admin.sendMessage(Component.text("§6[AtomHub] - §7Use §f/regen §7command to regenerate terrain"));
                new WorldManagerMenu(plugin, admin, world).open();
            }
            case BEDROCK -> {
                world.setSpawnLocation(0, 64, 0);
                admin.sendMessage(Component.text("§6[AtomHub] - §7Spawn point reset to §f0, 64, 0"));
                new WorldManagerMenu(plugin, admin, world).open();
            }
            case MAP -> {
                admin.sendMessage(Component.text("§6[AtomHub] - §7Enter new seed in chat:"));
                admin.closeInventory();
            }
        }
    }

    private void clearWorldEntities(Player admin, World world) {
        int cleared = 0;
        for (org.bukkit.Entity e : world.getEntities()) {
            if (!(e instanceof org.bukkit.entity.Player)) {
                e.remove();
                cleared++;
            }
        }
        admin.sendMessage(Component.text("§6[AtomHub] - §7Cleared §f" + cleared + "§7 entities from §f" + world.getName()));
        new WorldManagerMenu(plugin, admin, world).open();
    }

    // Prevent dragging inside custom menus
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent ev) {
        if (ev.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            ev.setCancelled(true);
        }
    }

    public Player getPendingTransfer(UUID adminUUID) {
        return pendingTransfers.get(adminUUID);
    }

    public boolean hasPendingTransfer(UUID adminUUID) {
        return pendingTransfers.containsKey(adminUUID);
    }

    public void clearPendingTransfer(UUID adminUUID) {
        pendingTransfers.remove(adminUUID);
    }
}
