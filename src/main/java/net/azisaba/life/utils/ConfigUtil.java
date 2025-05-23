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
            plugin.getLogger().info(ChatColor.YELLOW + "New version of config.yml found.");
            plugin.getLogger().info(ChatColor.YELLOW + "Attempting update...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        plugin.getLogger().info(ChatColor.GREEN + "Successfully update config.yml.");
    }

    public Map<String, Map<Integer, ItemUtil>> loadItems() {
        Map<String, Map<Integer, ItemUtil>> items = new HashMap<>();
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("Items");

        if (itemsSection == null) {
            plugin.getLogger().warning("Config section 'Items' not found.");
            return items;
        }

        for (String pageKey : itemsSection.getKeys(false)) {
            ConfigurationSection pageSection = itemsSection.getConfigurationSection(pageKey);
            if (pageSection == null) {
                plugin.getLogger().warning("Invalid section under 'Items': " + pageKey);
                continue;
            }

            Map<Integer, ItemUtil> pageItems = new HashMap<>();
            for (String slotKey : pageSection.getKeys(false)) {
                ConfigurationSection itemSection = pageSection.getConfigurationSection(slotKey);
                if (itemSection == null) {
                    addError(pageKey, slotKey, "Item section is null.");
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
                    addError(pageKey, slotKey, "Invalid material or other parse error: " + e.getMessage());
                } catch (NullPointerException e) {
                    addError(pageKey, slotKey, "Missing required field: " + e.getMessage());
                } catch (Exception e) {
                    addError(pageKey, slotKey, "Unknown error: " + e.getMessage());
                }
            }
            items.put(pageKey, pageItems);
        }
        return items;
    }

    private List<RequiredOrResultItem> parseRequiredOrResultItemsList(List<?> itemList, String parentKey, String pageKey, String slotKey) {
        List<RequiredOrResultItem> items = new ArrayList<>();
        if (itemList == null || itemList.isEmpty()) {
            plugin.getLogger().log(Level.FINE, String.format("Section '%s' for page '%s', slot '%s' is missing or empty. Skipping.", parentKey, pageKey, slotKey));
            return items;
        }

        int index = 0;
        for (Object obj : itemList) {
            if (!(obj instanceof Map)) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] is not a map. Found type: " + obj.getClass().getName());
                index++;
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) obj;

            try {
                boolean isMythicItem = (boolean) itemMap.getOrDefault("isMythicItem", false);
                String mmid = (String) itemMap.get("mmid");
                Material type = null;
                if (!isMythicItem) {
                    String typeStr = (String) itemMap.get("type");
                    if (typeStr == null) {
                        throw new NullPointerException("'type' field is missing for non-Mythic item.");
                    }
                    type = Material.valueOf(typeStr.toUpperCase());
                }
                String displayName = ChatColor.translateAlternateColorCodes('&', (String) itemMap.getOrDefault("displayName", ""));
                int amount = (int) itemMap.getOrDefault("amount", 0);
                if (amount == 0 && itemMap.containsKey("amount") && itemMap.get("amount") instanceof Integer) {
                } else if (!itemMap.containsKey("amount")) {
                    throw new NullPointerException("'amount' field is missing.");
                }


                items.add(new RequiredOrResultItem(isMythicItem, mmid, type, displayName, amount));
            } catch (ClassCastException e) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] Data type mismatch: " + e.getMessage() + ". Map: " + itemMap);
            } catch (IllegalArgumentException e) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] Invalid material type or enum constant: " + e.getMessage());
            } catch (NullPointerException e) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] Missing required field: " + e.getMessage());
            } catch (Exception e) {
                addError(pageKey, slotKey, parentKey + "[" + index + "] Unknown error: " + e.getMessage());
            }
            index++;
        }
        return items;
    }


    public Map<String, List<String>> loadLores() {
        Map<String, List<String>> lores = new HashMap<>();
        ConfigurationSection loresSection = plugin.getConfig().getConfigurationSection("Lores");

        if (loresSection == null) {
            plugin.getLogger().warning("Config section 'Lores' not found.");
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
        errorDetail.add(String.format("Error in page '%s', slot '%s': %s", pageKey, slotKey, message));
        plugin.getLogger().log(Level.WARNING, String.format("Config load error in page '%s', slot '%s': %s", pageKey, slotKey, message));
    }

    public int getErrorItemsCount() {
        return errorItems;
    }

    public List<String> getErrorsDetail() {
        return errorDetail;
    }

}
