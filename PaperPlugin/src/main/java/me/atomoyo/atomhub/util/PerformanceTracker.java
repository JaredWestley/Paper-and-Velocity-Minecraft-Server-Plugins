// File: me/atomoyo/atomhub/utils/PerformanceTracker.java
package me.atomoyo.atomhub.util;

import me.atomoyo.atomhub.AtomHub;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Queue;

public class PerformanceTracker {

    private final AtomHub plugin;
    private final Queue<Double> tpsHistory = new LinkedList<>();
    private final Queue<Long> memoryHistory = new LinkedList<>();
    private final Queue<Integer> entityHistory = new LinkedList<>();
    private final Queue<Integer> chunkHistory = new LinkedList<>();
    private final int maxHistory = 100;

    public PerformanceTracker(AtomHub plugin) {
        this.plugin = plugin;
        startTracking();
    }

    private void startTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Track TPS
                double currentTPS = Bukkit.getTPS()[0];
                tpsHistory.add(currentTPS);

                // Track memory
                long currentMemory = (Runtime.getRuntime().totalMemory() -
                        Runtime.getRuntime().freeMemory()) / 1024 / 1024;
                memoryHistory.add(currentMemory);

                // Track entities
                int currentEntities = Bukkit.getWorlds().stream()
                        .mapToInt(w -> w.getEntities().size())
                        .sum();
                entityHistory.add(currentEntities);

                // Track chunks
                int currentChunks = Bukkit.getWorlds().stream()
                        .mapToInt(w -> w.getLoadedChunks().length)
                        .sum();
                chunkHistory.add(currentChunks);

                // Limit history size
                if (tpsHistory.size() > maxHistory) tpsHistory.poll();
                if (memoryHistory.size() > maxHistory) memoryHistory.poll();
                if (entityHistory.size() > maxHistory) entityHistory.poll();
                if (chunkHistory.size() > maxHistory) chunkHistory.poll();

                // Log to console if performance is low (optional)
                if (currentTPS < 15) {
                    plugin.getLogger().warning("Low TPS detected: " + String.format("%.2f", currentTPS));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60L); // Update every minute
    }

    // Getters for performance data
    public double getAverageTPS() {
        return tpsHistory.stream().mapToDouble(Double::doubleValue).average().orElse(20.0);
    }

    public long getAverageMemory() {
        return (long) memoryHistory.stream().mapToLong(Long::longValue).average().orElse(0L);
    }

    public int getAverageEntities() {
        return (int) entityHistory.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    public int getAverageChunks() {
        return (int) chunkHistory.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    public Queue<Double> getTPSHistory() {
        return new LinkedList<>(tpsHistory);
    }

    public Queue<Long> getMemoryHistory() {
        return new LinkedList<>(memoryHistory);
    }

    public Queue<Integer> getEntityHistory() {
        return new LinkedList<>(entityHistory);
    }

    public Queue<Integer> getChunkHistory() {
        return new LinkedList<>(chunkHistory);
    }

    // Performance analysis methods
    public String getPerformanceStatus() {
        double avgTPS = getAverageTPS();
        if (avgTPS >= 18) return "§aExcellent";
        if (avgTPS >= 15) return "§eGood";
        if (avgTPS >= 12) return "§6Fair";
        return "§cPoor";
    }

    public boolean isPerformanceDegrading() {
        if (tpsHistory.size() < 10) return false;

        // Check if last 5 TPS readings are lower than average
        Double[] recent = tpsHistory.toArray(new Double[0]);
        double recentAvg = 0;
        int count = Math.min(5, recent.length);

        for (int i = recent.length - count; i < recent.length; i++) {
            recentAvg += recent[i];
        }
        recentAvg /= count;

        return recentAvg < (getAverageTPS() * 0.9); // 10% degradation
    }
}