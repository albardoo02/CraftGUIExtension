package net.azisaba.life.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapUtil {

    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public int getPlayerPage(UUID uuid) {
        return playerPages.getOrDefault(uuid, 1);
    }

    public void setPlayerPage(UUID uuid, int page) {
        playerPages.put(uuid, page);
    }

    public void removePlayerPage(UUID uuid) {
        playerPages.remove(uuid);
    }
}
