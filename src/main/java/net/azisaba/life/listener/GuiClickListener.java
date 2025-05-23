package net.azisaba.life.listener;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.utils.GuiUtil;
import net.azisaba.life.utils.MapUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiClickListener implements Listener {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiManager guiManager;
    private final GuiUtil guiUtil;

    public GuiClickListener(CraftGUIExtension plugin, MapUtil mapUtil, GuiManager guiManager, GuiUtil guiUtil) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiManager = guiManager;
        this.guiUtil = guiUtil;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player)event.getWhoClicked();
            Inventory inv = event.getClickedInventory();
            ItemStack clicked = event.getCurrentItem();
            Material material = clicked.getType();
            Location location = player.getLocation();
            int currentPage = this.mapUtil.getPlayerPage(player.getUniqueId());
            int slot = event.getRawSlot();
            if (inv != null && event.getView().getTitle().contains("CraftGUI Extension")) {
                event.setCancelled(true);

                if (slot >= 45 && slot <= 53) {
                    handleNavigationAndCloseButton(player, location, slot, clicked);
                    return;
                }

                ConfigurationSection pageSection = this.plugin.getConfig().getConfigurationSection("Items.page" + currentPage);
                if (pageSection != null) {
                    ConfigurationSection slotSection = pageSection.getConfigurationSection("" + slot);
                    if (slotSection != null) {
                        if (slotSection.getBoolean("enabled")) {
                            List<Map<?, ?>> requiredList = slotSection.getMapList("requiredItems");
                            Map<Integer, Integer> haveCounts = new HashMap();
                            boolean canCompress = true;

                            for(int i = 0; i < requiredList.size(); ++i) {
                                Map<?, ?> map = (Map)requiredList.get(i);
                                boolean isMythic = Boolean.TRUE.equals(map.get("isMythic"));
                                Object rawAmount = map.get("amount");
                                if (rawAmount == null) {
                                    rawAmount = map.get("amount");
                                }

                                int need = rawAmount instanceof Number ? ((Number)rawAmount).intValue() : 1;
                                int amount;
                                if (isMythic) {
                                    String mmid = map.get("mmid").toString();
                                    amount = this.guiUtil.countMythic(player, mmid);
                                } else {
                                    String type = map.get("type").toString();
                                    amount = this.guiUtil.countVanilla(player, Material.getMaterial(type));
                                }

                                haveCounts.put(i, amount);
                                if (amount < need) {
                                    canCompress = false;
                                }
                            }

                            for(int i = 0; i < requiredList.size(); ++i) {
                                Map<?, ?> map = (Map)requiredList.get(i);
                                boolean isMythic = Boolean.TRUE.equals(map.get("isMythic"));
                                String id = isMythic ? map.get("mmid").toString() : map.get("type").toString();
                                Object rawAmount = map.get("amount");
                                if (rawAmount == null) {
                                    rawAmount = map.get("amount");
                                }

                                int need = rawAmount instanceof Number ? ((Number)rawAmount).intValue() : 1;
                                int amount = (Integer)haveCounts.get(i);
                                player.sendMessage((isMythic ? "§7[§bMMID§7] " + (amount >= need ? "§a" : "§c") : "§7[§aVanilla§7] " + (amount >= need ? "§a" : "§c")) + id + " : " + amount + " / " + need);
                            }

                            if (canCompress) {
                                player.sendMessage(ChatColor.AQUA + "必要数を満たしています");

                                for(Map<?, ?> map : requiredList) {
                                    boolean isMythic = Boolean.TRUE.equals(map.get("isMythic"));
                                    String id = (String)map.get(isMythic ? "mmid" : "type");
                                    Object rawAmount = map.get("amount");
                                    if (rawAmount == null) {
                                        rawAmount = map.get("amount");
                                    }

                                    int need = rawAmount instanceof Number ? ((Number)rawAmount).intValue() : 1;
                                    this.guiUtil.removeItems(player, isMythic, id, need);
                                }

                                this.guiUtil.giveResultItems(player, slotSection);
                                player.sendMessage(ChatColor.AQUA + "アイテムを変換しました");
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                            } else {
                                player.sendMessage(ChatColor.RED + "必要数が不足しています");
                                player.playSound(location,Sound.ENTITY_VILLAGER_NO, 2, 1);
                            }

                        }
                    }
                }
            }
        }
    }

    private void handleNavigationAndCloseButton(Player player, Location location, int slot, ItemStack currentItem) {
        if (currentItem == null || currentItem.getType() == Material.AIR || currentItem.getItemMeta() == null || !currentItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (slot == 45) {
            handlePreviousPage(player, location);
        } else if (slot == 53) {
            handleNextPage(player, location);
        } else if (slot == 49) {
            player.closeInventory();
        }
    }

    private void handlePreviousPage(Player player, Location location) {
        int previousPage = mapUtil.getPlayerPage(player.getUniqueId()) - 1;
        if (previousPage >= 1) {
            guiManager.openCraftGUI(player, previousPage);
            player.playSound(location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 2, 1);
        }
    }

    private void handleNextPage(Player player, Location location) {
        int nextPage = mapUtil.getPlayerPage(player.getUniqueId()) + 1;
        if (nextPage <= guiUtil.getMaxPage()) {
            guiManager.openCraftGUI(player, nextPage);
            player.playSound(location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 2, 1);
        }
    }
}
