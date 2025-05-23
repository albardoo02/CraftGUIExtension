package net.azisaba.life.utils;

import net.azisaba.life.CraftGUIExtension;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;

public class GuiUtil{

    private final CraftGUIExtension plugin;

    public GuiUtil(CraftGUIExtension plugin) {
        this.plugin = plugin;
    }

    public int getMaxPage() {
        ConfigurationSection itemsRoot = plugin.getConfig().getConfigurationSection("Items");
        int max = 1;
        if (itemsRoot != null) {
            for (String key : itemsRoot.getKeys(false)) {
                if (key.startsWith("page")) {
                    try {
                        int num = Integer.parseInt(key.replace("page", ""));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return max;
    }

    public int countMythic(Player player, String targetMMID) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            String mmid = MythicItemUtil.getMythicType(stack);
            if (targetMMID.equals(mmid)) count += stack.getAmount();
        }
        return count;
    }

    public int countVanilla(Player player, Material mat) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == mat) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    public void removeItems(Player player, boolean isMythic, String idOrMaterial, int amount) {
        PlayerInventory inv = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            if (isMythic) {
                if (!item.hasItemMeta()) continue;
                String mmid = MythicItemUtil.getMythicType(item);
                if (mmid == null || !idOrMaterial.equalsIgnoreCase(mmid)) continue;
            } else {
                if (!item.getType().name().equalsIgnoreCase(idOrMaterial)) continue;
            }

            int stackAmount = item.getAmount();
            if (stackAmount <= remaining) {
                inv.setItem(i, null);
                remaining -= stackAmount;
            } else {
                item.setAmount(stackAmount - remaining);
                inv.setItem(i, item);
                break;
            }

            if (remaining <= 0) break;
        }
    }

    public void giveMythicItem(Player player, String mmid, int amount) {
        String command = "mlg " + player.getName() + " " + mmid + " " + amount + " 1";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void giveResultItems(Player player, ConfigurationSection slotSection) {
        List<Map<?, ?>> resultItems = slotSection.getMapList("resultItems");

        for (Map<?, ?> result : resultItems) {
            boolean isMythic = Boolean.TRUE.equals(result.get("isMythicItem"));
            Object rawAmount = result.get("amount");
            if (rawAmount == null) rawAmount = result.get("amount");
            int amount = (rawAmount instanceof Number) ? ((Number) rawAmount).intValue() : 1;

            if (isMythic) {
                String mmid = (String) result.get("mmid");
                if (mmid != null) {
                    giveMythicItem(player, mmid, amount);
                }
            } else {
                String type = (String) result.get("type");
                if (type != null) {
                    Material material = Material.matchMaterial(type.toUpperCase());
                    if (material == null) {
                        player.sendMessage(ChatColor.RED + type + "は存在しないアイテムです");
                        continue;
                    }

                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&aVanilla&7] " + type + "を" + amount + "個付与できません（インベントリに空きがありません）"));
                        continue;
                    }

                    ItemStack item = new ItemStack(material, amount);
                    player.getInventory().addItem(item);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aVanilla&7] " + type + "を" + amount + "個付与しました"));
                }
            }
        }
    }
}
