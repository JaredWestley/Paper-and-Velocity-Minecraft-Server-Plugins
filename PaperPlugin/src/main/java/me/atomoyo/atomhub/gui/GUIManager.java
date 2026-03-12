package me.atomoyo.atomhub.gui;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.gui.network.NetworkPlayerListMenu;
import me.atomoyo.atomhub.gui.network.NetworkServerTransferMenu;
import me.atomoyo.atomhub.gui.server.WhitelistMenu;
import me.atomoyo.atomhub.gui.server.WorldManagerMenu;
import org.bukkit.entity.Player;

public class GUIManager {

    private final AtomHub plugin;

    public GUIManager(AtomHub plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player p) {
        new MainMenu(p).open();
    }

    //#########################################
    // Server
    //#########################################
    public void openServerMenu(Player p) {
        new ServerMenu(p, plugin).open();
    }

    //#########################################
    // World
    //#########################################
    public void openWorldMenu(Player p) {
        new WorldMenu(p, plugin).open();
    }


    //#########################################
    // Network
    //#########################################
    public void openNetworkMenu(Player p) {
        new NetworkMenu(p).open();
    }

    public void openNetworkTransferMenu(Player admin, Player target) {
        new NetworkServerTransferMenu(admin, target, plugin).open();
    }

    public void openNetworkPlayerList(Player admin, int page) {
        new NetworkPlayerListMenu(admin, page).open();
    }

    //#########################################
    // Player
    //#########################################
    public void openPlayerList(Player admin, int page) {
        new PlayerListMenu(admin, page).open();
    }

    public void openPlayerMenu(Player admin, Player target) {
        new PlayerMenu(admin, target, plugin).open();
    }
}
