package net.azisaba.life.gui;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.utils.GuiUtil;
import net.azisaba.life.utils.MapUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiManager {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiUtil guiUtil;

    public GuiManager(CraftGUIExtension plugin, MapUtil mapUtil, GuiUtil guiUtil) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiUtil = guiUtil;
    }

    public void openCraftGUI(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "CraftGUI Extension - Page" + page);
        ConfigurationSection itemSection = this.plugin.getConfig().getConfigurationSection("Items.page" + page);
        if (itemSection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cページ" + page + "は存在しません"));
        } else {
            for(String key : itemSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    if (slot >= 0 && slot <= 44) {
                        ConfigurationSection itemData = itemSection.getConfigurationSection(key);
                        if (itemData != null) {
                            String materialName = itemData.getString("material");
                            if (materialName != null && !materialName.isEmpty()) {
                                Material material = Material.matchMaterial(materialName.toUpperCase());
                                if (material != null) {
                                    ItemStack item = new ItemStack(material);
                                    ItemMeta meta = item.getItemMeta();
                                    String displayName = itemData.getString("displayName");
                                    if (displayName != null) {
                                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                                    }

                                    String loreKey = itemData.getString("lore");
                                    if (loreKey != null && this.plugin.getConfig().contains("Lores." + loreKey)) {
                                        List<String> lore = this.plugin.getConfig().getStringList("Lores." + loreKey);
                                        List<String> coloredLore = new ArrayList<>();
                                        for (String line : lore) {
                                            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                                        }

                                        List<String> requirementLore = null;
                                        if (itemData.contains("requiredItems")) {
                                            requirementLore = new ArrayList<>();
                                            requirementLore.add(ChatColor.GRAY + "必要素材:");

                                            List<?> requiredItems = itemData.getList("requiredItems");
                                            if (requiredItems != null) {
                                                for (Object obj : requiredItems) {
                                                    if (obj instanceof Map) {
                                                        @SuppressWarnings("unchecked")
                                                        Map<String, Object> requiredItemMap = (Map<String, Object>) obj;

                                                        boolean isMythic = (boolean) requiredItemMap.getOrDefault("isMythicItem", false);
                                                        String requiredType = (String) requiredItemMap.getOrDefault("type", "UNKNOWN");
                                                        String requiredDisplay = (String) requiredItemMap.getOrDefault("displayName", requiredType);
                                                        int amount = (int) requiredItemMap.getOrDefault("amount", 1);

                                                        int playerAmount = 0;
                                                        if (isMythic) {
                                                            playerAmount = guiUtil.countMythic(player, requiredType);
                                                        } else {
                                                            Material mat = Material.matchMaterial(requiredType.toUpperCase());
                                                            if (mat != null) {
                                                                playerAmount = guiUtil.countVanilla(player, mat);
                                                            }
                                                        }

                                                        String title = playerAmount >= amount ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✘ ";
                                                        ChatColor color = playerAmount >= amount ? ChatColor.GREEN : ChatColor.RED;

                                                        String hasItemMessage = getString(playerAmount, amount);

                                                        requirementLore.add(title + ChatColor.translateAlternateColorCodes('&', requiredDisplay)
                                                                + color + " x" + amount + hasItemMessage
                                                                + (isMythic ? ChatColor.GRAY + " (Mythic)" : ChatColor.DARK_GRAY + " (Vanilla)"));
                                                    }
                                                }
                                            }
                                        }
                                        coloredLore.addAll(requirementLore);
                                        meta.setLore(coloredLore);
                                    }

                                    boolean enchanted = itemData.getBoolean("enchanted");
                                    if (enchanted) {
                                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    }

                                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                                    if (meta != null) {
                                        Integer model = itemData.getInt("model");
                                        if (model != null && model > 0) {
                                            meta.setCustomModelData(model);
                                        }
                                    }

                                    item.setItemMeta(meta);
                                    inv.setItem(slot, item);
                                }
                            }
                        }
                    } else if (slot >= 45 && slot <= 53) {
                        Bukkit.getLogger().warning("ページ" + page + "のスロット" + slot + "はメニュー専用スロットです．config.ymlから除外してください．");
                    }
                } catch (NumberFormatException var19) {
                    Bukkit.getLogger().warning("config.ymlの項目" + key + "はスロット番号として不正です．");
                }
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
        String hasItemMessage = "";
        if (playerAmount >= amount) {
            hasItemMessage = ChatColor.AQUA + " (" + playerAmount + "個所持)";
        } else if (playerAmount > 0) {
            hasItemMessage = ChatColor.YELLOW + " (" + playerAmount + " / " + ChatColor.GREEN + amount + "個所持)";
        } else {
            hasItemMessage = ChatColor.RED + " (所持していません)";
        }
        return hasItemMessage;
    }
}
