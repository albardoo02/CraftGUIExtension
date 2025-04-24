package net.azisaba.life.commands;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftGuiExtensionCommand implements CommandExecutor {

    private final CraftGUIExtension plugin;
    private final MessageUtil messageUtil;

    public CraftGuiExtensionCommand(CraftGUIExtension plugin, MessageUtil messageUtil) {
        this.plugin = plugin;
        this.messageUtil = messageUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String CommandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtil.sendMessage(sender, "&cコンソールからは実行できません");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            messageUtil.sendMessage(player, "/cge <reload>");
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("craftgui.command.reload")) {
                    plugin.reloadConfig();
                    messageUtil.sendMessage(player, "&aConfigをリロードしました");
                } else {
                    messageUtil.sendMessage(player,"&c権限がありません");
                }
                return true;
            }
        }
        return true;
    }
}
