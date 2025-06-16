package net.azisaba.life.listener;

import net.azisaba.life.gui.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
public class GuiClickListener implements Listener {

    private final GuiManager guiManager;

    public GuiClickListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        guiManager.handleGuiClick(event);
    }
}
