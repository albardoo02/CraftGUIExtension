package net.azisaba.life.listener;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiClickListener implements Listener {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiManager guiManager;
    private final GuiUtil guiUtil;
    private final Map<String, Map<Integer, ItemUtil>> loadedItems;
    private final Map<String, List<String>> loadedLores;
    private final ConfigUtil configUtil;


    public GuiClickListener(CraftGUIExtension plugin, MapUtil mapUtil, GuiManager guiManager, GuiUtil guiUtil, Map<String, Map<Integer, ItemUtil>> loadedItems, Map<String, List<String>> loadedLores, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiManager = guiManager;
        this.guiUtil = guiUtil;
        this.loadedItems = loadedItems;
        this.loadedLores = loadedLores;
        this.configUtil = configUtil;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();
        Location location = player.getLocation();
        int currentPage = this.mapUtil.getPlayerPage(player.getUniqueId());
        int slot = event.getRawSlot();
        ClickType clickType = event.getClick();

        if (clickedInventory == null || !event.getView().getTitle().contains("CraftGUI Extension")) {
            return;
        }
        event.setCancelled(true);

        if (slot >= 45 && slot <= 53) {
            handleNavigationAndCloseButton(player, location, slot, clickedItem);
            return;
        }

        Map<Integer, ItemUtil> pageItems = loadedItems.get("page" + currentPage);
        if (pageItems == null) {
            player.sendMessage(ChatColor.RED + "エラー: ページ" + currentPage + "のデータが見つかりません．");
            return;
        }

        ItemUtil clickedItemUtil = pageItems.get(slot);
        if (clickedItemUtil == null || !clickedItemUtil.isEnabled()) {
            return;
        }

        List<RequiredOrResultItem> requiredItems = clickedItemUtil.getRequiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            player.sendMessage(ChatColor.RED + "エラー: このアイテムには必要な素材が設定されていません．");
            return;
        }

        int maxCraftableAmount = getMaxCraftableAmount(player, requiredItems);

        if (maxCraftableAmount == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aCraftGUI&7] &c変換に必要なアイテムが不足しています"));
            player.playSound(location, Sound.ENTITY_VILLAGER_NO, 2, 1);
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

        player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&aCraftGUI&7] &aアイテムを" + craftAmount + "回変換しました"));

        guiManager.openCraftGUI(player, currentPage);
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
