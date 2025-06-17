package net.azisaba.life.gui;

import net.azisaba.life.editor.RecipeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class EditorGUI {

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void openStep1_RequiredItemsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "CraftGUI登録 - 要求アイテム");
        ItemStack placeholder = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 36; i < gui.getSize(); i++) { gui.setItem(i, placeholder); }

        gui.setItem(45, createItem(Material.BARRIER, "§cキャンセル"));
        gui.setItem(53, createItem(Material.LIME_WOOL, "§a次へ", "§7付与アイテムの設定に進みます"));

        player.openInventory(gui);
    }

    public static void openStep2_ResultItemGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "CraftGUI登録 - 付与アイテム");
        ItemStack placeholder = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) { gui.setItem(i, placeholder); }

        gui.setItem(13, null);
        gui.setItem(18, createItem(Material.BARRIER, "§cキャンセル"));
        gui.setItem(26, createItem(Material.LIME_WOOL, "§a次へ", "§7最終確認に進みます"));

        player.openInventory(gui);
    }

    public static void openStep3_ConfirmGUI(Player player, RecipeBuilder builder) {
        Inventory gui = Bukkit.createInventory(null, 54, "CraftGUI登録 - 登録確認");
        ItemStack placeholder = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) { gui.setItem(i, placeholder); }

        if (!builder.getResultItems().isEmpty()) {
            gui.setItem(4, builder.getResultItems().get(0));
        }

        gui.setItem(19, createItem(Material.HOPPER, "§6要求アイテム"));
        int requiredSlot = 20;
        for(ItemStack item : builder.getRequiredItems()) {
            if (requiredSlot > 25) break;
            gui.setItem(requiredSlot++, item);
        }

        gui.setItem(45, createItem(Material.BARRIER, "§cキャンセル"));
        gui.setItem(53, createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lアイテム設定を保存する", "§7現在の設定をconfig.ymlに書き込みます"));

        player.openInventory(gui);
    }
}
