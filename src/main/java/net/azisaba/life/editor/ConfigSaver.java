package net.azisaba.life.editor;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.utils.MythicItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ConfigSaver {

    private final CraftGUIExtension plugin;
    private final RecipeBuilder builder;

    public ConfigSaver(CraftGUIExtension plugin, RecipeBuilder builder) {
        this.plugin = plugin;
        this.builder = builder;
    }

    public boolean save() {
        FileConfiguration config = plugin.getConfig();

        if (builder.getResultItems().isEmpty()) {
            return false;
        }

        ItemStack mainDisplayItem = builder.getResultItems().get(0);
        String path = findNextAvailablePath(config);

        config.set(path + ".enabled", true);
        config.set(path + ".lore", "CommonLore");
        config.set(path + ".material", mainDisplayItem.getType().toString());

        ItemMeta meta = mainDisplayItem.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            config.set(path + ".displayName", meta.getDisplayName());
        } else {
            config.set(path + ".displayName", mainDisplayItem.getType().name());
        }

        config.set(path + ".enchanted", meta != null && meta.hasEnchants());

        if (meta != null && meta.hasCustomModelData()) {
            config.set(path + ".model", meta.getCustomModelData());
        } else {
            config.set(path + ".model", 0);
        }

        List<ItemStack> aggregatedRequired = aggregateItems(builder.getRequiredItems());
        List<Map<String, Object>> requiredList = new ArrayList<>();
        for (ItemStack item : aggregatedRequired) {
            requiredList.add(serializeItem(item));
        }
        config.set(path + ".requiredItems", requiredList);

        List<ItemStack> aggregatedResult = aggregateItems(builder.getResultItems());
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ItemStack item : aggregatedResult) {
            resultList.add(serializeItem(item));
        }
        config.set(path + ".resultItems", resultList);

        plugin.saveConfig();
        return true;
    }

    private List<ItemStack> aggregateItems(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        Map<ItemStack, Integer> aggregatedMap = new LinkedHashMap<>();
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            ItemStack key = item.clone();
            key.setAmount(1);
            aggregatedMap.put(key, aggregatedMap.getOrDefault(key, 0) + item.getAmount());
        }

        List<ItemStack> finalItems = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : aggregatedMap.entrySet()) {
            ItemStack finalItem = entry.getKey().clone();
            finalItem.setAmount(entry.getValue());
            finalItems.add(finalItem);
        }

        return finalItems;
    }

    private String findNextAvailablePath(FileConfiguration config) {
        ConfigurationSection itemsSection = config.getConfigurationSection("Items");
        int targetPage = 1;
        int targetSlot = 0;

        if (itemsSection == null) return "Items.page1";

        Set<String> pageKeys = itemsSection.getKeys(false);
        if (!pageKeys.isEmpty()) {
            targetPage = pageKeys.stream().map(key -> key.replace("page", "")).mapToInt(Integer::parseInt).max().orElse(1);
        }

        ConfigurationSection lastPageSection = itemsSection.getConfigurationSection("page" + targetPage);
        if (lastPageSection != null) {
            Set<String> slotKeys = lastPageSection.getKeys(false);
            if (!slotKeys.isEmpty()) {
                int maxSlot = slotKeys.stream().mapToInt(Integer::parseInt).max().orElse(-1);
                if (maxSlot < 44) {
                    targetSlot = maxSlot + 1;
                } else {
                    targetPage++;
                    targetSlot = 0;
                }
            }
        }
        return "Items.page" + targetPage + "." + targetSlot;
    }

    private Map<String, Object> serializeItem(ItemStack item) {
        Map<String, Object> serialized = new HashMap<>();
        ItemMeta meta = item.getItemMeta();

        String mmid = MythicItemUtil.getMythicType(item);
        boolean hasDisplayName = meta != null && meta.hasDisplayName();

        if (hasDisplayName) {
            serialized.put("mmid", mmid);

        } else {
            serialized.put("type", item.getType().toString());
        }

        serialized.put("displayName", hasDisplayName ? meta.getDisplayName() : item.getType().name());
        serialized.put("amount", item.getAmount());

        return serialized;
    }
}