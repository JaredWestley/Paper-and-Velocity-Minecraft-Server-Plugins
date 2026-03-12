package me.atomoyo.atomhub.commands;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.*;
import me.atomoyo.atomhub.gui.server.ViewDistanceMenu;
import me.atomoyo.atomhub.gui.server.PerformanceMenu;
import me.atomoyo.atomhub.gui.server.WhitelistMenu;
import me.atomoyo.atomhub.gui.world.WorldEffectsMenu;
import me.atomoyo.atomhub.gui.world.WorldEnvironmentMenu;
import me.atomoyo.atomhub.gui.world.WorldTimeMenu;
import me.atomoyo.atomhub.gui.world.WorldWeatherMenu;
import me.atomoyo.atomhub.gui.network.NetworkBanMenu;
import me.atomoyo.atomhub.gui.network.NetworkKickMenu;
import me.atomoyo.atomhub.gui.network.NetworkMuteMenu;
import me.atomoyo.atomhub.gui.player.PlayerEffectsMenu;
import me.atomoyo.atomhub.gui.player.PlayerHealthMenu;
import me.atomoyo.atomhub.gui.player.PlayerMovementMenu;
import me.atomoyo.atomhub.gui.player.PlayerOptionsMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminPanelCommand implements CommandExecutor, TabCompleter {

    private final AtomHub plugin;
    private final DecimalFormat df = new DecimalFormat("0.00");
    private Integer pendingShutdownDelay = null;

    public AdminPanelCommand(AtomHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            handleConsoleCommands(sender, args);
            return true;
        }

        if (args.length == 0) {
            showHelp(admin);
            return true;
        }

        String sub = args[0].toLowerCase();

        // Main GUI menus
        if (sub.equals("admin") && checkPermission(admin, "atomhub.admin")) {
            plugin.getGuiManager().openMainMenu(admin);
            return true;
        }

        if (sub.equals("server") && checkPermission(admin, "atomhub.server.menu")) {
            plugin.getGuiManager().openServerMenu(admin);
            return true;
        }

        if (sub.equals("network") && checkPermission(admin, "atomhub.network.menu")) {
            plugin.getGuiManager().openNetworkMenu(admin);
            return true;
        }

        if (sub.equals("world") && checkPermission(admin, "atomhub.world.menu")) {
            plugin.getGuiManager().openWorldMenu(admin);
            return true;
        }

        if (sub.equals("performance") && checkPermission(admin, "atomhub.server.performance")) {
            new PerformanceMenu(admin, plugin).open();
            return true;
        }

        if (sub.equals("viewdistance") && checkPermission(admin, "atomhub.server.viewdistance")) {
            new ViewDistanceMenu(admin, plugin).open();
            return true;
        }

        if (sub.equals("whitelist") && checkPermission(admin, "atomhub.server.whitelist")) {
            new WhitelistMenu(admin, plugin, 1).open();
            return true;
        }

        // Server control commands
        switch (sub) {
            case "reload":
                if (checkPermission(admin, "atomhub.admin")) {
                    plugin.reloadConfiguration();
                    admin.sendMessage(Component.text("§a[AtomHub] Configuration reloaded!"));
                }
                return true;

            case "stopserver":
                return handleStopServer(admin, args);

            case "restartserver":
                return handleRestartServer(admin, args);

            case "infoserver":
                return handleInfoServer(admin);

            case "maintenancemode":
                return handleMaintenanceMode(admin, args);

            case "whitelistcmd":
                return handleWhitelistCommand(admin, args);

            case "clearmemory":
                return handleClearMemory(admin);

            case "cleanentities":
                return handleCleanEntities(admin);

            case "emergencyclean":
                return handleEmergencyClean(admin);

            case "killmobs":
                return handleKillMobs(admin);

            case "setviewdistance":
                return handleSetViewDistance(admin, args);

            case "viewdistanceinfo":
                return handleViewDistanceInfo(admin);
        }

        // Player management commands
        if (sub.equals("player") && args.length >= 2) {
            return handlePlayerCommand(admin, args);
        }
        if (sub.equals("troll") && args.length >= 2) {
            return handlePlayerTrollCommand(admin, args);
        }

        // World management commands
        if (sub.equals("worldcmd")) {
            return handleWorldCommand(admin, args);
        }

        admin.sendMessage(Component.text("§cUnknown subcommand. Use §e/atomhub help§c for a list of commands."));
        return true;
    }

    private void showHelp(Player admin) {
        admin.sendMessage(Component.text("""
§a================= AtomHub Commands =================
§7GUI Menus:
§e/atomhub admin §7- Open main admin menu
§e/atomhub server §7- Open server management
§e/atomhub network §7- Open network tools
§e/atomhub world §7- Open world management
§e/atomhub performance §7- Performance monitoring
§e/atomhub viewdistance §7- View distance settings
§e/atomhub whitelist §7- Whitelist management

§7Server Control:
§e/atomhub stopserver [time] §7- Stop server
§e/atomhub restartserver [time] §7- Restart server
§e/atomhub infoserver §7- Show server info
§e/atomhub maintenancemode <on|off> §7- Toggle maintenance
§e/atomhub reload §7- Reload config
§e/atomhub clearmemory §7- Force garbage collection
§e/atomhub cleanentities §7- Clean up entities
§e/atomhub emergencyclean §7- Emergency item cleanup
§e/atomhub killmobs §7- Kill all hostile mobs

§7Player Management:
§e/atomhub player <name> §7- Open player menu
§e/atomhub player <name> ban §7- Ban player
§e/atomhub player <name> kick §7- Kick player
§e/atomhub player <name> mute §7- Mute player
§e/atomhub player <name> teleport §7- Teleport to player
§e/atomhub player <name> heal §7- Heal player
§e/atomhub player <name> gamemode <mode> §7- Set gamemode

§7World Management:
§e/atomhub worldcmd time <day|night|value> §7- Set time
§e/atomhub worldcmd weather <clear|rain|storm> §7- Set weather
§e/atomhub setviewdistance <2-32> [world] §7- Set view distance
§e/atomhub viewdistanceinfo §7- Show view distance info

§a==================================================
"""));
    }

    private boolean handleConsoleCommands(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("AtomHub Console Commands:");
            sender.sendMessage("/atomhub stopserver [time] - Stop the server");
            sender.sendMessage("/atomhub restartserver [time] - Restart the server");
            sender.sendMessage("/atomhub infoserver - Show server info");
            sender.sendMessage("/atomhub maintenancemode <on|off> - Toggle maintenance");
            sender.sendMessage("/atomhub whitelistcmd <add|remove|list> <player>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "stopserver":
                int delay = args.length > 1 ? parseTimeArg(args[1]) : 5;
                scheduleCountdown(plugin, delay, false);
                sender.sendMessage("Server will stop in " + formatTimeReadable(delay));
                return true;

            case "restartserver":
                int delay2 = args.length > 1 ? parseTimeArg(args[1]) : 5;
                scheduleCountdown(plugin, delay2, true);
                sender.sendMessage("Server will restart in " + formatTimeReadable(delay2));
                return true;

            case "infoserver":
                showServerInfo(sender);
                return true;

            case "maintenancemode":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /atomhub maintenancemode <on|off>");
                    return true;
                }
                boolean enable = args[1].equalsIgnoreCase("on");
                plugin.setMaintenanceMode(enable);
                Bukkit.broadcast(Component.text("Maintenance mode " + (enable ? "enabled" : "disabled")));
                return true;

            case "whitelistcmd":
                return handleWhitelistConsole(sender, args);
        }

        sender.sendMessage("Unknown command. Use /atomhub for help.");
        return true;
    }

    private boolean handleStopServer(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.server.stop")) return true;

        if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
            pendingShutdownDelay = null;
            admin.sendMessage(Component.text("§aShutdown cancelled."));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            if (pendingShutdownDelay == null) {
                admin.sendMessage(Component.text("§cNothing to confirm."));
                return true;
            }
            scheduleCountdown(plugin, pendingShutdownDelay, false);
            pendingShutdownDelay = null;
            return true;
        }

        int delaySeconds = args.length >= 2 ? parseTimeArg(args[1]) : 5;
        if (delaySeconds <= 0) delaySeconds = 5;

        pendingShutdownDelay = delaySeconds;
        admin.sendMessage(Component.text("§6[AtomHub] §eShutdown requested."));
        admin.sendMessage(Component.text("§7Delay: " + formatTimeReadable(delaySeconds)));
        admin.sendMessage(Component.text("§eType §a/atomhub stopserver confirm §eto proceed."));
        admin.sendMessage(Component.text("§eType §c/atomhub stopserver cancel §eto abort."));
        return true;
    }

    private boolean handleRestartServer(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.server.restart")) return true;

        if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
            pendingShutdownDelay = null;
            admin.sendMessage(Component.text("§aRestart cancelled."));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            if (pendingShutdownDelay == null) {
                admin.sendMessage(Component.text("§cNothing to confirm."));
                return true;
            }
            scheduleCountdown(plugin, pendingShutdownDelay, true);
            pendingShutdownDelay = null;
            return true;
        }

        int delaySeconds = args.length >= 2 ? parseTimeArg(args[1]) : 5;
        if (delaySeconds <= 0) delaySeconds = 5;

        pendingShutdownDelay = delaySeconds;
        admin.sendMessage(Component.text("§6[AtomHub] §eRestart requested."));
        admin.sendMessage(Component.text("§7Delay: " + formatTimeReadable(delaySeconds)));
        admin.sendMessage(Component.text("§eType §a/atomhub restartserver confirm §eto proceed."));
        admin.sendMessage(Component.text("§eType §c/atomhub restartserver cancel §eto abort."));
        return true;
    }

    private boolean handleInfoServer(Player admin) {
        if (!checkPermission(admin, "atomhub.server.info")) return true;
        showServerInfo(admin);
        return true;
    }

    private void showServerInfo(CommandSender sender) {
        double[] tps = Bukkit.getTPS();
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

        sender.sendMessage(Component.text(
                "§6§lAtomHub Server Info\n" +
                        "§7----------------------------\n" +
                        "§eTPS: §f" + df.format(tps[0]) + "/" + df.format(tps[1]) + "/" + df.format(tps[2]) + "\n" +
                        "§ePlayers: §f" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + "\n" +
                        "§eMemory: §f" + used + "MB/" + max + "MB\n" +
                        "§eUptime: §f" + formatUptime(uptimeMs) + "\n" +
                        "§eView Distance: §f" + Bukkit.getWorlds().get(0).getViewDistance() + " chunks\n" +
                        "§eWorlds: §f" + Bukkit.getWorlds().size() + "\n" +
                        "§7----------------------------"
        ));
    }

    private boolean handleMaintenanceMode(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.server.maintenance")) return true;

        if (args.length == 1) {
            admin.sendMessage(Component.text("§6[AtomHub] Maintenance Mode: " +
                    (plugin.isMaintenanceMode() ? "§aON" : "§cOFF")));
            return true;
        }

        boolean enable = args[1].equalsIgnoreCase("on");
        plugin.setMaintenanceMode(enable);

        if (enable) {
            Bukkit.broadcast(Component.text("§6[AtomHub] §c⚠ Maintenance mode enabled!"));
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.isOp())
                    .forEach(p -> p.kick(Component.text("§cServer in maintenance mode")));
        } else {
            Bukkit.broadcast(Component.text("§6[AtomHub] §a✅ Maintenance mode disabled!"));
        }

        admin.sendMessage(Component.text("§6[AtomHub] Maintenance mode " + (enable ? "§aenabled" : "§cdisabled")));
        return true;
    }

    private boolean handleWhitelistCommand(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.server.whitelist")) return true;

        if (args.length < 2) {
            admin.sendMessage(Component.text("§cUsage: /atomhub whitelistcmd <add|remove|list|on|off> [player]"));
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "on":
                Bukkit.setWhitelist(true);
                admin.sendMessage(Component.text("§aWhitelist enabled"));
                break;

            case "off":
                Bukkit.setWhitelist(false);
                admin.sendMessage(Component.text("§aWhitelist disabled"));
                break;

            case "list":
                Set<OfflinePlayer> whitelisted = Bukkit.getWhitelistedPlayers();
                admin.sendMessage(Component.text("§6Whitelisted Players (" + whitelisted.size() + "):"));
                whitelisted.forEach(p ->
                        admin.sendMessage(Component.text("§7- §f" + p.getName()))
                );
                break;

            case "add":
                if (args.length < 3) {
                    admin.sendMessage(Component.text("§cUsage: /atomhub whitelistcmd add <player>"));
                    return true;
                }
                OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(args[2]);
                playerToAdd.setWhitelisted(true);
                admin.sendMessage(Component.text("§aAdded " + args[2] + " to whitelist"));
                break;

            case "remove":
                if (args.length < 3) {
                    admin.sendMessage(Component.text("§cUsage: /atomhub whitelistcmd remove <player>"));
                    return true;
                }
                OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(args[2]);
                playerToRemove.setWhitelisted(false);
                admin.sendMessage(Component.text("§aRemoved " + args[2] + " from whitelist"));
                break;

            default:
                admin.sendMessage(Component.text("§cUnknown action. Use: add, remove, list, on, off"));
        }
        return true;
    }

    private boolean handleClearMemory(Player admin) {
        if (!checkPermission(admin, "atomhub.server.memory")) return true;

        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freed = (before - after) / 1024 / 1024;

        admin.sendMessage(Component.text("§6[AtomHub] §aMemory cleaned! Freed §f" + freed + "MB"));
        return true;
    }

    private boolean handleCleanEntities(Player admin) {
        if (!checkPermission(admin, "atomhub.server.entities")) return true;

        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            removed += world.getEntities().stream()
                    .filter(e -> e instanceof Item ||
                            (e instanceof Monster && e.getTicksLived() > 6000))
                    .peek(Entity::remove)
                    .count();
        }

        admin.sendMessage(Component.text("§6[AtomHub] §aCleaned §f" + removed + "§a entities"));
        return true;
    }

    private boolean handleEmergencyClean(Player admin) {
        if (!checkPermission(admin, "atomhub.server.emergency")) return true;

        int cleaned = 0;
        for (World world : Bukkit.getWorlds()) {
            cleaned += world.getEntities().stream()
                    .filter(e -> e instanceof Item)
                    .peek(Entity::remove)
                    .count();
        }

        Bukkit.broadcast(Component.text("§c⚠ Emergency item cleanup performed"));
        admin.sendMessage(Component.text("§6[AtomHub] §cRemoved §f" + cleaned + "§c items"));
        return true;
    }

    private boolean handleKillMobs(Player admin) {
        if (!checkPermission(admin, "atomhub.server.entities")) return true;

        int killed = 0;
        for (World world : Bukkit.getWorlds()) {
            killed += world.getEntities().stream()
                    .filter(e -> e instanceof Monster)
                    .peek(Entity::remove)
                    .count();
        }

        admin.sendMessage(Component.text("§6[AtomHub] §cKilled §f" + killed + "§c hostile mobs"));
        return true;
    }

    private boolean handleSetViewDistance(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.server.viewdistance")) return true;

        if (args.length < 2) {
            admin.sendMessage(Component.text("§cUsage: /atomhub setviewdistance <2-32> [world]"));
            admin.sendMessage(Component.text("§7Current: §f" + Bukkit.getWorlds().get(0).getViewDistance() + " chunks"));
            return true;
        }

        try {
            int distance = Integer.parseInt(args[1]);
            if (distance < 2 || distance > 32) {
                admin.sendMessage(Component.text("§cView distance must be between 2 and 32"));
                return true;
            }

            if (args.length >= 3) {
                // Set for specific world
                World world = Bukkit.getWorld(args[2]);
                if (world == null) {
                    admin.sendMessage(Component.text("§cWorld not found: " + args[2]));
                    return true;
                }
                world.setViewDistance(distance);
                admin.sendMessage(Component.text("§aSet view distance to §f" + distance + "§a for world §f" + world.getName()));
            } else {
                // Set for all worlds
                for (World world : Bukkit.getWorlds()) {
                    world.setViewDistance(distance);
                }
                admin.sendMessage(Component.text("§aSet view distance to §f" + distance + "§a for all worlds"));
            }
        } catch (NumberFormatException e) {
            admin.sendMessage(Component.text("§cInvalid number: " + args[1]));
        }
        return true;
    }

    private boolean handleViewDistanceInfo(Player admin) {
        if (!checkPermission(admin, "atomhub.server.viewdistance")) return true;

        int players = Bukkit.getOnlinePlayers().size();
        admin.sendMessage(Component.text("§6§lView Distance Information"));
        admin.sendMessage(Component.text("§7────────────────────"));

        for (World world : Bukkit.getWorlds()) {
            int distance = world.getViewDistance();
            int chunksPerPlayer = (int)Math.pow(2 * distance + 1, 2);
            int worldPlayers = world.getPlayers().size();
            int estimatedChunks = chunksPerPlayer * worldPlayers;

            admin.sendMessage(Component.text("§e" + world.getName() + ":"));
            admin.sendMessage(Component.text("  §7View Distance: §f" + distance + " chunks"));
            admin.sendMessage(Component.text("  §7Players: §f" + worldPlayers));
            admin.sendMessage(Component.text("  §7Estimated chunks: §f" + estimatedChunks));
        }

        admin.sendMessage(Component.text("§7────────────────────"));
        return true;
    }

    private boolean handlePlayerCommand(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.player.manage")) return true;

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            admin.sendMessage(Component.text("§cPlayer not found: " + args[1]));
            return true;
        }

        if (args.length == 2) {
            // Open player GUI menu
            plugin.getGuiManager().openPlayerMenu(admin, target);
            return true;
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "ban":
                if (args.length >= 4) {
                    String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, admin.getName());
                } else {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), "Banned by admin", null, admin.getName());
                }
                target.kick(Component.text("§cYou have been banned"));
                admin.sendMessage(Component.text("§aBanned " + target.getName()));
                break;

            case "kick":
                if (args.length >= 4) {
                    String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    target.kick(Component.text("§cKicked: " + reason));
                } else {
                    target.kick(Component.text("§cKicked by admin"));
                }
                admin.sendMessage(Component.text("§aKicked " + target.getName()));
                break;

            case "mute":
                boolean isMuted = plugin.getMuteManager().isNetworkMuted(target.getUniqueId());
                plugin.getMuteManager().setNetworkMute(target.getUniqueId(), !isMuted);
                admin.sendMessage(Component.text("§a" + target.getName() + " " + (!isMuted ? "muted" : "unmuted")));
                break;

            case "teleport":
                if (args.length >= 4 && args[3].equalsIgnoreCase("here")) {
                    target.teleport(admin.getLocation());
                    admin.sendMessage(Component.text("§aTeleported " + target.getName() + " to you"));
                } else {
                    admin.teleport(target.getLocation());
                    admin.sendMessage(Component.text("§aTeleported to " + target.getName()));
                }
                break;

            case "heal":
                target.setHealth(Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue());
                target.setFoodLevel(20);
                target.setSaturation(20f);
                target.sendMessage(Component.text("§aYou have been healed by " + admin.getName()));
                admin.sendMessage(Component.text("§aHealed " + target.getName()));
                break;

            case "gamemode":
                if (args.length < 4) {
                    admin.sendMessage(Component.text("§cUsage: /atomhub player <name> gamemode <survival|creative|adventure|spectator>"));
                    return true;
                }
                GameMode gamemode = getGamemodeFromString(args[3]);
                if (gamemode == null) {
                    admin.sendMessage(Component.text("§cInvalid gamemode"));
                    return true;
                }
                target.setGameMode(gamemode);
                target.sendMessage(Component.text("§aYour gamemode was changed to " + gamemode.name().toLowerCase()));
                admin.sendMessage(Component.text("§aSet " + target.getName() + "'s gamemode to " + gamemode.name().toLowerCase()));
                break;

            case "freeze":
                plugin.getFreezeManager().toggle(target);
                admin.sendMessage(Component.text("§a" + target.getName() + " " + (plugin.getFreezeManager().isFrozen(target) ? "frozen" : "unfrozen")));
                break;

            case "inventory":
                admin.openInventory(target.getInventory());
                admin.sendMessage(Component.text("§aViewing " + target.getName() + "'s inventory"));
                break;

            case "flip":
                admin.sendMessage(Component.text("§aFlipping controls for " + target.getName()));
                startFlipTask(target);
                break;

            default:
                admin.sendMessage(Component.text("§cUnknown player action. Use: ban, kick, mute, teleport, heal, gamemode, freeze, inventory"));
        }
        return true;
    }

    private boolean handlePlayerTrollCommand(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.player.troll")) return true;

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            admin.sendMessage(Component.text("§cPlayer not found: " + args[1]));
            return true;
        }

        if (args.length == 2) {
            // Open player GUI menu
            plugin.getGuiManager().openPlayerMenu(admin, target);
            return true;
        }

        String action = args[3].toLowerCase();

        switch (action) {
            case "flip":
                admin.sendMessage(Component.text("§aFlipping controls for " + target.getName()));
                startFlipTask(target);
                break;

            case "spin":
                admin.sendMessage(Component.text("§aSpinning " + target.getName()));

                new BukkitRunnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (!target.isOnline() || ticks > 60) {
                            cancel();
                            return;
                        }

                        Location loc = target.getLocation();
                        loc.setYaw(loc.getYaw() + 35);
                        target.teleport(loc);

                        ticks++;
                    }
                }.runTaskTimer(plugin, 1, 2);
                break;

            case "earrape":
                admin.sendMessage(Component.text("§aPlaying loud noises for " + target.getName()));

                target.playSound(target.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 8f, 1f);
                break;

            case "attract":
                admin.sendMessage(Component.text("§aPulling " + target.getName() + " toward you"));

                Vector pull = admin.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.3);
                target.setVelocity(pull);
                break;

            case "void":
                admin.sendMessage(Component.text("§aSending " + target.getName() + " into the fake void"));

                target.sendActionBar(Component.text("§0§lFalling into the void..."));

                new BukkitRunnable() {
                    int count = 0;

                    @Override
                    public void run() {
                        if (!target.isOnline() || count > 30) {
                            cancel();
                            return;
                        }

                        target.teleport(target.getLocation().subtract(0, 0.6, 0));
                        count++;
                    }
                }.runTaskTimer(plugin, 1, 1);
                break;

            case "crawl":
                admin.sendMessage(Component.text("§aForcing " + target.getName() + " to crawl"));

                Location loc = target.getLocation();
                loc.setYaw(loc.getYaw());
                loc.setPitch(90);

                target.teleport(loc);
                break;

            case "freezeview":
                admin.sendMessage(Component.text("§aFreezing " + target.getName() + "'s camera"));

                Location view = target.getLocation();

                new BukkitRunnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (!target.isOnline() || ticks > 80) {
                            cancel();
                            return;
                        }

                        target.teleport(view);
                        ticks++;
                    }
                }.runTaskTimer(plugin, 1, 1);
                break;

            default:
                admin.sendMessage(Component.text("§cUnknown player action. Use: ban, kick, mute, teleport, heal, gamemode, freeze, inventory"));
        }
        return true;
    }

    private boolean handleWorldCommand(Player admin, String[] args) {
        if (!checkPermission(admin, "atomhub.world.manage")) return true;

        if (args.length < 2) {
            admin.sendMessage(Component.text("§cUsage: /atomhub worldcmd <time|weather|pvp|mobs|heal|explode|tpall> [value]"));
            return true;
        }

        World world = admin.getWorld();
        String action = args[1].toLowerCase();

        switch (action) {
            case "time":
                if (args.length < 3) {
                    admin.sendMessage(Component.text("§cCurrent time: " + world.getTime()));
                    return true;
                }
                if (args[2].equalsIgnoreCase("day")) {
                    world.setTime(1000);
                    admin.sendMessage(Component.text("§aTime set to day"));
                } else if (args[2].equalsIgnoreCase("night")) {
                    world.setTime(13000);
                    admin.sendMessage(Component.text("§aTime set to night"));
                } else {
                    try {
                        long time = Long.parseLong(args[2]);
                        world.setTime(time);
                        admin.sendMessage(Component.text("§aTime set to " + time));
                    } catch (NumberFormatException e) {
                        admin.sendMessage(Component.text("§cInvalid time value"));
                    }
                }
                break;

            case "weather":
                if (args.length < 3) {
                    admin.sendMessage(Component.text("§cUsage: /atomhub worldcmd weather <clear|rain|storm>"));
                    return true;
                }
                switch (args[2].toLowerCase()) {
                    case "clear":
                        world.setStorm(false);
                        world.setThundering(false);
                        admin.sendMessage(Component.text("§aWeather cleared"));
                        break;
                    case "rain":
                        world.setStorm(true);
                        world.setThundering(false);
                        admin.sendMessage(Component.text("§aRain started"));
                        break;
                    case "storm":
                        world.setStorm(true);
                        world.setThundering(true);
                        admin.sendMessage(Component.text("§aStorm started"));
                        break;
                    default:
                        admin.sendMessage(Component.text("§cInvalid weather type"));
                }
                break;

            case "pvp":
                boolean newPvP = !world.getPVP();
                world.setPVP(newPvP);
                world.getPlayers().forEach(p ->
                        p.sendMessage(Component.text("§cPvP is now " + (newPvP ? "enabled" : "disabled")))
                );
                admin.sendMessage(Component.text("§aPvP " + (newPvP ? "enabled" : "disabled")));
                break;

            case "mobs":
                boolean allowSpawn = world.getAllowMonsters();
                world.setSpawnFlags(!allowSpawn, world.getAllowAnimals());
                admin.sendMessage(Component.text("§aMob spawning " + (!allowSpawn ? "enabled" : "disabled")));
                break;

            case "heal":
                int healed = 0;
                for (Player p : world.getPlayers()) {
                    p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getValue());
                    p.setFoodLevel(20);
                    p.setSaturation(20f);
                    healed++;
                }
                admin.sendMessage(Component.text("§aHealed " + healed + " players"));
                break;

            case "explode":
                if (args.length >= 3 && args[2].equalsIgnoreCase("confirm")) {
                    world.createExplosion(admin.getLocation(), 4.0f, false, false);
                    admin.sendMessage(Component.text("§aCreated explosion"));
                } else {
                    admin.sendMessage(Component.text("§cType /atomhub worldcmd explode confirm to create explosion"));
                }
                break;

            case "tpall":
                int teleported = 0;
                for (Player p : world.getPlayers()) {
                    if (p != admin) {
                        p.teleport(admin.getLocation());
                        teleported++;
                    }
                }
                admin.sendMessage(Component.text("§aTeleported " + teleported + " players to you"));
                break;

            default:
                admin.sendMessage(Component.text("§cUnknown world action"));
        }
        return true;
    }

    private boolean handleWhitelistConsole(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /atomhub whitelistcmd <add|remove|list|on|off> [player]");
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "on":
                Bukkit.setWhitelist(true);
                sender.sendMessage("Whitelist enabled");
                break;

            case "off":
                Bukkit.setWhitelist(false);
                sender.sendMessage("Whitelist disabled");
                break;

            case "list":
                Set<OfflinePlayer> whitelisted = Bukkit.getWhitelistedPlayers();
                sender.sendMessage("Whitelisted Players (" + whitelisted.size() + "):");
                whitelisted.forEach(p -> sender.sendMessage("- " + p.getName()));
                break;

            case "add":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /atomhub whitelistcmd add <player>");
                    return true;
                }
                OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(args[2]);
                playerToAdd.setWhitelisted(true);
                sender.sendMessage("Added " + args[2] + " to whitelist");
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /atomhub whitelistcmd remove <player>");
                    return true;
                }
                OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(args[2]);
                playerToRemove.setWhitelisted(false);
                sender.sendMessage("Removed " + args[2] + " from whitelist");
                break;

            default:
                sender.sendMessage("Unknown action. Use: add, remove, list, on, off");
        }
        return true;
    }

    // Helper methods
    private boolean checkPermission(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            player.sendMessage(Component.text("§cYou don't have permission!"));
            return false;
        }
        return true;
    }

    private GameMode getGamemodeFromString(String str) {
        switch (str.toLowerCase()) {
            case "0":
            case "survival":
            case "s": return GameMode.SURVIVAL;
            case "1":
            case "creative":
            case "c": return GameMode.CREATIVE;
            case "2":
            case "adventure":
            case "a": return GameMode.ADVENTURE;
            case "3":
            case "spectator":
            case "sp": return GameMode.SPECTATOR;
            default: return null;
        }
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    // Existing methods from your original code (keep these)
    private int parseTimeArg(String arg) {
        try {
            arg = arg.toLowerCase().replaceAll("\\s+", "");
            int totalSeconds = 0;
            StringBuilder number = new StringBuilder();

            for (char c : arg.toCharArray()) {
                if (Character.isDigit(c)) {
                    number.append(c);
                } else {
                    if (number.length() == 0) continue;
                    int value = Integer.parseInt(number.toString());
                    number.setLength(0);

                    switch (c) {
                        case 'd' -> totalSeconds += value * 86400;
                        case 'h' -> totalSeconds += value * 3600;
                        case 'm' -> totalSeconds += value * 60;
                        case 's' -> totalSeconds += value;
                    }
                }
            }

            if (number.length() > 0) {
                totalSeconds += Integer.parseInt(number.toString());
            }

            return totalSeconds > 0 ? totalSeconds : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private void scheduleCountdown(AtomHub plugin, int totalSeconds, boolean restart) {
        Bukkit.broadcast(Component.text("§c⚠ Server " + (restart ? "restart" : "shutdown") +
                " scheduled in " + formatTimeReadable(totalSeconds) + "!"));

        for (int i = totalSeconds; i > 0; i--) {
            int timeLeft = i;
            boolean announce = false;

            if (timeLeft >= 86400 && timeLeft % 86400 == 0) announce = true;
            else if (timeLeft >= 3600 && timeLeft % 3600 == 0) announce = true;
            else if (timeLeft >= 900 && timeLeft % 900 == 0) announce = true;
            else if (timeLeft >= 300 && timeLeft % 300 == 0) announce = true;
            else if (timeLeft >= 60 && timeLeft < 300 && timeLeft % 60 == 0) announce = true;
            else if (timeLeft >= 10 && timeLeft < 60 && timeLeft % 10 == 0) announce = true;
            else if (timeLeft < 10) announce = true;

            if (!announce) continue;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.broadcast(Component.text("§c⚠ Server " +
                        (restart ? "restart" : "shutdown") +
                        " in " + formatTimeReadable(timeLeft) + "..."));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                }
            }, (totalSeconds - i) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }

            Bukkit.broadcast(Component.text("§c⚠ Server " + (restart ? "restarting" : "shutting down") + " now!"));

            if (restart) plugin.getServer().restart();
            else plugin.getServer().shutdown();
        }, totalSeconds * 20L);
    }

    private String formatTimeReadable(int totalSeconds) {
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(days == 1 ? " day" : " days");
        if (hours > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }
        if (seconds > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }

        if (sb.length() == 0) return "0 seconds";
        return sb.toString();
    }

    private void startFlipTask(Player target) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!target.isOnline() || ticks > 200) {
                    cancel();
                    return;
                }
                target.setVelocity(target.getVelocity().multiply(-1));
                ticks++;
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    // Tab completer implementation
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            List<String> subs = Arrays.asList(
                    "admin", "server", "network", "world", "performance", "viewdistance", "whitelist",
                    "stopserver", "restartserver", "infoserver", "maintenancemode", "reload",
                    "clearmemory", "cleanentities", "emergencyclean", "killmobs",
                    "setviewdistance", "viewdistanceinfo", "player", "worldcmd", "help"
            );

            for (String sub : subs) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }
        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "player":
                    // Player names
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList()));
                    break;

                case "maintenancemode":
                    completions.addAll(Arrays.asList("on", "off"));
                    break;

                case "whitelistcmd":
                    completions.addAll(Arrays.asList("add", "remove", "list", "on", "off"));
                    break;

                case "worldcmd":
                    completions.addAll(Arrays.asList("time", "weather", "pvp", "mobs", "heal", "explode", "tpall"));
                    break;

                case "setviewdistance":
                    for (int i = 2; i <= 32; i += 2) {
                        completions.add(String.valueOf(i));
                    }
                    break;
            }
        }
        else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "player":
                    if (sender instanceof Player) {
                        List<String> actions = Arrays.asList(
                                "ban", "kick", "mute", "teleport", "heal", "gamemode",
                                "freeze", "inventory", "effects", "health", "movement"
                        );
                        completions.addAll(actions.stream()
                                .filter(action -> action.startsWith(args[2].toLowerCase()))
                                .collect(Collectors.toList()));
                    }
                    break;

                case "worldcmd":
                    if (args[1].equalsIgnoreCase("time")) {
                        completions.addAll(Arrays.asList("day", "night", "0", "6000", "12000", "18000"));
                    } else if (args[1].equalsIgnoreCase("weather")) {
                        completions.addAll(Arrays.asList("clear", "rain", "storm"));
                    } else if (args[1].equalsIgnoreCase("explode")) {
                        completions.add("confirm");
                    }
                    break;

                case "setviewdistance":
                    // World names for third argument
                    completions.addAll(Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList()));
                    break;
            }
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("player") && args[2].equalsIgnoreCase("gamemode")) {
            completions.addAll(Arrays.asList("survival", "creative", "adventure", "spectator"));
        }

        return completions;
    }
}