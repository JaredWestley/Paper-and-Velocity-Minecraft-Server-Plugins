// File: me/atomoyo/atomhub/arena/ArenaPreset.java
package me.atomoyo.atomhub.arena;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.function.BiConsumer;

public class ArenaPreset {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<String> features;
    private final BiConsumer<Location, Arena> generator;

    public ArenaPreset(String id, String displayName, Material icon,
                       List<String> features, BiConsumer<Location, Arena> generator) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.features = features;
        this.generator = generator;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public List<String> getFeatures() { return features; }
    public BiConsumer<Location, Arena> getGenerator() { return generator; }

    // Helper method to generate the arena
    public void generate(Location center, Arena arena) {
        generator.accept(center, arena);
    }
}