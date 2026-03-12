// File: me/atomoyo/atomhub/gui/network/NetworkPlayersMenu.java
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class NetworkPlayersMenu {

    private final AtomHub plugin;
    private final Player admin;
    private final int page;
    private final boolean onlineOnly;

    public NetworkPlayersMenu(AtomHub plugin, Player admin, int page, boolean onlineOnly) {
        this.plugin = plugin;
        this.admin = admin;
        this.page = page;
        this.onlineOnly = onlineOnly;
    }

    public void open() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        // For network view, we simulate multiple servers
        List<NetworkPlayerInfo> networkPlayers = createNetworkPlayerList(onlinePlayers);

        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) networkPlayers.size() / itemsPerPage);
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, networkPlayers.size());

        Inventory inv = Bukkit.createInventory(new MenuHolder("NETWORK_PLAYERS"), 54,
                Component.text("§8Network Players - Page " + page + "/" + totalPages));

        // Display network players
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            NetworkPlayerInfo playerInfo = networkPlayers.get(i);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerInfo.getUuid()));

            String serverColor = getServerColor(playerInfo.getServer());
            String pingColor = getPingColor(playerInfo.getPing());

            meta.displayName(Component.text("§f" + playerInfo.getName()));
            meta.lore(Arrays.asList(
                    Component.text("§7Server: " + serverColor + playerInfo.getServer()),
                    Component.text("§7Ping: " + pingColor + playerInfo.getPing() + "ms"),
                    Component.text("§7Playtime: §f" + formatPlaytime(playerInfo.getPlaytime())),
                    Component.text("§7Rank: §f" + playerInfo.getRank()),
                    Component.text(""),
                    Component.text("§aLeft-click: Quick actions"),
                    Component.text("§eRight-click: Player info"),
                    Component.text("§cShift-click: Network actions")
            ));
            skull.setItemMeta(meta);

            inv.setItem(slot, skull);
            slot++;
        }

        // Filter controls
        inv.setItem(45, createFilterToggle(onlineOnly));

        // Sort options
        inv.setItem(46, createSortItem(Material.NAME_TAG, "§eSort by Name", "name"));
        inv.setItem(47, createSortItem(Material.CLOCK, "§6Sort by Playtime", "playtime"));
        inv.setItem(48, createSortItem(Material.COMPARATOR, "§bSort by Server", "server"));
        inv.setItem(49, createSortItem(Material.ANVIL, "§cSort by Rank", "rank"));

        // Network statistics
        inv.setItem(50, createStatsItem());

        // Search players
        inv.setItem(51, createActionItem(Material.COMPASS, "§aSearch Player", "search"));

        // Mass actions
        inv.setItem(52, createMassActionsItem());

        // Navigation
        if (page > 1) {
            inv.setItem(53, createNavItem(Material.ARROW, "§7Previous Page", "previous"));
        }

        if (page < totalPages) {
            inv.setItem(53, createNavItem(Material.ARROW, "§7Next Page", "next"));
        }

        // Back button (always in slot 53 if no navigation)
        if (networkPlayers.size() <= itemsPerPage) {
            inv.setItem(53, createNavItem(Material.ARROW, "§7Back to Network", "back"));
        }

        admin.openInventory(inv);
    }

    private List<NetworkPlayerInfo> createNetworkPlayerList(List<Player> onlinePlayers) {
        List<NetworkPlayerInfo> networkPlayers = new ArrayList<>();

        // Simulate players across different servers
        String[] servers = {"Survival", "Creative", "Minigames", "Lobby", "Skyblock"};

        for (Player player : onlinePlayers) {
            // For real implementation, you would get this data from Velocity
            String server = servers[(int) (Math.random() * servers.length)];
            int ping = player.getPing();
            long playtime = (long) (Math.random() * 3600000); // Random playtime up to 1 hour
            String rank = getPlayerRank(player);

            networkPlayers.add(new NetworkPlayerInfo(
                    player.getUniqueId(),
                    player.getName(),
                    server,
                    ping,
                    playtime,
                    rank
            ));
        }

        // Sort by name for consistent display
        networkPlayers.sort(Comparator.comparing(NetworkPlayerInfo::getName));

        return networkPlayers;
    }

    private ItemStack createFilterToggle(boolean onlineOnly) {
        Material mat = onlineOnly ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = onlineOnly ? "§aOnline Only" : "§7All Players";

        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text("§ePlayer Filter"));
        meta.lore(Arrays.asList(
                Component.text("§7Current: " + status),
                Component.text(""),
                Component.text("§aOnline Only: Only show online players"),
                Component.text("§7All Players: Show all players (online & offline)"),
                Component.text(""),
                Component.text("§eClick to toggle")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createSortItem(Material mat, String name, String sortType) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7Sort players by " + sortType),
                Component.text("§8Action: sort-" + sortType),
                Component.text("§eClick to sort")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createStatsItem() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        int survivalPlayers = (int) (totalPlayers * 0.4);
        int creativePlayers = (int) (totalPlayers * 0.3);
        int minigamesPlayers = (int) (totalPlayers * 0.2);
        int lobbyPlayers = totalPlayers - survivalPlayers - creativePlayers - minigamesPlayers;

        ItemStack i = new ItemStack(Material.PAPER);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text("§6Network Statistics"));
        meta.lore(Arrays.asList(
                Component.text("§7Total Online: §f" + totalPlayers),
                Component.text("§7Survival: §a" + survivalPlayers),
                Component.text("§7Creative: §b" + creativePlayers),
                Component.text("§7Minigames: §e" + minigamesPlayers),
                Component.text("§7Lobby: §f" + lobbyPlayers),
                Component.text(""),
                Component.text("§7Network Uptime: §f24h 30m"),
                Component.text("§7Staff Online: §f" + getStaffCount()),
                Component.text(""),
                Component.text("§eClick to refresh")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createActionItem(Material mat, String name, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
                Component.text("§7" + action.replace("-", " ") + " on network"),
                Component.text("§8Action: " + action),
                Component.text("§aClick to use")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createMassActionsItem() {
        ItemStack i = new ItemStack(Material.CHEST);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text("§cMass Actions"));
        meta.lore(Arrays.asList(
                Component.text("§7Perform actions on multiple players"),
                Component.text(""),
                Component.text("§aMass Message: Send message to all"),
                Component.text("§6Mass Teleport: Teleport players"),
                Component.text("§cMass Kick: Kick all non-staff"),
                Component.text("§dMass Effects: Apply effects to all"),
                Component.text(""),
                Component.text("§eClick to open mass actions menu")
        ));
        i.setItemMeta(meta);
        return i;
    }

    private ItemStack createNavItem(Material mat, String name, String action) {
        ItemStack i = new ItemStack(mat);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Collections.singletonList(Component.text("§8Action: " + action)));
        i.setItemMeta(meta);
        return i;
    }

    // Utility methods
    private String getServerColor(String server) {
        switch (server.toLowerCase()) {
            case "survival": return "§a";
            case "creative": return "§b";
            case "minigames": return "§e";
            case "lobby": return "§f";
            case "skyblock": return "§d";
            default: return "§7";
        }
    }

    private String getPingColor(int ping) {
        if (ping < 50) return "§a";
        if (ping < 100) return "§e";
        if (ping < 200) return "§6";
        return "§c";
    }

    private String formatPlaytime(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "s";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";

        long hours = minutes / 60;
        if (hours < 24) return hours + "h";

        long days = hours / 24;
        return days + "d " + (hours % 24) + "h";
    }

    private String getPlayerRank(Player player) {
        if (player.isOp()) return "§cOperator";
        if (player.hasPermission("atomhub.admin")) return "§4Admin";
        if (player.hasPermission("atomhub.mod")) return "§9Moderator";
        if (player.hasPermission("atomhub.vip")) return "§6VIP";
        return "§7Member";
    }

    private int getStaffCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.isOp() || p.hasPermission("atomhub.staff"))
                .count();
    }

    // Inner class for player info
    private static class NetworkPlayerInfo {
        private final UUID uuid;
        private final String name;
        private final String server;
        private final int ping;
        private final long playtime;
        private final String rank;

        public NetworkPlayerInfo(UUID uuid, String name, String server, int ping, long playtime, String rank) {
            this.uuid = uuid;
            this.name = name;
            this.server = server;
            this.ping = ping;
            this.playtime = playtime;
            this.rank = rank;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public String getServer() { return server; }
        public int getPing() { return ping; }
        public long getPlaytime() { return playtime; }
        public String getRank() { return rank; }
    }
}