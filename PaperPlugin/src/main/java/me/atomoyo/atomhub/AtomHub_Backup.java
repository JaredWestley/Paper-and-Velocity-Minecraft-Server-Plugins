package me.atomoyo.atomhub;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class AtomHub_Backup extends JavaPlugin implements Listener {

    public static final String CHANNEL_BAN = "network:ban";
    public static final String CHANNEL_KICK = "network:kick";
    public static final String CHANNEL_MUTE = "network:mute";

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();


    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("adminpanel").setExecutor(this::onAdminPanelCommand);

        // Register outgoing channels
        registerChannel(CHANNEL_BAN);
        registerChannel(CHANNEL_KICK);
        registerChannel(CHANNEL_MUTE);

        // Register incoming channels (optional, if needed)
        registerIncomingChannel(CHANNEL_BAN);
        registerIncomingChannel(CHANNEL_KICK);
        registerIncomingChannel(CHANNEL_MUTE);

        // Start particle task for frozen players
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : frozenPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            p.getLocation().add(0, 1, 0),
                            10, 0.5, 0.5, 0.5, 0.05
                    );
                }
            }
        }, 0L, 10L);
    }

    private void registerChannel(String channel) {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, channel);
    }

    private void registerIncomingChannel(String channel) {
        this.getServer().getMessenger().registerIncomingPluginChannel(this, channel, (ch, player, message) -> {
            // Handle incoming plugin messages if needed
        });
    }

    @Override
    public void onDisable() {
        // Unregister all channels
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL_BAN);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL_KICK);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL_MUTE);

        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL_BAN);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL_KICK);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL_MUTE);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player p = event.getPlayer();
        p.sendMessage(Component.text("Hello " + event.getPlayer().getName() + ", Welcome to AtomHub!"));

        Particle[] safeParticles = {
                Particle.FLAME,
                Particle.HEART,
                Particle.HAPPY_VILLAGER,
                Particle.END_ROD,
                Particle.CRIT,
                Particle.CLOUD,
                Particle.FIREWORK
        };

        Particle particle = safeParticles[new Random().nextInt(safeParticles.length)];

        p.getWorld().spawnParticle(
                particle,
                p.getLocation().add(0, 1, 0),
                40, 0.5, 0.5, 0.5, 0.05
        );
    }

    private boolean onAdminPanelCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            openMainMenu(admin);
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                admin.sendMessage(Component.text("Player not found or offline."));
                return true;
            }
            openPlayerMenu(admin, target);
        }
        return true;
    }

    private void openServerMenu(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Server Settings"));
        inv.setItem(11, createNamedStack(Material.LIME_WOOL, "Enable PvP"));
        inv.setItem(13, createNamedStack(Material.RED_WOOL, "Disable PvP"));
        inv.setItem(15, createNamedStack(Material.BOOK, "Server Info"));
        inv.setItem(26, createNamedStack(Material.ARROW, "Back"));
        admin.openInventory(inv);
    }

    private void openNetworkMenu(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Network Settings"));
        inv.setItem(11, createNamedStack(Material.PAPER, "List Banned Players"));
        inv.setItem(13, createNamedStack(Material.OAK_DOOR, "Restart Server"));
        inv.setItem(15, createNamedStack(Material.COMPASS, "Network Status"));
        inv.setItem(26, createNamedStack(Material.ARROW, "Back"));
        admin.openInventory(inv);
    }

    private void openWorldMenu(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("World Settings"));
        inv.setItem(11, createNamedStack(Material.GRASS_BLOCK, "Day Time"));
        inv.setItem(13, createNamedStack(Material.NETHER_STAR, "Night Time"));
        inv.setItem(15, createNamedStack(Material.COBBLESTONE, "Clear Weather"));
        inv.setItem(26, createNamedStack(Material.ARROW, "Back"));
        admin.openInventory(inv);
    }

    private void openMainMenu(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Admin Panel"));
        inv.setItem(10, createNamedStack(Material.COMMAND_BLOCK, "Server Settings"));
        inv.setItem(12, createNamedStack(Material.NETHER_STAR, "Network Settings"));
        inv.setItem(14, createNamedStack(Material.GRASS_BLOCK, "World Settings"));
        inv.setItem(16, createNamedStack(Material.PLAYER_HEAD, "Player Settings"));
        admin.openInventory(inv);
    }

    private void openPlayerListMenu(Player admin, int page) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int playersPerPage = 21;
        int maxPages = (int) Math.ceil((double) onlinePlayers.size() / playersPerPage);
        page = Math.max(1, Math.min(page, maxPages));
        playerPages.put(admin.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Player List (Page " + page + ")"));

        int start = (page - 1) * playersPerPage;
        int end = Math.min(start + playersPerPage, onlinePlayers.size());
        int slot = 0;

        for (int i = start; i < end; i++) {
            Player target = onlinePlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.displayName(Component.text(target.getName()));
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
        }

        if (page > 1) inv.setItem(21, createNamedStack(Material.ARROW, "Previous Page"));
        if (page < maxPages) inv.setItem(23, createNamedStack(Material.ARROW, "Next Page"));
        inv.setItem(26, createNamedStack(Material.ARROW, "Back"));
        admin.openInventory(inv);
    }

    private void openPlayerMenu(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Admin Panel: " + target.getName()).asComponent());
        inv.setItem(1, createNamedStack(Material.IRON_BARS, "Gamemode Ban Player"));
        inv.setItem(2, createNamedStack(Material.BLAZE_ROD, "Gamemode Kick Player"));
        inv.setItem(10, createNamedStack(Material.ANVIL, "Network Ban Player"));
        inv.setItem(11, createNamedStack(Material.BARRIER, "Network Kick Player"));
        inv.setItem(12, createNamedStack(Material.PAPER, "Mute Player"));
        inv.setItem(13, createNamedStack(Material.ENDER_PEARL, "Teleport To"));

        // Freeze player icon changes based on frozen state
        Material freezeIcon = frozenPlayers.contains(target.getUniqueId()) ? Material.PACKED_ICE : Material.ICE;
        String freezeText = frozenPlayers.contains(target.getUniqueId()) ? "Unfreeze Player" : "Freeze Player";
        inv.setItem(14, createNamedStack(freezeIcon, freezeText));

        inv.setItem(15, createNamedStack(Material.GOLDEN_APPLE, "Heal Player"));
        inv.setItem(16, createNamedStack(Material.COMPASS, "Send to Server"));
        inv.setItem(26, createNamedStack(Material.ARROW, "Back"));
        admin.openInventory(inv);
    }

    private ItemStack createNamedStack(Material mat, String name) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) meta.displayName(Component.text(name));
        is.setItemMeta(meta);
        return is;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player admin)) return;
        if (ev.getView().title() == null) return;

        String title = PlainTextComponentSerializer.plainText().serialize(ev.getView().title());
        ev.setCancelled(true);

        // Main Menu click
        if (title.equals("Admin Panel")) {
            Material clicked = ev.getCurrentItem().getType();
            switch (clicked) {
                case PLAYER_HEAD -> openPlayerListMenu(admin, 1);
                case COMMAND_BLOCK -> openServerMenu(admin);
                case NETHER_STAR -> openNetworkMenu(admin);
                case GRASS_BLOCK -> openWorldMenu(admin);
                default -> admin.sendMessage(Component.text("Not implemented yet."));
            }
            return;
        }

        // Server Menu
        if (title.equals("Server Settings")) {
            Material clicked = ev.getCurrentItem().getType();
            switch (clicked) {
                case ARROW -> openMainMenu(admin);
                case LIME_WOOL -> {
                    admin.sendMessage(Component.text("PvP Enabled!"));

                }
                case RED_WOOL -> {
                    admin.sendMessage(Component.text("PvP Disabled!"));
                }
                case BOOK -> admin.sendMessage(Component.text("Server version: " + Bukkit.getVersion()));
            }
            return;
        }

        // Network Menu
        if (title.equals("Network Settings")) {
            Material clicked = ev.getCurrentItem().getType();
            switch (clicked) {
                case ARROW -> openMainMenu(admin);
                case PAPER -> admin.sendMessage(Component.text("Banned players: " + Bukkit.getBanList(BanList.Type.NAME).getBanEntries().size()));
                case OAK_DOOR -> admin.sendMessage(Component.text("Server restart command triggered!"));
                case COMPASS -> admin.sendMessage(Component.text("Network status: Online"));
            }
            return;
        }

        // World Menu
        if (title.equals("World Settings")) {
            Material clicked = ev.getCurrentItem().getType();
            switch (clicked) {
                case ARROW -> openMainMenu(admin);
                case GRASS_BLOCK -> {
                    Bukkit.getWorlds().forEach(w -> w.setTime(1000));
                    admin.sendMessage(Component.text("Set day time!"));
                }
                case NETHER_STAR -> {
                    Bukkit.getWorlds().forEach(w -> w.setTime(13000));
                    admin.sendMessage(Component.text("Set night time!"));
                }
                case COBBLESTONE -> {
                    Bukkit.getWorlds().forEach(w -> w.setStorm(false));
                    admin.sendMessage(Component.text("Cleared weather!"));
                }
            }
            return;
        }

        // Player List Menu
        if (title.startsWith("Player List")) {
            Material clicked = ev.getCurrentItem().getType();
            if (clicked == Material.ARROW) {
                if (PlainTextComponentSerializer.plainText().serialize(ev.getCurrentItem().getItemMeta().displayName()).equals("Back")) {
                    openMainMenu(admin);
                } else {
                    int page = playerPages.getOrDefault(admin.getUniqueId(), 1);
                    if (PlainTextComponentSerializer.plainText().serialize(ev.getCurrentItem().getItemMeta().displayName()).contains("Next"))
                        page++;
                    else page--;
                    openPlayerListMenu(admin, page);
                }
            } else if (clicked == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) ev.getCurrentItem().getItemMeta();
                if (meta != null && meta.getOwningPlayer() != null) {
                    openPlayerMenu(admin, (Player) meta.getOwningPlayer());
                }
            }
            return;
        }

        // Player Menu
        if (title.startsWith("Admin Panel: ")) {
            String targetName = title.substring("Admin Panel: ".length());
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                admin.sendMessage(Component.text("Player is offline."));
                admin.closeInventory();
                return;
            }

            Material clicked = ev.getCurrentItem().getType();
            switch (clicked) {
                case ARROW -> openPlayerListMenu(admin, 1);
                case IRON_BARS -> {
                    String reason = "Banned by admin: " + admin.getName();
                    Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, admin.getName());
                    target.kick(Component.text("You were gamemode-banned: " + reason));

                    admin.sendMessage(Component.text("Player network-banned and locally banned."));
                    admin.closeInventory();
                }
                case ANVIL -> {
                    String reason = "Banned by admin: " + admin.getName();

                    sendNetwork(CHANNEL_BAN, admin, target.getUniqueId(), reason);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, admin.getName());
                    target.kick(Component.text("You were network banned: " + reason));

                    admin.sendMessage(Component.text("Player network-banned and locally banned."));
                    admin.closeInventory();
                }
                case BLAZE_ROD -> {
                    target.kick(Component.text("Kicked by admin."));
                    admin.sendMessage(Component.text("Player gamemode kicked."));
                    admin.closeInventory();
                }
                case BARRIER -> {
                    String reason = "Kicked by admin: " + admin.getName();
                    sendNetwork(CHANNEL_KICK, admin, target.getUniqueId(), reason);
                    admin.sendMessage(Component.text("Player network kicked."));
                    admin.closeInventory();
                }
                case ENDER_PEARL -> {
                    admin.teleport(target.getLocation());
                    admin.sendMessage(Component.text("Teleported to " + target.getName()));
                    admin.closeInventory();
                }
                case ICE, PACKED_ICE -> {
                    if (frozenPlayers.add(target.getUniqueId())) {
                        admin.sendMessage(Component.text("Player " + target.getName() + " has been frozen."));
                        target.sendMessage(Component.text("§cYou have been frozen by an admin!"));
                    } else {
                        frozenPlayers.remove(target.getUniqueId());
                        admin.sendMessage(Component.text("Player " + target.getName() + " has been unfrozen."));
                        target.sendMessage(Component.text("§aYou have been unfrozen by an admin."));
                    }
                    admin.closeInventory();
                }
                case GOLDEN_APPLE -> {
                    target.setHealth(Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue());
                    target.setAbsorptionAmount(Objects.requireNonNull(target.getAttribute(Attribute.MAX_ABSORPTION)).getValue());
                    admin.sendMessage(Component.text("Healed player."));
                    admin.closeInventory();
                }
                default -> admin.sendMessage(Component.text("Button not implemented yet."));
            }
        }
    }


    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent ev) {
        String title = PlainTextComponentSerializer.plainText().serialize(ev.getView().title());
        if (title.startsWith("Admin Panel: ")) {
            ev.setCancelled(true);
        }
    }


    /**
     * Send a plugin message to the proxy indicating a network ban.
     * Message format: [subcommand=ban][uuid][reason][staffName]
     */
    private void sendNetwork(String channel, Player admin, UUID targetUuid, String reason) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF(targetUuid.toString());
            out.writeUTF(reason);
            out.writeUTF(admin.getName());

            admin.sendPluginMessage(this, channel, baos.toByteArray());
        } catch (IOException ex) {
            getLogger().severe("Failed to write plugin message for network ban: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        if (frozenPlayers.contains(ev.getPlayer().getUniqueId())) {
            if (!ev.getFrom().equals(ev.getTo())) {
                ev.setTo(ev.getFrom());
            }
        }
    }

    // Prevent frozen players from interacting
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (frozenPlayers.contains(ev.getPlayer().getUniqueId())) {
            ev.setCancelled(true);
        }
    }

    // Optional: Unfreeze players or cleanup when they quit (if you implement freeze)
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev) {
//        frozenPlayers.remove(ev.getPlayer().getUniqueId());
    }


}