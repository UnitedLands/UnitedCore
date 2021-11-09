package me.obito.chromiumcustom.items;

import me.obito.chromiumcustom.util.EdibleCustomItem;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public class BracketMushroom extends EdibleCustomItem {
	
	public BracketMushroom() {
		super(BracketMushroom.class, 1, Material.PUMPKIN_PIE, "&fBracket Mushroom", false, 5, 
				new ArrayList<PotionEffect>(Arrays.asList(new PotionEffect(PotionEffectType.REGENERATION, 30*3, 0))),
				"&7&oHarvested from Fungal Birch trees.");
	}
	
}
