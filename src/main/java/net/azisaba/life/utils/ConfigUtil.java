package net.azisaba.life.utils;

import net.azisaba.life.CraftGUIExtension;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigUtil {

    private final CraftGUIExtension plugin;
    private int errorItems = 0;
    private final List<String> errorDetail = new ArrayList<>();

    public ConfigUtil(CraftGUIExtension plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File oldFolder = new File(plugin.getDataFolder(), "oldConfig");

        if (!oldFolder.exists()) {
            oldFolder.mkdir();
        }
        String oldFileName = "config-v" + plugin.getConfig().getString("configVersion", "0.0") + ".yml";
        Path oldPath = Paths.get(plugin.getDataFolder().getPath(), "oldConfig", oldFileName);
        try {
            Files.move(configFile.toPath(), oldPath, StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info(ChatColor.YELLOW + "新しいバージョンのconfig.ymlが必要です");
            plugin.getLogger().info(ChatColor.YELLOW + "config.ymlの更新をしています...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        plugin.getLogger().info(ChatColor.GREEN + "config.ymlを最新バージョンに更新しました");
    }

    public Map<String, Map<Integer, ItemUtil>> loadItems() {
        Map<String, Map<Integer, ItemUtil>> items = new HashMap<>();
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("Items");

        if (itemsSection == null) {
            plugin.getLogger().warning("config.ymlにItemsセクションがありません");
            return items;
        }

        for (String pageKey : itemsSection.getKeys(false)) {
            ConfigurationSection pageSection = itemsSection.getConfigurationSection(pageKey);
            if (pageSection == null) {
                plugin.getLogger().warning(pageKey + "は無効なセクションです");
                continue;
            }

            Map<Integer, ItemUtil> pageItems = new HashMap<>();
            for (String slotKey : pageSection.getKeys(false)) {
                ConfigurationSection itemSection = pageSection.getConfigurationSection(slotKey);
                if (itemSection == null) {
                    addError(pageKey, slotKey, "Itemsセクションはnullです");
                    continue;
                }

                try {
                    boolean enabled = itemSection.getBoolean("enabled");
                    Material material = Material.valueOf(itemSection.getString("material", "").toUpperCase());
                    String displayName = ChatColor.translateAlternateColorCodes('&', itemSection.getString("displayName", ""));
                    String loreKey = itemSection.getString("lore", "CommonLore");
                    boolean enchanted = itemSection.getBoolean("enchanted");
                    int model = itemSection.getInt("model");

                    List<RequiredOrResultItem> resultItems = parseRequiredOrResultItemsList(itemSection.getList("resultItems"), "resultItems", pageKey, slotKey);
                    List<RequiredOrResultItem> requiredItems = parseRequiredOrResultItemsList(itemSection.getList("requiredItems"), "requiredItems", pageKey, slotKey);

                    ItemUtil customItem = new ItemUtil(enabled, material, displayName, loreKey, enchanted, model, resultItems, requiredItems);
                    pageItems.put(Integer.parseInt(slotKey), customItem);

                } catch (IllegalArgumentException e) {
                    addError(pageKey, slotKey, "無効なアイテムIDかその他エラー: " + e.getMessage());
                } catch (NullPointerException e) {
                    addError(pageKey, slotKey, "必要なフィールドがありません " + e.getMessage());
                } catch (Exception e) {
                    addError(pageKey, slotKey, "不明なエラーが発生しました: " + e.getMessage());
                }
            }
            items.put(pageKey, pageItems);
        }
        return items;
    }

    private List<RequiredOrResultItem> parseRequiredOrResultItemsList(List<?> itemList, String parentKey, String pageKey, String slotKey) {
        List<RequiredOrResultItem> items = new ArrayList<>();
        if (itemList == null || itemList.isEmpty()) {
            plugin.getLogger().log(Level.FINE, String.format("ページ%sのスロット%sは欠落もしくは空のためスキップします", pageKey, slotKey));
            return items;
        }

        int index = 0;
        for (Object obj : itemList) {
            if (!(obj instanceof Map)) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] はマップではありません．見つかった型: " + obj.getClass().getName());
                index++;
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) obj;

            try {
                // boolean isMythicItem = (boolean) itemMap.getOrDefault("isMythicItem", false);
                String mmid = (String) itemMap.get("mmid");
                String typeStr = (String) itemMap.get("type");

                boolean isMythicItem;
                Material type = null;

                if (mmid != null && !mmid.trim().isEmpty()) {
                    isMythicItem = true;
                    if (typeStr != null && !typeStr.trim().isEmpty()) {
                        addError(pageKey, slotKey, parentKey + "[" + index + "] mmidとtypeの両方が設定されているため，mmidが使用されました");
                    }
                } else if (typeStr != null && !typeStr.trim().isEmpty()) {
                    isMythicItem = false;
                    try {
                        type = Material.valueOf(typeStr.trim().toUpperCase());
                    } catch (IllegalArgumentException exception) {
                        addError(pageKey, slotKey, parentKey + "[" + index + "] " + typeStr + "は無効なアイテムIDです");
                        index++;
                        continue;
                    }
                } else {
                    addError(pageKey, slotKey, parentKey + "[" + index + "] mmidもしくはtypeを設定してください");
                    index++;
                    continue;
                }

                String displayName = ChatColor.translateAlternateColorCodes('&', (String) itemMap.getOrDefault("displayName", ""));
                int amount = 1;
                Object amountObj = itemMap.get("amount");
                if (amountObj instanceof Integer) {
                    amount = (Integer) amountObj;
                } else if (amountObj != null) {
                    addError(pageKey, slotKey, parentKey + "[" + index + "] " + amountObj + "は有効な整数ではありません");
                    index++;
                    continue;
                }

                if (amount < 0) {
                    addError(pageKey, slotKey, parentKey + "[" + index + "] amountは0より大きい値を設定して下さい");
                    index++;
                    continue;
                }

                items.add(new RequiredOrResultItem(isMythicItem, mmid, type, displayName, amount));
            } catch (ClassCastException e) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] データ型が一致しません: " + e.getMessage() + ".Map:" + itemMap);
            }  catch (Exception e) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] 不明なエラーが発生しました: " + e.getMessage());
            }
            index++;
        }
        return items;
    }


    public Map<String, List<String>> loadLores() {
        Map<String, List<String>> lores = new HashMap<>();
        ConfigurationSection loresSection = plugin.getConfig().getConfigurationSection("Lores");

        if (loresSection == null) {
            plugin.getLogger().warning("Loresセクションが見つかりません");
            return lores;
        }

        for (String loreKey : loresSection.getKeys(false)) {
            List<String> loreLines = loresSection.getStringList(loreKey);
            List<String> coloredLoreLines = new ArrayList<>();
            for (String line : loreLines) {
                coloredLoreLines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            lores.put(loreKey, coloredLoreLines);
        }
        return lores;
    }

    private void addError(String pageKey, String slotKey, String message) {
        errorItems++;
        errorDetail.add(String.format("ページ%sのスロット%sでエラーが発生しました: %s", pageKey, slotKey, message));
    }

    public int getErrorItemsCount() {
        return errorItems;
    }

    public List<String> getErrorsDetail() {
        return errorDetail;
    }

}
