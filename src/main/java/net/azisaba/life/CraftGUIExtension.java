package net.azisaba.life;

import net.azisaba.life.commands.CraftGuiExtensionCommand;
import net.azisaba.life.commands.OpenGuiCommand;
import net.azisaba.life.utils.ConfigUtil;
import net.azisaba.life.utils.MapUtil;
import net.azisaba.life.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftGUIExtension extends JavaPlugin {

    private ConfigUtil configUtil = new ConfigUtil(this);
    private MessageUtil messageUtil;
    private GuiHandler guiHandler;
    private MapUtil mapUtil;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = getConfig();
        String currentVersion = config.getString("configVersion", "0.0");
        String CONFIG_VERSION = "1.0";
        if (!currentVersion.equals(CONFIG_VERSION)) {
            configUtil.updateConfig();
        } else {
            getLogger().info("Configファイルは最新です");
        }

        messageUtil = new MessageUtil(this);
        mapUtil = new MapUtil();
        guiHandler = new GuiHandler(this, messageUtil, mapUtil);

        this.getLogger().info("CraftGUIExtension has been enabled");
        this.getServer().getPluginManager().registerEvents(new GuiListener(this, messageUtil, guiHandler, mapUtil), this);
        this.getCommand("craftguiextension").setExecutor(new CraftGuiExtensionCommand(this, messageUtil));
        this.getCommand("rgui").setExecutor(new OpenGuiCommand(this, messageUtil, guiHandler, mapUtil));


    }

    @Override
    public void onDisable() {
        this.saveConfig();
        this.getLogger().info("CraftGUIExtension has been disabled");
    }
}
