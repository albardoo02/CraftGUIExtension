package net.azisaba.life;

import net.azisaba.life.utils.MapUtil;
import net.azisaba.life.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiListener implements Listener {

    private final CraftGUIExtension plugin;
    private final MessageUtil messageUtil;
    private final GuiHandler guiHandler;
    private final MapUtil mapUtil;

    public GuiListener(CraftGUIExtension plugin, MessageUtil messageUtil, GuiHandler guiHandler, MapUtil mapUtil) {
        this.plugin = plugin;
        this.messageUtil = messageUtil;
        this.guiHandler = guiHandler;
        this.mapUtil = mapUtil;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ClickType clickType = event.getClick();
        Location location = player.getLocation();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || !event.getView().getTitle().contains("CraftGUI Extension - Page")) {
            return;
        }
        event.setCancelled(true);

        int slot = event.getSlot();

        if (slot >= 45 && slot <= 53) {
            handleNavigationAndCloseButton(player, location, slot, currentItem);
            return;
        }

        if (slot >= 0 && slot <= 44 && currentItem != null && currentItem.getType() != Material.AIR) {
            handleItemClick(player, location, clickType, currentItem, slot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        mapUtil.removePlayerPage(event.getPlayer().getUniqueId());
    }

    private void handleNavigationAndCloseButton(Player player, Location location, int slot, ItemStack currentItem) {
        if (currentItem == null || currentItem.getType() == Material.AIR || currentItem.getItemMeta() == null || !currentItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (slot == 45) {
            handlePreviousPage(player, location);
        } else if (slot == 53) {
            handleNextPage(player, location);
        } else if (slot == 49) {
            player.closeInventory();
        }
    }

    private void handlePreviousPage(Player player, Location location) {
        int previousPage = mapUtil.getPlayerPage(player.getUniqueId()) - 1;
        if (previousPage >= 1) {
            guiHandler.openGui(player, previousPage);
            player.playSound(location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 2, 1);
        }
    }

    private void handleNextPage(Player player, Location location) {
        int nextPage = mapUtil.getPlayerPage(player.getUniqueId()) + 1;
        if (nextPage <= guiHandler.getMaxPage()) {
            guiHandler.openGui(player, nextPage);
            player.playSound(location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 2, 1);
        }
    }

    private void handleItemClick(Player player, Location location, ClickType clickType, ItemStack clickedItem, int slot) {
        int currentPage = mapUtil.getPlayerPage(player.getUniqueId());
        ConfigurationSection pageSection = plugin.getConfig().getConfigurationSection("Items.page" + currentPage);
        if (pageSection == null) return;

        ConfigurationSection slotSection = pageSection.getConfigurationSection("" + slot);
        if (slotSection == null) return;

        if (!slotSection.getBoolean("enabled")) {
            messageUtil.sendMessage(player, plugin.getConfig().getString("viewonly"));
            return;
        }

        String expectedMaterialName = slotSection.getString("material");
        String expectedDisplayName = slotSection.getString("displayName");
        String giveMythicItemID = slotSection.getString("giveMythicItemID");
        int requiredAmount = slotSection.getInt("requiredAmount");
        boolean isMythicConfig = slotSection.getBoolean("isMythicItem", false);

        if (expectedMaterialName == null || expectedMaterialName.isEmpty() || giveMythicItemID == null || giveMythicItemID.isEmpty()) return;
        Material expectedMaterial = Material.matchMaterial(expectedMaterialName.toUpperCase());
        if (expectedMaterial == null) return;

        int totalAmount = getTotalItemCount(player, expectedMaterial, isMythicConfig, expectedDisplayName);
        int maxCompressible = totalAmount / requiredAmount;

        if (totalAmount == 0) {
            player.playSound(location,Sound.ENTITY_VILLAGER_NO, 2, 1);
            if (isMythicConfig) {
                messageUtil.sendMessage(player, ChatColor.RED + expectedDisplayName + ChatColor.RED + "を持っていません");
            } else {
                messageUtil.sendMessage(player, ChatColor.RED + expectedMaterialName + ChatColor.RED + "を持っていません");
            }
        } else if (totalAmount < requiredAmount) {
            int result = requiredAmount - totalAmount;
            player.playSound(location,Sound.ENTITY_VILLAGER_NO, 2, 1);
            if (isMythicConfig) {
                messageUtil.sendMessage(player, ChatColor.RED + expectedDisplayName + ChatColor.RED + "が" + result + "個不足しています  (最小必要数: " + requiredAmount + "個)");
            } else {
                messageUtil.sendMessage(player, ChatColor.RED + expectedMaterialName + ChatColor.RED + "が" + result + "個不足しています  (最小必要数: " + requiredAmount + "個)");
            }
        } else if (player.getInventory().firstEmpty() == -1) {
            player.playSound(location,Sound.ENTITY_VILLAGER_NO, 2, 1);
            player.sendMessage(ChatColor.GOLD + "インベントリに空きがないので変換できません");
            player.closeInventory();
        } else {
            int compressTimes = (clickType == ClickType.LEFT) ? maxCompressible : 1;
            processCompression(player, clickedItem.getType(), giveMythicItemID, requiredAmount, compressTimes, isMythicConfig, expectedDisplayName);
        }
    }

    private int getTotalItemCount(Player player, Material expectedMaterial, boolean isMythicConfig, String expectedDisplayName) {
        int totalAmount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == expectedMaterial) {
                boolean isMythic = isMythicItem(item);
                boolean hasName = hasCustomName(item);
                ItemMeta meta = item.getItemMeta();
                String currentItemDisplayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null;

                if (isMythicConfig) {
                    if (isMythic && hasName && currentItemDisplayName.equals(ChatColor.translateAlternateColorCodes('&', expectedDisplayName))) {
                        totalAmount += item.getAmount();
                    }
                } else {
                    if (!isMythic && !hasName && currentItemDisplayName == null) {
                        totalAmount += item.getAmount();
                    }
                }
            }
        }
        return totalAmount;
    }

    public boolean isMythicItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        if (nmsStack.hasTag()) {
            return nmsStack.getTag().hasKey("MYTHIC_TYPE");
        }
        return false;
    }

    public boolean hasCustomName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName();
    }

    public  void processCompression(Player player, Material material, String giveMythicItemID, int requiredAmount, int times, boolean isMythicConfig, String expectedDisplayName) {
        int giveAmount = times;
        removeItems(player, material, requiredAmount * times, isMythicConfig, expectedDisplayName);
        giveCompressedItem(player, giveMythicItemID, giveAmount);
        player.sendMessage(ChatColor.GREEN + "" + times + "回圧縮しました");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
    }

    private void removeItems(Player player, Material expectedMaterial, int amountToRemove, boolean isMythicConfig, String expectedDisplayName) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() != expectedMaterial) continue;

            boolean isMythic = isMythicItem(item);
            boolean hasName = hasCustomName(item);
            String currentItemDisplayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                    ? item.getItemMeta().getDisplayName() : null;

            boolean matches = isMythicConfig
                    ? (isMythic && hasName && currentItemDisplayName.equals(ChatColor.translateAlternateColorCodes('&', expectedDisplayName)))
                    : (!isMythic && !hasName && currentItemDisplayName == null);

            if (!matches) continue;

            int amount = item.getAmount();
            if (amountToRemove >= amount) {
                player.getInventory().setItem(i, null);
                amountToRemove -= amount;
            } else {
                item.setAmount(amount - amountToRemove);
                amountToRemove = 0;
            }

            if (amountToRemove <= 0) break;
        }
    }

    private void giveCompressedItem(Player player, String mmItemID, int amount) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mlg " + player.getName() + " " + mmItemID + " " + amount + " 1");
    }

}
