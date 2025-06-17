package net.azisaba.life.commands;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.editor.RecipeEditorManager;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.utils.MapUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CraftGuiCommand implements CommandExecutor, TabCompleter {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiManager guiManager;
    private final RecipeEditorManager editorManager;

    public CraftGuiCommand(CraftGUIExtension plugin, MapUtil mapUtil, GuiManager guiManager, RecipeEditorManager editorManager) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiManager = guiManager;
        this.editorManager = editorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String CommandLabel, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("craftguiextension.reload")) {
                sender.sendMessage(ChatColor.RED + "権限がありません");
                return true;
            }
            plugin.reloadPluginConfig();
            sender.sendMessage(ChatColor.GREEN + "CraftGUI ExtensionのConfigを再読み込みしました");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                guiManager.openCraftGUI(player, 1);
                mapUtil.setPlayerPage(player.getUniqueId(), 1);
                player.sendMessage(ChatColor.DARK_GREEN + "CraftGUI Extensionを開きました");
                return true;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("register")) {
                    if (player.hasPermission("craftguiextension.register")) {
                        editorManager.start(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "権限がありません");
                    }
                    return true;
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"/craftgui"));
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("craftguiextension.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("craftguiextension.register")) {
                completions.add("register");
            }
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
