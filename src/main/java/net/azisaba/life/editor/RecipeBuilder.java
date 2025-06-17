package net.azisaba.life.editor;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeBuilder {

    private final List<ItemStack> requiredItems = new ArrayList<>();
    private final List<ItemStack> resultItems = new ArrayList<>();

    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    public List<ItemStack> getResultItems() {
        return resultItems;
    }
}