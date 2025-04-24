package net.azisaba.life.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapUtil {

    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public int getPlayerPage(UUID player) {
        return playerPages.getOrDefault(player, 1);
    }

    public void setPlayerPage(UUID player, int page) {
        playerPages.put(player, page);
    }

    public void removePlayerPage(UUID player) {
        playerPages.remove(player);
    }


}
