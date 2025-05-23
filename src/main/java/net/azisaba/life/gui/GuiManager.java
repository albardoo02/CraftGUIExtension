package net.azisaba.life.gui;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.utils.GuiUtil;
import net.azisaba.life.utils.ItemUtil;
import net.azisaba.life.utils.MapUtil;
import net.azisaba.life.utils.RequiredOrResultItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiManager implements Listener {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiUtil guiUtil;
    private final Map<String, Map<Integer, ItemUtil>> loadedItems;
    private final Map<String, List<String>> loadedLores;

    public GuiManager(CraftGUIExtension plugin, MapUtil mapUtil, GuiUtil guiUtil,
                      Map<String, Map<Integer, ItemUtil>> loadedItems, Map<String, List<String>> loadedLores) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiUtil = guiUtil;
        this.loadedItems = loadedItems;
        this.loadedLores = loadedLores;
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
                requirementLore.add(ChatColor.GRAY + "必要素材:");

                boolean canCraft = true;

                for (RequiredOrResultItem requiredItem : itemUtil.getRequiredItems()) {
                    String requiredDisplay = requiredItem.getDisplayName();
                    int amount = requiredItem.getAmount();

                    int playerAmount = 0;
                    if (requiredItem.isMythicItem()) {
                        playerAmount = guiUtil.countMythic(player, requiredItem.getMmid());
                    } else if (requiredItem.getType() != null) {
                        playerAmount = guiUtil.countVanilla(player, requiredItem.getType());
                    }

                    String title = playerAmount >= amount ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✘ ";
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
                    combinedLore.add(ChatColor.RED + "✘ 素材が不足しているため変換できません");
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
}
