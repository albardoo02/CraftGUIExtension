package net.azisaba.life;

import net.azisaba.life.commands.CraftGuiCommand;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.listener.GuiClickListener;
import net.azisaba.life.utils.ConfigUtil;
import net.azisaba.life.utils.GuiUtil;
import net.azisaba.life.utils.MapUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftGUIExtension extends JavaPlugin {

    private ConfigUtil configUtil = new ConfigUtil(this);
    private GuiUtil guiUtil;
    private MapUtil mapUtil;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        guiUtil = new GuiUtil(this);
        mapUtil = new MapUtil();
        guiManager = new GuiManager(this, mapUtil, guiUtil);

        String currentVersion = getConfig().getString("configVersion", "0.0");
        String CONFIG_VERSION = "1.0";
        if (!currentVersion.equals(CONFIG_VERSION)) {
            configUtil.updateConfig();
        } else {
            getLogger().info("config.ymlは最新バージョンです");
        }

        this.getCommand("raggui").setExecutor(new CraftGuiCommand(this, mapUtil, guiManager));
        this.getServer().getPluginManager().registerEvents(new GuiClickListener(this, mapUtil, guiManager, guiUtil), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
