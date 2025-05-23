package net.azisaba.life.listener;

import net.azisaba.life.utils.MapUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiCloseListener implements Listener {

    private final MapUtil mapUtil;

    public GuiCloseListener(MapUtil mapUtil) {
        this.mapUtil = mapUtil;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        mapUtil.removePlayerPage(player.getUniqueId());
    }
}
