package net.azisaba.life.listener;

import net.azisaba.life.editor.RecipeBuilder;
import net.azisaba.life.editor.RecipeEditorManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditorListener implements Listener {

    private final RecipeEditorManager manager;

    public EditorListener(RecipeEditorManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (manager.getPlayerStep(player) == 0) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("CraftGUI登録")) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (clickedInventory.equals(event.getView().getBottomInventory())) {
            return;
        }

        if (clickedInventory.equals(event.getView().getTopInventory())) {
            event.setCancelled(true);

            int step = manager.getPlayerStep(player);
            int slot = event.getSlot();
            Inventory gui = event.getInventory();
            RecipeBuilder builder = manager.getBuilder(player);

            switch (step) {
                case 1:
                    if (slot == 13) {
                        event.setCancelled(false);
                    } else if (slot == 18) {
                        manager.cancel(player);
                    } else if (slot == 26) {
                        ItemStack mainItem = gui.getItem(13);
                        if (mainItem == null || mainItem.getType().isAir()) {
                            player.sendMessage(ChatColor.RED + "メインアイテムを設置してください");
                            return;
                        }
                        builder.setMainItem(mainItem.clone());
                        builder.setModelData(mainItem.hasItemMeta() && mainItem.getItemMeta().hasCustomModelData() ? mainItem.getItemMeta().getCustomModelData() : 0);
                        manager.nextStep(player);
                    }
                    break;

                case 2:
                case 3:
                    if (slot < 36) {
                        event.setCancelled(false);
                    } else if (slot == 45) {
                        manager.cancel(player);
                    } else if (slot == 53) {
                        if (step == 2) {
                            builder.getRequiredItems().clear();
                            for (int i = 0; i < 36; i++) {
                                ItemStack item = gui.getItem(i);
                                if (item != null && !item.getType().isAir()) builder.getRequiredItems().add(item.clone());
                            }
                        } else {
                            builder.getResultItems().clear();
                            for (int i = 0; i < 36; i++) {
                                ItemStack item = gui.getItem(i);
                                if (item != null && !item.getType().isAir()) builder.getResultItems().add(item.clone());
                            }
                        }
                        manager.nextStep(player);
                    }
                    break;

                case 4:
                    if (slot == 45) manager.cancel(player);
                    if (slot == 53) manager.save(player);
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (manager.checkAndRemoveIntentionalClosure(player)) {
            return;
        }
        if (manager.getPlayerStep(player) > 0) {
            manager.cancel(player);
            player.sendMessage(ChatColor.RED + "アイテムの登録が中断されました");
        }
    }
}