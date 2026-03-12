package me.atomoyo.atomhub.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPageManager {

    private final Map<UUID, Integer> pageMap = new HashMap<>();

    public void setPage(UUID uuid, int page) {
        pageMap.put(uuid, page);
    }

    public int getPage(UUID uuid) {
        return pageMap.getOrDefault(uuid, 1);
    }

    public void clear(UUID uuid) {
        pageMap.remove(uuid);
    }
}
