package me.atomoyo.atomhub.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    private final String type; // "MAIN", "NETWORK", "PLAYER", "PLAYERLIST", "SERVER", "WORLD"

    public MenuHolder(String type) {
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return null; // We don't need to store it here
    }

    public String getType() {
        return type;
    }
}
