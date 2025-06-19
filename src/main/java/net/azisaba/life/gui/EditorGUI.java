package net.azisaba.life.gui;

import net.azisaba.life.editor.RecipeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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

        gui.setItem(0, builder.getMainItem());
        gui.setItem(1, createItem(Material.PAPER, "§bモデルデータ: §f" + builder.getModelData()));

        ItemStack requiredHopper = createItem(Material.HOPPER, "§6必要アイテム一覧");
        ItemMeta requiredMeta = requiredHopper.getItemMeta();
        requiredMeta.setLore(generateSummaryLore(builder.getRequiredItems()));
        requiredHopper.setItemMeta(requiredMeta);
        gui.setItem(3, requiredHopper);

        ItemStack resultDispenser = createItem(Material.DISPENSER, "§a付与アイテム一覧");
        ItemMeta resultMeta = resultDispenser.getItemMeta();
        resultMeta.setLore(generateSummaryLore(builder.getResultItems()));
        resultDispenser.setItemMeta(resultMeta);
        gui.setItem(4, resultDispenser);

        List<ItemStack> requiredItems = builder.getRequiredItems();
        for (int i = 0; i < requiredItems.size(); i++) {
            if (i >= 36) break;
            gui.setItem(i + 9, requiredItems.get(i));
        }

        gui.setItem(45, createItem(Material.BARRIER, "§cキャンセル"));
        gui.setItem(53, createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lアイテム設定を保存する", "§7現在の設定を保存する"));

        player.openInventory(gui);
    }

    private static List<String> generateSummaryLore(List<ItemStack> items) {
        List<String> lore = new ArrayList<>();
        if (items.isEmpty()) {
            lore.add("§7(なし)");
            return lore;
        }

        Map<ItemStack, Integer> aggregated = new LinkedHashMap<>();
        for (ItemStack item : items) {
            ItemStack key = item.clone();
            key.setAmount(1);
            aggregated.put(key, aggregated.getOrDefault(key, 0) + item.getAmount());
        }

        lore.add("§7--------------------");
        for (Map.Entry<ItemStack, Integer> entry : aggregated.entrySet()) {
            ItemStack item = entry.getKey();
            int totalAmount = entry.getValue();

            String displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    ? item.getItemMeta().getDisplayName()
                    : item.getType().name();

            lore.add("§f- " + displayName + " §ex" + totalAmount);
        }
        lore.add("§7--------------------");

        return lore;
    }
}
