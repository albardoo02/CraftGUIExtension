package net.azisaba.life.editor;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.gui.EditorGUI;
import org.bukkit.entity.Player;

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
        EditorGUI.openMainItemGUI(player);
    }

    public void nextStep(Player player) {
        setIntentionalClosure(player);
        int currentStep = playerSteps.getOrDefault(player.getUniqueId(), 0);
        if (currentStep == 0) return;

        playerSteps.put(player.getUniqueId(), currentStep + 1);
        RecipeBuilder builder = getBuilder(player);

        switch (currentStep) {
            case 1: EditorGUI.openRequiredItemsGUI(player, builder); break;
            case 2: EditorGUI.openResultItemsGUI(player, builder); break;
            case 3: EditorGUI.openConfirmGUI(player, builder); break;
        }
    }

    public void save(Player player) {
        setIntentionalClosure(player);
        RecipeBuilder builder = getBuilder(player);
        if (builder != null) {
            ConfigSaver saver = new ConfigSaver(plugin, builder);
            if (saver.save()) {
                plugin.reloadPluginConfig();
                player.sendMessage("アイテム設定をconfig.ymlに保存しました");
            } else {
                player.sendMessage("アイテム設定の保存に失敗しました");
            }
        }
        player.closeInventory();
        cancel(player);
    }

    public void cancel(Player player) {
        setIntentionalClosure(player);
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
