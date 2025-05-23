package net.azisaba.life.commands;

import net.azisaba.life.CraftGUIExtension;
import net.azisaba.life.gui.GuiManager;
import net.azisaba.life.utils.MapUtil;
import net.azisaba.life.utils.MythicItemUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftGuiCommand implements CommandExecutor {

    private final CraftGUIExtension plugin;
    private final MapUtil mapUtil;
    private final GuiManager guiManager;

    public CraftGuiCommand(CraftGUIExtension plugin, MapUtil mapUtil, GuiManager guiManager) {
        this.plugin = plugin;
        this.mapUtil = mapUtil;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String CommandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行可能です");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2CraftGUI Extensionを開きました"));
            int page = mapUtil.getPlayerPage(player.getUniqueId());
            guiManager.openCraftGUI(player, page);
            mapUtil.setPlayerPage(player.getUniqueId(), page);
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mmid")) {
                ItemStack stack = player.getInventory().getItemInMainHand();
                String id = MythicItemUtil.getMythicType(stack);
                TextComponent component = new TextComponent("ID: ");
                TextComponent clickableId = new TextComponent(id == null ? "<null>" : id);
                clickableId.setColor(ChatColor.AQUA.asBungee());
                clickableId.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("クリックでコピー")}));
                clickableId.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id == null ? "null" : id));
                component.addExtra(clickableId);
                player.sendMessage(component);
            }
            return true;
        }
        return true;
    }
}
