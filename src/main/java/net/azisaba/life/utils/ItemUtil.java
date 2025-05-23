package net.azisaba.life.utils;

import org.bukkit.Material;

import java.util.List;

public class ItemUtil {

    private final boolean enabled;
    private final Material material;
    private final String displayName;
    private final String loreKey; // Loresセクションのキー
    private final boolean enchanted;
    private final int model;
    private final List<RequiredOrResultItem> resultItems;
    private final List<RequiredOrResultItem> requiredItems;

    public ItemUtil(boolean enabled, Material material, String displayName, String loreKey, boolean enchanted, int model, List<RequiredOrResultItem> resultItems, List<RequiredOrResultItem> requiredItems) {
        this.enabled = enabled;
        this.material = material;
        this.displayName = displayName;
        this.loreKey = loreKey;
        this.enchanted = enchanted;
        this.model = model;
        this.resultItems = resultItems;
        this.requiredItems = requiredItems;
    }

    public boolean isEnabled() { return enabled; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public String getLoreKey() { return loreKey; }
    public boolean isEnchanted() { return enchanted; }
    public int getModel() { return model; }
    public List<RequiredOrResultItem> getResultItems() { return resultItems; }
    public List<RequiredOrResultItem> getRequiredItems() { return requiredItems; }
}