package net.azisaba.life.utils;

import net.azisaba.itemstash.ItemStash;
import net.azisaba.life.CraftGUIExtension;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class GuiUtil{

    private final CraftGUIExtension plugin;
    private final ConfigUtil configUtil;

    public GuiUtil(CraftGUIExtension plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    public int getMaxPage() {
        if (configUtil == null) {
            return 1;
        }

        Map<String, Map<Integer, ItemUtil>> allPagesData = configUtil.loadItems();

        if (allPagesData == null || allPagesData.isEmpty()) {
            return 1;
        }

        return allPagesData.size();
    }

    public int countMythic(Player player, String targetMMID, String displayNameFromConfig) {
        return countMatchingItems(player, stack -> {
            String mmid = MythicItemUtil.getMythicType(stack);
            String displayName = null;
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                displayName = meta.getDisplayName();
            }

            if (targetMMID != null && !targetMMID.isEmpty()) {
                if (targetMMID.equals(mmid)) {
                    return true;
                }
            }

            if (displayNameFromConfig != null && !displayNameFromConfig.isEmpty()) {
                if (displayName != null) {
                    String configDisplayName = ChatColor.translateAlternateColorCodes('&',displayNameFromConfig);
                    if (configDisplayName.equals(displayName)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public int countVanilla(Player player, Material material) {
        return countMatchingItems(player, stack -> {
            ItemMeta meta = stack.getItemMeta();
            boolean hasCustomName = meta != null && meta.hasDisplayName();

            return stack.getType() == material && !hasCustomName;
        });
    }

    private int countMatchingItems(Player player, java.util.function.Predicate<ItemStack> predicate) {
        int count = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack != null && stack.getType() != Material.AIR && predicate.test(stack)) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    public void removeMythic(Player player, String mmid, String displayNameFromConfig, int amount) {
        removeItemsInternal(player, true, mmid, displayNameFromConfig, amount);
    }

    public void removeVanilla(Player player, Material material, int amount) {
        int remaining = amount;
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            ItemMeta meta = item.getItemMeta();
            boolean hasCustomName = meta != null && meta.hasDisplayName();

            if (item.getType() == material && !hasCustomName) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    inv.setItem(i, null);
                    remaining -= stackAmount;
                } else {
                    item.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }

            if (remaining <= 0) break;
        }
    }

    private void removeItemsInternal(Player player, boolean isMythic, String idOrMaterial, String displayNameFromConfig, int amount) {
        PlayerInventory inv = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inv.getSize(); i++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            String mmid = null;
            String displayName = null;
            ItemMeta meta = item.getItemMeta();

            if (isMythic) {
                mmid = MythicItemUtil.getMythicType(item);
            }
            if (meta != null && meta.hasDisplayName()) {
                displayName = meta.getDisplayName();
            }

            boolean matched = false;

            if (isMythic) {
                if (idOrMaterial != null && !idOrMaterial.isEmpty()) {
                    if (idOrMaterial.equalsIgnoreCase(mmid)) {
                        matched = true;
                    }
                }

                if (!matched && displayNameFromConfig != null && !displayNameFromConfig.isEmpty()) {
                    if (displayName != null) {
                        String configDisplayNameProcessed = ChatColor.translateAlternateColorCodes('&', displayNameFromConfig);
                        if (configDisplayNameProcessed.equals(displayName)) {
                            matched = true;
                        }
                    }
                }
            }

            if (matched) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    inv.setItem(i, null);
                    remaining -= stackAmount;
                } else {
                    item.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }

    public void giveMythicItem(Player player, String mmid, int amount) {
        String command = "mlg " + player.getName() + " " + mmid + " " + amount + " 1";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void giveMythic(Player player, String mmid, int amount) {
        giveMythicItem(player, mmid, amount);
    }

    public void giveVanilla(Player player, Material material, String displayName, int amount) {
        ItemStack item = new ItemStack(material, amount);
        if (displayName != null && !displayName.isEmpty()) {
            item.getItemMeta().setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ItemStash")) {
            ItemStash.getInstance().addItemToStash(player.getUniqueId(), new ItemStack(material, amount));
        } else {
            player.getInventory().addItem(item);
        }
    }

    public void giveResultItems(Player player, List<RequiredOrResultItem> resultItems, int craftAmount) {
        for (RequiredOrResultItem result : resultItems) {
            int totalAmount = result.getAmount() * craftAmount;

            String displayName = result.getDisplayName();

            if (result.isMythicItem()) {
                if (result.getMmid() != null) {
                    giveMythic(player, result.getMmid(), totalAmount);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                }
            } else {
                if (result.getType() == null) {
                    player.sendMessage(ChatColor.RED + "エラー: アイテムIDが設定されていないバニラアイテムです．");
                    continue;
                }
                giveVanilla(player, result.getType(), displayName, totalAmount);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aCraftGUI&7] &b" + displayName + " &7(×" + totalAmount + ")&aを付与しました"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
            }
        }
    }
}
