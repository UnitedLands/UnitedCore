package org.unitedlands.items.sapling;

import org.bukkit.Material;
import org.unitedlands.items.util.CustomItem;

public class AncientOak extends CustomItem {
    public AncientOak() {
        super(AncientOak.class, 2, Material.OAK_SAPLING, "&6Ancient Oak Sapling", true, "&eCan be planted", "&7&oCan only be planted by the greatest woodcutters");
    }
}
