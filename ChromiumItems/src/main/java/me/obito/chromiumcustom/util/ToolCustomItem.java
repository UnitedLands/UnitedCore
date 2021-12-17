package me.obito.chromiumcustom.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ToolCustomItem extends CustomItem {

    private static HashMap<String, ToolCustomItem> toolCustomItems = new HashMap<String, ToolCustomItem>();

    public ToolCustomItem(Class c, int ModelData, Material material, String name, boolean glow, String... lore) {
        super(c, ModelData, material, name, glow, lore);

        toolCustomItems.put(getKey(super.getItem()), this);
    }

    public static ToolCustomItem getItem(ItemStack i) {
        return toolCustomItems.get(getKey(i));

    }
}