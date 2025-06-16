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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiManager implements Listener {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiUtil guiUtil;
    private final ConfigUtil configUtil;
    private Map<String, Map<Integer, ItemUtil>> loadedItems;
    private Map<String, List<String>> loadedLores;

    public GuiManager(CraftGUIExtension plugin, MapUtil mapUtil, GuiUtil guiUtil, ConfigUtil configUtil, Map<String, Map<Integer, ItemUtil>> loadedItems, Map<String, List<String>> loadedLores) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiUtil = guiUtil;
        this.configUtil = configUtil;
        this.loadedItems = loadedItems;
        this.loadedLores = loadedLores;
    }

    public void updateData(Map<String, Map<Integer, ItemUtil>> newItems, Map<String, List<String>> newLores) {
        this.loadedItems = newItems;
        this.loadedLores = newLores;
        plugin.getLogger().info("GuiManagerのデータが更新されました");
    }

    public void openCraftGUI(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "CraftGUI Extension - Page" + page);

        Map<Integer, ItemUtil> pageItems = loadedItems.get("page" + page);

        if (pageItems == null || pageItems.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cページ" + page + "は存在しないか、アイテムが設定されていません。"));
            player.closeInventory();
            return;
        }

        for (Map.Entry<Integer, ItemUtil> entry : pageItems.entrySet()) {
            int slot = entry.getKey();
            ItemUtil itemUtil = entry.getValue();

            if (!itemUtil.isEnabled()) {
                continue;
            }

            if (slot >= 0 && slot <= 44) {
                ItemStack item = new ItemStack(itemUtil.getMaterial());
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(itemUtil.getDisplayName());

                List<String> lore = loadedLores.get(itemUtil.getLoreKey());
                List<String> combinedLore = new ArrayList<>();
                if (lore != null) {
                    combinedLore.addAll(lore);
                }

                List<String> requirementLore = new ArrayList<>();
                requirementLore.add(ChatColor.GRAY + "変換に必要素材:");

                boolean canCraft = true;

                for (RequiredOrResultItem requiredItem : itemUtil.getRequiredItems()) {
                    String requiredDisplay = requiredItem.getDisplayName();
                    int amount = requiredItem.getAmount();

                    int playerAmount = 0;
                    if (requiredItem.isMythicItem()) {
                        playerAmount = guiUtil.countMythic(player, requiredItem.getMmid(), requiredItem.getDisplayName());
                    } else if (requiredItem.getType() != null) {
                        playerAmount = guiUtil.countVanilla(player, requiredItem.getType());
                    }

                    String title = playerAmount >= amount ? ChatColor.GREEN + "✓ " + ChatColor.RESET: ChatColor.RED + "✘ " + ChatColor.RESET;
                    ChatColor color = playerAmount >= amount ? ChatColor.GREEN : ChatColor.RED;

                    String hasItemMessage = getString(playerAmount, amount);

                    requirementLore.add(title + ChatColor.translateAlternateColorCodes('&', requiredDisplay)
                            + color + " x" + amount + hasItemMessage
                            + (requiredItem.isMythicItem() ? ChatColor.DARK_GRAY + " (Mythic)" : ChatColor.DARK_GRAY + " (Vanilla)"));

                    if (playerAmount < amount) {
                        canCraft = false;
                    }
                }
                combinedLore.addAll(requirementLore);
                combinedLore.add("");
                if (canCraft) {
                    combinedLore.add(ChatColor.GREEN + "✓ 変換可能です");
                } else {
                    combinedLore.add(ChatColor.RED + "✘ 変換できません");
                }

                meta.setLore(combinedLore);

                if (itemUtil.isEnchanted()) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                if (itemUtil.getModel() > 0) {
                    meta.setCustomModelData(itemUtil.getModel());
                }

                item.setItemMeta(meta);
                inv.setItem(slot, item);
            } else if (slot >= 45 && slot <= 53) {
                plugin.getLogger().warning("ページ" + page + "のスロット" + slot + "はメニュー専用スロットです．config.ymlから除外してください．");
            }

            int maxPage = guiUtil.getMaxPage();

            if (page > 1) {
                ItemStack prev = new ItemStack(Material.ARROW);
                ItemMeta meta = prev.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "戻る");
                prev.setItemMeta(meta);
                inv.setItem(45, prev);
            }

            if (page < maxPage) {
                ItemStack next = new ItemStack(Material.ARROW);
                ItemMeta meta = next.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "次へ");
                next.setItemMeta(meta);
                inv.setItem(53, next);
            }

            ItemStack close = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = close.getItemMeta();
            closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&c&l閉じる"));
            closeMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&7クリックで閉じる")));
            close.setItemMeta(closeMeta);
            inv.setItem(49, close);

            this.mapUtil.setPlayerPage(player.getUniqueId(), page);
            player.openInventory(inv);
        }
    }

    private static String getString(int playerAmount, int amount) {
        String hasItemMessage;
        if (playerAmount >= amount) {
            hasItemMessage = ChatColor.AQUA + " (" + playerAmount + "個所持)";
        } else if (playerAmount > 0) {
            hasItemMessage = ChatColor.YELLOW + " (" + playerAmount + "/" + amount + "個所持)";
        } else {
            hasItemMessage = ChatColor.RED + " (所持していません)";
        }
        return hasItemMessage;
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
