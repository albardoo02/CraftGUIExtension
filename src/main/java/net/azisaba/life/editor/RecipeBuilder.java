package net.azisaba.life.editor;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeBuilder {

    private ItemStack mainItem;
    private int modelData = 0;

    private final List<ItemStack> requiredItems = new ArrayList<>();
    private final List<ItemStack> resultItems = new ArrayList<>();

    public ItemStack getMainItem() {
        return mainItem;
    }

    public void setMainItem(ItemStack mainItem) {
        this.mainItem = mainItem;
    }

    public int getModelData() {
        return modelData;
    }

    public void setModelData(int modelData) {
        this.modelData = modelData;
    }

    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    public List<ItemStack> getResultItems() {
        return resultItems;
    }
}
