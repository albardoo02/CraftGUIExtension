package net.azisaba.life.gui;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.utils.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class GuiManager implements Listener {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiUtil guiUtil;
    private final ConfigUtil configUtil;
    private Map<String, Map<Integer, ItemUtil>> loadedItems;
    private final Map<Integer, Inventory> guiCache = new HashMap<>();
    private Map<String, List<String>> loadedLores;

    public GuiManager(CraftGUIExtension plugin, MapUtil mapUtil, GuiUtil guiUtil, ConfigUtil configUtil, Map<String, Map<Integer, ItemUtil>> loadedItems, Map<String, List<String>> newLores) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiUtil = guiUtil;
        this.configUtil = configUtil;
        this.loadedItems = loadedItems;
        this.loadedLores = newLores;
    }
    public void updateData(Map<String, Map<Integer, ItemUtil>> newItems, Map<String, List<String>> newLores) {
        this.loadedItems = newItems;
        this.loadedLores = newLores;
        buildCache();
        plugin.getLogger().info("GuiManagerのデータとキャッシュが更新されました");
    }

    private void buildCache() {
        guiCache.clear();
        if (loadedItems == null) return;

        for (String pageKey : loadedItems.keySet()) {
            int pageNum = Integer.parseInt(pageKey.replace("page", ""));
            Inventory staticGui = Bukkit.createInventory(null, 54, "CraftGUI Extension - Page" + pageNum);

            Map<Integer, ItemUtil> pageItems = loadedItems.get(pageKey);
            for (Map.Entry<Integer, ItemUtil> entry : pageItems.entrySet()) {
                if (entry.getValue().isEnabled()) {
                    staticGui.setItem(entry.getKey(), guiUtil.createStaticDisplayItem(entry.getValue()));
                }
            }

            guiUtil.setNavigationButtons(staticGui, pageNum);

            guiCache.put(pageNum, staticGui);
        }
    }

    public void openCraftGUI(Player player, int page) {
        Inventory staticGui = guiCache.get(page);
        if (staticGui == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cページ" + page + "は存在しません。"));
            return;
        }

        String title = "CraftGUI Extension - Page" + page;
        Inventory playerGui = Bukkit.createInventory(null, staticGui.getSize(), title);
        playerGui.setContents(staticGui.getContents());

        Map<Integer, ItemUtil> pageItems = loadedItems.get("page" + page);
        if (pageItems != null) {
            for (Map.Entry<Integer, ItemUtil> entry : pageItems.entrySet()) {
                int slot = entry.getKey();
                ItemUtil itemUtil = entry.getValue();
                ItemStack currentItem = playerGui.getItem(slot);

                if (currentItem != null && itemUtil.isEnabled()) {
                    guiUtil.updateLoreForPlayer(currentItem, itemUtil, player);
                }
            }
        }

        this.mapUtil.setPlayerPage(player.getUniqueId(), page);
        player.openInventory(playerGui);
    }

    public void handleGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();
        ClickType clickType = event.getClick();

        if (clickedInventory == null || !event.getView().getTitle().contains("CraftGUI Extension")) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot >= 45 && slot <= 53) {
            handleNavigationAndCloseButton(player, slot, clickedItem);
            return;
        }

        int currentPage = this.mapUtil.getPlayerPage(player.getUniqueId());
        Map<Integer, ItemUtil> pageItems = loadedItems.get("page" + currentPage);
        if (pageItems == null) {
            player.sendMessage(ChatColor.RED + "エラー: ページ" + currentPage + "のデータが見つかりません．");
            return;
        }

        ItemUtil clickedItemUtil = pageItems.get(slot);
        if (clickedItemUtil == null || !clickedItemUtil.isEnabled()) return;

        List<RequiredOrResultItem> requiredItems = clickedItemUtil.getRequiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            player.sendMessage(ChatColor.RED + "エラー: このアイテムには必要な素材が設定されていません．");
            return;
        }

        int maxCraftableAmount = getMaxCraftableAmount(player, requiredItems);
        if (maxCraftableAmount == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aCraftGUI&7] &c変換に必要なアイテムが不足しています"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2, 1);
            return;
        }

        int craftAmount = 0;
        if (clickType == ClickType.LEFT) {
            craftAmount = 1;
        } else if (clickType == ClickType.RIGHT) {
            craftAmount = maxCraftableAmount;
        }

        if (craftAmount > maxCraftableAmount) {
            craftAmount = maxCraftableAmount;
        }

        if (craftAmount == 0) {
            return;
        }

        boolean hasEnoughSpace = true;
        for (RequiredOrResultItem resultItem : clickedItemUtil.getResultItems()) {
            if (resultItem.getType() != null && resultItem.getType() != Material.AIR) {
                if (player.getInventory().firstEmpty() == -1 && resultItem.getAmount() * craftAmount > 0) {
                    hasEnoughSpace = false;
                    break;
                }
            }
        }

        if (!hasEnoughSpace) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2, 1);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aCraftGUI&7] &6インベントリに空きがないので変換できません"));
            player.closeInventory();
            return;
        }

        consumeRequiredItems(player, requiredItems, craftAmount);
        guiUtil.giveResultItems(player, clickedItemUtil.getResultItems(), craftAmount);

        try {
            int finalCraftAmount = craftAmount;
            String requiredItemsString = requiredItems.stream()
                    .map(item -> String.format("%s(x%d)",
                            ChatColor.stripColor(item.getDisplayName()),
                            item.getAmount() * finalCraftAmount))
                    .collect(Collectors.joining(", "));

            int finalCraftAmount1 = craftAmount;
            String resultItemsString = clickedItemUtil.getResultItems().stream()
                    .map(item -> String.format("%s(x%d)",
                            ChatColor.stripColor(item.getDisplayName()),
                            item.getAmount() * finalCraftAmount1))
                    .collect(Collectors.joining(", "));

            String logMessage = String.format("%sが%sを%sに変換しました (UUID: %s)",
                    player.getName(), requiredItemsString, resultItemsString, player.getUniqueId());

            plugin.getLogger().info(logMessage);
            configUtil.saveLog(logMessage);

        } catch (Exception e) {
            plugin.getLogger().severe("ログの記録中にエラーが発生しました");
            e.printStackTrace();
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&aCraftGUI&7] &aアイテムを" + craftAmount + "回変換しました"));

        openCraftGUI(player, currentPage);
    }

    private int getMaxCraftableAmount(Player player, List<RequiredOrResultItem> requiredItems) {
        int maxCraftable = Integer.MAX_VALUE;

        for (RequiredOrResultItem requiredItem : requiredItems) {
            int amountNeededPerCraft = requiredItem.getAmount();

            if (amountNeededPerCraft <= 0) {
                plugin.getLogger().warning("Required item " + requiredItem.getDisplayName() + " has amount <= 0. Skipping for craftability check.");
                continue;
            }
            int playerAmount = 0;
            if (requiredItem.isMythicItem()) {
                playerAmount = guiUtil.countMythic(player, requiredItem.getMmid(), requiredItem.getDisplayName());
            } else if (requiredItem.getType() != null) {
                playerAmount = guiUtil.countVanilla(player, requiredItem.getType());
            }

            int craftableByThisItem = playerAmount / amountNeededPerCraft;
            if (craftableByThisItem < maxCraftable) {
                maxCraftable = craftableByThisItem;
            }
        }
        return maxCraftable;
    }

    private void consumeRequiredItems(Player player, List<RequiredOrResultItem> requiredItems, int craftAmount) {
        for (RequiredOrResultItem requiredItem : requiredItems) {
            int totalAmountToConsume = requiredItem.getAmount() * craftAmount;
            if (requiredItem.isMythicItem()) {
                guiUtil.removeMythic(player, requiredItem.getMmid(), requiredItem.getDisplayName(), totalAmountToConsume);
            } else if (requiredItem.getType() != null) {
                guiUtil.removeVanilla(player, requiredItem.getType(), totalAmountToConsume);
            }
        }
    }

    private void handleNavigationAndCloseButton(Player player, int slot, ItemStack currentItem) {
        if (currentItem == null || currentItem.getType() == Material.AIR || currentItem.getItemMeta() == null || !currentItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (slot == 45) {
            handlePreviousPage(player);
        } else if (slot == 53) {
            handleNextPage(player);
        } else if (slot == 49) {
            player.closeInventory();
        }
    }

    private void handlePreviousPage(Player player) {
        int previousPage = mapUtil.getPlayerPage(player.getUniqueId()) - 1;
        if (previousPage >= 1) {
            openCraftGUI(player, previousPage);
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 2, 1);
        }
    }

    private void handleNextPage(Player player) {
        int nextPage = mapUtil.getPlayerPage(player.getUniqueId()) + 1;
        if (nextPage <= guiUtil.getMaxPage()) {
            openCraftGUI(player, nextPage);
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 2, 1);
        }
    }
}
