package org.unitedlands.items.sapling;

import org.unitedlands.items.util.CustomItem;
import org.bukkit.Material;

public class FungalSapling extends CustomItem {

    public FungalSapling() {
        super(FungalSapling.class, 1, Material.BIRCH_SAPLING, "&fFungal Sapling", false, "&eCan be planted", "&7&oSpores waiting to grow");
    }

}