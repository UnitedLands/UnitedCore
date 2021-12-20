package me.obito.chromiumcustom.items;

import java.util.ArrayList;
import java.util.Arrays;

import me.obito.chromiumcustom.util.EdibleCustomItem;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sausage extends EdibleCustomItem {
	
	public Sausage() {
		super(Sausage.class, 1, Material.PORKCHOP, "&fSausage", false, 20, 
				new ArrayList<PotionEffect>(Arrays.asList(new PotionEffect(PotionEffectType.CONFUSION, 30*20, 0))),
				"&e&oCan be planted?", "&7&oOddly falic.");
	}

}
