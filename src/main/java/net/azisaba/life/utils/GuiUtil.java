package net.azisaba.life.utils;

import net.azisaba.life.CraftGUIExtension;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
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
                        int num = Integer.parseInt(key.substring(4));
                        max = Math.max(max, num);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return max;
    }

    public int countMythic(Player player, String targetMMID) {
        return countMatchingItems(player, stack -> targetMMID.equals(MythicItemUtil.getMythicType(stack)));
    }

    public int countVanilla(Player player, Material mat) {
        return countMatchingItems(player, stack -> stack.getType() == mat);
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

    public void removeItems(Player player, boolean isMythic, String idOrMaterial, int amount) {
        PlayerInventory inv = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            boolean matched = isMythic
                    ? idOrMaterial.equalsIgnoreCase(MythicItemUtil.getMythicType(item))
                    : item.getType().name().equalsIgnoreCase(idOrMaterial);

            if (!matched) continue;

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

    /*
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
                        return;
                    }

                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&aVanilla&7] §cインベントリに空きがありません"));
                        return;
                    }

                    ItemStack item = new ItemStack(material, amount);
                    player.getInventory().addItem(item);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aVanilla&7] §a" + type + "を" + amount + "個付与しました"));
                }
            }
        }
    }
     */
    public void giveResultItems(Player player, ConfigurationSection slotSection) {
        List<Map<?, ?>> resultItems = slotSection.getMapList("resultItems");

        for (Map<?, ?> result : resultItems) {
            boolean isMythic = Boolean.TRUE.equals(result.get("isMythicItem"));
            Object rawAmount = result.get("amount");
            int amount = (rawAmount instanceof Number) ? ((Number) rawAmount).intValue() : 1;

            String displayName = result.containsKey("displayName")
                    ? ChatColor.translateAlternateColorCodes('&', result.get("displayName").toString())
                    : isMythic ? result.get("mmid").toString() : result.get("type").toString();

            if (isMythic) {
                String mmid = (String) result.get("mmid");
                if (mmid != null) {
                    giveMythicItem(player, mmid, amount);
                    player.sendMessage("§7[§bMMID§7] §a" + displayName + "を" + amount + "個付与しました");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                }
            } else {
                String type = (String) result.get("type");
                Material material = Material.matchMaterial(type.toUpperCase());
                if (material == null) {
                    player.sendMessage(ChatColor.RED + type + "は存在しないアイテムです");
                    return;
                }

                ItemStack item = new ItemStack(material, amount);
                player.getInventory().addItem(item);
                player.sendMessage("§7[§aVanilla§7] §a" + displayName + "を" + amount + "個付与しました");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
            }
        }
    }
}
