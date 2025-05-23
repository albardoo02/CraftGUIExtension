package net.azisaba.life.utils;

import net.azisaba.life.CraftGUIExtension;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigUtil {

    private final CraftGUIExtension plugin;

    public ConfigUtil(CraftGUIExtension plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File oldFolder = new File(plugin.getDataFolder(), "oldConfig");

        if (!oldFolder.exists()) {
            oldFolder.mkdir();
        }
        String oldFileName = "config-v" + plugin.getConfig().getString("configVersion", "0.0") + ".yml";
        Path oldPath = Paths.get(plugin.getDataFolder().getPath(), "oldConfig", oldFileName);
        try {
            Files.move(configFile.toPath(), oldPath, StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info(ChatColor.GOLD + "config.ymlに新しいバージョンが見つかったため、config.ymlを更新しています...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        plugin.getLogger().info(ChatColor.GREEN + "config.ymlを更新しました");
    }
}
