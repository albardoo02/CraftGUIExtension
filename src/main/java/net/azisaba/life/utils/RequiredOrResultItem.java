package net.azisaba.life.utils;

import org.bukkit.Material;

public class RequiredOrResultItem {

    private final boolean isMythicItem;
    private final String mmid; // MythicMobsのID
    private final Material type; // BukkitのMaterial
    private final String displayName;
    private final int amount;

    public RequiredOrResultItem(boolean isMythicItem, String mmid, Material type, String displayName, int amount) {
        this.isMythicItem = isMythicItem;
        this.mmid = mmid;
        this.type = type;
        this.displayName = displayName;
        this.amount = amount;
    }

    public boolean isMythicItem() { return isMythicItem; }
    public String getMmid() { return mmid; }
    public Material getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getAmount() { return amount; }
}
