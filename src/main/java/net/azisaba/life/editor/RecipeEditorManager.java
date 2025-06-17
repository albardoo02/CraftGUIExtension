package net.azisaba.life.editor;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.gui.EditorGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RecipeEditorManager {

    private final CraftGUIExtension plugin;
    private final Map<UUID, RecipeBuilder> builders = new HashMap<>();
    private final Map<UUID, Integer> playerSteps = new HashMap<>();
    private final Set<UUID> intentionalClosures = new HashSet<>();

    public RecipeEditorManager(CraftGUIExtension plugin) {
        this.plugin = plugin;
    }

    public void start(Player player) {
        builders.put(player.getUniqueId(), new RecipeBuilder());
        playerSteps.put(player.getUniqueId(), 1);
        EditorGUI.openStep1_RequiredItemsGUI(player);
    }

    public void nextStep(Player player) {
        setIntentionalClosure(player);
        int currentStep = playerSteps.getOrDefault(player.getUniqueId(), 0);
        if (currentStep == 0) return;

        playerSteps.put(player.getUniqueId(), currentStep + 1);

        switch (currentStep) {
            case 1:
                EditorGUI.openStep2_ResultItemGUI(player);
                break;
            case 2:
                EditorGUI.openStep3_ConfirmGUI(player, getBuilder(player));
                break;
        }
    }
    public void save(Player player) {
        setIntentionalClosure(player);
        RecipeBuilder builder = getBuilder(player);
        if (builder != null) {
            ConfigSaver saver = new ConfigSaver(plugin, builder);
            if (saver.save()) {
                plugin.reloadPluginConfig();
                player.sendMessage(ChatColor.GREEN + "アイテム設定をconfig.ymlに保存しました");
            } else {
                player.sendMessage(ChatColor.RED + "アイテム設定の保存に失敗しました．付与アイテムが設定されているか確認してください．");
            }
        }
        player.closeInventory();
        cancel(player);
    }

    public void cancel(Player player) {
        setIntentionalClosure(player);

        List<ItemStack> itemsToReturn = new ArrayList<>();
        RecipeBuilder builder = getBuilder(player);

        if (builder != null) {
            itemsToReturn.addAll(builder.getRequiredItems());
            itemsToReturn.addAll(builder.getResultItems());
        }

        InventoryView openInventoryView = player.getOpenInventory();
        if (openInventoryView.getTitle().startsWith("CraftGUI登録")) {
            Inventory currentGui = openInventoryView.getTopInventory();
            for (ItemStack item : currentGui.getContents()) {
                if (item != null && !item.getType().isAir()) {
                    if (item.getType() != Material.ARROW && item.getType() != Material.BARRIER && !item.getType().name().contains("STAINED_GLASS_PANE")) {
                        itemsToReturn.add(item);
                    }
                }
            }
        }

        if (!itemsToReturn.isEmpty()) {
            Collection<ItemStack> leftoverItems = player.getInventory().addItem(itemsToReturn.toArray(new ItemStack[0])).values();

            if (!leftoverItems.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "インベントリに空きがないため，一部のアイテムを足元にドロップしました．");
                for (ItemStack leftover : leftoverItems) {
                    player.getWorld().dropItem(player.getLocation(), leftover);
                }
            }
        }

        builders.remove(player.getUniqueId());
        playerSteps.remove(player.getUniqueId());
        player.closeInventory();
    }

    public RecipeBuilder getBuilder(Player player) {
        return builders.get(player.getUniqueId());
    }

    public int getPlayerStep(Player player) {
        return playerSteps.getOrDefault(player.getUniqueId(), 0);
    }

    public void setIntentionalClosure(Player player) {
        intentionalClosures.add(player.getUniqueId());
    }

    public boolean checkAndRemoveIntentionalClosure(Player player) {
        return intentionalClosures.remove(player.getUniqueId());
    }
}
