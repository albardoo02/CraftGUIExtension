package net.azisaba.life;

import net.azisaba.life.utils.MapUtil;
import net.azisaba.life.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GuiHandler {

    private final CraftGUIExtension plugin;
    private final MessageUtil messageUtil;
    private final MapUtil mapUtil;

    public GuiHandler(CraftGUIExtension plugin, MessageUtil messageUtil, MapUtil mapUtil) {
        this.plugin = plugin;
        this.messageUtil = messageUtil;
        this.mapUtil = mapUtil;
    }

    public void openGui(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "CraftGUI Extension - Page" + page);

        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("Items.page" + page);
        if (itemsSection == null) {
            messageUtil.sendMessage(player, "&cページ" + page + "は存在しません");
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            try {
                int slot = Integer.parseInt(key);
                if (slot >= 0 && slot <= 44) {
                    ConfigurationSection itemData = itemsSection.getConfigurationSection(key);
                    if (itemData == null) continue;

                    String materialName = itemData.getString("material");
                    if (materialName == null || materialName.isEmpty()) continue;

                    Material material = Material.matchMaterial(materialName.toUpperCase());
                    if (material == null) continue;

                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();

                    String displayName = itemData.getString("displayName");
                    if (displayName != null) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                    }

                    String loreKey = itemData.getString("lore");
                    if (loreKey != null && plugin.getConfig().contains("Lores." + loreKey)) {
                        List<String> lore = plugin.getConfig().getStringList("Lores." + loreKey);
                        List<String> coloredLore = new ArrayList<>();
                        for (String line : lore) {
                            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                        meta.setLore(coloredLore);
                    }

                    boolean enchanted = itemData.getBoolean("enchanted");
                    if (enchanted) {
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    if (meta != null) {
                        Integer model = itemData.getInt("model");
                        if (model != null && model > 0) {
                            meta.setCustomModelData(model);
                        }
                    }

                    item.setItemMeta(meta);
                    inv.setItem(slot, item);
                } else if (slot >= 45 && slot <= 53) {
                    Bukkit.getLogger().warning("スロット" + slot + "はメニュー専用スロットです。items.ymlから除外してください。");
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("items.ymlの項目" + key + "はスロット番号として不正です。");
            }
        }

        int maxPage = getMaxPage();

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

        mapUtil.setPlayerPage(player.getUniqueId(), page);
        player.openInventory(inv);


    }

    public int getMaxPage() {
        ConfigurationSection itemsRoot = plugin.getConfig().getConfigurationSection("Items");
        int max = 1;
        if (itemsRoot != null) {
            for (String key : itemsRoot.getKeys(false)) {
                if (key.startsWith("page")) {
                    try {
                        int num = Integer.parseInt(key.replace("page", ""));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return max;
    }
}
