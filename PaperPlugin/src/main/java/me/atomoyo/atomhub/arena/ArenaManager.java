// File: me/atomoyo/atomhub/arena/ArenaManager.java
package me.atomoyo.atomhub.arena;

import me.atomoyo.atomhub.AtomHub;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {

    private final AtomHub plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<Location, BlockState> backupMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Arena presets
    private final List<ArenaPreset> presets = new ArrayList<>();

    public ArenaManager(AtomHub plugin) {
        this.plugin = plugin;
        loadPresets();
    }

    private void loadPresets() {
        // Classic Arena
        presets.add(new ArenaPreset("classic", "Classic Arena", Material.STONE_BRICKS,
                Arrays.asList("Flat 32x32 platform", "4 corner pillars", "Central platform"),
                (center, arena) -> generateClassicArena(center, arena)));

        // Sky Islands
        presets.add(new ArenaPreset("skyislands", "Sky Islands", Material.END_STONE,
                Arrays.asList("Floating islands", "Bridges between islands", "Risk of falling"),
                (center, arena) -> generateSkyIslands(center, arena)));

        // Labyrinth
        presets.add(new ArenaPreset("labyrinth", "Stone Labyrinth", Material.COBBLESTONE,
                Arrays.asList("Maze layout", "Dead ends", "Central treasure room"),
                (center, arena) -> generateLabyrinth(center, arena)));

        // Volcano
        presets.add(new ArenaPreset("volcano", "Volcano Arena", Material.NETHERRACK,
                Arrays.asList("Central volcano", "Lava flows", "Hidden tunnels"),
                (center, arena) -> generateVolcano(center, arena)));

        // Ice Palace
        presets.add(new ArenaPreset("icepalace", "Ice Palace", Material.PACKED_ICE,
                Arrays.asList("Slippery floors", "Ice pillars", "Frozen lake"),
                (center, arena) -> generateIcePalace(center, arena)));

        // Futuristic
        presets.add(new ArenaPreset("futuristic", "Futuristic Arena", Material.QUARTZ_BLOCK,
                Arrays.asList("Floating platforms", "Energy bridges", "Teleport pads"),
                (center, arena) -> generateFuturistic(center, arena)));

        // Random (Combines elements from all)
        presets.add(new ArenaPreset("random", "Random Arena", Material.DIAMOND_BLOCK,
                Arrays.asList("Random layout", "Surprise elements", "Unique each time"),
                (center, arena) -> generateRandomArena(center, arena)));
    }

    public void createArena(Player creator, String name, String presetName, int radius, String rules) {
        Location center = creator.getLocation().clone();

        // Check if arena with this name already exists
        if (arenas.containsKey(name.toLowerCase())) {
            creator.sendMessage(Component.text("§cAn arena with that name already exists!"));
            return;
        }

        // Find preset
        ArenaPreset preset = presets.stream()
                .filter(p -> p.getId().equalsIgnoreCase(presetName))
                .findFirst()
                .orElse(presets.get(0)); // Default to classic

        // Create arena
        Arena arena = new Arena(name, center, radius, preset, rules, creator.getName());
        arenas.put(name.toLowerCase(), arena);

        // Backup the area
        backupArea(center, radius);

        // Generate arena with animation
        generateArenaWithAnimation(creator, arena);
    }

    private void backupArea(Location center, int radius) {
        int halfRadius = radius / 2;

        for (int x = -halfRadius; x <= halfRadius; x++) {
            for (int y = -10; y <= 20; y++) { // Height range
                for (int z = -halfRadius; z <= halfRadius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    BlockState state = loc.getBlock().getState();
                    backupMap.put(loc, state);
                }
            }
        }
    }

    private void generateArenaWithAnimation(Player creator, Arena arena) {
        creator.sendMessage(Component.text("§6[AtomHub Arena] §7Creating arena §f" + arena.getName() + "§7..."));

        // Play creation sound
        creator.getWorld().playSound(arena.getCenter(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);

        // Generate the arena structure
        arena.getPreset().getGenerator().accept(arena.getCenter(), arena);

        // Animate blocks flying into place
        animateBlocks(creator, arena);

        // Add players who are nearby
        addNearbyPlayersToArena(creator, arena);
    }

    private void animateBlocks(Player creator, Arena arena) {
        Location center = arena.getCenter();
        int radius = arena.getRadius();
        World world = center.getWorld();

        // Create particle ring
        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                for (int i = 0; i < 360; i += 10) {
                    double radians = Math.toRadians(i + angle);
                    double x = center.getX() + Math.cos(radians) * radius;
                    double z = center.getZ() + Math.sin(radians) * radius;

                    Location particleLoc = new Location(world, x, center.getY() + 10, z);
                    world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }

                angle += 5;
                if (angle >= 360) {
                    cancel();

                    // Spawn falling blocks for dramatic effect
                    spawnFallingBlocks(center, radius);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnFallingBlocks(Location center, int radius) {
        World world = center.getWorld();
        int halfRadius = radius / 2;

        // Spawn blocks from sky
        for (int x = -halfRadius; x <= halfRadius; x += 2) {
            for (int z = -halfRadius; z <= halfRadius; z += 2) {
                Location spawnLoc = center.clone().add(x, center.getY() + 30 + random.nextInt(10), z);
                Location targetLoc = center.clone().add(x, center.getY(), z);

                // Choose random decorative block
                Material[] decorativeBlocks = {
                        Material.GLASS, Material.GLOWSTONE, Material.SEA_LANTERN,
                        Material.AMETHYST_BLOCK, Material.COPPER_BLOCK
                };

                Material blockType = decorativeBlocks[random.nextInt(decorativeBlocks.length)];
                BlockData blockData = blockType.createBlockData();

                // Spawn falling block
                FallingBlock fallingBlock = world.spawnFallingBlock(spawnLoc, blockData);
                fallingBlock.setDropItem(false);
                fallingBlock.setHurtEntities(false);

                // Calculate trajectory
                Vector direction = targetLoc.toVector().subtract(spawnLoc.toVector()).normalize();
                fallingBlock.setVelocity(direction.multiply(0.5));

                // Remove after landing
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (fallingBlock.isOnGround()) {
                            fallingBlock.remove();
                            // Place actual arena block
                            targetLoc.getBlock().setType(blockType);
                        }
                    }
                }.runTaskTimer(plugin, 0L, 5L);
            }
        }

        // Final effect
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1f);
        center.getWorld().spawnParticle(Particle.DRAGON_BREATH, center, 100, 5, 5, 5, 0.1);
    }

    private void addNearbyPlayersToArena(Player creator, Arena arena) {
        int radius = arena.getRadius();
        Location center = arena.getCenter();

        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= radius * 2) {
                arena.addPlayer(player);
                player.sendMessage(Component.text("§6[AtomHub Arena] §eYou've been added to arena §f" + arena.getName()));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        }
    }

    public boolean deleteArena(Player deleter, String arenaName) {
        Arena arena = arenas.get(arenaName.toLowerCase());

        if (arena == null) {
            deleter.sendMessage(Component.text("§cArena not found!"));
            return false;
        }

        if (!arena.getCreator().equals(deleter.getName()) && !deleter.isOp()) {
            deleter.sendMessage(Component.text("§cOnly the arena creator or an OP can delete this arena!"));
            return false;
        }

        // Remove players first
        arena.getPlayers().forEach(p -> {
            p.sendMessage(Component.text("§6[AtomHub Arena] §cArena §f" + arena.getName() + "§c is being deleted!"));
            p.teleport(arena.getCenter()); // Teleport to safe location
        });

        // Restore area with animation
        restoreAreaWithAnimation(deleter, arena);

        // Remove from map
        arenas.remove(arenaName.toLowerCase());

        return true;
    }

    private void restoreAreaWithAnimation(Player deleter, Arena arena) {
        deleter.sendMessage(Component.text("§6[AtomHub Arena] §7Restoring area..."));

        Location center = arena.getCenter();
        World world = center.getWorld();

        // Reverse animation - blocks fly away
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= 100) { // After 5 seconds
                    // Actually restore blocks
                    restoreArea(arena);
                    cancel();
                    return;
                }

                // Make blocks "dissolve" with particles
                for (int i = 0; i < 10; i++) {
                    int x = random.nextInt(arena.getRadius()) - arena.getRadius()/2;
                    int y = random.nextInt(20) - 10;
                    int z = random.nextInt(arena.getRadius()) - arena.getRadius()/2;

                    Location loc = center.clone().add(x, y, z);
                    if (loc.getBlock().getType() != Material.AIR) {
                        // Spawn breaking particles
                        world.spawnParticle(Particle.BLOCK_CRUMBLE, loc, 10,
                                loc.getBlock().getBlockData());

                        // Small chance to make block disappear
                        if (random.nextInt(10) == 0) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Final restoration
        new BukkitRunnable() {
            @Override
            public void run() {
                restoreArea(arena);
                deleter.sendMessage(Component.text("§aArea restored successfully!"));
                world.playSound(center, Sound.BLOCK_ANVIL_USE, 1f, 1f);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void restoreArea(Arena arena) {
        Location center = arena.getCenter();
        int restored = 0;

        for (Map.Entry<Location, BlockState> entry : backupMap.entrySet()) {
            if (entry.getKey().distance(center) <= arena.getRadius()) {
                entry.getValue().update(true, false);
                restored++;
            }
        }

        // Clear backup for this area
        backupMap.entrySet().removeIf(entry ->
                entry.getKey().distance(center) <= arena.getRadius());

        plugin.getLogger().info("Restored " + restored + " blocks for arena " + arena.getName());
    }

    public void addPlayerToArena(Player player, String arenaName) {
        Arena arena = arenas.get(arenaName.toLowerCase());

        if (arena == null) {
            player.sendMessage(Component.text("§cArena not found!"));
            return;
        }

        if (arena.addPlayer(player)) {
            player.sendMessage(Component.text("§aJoined arena §f" + arena.getName()));
            player.teleport(arena.getSpawnPoint());
        } else {
            player.sendMessage(Component.text("§cCould not join arena. It might be full or in progress."));
        }
    }

    public void removePlayerFromArena(Player player, String arenaName) {
        Arena arena = arenas.get(arenaName.toLowerCase());

        if (arena != null) {
            arena.removePlayer(player);
            player.sendMessage(Component.text("§aLeft arena §f" + arena.getName()));
        }
    }

    public void listArenas(Player player) {
        if (arenas.isEmpty()) {
            player.sendMessage(Component.text("§7No active arenas."));
            return;
        }

        player.sendMessage(Component.text("§6§lActive Arenas (§f" + arenas.size() + "§6§l)"));
        player.sendMessage(Component.text("§7────────────────────"));

        for (Arena arena : arenas.values()) {
            String status = arena.isActive() ? "§a● Active" : "§7● Inactive";
            String players = "§7Players: §f" + arena.getPlayerCount() + "/" + arena.getMaxPlayers();

            player.sendMessage(Component.text("§e" + arena.getName()));
            player.sendMessage(Component.text("  §7Type: §f" + arena.getPreset().getDisplayName()));
            player.sendMessage(Component.text("  §7" + players));
            player.sendMessage(Component.text("  " + status));
            player.sendMessage(Component.text("  §7Creator: §f" + arena.getCreator()));
        }
    }

    public void arenaInfo(Player player, String arenaName) {
        Arena arena = arenas.get(arenaName.toLowerCase());

        if (arena == null) {
            player.sendMessage(Component.text("§cArena not found!"));
            return;
        }

        player.sendMessage(Component.text("§6§lArena: §f" + arena.getName()));
        player.sendMessage(Component.text("§7────────────────────"));
        player.sendMessage(Component.text("§7Type: §f" + arena.getPreset().getDisplayName()));
        player.sendMessage(Component.text("§7Size: §f" + arena.getRadius() + " blocks radius"));
        player.sendMessage(Component.text("§7Status: " + (arena.isActive() ? "§aActive" : "§7Inactive")));
        player.sendMessage(Component.text("§7Players: §f" + arena.getPlayerCount() + "/" + arena.getMaxPlayers()));
        player.sendMessage(Component.text("§7Rules: §f" + arena.getRules()));
        player.sendMessage(Component.text("§7Creator: §f" + arena.getCreator()));

        if (!arena.getPlayers().isEmpty()) {
            player.sendMessage(Component.text("§7Online Players:"));
            for (Player p : arena.getPlayers()) {
                player.sendMessage(Component.text("  §f- " + p.getName()));
            }
        }
    }

    // Arena generation methods
    private void generateClassicArena(Location center, Arena arena) {
        World world = center.getWorld();
        int radius = arena.getRadius();
        int platformHeight = 5;

        // Create main platform
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius) { // Circular platform
                    Location loc = center.clone().add(x, platformHeight, z);
                    loc.getBlock().setType(Material.STONE_BRICKS);

                    // Border
                    if (x*x + z*z >= (radius-1)*(radius-1)) {
                        loc.getBlock().setType(Material.SMOOTH_STONE);
                    }
                }
            }
        }

        // Corner pillars
        int pillarHeight = 10;
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            int x = (int) (Math.cos(angle) * (radius - 2));
            int z = (int) (Math.sin(angle) * (radius - 2));

            for (int y = platformHeight + 1; y <= platformHeight + pillarHeight; y++) {
                Location pillarLoc = center.clone().add(x, y, z);
                pillarLoc.getBlock().setType(Material.SMOOTH_STONE);
            }
        }

        // Central platform
        int centerHeight = platformHeight + 3;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Location centerLoc = center.clone().add(x, centerHeight, z);
                centerLoc.getBlock().setType(Material.GOLD_BLOCK);
            }
        }
    }

    private void generateSkyIslands(Location center, Arena arena) {
        World world = center.getWorld();
        int baseY = center.getBlockY() + 20;

        // Create 5-7 floating islands
        int islandCount = 5 + random.nextInt(3);

        for (int i = 0; i < islandCount; i++) {
            double angle = (i * 2 * Math.PI) / islandCount;
            int distance = 15 + random.nextInt(10);

            int islandX = (int) (Math.cos(angle) * distance);
            int islandZ = (int) (Math.sin(angle) * distance);
            int islandY = baseY + random.nextInt(10) - 5;

            // Create island
            createIsland(center.clone().add(islandX, islandY, islandZ));

            // Create bridges between islands (sometimes)
            if (random.nextBoolean() && i < islandCount - 1) {
                double nextAngle = ((i + 1) * 2 * Math.PI) / islandCount;
                int nextDistance = 15 + random.nextInt(10);
                int nextX = (int) (Math.cos(nextAngle) * nextDistance);
                int nextZ = (int) (Math.sin(nextAngle) * nextDistance);
                int nextY = baseY + random.nextInt(10) - 5;

                createBridge(
                        center.clone().add(islandX, islandY + 3, islandZ),
                        center.clone().add(nextX, nextY + 3, nextZ)
                );
            }
        }
    }

    private void createIsland(Location center) {
        int radius = 3 + random.nextInt(4);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 2; y++) {
                    double distance = Math.sqrt(x*x + z*z);
                    if (distance <= radius) {
                        Location loc = center.clone().add(x, y, z);

                        if (y == 0) {
                            // Bottom layer
                            loc.getBlock().setType(Material.END_STONE);
                        } else if (y == 1) {
                            // Middle layer
                            loc.getBlock().setType(Material.STONE);
                        } else {
                            // Top layer (grass/flowers)
                            if (distance <= radius - 1) {
                                if (random.nextInt(10) == 0) {
                                    loc.getBlock().setType(Material.POPPY);
                                } else {
                                    loc.getBlock().setType(Material.GRASS_BLOCK);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add a tree sometimes
        if (random.nextBoolean()) {
            Location treeLoc = center.clone().add(0, 3, 0);
            treeLoc.getBlock().setType(Material.OAK_LOG);
            for (int y = 1; y <= 3; y++) {
                treeLoc.clone().add(0, y, 0).getBlock().setType(Material.OAK_LOG);
            }
            // Add leaves
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    for (int y = 3; y <= 5; y++) {
                        if (Math.abs(x) + Math.abs(z) + Math.abs(y - 4) <= 4) {
                            Location leafLoc = center.clone().add(x, y + 1, z);
                            leafLoc.getBlock().setType(Material.OAK_LEAVES);
                        }
                    }
                }
            }
        }
    }

    private void createBridge(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        direction.normalize();

        for (double i = 0; i <= length; i += 0.5) {
            Location point = from.clone().add(direction.clone().multiply(i));

            // Bridge floor
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location floorLoc = point.clone().add(x, 0, z);
                    if (floorLoc.getBlock().getType() == Material.AIR) {
                        floorLoc.getBlock().setType(Material.OAK_PLANKS);
                    }
                }
            }

            // Bridge rails
            point.clone().add(-2, 1, 0).getBlock().setType(Material.OAK_FENCE);
            point.clone().add(2, 1, 0).getBlock().setType(Material.OAK_FENCE);
        }
    }

    private void generateLabyrinth(Location center, Arena arena) {
        World world = center.getWorld();
        int mazeSize = arena.getRadius() / 2;
        boolean[][] maze = generateMaze(mazeSize);

        // Build maze walls
        for (int x = 0; x < mazeSize; x++) {
            for (int z = 0; z < mazeSize; z++) {
                if (!maze[x][z]) { // Wall
                    for (int y = 0; y < 4; y++) {
                        Location wallLoc = center.clone().add(x - mazeSize/2, y, z - mazeSize/2);
                        wallLoc.getBlock().setType(Material.COBBLESTONE);
                    }
                } else { // Path
                    Location floorLoc = center.clone().add(x - mazeSize/2, 0, z - mazeSize/2);
                    floorLoc.getBlock().setType(Material.STONE_BRICKS);

                    // Torches for lighting
                    if (random.nextInt(10) == 0) {
                        Location torchLoc = center.clone().add(x - mazeSize/2, 1, z - mazeSize/2);
                        torchLoc.getBlock().setType(Material.TORCH);
                    }
                }
            }
        }

        // Add treasure room in center
        int treasureSize = 5;
        for (int x = -treasureSize; x <= treasureSize; x++) {
            for (int z = -treasureSize; z <= treasureSize; z++) {
                for (int y = 0; y <= 6; y++) {
                    Location loc = center.clone().add(x, y, z);

                    if (y == 0) {
                        loc.getBlock().setType(Material.GOLD_BLOCK);
                    } else if (y == 6) {
                        loc.getBlock().setType(Material.GLOWSTONE);
                    } else if (Math.abs(x) == treasureSize || Math.abs(z) == treasureSize) {
                        loc.getBlock().setType(Material.EMERALD_BLOCK);
                    }
                }
            }
        }
    }

    private boolean[][] generateMaze(int size) {
        boolean[][] maze = new boolean[size][size];

        // Initialize all as walls
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                maze[x][z] = false;
            }
        }

        // Simple maze generation algorithm
        for (int x = 1; x < size - 1; x += 2) {
            for (int z = 1; z < size - 1; z += 2) {
                maze[x][z] = true; // Create cell

                // Randomly open walls
                if (x > 1 && random.nextBoolean()) {
                    maze[x-1][z] = true;
                }
                if (z > 1 && random.nextBoolean()) {
                    maze[x][z-1] = true;
                }
            }
        }

        // Ensure entrance and exit
        maze[1][0] = true;
        maze[size-2][size-1] = true;

        return maze;
    }

    private void generateVolcano(Location center, Arena arena) {
        World world = center.getWorld();
        int volcanoRadius = arena.getRadius();
        int craterDepth = 10;

        // Create volcano cone
        for (int y = 0; y <= 20; y++) {
            int radius = volcanoRadius - (y * volcanoRadius / 20);

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + z*z <= radius*radius) {
                        Location loc = center.clone().add(x, y, z);

                        if (y <= 15) {
                            // Solid volcano
                            loc.getBlock().setType(Material.NETHERRACK);
                        } else if (y <= 18) {
                            // Lava layer
                            loc.getBlock().setType(Material.LAVA);
                        } else {
                            // Rim
                            loc.getBlock().setType(Material.BLACKSTONE);
                        }
                    }
                }
            }
        }

        // Create lava rivers flowing down
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            createLavaRiver(center, angle, volcanoRadius);
        }

        // Create hidden tunnels
        for (int i = 0; i < 3; i++) {
            createHiddenTunnel(center);
        }
    }

    private void createLavaRiver(Location center, double angle, int length) {
        for (int i = 1; i <= length; i++) {
            int x = (int) (Math.cos(angle) * i);
            int z = (int) (Math.sin(angle) * i);

            for (int y = 0; y >= -3; y--) {
                Location loc = center.clone().add(x, y, z);

                if (y == -3) {
                    loc.getBlock().setType(Material.OBSIDIAN);
                } else {
                    loc.getBlock().setType(Material.LAVA);
                }
            }
        }
    }

    private void createHiddenTunnel(Location center) {
        int tunnelLength = 10 + random.nextInt(10);
        double angle = random.nextDouble() * 2 * Math.PI;

        for (int i = 0; i < tunnelLength; i++) {
            int x = (int) (Math.cos(angle) * i);
            int z = (int) (Math.sin(angle) * i);
            int y = -2 - random.nextInt(3);

            // Create tunnel
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 2; dy++) {
                        Location loc = center.clone().add(x + dx, y + dy, z + dz);
                        if (dx == 0 && dz == 0 && dy >= 0 && dy <= 1) {
                            // Tunnel passage
                            loc.getBlock().setType(Material.AIR);
                        } else {
                            // Tunnel walls
                            loc.getBlock().setType(Material.BLACKSTONE);
                        }
                    }
                }
            }

            // Add glowstone for lighting
            if (i % 3 == 0) {
                center.clone().add(x, y + 1, z).getBlock().setType(Material.GLOWSTONE);
            }
        }
    }

    private void generateIcePalace(Location center, Arena arena) {
        World world = center.getWorld();
        int palaceSize = arena.getRadius();

        // Create ice floor
        for (int x = -palaceSize; x <= palaceSize; x++) {
            for (int z = -palaceSize; z <= palaceSize; z++) {
                if (x*x + z*z <= palaceSize*palaceSize) {
                    Location floorLoc = center.clone().add(x, 0, z);
                    floorLoc.getBlock().setType(Material.PACKED_ICE);

                    // Slippery ice in center
                    if (x*x + z*z <= (palaceSize/2)*(palaceSize/2)) {
                        floorLoc.getBlock().setType(Material.ICE);
                    }
                }
            }
        }

        // Create ice pillars
        int pillarCount = 8;
        for (int i = 0; i < pillarCount; i++) {
            double angle = (i * 2 * Math.PI) / pillarCount;
            int distance = palaceSize - 3;

            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            for (int y = 1; y <= 10; y++) {
                Location pillarLoc = center.clone().add(x, y, z);
                pillarLoc.getBlock().setType(Material.BLUE_ICE);
            }

            // Add ice spikes on top
            center.clone().add(x, 11, z).getBlock().setType(Material.ICE);
        }

        // Create frozen lake in center
        int lakeRadius = palaceSize / 3;
        for (int x = -lakeRadius; x <= lakeRadius; x++) {
            for (int z = -lakeRadius; z <= lakeRadius; z++) {
                if (x*x + z*z <= lakeRadius*lakeRadius) {
                    Location lakeLoc = center.clone().add(x, -1, z);
                    lakeLoc.getBlock().setType(Material.BLUE_ICE);

                    // Add frozen fish
                    if (random.nextInt(20) == 0) {
                        lakeLoc.clone().add(0, 1, 0).getBlock().setType(Material.PRISMARINE);
                    }
                }
            }
        }

        // Create ice throne
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 1; y <= 3; y++) {
                    Location throneLoc = center.clone().add(x, y, 0);

                    if (y == 1 && Math.abs(x) <= 1 && z == 0) {
                        throneLoc.getBlock().setType(Material.DIAMOND_BLOCK);
                    } else if (y == 2 && x == 0 && z == 0) {
                        throneLoc.getBlock().setType(Material.EMERALD_BLOCK);
                    } else if (y == 3 && Math.abs(x) <= 1 && z == 0) {
                        throneLoc.getBlock().setType(Material.BLUE_ICE);
                    }
                }
            }
        }
    }

    private void generateFuturistic(Location center, Arena arena) {
        World world = center.getWorld();
        int arenaSize = arena.getRadius();

        // Create floating platforms
        for (int i = 0; i < 5; i++) {
            double angle = (i * 2 * Math.PI) / 5;
            int distance = 10 + i * 3;
            int height = 5 + i * 2;

            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            // Create platform
            createFloatingPlatform(center.clone().add(x, height, z));

            // Create energy bridges to center
            if (i > 0) {
                createEnergyBridge(
                        center.clone().add(x, height + 1, z),
                        center.clone().add(0, 6, 0)
                );
            }
        }

        // Central teleport hub
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y <= 2; y++) {
                    Location loc = center.clone().add(x, y, z);

                    if (y == 0) {
                        loc.getBlock().setType(Material.QUARTZ_BLOCK);
                    } else if (y == 1 && (x == 0 || z == 0)) {
                        loc.getBlock().setType(Material.REDSTONE_BLOCK);
                    } else if (y == 2 && x == 0 && z == 0) {
                        loc.getBlock().setType(Material.BEACON);
                    }
                }
            }
        }

        // Add teleport pads
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            int x = (int) (Math.cos(angle) * 8);
            int z = (int) (Math.sin(angle) * 8);

            Location padLoc = center.clone().add(x, 1, z);
            padLoc.getBlock().setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
            padLoc.clone().add(0, -1, 0).getBlock().setType(Material.GOLD_BLOCK);
        }
    }

    private void createFloatingPlatform(Location center) {
        int size = 3 + random.nextInt(3);

        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (x*x + z*z <= size*size) {
                    // Platform
                    center.clone().add(x, 0, z).getBlock().setType(Material.QUARTZ_BLOCK);

                    // Force field edges
                    if (x*x + z*z >= (size-1)*(size-1)) {
                        for (int y = 1; y <= 3; y++) {
                            Location edgeLoc = center.clone().add(x, y, z);
                            edgeLoc.getBlock().setType(Material.GLASS);
                        }
                    }
                }
            }
        }

        // Add beacon in center
        center.clone().add(0, 1, 0).getBlock().setType(Material.SEA_LANTERN);
    }

    private void createEnergyBridge(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        direction.normalize();

        for (double i = 0; i <= length; i += 0.5) {
            Location point = from.clone().add(direction.clone().multiply(i));

            // Bridge
            point.getBlock().setType(Material.GLOWSTONE);

            // Energy particles
            if ((int)i % 2 == 0) {
                point.clone().add(0, 1, 0).getBlock().setType(Material.REDSTONE_BLOCK);
            }
        }
    }

    private void generateRandomArena(Location center, Arena arena) {
        // Pick 2-3 random features from other arenas
        List<Runnable> features = new ArrayList<>();

        features.add(() -> {
            // Add some classic elements
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    center.clone().add(x, 0, z).getBlock().setType(Material.STONE_BRICKS);
                }
            }
        });

        features.add(() -> {
            // Add floating islands
            for (int i = 0; i < 3; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                int distance = 8 + random.nextInt(8);
                int height = 10 + random.nextInt(10);

                int x = (int) (Math.cos(angle) * distance);
                int z = (int) (Math.sin(angle) * distance);

                createIsland(center.clone().add(x, height, z));
            }
        });

        features.add(() -> {
            // Add lava features
            for (int i = 0; i < 2; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                createLavaRiver(center, angle, 8 + random.nextInt(8));
            }
        });

        features.add(() -> {
            // Add ice features
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (random.nextBoolean()) {
                        center.clone().add(x, 0, z).getBlock().setType(Material.ICE);
                    }
                }
            }
        });

        features.add(() -> {
            // Add futuristic elements
            for (int i = 0; i < 2; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                int distance = 6 + random.nextInt(6);

                int x = (int) (Math.cos(angle) * distance);
                int z = (int) (Math.sin(angle) * distance);

                createFloatingPlatform(center.clone().add(x, 8, z));
            }
        });

        // Execute 2-3 random features
        Collections.shuffle(features);
        int featureCount = 2 + random.nextInt(2);

        for (int i = 0; i < featureCount && i < features.size(); i++) {
            features.get(i).run();
        }

        // Always add some central feature
        center.clone().add(0, 1, 0).getBlock().setType(
                random.nextBoolean() ? Material.DIAMOND_BLOCK : Material.EMERALD_BLOCK
        );
    }

    // Getters
    public Map<String, Arena> getArenas() {
        return arenas;
    }

    public List<ArenaPreset> getPresets() {
        return presets;
    }
}