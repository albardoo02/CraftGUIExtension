package net.azisaba.life.utils;

import net.azisaba.life.CraftGUIExtension;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    private String prefix;
    private final CraftGUIExtension plugin;

    public MessageUtil(CraftGUIExtension plugin) {
        this.plugin = plugin;
        String prefixConfig = plugin.getConfig().getString("prefix");
        if (prefixConfig == null) {
            this.prefix = "§7[§aCraftGUI§7]§r ";
        } else {
            this.prefix = ChatColor.translateAlternateColorCodes('&', prefixConfig) + " ";
        }
    }

    public void sendMessage(CommandSender sender, String message) {
        if (plugin == null || prefix == null) {
            Bukkit.getLogger().warning("Configでprefixが設定されていないため、デフォルトのprefixが適用されました");
            return;
        }
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }
}
