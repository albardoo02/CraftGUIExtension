package net.azisaba.life;

import net.azisaba.life.commands.CraftGuiCommand;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.listener.GuiClickListener;
import net.azisaba.life.utils.ConfigUtil;
import net.azisaba.life.utils.GuiUtil;
import net.azisaba.life.utils.ItemUtil;
import net.azisaba.life.utils.MapUtil;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class CraftGUIExtension extends JavaPlugin {

    private ConfigUtil configUtil;
    private GuiManager guiManager;
    private MapUtil mapUtil;
    private GuiUtil guiUtil;

    private Map<String, Map<Integer, ItemUtil>> loadedItems;
    private Map<String, List<String>> loadedLores;
    private int totalItems;
    private int errorItems;
    private List<String> errorDetails;

    private static boolean enabled = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.configUtil = new ConfigUtil(this);

        String currentVersion = getConfig().getString("configVersion", "0.0");
        String CONFIG_VERSION = "1.3";
        if (!currentVersion.equals(CONFIG_VERSION)) {
            configUtil.updateConfig();
        } else {
            getLogger().info("config.ymlは最新バージョンです");
        }

        this.guiUtil = new GuiUtil(this, configUtil);
        this.mapUtil = new MapUtil();
        this.loadedItems = configUtil.loadItems();
        this.loadedLores = configUtil.loadLores();

        GuiManager guiManager = new GuiManager(this, mapUtil, guiUtil, loadedItems, loadedLores);

        this.totalItems = 0;
        for (Map<Integer, ItemUtil> pageItems : loadedItems.values()) {
         this.totalItems += pageItems.size();
        }
        this.errorItems = configUtil.getErrorItemsCount();
        this.errorDetails = configUtil.getErrorsDetail();

        logConfigSummary();

        configUtil.createLogDirectory();

        this.getCommand("craftgui").setExecutor(new CraftGuiCommand(this, mapUtil, guiManager));
        this.getServer().getPluginManager().registerEvents(new GuiClickListener(this, mapUtil, guiManager, guiUtil, loadedItems, loadedLores, configUtil), this);
    }

    private void logConfigSummary() {
        getLogger().info("--- CraftGUIExtension Config Loading Summary ---");
        getLogger().info(ChatColor.GREEN + "✓ " + ChatColor.RESET + "正常に読み込まれたアイテム数: " + totalItems);
        if (errorItems > 0) {
            getLogger().info(ChatColor.RED+ "✘ " + ChatColor.RESET + "読み込み途中にエラーが発生したアイテム数: " + errorItems);
        }
        if (!errorDetails.isEmpty()) {
            getLogger().warning("エラー詳細:");
            for (String error : errorDetails) {
                getLogger().warning("  - " + error);
            }
        } else {
            getLogger().info("読み込みエラーが発生したエラーはありませんでした");
        }
        getLogger().info("-------------------------------------------------");
    }

    private void loadPluginData() {
        this.loadedItems = configUtil.loadItems();
        this.loadedLores = configUtil.loadLores();

        this.guiUtil = new GuiUtil(this, configUtil);
        this.mapUtil = new MapUtil();
        this.guiManager = new GuiManager(this, mapUtil, guiUtil, loadedItems, loadedLores);

        this.totalItems = 0;
        for (Map<Integer, ItemUtil> pageItems : loadedItems.values()) {
            this.totalItems += pageItems.size();
        }
        this.errorItems = configUtil.getErrorItemsCount();
        this.errorDetails = configUtil.getErrorsDetail();

        logConfigSummary();
    }

    public void reloadPluginConfig() {
        this.getLogger().info("CraftGUI Extensionのconfig.ymlを再読み込みしています...");
        this.reloadConfig();
        loadPluginData();
        this.getLogger().info(ChatColor.GREEN +  "CraftGUI Extensionのconfig.ymlの再読み込みが完了しました");
    }
}
