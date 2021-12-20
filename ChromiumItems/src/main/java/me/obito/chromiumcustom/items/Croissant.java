package me.obito.chromiumcustom.items;

import java.util.ArrayList;
import java.util.Arrays;

import me.obito.chromiumcustom.ChromiumItems;
import me.obito.chromiumcustom.util.EdibleCustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;

public class Croissant extends EdibleCustomItem {
	
	public Croissant() {
		super(Croissant.class, 1, Material.BREAD, "&fCroissant", false, 10, 
				new ArrayList<PotionEffect>(Arrays.asList()),
				"&7&oBetter bread");
		// create a NamespacedKey for your recipe
		NamespacedKey key = new NamespacedKey(ChromiumItems.getPlugin(ChromiumItems.class), "croissant");

		// Create our custom recipe variable
		ShapedRecipe recipe = new ShapedRecipe(key, super.getItem());

		// Here we will set the places. E and S can represent anything, and the letters can be anything. Beware; this is case sensitive.
		recipe.shape("WWW", "W W", "   ");

		// Set what the letters represent.
		// E = Emerald, S = Stick
		recipe.setIngredient('W', Material.WHEAT);

		// Finally, add the recipe to the bukkit recipes
		Bukkit.addRecipe(recipe);
	}
	
}
