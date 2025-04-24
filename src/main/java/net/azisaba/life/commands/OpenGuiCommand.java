package net.azisaba.life.commands;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.GuiHandler;
import net.azisaba.life.utils.MapUtil;
import net.azisaba.life.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenGuiCommand implements CommandExecutor {

    private final CraftGUIExtension plugin;
    private final MessageUtil messageUtil;
    private final GuiHandler guiHandler;
    private final MapUtil mapUtil;

    public OpenGuiCommand(CraftGUIExtension plugin, MessageUtil messageUtil, GuiHandler guiHandler, MapUtil mapUtil) {
        this.plugin = plugin;
        this.messageUtil = messageUtil;
        this.guiHandler = guiHandler;
        this.mapUtil = mapUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String CommandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtil.sendMessage(sender, "&cコンソールからは実行できません");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            guiHandler.openGui(player, 1);
            int page = mapUtil.getPlayerPage(player.getUniqueId());
            guiHandler.openGui(player, page);
            messageUtil.sendMessage(player, "&2CraftGUIを開きました");
            return true;
        }
        return true;
    }
}
