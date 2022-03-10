package org.unitedlands.uniteditems.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

public interface Edible {

    ItemStack getFood();
    Integer getHunger();
    ArrayList<PotionEffect> getOnEatApply() ;

}
