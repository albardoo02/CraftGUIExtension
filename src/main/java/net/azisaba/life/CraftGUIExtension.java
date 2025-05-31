package net.azisaba.life;

import net.azisaba.life.commands.CraftGuiCommand;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.listener.GuiClickListener;
import net.azisaba.life.utils.ConfigUtil;
import net.azisaba.life.utils.GuiUtil;
import net.azisaba.life.utils.ItemUtil;
import net.azisaba.life.utils.MapUtil;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
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
        String CONFIG_VERSION = "1.2";
        if (!currentVersion.equals(CONFIG_VERSION)) {
            configUtil.updateConfig();
        } else {
            getLogger().info("config.yml is up to data.");
        }

        this.guiUtil = new GuiUtil(this, loadedItems, configUtil);
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

        this.getCommand("craftgui").setExecutor(new CraftGuiCommand(this, mapUtil, guiManager, guiUtil));
        this.getServer().getPluginManager().registerEvents(new GuiClickListener(this, mapUtil, guiManager, guiUtil, loadedItems, loadedLores), this);

        this.getLogger().info("CraftGUI Extension has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("CraftGUI Extension has been disabled.");
    }

    private void logConfigSummary() {
        getLogger().info("--- CraftGUIExtension Config Loading Summary ---");
        getLogger().info(ChatColor.GREEN + "✓ " + ChatColor.RESET + "Total items successfully loaded from config: " + totalItems);
        getLogger().info(ChatColor.RED+ "✘ " + ChatColor.RESET + "Items with errors (failed to parse): " + errorItems);
        if (!errorDetails.isEmpty()) {
            getLogger().warning("Details of parsing errors for items:");
            for (String error : errorDetails) {
                getLogger().warning("  - " + error);
            }
        } else {
            getLogger().info("No specific parsing errors reported for items.");
        }
        getLogger().info("-------------------------------------------------");
    }

    private void loadPluginData() {
        this.loadedItems = configUtil.loadItems();
        this.loadedLores = configUtil.loadLores();

        this.guiUtil = new GuiUtil(this, loadedItems, configUtil);
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
        this.getLogger().info("Reloading CraftGUI Extension config...");
        this.reloadConfig();
        loadPluginData();
        this.getLogger().info(ChatColor.GREEN +  "Successfully reloaded CraftGUI Extension config.");
    }
}
