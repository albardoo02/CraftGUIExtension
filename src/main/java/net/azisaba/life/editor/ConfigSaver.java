package net.azisaba.life.editor;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.utils.MythicItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        List<Map<String, Object>> requiredList = new ArrayList<>();
        for (ItemStack item : builder.getRequiredItems()) {
            requiredList.add(serializeItem(item));
        }
        config.set(path + ".requiredItems", requiredList);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ItemStack item : builder.getResultItems()) {
            resultList.add(serializeItem(item));
        }
        config.set(path + ".resultItems", resultList);

        plugin.saveConfig();
        return true;
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