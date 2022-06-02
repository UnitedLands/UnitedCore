package org.unitedlands.items.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItem {

    private static HashMap<String, CustomItem> customItems = new HashMap<String, CustomItem>();

    private ItemStack item;
    private Class clazz;

    public CustomItem(Class c, int ModelData, Material material, @NotNull String name, boolean glow, String... lore) {
        super();
        this.clazz = c;
        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(ModelData);
        if(!name.equals("") && name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        } else {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fNULL"));
        }
        if(glow) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        List<String> tempLore = new ArrayList<String>();
        if(!lore[0].equals("") && lore != null) {
            for(int i = 0; i < lore.length; i++) {
                tempLore.add(ChatColor.translateAlternateColorCodes('&', lore[i]));
            }
        }
        meta.setLore(tempLore);
        item.setItemMeta(meta);

        customItems.put(getKey(item), this);

    }

    public static String getKey(ItemStack i) {
        if(i != null)
            if(i.hasItemMeta()) {
                if(i.getItemMeta().hasCustomModelData()) {
                    return String.format("%s:%d", i.getType(), i.getItemMeta().getCustomModelData());
                }
            } else {
                return String.format("%s:0", i.getType());
            }
        return null;
    }

    public ItemStack getItem() {
        return item;
    }

    public static CustomItem getCustomItem(ItemStack item) {
        String key = getKey(item);
        return key == null ? null : customItems.get(key);
    }

    public Class getClazz() {
        return clazz;
    }

    public static CustomItem getCustomItemByName(String string) {
        String name = ChatColor.translateAlternateColorCodes('&', string);
        for (Map.Entry<String, CustomItem> e : customItems.entrySet()) {
            if(e.getValue().getItem().getItemMeta().getDisplayName().contentEquals(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    public static ItemStack getItemByName(String string) {
        CustomItem item = getCustomItemByName(string);
        if (item == null) {
            return new ItemStack(Material.AIR);
        }
        return item.getItem();
    }

    public static ItemStack getItemBySimilarName(String name) {
        for (Map.Entry<String, CustomItem> e : customItems.entrySet()) {
            String displayName = e.getValue().getItem().getItemMeta().getDisplayName();
            String itemName = ChatColor.stripColor(displayName.replaceAll(" ", "_").toLowerCase());
            if(itemName.equals(name.toLowerCase())) {
                return e.getValue().getItem();
            }
        }
        return null;
    }

    public void setItem(ItemStack item) {
        customItems.remove(getKey(this.item));
        this.item = item;
        customItems.put(getKey(this.item), this);
    }


    public static HashMap<String, CustomItem> getAllItems(){
        return customItems;
    }
}