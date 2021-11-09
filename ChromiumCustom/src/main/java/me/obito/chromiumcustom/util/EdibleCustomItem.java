package me.obito.chromiumcustom.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;

public class EdibleCustomItem extends CustomItem implements Edible {

    private static HashMap<String, EdibleCustomItem> edibleCustomItems = new HashMap<String, EdibleCustomItem>();

    private Integer hunger;
    private ArrayList<PotionEffect> onEatApply;

    public EdibleCustomItem(Class c, int ModelData, Material material, String name, boolean glow, Integer hunger, ArrayList<PotionEffect> onEatApply, String... lore) {
        super(c, ModelData, material, name, glow, lore);
        this.hunger = hunger;
        this.onEatApply = onEatApply;
        edibleCustomItems.put(getKey(getItem()), this);
    }

    public EdibleCustomItem(Class c, Material material, String name, boolean glow, Integer hunger, ArrayList<PotionEffect> onEatApply, String... lore) {
        this(c, 0, material, name, glow, hunger, onEatApply, lore);
    }

    @Override
    public Integer getHunger() {
        return hunger;
    }

    @Override
    public ArrayList<PotionEffect> getOnEatApply() {
        return onEatApply;
    }

    @Override
    public ItemStack getFood() {
        return super.getItem();
    }

    public static EdibleCustomItem getEdibleCustomItem(ItemStack item) {
        return edibleCustomItems.get(getKey(item));
    }

}

