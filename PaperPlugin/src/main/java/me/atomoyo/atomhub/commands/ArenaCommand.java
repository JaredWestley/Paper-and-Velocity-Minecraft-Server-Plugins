// File: me/atomoyo/atomhub/commands/ArenaCommand.java
package me.atomoyo.atomhub.commands;

import me.atomoyo.atomhub.AtomHub;
import me.atomoyo.atomhub.arena.ArenaManager;
import me.atomoyo.atomhub.arena.ArenaPreset;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ArenaCommand implements CommandExecutor, TabCompleter {

    private final AtomHub plugin;
    private final ArenaManager arenaManager;

    public ArenaCommand(AtomHub plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use arena commands!");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                if (!player.hasPermission("atomhub.arena.create")) {
                    player.sendMessage(Component.text("§cYou don't have permission to create arenas!"));
                    return true;
                }
                return handleCreate(player, args);

            case "delete":
                if (!player.hasPermission("atomhub.arena.delete")) {
                    player.sendMessage(Component.text("§cYou don't have permission to delete arenas!"));
                    return true;
                }
                return handleDelete(player, args);

            case "join":
                if (!player.hasPermission("atomhub.arena.join")) {
                    player.sendMessage(Component.text("§cYou don't have permission to join arenas!"));
                    return true;
                }
                return handleJoin(player, args);

            case "leave":
                return handleLeave(player, args);

            case "list":
                return handleList(player);

            case "info":
                return handleInfo(player, args);

            case "presets":
                return handlePresets(player);

            case "start":
                if (!player.hasPermission("atomhub.arena.start")) {
                    player.sendMessage(Component.text("§cYou don't have permission to start arenas!"));
                    return true;
                }
                return handleStart(player, args);

            case "stop":
                if (!player.hasPermission("atomhub.arena.start")) {
                    player.sendMessage(Component.text("§cYou don't have permission to stop arenas!"));
                    return true;
                }
                return handleStop(player, args);

            case "add":
            case "remove":
                if (!player.hasPermission("atomhub.arena.manage")) {
                    player.sendMessage(Component.text("§cYou don't have permission to manage arenas!"));
                    return true;
                }
                return sub.equals("add") ? handleAdd(player, args) : handleRemove(player, args);

            case "help":
                showHelp(player);
                return true;

            default:
                player.sendMessage(Component.text("§cUnknown subcommand. Use §e/arena help§c."));
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.text("""
§6§lAtomHub Arena System
§7────────────────────
§e/arena create <name> <preset> [radius] [rules] §7- Create new arena
§e/arena delete <name> §7- Delete arena (restores area)
§e/arena join <name> §7- Join an arena
§e/arena leave §7- Leave current arena
§e/arena list §7- List all arenas
§e/arena info <name> §7- Show arena info
§e/arena presets §7- Show available arena types
§e/arena start <name> §7- Start arena match
§e/arena stop <name> §7- Stop arena match
§e/arena add <name> <player> §7- Add player to arena
§e/arena remove <name> <player> §7- Remove player from arena
§7────────────────────
§7Radius: 10-50 blocks (default: 30)
§7Rules: PvP rules description
"""));
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("atomhub.arena.create")) {
            player.sendMessage(Component.text("§cYou don't have permission to create arenas!"));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("§cUsage: /arena create <name> <preset> [radius] [rules]"));
            player.sendMessage(Component.text("§7Example: /arena create duel classic 30 \"No healing, melee only\""));
            return true;
        }

        String name = args[1];
        String presetName = args[2];
        int radius = args.length >= 4 ? parseInt(args[3], 30) : 30;
        String rules = args.length >= 5 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "Standard PvP";

        // Validate radius
        if (radius < 10 || radius > 50) {
            player.sendMessage(Component.text("§cRadius must be between 10 and 50 blocks!"));
            return true;
        }

        // Check if preset exists
        boolean presetExists = arenaManager.getPresets().stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(presetName));

        if (!presetExists) {
            player.sendMessage(Component.text("§cInvalid preset! Use §e/arena presets §cto see available presets."));
            return true;
        }

        arenaManager.createArena(player, name, presetName, radius, rules);
        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("atomhub.arena.delete")) {
            player.sendMessage(Component.text("§cYou don't have permission to delete arenas!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§cUsage: /arena delete <name>"));
            return true;
        }

        String arenaName = args[1];

        if (arenaManager.deleteArena(player, arenaName)) {
            player.sendMessage(Component.text("§aArena §f" + arenaName + "§a deleted and area restored!"));
        }
        return true;
    }

    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("§cUsage: /arena join <name>"));
            return true;
        }

        String arenaName = args[1];
        arenaManager.addPlayerToArena(player, arenaName);
        return true;
    }

    private boolean handleLeave(Player player, String[] args) {
        arenaManager.removePlayerFromArena(player, "current"); // Will need to track current arena
        player.sendMessage(Component.text("§aLeft current arena."));
        return true;
    }

    private boolean handleList(Player player) {
        arenaManager.listArenas(player);
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("§cUsage: /arena info <name>"));
            return true;
        }

        String arenaName = args[1];
        arenaManager.arenaInfo(player, arenaName);
        return true;
    }

    private boolean handlePresets(Player player) {
        player.sendMessage(Component.text("§6§lAvailable Arena Presets"));
        player.sendMessage(Component.text("§7────────────────────"));

        for (ArenaPreset preset : arenaManager.getPresets()) {
            player.sendMessage(Component.text("§e" + preset.getId() + " §7- §f" + preset.getDisplayName()));
            for (String feature : preset.getFeatures()) {
                player.sendMessage(Component.text("  §7- " + feature));
            }
        }
        return true;
    }

    private boolean handleStart(Player player, String[] args) {
        if (!player.hasPermission("atomhub.arena.start")) {
            player.sendMessage(Component.text("§cYou don't have permission to start arenas!"));
            return true;
        }

        player.sendMessage(Component.text("§6[AtomHub Arena] §7Starting arenas - Coming soon!"));
        return true;
    }

    private boolean handleStop(Player player, String[] args) {
        if (!player.hasPermission("atomhub.arena.stop")) {
            player.sendMessage(Component.text("§cYou don't have permission to stop arenas!"));
            return true;
        }

        player.sendMessage(Component.text("§6[AtomHub Arena] §7Stopping arenas - Coming soon!"));
        return true;
    }

    private boolean handleAdd(Player player, String[] args) {
        if (!player.hasPermission("atomhub.arena.manage")) {
            player.sendMessage(Component.text("§cYou don't have permission to manage arenas!"));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("§cUsage: /arena add <arena> <player>"));
            return true;
        }

        player.sendMessage(Component.text("§6[AtomHub Arena] §7Adding players - Coming soon!"));
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("atomhub.arena.manage")) {
            player.sendMessage(Component.text("§cYou don't have permission to manage arenas!"));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("§cUsage: /arena remove <arena> <player>"));
            return true;
        }

        player.sendMessage(Component.text("§6[AtomHub Arena] §7Removing players - Coming soon!"));
        return true;
    }

    private int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList(
                    "create", "delete", "join", "leave", "list",
                    "info", "presets", "start", "stop", "add", "remove", "help"
            );

            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }
        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "delete":
                case "join":
                case "info":
                case "start":
                case "stop":
                case "add":
                case "remove":
                    // Arena names
                    completions.addAll(arenaManager.getArenas().keySet());
                    break;

                case "create":
                    // Preset names
                    completions.addAll(arenaManager.getPresets().stream()
                            .map(ArenaPreset::getId)
                            .filter(id -> id.startsWith(args[1].toLowerCase()))
                            .toList());
                    break;
            }
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Radius suggestions
            for (int i = 10; i <= 50; i += 10) {
                completions.add(String.valueOf(i));
            }
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            // Rule suggestions
            completions.addAll(Arrays.asList(
                    "Standard PvP",
                    "No healing",
                    "Melee only",
                    "No armor",
                    "No potions",
                    "Team deathmatch"
            ));
        }

        return completions;
    }
}